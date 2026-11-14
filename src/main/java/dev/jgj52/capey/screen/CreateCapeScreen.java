package dev.jgj52.capey.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.jgj52.capey.util.Cape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class CreateCapeScreen extends CapeEditorScreen {
    private static final Minecraft mc = Minecraft.getInstance();

    private final Screen parent;

    public CreateCapeScreen(Screen parent) {
        this(parent, new Screen[1]);
    }

    private CreateCapeScreen(Screen parent, Screen[] self) {
        super(new NativeImage(64, 32, true), "Cape name", true, (png, name, elytra) ->
                Cape.createCape(name, elytra, png).thenAccept(success -> {
                    if (success) {
                        mc.execute(() -> self[0].onClose());
                    }
                })
        );
        this.parent = parent;
        self[0] = this;
    }

    @Override
    public void onClose() {
        clean();
        setScreen(parent);
    }
}