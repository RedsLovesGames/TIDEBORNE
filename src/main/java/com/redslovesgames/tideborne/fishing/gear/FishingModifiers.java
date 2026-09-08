/* RECONSTRUCTED SOURCE BASELINE */
package com.redslovesgames.tideborne.fishing.gear;

import com.li64.tide.Tide;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.data.fishing.MinigameBehavior;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.FightProfileService;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalCatchStateManager;
import com.redslovesgames.tideborne.config.TideboundConfig;

/** Compatibility adapters around canonical Fishing System 2.0 modifier/fight services. */
public final class FishingModifiers {
   private static final FightProfileService CANONICAL_FIGHTS = new FightProfileService();
   private FishingModifiers() {}

   public static double modifyCrateWeight(FishingContext context, double original) {
      FishingGearModifiers gear = TideborneFishingGearModifiers.forCrateWeight(context, TideboundConfig.get());
      return original * FishingGearEffects.crateWeightMultiplier(gear);
   }

   public static FishingModifiers.MinigameValues modifyMinigame(TideFishingHook hook, byte behavior, float area, float speed) {
      FishingGearModifiers compatibilityGear = TideborneFishingGearModifiers.forMinigame(hook, TideboundConfig.get());
      FightProfileService.MinigameProjection projection;
      var canonical = CanonicalCatchStateManager.get(hook);
      if (canonical.isPresent()) {
         var fightProfile = canonical.get().fightProfile();
         FishingGearModifiers allMinigameGear = TideborneFishingGearModifiers.forCanonicalFight(hook, TideboundConfig.get());
         projection = CANONICAL_FIGHTS.projectMinigame(canonical.get().species(), canonical.get().specimen(),
                 allMinigameGear, Tide.SERVER_CONFIG.minigame.minigameDifficultyMultiplier);
         behavior = canonicalBehavior(fightProfile.behavior(), behavior);
      } else {
         projection = CANONICAL_FIGHTS.projectCompatibilityMinigame(area, speed, compatibilityGear);
      }
      return new FishingModifiers.MinigameValues(behavior, (float)projection.catchZoneArea(), (float)projection.speed());
   }

   private static byte canonicalBehavior(String behavior, byte fallback) {
      if (behavior == null || behavior.isBlank()) return fallback;
      for (MinigameBehavior candidate : MinigameBehavior.values()) {
         if (candidate.name().equalsIgnoreCase(behavior) || candidate.toString().equalsIgnoreCase(behavior)) return (byte)candidate.ordinal();
      }
      return fallback;
   }
   public record MinigameValues(byte behavior, float area, float speed) {}
}
