package dev.jgj52.capey.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Drawer;
import dev.jgj52.screenapi.screen.BetterScreen;
import dev.jgj52.screenapi.util.DrawHelper;
import dev.jgj52.screenapi.widget.BoardWidget;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CapeEditorScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Identifier previewId = Identifier.fromNamespaceAndPath("capey", "preview");

    @FunctionalInterface
    public interface onSaveI {
        void accept(byte[] png, String name, boolean elytra);
    }

    private final NativeImage defaultImage;
    private final String defaultName;
    private final boolean defaultElytra;
    private final onSaveI onSaveE;
    private DynamicTexture texture;
    private Supplier<Integer> colorSupply = () -> 0;
    public CapeEditorScreen(NativeImage defaultImage, String defaultName, boolean defaultElytra, onSaveI onSaveE) {
        this.defaultImage = defaultImage;
        this.defaultName = defaultName;
        this.defaultElytra = defaultElytra;
        this.onSaveE = onSaveE;
    }

    protected void clean() {
        if (texture != null) texture.close();
        mc.getTextureManager().release(previewId);
    }

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    @Override
    public void render(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

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

        Drawer.level(graphics, font, width, height);
    }


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

        try {
            int W = defaultImage.getWidth();
            int H = defaultImage.getHeight();

            NativeImage image = new NativeImage(W, H, false);

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    image.setPixel(x, y, defaultImage.getPixel(x, y));
                }
            }

            BoardWidget board = widget(new BoardWidget(
                    width / 2 - W * BoardWidget.scale / 2,
                    60,
                    W,
                    H
            ) {
                @Override
                protected void renderBoard(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    super.renderBoard(graphics, mouseX, mouseY, a);

                    int width = w / 64 * 10;
                    int height = h / 32 * 16;

                    DrawHelper.drawBox(
                            graphics,
                            getX() + scale,
                            getY(),
                            getX() + scale * (width + 1) - 1,
                            getY() + scale - 1,
                            0xff00ff00
                    );

                    DrawHelper.drawBox(
                            graphics,
                            getX() + scale * (width + 1),
                            getY(),
                            getX() + scale * (width * 2 + 1) - 1,
                            getY() + scale - 1,
                            0xff0000ff
                    );

                    DrawHelper.drawBox(
                            graphics,
                            getX(),
                            getY() + scale,
                            getX() + scale - 1,
                            getY() + scale * (height + 1) - 1,
                            0xffff0000
                    );

                    DrawHelper.drawBox(
                            graphics,
                            getX() + scale,
                            getY() + scale,
                            getX() + scale * (width + 1) - 1,
                            getY() + scale * (height + 1) - 1,
                            0xffffff00
                    );

                    DrawHelper.drawBox(
                            graphics,
                            getX() + scale * (width + 1),
                            getY() + scale,
                            getX() + scale * (width + 2) - 1,
                            getY() + scale * (height + 1) - 1,
                            0xffff00ff
                    );

                    DrawHelper.drawBox(
                            graphics,
                            getX() + scale * (width + 2),
                            getY() + scale,
                            getX() + scale * (width * 2 + 2) - 1,
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
            name.setValue(defaultName);

            AtomicBoolean elytra = new AtomicBoolean(defaultElytra);
            widget(
                    Button.builder(
                                    Component.translatable("capey.config.edit.save"),
                                    button -> {
                                        try {
                                            Path temp = Files.createTempFile("capey", ".png");
                                            image.writeToFile(temp);
                                            image.close();

                                            byte[] png = Files.readAllBytes(temp);
                                            Files.deleteIfExists(temp);

                                            onSaveE.accept(png, name.getValue(), elytra.get());
                                        } catch (IOException e) {
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

        //? if >=1.21.11 {
        @Override
                //?}
        public void setValue(double newValue) {
            //? if >=1.21.11 {
            super.setValue(newValue);
            //?} else {
            /*value = newValue;
             *///?}
        }

        public AtomicInteger integer() {
            return integer;
        }
    }

    private void preview(BoardWidget board, int w, int h) {
        if (texture == null) {
            texture = new DynamicTexture(() -> "capey:preview", w, h, false);
            mc.getTextureManager().register(Identifier.fromNamespaceAndPath("capey", "preview"), texture);
        }

        NativeImage img = texture.getPixels();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setPixel(x, y, board.getColor(y * w + x));
            }
        }
        texture.upload();
    }
}
