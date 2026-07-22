package dev.jgj52.capey.types;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import dev.jgj52.capey.Capey;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.core.ClientAsset;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final Semaphore semaphore = new Semaphore(5);

    private final UUID uuid;
    private final Path path;
    private volatile Identifier identifier;
    private volatile String name;
    private volatile boolean elytra;
    private volatile UUID uploader;

    private Cape(UUID uuid) {
        this.uuid = uuid;
        path = Path.of(FabricLoader.getInstance().getGameDir().toString(), "capey", uuid.toString());
        File file = new File(path.toUri());
        if (file.exists()) {
            registerTextures();
            JsonObject obj = Capey.capes.get(uuid.toString()).getAsJsonObject();
            name = obj.get("name").getAsString();
            elytra = obj.get("elytra").getAsBoolean();
            uploader = UUID.fromString(obj.get("uploader").getAsString());
        } else {
            file.getParentFile().mkdirs();
            fetcher.submit(() -> {
                try {
                    semaphore.acquire();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("https://api.capey.app/v1/cape/" + uuid))
                            .GET()
                            .build();

                    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    if (response.statusCode() == 404) {
                        identifier = null;
                    }
                    if (response.statusCode() == 200) {
                        Files.write(path, response.body());
                        registerTextures();
                    }
                    name = response.headers().firstValue("X-Cape-Name").orElse("");
                    elytra = response.headers().firstValue("X-Cape-Change-Elytra").map(Boolean::parseBoolean).orElse(true);
                    uploader = response.headers().firstValue("X-Capey-Uploader").map(UUID::fromString).orElse(null);

                    JsonObject obj = new JsonObject();
                    obj.addProperty("name", name);
                    obj.addProperty("elytra", elytra);
                    obj.addProperty("uploader", uploader.toString());

                    Capey.capes.set(uuid.toString(), obj);
                    Capey.capes.save();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    semaphore.release();
                }
            });
        }
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

    public void setName(String name) {
        Capey.capes.get(uuid.toString()).getAsJsonObject().addProperty("name", name);
        Capey.capes.save();
        this.name = name;
    }

    public void changesElytra(boolean elytra) {
        Capey.capes.get(uuid.toString()).getAsJsonObject().addProperty("elytra", elytra);
        Capey.capes.save();
        this.elytra = elytra;
    }

    public Path getPath() {
        return path;
    }

    private void registerTextures() {
        mc.execute(() -> {
            try {
                NativeImage img = NativeImage.read(Files.newInputStream(path));
                Identifier identifier = Identifier.fromNamespaceAndPath("capey", "capes/" + uuid);
                DynamicTexture texture = new DynamicTexture(() -> "capey:capes/" + uuid, img);

                mc.getTextureManager().register(identifier, texture);
                texture.upload();
                this.identifier = identifier;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Supplier<PlayerSkin> fromSkin(Supplier<PlayerSkin> original) {
        return () -> {
            PlayerSkin orig = original.get();
            if (getIdentifier() == null) return orig;
            return new PlayerSkin(
                    orig.body(),
                    new TextureImpl(getIdentifier()), changesElytra() ? new TextureImpl(getIdentifier()) : orig.elytra(),
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
