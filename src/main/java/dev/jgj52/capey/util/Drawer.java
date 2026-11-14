package dev.jgj52.capey.util;

import dev.jgj52.screenapi.version.VGraphics;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class Drawer {
    private static long levelStart = -1;

    public static void level(GuiGraphicsExtractor graphics, Font font, int width, int height) {
        if (PlayerWithCapeWidget.FakePlayer.ely().isEmpty()) {
            levelStart = -1;
            return;
        }

        Component level = Component.translatable("capey.config.main.level")
                .withColor(0x00ff0000);

        int textWidth = font.width(level);
        int maxWidth = width * 2 / 3;
        int x = width / 2 - maxWidth / 2;
        int y = 10 + font.lineHeight;

        if (textWidth <= maxWidth) {
            VGraphics.text(graphics, font, level, width / 2 - textWidth / 2, y, 0xFFFFFFFF);
            return;
        }

        long now = System.currentTimeMillis();

        if (levelStart == -1) levelStart = now;

        double pause = 1500;
        double speed = 30.0;
        double distance = textWidth - maxWidth;
        double duration = distance / speed * 1000;

        double elapsed = (now - levelStart) - pause;
        double offset = Math.clamp(elapsed / 1000 * speed, 0, distance);

        graphics.enableScissor(x, y, x + maxWidth, y + font.lineHeight);
        VGraphics.text(graphics, font, level, x - (int) offset, y, 0xFFFFFFFF);
        graphics.disableScissor();

        if (elapsed > duration + 2000)
            levelStart = now;
    }
}