package dev.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;
import dev.jgj52.capey.Capey;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Player;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import dev.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class ConfigScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();

    private final Screen parent;
    public ConfigScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    private int perRow;
    private int visCol;
    private UUID localUUID;
    private Player local;
    private Supplier<Cape> selected;
    List<PlayerWithCapeWidget> all;
    private List<JsonObject> capes;

    @Override
    protected void createWidgets(Font font) {
        perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
        visCol = mc.getWindow().getGuiScaledHeight() / 100;
        localUUID = mc.player != null ? mc.player.getUUID() : mc.getUser().getProfileId();
        local = Player.of(localUUID);
        selected = local::getCape;
        all = new ArrayList<>();
        capes = Cape.all(false);
        capes.subList(all.size(), Math.min(visCol * perRow + all.size(), capes.size())).forEach(this::addCape);
        widget(
                Button.builder(
                        Component.translatable("capey.config.main.elytra"),
                        button -> PlayerWithCapeWidget.FakePlayer.elytra()
                )
                .bounds(10, 40, 80, 20)
                .build(),
            "elytra",
            true
        );
        widget(
                Button.builder(
                                Component.translatable("capey.config.main.reload"),
                                button ->
                                    CompletableFuture.supplyAsync(() ->
                                        Cape.all(true)).thenAccept(capeList ->
                                        mc.execute(this::onClose)
                                    )
                        )
                        .bounds(10, 10, 80, 20)
                        .build(),
                "reload",
                true
        );
        widget(
                Button.builder(
                                Component.translatable("capey.config.main.profile"),
                                button ->
                                    setScreen(mc.player != null ? new MyProfileScreen(this, mc.player) : new MyProfileScreen(this, mc.getUser()))
                        )
                        .bounds(width - 90, 10, 80, 20)
                        .build(),
                "profile",
                true
        );
        if (PlayerWithCapeWidget.FakePlayer.ely().isEmpty()) {
            Component level = Component.translatable("capey.config.main.level").withColor(0x00ff0000);
            int w = Math.min(
                    font.width(level),
                    width - 200
            );
            widget(new StringWidget(
                    width / 2 - w / 2,
                    10 + font.lineHeight,
                    w,
                    font.lineHeight,
                    level,
                    font
            ), null, true);
        }
    }

    private void addCape(JsonObject capeO) {
        Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
        Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
        Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

        int i = all.size();
        all.add(widget(new PlayerWithCapeWidget(
                (i % perRow) * 80 + (width - perRow * 80) / 2,
                (i / perRow) * 100 + 50 + getScrollOffset(),
                70,
                100,
                skinWithCape
        ) {
            @Override
            protected void renderPlayer(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                super.renderPlayer(graphics, mouseX, mouseY, a);

                Cape current = selected.get();
                background(current != null && current.getUUID().equals(cape.getUUID()));

                super.renderPlayer(graphics, mouseX, mouseY, a);
            }

            @Override
            public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
                String nowUUID = localUUID.toString();
                String capeUUID = cape.getUUID().toString();
                if (background()) Capey.local.get().remove(nowUUID);
                else Capey.local.get().addProperty(nowUUID, capeUUID);
                Capey.local.save();
                local.reFetch();
                HttpRequest.BodyPublisher body = background()
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(capeUUID);
                fetcher.submit(() -> {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI("https://api.capey.app/v1/player"))
                                .POST(body)
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        Capey.tokens.get().has(nowUUID)
                                                ? Capey.tokens.get().get(nowUUID).getAsString()
                                                : ""
                                )
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() != 200) {
                            System.out.println(response.body());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }));
        Component name = Component.literal(capeO.get("name").getAsString());
        int add = font.width(name) > 70 ? 2 : 35 - font.width(name) / 2;
        widget(new StringWidget(
                (i % perRow) * 80 + (width - perRow * 80) / 2 + add,
                (i / perRow) * 100 + 90 + 50 + getScrollOffset(),
                70,
                font.lineHeight,
                name,
                font
        ));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int contentBottom = all.size() / perRow * 100;

        int viewportBottom = height - getScrollOffset();

        if (contentBottom - viewportBottom < 200) {
            capes.subList(all.size(), Math.min(visCol * perRow + all.size(), capes.size())).forEach(this::addCape);
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }
}