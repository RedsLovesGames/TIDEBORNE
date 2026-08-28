/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.mixin;

import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideteamjournal.TeamJournalService;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TideUtils.class)
abstract class TideUtilsMixin {
   @Unique
   private static final ThreadLocal<TidePlayerData> TIDE_TEAM_JOURNAL_BEFORE_CATCH = new ThreadLocal<>();

   @Inject(method = "tryLogCatch", at = @At("HEAD"))
   private static void tideTeamJournal$snapshotTeamJournal(ItemStack stack, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
      TeamProgressStore.tideborneBeginCatch(stack);
      TIDE_TEAM_JOURNAL_BEFORE_CATCH.set(new TidePlayerData(TeamJournalService.loadFor(player).getAsTag()));
   }

   @Inject(method = "tryLogCatch", at = @At("RETURN"))
   private static void tideTeamJournal$captureAfterSavedCatch(ItemStack stack, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
      TidePlayerData before = TIDE_TEAM_JOURNAL_BEFORE_CATCH.get();
      TIDE_TEAM_JOURNAL_BEFORE_CATCH.remove();
      if (callback.getReturnValueZ() && before != null) {
         TeamJournalService.captureCatchAfterSave(before, TeamJournalService.loadFor(player), player);
      }

      TeamProgressStore.tideborneClearCatch();
   }
}
