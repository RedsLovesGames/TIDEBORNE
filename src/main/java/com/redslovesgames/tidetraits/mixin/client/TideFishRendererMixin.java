/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.registries.entities.renderers.FishRenderer;
import com.redslovesgames.tidetraits.client.render.MutationRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = FishRenderer.class, priority = 1100)
public abstract class TideFishRendererMixin {
   @Inject(method = "getTextureLocation(Lnet/minecraft/MobEntity;)Lnet/minecraft/Identifier;", at = @At("RETURN"), cancellable = true, require = 0)
   private void tideTraits$mutationTexture(MobEntity mob, CallbackInfoReturnable<Identifier> cir) {
      Identifier original = (Identifier)cir.getReturnValue();
      if (original != null) {
         cir.setReturnValue(MutationRendering.textureFor(original, mob));
      }
   }
}
