/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import java.util.Arrays;
import java.util.Optional;

public enum SatchelCapacityLevel {
   BASE(0, 1.0, 0),
   LEVEL_ONE(1, 1.5, 150),
   LEVEL_TWO(2, 2.0, 450),
   LEVEL_THREE(3, 3.0, 1000);

   private final int level;
   private final double multiplier;
   private final int xpCost;

   SatchelCapacityLevel(int level, double multiplier, int xpCost) {
      this.level = level;
      this.multiplier = multiplier;
      this.xpCost = xpCost;
   }

   public int level() {
      return this.level;
   }

   public double multiplier() {
      return TideTraitsConfigManager.current().satchel().capacityMultipliers().get(this.level);
   }

   public int xpCost() {
      return TideTraitsConfigManager.current().satchel().capacityXpCosts().get(this.level);
   }

   public Optional<SatchelCapacityLevel> next() {
      return byLevel(this.level + 1);
   }

   public static Optional<SatchelCapacityLevel> byLevel(int level) {
      return Arrays.stream(values()).filter(value -> value.level == level).findFirst();
   }
}
