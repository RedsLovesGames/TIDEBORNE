/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.Tide;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.data.fishing.MinigameBehavior;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FightProfileService;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.v2.TideFishingLineModifiers;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalCatchStateManager;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import net.minecraft.util.math.MathHelper;

public final class FishingModifiers {
   private static final FightProfileService CANONICAL_FIGHTS = new FightProfileService();

   private FishingModifiers() {
   }

   public static double modifyFishWeight(FishData data, FishingContext context, double original) {
      FishingGearModifiers gear = TideborneFishingGearModifiers.forFishWeight(data, context, TideboundConfig.get());
      return original * FishingGearEffects.fishWeightMultiplier(gear);
   }

   public static double modifyCrateWeight(FishingContext context, double original) {
      FishingGearModifiers gear = TideborneFishingGearModifiers.forCrateWeight(context, TideboundConfig.get());
      return original * FishingGearEffects.crateWeightMultiplier(gear);
   }

   public static FishingModifiers.MinigameValues modifyMinigame(TideFishingHook hook, byte behavior, float area, float speed) {
      var canonical = CanonicalCatchStateManager.get(hook);
      if (canonical.isPresent()) {
         var fightProfile = canonical.get().fightProfile();
         FishingGearModifiers tideLineGear = TideFishingLineModifiers.forLine(hook.getLine());
         double strength = fightProfile.strength() * tideLineGear.strengthMultiplier();
         double tempo = fightProfile.tempo() * tideLineGear.tempoMultiplier();
         behavior = canonicalBehavior(fightProfile.behavior(), behavior);

         area = (float)CANONICAL_FIGHTS.catchZoneArea(strength);
         speed = (float)Math.max(
            FightProfileService.MIN_FINAL_TEMPO,
            tempo * Tide.SERVER_CONFIG.minigame.minigameDifficultyMultiplier
         );
      }

      FishingGearModifiers customGear = TideborneFishingGearModifiers.forMinigame(hook, TideboundConfig.get());
      area *= (float)FishingGearEffects.catchZoneAreaMultiplier(customGear);
      speed *= (float)FishingGearEffects.minigameSpeedMultiplier(customGear);
      return new FishingModifiers.MinigameValues(
         behavior,
         MathHelper.clamp(area, 0.05F, 1.0F),
         Math.max(0.05F, speed)
      );
   }

   private static byte canonicalBehavior(String behavior, byte fallback) {
      if (behavior == null || behavior.isBlank()) {
         return fallback;
      }

      for (MinigameBehavior candidate : MinigameBehavior.values()) {
         if (candidate.name().equalsIgnoreCase(behavior) || candidate.toString().equalsIgnoreCase(behavior)) {
            return (byte)candidate.ordinal();
         }
      }

      return fallback;
   }

   public record MinigameValues(byte behavior, float area, float speed) {
   }
}
