package hu.jgj52.capey;

import com.mojang.authlib.GameProfile;
import hu.jgj52.capey.config.WebSocket;
import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.types.Config;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.UUID;

public class Capey implements ModInitializer {
    public static WebSocket webSocket;
    public static Config tokens;
    public static Config local;
    public static Config config;

    @Override
    public void onInitialize() {
        webSocket = new WebSocket();
        Cape.all(true);
        tokens = new Config("tokens");
        local = new Config("local");
        config = new Config("config");
    }

    private static final Minecraft mc = Minecraft.getInstance();

    public static UUID getUUID(GameProfile profile) {
        return profile.
                //? >= 1.21.10 {
                        id
                //? } else {
                /*getId
                 *///? }
                        ();
    }

    public static void setScreen(Screen screen) {
        mc.
        //? >= 26.2 {
        gui.
        //? }
        setScreen(screen);
    }
}
