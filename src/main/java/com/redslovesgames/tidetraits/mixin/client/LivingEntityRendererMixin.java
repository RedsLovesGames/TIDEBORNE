/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.redslovesgames.tidetraits.client.render.MutationRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, priority = 900)
public abstract class LivingEntityRendererMixin {
   @ModifyExpressionValue(
      method = "getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getTexture(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/Identifier;"),
      require = 0
   )
   private Identifier tideTraits$foreignMutationTexture(Identifier original, LivingEntity entity) {
      return MutationRendering.textureForForeignFish(original, entity);
   }

   @Inject(
      method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;scale(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V", shift = Shift.BEFORE),
      require = 0
   )
   private void tideTraits$physicalLengthScale(
      LivingEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider buffers, int packedLight, CallbackInfo ci
   ) {
      MutationRendering.applyLengthScale(entity, poseStack);
   }
}
