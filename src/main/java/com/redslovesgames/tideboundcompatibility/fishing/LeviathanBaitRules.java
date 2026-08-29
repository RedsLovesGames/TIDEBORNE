/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.math.MathHelper;

public final class LeviathanBaitRules {
   private LeviathanBaitRules() {
   }

   public static int effectiveFishLuck(int normalLuck, int bonus) {
      return Math.addExact(normalLuck, bonus);
   }

   public static float catchZone(float normalArea, double multiplier) {
      return MathHelper.clamp(normalArea * (float)multiplier, 0.05F, 1.0F);
   }

   public static float fishSpeed(float normalSpeed, double multiplier) {
      return Math.max(0.05F, normalSpeed * (float)multiplier);
   }

   public static boolean isFishOnlyCatchPool(FishingGearModifiers modifiers) {
      Objects.requireNonNull(modifiers, "modifiers");
      FishingGearModifiers.IdRestriction categories = modifiers.categoryRestriction();
      return categories.allowListActive()
         && categories.allowedIds().size() == 1
         && categories.allows(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY);
   }

   public static <T> T selectCatch(
      FishingGearModifiers modifiers,
      Supplier<T> normalSelector,
      Supplier<T> fishSelector
   ) {
      Objects.requireNonNull(normalSelector, "normalSelector");
      Objects.requireNonNull(fishSelector, "fishSelector");
      return (isFishOnlyCatchPool(modifiers) ? fishSelector : normalSelector).get();
   }
}
