package dev.jgj52.capey.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jgj52.capey.widget.PlayerWithCapeWidget;
import dev.tr7zw.waveycapes.renderlayers.CustomCapeRenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
//? >= 1.21.11 {
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
//? } else {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.PlayerCapeModel;
*///? }
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomCapeRenderLayer.class)
public class WaveyCapesMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    public void capey$submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, AvatarRenderState renderState, float f, float g, CallbackInfo ci) {
        if (renderState instanceof PlayerWithCapeWidget.WAvatarRenderState state) {
            ci.cancel();
            if (renderState.isInvisible || !renderState.showCape) {
                return;
            }
            PlayerSkin skin = renderState.skin;
            if (skin.cape() == null) {
                return;
            }
            if (state.chestEquipment.is(Items.ELYTRA)) return;
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.06875f);
            HumanoidModel<AvatarRenderState> model = new PlayerCapeModel(EntityModelSet.vanilla().bakeLayer(ModelLayers.PLAYER_CAPE));
            submitNodeCollector.submitModel(
                    model,
                    renderState,
                    poseStack,
                    //? >= 1.21.11 {
                    RenderTypes
                    //? } else {
                    /*RenderType
                    *///? }
                            .entitySolid(skin.cape().texturePath()),
                    packedLight, OverlayTexture.NO_OVERLAY,
                    renderState.outlineColor,
                    null
            );
            poseStack.popPose();
        }
    }
}
