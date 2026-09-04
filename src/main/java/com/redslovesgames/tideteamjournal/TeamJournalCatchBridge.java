package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.v2.integration.CrateFishProgressionBridge;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Owns Team Journal catch-accounting state around Tide lifecycle callbacks.
 * Tide-targeting mixins should only signal these boundaries and must not duplicate
 * canonicalization, snapshot, record, or cleanup rules.
 */
public final class TeamJournalCatchBridge {
   private static final ThreadLocal<TidePlayerData> DIRECT_BEFORE = new ThreadLocal<>();
   private static final ThreadLocal<TidePlayerData> TRY_LOG_BEFORE = new ThreadLocal<>();

   private TeamJournalCatchBridge() {
   }

   public static void beginDirectLog(TidePlayerData current, ItemStack stack) {
      CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(stack);
      TeamProgressStore.tideborneBeginCatch(stack);
      DIRECT_BEFORE.set(snapshot(current));
   }

   public static void finishDirectLog(TidePlayerData current, ServerPlayerEntity player) {
      TidePlayerData before = DIRECT_BEFORE.get();
      DIRECT_BEFORE.remove();
      try {
         TeamJournalService.captureCatch(before == null ? new TidePlayerData() : before, current, player);
      } finally {
         TeamProgressStore.tideborneClearCatch();
      }
   }

   public static void beginTryLog(ItemStack stack, ServerPlayerEntity player) {
      TeamProgressStore.tideborneBeginCatch(stack);
      TRY_LOG_BEFORE.set(snapshot(TeamJournalService.loadFor(player)));
   }

   public static void finishTryLog(boolean saved, ServerPlayerEntity player) {
      TidePlayerData before = TRY_LOG_BEFORE.get();
      TRY_LOG_BEFORE.remove();
      try {
         if (saved && before != null) {
            TeamJournalService.captureCatchAfterSave(before, TeamJournalService.loadFor(player), player);
         }
      } finally {
         TeamProgressStore.tideborneClearCatch();
      }
   }

   private static TidePlayerData snapshot(TidePlayerData data) {
      return data == null ? new TidePlayerData() : new TidePlayerData(data.getAsTag());
   }
}
