package hu.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import hu.jgj52.capey.Capey;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Player;
import hu.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jspecify.annotations.NonNull;

import java.awt.*;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static hu.jgj52.capey.version.VScreen.setScreen;

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

    @Override
    public void render(GuiGraphicsExtractor graphics, int i, int i1, float v) {

    }

    @Override
    protected void createWidgets(Font font) {
        AtomicInteger offset = new AtomicInteger();
        int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
        UUID localUUID = mc.player != null ? mc.player.getUUID() : mc.getUser().getProfileId();
        Player local = Player.of(localUUID);
        Supplier<Cape> selected = local::getCape;
        List<PlayerWithCapeWidget> all = new ArrayList<>();
        Cape.all(false).forEach(capeO -> {
            Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
            Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
            Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

            int i = offset.getAndIncrement();
            all.add(widget(new PlayerWithCapeWidget(
                    (i % perRow) * 80 + (width - perRow * 80) / 2,
                    (i / perRow) * 100 + 50,
                    70,
                    100,
                    skinWithCape
            ) {
                @Override
                protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

                    if (selected.get() == cape && !background()) {
                        all.forEach(w -> w.background(false));
                        background(true);
                    } else if (selected.get() == null && background()) {
                        background(false);
                    }
                }

                @Override
                public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
                    String nowUUID = localUUID.toString();
                    String capeUUID = cape.getUUID().toString();
                    if (background()) Capey.local.get().remove(nowUUID);
                    else Capey.local.get().addProperty(nowUUID, capeUUID);
                    Capey.local.save();
                    local.reFetch();
                    fetcher.submit(() -> {
                        try {
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(new URI("https://api.capey.app/v1/player"))
                                    .POST(
                                            background()
                                                    ? HttpRequest.BodyPublishers.noBody()
                                                    : HttpRequest.BodyPublishers.ofString(capeUUID)
                                    )
                                    .header(
                                            HttpHeaders.AUTHORIZATION,
                                            Capey.tokens.get().has(nowUUID)
                                                    ? Capey.tokens.get().get(nowUUID).getAsString()
                                                    : ""
                                    )
                                    .build();

                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                            if (response.statusCode() != 200) {
                                throw new RuntimeException(response.body());
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
                    (i / perRow) * 100 + 90 + 50,
                    70,
                    font.lineHeight,
                    name,
                    font
            ));
        });
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
                                        Cape.all(true)).thenAccept(capes ->
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

    @Override
    public void onClose() {
        setScreen(parent);
    }
}