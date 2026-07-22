package dev.jgj52.capey.screen;

import com.google.common.net.HttpHeaders;
import com.mojang.blaze3d.platform.NativeImage;
import dev.jgj52.capey.Capey;
import dev.jgj52.capey.types.Cape;
import dev.jgj52.capey.widget.BoardWidget;
import dev.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static dev.jgj52.capey.version.VScreen.setScreen;

public class EditCapeScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ExecutorService fetcher = Executors.newVirtualThreadPerTaskExecutor();

    private final Screen parent;
    private final Cape cape;
    public EditCapeScreen(Screen parent, Cape cape) {
        this.parent = parent;
        this.cape = cape;
    }

    private DynamicTexture texture;
    private Supplier<Integer> colorSupply = () -> 0;

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    @Override
    public void render(@NotNull GuiGraphicsExtractor graphics, int i, int i1, float v) {
        super.render(graphics, i, i1, v);
        graphics.fill(
                10,
                height - 140,
                80,
                height - 60,
                0x80000000
        );

        graphics.fill(
                10,
                40,
                97,
                50,
                colorSupply.get()
        );
    }

    private static final Identifier previewId = Identifier.fromNamespaceAndPath("capey", "preview");

    @Override
    protected void createWidgets(Font font) {
        PlayerWithCapeWidget cw = widget(new PlayerWithCapeWidget(
                10,
                height - 140,
                70,
                80,
                Cape.emptySkin(previewId)
        ), "preview", true);
        cw.offset(0.85f);
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

        try {
            NativeImage image = NativeImage.read(Files.newInputStream(cape.getPath()));

            int W = image.getWidth();
            int H = image.getHeight();

            BoardWidget board = widget(new BoardWidget(
                    width / 2 - W * BoardWidget.scale / 2,
                    60,
                    W,
                    H
            ) {
                @Override
                protected void renderBoard(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    super.renderBoard(graphics, mouseX, mouseY, a);
                    // why does this take so long
                    // i spent 45 minutes on this

                    int width = w / 64 * 10;
                    int height = h / 32 * 16;

                    // top
                    graphics.fill(
                            getX() + scale + 1,
                            getY(),
                            getX() + scale * (width + 1) - 1,
                            getY() + 1,
                            0xff00ff00
                    );
                    // left
                    graphics.fill(
                            getX() + scale,
                            getY(),
                            getX() + scale + 1,
                            getY() + scale,
                            0xff00ff00
                    );
                    // bottom
                    graphics.fill(
                            getX() + scale + 1,
                            getY() + scale - 1,
                            getX() + scale * (width + 1) - 1,
                            getY() + scale,
                            0xff00ff00
                    );
                    // right
                    graphics.fill(
                            getX() + scale * (width + 1) - 1,
                            getY(),
                            getX() + scale * (width + 1),
                            getY() + scale,
                            0xff00ff00
                    );

                    // top
                    graphics.fill(
                            getX() + scale * (width + 1) + 1,
                            getY(),
                            getX() + scale * (width * 2 + 1) - 1,
                            getY() + 1,
                            0xff0000ff
                    );
                    // left
                    graphics.fill(
                            getX() + scale * (width + 1),
                            getY(),
                            getX() + scale * (width + 1) + 1,
                            getY() + scale,
                            0xff0000ff
                    );
                    // bottom
                    graphics.fill(
                            getX() + scale * (width + 1) + 1,
                            getY() + scale - 1,
                            getX() + scale * (width * 2 + 1) - 1,
                            getY() + scale,
                            0xff0000ff
                    );
                    // right
                    graphics.fill(
                            getX() + scale * (width * 2 + 1) - 1,
                            getY(),
                            getX() + scale * (width * 2 + 1),
                            getY() + scale,
                            0xff0000ff
                    );

                    // top
                    graphics.fill(
                            getX(),
                            getY() + scale,
                            getX() + scale,
                            getY() + scale + 1,
                            0xffff0000
                    );
                    // left
                    graphics.fill(
                            getX(),
                            getY() + scale + 1,
                            getX() + 1,
                            getY() + scale * (height + 1) - 1,
                            0xffff0000
                    );
                    // bottom
                    graphics.fill(
                            getX(),
                            getY() + scale * (height + 1) - 1,
                            getX() + scale,
                            getY() + scale * (height + 1),
                            0xffff0000
                    );
                    // right
                    graphics.fill(
                            getX() + scale - 1,
                            getY() + scale + 1,
                            getX() + scale,
                            getY() + scale * (height + 1) - 1,
                            0xffff0000
                    );

                    // what color now

                    // top
                    graphics.fill(
                            getX() + scale,
                            getY() + scale,
                            getX() + scale * (width + 1),
                            getY() + scale + 1,
                            0xffffff00
                    );
                    // left
                    graphics.fill(
                            getX() + scale,
                            getY() + scale + 1,
                            getX() + scale + 1,
                            getY() + scale * (height + 1) - 1,
                            0xffffff00
                    );
                    // bottom
                    graphics.fill(
                            getX() + scale,
                            getY() + scale * (height + 1) - 1,
                            getX() + scale * (width + 1),
                            getY() + scale * (height + 1),
                            0xffffff00
                    );
                    // right
                    graphics.fill(
                            getX() + scale * (width + 1) - 1,
                            getY() + scale + 1,
                            getX() + scale * (width + 1),
                            getY() + scale * (height + 1) - 1,
                            0xffffff00
                    );

                    // top
                    graphics.fill(
                            getX() + scale * (width + 1),
                            getY() + scale,
                            getX() + scale * (width + 2),
                            getY() + scale + 1,
                            0xffff00ff
                    );
                    // left
                    graphics.fill(
                            getX() + scale * (width + 1),
                            getY() + scale + 1,
                            getX() + scale * (width + 1) + 1,
                            getY() + scale * (height + 1) - 1,
                            0xffff00ff
                    );
                    // bottom
                    graphics.fill(
                            getX() + scale * (width + 1),
                            getY() + scale * (height + 1) - 1,
                            getX() + scale * (width + 2),
                            getY() + scale * (height + 1),
                            0xffff00ff
                    );
                    // right
                    graphics.fill(
                            getX() + scale * (width + 2) - 1,
                            getY() + scale + 1,
                            getX() + scale * (width + 2),
                            getY() + scale * (height + 1) - 1,
                            0xffff00ff
                    );

                    // top
                    graphics.fill(
                            getX() + scale * (width + 2),
                            getY() + scale,
                            getX() + scale * (width * 2 + 2),
                            getY() + scale + 1,
                            0xff00ffff
                    );
                    // left
                    graphics.fill(
                            getX() + scale * (width + 2),
                            getY() + scale + 1,
                            getX() + scale * (width + 2) + 1,
                            getY() + scale * (height + 1) - 1,
                            0xff00ffff
                    );
                    // bottom
                    graphics.fill(
                            getX() + scale * (width + 2),
                            getY() + scale * (height + 1) - 1,
                            getX() + scale * (width * 2 + 2),
                            getY() + scale * (height + 1),
                            0xff00ffff
                    );
                    // right
                    graphics.fill(
                            getX() + scale * (width * 2 + 2) - 1,
                            getY() + scale + 1,
                            getX() + scale * (width * 2 + 2),
                            getY() + scale * (height + 1) - 1,
                            0xff00ffff
                    );
                }
            }, "board", true);

            board.setPixels(image.getPixels());

            board.onDraw(pixel ->
                image.setPixel(
                        pixel % W,
                        pixel / W,
                        board.color()
                )
            );

            colorSupply = board::color;

            Component componentH = Component.literal("#");
            widget(new StringWidget(
                    10,
                    42 + font.lineHeight * 10,
                    font.width(componentH),
                    font.lineHeight,
                    componentH,
                    font
            ));
            EditBox hex = widget(new EditBox(
                    font,
                    12 + font.width(componentH),
                    40 + font.lineHeight * 10,
                    80,
                    font.lineHeight + 2,
                    Component.empty()
            ));
            hex.setValue("ffffff");

            AtomicInteger a = new AtomicInteger(255);
            AtomicInteger r = new AtomicInteger(255);
            AtomicInteger g = new AtomicInteger(255);
            AtomicInteger b = new AtomicInteger(255);

            List<Slider> sliders = new ArrayList<>();

            int i = -1;
            for (AtomicInteger integer : List.of(a, r, g, b)) {
                i++;
                Component component = Component.literal("ARGB".substring(i, i + 1));
                widget(new StringWidget(
                        10,
                        40 + font.lineHeight * 2 * (i + 1) + 2,
                        font.width(component),
                        font.lineHeight,
                        component,
                        font
                ));

                sliders.add(widget(new Slider(
                        12 + font.width(component),
                        40 + font.lineHeight * 2 * (i + 1),
                        80,
                        font.lineHeight + 2,
                        Component.literal(String.valueOf(integer.get())),
                        (double) integer.get() / 255,
                        integer
                ) {
                    @Override
                    protected void updateMessage() {
                        setMessage(Component.literal(String.valueOf(integer.get())));
                    }

                    @Override
                    protected void applyValue() {
                        int val = (int) (value * 255);
                        integer.set(val);
                        int A = Math.clamp(a.get(), 0, 255);
                        int R = Math.clamp(r.get(), 0, 255);
                        int G = Math.clamp(g.get(), 0, 255);
                        int B = Math.clamp(b.get(), 0, 255);

                        board.color(
                                (A << 24) | (R << 16) | (G << 8) | B
                        );
                        if (!hex.isFocused()) hex.setValue(
                                a.get() == 255
                                        ? String.format("%06x", board.color() & 0xFFFFFF)
                                        : String.format("%08x", board.color())
                        );
                    }
                }));
            }
            hex.setResponder(s -> {
                if (!hex.isFocused()) return;
                int h;
                try {
                    h = (int) Long.parseLong(s, 16);
                } catch (NumberFormatException ignored) {
                    return;
                }

                int A = (h >> 24) & 0xFF;

                a.set(A == 0 && s.length() == 6 ? 255 : A);
                r.set((h >> 16) & 0xFF);
                g.set((h >> 8) & 0xFF);
                b.set(h & 0xFF);
                sliders.forEach(slider -> slider.setValue((double) slider.integer().get() / 255));
            });

            preview(board, W, H);
            board.onDraw(pixel -> preview(board, W, H));

            EditBox name = widget(new EditBox(
                    font,
                    width - 90,
                    height - 60,
                    80,
                    20,
                    Component.empty()
            ));
            name.setValue(cape.getName());

            AtomicBoolean elytra = new AtomicBoolean(cape.changesElytra());
            widget(
                    Button.builder(
                                    Component.translatable("capey.config.edit.save"),
                                    button -> {
                                        try {
                                            Path temp = Files.createTempFile("capey", ".png");
                                            image.writeToFile(temp);

                                            byte[] png = Files.readAllBytes(temp);
                                            Files.deleteIfExists(temp);

                                            fetcher.submit(() -> {
                                                try {
                                                    String nowUUID = mc.player != null ? mc.player.getUUID().toString() : mc.getUser().getProfileId().toString();
                                                    HttpRequest request = HttpRequest.newBuilder()
                                                            .uri(new URI("https://api.capey.app/v1/cape/" + cape.getUUID()))
                                                            .POST(
                                                                    HttpRequest.BodyPublishers.ofByteArray(png)
                                                            )
                                                            .header(
                                                                    HttpHeaders.AUTHORIZATION,
                                                                    Capey.tokens.get().has(nowUUID)
                                                                            ? Capey.tokens.get().get(nowUUID).getAsString()
                                                                            : ""
                                                            )
                                                            .header(
                                                                    HttpHeaders.CONTENT_TYPE,
                                                                    "image/png"
                                                            )
                                                            .header(
                                                                    "X-Cape-Name",
                                                                    name.getValue()
                                                            )
                                                            .header(
                                                                    "X-Cape-Change-Elytra",
                                                                    elytra.toString()
                                                            )
                                                            .build();

                                                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                                                    if (response.statusCode() != 200) {
                                                        System.out.println(response.statusCode());
                                                        System.out.println(response.body());
                                                    } else {
                                                        cape.setName(name.getValue());
                                                        cape.changesElytra(elytra.get());
                                                        image.writeToFile(cape.getPath());
                                                        Cape.all(true);
                                                        mc.execute(this::onClose);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            )
                            .bounds(width - 90, height - 30, 80, 20)
                            .build(),
                    "save",
                    true
            );

            widget(
                    Button.builder(
                                    Component.translatable("capey.config.create.elytra", elytra.toString()),
                                    button -> {
                                        elytra.set(!elytra.get());
                                        button.setMessage(Component.translatable("capey.config.create.elytra", elytra.toString()));
                                    }
                            )
                            .bounds(width - 300, height - 30, 200, 20)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static abstract class Slider extends AbstractSliderButton {
        private final AtomicInteger integer;
        public Slider(int x, int y, int width, int height, Component message, double initialValue, AtomicInteger integer) {
            super(x, y, width, height, message, initialValue);
            this.integer = integer;
        }

        //? >= 1.21.11 {
        @Override
                //? }
        public void setValue(double newValue) {
            //? >= 1.21.11 {
            super.setValue(newValue);
            //? } else {
            /*value = newValue;
             *///? }
        }

        public AtomicInteger integer() {
            return integer;
        }
    }

    private void preview(BoardWidget board, int w, int h) {
        if (texture != null) texture.close();
        try (NativeImage image = new NativeImage(w, h, false)) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    image.setPixel(x, y, board.getColor(y * w + x));
                }
            }
            texture = new DynamicTexture(() -> "capey:preview", image);
            mc.getTextureManager().register(Identifier.fromNamespaceAndPath("capey", "preview"),
                    texture
            );
            texture.upload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose() {
        if (texture != null) texture.close();
        mc.getTextureManager().release(previewId);
        setScreen(parent);
    }
}
// why is this so long