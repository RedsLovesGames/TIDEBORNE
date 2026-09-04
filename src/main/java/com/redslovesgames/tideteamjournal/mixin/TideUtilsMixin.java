/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.mixin;

import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideteamjournal.TeamJournalCatchBridge;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TideUtils.class)
abstract class TideUtilsMixin {
   @Inject(method = "tryLogCatch", at = @At("HEAD"))
   private static void tideTeamJournal$beginCatch(ItemStack stack, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
      TeamJournalCatchBridge.beginTryLog(stack, player);
   }

   @Inject(method = "tryLogCatch", at = @At("RETURN"))
   private static void tideTeamJournal$finishCatch(ItemStack stack, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
      TeamJournalCatchBridge.finishTryLog(callback.getReturnValueZ(), player);
   }
}
