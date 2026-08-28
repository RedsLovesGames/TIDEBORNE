/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;

public final class RecordHolderStore {
   static final String ROOT_KEY = "record_holders";
   public static final String CLIENT_KEY = "tide_team_journal_record_holders";
   private static final String LARGEST_UUID = "largest_uuid";
   private static final String LARGEST_NAME = "largest_name";
   private static final String SMALLEST_UUID = "smallest_uuid";
   private static final String SMALLEST_NAME = "smallest_name";

   private RecordHolderStore() {
   }

   static boolean backfillPersonal(NbtCompound root, TidePlayerData journal, UUID playerId, String playerName) {
      NbtCompound records = records(root);
      boolean[] changed = new boolean[]{false};
      journal.fishPlayerData.forEach((item, data) -> data.stats.filter(stats -> !stats.isEmpty() && stats.getLargestCatch() > 0.0).ifPresent(stats -> {
         NbtCompound record = record(records, item);
         if (!record.containsUuid("largest_uuid")) {
            putHolder(record, true, playerId, playerName);
            changed[0] = true;
         }

         if (!record.containsUuid("smallest_uuid")) {
            putHolder(record, false, playerId, playerName);
            changed[0] = true;
         }
      }));
      return changed[0];
   }

   static boolean updateAfterCatch(NbtCompound root, TidePlayerData before, TidePlayerData after, UUID playerId, String playerName) {
      NbtCompound records = records(root);
      NbtCompound beforeRecords = records.copy();
      records.getKeys().removeIf(key -> !hasSizedStats(after, key));

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : after.fishPlayerData.entrySet()) {
         Optional<FishStats> afterStats = realStats(entry.getValue());
         if (!afterStats.isEmpty() && !(afterStats.orElseThrow().getLargestCatch() <= 0.0)) {
            Optional<FishStats> beforeStats = Optional.ofNullable((FishPlayerData)before.fishPlayerData.get(entry.getKey()))
               .flatMap(RecordHolderStore::realStats);
            FishStats next = afterStats.orElseThrow();
            int previousCount = beforeStats.<Integer>map(FishStats::getAmountCaught).orElse(0);
            if (next.getAmountCaught() > previousCount) {
               NbtCompound record = record(records, entry.getKey());
               if (beforeStats.isEmpty() || next.getLargestCatch() > beforeStats.orElseThrow().getLargestCatch()) {
                  putHolder(record, true, playerId, playerName);
               }

               if (beforeStats.isEmpty() || next.getSmallestCatch() < beforeStats.orElseThrow().getSmallestCatch()) {
                  putHolder(record, false, playerId, playerName);
               }
            }
         }
      }

