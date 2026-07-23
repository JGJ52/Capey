package dev.jgj52.capey;

import dev.jgj52.capey.config.WebSocket;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.screenapi.util.Config;
import net.fabricmc.api.ModInitializer;

public class Capey implements ModInitializer {
    public static WebSocket webSocket;
    public static Config tokens;
    public static Config local;

    @Override
    public void onInitialize() {
        webSocket = new WebSocket();
        Cape.all(true);
        tokens = new Config("tokens", "capey");
        local = new Config("local", "capey");
    }
}
