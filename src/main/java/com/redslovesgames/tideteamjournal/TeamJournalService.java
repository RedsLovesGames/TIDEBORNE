/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;

import com.li64.tide.Tide;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.li64.tide.network.messages.SyncPlayerDataMsg;
import com.redslovesgames.tideteamjournal.network.BobberSettingsPayload;
import com.redslovesgames.tideteamjournal.network.RecordEventPayload;
import com.redslovesgames.tideteamjournal.network.RecordHoldersPayload;
import com.redslovesgames.tideteamjournal.network.TeamDataPayload;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamManager;
import dev.ftb.mods.ftbteams.api.TeamRank;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;

public final class TeamJournalService {
   private static final int SCHEMA_VERSION = 3;
   private static final String TIDE_NATIVE_KEY = "TidePlayerData";
   private static final String SCHEMA_KEY = "schema_version";
   private static final String JOURNAL_KEY = "journal";
   private static final String LEGACY_IMPORTED_KEY = "legacy_imported";
   private static final String IMPORTED_MEMBERS_KEY = "imported_members";
   private static final String LEGACY_CANDIDATES_KEY = "legacy_import_candidates";

   private TeamJournalService() {
   }

   public static TidePlayerData loadFor(ServerPlayerEntity player) {
      PersonalTideJournal.markExtrasTrackerActive();
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         TidePlayerData data = new TidePlayerData(readJournal(context.effectiveTeam()));
         PersonalTideJournal.markExtrasTeamData(data);
         return data;
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER
            .warn("Could not resolve the FTB team journal for {}; using native Tide data for this operation", player.getGameProfile().getName(), exception);
         return loadNative(player);
      }
   }

   public static boolean saveAndSync(TidePlayerData data, ServerPlayerEntity player) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         NbtCompound journal = data.getAsTag();
         NbtCompound previousTag = readJournal(context.effectiveTeam());
         NbtCompound teamRoot = getOrCreateRoot(context.effectiveTeam());
         RecordHolderStore.updateAfterCatch(teamRoot, new TidePlayerData(previousTag), data, player.getUuid(), player.getGameProfile().getName());
         writeJournal(context.effectiveTeam(), journal);
         syncToOnlineMembers(context.effectiveTeam(), player, RecordHolderStore.attachForClient(journal, teamRoot));
         return true;
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not save the FTB team journal for {}; allowing Tide's native save path", player.getGameProfile().getName(), exception);
         return false;
      }
   }

   public static void captureCatch(TidePlayerData beforeData, TidePlayerData updatedData, ServerPlayerEntity player) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         NbtCompound root = getOrCreateRoot(context.effectiveTeam());
         boolean captured = RecordHolderStore.updateAfterCatch(root, beforeData, updatedData, player.getUuid(), player.getGameProfile().getName());
         if (captured) {
            context.effectiveTeam().markDirty();
            TideTeamJournal.LOGGER
               .info("Captured a Tide size record for {} on team {}", player.getGameProfile().getName(), context.effectiveTeam().getShortName());
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not capture Tide record ownership for {}", player.getGameProfile().getName(), exception);
      }
   }

   public static void captureCatchAfterSave(TidePlayerData beforeData, TidePlayerData afterData, ServerPlayerEntity player) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         NbtCompound root = getOrCreateRoot(context.effectiveTeam());
         boolean captured = RecordHolderStore.updateAfterCatch(root, beforeData, afterData, player.getUuid(), player.getGameProfile().getName());
         List<TeamProgressStore.RecordEvent> events = TeamProgressStore.recordCatch(
            root, beforeData, afterData, player.getUuid(), player.getGameProfile().getName(), player.getServerWorld().getTime(), ServerConfig.get()
         );
         TeamProgressStore.tideborneRecordCurrentTopFish(root, player.getUuid(), player.getGameProfile().getName());
         context.effectiveTeam().markDirty();
         if (!events.isEmpty()) {
            context.effectiveTeam().markDirty();
            if (ServerConfig.get().announcementsEnabled) {
               events.forEach(event -> broadcastEvent(context.effectiveTeam(), event));
            }
         }

         if (captured) {
            context.effectiveTeam().markDirty();
            syncToOnlineMembers(context.effectiveTeam(), player, RecordHolderStore.attachForClient(afterData.getAsTag(), root));
            TideTeamJournal.LOGGER
               .info(
                  "Captured and resynchronized a post-save Tide size record for {} on team {}",
                  player.getGameProfile().getName(),
                  context.effectiveTeam().getShortName()
               );
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not capture post-save Tide record ownership for {}", player.getGameProfile().getName(), exception);
      }
   }

   public static TeamJournalService.ClaimResult claimHeldRecord(ServerPlayerEntity player, boolean largest) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         if (!context.effectiveTeam().isPartyTeam()) {
            return TeamJournalService.ClaimResult.NOT_IN_PARTY;
         }

         ItemStack held = player.getMainHandStack();
         TidePlayerData journal = new TidePlayerData(readJournal(context.effectiveTeam()));
         RegistryEntry<Item> fish = held.isEmpty() ? null : journalFish(held);
         FishPlayerData fishData = fish == null ? null : (FishPlayerData)journal.getDataFor((Item)fish.value()).orElse(null);
         if (fishData != null
            && !fishData.stats.isEmpty()
            && !((FishStats)fishData.stats.orElseThrow()).isEmpty()
            && !(((FishStats)fishData.stats.orElseThrow()).getLargestCatch() <= 0.0)) {
            boolean officer = hasRankOrOperator(player, context.effectiveTeam(), rank(ServerConfig.get().repairMinimumRank));
            if (officer || ServerConfig.get().membersMayClaimWithExactFish && heldProvesRecord(held, fishData, largest)) {
               NbtCompound root = getOrCreateRoot(context.effectiveTeam());
               RecordHolderStore.claim(root, fish, largest, player.getUuid(), player.getGameProfile().getName());
               if (ServerConfig.get().trackRepairs) {
                  double size = largest
                     ? ((FishStats)fishData.stats.orElseThrow()).getLargestCatch()
                     : ((FishStats)fishData.stats.orElseThrow()).getSmallestCatch();
                  TeamProgressStore.recordRepair(
                     root,
                     player.getUuid(),
                     player.getGameProfile().getName(),
                     player.getUuid(),
                     player.getGameProfile().getName(),
                     Registries.ITEM.getId((Item)fish.value()),
                     size,
                     player.getServerWorld().getTime(),
                     ServerConfig.get().historyLimit
                  );
               }

               context.effectiveTeam().markDirty();
               syncToOnlineMembers(context.effectiveTeam(), player, RecordHolderStore.attachForClient(journal.getAsTag(), root));
               return TeamJournalService.ClaimResult.CLAIMED;
            } else {
               return TeamJournalService.ClaimResult.INVALID_PROOF;
            }
         } else {
            return TeamJournalService.ClaimResult.INVALID_FISH;
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not claim Tide record for {}", player.getGameProfile().getName(), exception);
         return TeamJournalService.ClaimResult.FAILED;
      }
   }

   public static TeamJournalService.ClaimAllResult claimAllRecords(ServerPlayerEntity player) {
      return claimAllRecords(player, player);
   }

   public static TeamJournalService.ClaimAllResult claimAllRecords(ServerPlayerEntity player, ServerPlayerEntity target) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         if (!context.effectiveTeam().isPartyTeam()) {
            return new TeamJournalService.ClaimAllResult(TeamJournalService.ClaimResult.NOT_IN_PARTY, 0);
         }

         if (!hasRankOrOperator(player, context.effectiveTeam(), rank(ServerConfig.get().claimAllMinimumRank))) {
            return new TeamJournalService.ClaimAllResult(TeamJournalService.ClaimResult.NOT_AUTHORIZED, 0);
         }

         if (!context.effectiveTeam().getMembers().contains(target.getUuid())) {
            return new TeamJournalService.ClaimAllResult(TeamJournalService.ClaimResult.INVALID_TARGET, 0);
         }

         TidePlayerData journal = new TidePlayerData(readJournal(context.effectiveTeam()));
         NbtCompound root = getOrCreateRoot(context.effectiveTeam());
         int claimed = RecordHolderStore.claimAll(root, journal, target.getUuid(), target.getGameProfile().getName());
         if (ServerConfig.get().trackRepairs) {
            for (Entry<RegistryEntry<Item>, FishPlayerData> entry : journal.fishPlayerData.entrySet()) {
               FishStats stats = (FishStats)entry.getValue().stats.orElse(null);
               if (stats != null && !stats.isEmpty() && stats.getLargestCatch() > 0.0) {
                  TeamProgressStore.recordRepair(
                     root,
                     player.getUuid(),
                     player.getGameProfile().getName(),
                     target.getUuid(),
                     target.getGameProfile().getName(),
                     Registries.ITEM.getId((Item)entry.getKey().value()),
                     stats.getLargestCatch(),
                     player.getServerWorld().getTime(),
                     ServerConfig.get().historyLimit
                  );
               }
            }
         }

         context.effectiveTeam().markDirty();
         syncToOnlineMembers(context.effectiveTeam(), player, RecordHolderStore.attachForClient(journal.getAsTag(), root));
         TideTeamJournal.LOGGER
            .info(
               "{} assigned every Tide size record to {} on team {}",
               new Object[]{player.getGameProfile().getName(), target.getGameProfile().getName(), context.effectiveTeam().getShortName()}
            );
         return new TeamJournalService.ClaimAllResult(TeamJournalService.ClaimResult.CLAIMED, claimed);
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not claim all Tide records for {}", player.getGameProfile().getName(), exception);
         return new TeamJournalService.ClaimAllResult(TeamJournalService.ClaimResult.FAILED, 0);
      }
   }

   public static TeamJournalService.RecordStatus getHeldRecordStatus(ServerPlayerEntity player) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         ItemStack held = player.getMainHandStack();
         TidePlayerData journal = new TidePlayerData(readJournal(context.effectiveTeam()));
         RegistryEntry<Item> fish = held.isEmpty() ? null : journalFish(held);
         if (fish != null && !journal.getDataFor((Item)fish.value()).flatMap(data -> data.stats).isEmpty()) {
            RecordHolderStore.RecordNames names = RecordHolderStore.readRecord(getOrCreateRoot(context.effectiveTeam()), fish);
            syncToOnlineMembers(
               context.effectiveTeam(), player, RecordHolderStore.attachForClient(journal.getAsTag(), getOrCreateRoot(context.effectiveTeam()))
            );
            return new TeamJournalService.RecordStatus(TeamJournalService.ClaimResult.CLAIMED, names.largest(), names.smallest());
         } else {
            return new TeamJournalService.RecordStatus(TeamJournalService.ClaimResult.INVALID_FISH, "", "");
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not inspect Tide records for {}", player.getGameProfile().getName(), exception);
         return new TeamJournalService.RecordStatus(TeamJournalService.ClaimResult.FAILED, "", "");
      }
   }

   public static TeamJournalService.ClaimResult assignHeldRecord(ServerPlayerEntity actor, ServerPlayerEntity target, boolean largest) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(actor);
         if (!context.effectiveTeam().isPartyTeam()) {
            return TeamJournalService.ClaimResult.NOT_IN_PARTY;
         }

         if (!hasRankOrOperator(actor, context.effectiveTeam(), rank(ServerConfig.get().repairMinimumRank))) {
            return TeamJournalService.ClaimResult.NOT_AUTHORIZED;
         }

         if (!context.effectiveTeam().getMembers().contains(target.getUuid())) {
            return TeamJournalService.ClaimResult.INVALID_TARGET;
         }

         ItemStack held = actor.getMainHandStack();
         TidePlayerData journal = new TidePlayerData(readJournal(context.effectiveTeam()));
         RegistryEntry<Item> fish = held.isEmpty() ? null : journalFish(held);
         FishPlayerData data = fish == null ? null : (FishPlayerData)journal.getDataFor((Item)fish.value()).orElse(null);
         if (data != null && !data.stats.isEmpty() && !((FishStats)data.stats.orElseThrow()).isEmpty()) {
            NbtCompound root = getOrCreateRoot(context.effectiveTeam());
            RecordHolderStore.claim(root, fish, largest, target.getUuid(), target.getGameProfile().getName());
            double size = largest ? ((FishStats)data.stats.orElseThrow()).getLargestCatch() : ((FishStats)data.stats.orElseThrow()).getSmallestCatch();
            if (ServerConfig.get().trackRepairs) {
               TeamProgressStore.recordRepair(
                  root,
                  actor.getUuid(),
                  actor.getGameProfile().getName(),
                  target.getUuid(),
                  target.getGameProfile().getName(),
                  Registries.ITEM.getId((Item)fish.value()),
                  size,
                  actor.getServerWorld().getTime(),
                  ServerConfig.get().historyLimit
               );
            }

            context.effectiveTeam().markDirty();
            syncToOnlineMembers(context.effectiveTeam(), actor, RecordHolderStore.attachForClient(journal.getAsTag(), root));
            TideTeamJournal.LOGGER
               .info(
                  "{} assigned {} {} record to {} on team {}",
                  new Object[]{
                     actor.getGameProfile().getName(),
                     Registries.ITEM.getId((Item)fish.value()),
                     largest ? "largest" : "smallest",
                     target.getGameProfile().getName(),
                     context.effectiveTeam().getShortName()
                  }
               );
            return TeamJournalService.ClaimResult.CLAIMED;
         } else {
            return TeamJournalService.ClaimResult.INVALID_FISH;
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Could not assign Tide record for {}", actor.getGameProfile().getName(), exception);
         return TeamJournalService.ClaimResult.FAILED;
      }
   }

   public static NbtCompound teamData(ServerPlayerEntity player, int page, String metric, String fishFilter) {
      return teamData(player, page, metric, fishFilter, "all");
   }

   public static NbtCompound teamData(ServerPlayerEntity player, int page, String metric, String fishFilter, String eventType) {
      TeamJournalService.TeamContext context = resolveAndMigrate(player);
      return TeamProgressStore.buildSnapshot(
         getOrCreateRoot(context.effectiveTeam()),
         new TidePlayerData(readJournal(context.effectiveTeam())),
         context.effectiveTeam(),
         page,
         metric,
         fishFilter,
         eventType,
         ServerConfig.get()
      );
   }

   public static void sendTeamData(ServerPlayerEntity player, int page, String metric, String fishFilter, String eventType) {
      if (ServerPlayNetworking.canSend(player, TeamDataPayload.TYPE)) {
         ServerPlayNetworking.send(player, new TeamDataPayload(teamData(player, page, metric, fishFilter, eventType)));
      }
   }

   public static void syncCurrentJournal(ServerPlayerEntity player) {
      TidePlayerData current = loadFor(player);
      if (!saveAndSync(current, player)) {
         current.syncTo(player);
      }
   }

   public static TeamJournalService.ManualMergeResult manuallyMergePersonalJournal(ServerPlayerEntity player) {
      try {
         TeamJournalService.TeamContext context = resolveAndMigrate(player);
         if (context.effectiveTeam().isPartyTeam() && !context.effectiveTeam().getId().equals(context.personalTeam().getId())) {
            NbtCompound partyRoot = getOrCreateRoot(context.effectiveTeam());
            NbtCompound personalRoot = getOrCreateRoot(context.personalTeam());
            TidePlayerData teamJournal = new TidePlayerData(readJournal(context.effectiveTeam()));
            TidePlayerData personalJournal = new TidePlayerData(readJournal(context.personalTeam()));
            NbtCompound beforeJournal = teamJournal.getAsTag();
            NbtCompound beforeRecords = partyRoot.getCompound("record_holders").copy();
            RecordHolderStore.mergeImportedRecords(partyRoot, teamJournal, personalRoot, personalJournal, player.getUuid(), player.getGameProfile().getName());
            TidePlayerData merged = JournalMerger.mergeConservatively(teamJournal, personalJournal);
            NbtCompound mergedTag = merged.getAsTag();
            boolean changed = !beforeJournal.equals(mergedTag) || !beforeRecords.equals(partyRoot.getCompound("record_holders"));
            writeJournal(context.effectiveTeam(), mergedTag);
            syncToOnlineMembers(context.effectiveTeam(), player, RecordHolderStore.attachForClient(mergedTag, partyRoot));
            return changed ? TeamJournalService.ManualMergeResult.MERGED : TeamJournalService.ManualMergeResult.ALREADY_SHARED;
         } else {
            return TeamJournalService.ManualMergeResult.NOT_IN_PARTY;
         }
      } catch (RuntimeException exception) {
         TideTeamJournal.LOGGER.warn("Manual Tide journal merge failed for {}", player.getGameProfile().getName(), exception);
         return TeamJournalService.ManualMergeResult.FAILED;
      }
   }

   private static TeamJournalService.TeamContext resolveAndMigrate(ServerPlayerEntity player) {
      TeamManager manager = FTBTeamsAPI.api().getManager();
      Team personalTeam = (Team)manager.getPlayerTeamForPlayerID(player.getUuid())
         .orElseThrow(() -> new IllegalStateException("No personal FTB team exists for " + player.getUuid()));
      Team effectiveTeam = (Team)manager.getTeamForPlayer(player)
         .orElseThrow(() -> new IllegalStateException("No effective FTB team exists for " + player.getUuid()));
      NbtCompound personalRoot = getOrCreateRoot(personalTeam);
      boolean personalChanged = false;
      if (!personalRoot.contains("journal", 10)) {
         personalRoot.put("journal", nativeJournalTag(player));
         personalChanged = true;
      }

      if (!personalRoot.getBoolean("legacy_imported")) {
         personalRoot.putBoolean("legacy_imported", true);
         personalChanged = true;
      }

      TidePlayerData personalJournal = new TidePlayerData(personalRoot.getCompound("journal"));
      personalChanged |= TeamProgressStore.ensureInitialized(personalRoot);
      personalChanged |= RecordHolderStore.backfillPersonal(personalRoot, personalJournal, player.getUuid(), player.getGameProfile().getName());
      if (personalRoot.getInt("schema_version") < 3) {
         personalRoot.putInt("schema_version", 3);
         personalChanged = true;
      }

      if (personalChanged) {
         personalTeam.markDirty();
      }

      ensureJournalExists(effectiveTeam);
      if (effectiveTeam.isPartyTeam()) {
         preparePartyMigration(effectiveTeam);
         importPersonalJournal(player, personalRoot, effectiveTeam);
      }

      return new TeamJournalService.TeamContext(personalTeam, effectiveTeam);
   }

   private static void preparePartyMigration(Team party) {
      NbtCompound root = getOrCreateRoot(party);
      boolean changed = false;
      if (root.getInt("schema_version") < 3) {
         Set<UUID> candidates = readUuidSet(root, "legacy_import_candidates");
         candidates.addAll(party.getMembers());
         writeUuidSet(root, "legacy_import_candidates", candidates);
         root.putInt("schema_version", 3);
         changed = true;
      }

      changed |= TeamProgressStore.ensureInitialized(root);
      Set<UUID> imported = readUuidSet(root, "imported_members");
      if (imported.add(party.getOwner())) {
         writeUuidSet(root, "imported_members", imported);
         changed = true;
      }

      if (changed) {
         party.markDirty();
      }
   }

   private static void importPersonalJournal(ServerPlayerEntity player, NbtCompound personalRoot, Team party) {
      NbtCompound partyRoot = getOrCreateRoot(party);
      Set<UUID> imported = readUuidSet(partyRoot, "imported_members");
      if (!imported.contains(player.getUuid())) {
         TidePlayerData teamJournal = new TidePlayerData(readJournal(party));
         TidePlayerData personalJournal = new TidePlayerData(personalRoot.getCompound("journal"));
         RecordHolderStore.mergeImportedRecords(partyRoot, teamJournal, personalRoot, personalJournal, player.getUuid(), player.getGameProfile().getName());
         Set<UUID> legacyCandidates = readUuidSet(partyRoot, "legacy_import_candidates");
         boolean alreadyRepresented = legacyCandidates.contains(player.getUuid()) && JournalMerger.isIncluded(teamJournal, personalJournal);
         if (!alreadyRepresented) {
            partyRoot.put("journal", JournalMerger.merge(teamJournal, personalJournal).getAsTag());
         }

         TeamProgressStore.mergeTrackedDataOnce(partyRoot, personalRoot, ServerConfig.get().historyLimit);
         imported.add(player.getUuid());
         legacyCandidates.remove(player.getUuid());
         writeUuidSet(partyRoot, "imported_members", imported);
         writeUuidSet(partyRoot, "legacy_import_candidates", legacyCandidates);
         partyRoot.putInt("schema_version", 3);
         party.markDirty();
         TideTeamJournal.LOGGER
            .info(
               "Imported {}'s personal Tide journal into FTB party {}{}",
               new Object[]{player.getGameProfile().getName(), party.getShortName(), alreadyRepresented ? " (legacy catches were already represented)" : ""}
            );
      }
   }

   private static void ensureJournalExists(Team team) {
      NbtCompound root = getOrCreateRoot(team);
      if (!root.contains("journal", 10)) {
         root.put("journal", new TidePlayerData().getAsTag());
         root.putInt("schema_version", 3);
         team.markDirty();
      }

      if (TeamProgressStore.ensureInitialized(root)) {
         root.putInt("schema_version", 3);
         team.markDirty();
      }
   }

   private static NbtCompound getOrCreateRoot(Team team) {
      NbtCompound extraData = team.getExtraData();
      if (!extraData.contains("tide_team_journal", 10)) {
         extraData.put("tide_team_journal", new NbtCompound());
      }

      return extraData.getCompound("tide_team_journal");
   }

   private static NbtCompound readJournal(Team team) {
      NbtCompound root = getOrCreateRoot(team);
      return root.contains("journal", 10) ? root.getCompound("journal").copy() : new TidePlayerData().getAsTag();
   }

   private static void writeJournal(Team team, NbtCompound journal) {
      NbtCompound root = getOrCreateRoot(team);
      root.putInt("schema_version", 3);
      root.put("journal", journal.copy());
      team.markDirty();
   }

   private static Set<UUID> readUuidSet(NbtCompound root, String key) {
      Set<UUID> result = new HashSet<>();
      NbtList list = root.getList(key, 8);
      list.forEach(tag -> {
         try {
            result.add(UUID.fromString(tag.asString()));
         } catch (IllegalArgumentException ignored) {
            TideTeamJournal.LOGGER.warn("Ignoring malformed UUID in team journal metadata: {}", tag);
         }
      });
      return result;
   }

   private static void writeUuidSet(NbtCompound root, String key, Set<UUID> values) {
      NbtList list = new NbtList();
      values.stream().map(UUID::toString).sorted().map(NbtString::of).forEach(list::add);
      root.put(key, list);
   }

   private static NbtCompound nativeJournalTag(ServerPlayerEntity player) {
      NbtCompound nativeRoot = Tide.PLATFORM.getPlayerData(player);
      return nativeRoot.contains("TidePlayerData", 10) ? nativeRoot.getCompound("TidePlayerData").copy() : new TidePlayerData().getAsTag();
   }

   private static TidePlayerData loadNative(ServerPlayerEntity player) {
      return TidePlayerData.getOrCreate(Tide.PLATFORM.getPlayerData(player));
   }

   private static void syncToOnlineMembers(Team team, ServerPlayerEntity initiatingPlayer, NbtCompound journal) {
      boolean initiatingPlayerIncluded = false;

      for (ServerPlayerEntity member : team.getOnlineMembers()) {
         initiatingPlayerIncluded |= member.getUuid().equals(initiatingPlayer.getUuid());
         Tide.NETWORK.sendToPlayer(new SyncPlayerDataMsg(journal.copy()), member);
         syncRecordHolders(member, journal);
      }

      if (!initiatingPlayerIncluded) {
         Tide.NETWORK.sendToPlayer(new SyncPlayerDataMsg(journal.copy()), initiatingPlayer);
         syncRecordHolders(initiatingPlayer, journal);
      }
   }

   private static void syncRecordHolders(ServerPlayerEntity player, NbtCompound packetTag) {
      if (ServerPlayNetworking.canSend(player, RecordHoldersPayload.TYPE)) {
         ServerPlayNetworking.send(player, new RecordHoldersPayload(packetTag.copy()));
      }

      syncBobberSettings(player);
   }

   public static void syncBobberSettings(ServerPlayerEntity player) {
      if (ServerPlayNetworking.canSend(player, BobberSettingsPayload.TYPE)) {
         ServerConfig.Values config = ServerConfig.get();
         NbtCompound tag = new NbtCompound();
         tag.putBoolean("enabled", config.bobberBonusesEnabled);
         tag.putBoolean("record_badges", config.recordBadgesEnabled);
         tag.putBoolean("record_tooltips", config.recordTooltipsEnabled);
         tag.putInt("fallback_luck", config.fallbackBobberBonus.luck());
         tag.putInt("fallback_speed", config.fallbackBobberBonus.lureSpeed());
         NbtCompound bonuses = new NbtCompound();
         config.bobberBonuses.forEach((id, bonus) -> {
            NbtCompound value = new NbtCompound();
            value.putInt("luck", bonus.luck());
            value.putInt("speed", bonus.lureSpeed());
            bonuses.put(id, value);
         });
         tag.put("bonuses", bonuses);
         ServerPlayNetworking.send(player, new BobberSettingsPayload(tag));
      }
   }

   public static boolean reloadServerConfig(MinecraftServer server) {
      if (!ServerConfig.load()) {
         return false;
      }

      TeamManager manager = FTBTeamsAPI.api().getManager();

      for (Team team : manager.getTeams()) {
         NbtCompound root = getOrCreateRoot(team);
         TeamProgressStore.trim(root, ServerConfig.get().historyLimit);
         team.markDirty();
      }

      for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
         syncBobberSettings(player);
      }

      return true;
   }

   private static void broadcastEvent(Team team, TeamProgressStore.RecordEvent event) {
      RecordEventPayload payload = new RecordEventPayload(event.toTag());

      for (ServerPlayerEntity member : team.getOnlineMembers()) {
         if (ServerPlayNetworking.canSend(member, RecordEventPayload.TYPE)) {
            ServerPlayNetworking.send(member, payload);
         }
      }
   }

   private static boolean heldProvesRecord(ItemStack held, FishPlayerData data, boolean largest) {
      Double length = (Double)TideItemData.FISH_LENGTH.getOptional(held).orElse(null);
      if (length != null && Double.isFinite(length) && !data.stats.isEmpty()) {
         double record = largest ? ((FishStats)data.stats.orElseThrow()).getLargestCatch() : ((FishStats)data.stats.orElseThrow()).getSmallestCatch();
         return RecordHolderStore.samePhysicalSize(length, record);
      } else {
         return false;
      }
   }

   private static RegistryEntry<Item> journalFish(ItemStack stack) {
      FishData fish = FishData.get(stack).or(() -> FishData.fromBucket(stack)).orElse(null);
      return fish == null ? null : fish.fish();
   }

   private static TeamRank rank(String configured) {
      return "owner".equalsIgnoreCase(configured) ? TeamRank.OWNER : TeamRank.OFFICER;
   }

   private static boolean hasRankOrOperator(ServerPlayerEntity player, Team team, TeamRank rank) {
      return team.getRankForPlayer(player.getUuid()).isAtLeast(rank) || ServerConfig.get().operatorBypass && player.getCommandSource().hasPermissionLevel(2);
   }

   public record ClaimAllResult(TeamJournalService.ClaimResult result, int fishCount) {
   }

   public enum ClaimResult {
      CLAIMED,
      NOT_IN_PARTY,
      INVALID_FISH,
      INVALID_PROOF,
      INVALID_TARGET,
      NOT_AUTHORIZED,
      FAILED;
   }

   public enum ManualMergeResult {
      MERGED,
      ALREADY_SHARED,
      NOT_IN_PARTY,
      FAILED;
   }

   public record RecordStatus(TeamJournalService.ClaimResult result, String largest, String smallest) {
   }

   private record TeamContext(Team personalTeam, Team effectiveTeam) {
   }
}