      return !beforeRecords.equals(records);
   }

   static boolean captureLoggedCatch(
      NbtCompound root, RegistryEntry<Item> fish, Optional<FishStats> beforeStats, FishStats afterStats, UUID playerId, String playerName
   ) {
      int previousCount = beforeStats.<Integer>map(FishStats::getAmountCaught).orElse(0);
      if (!afterStats.isEmpty() && afterStats.getAmountCaught() > previousCount && !(afterStats.getLargestCatch() <= 0.0)) {
         NbtCompound record = record(records(root), fish);
         boolean changed = false;
         if (beforeStats.isEmpty() || beforeStats.orElseThrow().isEmpty() || afterStats.getLargestCatch() > beforeStats.orElseThrow().getLargestCatch()) {
            putHolder(record, true, playerId, playerName);
            changed = true;
         }

         if (beforeStats.isEmpty() || beforeStats.orElseThrow().isEmpty() || afterStats.getSmallestCatch() < beforeStats.orElseThrow().getSmallestCatch()) {
            putHolder(record, false, playerId, playerName);
            changed = true;
         }

         return changed;
      } else {
         return false;
      }
   }

   static List<RecordHolderStore.RecordChange> findRecordChanges(TidePlayerData before, TidePlayerData after) {
      List<RecordHolderStore.RecordChange> changes = new ArrayList<>();

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : after.fishPlayerData.entrySet()) {
         Optional<FishStats> afterStats = realStats(entry.getValue());
         if (!afterStats.isEmpty() && !(afterStats.orElseThrow().getLargestCatch() <= 0.0)) {
            Optional<FishStats> beforeStats = Optional.ofNullable((FishPlayerData)before.fishPlayerData.get(entry.getKey()))
               .flatMap(RecordHolderStore::realStats);
            FishStats next = afterStats.orElseThrow();
            int previousCount = beforeStats.<Integer>map(FishStats::getAmountCaught).orElse(0);
            if (next.getAmountCaught() > previousCount) {
               if (!beforeStats.isEmpty() && !beforeStats.orElseThrow().isEmpty() && !(beforeStats.orElseThrow().getLargestCatch() <= 0.0)) {
                  FishStats previous = beforeStats.orElseThrow();
                  if (next.getLargestCatch() > previous.getLargestCatch()) {
                     changes.add(
                        new RecordHolderStore.RecordChange(
                           entry.getKey(), RecordHolderStore.RecordKind.LARGEST, next.getLargestCatch(), previous.getLargestCatch()
                        )
                     );
                  }

                  if (next.getSmallestCatch() < previous.getSmallestCatch()) {
                     changes.add(
                        new RecordHolderStore.RecordChange(
                           entry.getKey(), RecordHolderStore.RecordKind.SMALLEST, next.getSmallestCatch(), previous.getSmallestCatch()
                        )
                     );
                  }
               } else {
                  changes.add(new RecordHolderStore.RecordChange(entry.getKey(), RecordHolderStore.RecordKind.FIRST, next.getLargestCatch(), 0.0));
               }
            }
         }
      }

      return List.copyOf(changes);
   }

   static void claim(NbtCompound root, RegistryEntry<Item> fish, boolean largest, UUID playerId, String playerName) {
      putHolder(record(records(root), fish), largest, playerId, playerName);
   }

   static int claimAll(NbtCompound root, TidePlayerData journal, UUID playerId, String playerName) {
      NbtCompound records = records(root);
      int claimed = 0;

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : journal.fishPlayerData.entrySet()) {
         Optional<FishStats> stats = realStats(entry.getValue());
         if (!stats.isEmpty() && !(stats.orElseThrow().getLargestCatch() <= 0.0)) {
            NbtCompound record = record(records, entry.getKey());
            putHolder(record, true, playerId, playerName);
            putHolder(record, false, playerId, playerName);
            claimed++;
         }
      }

      return claimed;
   }

   static void mergeImportedRecords(
      NbtCompound targetRoot, TidePlayerData targetJournal, NbtCompound sourceRoot, TidePlayerData sourceJournal, UUID fallbackId, String fallbackName
   ) {
      NbtCompound targetRecords = records(targetRoot);
      NbtCompound sourceRecords = records(sourceRoot);

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : sourceJournal.fishPlayerData.entrySet()) {
         Optional<FishStats> incomingStats = realStats(entry.getValue());
         if (!incomingStats.isEmpty() && !(incomingStats.orElseThrow().getLargestCatch() <= 0.0)) {
            Optional<FishStats> existingStats = Optional.ofNullable((FishPlayerData)targetJournal.fishPlayerData.get(entry.getKey()))
               .flatMap(RecordHolderStore::realStats);
            FishStats incoming = incomingStats.orElseThrow();
            String fishKey = key(entry.getKey());
            NbtCompound sourceRecord = sourceRecords.getCompound(fishKey);
            NbtCompound targetRecord = record(targetRecords, entry.getKey());
            if (existingStats.isEmpty()
               || incoming.getLargestCatch() > existingStats.orElseThrow().getLargestCatch()
               || incoming.getLargestCatch() == existingStats.orElseThrow().getLargestCatch() && !targetRecord.containsUuid("largest_uuid")) {
               copyHolder(sourceRecord, targetRecord, true, fallbackId, fallbackName);
            }

            if (existingStats.isEmpty()
               || incoming.getSmallestCatch() < existingStats.orElseThrow().getSmallestCatch()
               || incoming.getSmallestCatch() == existingStats.orElseThrow().getSmallestCatch() && !targetRecord.containsUuid("smallest_uuid")) {
               copyHolder(sourceRecord, targetRecord, false, fallbackId, fallbackName);
            }
         }
      }
   }

   static NbtCompound attachForClient(NbtCompound journal, NbtCompound teamRoot) {
      NbtCompound packetTag = journal.copy();
      packetTag.put("tide_team_journal_record_holders", records(teamRoot).copy());
      return packetTag;
   }

   public static RecordHolderStore.RecordNames readClientRecord(NbtCompound packetTag, Identifier fish) {
      NbtCompound records = packetTag.getCompound("tide_team_journal_record_holders");
      NbtCompound record = records.getCompound(fish.toString());
      return new RecordHolderStore.RecordNames(record.getString("largest_name"), record.getString("smallest_name"));
   }

   static RecordHolderStore.RecordNames readRecord(NbtCompound root, RegistryEntry<Item> fish) {
      NbtCompound record = records(root).getCompound(key(fish));
      return new RecordHolderStore.RecordNames(record.getString("largest_name"), record.getString("smallest_name"));
   }

   static int countActiveRecords(NbtCompound root, UUID playerId) {
      int count = 0;
      NbtCompound all = records(root);

      for (String key : all.getKeys()) {
         NbtCompound record = all.getCompound(key);
         if (record.containsUuid("largest_uuid") && record.getUuid("largest_uuid").equals(playerId)) {
            count++;
         }

         if (record.containsUuid("smallest_uuid") && record.getUuid("smallest_uuid").equals(playerId)) {
            count++;
         }
      }

      return count;
   }

   private static Optional<FishStats> realStats(FishPlayerData data) {
      return data.stats.filter(stats -> !stats.isEmpty());
   }

   private static boolean hasSizedStats(TidePlayerData journal, String fishKey) {
      return journal.fishPlayerData
         .entrySet()
         .stream()
         .filter(entry -> key((RegistryEntry<Item>)entry.getKey()).equals(fishKey))
         .map(Entry::getValue)
         .map(RecordHolderStore::realStats)
         .flatMap(Optional::stream)
         .anyMatch(stats -> stats.getLargestCatch() > 0.0);
   }

   private static NbtCompound records(NbtCompound root) {
      if (!root.contains("record_holders", 10)) {
         root.put("record_holders", new NbtCompound());
      }

      return root.getCompound("record_holders");
   }

   private static NbtCompound record(NbtCompound records, RegistryEntry<Item> item) {
      String key = key(item);
      if (!records.contains(key, 10)) {
         records.put(key, new NbtCompound());
      }

      return records.getCompound(key);
   }

   private static String key(RegistryEntry<Item> item) {
      return Registries.ITEM.getId((Item)item.value()).toString();
   }

   private static void copyHolder(NbtCompound source, NbtCompound target, boolean largest, UUID fallbackId, String fallbackName) {
      String uuidKey = largest ? "largest_uuid" : "smallest_uuid";
      String nameKey = largest ? "largest_name" : "smallest_name";
      if (source.containsUuid(uuidKey)) {
         putHolder(target, largest, source.getUuid(uuidKey), source.getString(nameKey));
      } else {
         putHolder(target, largest, fallbackId, fallbackName);
      }
   }

   private static void putHolder(NbtCompound record, boolean largest, UUID id, String name) {
      record.putUuid(largest ? "largest_uuid" : "smallest_uuid", id);
      record.putString(largest ? "largest_name" : "smallest_name", name);
   }

   record RecordChange(RegistryEntry<Item> fish, RecordHolderStore.RecordKind kind, double newSize, double previousSize) {
      double improvement() {
         return Math.abs(this.newSize - this.previousSize);
      }
   }

   enum RecordKind {
      FIRST,
      LARGEST,
      SMALLEST;
   }

   public record RecordNames(String largest, String smallest) {
      public static final RecordHolderStore.RecordNames EMPTY = new RecordHolderStore.RecordNames("", "");
   }
}
