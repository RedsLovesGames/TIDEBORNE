/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin;

import com.redslovesgames.tideboundcompatibility.fishing.AnglingTableLeaderSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.li64.tide.client.gui.menus.AnglingTableMenu", remap = false)
public abstract class AnglingTableLeaderMixin {
   @Inject(method = "getForgingSlotsManager", at = @At("HEAD"), cancellable = true, remap = false)
   private void tideborne$leaderSlots(CallbackInfoReturnable<Object> var1) {
      var1.setReturnValue(AnglingTableLeaderSupport.createSlotDefinition());
   }

   @Inject(method = "updateResult", at = @At("HEAD"), remap = false)
   private void tideborne$extractLeader(CallbackInfo var1) {
      AnglingTableLeaderSupport.beforeUpdate(this);
   }

   @Inject(method = "updateResult", at = @At("TAIL"), remap = false)
   private void tideborne$applyLeader(CallbackInfo var1) {
      AnglingTableLeaderSupport.afterUpdate(this);
   }
}
