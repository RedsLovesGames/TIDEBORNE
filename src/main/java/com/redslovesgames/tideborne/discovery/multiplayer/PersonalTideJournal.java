/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.discovery.multiplayer;

import com.li64.tide.Tide;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.CatchTimestamp;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.journal.JournalSpecimenStore;
import com.redslovesgames.tideborne.fishing.TideTraits;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

public final class PersonalTideJournal {
   private static final String TIDE_PLAYER_DATA_KEY = "TidePlayerData";
   private static final Set<TidePlayerData> EXTRAS_TEAM_DATA = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
   private static final WarningThrottle WARNINGS = new WarningThrottle();
   private static volatile boolean extrasTrackerActive;

   private PersonalTideJournal() {
   }

   public static void markExtrasTrackerActive() {
      extrasTrackerActive = true;
   }

   public static void markExtrasTeamData(TidePlayerData data) {
      if (data != null) {
         EXTRAS_TEAM_DATA.add(data);
      }
   }

   public static boolean extrasTrackerActive() {
      return extrasTrackerActive;
   }

   public static boolean isExtrasTeamData(TidePlayerData data) {
      return data != null && EXTRAS_TEAM_DATA.contains(data);
   }

   public static Optional<TidePlayerData> load(ServerPlayerEntity player) {
      if (player == null) {
         return Optional.empty();
      }

      try {
         NbtCompound root = Tide.PLATFORM.getPlayerData(player);
         JournalSpecimenStore.migrateLegacyJournal(root);
         return Optional.of(TidePlayerData.getOrCreate(root));
      } catch (RuntimeException | LinkageError failure) {
         warn(player, "load personal Tide journal", failure);
         return Optional.empty();
      }
   }

   /**
    * Loads native Tide journal data for read-only record lookups without running canonical legacy
    * backfill. Satchel opening only needs aggregate largest/smallest stats, so doing migration here
    * would add avoidable work to the screen-open path.
    */
   public static Optional<TidePlayerData> loadForRecordLookup(ServerPlayerEntity player) {
      if (player == null) {
         return Optional.empty();
      }

      try {
         return Optional.of(TidePlayerData.getOrCreate(Tide.PLATFORM.getPlayerData(player)));
      } catch (RuntimeException | LinkageError failure) {
         warn(player, "read personal Tide journal records", failure);
         return Optional.empty();
      }
   }

