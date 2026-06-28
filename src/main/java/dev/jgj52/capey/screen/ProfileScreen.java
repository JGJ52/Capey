package dev.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import dev.jgj52.capey.Capey;
import dev.jgj52.capey.types.Cape;
import dev.jgj52.capey.types.Player;
import dev.jgj52.capey.version.VGameProfile;
import dev.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
//? >= 1.21.9 {
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.input.MouseButtonEvent;
//? } else {
/*import net.minecraft.client.resources.PlayerSkin;
*///? }
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static dev.jgj52.capey.version.VGameProfile.getName;
import static dev.jgj52.capey.version.VScreen.setScreen;

public class ProfileScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();

    private final Screen parent;
    private final VGameProfile player;
    public ProfileScreen(Screen parent, AbstractClientPlayer player) {
        this.parent = parent;
        this.player = new VGameProfile(player.getUUID(), getName(player.getGameProfile()));
    }
    public ProfileScreen(Screen parent, User player) {
        this.parent = parent;
        this.player = new VGameProfile(player.getProfileId(), player.getName());
    }

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    protected int used = 0;

    @Override
    protected void createWidgets(Font font) {
        Player cplayer = Player.of(player.getUUID());
        Cape ccape = cplayer.getCape();
        widget(new StringWidget(
                70 - font.width(this.player.name()) / 2,
                height / 2 - 110 - font.lineHeight,
                font.width(this.player.name()),
                font.lineHeight,
                this.player.name(),
                font
        ), "name", true);
        PlayerWithCapeWidget widget = widget(new PlayerWithCapeWidget(
                0,
                height / 2 - 100,
                140,
                200,
                ccape != null ? ccape.fromSkin(cplayer.getSkin()) : cplayer.getSkin(),
                80
        ), "skin", true);
        widget.rotateX(180);
        widget.rotateY(20);

        AtomicInteger offset = new AtomicInteger();
        int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
        Player local = Player.of(this.player.getUUID());
        Supplier<Cape> selected = local::getCape;
        List<PlayerWithCapeWidget> all = new ArrayList<>();
        all.add(widget);
        Cape.all(false).forEach(capeO -> {
            Player player = Player.of(UUID.fromString(capeO.get("uploader").getAsString()));
            if (player != cplayer) return;
            Cape cape = Cape.of(UUID.fromString(capeO.get("uuid").getAsString()));
            Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

            int i = offset.getAndIncrement();
            VGameProfile tPlayer = this.player;
            all.add(widget(new PlayerWithCapeWidget(
                    (i % perRow) * 80 + (width - perRow * 80) / 2,
                    (i / perRow) * 100 + 50,
                    70,
                    100,
                    skinWithCape
            ) {
                @Override
                protected void renderPlayer(
                        @NotNull
                        net.minecraft.client.gui.
                        //? >= 26.1 {
                        GuiGraphicsExtractor
                         //? } else {
                        /*GuiGraphics
                        *///? }
                        graphics, int mouseX, int mouseY, float a
                ) {
                    super.renderPlayer(graphics, mouseX, mouseY, a);

                    if (selected.get() == cape && !background()) {
                        all.forEach(w -> w.background(false));
                        background(true);
                    } else if (selected.get() == null && background()) {
                        background(false);
                    }
                }

                @Override
                public void onClick(
                        //? >= 1.21.9 {
                        @NotNull MouseButtonEvent event, boolean doubleClick
                         //? } else {
                        /*double x, double y
                        *///? }
                ) {
                    String nowUUID = tPlayer.getUUID().toString();
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
            used++;
        });
        widget(
                Button.builder(
                                Component.translatable("capey.config.main.elytra"),
                                button -> PlayerWithCapeWidget.FakePlayer.elytra()
                        )
                        .bounds(10, 10, 80, 20)
                        .build(),
                "elytra",
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
