package dev.jgj52.capey.screen;

import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Player;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class ConfigScreen extends CapeSelectorScreen {
    private static final Minecraft mc = Minecraft.getInstance();

    private final Screen parent;
    public ConfigScreen(Screen parent) {
        super(
                Player.of(Player.getCurrentUUID()),
                Optional.of(Cape.all(false).stream().map(cape -> Cape.of(UUID.fromString(cape.get("uuid").getAsString()))).toList()).orElse(List.of()),
                CapeSelectorScreen.selectCape
        );
        this.parent = parent;
    }

    @Override
    protected void createWidgets(Font font) {
        super.createWidgets(font);
        widget(
                Button.builder(
                                Component.translatable("capey.config.main.reload"),
                                button -> CompletableFuture.supplyAsync(() -> Cape.all(true)).thenAccept(capeList -> setScreen(new ConfigScreen(parent)))
                        )
                        .bounds(10, 10, 80, 20)
                        .build(),
                "reload",
                true
        );
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
                                Component.translatable("capey.config.main.profile"),
                                button -> setScreen(mc.player != null ? new MyProfileScreen(this, mc.player) : new MyProfileScreen(this, mc.getUser()))
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