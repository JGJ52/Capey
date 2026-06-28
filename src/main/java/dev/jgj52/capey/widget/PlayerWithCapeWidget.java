package dev.jgj52.capey.widget;

import com.google.gson.JsonPrimitive;
import dev.jgj52.capey.version.VPlayerRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//? >= 1.21.9 {
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.input.MouseButtonEvent;
//? } else {
/*import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.PlayerSkin;
*///? }
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static dev.jgj52.capey.Capey.config;

public class PlayerWithCapeWidget extends AbstractWidget {
    private static final Minecraft mc = Minecraft.getInstance();

    public static class FakePlayer {
        private static ItemStack ely = ItemStack.EMPTY;

        public static ItemStack ely() {
            if (!ely.isEmpty()) return ely;
            try {
                ely = new ItemStack(Items.ELYTRA);
            } catch (NullPointerException e) {
                ely = ItemStack.EMPTY;
            }
            return ely;
        }

        private final Supplier<PlayerSkin> skin;
        private static boolean elytra = config.getOrElse("elytra", new JsonPrimitive(false)).getAsBoolean();

        public FakePlayer(Supplier<PlayerSkin> skin) {
            this.skin = skin;
        }

        public static void elytra() {
            elytra = !elytra;
            config.set("elytra", new JsonPrimitive(elytra));
            config.save();
        }

        public VPlayerRenderState state() {
            VPlayerRenderState state = new VPlayerRenderState();
            state.skin = skin.get();
            state.elytraRotX = 0.25f;
            state.elytraRotY = -0.01f;
            state.elytraRotZ = -0.275f;
            if (elytra) {
                state.
                //? >= 1.21.4 {
                chestEquipment
                //? } else {
                /*chestItem
                *///? }
                        = ely();
            }
            return state;
        }
    }

    private final FakePlayer player;
    private final int scale;
    private boolean background = false;
    private float rotationX = 160;
    private float rotationY = 200;
    private float offset = 0.7f;
    public PlayerWithCapeWidget(int x, int y, int width, int height, Supplier<PlayerSkin> skin, int scale) {
        super(x, y, width, height, Component.empty());
        this.player = new FakePlayer(skin);
        this.scale = scale;
    }
    public PlayerWithCapeWidget(int x, int y, int width, int height, Supplier<PlayerSkin> skin) {
        this(x, y, width, height, skin, 40);
    }

    public FakePlayer getPlayer() {
        return player;
    }

    public void rotateX(float x) {
        rotationX = x;
    }

    public void rotateY(float y) {
        rotationY = y;
    }

    public void offset(float offset) {
        this.offset = offset;
    }

    public void background(boolean background) {
        this.background = background;
    }

    public boolean background() {
        return background;
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
        renderPlayer(graphics, mouseX, mouseY, a);
    }

    protected void renderPlayer(
            @NotNull
            net.minecraft.client.gui.
            //? >= 26.1 {
            GuiGraphicsExtractor
             //? } else {
            /*GuiGraphics
            *///? }
            graphics, int mouseX, int mouseY, float a
    ) {
        if (background) {
            graphics.fill(
                    getX(),
                    getY(),
                    getX() + getWidth(),
                    getY() + getHeight(),
                    0xff000000
            );
        }

        //? >= 1.21.6 {
        graphics.
                //? >= 26.1 {
                entity
                //? } else {
                /*submitEntityRenderState
                *///? }
                        (
                                player.state(),
                                scale,
                                new Vector3f(0, offset, 0),
                                new Quaternionf()
                                        .rotateX((float) Math.toRadians(rotationX))
                                        .rotateY((float) Math.toRadians(rotationY)),
                                new Quaternionf(),
                                getX(),
                                getY(),
                                getX() + getWidth(),
                                getY() + getHeight()
                        );

        //? }
    }

    @Override
    protected void onDrag(
            //? >= 1.21.9 {
            @NotNull MouseButtonEvent event,
            //? } else {
            /*double x, double y,
            *///? }
            double dx, double dy) {
        rotationX = Mth.clamp(this.rotationX - (float) -dy * 2.5f, 140.0f, 220.0f);
        rotationY += (float) dx * 2.5f;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
