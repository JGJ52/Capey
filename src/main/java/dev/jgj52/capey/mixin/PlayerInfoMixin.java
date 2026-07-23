package dev.jgj52.capey.mixin;

import dev.jgj52.capey.types.Cape;
import dev.jgj52.capey.types.Player;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void capey$getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin original = cir.getReturnValue();
        PlayerInfo info = (PlayerInfo) (Object) this;
        Player player = Player.of(info.getProfile().id());
        Cape cape = player.getCape();

        if (cape == null) return;

        cir.setReturnValue(cape.fromSkin(() -> original).get());
    }
}
