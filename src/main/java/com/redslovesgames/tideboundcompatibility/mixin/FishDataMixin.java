/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.FishingContext;
import com.redslovesgames.tideboundcompatibility.fishing.FishingModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishData.class)
abstract class FishDataMixin {
   @Inject(method = "weight", at = @At("RETURN"), cancellable = true, remap = false)
   private void tidebound$modifyFishWeight(FishingContext context, CallbackInfoReturnable<Double> callback) {
      callback.setReturnValue(FishingModifiers.modifyFishWeight((FishData)(Object)this, context, (Double)callback.getReturnValue()));
   }
}
