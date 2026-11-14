package dev.jgj52.capey.screen;

import dev.jgj52.capey.util.Cape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class EditCapeScreen extends CapeEditorScreen {
    private static final Minecraft mc = Minecraft.getInstance();

    private final Screen parent;

    public EditCapeScreen(Screen parent, Cape cape) {
        this(parent, cape, new Screen[1]);
    }

    private EditCapeScreen(Screen parent, Cape cape, Screen[] self) {
        super(cape.getImage(), cape.getName(), cape.changesElytra(), (png, name, elytra) ->
            Cape.editCape(cape.getUUID(), name, elytra, png).thenAccept(success -> {
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