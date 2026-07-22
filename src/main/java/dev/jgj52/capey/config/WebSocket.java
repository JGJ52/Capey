package dev.jgj52.capey.config; // idk why put this here

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.jgj52.capey.types.Cape;
import dev.jgj52.capey.types.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocket implements java.net.http.WebSocket.Listener {
    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newHttpClient();
    private java.net.http.WebSocket webSocket;

    public WebSocket() {
        try {
            client.newWebSocketBuilder()
                    .buildAsync(URI.create("wss://api.capey.app/v1"), this)
                    .whenComplete((ws, throwable) -> {
                        if (throwable != null) {
                            System.err.println("Failed to connect to Capey API: " + throwable.getMessage());
                            return;
                        }

                        this.webSocket = ws;
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<java.net.http.WebSocket> sendText(CharSequence text, boolean b) {
        return webSocket.sendText(text, b);
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        java.net.http.WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        String message = data.toString();
        String[] strings = message.split(" ");
        switch (strings[0]) {
            case "cape": {
                UUID uuid = UUID.fromString(strings[1]);
                Player.of(uuid).reFetch();
                break;
            }
            case "update": {
                UUID uuid = UUID.fromString(strings[1]);
                Cape.of(uuid).reFetch();
                break;
            }
            case "new": {
                JsonObject cape = gson.fromJson(message.split(" ", 2)[1], JsonObject.class);
                Cape.add(cape);
            }
        }
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}
