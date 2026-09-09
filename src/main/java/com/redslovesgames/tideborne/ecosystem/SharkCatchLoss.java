/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.ecosystem;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.registry.TideboundTags;
import net.minecraft.item.ItemStack;

public final class SharkCatchLoss {
   private SharkCatchLoss() {
   }

   public static double probability(ItemStack fish) {
      TideboundConfig.Values c = TideboundConfig.get();
      double scentBonus = Math.min(
         c.sharkTheftScentBonusCap, Math.max(0.0, SharkCatchLoss.SharkScentManagerBridge.scent(fish) - 1.0) * c.sharkTheftScentChancePerStrength
      );
      double chance = c.sharkTheftBaseChance + scentBonus;
      if (isLarge(fish)) {
         chance += c.sharkTheftLargeFishBonus;
      }

      if (fish.isIn(TideboundTags.TUNA)) {
         chance += c.sharkTheftTunaBonus;
      }

      return Math.min(c.sharkTheftMaximumChance, chance);
   }

   public static boolean isLarge(ItemStack fish) {
      return fish.isIn(TideboundTags.LARGE_FISH)
         || fish.isIn(Items.LEGENDARY_FISH)
         || FishData.get(fish).map(data -> data.getAverageLength() >= 100.0).orElse(false);
   }

   private static final class SharkScentManagerBridge {
      private static double scent(ItemStack fish) {
         return SharkScentManager.fishScent(fish);
      }
   }
}
