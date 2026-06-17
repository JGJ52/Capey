package hu.jgj52.capey.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class BoardWidget extends AbstractWidget {
    public static final int scale = 4;

    protected final int w;
    protected final int h;
    private final int[] pixels;

    private int color = 0xffffffff;
    public void color(int color) {
        this.color = color;
    }

    public BoardWidget(int x, int y, int width, int height) {
        super(x, y, width * scale, height * scale, Component.empty());
        w = width;
        h = height;

        pixels = new int[width * height];
    }

    @Override
    protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.fill(
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                0x80000000
        );

        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int x = i % w;
            int y = i / w;

            graphics.fill(
                    getX() + x * scale,
                    getY() + y * scale,
                    getX() + (x + 1) * scale,
                    getY() + (y + 1) * scale,
                    color
            );
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int insideX = ((int) event.x() - getX()) / scale;
        int insideY = ((int) event.y() - getY()) / scale;

        pixels[insideX + insideY * w] = color;

        return true;
    }

    private int lastX = -1;
    private int lastY = -1;

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!isMouseOver(event.x(), event.y())) {
            lastX = -1;
            lastY = -1;
            return false;
        }

        int x = ((int) event.x() - getX()) / scale;
        int y = ((int) event.y() - getY()) / scale;

        if (lastX != -1) {
            drawLine(lastX, lastY, x, y);
        }

        lastX = x;
        lastY = y;

        return true;
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        lastX = -1;
        lastY = -1;
        return super.mouseReleased(event);
    }

    // chatgpt code
    // idk what is this
    // i dont even want to
    private void drawLine(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;

        while (true) {
            if (x0 >= 0 && y0 >= 0 && x0 < w && y0 < h) {
                pixels[x0 + y0 * w] = color;
            }

            if (x0 == x1 && y0 == y1) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}
