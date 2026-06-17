package hu.jgj52.capey.screen;

import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.widget.BoardWidget;
import hu.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static hu.jgj52.capey.version.VScreen.setScreen;

public class CreateCapeScreen extends BetterScreen {
    private final Screen parent;
    public CreateCapeScreen(Screen parent) {
        this.parent = parent;
    }

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
            Cape.of(UUID.fromString("e36e5e41-4fd8-48f5-a720-2f912ca97a25")).emptySkin()
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

        widget(new BoardWidget(
                width / 2 - 64 * BoardWidget.scale / 2,
                60,
                64,
                32
        ) {
            @Override
            protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
                // why does this take so long
                // i spent 45 minutes on this

                // top
                graphics.fill(
                        getX() + scale + 1,
                        getY(),
                        getX() + scale * 11 - 1,
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
                        getX() + scale * 11 - 1,
                        getY() + scale,
                        0xff00ff00
                );
                // right
                graphics.fill(
                        getX() + scale * 11 -1,
                        getY(),
                        getX() + scale * 11,
                        getY() + scale,
                        0xff00ff00
                );

                // top
                graphics.fill(
                        getX() + scale * 11 + 1,
                        getY(),
                        getX() + scale * 21 - 1,
                        getY() + 1,
                        0xff0000ff
                );
                // left
                graphics.fill(
                        getX() + scale * 11,
                        getY(),
                        getX() + scale * 11 + 1,
                        getY() + scale,
                        0xff0000ff
                );
                // bottom
                graphics.fill(
                        getX() + scale * 11 + 1,
                        getY() + scale - 1,
                        getX() + scale * 21 - 1,
                        getY() + scale,
                        0xff0000ff
                );
                // right
                graphics.fill(
                        getX() + scale * 21 - 1,
                        getY(),
                        getX() + scale * 21,
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
                        getY() + 17 * scale - 1,
                        0xffff0000
                );
                // bottom
                graphics.fill(
                        getX(),
                        getY() + 17 * scale - 1,
                        getX() + scale,
                        getY() + 17 * scale,
                        0xffff0000
                );
                // right
                graphics.fill(
                        getX() + scale - 1,
                        getY() + scale + 1,
                        getX() + scale,
                        getY() + scale * 17 - 1,
                        0xffff0000
                );

                // what color now

                // top
                graphics.fill(
                        getX() + scale,
                        getY() + scale,
                        getX() + scale * 11,
                        getY() + scale + 1,
                        0xffffff00
                );
                // left
                graphics.fill(
                        getX() + scale,
                        getY() + scale + 1,
                        getX() + scale + 1,
                        getY() + scale * 17 - 1,
                        0xffffff00
                );
                // bottom
                graphics.fill(
                        getX() + scale,
                        getY() + scale * 17 - 1,
                        getX() + scale * 11,
                        getY() + scale * 17,
                        0xffffff00
                );
                // right
                graphics.fill(
                        getX() + scale * 11 - 1,
                        getY() + scale + 1,
                        getX() + scale * 11,
                        getY() + scale * 17 - 1,
                        0xffffff00
                );

                // top
                graphics.fill(
                        getX() + scale * 11,
                        getY() + scale,
                        getX() + scale * 12,
                        getY() + scale + 1,
                        0xffff00ff
                );
                // left
                graphics.fill(
                        getX() + scale * 11,
                        getY() + scale + 1,
                        getX() + scale * 11 + 1,
                        getY() + scale * 17 - 1,
                        0xffff00ff
                );
                // bottom
                graphics.fill(
                        getX() + scale * 11,
                        getY() + scale * 17 - 1,
                        getX() + scale * 12,
                        getY() + scale * 17,
                        0xffff00ff
                );
                // right
                graphics.fill(
                        getX() + scale * 12 - 1,
                        getY() + scale + 1,
                        getX() + scale * 12,
                        getY() + scale * 17 - 1,
                        0xffff00ff
                );

                // top
                graphics.fill(
                        getX() + scale * 12,
                        getY() + scale,
                        getX() + scale * 22,
                        getY() + scale + 1,
                        0xff00ffff
                );
                // left
                graphics.fill(
                        getX() + scale * 12,
                        getY() + scale + 1,
                        getX() + scale * 12 + 1,
                        getY() + scale * 17 - 1,
                        0xff00ffff
                );
                // bottom
                graphics.fill(
                        getX() + scale * 12,
                        getY() + scale * 17 - 1,
                        getX() + scale * 22,
                        getY() + scale * 17,
                        0xff00ffff
                );
                // right
                graphics.fill(
                        getX() + scale * 22 - 1,
                        getY() + scale + 1,
                        getX() + scale * 22,
                        getY() + scale * 17 - 1,
                        0xff00ffff
                );
            }
        }, "board", true);
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }
}
