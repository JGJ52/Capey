package dev.jgj52.capey.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? >= 1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//? }
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardWidget extends AbstractWidget {
    public static final int scale = 4;

    protected final int w;
    protected final int h;
    private final int[] pixels;

    private int color = 0xffffffff;
    public void color(int color) {
        this.color = color;
    }
    public int color() {
        return color;
    }

    private final List<Consumer<Integer>> onDraw = new ArrayList<>();
    private void draw(int pixel) {
        pixels[pixel] = color;
        onDraw.forEach(onDraw -> onDraw.accept(pixel));
    }
    public void onDraw(Consumer<Integer> onDraw) {
        this.onDraw.add(onDraw);
    }

    public int getColor(int pixel) {
        return pixels[pixel];
    }

    public BoardWidget(int x, int y, int width, int height) {
        super(x, y, width * scale, height * scale, Component.empty());
        w = width;
        h = height;

        pixels = new int[width * height];
    }

    @Override
    protected void
    //? >= 26.1 {
    extractWidgetRenderState
    //? } else {
    /*renderWidget
    *///? }
    (
            @NotNull
            net.minecraft.client.gui.
            //? >= 26.1 {
            GuiGraphicsExtractor
            //? } else {
            /*GuiGraphics
             *///? }
            graphics, int mouseX, int mouseY, float a
    ) {
        renderBoard(graphics, mouseX, mouseY, a);
    }

    protected void renderBoard(
            @NotNull
            net.minecraft.client.gui.
            //? >= 26.1 {
            GuiGraphicsExtractor
             //? } else {
            /*GuiGraphics
            *///? }
            graphics, int mouseX, int mouseY, float a
    ) {
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

            if ((color >>> 24) == 0) continue;

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
    //? >= 1.21.9 {
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
    //? } else {
    /*public boolean mouseClicked(double x, double y, int i) {
    *///? }
        int insideX = ((int) x - getX()) / scale;
        int insideY = ((int) y - getY()) / scale;

        draw(insideX + insideY * w);

        return true;
    }

    private int lastX = -1;
    private int lastY = -1;

    @Override
    //? >= 1.21.9 {
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double X = event.x();
        double Y = event.y();
    //? } else {
      /*public boolean mouseDragged(double X, double Y, int i, double dx, double dy) {
    *///? }
        if (!isMouseOver(X, Y)) {
            lastX = -1;
            lastY = -1;
            return false;
        }

        int x = ((int) X - getX()) / scale;
        int y = ((int) Y - getY()) / scale;

        if (lastX != -1) {
            drawLine(lastX, lastY, x, y);
        }

        lastX = x;
        lastY = y;

        return true;
    }

    @Override
    public boolean mouseReleased(
            //? >= 1.21.9 {
            @NotNull MouseButtonEvent event
            //? } else {
            /*double x, double y, int i
            *///? }
    ) {
        lastX = -1;
        lastY = -1;
        return super.mouseReleased(
                //? >= 1.21.9 {
                event
                //? } else {
                /*x, y, i
                *///? }
        );
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
                draw(x0 + y0 * w);
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