   public static Optional<FishStats> statsFor(TidePlayerData personalData, ItemStack specimen) {
      if (personalData != null && specimen != null && !specimen.isEmpty()) {
         try {
            return FishData.get(specimen)
               .flatMap(fish -> personalData.getDataFor((Item)fish.fish().value()))
               .flatMap(value -> value.stats)
               .filter(stats -> !stats.isEmpty());
         } catch (RuntimeException | LinkageError failure) {
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public static Optional<FishStats> statsFor(ServerPlayerEntity player, ItemStack specimen) {
      return load(player).flatMap(data -> statsFor(data, specimen));
   }

   public static PersonalTideJournal.CatchContext prepareCatch(TidePlayerData routedData, ItemStack caught, ServerPlayerEntity player) {
      if (routedData == null || caught == null || caught.isEmpty() || player == null) {
         return PersonalTideJournal.CatchContext.unavailable();
      } else {
         return !isExtrasTeamData(routedData)
            ? new PersonalTideJournal.CatchContext(false, PersonalTideJournal.RecordBefore.capture(routedData, caught))
            : new PersonalTideJournal.CatchContext(
               true, load(player).map(data -> PersonalTideJournal.RecordBefore.capture(data, caught)).orElseGet(PersonalTideJournal.RecordBefore::unavailable)
            );
      }
   }

   public static PersonalTideJournal.RecordBefore completeCatch(
      PersonalTideJournal.CatchContext context, ItemStack caught, ServerPlayerEntity player, World level
   ) {
      if (context == null) {
         return PersonalTideJournal.RecordBefore.unavailable();
      }

      PersonalTideJournal.RecordBefore before = context.before();
      if (context.teamRouted()) {
         if (!before.available() || caught == null || caught.isEmpty() || player == null || level == null) {
            return PersonalTideJournal.RecordBefore.unavailable();
         }

         try {
            NbtCompound root = Tide.PLATFORM.getPlayerData(player);
            TidePlayerData personalData = TidePlayerData.getOrCreate(root);
            if (!mirrorCatch(personalData, caught, level)) {
               return PersonalTideJournal.RecordBefore.unavailable();
            }

            root.put(TIDE_PLAYER_DATA_KEY, personalData.getAsTag());
            persistCanonicalCatch(root, caught, before);
            return before;
         } catch (RuntimeException | LinkageError failure) {
            warn(player, "update personal Tide journal", failure);
            return PersonalTideJournal.RecordBefore.unavailable();
         }
      }

      if (caught != null && !caught.isEmpty() && player != null) {
         try {
            persistCanonicalCatch(Tide.PLATFORM.getPlayerData(player), caught, before);
         } catch (RuntimeException | LinkageError failure) {
            warn(player, "persist canonical personal Tide journal specimen", failure);
         }
      }
      return before;
   }

   private static boolean mirrorCatch(TidePlayerData personalData, ItemStack caught, World level) {
      FishData fish = (FishData)FishData.get(caught).orElse(null);
      if (fish == null) {
         return false;
      }

      RegistryEntry<Item> fishHolder = fish.fish();
      FishPlayerData entry = (FishPlayerData)personalData.fishPlayerData.get(fishHolder);
      if (entry == null) {
         entry = new FishPlayerData(true, true, false, Optional.empty());
         personalData.fishPlayerData.put(fishHolder, entry);
      } else if (!entry.isUnlocked) {
         entry.isUnlocked = true;
         entry.isUnread = true;
      }

      FishStats stats = (FishStats)entry.stats.orElseGet(FishStats::new);
      if (fish.size().isPresent()) {
         double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(caught, 0.0);
         stats.logCatch(CatchTimestamp.now(level), length);
      }

      entry.stats = Optional.of(stats);
      return true;
   }

   private static void persistCanonicalCatch(NbtCompound root, ItemStack caught, PersonalTideJournal.RecordBefore before) {
      SpecimenData specimen = CanonicalSpecimenStorage.read(caught).orElse(null);
      if (root == null || specimen == null) {
         return;
      }

      double length = specimen.finalLength();
      boolean largest = before.available()
         && (!before.hadStats() || length > before.largest() + tolerance(before.largest()));
      boolean smallest = before.available()
         && (!before.hadStats() || length < before.smallest() - tolerance(before.smallest()));
      JournalSpecimenStore.capture(root, specimen, largest, smallest);
   }

   private static double tolerance(double value) {
      return Double.isFinite(value) ? Math.max(1.0E-6, Math.ulp(value) * 4.0) : 0.0;
   }

   private static void warn(ServerPlayerEntity player, String operation, Throwable failure) {
      String key = operation + ":" + player.getUuid();
      if (WARNINGS.rateLimited(key)) {
         TideTraits.LOGGER
            .warn(
               "Could not {}; Multiplayer Extras team behavior continues, but personal record features are unavailable for this operation", operation, failure
            );
      }
   }

   public record CatchContext(boolean teamRouted, PersonalTideJournal.RecordBefore before) {
      public CatchContext {
         before = before == null ? PersonalTideJournal.RecordBefore.unavailable() : before;
      }

      public static PersonalTideJournal.CatchContext unavailable() {
         return new PersonalTideJournal.CatchContext(false, PersonalTideJournal.RecordBefore.unavailable());
      }
   }

   public record RecordBefore(boolean available, boolean hadStats, double largest, double smallest) {
      public static PersonalTideJournal.RecordBefore unavailable() {
         return new PersonalTideJournal.RecordBefore(false, false, 0.0, 1.0 / 0.0);
      }

      private static PersonalTideJournal.RecordBefore capture(TidePlayerData data, ItemStack stack) {
         FishStats stats = PersonalTideJournal.statsFor(data, stack).orElse(null);
         return stats == null
            ? new PersonalTideJournal.RecordBefore(true, false, 0.0, 1.0 / 0.0)
            : new PersonalTideJournal.RecordBefore(true, true, stats.getLargestCatch(), stats.getSmallestCatch());
      }
   }
}
