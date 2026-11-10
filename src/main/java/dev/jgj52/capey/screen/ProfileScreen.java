package dev.jgj52.capey.screen;

import com.mojang.authlib.GameProfile;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Player;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class ProfileScreen extends CapeSelectorScreen {
    private final Screen parent;
    protected final GameProfile player;
    public ProfileScreen(Screen parent, GameProfile player) {
        super(Player.of(player.id()), getCapes(player), CapeSelectorScreen.selectCape); // super has to be first for java 21
        this.parent = parent;
        this.player = player;
    }
    public ProfileScreen(Screen parent, AbstractClientPlayer player) {
        this(parent, player.getGameProfile());
    }
    public ProfileScreen(Screen parent, User player) {
        this(parent, new GameProfile(player.getProfileId(), player.getName()));
    }
    private static List<Cape> getCapes(GameProfile player) {
        return Cape.all(false).stream()
                .map(cape -> Cape.of(UUID.fromString(cape.get("uuid").getAsString())))
                .filter(cape -> cape.getUploader().equals(player.id()))
                .toList();
    }

    protected int used = 0;
    protected Supplier<Cape> selected;

    @Override
    protected void createWidgets(Font font) {
        super.createWidgets(font);
        Player cplayer = Player.of(player.id());
        widget(new StringWidget(
                70 - font.width(this.player.name()) / 2,
                height / 2 - 110 - font.lineHeight,
                font.width(this.player.name()),
                font.lineHeight,
                Component.literal(this.player.name()),
                font
        ), "name", true);
        PlayerWithCapeWidget widget = widget(new PlayerWithCapeWidget(
                0,
                height / 2 - 100,
                140,
                200,
                () -> {
                    Cape cape = cplayer.getCape();
                    return cape != null ? cape.fromSkin(cplayer.getSkin()).get() : cplayer.getSkin().get();
                },
                80
        ), "skin", true);
        widget.rotateX(180);
        widget.rotateY(20);
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }
}
