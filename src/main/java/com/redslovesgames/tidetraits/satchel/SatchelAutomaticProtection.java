/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.journal.FishRarity;
import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SatchelAutomaticProtection {
   private SatchelAutomaticProtection() {
   }

   public static boolean applyInsertedRange(ItemStack satchel, ItemStack specimen, PlayerEntity player, int firstSlot, int insertedCount) {
      if (insertedCount > 0 && firstSlot >= 0 && specimen != null && !specimen.isEmpty()) {
         SatchelState state = AnglersSatchelStorage.state(satchel);
         if (!SatchelProtectionPolicy.shouldProtect(state, evidence(specimen, player))) {
            return false;
         }

         for (int offset = 0; offset < insertedCount; offset++) {
            if (!AnglersSatchelStorage.setProtected(satchel, firstSlot + offset, true)) {
               throw new IllegalStateException("Could not protect committed satchel slot " + (firstSlot + offset));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static SatchelProtectionPolicy.Evidence evidence(ItemStack specimen, PlayerEntity player) {
      String mutation = (String)specimen.getOrDefault(TideTraitsComponents.MUTATION, "normal");
      double percentile = (Double)specimen.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
      boolean legendaryRarity = safeFishData(specimen).map(data -> data.profile().rarity() == FishRarity.LEGENDARY).orElse(false);
      boolean largest = false;
      boolean smallest = false;
      if (player instanceof ServerPlayerEntity serverPlayer) {
         Optional<FishStats> stats = currentStats(specimen, serverPlayer);
         double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(specimen, 0.0);
         if (stats.isPresent() && Double.isFinite(length) && length > 0.0) {
            largest = sameLength(length, stats.get().getLargestCatch());
            smallest = sameLength(length, stats.get().getSmallestCatch());
         }
      }

      return new SatchelProtectionPolicy.Evidence(TraitAxesRuntime.isSpecial(specimen), percentile, largest, smallest, legendaryRarity);
   }

   private static Optional<FishData> safeFishData(ItemStack specimen) {
      try {
         return FishData.get(specimen);
      } catch (RuntimeException | LinkageError exception) {
         return Optional.empty();
      }
   }

   private static Optional<FishStats> currentStats(ItemStack specimen, ServerPlayerEntity player) {
      try {
         return PersonalTideJournal.statsFor(player, specimen);
      } catch (RuntimeException | LinkageError exception) {
         return Optional.empty();
      }
   }

   private static boolean sameLength(double first, double second) {
      return Double.isFinite(second) && Math.abs(first - second) <= Math.max(1.0E-6, Math.ulp(second) * 4.0);
   }
}
