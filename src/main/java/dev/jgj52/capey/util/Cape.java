package dev.jgj52.capey.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.core.ClientAsset;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Cape {
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Gson gson = new Gson();
    private static final Map<UUID, Cape> capes = new ConcurrentHashMap<>();
    public static Cape of(UUID uuid) {
        return capes.computeIfAbsent(uuid, Cape::new);
    }
    private static List<JsonObject> all;
    public static List<JsonObject> all(boolean reload) {
        if (!reload) return all;
        List<JsonObject> capes = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.capey.app/v1/capes"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                array.forEach(s -> capes.add(s.getAsJsonObject()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        all = capes;
        return capes;
    }
    public static void add(JsonObject cape) {
        all.add(cape);
    }
    private static final Semaphore semaphore = new Semaphore(5);

    private final UUID uuid;
    private NativeImage image;
    private Identifier identifier;
    private volatile String name;
    private volatile boolean elytra;
    private volatile UUID uploader;

    private Cape(UUID uuid) {
        this.uuid = uuid;
        reFetch();
    }

    public String getName() {
        return name;
    }

    public boolean changesElytra() {
        return elytra;
    }

    public UUID getUploader() {
        return uploader;
    }

    public NativeImage getImage() {
        return image;
    }

    public void reFetch() {
        fetcher.submit(() -> {
            try {
                semaphore.acquire();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://api.capey.app/v1/cape/" + uuid))
                        .GET()
                        .build();

                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 404) {
                    mc.execute(() -> identifier = null);
                }
                if (response.statusCode() == 200) {
                    mc.execute(() -> {
                        try {
                            Identifier identifier = Identifier.fromNamespaceAndPath("capey", "capes/" + uuid);
                            this.identifier = identifier;
                            unload();

                            if (image != null) image.close();
                            image = NativeImage.read(response.body());
                            DynamicTexture texture = new DynamicTexture(() -> "capey:capes/" + uuid, image);

                            mc.getTextureManager().register(identifier, texture);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                name = response.headers().firstValue("X-Cape-Name").orElse("");
                elytra = response.headers().firstValue("X-Cape-Change-Elytra").map(Boolean::parseBoolean).orElse(true);
                uploader = response.headers().firstValue("X-Capey-Uploader").map(UUID::fromString).orElse(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                semaphore.release();
            }
        });
    }

    public void unload() {
        mc.getTextureManager().release(identifier);
    }

    public Supplier<PlayerSkin> fromSkin(Supplier<PlayerSkin> original) {
        return () -> {
            PlayerSkin orig = original.get();
            if (getIdentifier() == null) return orig;
            return new PlayerSkin(
                    orig.body(),
                    new TextureImpl(getIdentifier()), changesElytra() ? new TextureImpl(getIdentifier()) : orig.elytra() == null ? orig.cape() : orig.elytra(),
                    orig.model(),
                    orig.secure()
            );
        };
    }

    public static Supplier<PlayerSkin> emptySkin(Identifier identifier) {
        return () -> new PlayerSkin(
                new TextureImpl(Identifier.fromNamespaceAndPath("capey", "skins/empty.png")),
                new TextureImpl(identifier), new TextureImpl(identifier),
                PlayerModelType.SLIM,
                false
        );
    }

    public record TextureImpl(Identifier identifier) implements ClientAsset.Texture {
        @Override
        public @NotNull Identifier texturePath() {
            return identifier;
        }

        @Override
        public @NotNull Identifier id() {
            return identifier;
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
