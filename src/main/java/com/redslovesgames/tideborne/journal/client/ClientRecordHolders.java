/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyPersistenceIds;

import com.redslovesgames.tideborne.journal.RecordHolderStore;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public final class ClientRecordHolders {
   private static Map<Identifier, RecordHolderStore.RecordNames> records = Map.of();

   private ClientRecordHolders() {
   }

   public static void update(NbtCompound packetTag) {
      NbtCompound recordTag = packetTag.getCompound(LegacyPersistenceIds.JOURNAL_RECORD_HOLDERS);
      Map<Identifier, RecordHolderStore.RecordNames> next = new HashMap<>();

      for (String key : recordTag.getKeys()) {
         Identifier fish = Identifier.tryParse(key);
         if (fish != null) {
            next.put(fish, RecordHolderStore.readClientRecord(packetTag, fish));
         }
      }

      records = Map.copyOf(next);
      ClientJournalSpecimens.update(packetTag);
   }

   public static RecordHolderStore.RecordNames get(Identifier fish) {
      return records.getOrDefault(fish, RecordHolderStore.RecordNames.EMPTY);
   }
}
