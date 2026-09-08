/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.backend;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.journal.TeamJournalService;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tideborne.discovery.multiplayer.PersonalTideJournal;
import com.redslovesgames.tideborne.discovery.multiplayer.SharedDiscoverySnapshot;
import com.redslovesgames.tideborne.discovery.DiscoveryManager;
import com.redslovesgames.tideborne.discovery.DiscoverySnapshot;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TideborneBackend {
   private TideborneBackend() {
   }

   public static DiscoverySnapshot personalDiscoveries(PlayerEntity var0) {
      return DiscoveryManager.snapshot(var0);
   }

   public static SharedDiscoverySnapshot sharedDiscoveries(ServerPlayerEntity var0) {
      return MultiplayerDiscoveryCompat.snapshot(var0);
   }

   public static TidePlayerData journal(ServerPlayerEntity var0) {
      return TeamJournalService.loadFor(var0);
   }

   public static Optional<TidePlayerData> personalJournal(ServerPlayerEntity var0) {
      return PersonalTideJournal.load(var0);
   }

   public static boolean discoverMutation(ServerPlayerEntity var0, Identifier var1, Identifier var2) {
      return DiscoveryManager.discoverMutationAndSync(var0, var1, var2);
   }

   public static boolean discoverSizeBand(ServerPlayerEntity var0, Identifier var1, Identifier var2) {
      return DiscoveryManager.discoverSizeBandAndSync(var0, var1, var2);
   }

   public static boolean recordSuccessfulCatch(ServerPlayerEntity var0, Identifier var1, Identifier var2, Identifier var3) {
      return MultiplayerDiscoveryCompat.recordCatch(var0, var1, var2, var3);
   }

   public static NbtCompound teamData(ServerPlayerEntity var0, int var1, String var2, String var3, String var4) {
      return TeamJournalService.teamData(var0, var1, var2, var3, var4);
   }

   public static TeamJournalService.RecordStatus heldRecordStatus(ServerPlayerEntity var0) {
      return TeamJournalService.getHeldRecordStatus(var0);
   }

   public static TeamJournalService.ClaimResult claimHeldLargest(ServerPlayerEntity var0) {
      return TeamJournalService.claimHeldRecord(var0, true);
   }

   public static TeamJournalService.ClaimResult claimHeldSmallest(ServerPlayerEntity var0) {
      return TeamJournalService.claimHeldRecord(var0, false);
   }

   public static TeamJournalService.ClaimAllResult claimAllRecords(ServerPlayerEntity var0) {
      return TeamJournalService.claimAllRecords(var0);
   }

   public static TeamJournalService.ManualMergeResult mergePersonalIntoTeam(ServerPlayerEntity var0) {
      return TeamJournalService.manuallyMergePersonalJournal(var0);
   }

   public static void sync(ServerPlayerEntity var0) {
      DiscoveryManager.sync(var0);
      MultiplayerDiscoveryCompat.sync(var0);
      TeamJournalService.syncCurrentJournal(var0);
   }
}
