package dev.jgj52.capey.screen;

import dev.jgj52.capey.Capey;
import dev.jgj52.capey.util.Cape;
import dev.jgj52.capey.util.Player;
import dev.jgj52.screenapi.screen.BetterScreen;
import dev.jgj52.screenapi.widget.PlayerWithCapeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static dev.jgj52.screenapi.version.VScreen.setScreen;

public class CapeSelectorScreen extends BetterScreen {
    private static final Minecraft mc = Minecraft.getInstance();

    public static onClickI selectCape = (Player player, Cape cape, boolean background, Button edit, Button delete, MouseButtonEvent event, boolean doubleClick) -> {
        String nowUUID = player.getUUID().toString();
        String capeUUID = cape.getUUID().toString();
        if (background) {
            Capey.local.get().addProperty(nowUUID, "");
            Player.setCape("");
        } else {
            Capey.local.get().addProperty(nowUUID, capeUUID);
            Player.setCape(capeUUID);
        }
        Capey.local.save();
        player.reFetch();

        boolean isOwn = player.getUUID().equals(cape.getUploader()) && !background;
        if (edit != null) edit.visible = isOwn;
        if (delete != null) delete.visible = isOwn;
    };

    @FunctionalInterface
    public interface onClickI {
        void accept(Player player, Cape cape, boolean background, Button edit, Button delete, MouseButtonEvent event, boolean doubleClick);
    }

    private final Player local;
    private final List<Cape> capes;
    private final onClickI onClickE;
    public CapeSelectorScreen(Player local, List<Cape> capes, onClickI onClick) {
        this.local = local;
        this.capes = capes;
        this.onClickE = onClick;
    }

    private Button edit;
    private Button delete;

    @Override
    protected int getScrollSpeed() {
        return 30;
    }

    @Override
    protected void createWidgets(Font font) {
        AtomicInteger offset = new AtomicInteger();
        int perRow = mc.getWindow().getGuiScaledWidth() / 80 - 2;
        Supplier<Cape> selected = local::getCape;
        List<PlayerWithCapeWidget> all = new ArrayList<>();

        capes.forEach(cape -> {
            while (!cape.isLoaded()) {}
            Player player = Player.of(cape.getUploader());
            Supplier<PlayerSkin> skinWithCape = cape.fromSkin(player.getSkin());

            int i = offset.getAndIncrement();
            all.add(widget(new PlayerWithCapeWidget(
                    (i % perRow) * 80 + (width - perRow * 80) / 2,
                    (i / perRow) * 100 + 50,
                    70,
                    100,
                    skinWithCape
            ) {
                @Override
                protected void renderPlayer(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                    super.renderPlayer(graphics, mouseX, mouseY, a);

                    if (selected.get() == cape && !background()) {
                        all.forEach(w -> w.background(false));
                        background(true);
                    } else if (selected.get() == null && background()) {
                        background(false);
                    }
                }

                @Override
                public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
                    onClickE.accept(local, cape, background(), edit, delete, event, doubleClick);
                }
            }));
            Component name = Component.literal(cape.getName());
            int add = font.width(name) > 70 ? 2 : 35 - font.width(name) / 2;
            widget(new StringWidget(
                    (i % perRow) * 80 + (width - perRow * 80) / 2 + add,
                    (i / perRow) * 100 + 90 + 50,
                    70,
                    font.lineHeight,
                    name,
                    font
            ));
        });
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

        edit = widget(
                Button.builder(
                                Component.translatable("capey.config.my.edit"),
                                button -> {
                                    Cape current = selected.get();
                                    if (current == null) return;

                                    UUID nowUUID = Player.getCurrentUUID();
                                    if (!nowUUID.equals(current.getUploader())) return;

                                    setScreen(new EditCapeScreen(this, current));
                                }
                        )
                        .bounds(width - 130, height - 30, 120, 20)
                        .build()
        );
        edit.visible = false;

        delete = widget(
                Button.builder(
                                Component.translatable("capey.config.my.delete"),
                                button -> {
                                    Cape current = selected.get();
                                    if (current == null) return;

                                    UUID nowUUID = Player.getCurrentUUID();
                                    if (!nowUUID.equals(current.getUploader())) return;

                                    Cape.deleteCape(current.getUUID()).thenAccept(success -> {
                                        if (success) {
                                            Cape.all(true);
                                            mc.execute(() -> {
                                                onClose();
                                                Capey.local.get().remove(local.getUUID().toString());
                                                local.reFetch();
                                                current.unload();
                                            });
                                        }
                                    });
                                }
                        )
                        .bounds(10, height - 30, 120, 20)
                        .build()
        );
        delete.visible = false;
    }
}
