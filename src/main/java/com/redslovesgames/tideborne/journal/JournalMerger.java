/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal;

import com.li64.tide.data.player.CatchTimestamp;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import java.util.Comparator;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

final class JournalMerger {
   private static final Comparator<CatchTimestamp> TIMESTAMP_ORDER = Comparator.comparing(CatchTimestamp::date).thenComparingLong(CatchTimestamp::ticks);

   private JournalMerger() {
   }

   static TidePlayerData merge(TidePlayerData existing, TidePlayerData incoming) {
      return merge(existing, incoming, false);
   }

   static TidePlayerData mergeConservatively(TidePlayerData existing, TidePlayerData incoming) {
      return merge(existing, incoming, true);
   }

   private static TidePlayerData merge(TidePlayerData existing, TidePlayerData incoming, boolean conservative) {
      TidePlayerData merged = new TidePlayerData(existing.getAsTag());
      merged.gotJournal = merged.gotJournal | incoming.gotJournal;

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : incoming.fishPlayerData.entrySet()) {
         merged.fishPlayerData
            .merge(entry.getKey(), copy(entry.getValue()), conservative ? JournalMerger::mergeFishDataConservatively : JournalMerger::mergeFishData);
      }

      return merged;
   }

   static boolean isIncluded(TidePlayerData team, TidePlayerData personal) {
      if (personal.gotJournal && !team.gotJournal) {
         return false;
      }

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : personal.fishPlayerData.entrySet()) {
         FishPlayerData incoming = entry.getValue();
         FishPlayerData existing = (FishPlayerData)team.fishPlayerData.get(entry.getKey());
         if (existing == null
            || incoming.isUnlocked && !existing.isUnlocked
            || incoming.isUnread && !existing.isUnread
            || incoming.hasNote && !existing.hasNote) {
            return false;
         }

         if (!statsIncluded(existing.stats, incoming.stats)) {
            return false;
         }
      }

      return true;
   }

   private static boolean statsIncluded(Optional<FishStats> existing, Optional<FishStats> incoming) {
      if (incoming.isEmpty() || incoming.orElseThrow().isEmpty()) {
         return true;
      } else if (!existing.isEmpty() && !existing.orElseThrow().isEmpty()) {
         FishStats team = existing.orElseThrow();
         FishStats personal = incoming.orElseThrow();
         boolean dateIncluded = personal.getInitialCatchDate()
            .map(personalDate -> team.getInitialCatchDate().map(teamDate -> TIMESTAMP_ORDER.compare(teamDate, personalDate) <= 0).orElse(false))
            .orElse(true);
         return team.getAmountCaught() >= personal.getAmountCaught()
            && team.getLargestCatch() >= personal.getLargestCatch()
            && team.getSmallestCatch() <= personal.getSmallestCatch()
            && dateIncluded;
      } else {
         return false;
      }
   }

   private static FishPlayerData mergeFishData(FishPlayerData existing, FishPlayerData incoming) {
      return new FishPlayerData(
         existing.isUnlocked || incoming.isUnlocked,
         existing.isUnread || incoming.isUnread,
         existing.hasNote || incoming.hasNote,
         mergeStats(existing.stats, incoming.stats)
      );
   }

   private static FishPlayerData mergeFishDataConservatively(FishPlayerData existing, FishPlayerData incoming) {
      return new FishPlayerData(
         existing.isUnlocked || incoming.isUnlocked,
         existing.isUnread || incoming.isUnread,
         existing.hasNote || incoming.hasNote,
         mergeStatsConservatively(existing.stats, incoming.stats)
      );
   }

   static Optional<FishStats> mergeStats(Optional<FishStats> existing, Optional<FishStats> incoming) {
      return mergeStats(existing, incoming, false);
   }

   static Optional<FishStats> mergeStatsConservatively(Optional<FishStats> existing, Optional<FishStats> incoming) {
      return mergeStats(existing, incoming, true);
   }

   private static Optional<FishStats> mergeStats(Optional<FishStats> existing, Optional<FishStats> incoming, boolean conservative) {
      if (existing.isEmpty()) {
         return incoming.map(JournalMerger::copy);
      }

      if (incoming.isEmpty()) {
         return existing.map(JournalMerger::copy);
      }

      FishStats first = existing.orElseThrow();
      FishStats second = incoming.orElseThrow();
      if (first.isEmpty()) {
         return Optional.of(copy(second));
      }

      if (second.isEmpty()) {
         return Optional.of(copy(first));
      }

      long total = conservative ? Math.max(first.getAmountCaught(), second.getAmountCaught()) : (long)first.getAmountCaught() + second.getAmountCaught();
      Optional<CatchTimestamp> earliest = first.getInitialCatchDate()
         .flatMap(
            firstDate -> second.getInitialCatchDate()
               .map(secondDate -> TIMESTAMP_ORDER.compare(firstDate, secondDate) <= 0 ? firstDate : secondDate)
               .or(() -> Optional.of(firstDate))
         )
         .or(second::getInitialCatchDate);
      return Optional.of(
         new FishStats(
            (int)Math.min(2147483647L, total),
            earliest,
            Math.max(first.getLargestCatch(), second.getLargestCatch()),
            Math.min(first.getSmallestCatch(), second.getSmallestCatch())
         )
      );
   }

   private static FishPlayerData copy(FishPlayerData data) {
      return new FishPlayerData(data.isUnlocked, data.isUnread, data.hasNote, data.stats.map(JournalMerger::copy));
   }

   private static FishStats copy(FishStats stats) {
      return new FishStats(stats.getAmountCaught(), stats.getInitialCatchDate(), stats.getLargestCatch(), stats.getSmallestCatch());
   }
}
