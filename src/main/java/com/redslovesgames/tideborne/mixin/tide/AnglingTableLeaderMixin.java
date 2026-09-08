/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.tide;

import com.redslovesgames.tideborne.fishing.gear.AnglingTableLeaderSupport;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.li64.tide.client.gui.menus.AnglingTableMenu", remap = false)
public abstract class AnglingTableLeaderMixin extends ForgingScreenHandler {
   protected AnglingTableLeaderMixin(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(type, syncId, playerInventory, context);
   }

   @Inject(method = "getForgingSlotsManager", at = @At("HEAD"), cancellable = true, remap = true)
   private void tideborne$leaderSlots(CallbackInfoReturnable<ForgingSlotsManager> callbackInfo) {
      callbackInfo.setReturnValue(AnglingTableLeaderSupport.createSlotDefinition());
   }

   @Inject(method = "updateResult", at = @At("HEAD"), remap = true)
   private void tideborne$extractLeader(CallbackInfo callbackInfo) {
      AnglingTableLeaderSupport.beforeUpdate(this.input);
   }

   @Inject(method = "updateResult", at = @At("TAIL"), remap = true)
   private void tideborne$applyLeader(CallbackInfo callbackInfo) {
      AnglingTableLeaderSupport.afterUpdate(this.input, this.output);
   }
}
