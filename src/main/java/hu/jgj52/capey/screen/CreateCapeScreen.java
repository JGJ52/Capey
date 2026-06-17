package hu.jgj52.capey.screen;

import hu.jgj52.capey.types.Cape;
import hu.jgj52.capey.widget.PlayerWithCapeWidget;
import hu.jgj52.screenapi.screen.BetterScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
    }

    @Override
    public void onClose() {
        setScreen(parent);
    }
}
