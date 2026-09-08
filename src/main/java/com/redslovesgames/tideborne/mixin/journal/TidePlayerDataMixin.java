/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.journal;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.journal.TeamJournalCatchBridge;
import com.redslovesgames.tideborne.journal.TeamJournalService;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TidePlayerData is owned by Tide rather than Minecraft, so its member names are not
 * part of Yarn's obfuscation map. Keeping this mixin non-remapped prevents the Mixin
 * annotation processor from trying to resolve Tide's getOrCreate/syncTo/logCatch names
 * as Minecraft symbols while Loom still remaps the Minecraft types in our bytecode.
 */
@Mixin(value = TidePlayerData.class, remap = false)
abstract class TidePlayerDataMixin {
   @Inject(method = "getOrCreate", at = @At("HEAD"), cancellable = true)
   private static void tideTeamJournal$getTeamData(ServerPlayerEntity player, CallbackInfoReturnable<TidePlayerData> callback) {
      callback.setReturnValue(TeamJournalService.loadFor(player));
   }

   @Inject(method = "syncTo", at = @At("HEAD"), cancellable = true)
   private void tideTeamJournal$saveTeamData(ServerPlayerEntity player, CallbackInfo callback) {
      if (TeamJournalService.saveAndSync((TidePlayerData)(Object)this, player)) {
         callback.cancel();
      }
   }

   @Inject(method = "logCatch", at = @At("HEAD"))
   private void tideTeamJournal$beginCatch(ItemStack stack, ServerPlayerEntity player, World level, CallbackInfo callback) {
      TeamJournalCatchBridge.beginDirectLog((TidePlayerData)(Object)this, stack);
   }

   @Inject(method = "logCatch", at = @At("TAIL"))
   private void tideTeamJournal$finishCatch(ItemStack stack, ServerPlayerEntity player, World level, CallbackInfo callback) {
      TeamJournalCatchBridge.finishDirectLog((TidePlayerData)(Object)this, player);
   }
}
