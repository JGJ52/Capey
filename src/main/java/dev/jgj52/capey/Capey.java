package dev.jgj52.capey;

import dev.jgj52.capey.config.WebSocket;
import dev.jgj52.capey.types.Cape;
import dev.jgj52.capey.types.Config;
import net.fabricmc.api.ModInitializer;

public class Capey implements ModInitializer {
    public static WebSocket webSocket;
    public static Config tokens;
    public static Config local;
    public static Config config;
    public static Config capes;

    @Override
    public void onInitialize() {
        webSocket = new WebSocket();
        Cape.all(true);
        tokens = new Config("tokens");
        local = new Config("local");
        config = new Config("config");
        capes = new Config("capes");
    }
}
