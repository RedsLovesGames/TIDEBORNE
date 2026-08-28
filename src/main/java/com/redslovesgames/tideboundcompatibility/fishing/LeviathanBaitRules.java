/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import java.util.function.Supplier;
import net.minecraft.util.math.MathHelper;

public final class LeviathanBaitRules {
   private LeviathanBaitRules() {
   }

   public static int effectiveFishLuck(int normalLuck, int bonus) {
      return Math.addExact(normalLuck, bonus);
   }

   public static double selectionWeight(double baseWeight, double selectionQuality, int effectiveLuck) {
      return Math.max(0.0, baseWeight + selectionQuality * effectiveLuck);
   }

   public static float catchZone(float normalArea, double multiplier) {
      return MathHelper.clamp(normalArea * (float)multiplier, 0.05F, 1.0F);
   }

   public static float fishSpeed(float normalSpeed, double multiplier) {
      return Math.max(0.05F, normalSpeed * (float)multiplier);
   }

   public static <T> T selectCatch(boolean fishOnly, Supplier<T> normalSelector, Supplier<T> fishSelector) {
      return (fishOnly ? fishSelector : normalSelector).get();
   }
}
