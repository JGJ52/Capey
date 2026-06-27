package dev.jgj52.capey.screen;

import com.mojang.blaze3d.platform.NativeImage;
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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.jgj52.capey.version.VScreen.setScreen;

public class CreateCapeScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();

    private final Screen parent;
    public CreateCapeScreen(Screen parent) {
        this.parent = parent;
    }

    private DynamicTexture texture;

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int i, int i1, float v) {
        graphics.fill(
                10,
                height - 140,
                80,
                height - 60,
                0x80000000
        );
    }

    @Override
    protected void createWidgets(Font font) {
        PlayerWithCapeWidget cw = widget(new PlayerWithCapeWidget(
            10,
            height - 140,
            70,
            80,
            Cape.emptySkin(Identifier.fromNamespaceAndPath("capey", "preview"))
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

        int W = 64;
        int H = 32;

        BoardWidget board = widget(new BoardWidget(
                width / 2 - W * BoardWidget.scale / 2,
                60,
                W,
                H
        ) {
            @Override
            protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
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

                    board.color(
                            (a.get() << 24) | (r.get() << 16) | (g.get() << 8) | b.get()
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
            int h;
            try {
                h = (int) Long.parseLong(s, 16);
            } catch (NumberFormatException ignored) {
                return;
            }

            if ((h & 0xFF000000) == 0) {
                h |= 0xFF000000;
            }

            a.set((h >> 24) & 0xFF);
            r.set((h >> 16) & 0xFF);
            g.set((h >> 8) & 0xFF);
            b.set(h & 0xFF);
            sliders.forEach(slider -> slider.setValue((double) slider.integer().get() / 255));
        });

        preview(board, W, H);
        board.onDraw(pixel -> {
            preview(board, W, H);
        });
    }

    private static abstract class Slider extends AbstractSliderButton {
        private final AtomicInteger integer;
        public Slider(int x, int y, int width, int height, Component message, double initialValue, AtomicInteger integer) {
            super(x, y, width, height, message, initialValue);
            this.integer = integer;
        }

        @Override
        public void setValue(double newValue) {
            super.setValue(newValue);
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
            mc.getTextureManager().register(
                    Identifier.fromNamespaceAndPath("capey", "preview"),
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
        mc.getTextureManager().release(Identifier.fromNamespaceAndPath("capey", "preview"));
        setScreen(parent);
    }
}
