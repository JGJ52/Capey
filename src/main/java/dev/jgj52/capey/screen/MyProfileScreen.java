package dev.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import dev.jgj52.capey.Capey;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.jgj52.capey.Capey.local;
import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class MyProfileScreen extends ProfileScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();

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
                                button ->
                                    setScreen(new CreateCapeScreen(this))
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

        widget(
                Button.builder(
                                Component.translatable("capey.config.my.edit"),
                                button -> {
                                    Cape selected = this.selected.get();
                                    if (selected == null) return;

                                    UUID nowUUID = mc.player != null ? mc.player.getUUID() : mc.getUser().getProfileId();
                                    if (!nowUUID.equals(selected.getUploader())) return;

                                    setScreen(new EditCapeScreen(this, selected));
                                }
                        )
                        .bounds(width - 130, height - 30, 120, 20)
                        .build()
        );

        widget(
                Button.builder(
                                Component.translatable("capey.config.my.delete"),
                                button -> {
                                    Cape selected = this.selected.get();
                                    if (selected == null) return;

                                    UUID nowUUID = mc.player != null ? mc.player.getUUID() : mc.getUser().getProfileId();
                                    if (!nowUUID.equals(selected.getUploader())) return;

                                    fetcher.submit(() -> {
                                        try {
                                            HttpRequest request = HttpRequest.newBuilder()
                                                    .uri(new URI("https://api.capey.app/v1/cape/" + selected.getUUID()))
                                                    .DELETE()
                                                    .header(
                                                            HttpHeaders.AUTHORIZATION,
                                                            Capey.tokens.get().has(nowUUID.toString())
                                                                    ? Capey.tokens.get().get(nowUUID.toString()).getAsString()
                                                                    : ""
                                                    )
                                                    .build();

                                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                                            if (response.statusCode() != 200) {
                                                System.out.println(response.statusCode());
                                                System.out.println(response.body());
                                            } else {
                                                Cape.all(true);
                                                mc.execute(() -> {
                                                    onClose();
                                                    local.get().remove(this.player.id().toString());
                                                    Player.of(this.player.id()).reFetch();
                                                    selected.unload();
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }
                        )
                        .bounds(10, height - 30, 120, 20)
                        .build()
        );
    }
}
