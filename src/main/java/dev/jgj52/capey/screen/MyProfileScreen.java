package dev.jgj52.capey.screen;

import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class MyProfileScreen extends ProfileScreen {
    public MyProfileScreen(Screen parent, AbstractClientPlayer player) {
        super(parent, player);
    }
    public MyProfileScreen(Screen parent, User player) {
        super(parent, player);
    }

    @Override
    protected void createWidgets(Font font) {
        super.createWidgets(font);
        widget(
                Button.builder(
                                Component.translatable("capey.config.my.create"),
                                button -> setScreen(new CreateCapeScreen(this))
                        )
                        .bounds(width - 90, 10, 80, 20)
                        .build(),
                "create",
                true
        );
        Component remaining = Component.translatable("capey.config.my.remaining", used, 5);
        widget(new StringWidget(
                70 - font.width(remaining) / 2,
                height / 2 + 110 + font.lineHeight,
                font.width(remaining),
                font.lineHeight,
                remaining,
                font
        ), "remaining", true);
    }
}
