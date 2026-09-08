/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import com.li64.tide.registries.TideItems;
import com.li64.tide.registries.items.FishSatchelItem;

public final class SatchelCapacity {
   public static final double MAX_MULTIPLIER = 4.0;

   private SatchelCapacity() {
   }

   public static int baseCapacity() {
      return SatchelCapacity.BaseCapacityHolder.VALUE;
   }

   public static int forLevel(int level) {
      SatchelCapacityLevel capacityLevel = SatchelCapacityLevel.byLevel(level).orElse(SatchelCapacityLevel.BASE);
      return effectiveCapacity(baseCapacity(), capacityLevel.multiplier());
   }

   public static int effectiveCapacity(int baseCapacity, double multiplier) {
      if (baseCapacity <= 0) {
         throw new IllegalArgumentException("baseCapacity must be positive");
      } else if (Double.isFinite(multiplier) && !(multiplier <= 0.0)) {
         double boundedMultiplier = Math.min(4.0, multiplier);
         long rounded = Math.round(baseCapacity * boundedMultiplier);
         long maximum = Math.multiplyExact(baseCapacity, 4L);
         return Math.toIntExact(Math.max(1L, Math.min(rounded, maximum)));
      } else {
         throw new IllegalArgumentException("multiplier must be finite and positive");
      }
   }

   private static final class BaseCapacityHolder {
      private static final int VALUE = resolve();

      private static int resolve() {
         int resolved = FishSatchelItem.getRemainingSlots(TideItems.FISH_SATCHEL.getDefaultStack());
         if (resolved <= 0) {
            throw new IllegalStateException("Tide reported a non-positive Fish Satchel base capacity: " + resolved);
         } else {
            return resolved;
         }
      }
   }
}
