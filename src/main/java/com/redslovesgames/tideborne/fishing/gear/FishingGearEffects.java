package com.redslovesgames.tideborne.fishing.gear;

import com.redslovesgames.tideborne.fishing.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;

import java.util.Objects;
import java.math.BigDecimal;
import java.util.function.DoubleSupplier;

/** Canonical named fishing-gear effects shared by runtime integrations. */
public final class FishingGearEffects {
    public static final String CATCH_ZONE_AREA_MULTIPLIER = "catch_zone_area";
    public static final String MINIGAME_SPEED_MULTIPLIER = "minigame_speed";
    public static final String FISH_WEIGHT_MULTIPLIER = "fish_weight";
    public static final String CRATE_WEIGHT_MULTIPLIER = "crate_weight";
    public static final String CATCH_LOSS_PREVENTION_CHANCE = "catch_loss_prevention_chance";
    public static final String CATCH_LOSS_PROTECTION_SOURCES = "catch_loss_protection_sources";
    public static final double MAX_CATCH_LOSS_PREVENTION_CHANCE = 0.95D;
    public static final String TROPHY_FIGHT_RELIEF = "trophy_fight_relief";
    public static final String LURE_BONUS = "lure_bonus";
    public static final String TARGET_WEIGHT_PREFIX = "target_weight.";
    public static final String ENVIRONMENT_WEIGHT_PREFIX = "environment_weight.";
    public static final double MAX_FISHING_LUCK = 8.0, MAX_TRAIT_LUCK = 3.0;
    public static final double MIN_STRENGTH = 0.70, MAX_STRENGTH = 1.35;
    public static final double MIN_TEMPO = 0.80, MAX_TEMPO = 1.30;
    public static final double MIN_ZONE = 0.70, MAX_ZONE = 1.40;
    public static final double MIN_SPEED = 0.80, MAX_SPEED = 1.25;
    public static final double MAX_SPECIES_WEIGHT = 3.0;

    private FishingGearEffects() {
    }

    // Clamp only at consumption of a complete composition. Never clamp individual profiles or
    // return a clamped modifier record for subsequent composition: that would discard tradeoffs.
    public static double fishingLuck(FishingGearModifiers modifiers) {
        return Math.min(MAX_FISHING_LUCK, requireModifiers(modifiers).fishingLuck());
    }

    public static double traitLuck(FishingGearModifiers modifiers) {
        return Math.min(MAX_TRAIT_LUCK, requireModifiers(modifiers).traitLuck());
    }

    public static double strengthMultiplier(FishingGearModifiers modifiers) {
        return clamp(requireModifiers(modifiers).strengthMultiplier(), MIN_STRENGTH, MAX_STRENGTH);
    }

    public static double tempoMultiplier(FishingGearModifiers modifiers) {
        return clamp(requireModifiers(modifiers).tempoMultiplier(), MIN_TEMPO, MAX_TEMPO);
    }

    public static double trophyFightRelief(FishingGearModifiers modifiers) {
        return clamp(requireModifiers(modifiers).namedAdditiveModifier(TROPHY_FIGHT_RELIEF), 0.0, 1.0);
    }

    /** Raw candidate contribution, kept uncapped until target effects and gear luck meet. */
    public static double targetWeightMultiplier(SpeciesProfile species, FishingEnvironment environment,
                                                FishingGearModifiers modifiers) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(environment, "environment");
        BigDecimal weight = BigDecimal.valueOf(requireModifiers(modifiers).namedMultiplierModifier(FISH_WEIGHT_MULTIPLIER));
        for (var entry : modifiers.namedMultiplierModifiers().entrySet()) {
            String key = entry.getKey();
            if ((key.startsWith(TARGET_WEIGHT_PREFIX) && species.targetTags().contains(key.substring(TARGET_WEIGHT_PREFIX.length())))
                    || (key.startsWith(ENVIRONMENT_WEIGHT_PREFIX) && environment.habitatTags().contains(key.substring(ENVIRONMENT_WEIGHT_PREFIX.length())))) {
                weight = weight.multiply(BigDecimal.valueOf(entry.getValue()));
            }
        }
        return weight.doubleValue();
    }

    /** Caps the gear-only ratio; native enchantment/environment luck keeps its own contribution. */
    public static double speciesWeightMultiplier(SpeciesProfile species, FishingEnvironment environment,
                                                 FishingGearModifiers modifiers, double nativeLuck) {
        double nativeMultiplier = species.rarity().fishingLuckMultiplier(nativeLuck);
        double combinedMultiplier = species.rarity().fishingLuckMultiplier(nativeLuck + fishingLuck(modifiers));
        return Math.min(MAX_SPECIES_WEIGHT,
                targetWeightMultiplier(species, environment, modifiers) * combinedMultiplier / nativeMultiplier);
    }

    public static double catchZoneAreaMultiplier(FishingGearModifiers modifiers) {
        return clamp(requireModifiers(modifiers).namedMultiplierModifier(CATCH_ZONE_AREA_MULTIPLIER), MIN_ZONE, MAX_ZONE);
    }

    public static double minigameSpeedMultiplier(FishingGearModifiers modifiers) {
        return clamp(requireModifiers(modifiers).namedMultiplierModifier(MINIGAME_SPEED_MULTIPLIER), MIN_SPEED, MAX_SPEED);
    }

    public static double fishWeightMultiplier(FishingGearModifiers modifiers) {
        return Math.min(MAX_SPECIES_WEIGHT, requireModifiers(modifiers).namedMultiplierModifier(FISH_WEIGHT_MULTIPLIER));
    }

    public static double crateWeightMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(CRATE_WEIGHT_MULTIPLIER);
    }

    public static double catchLossPreventionChance(FishingGearModifiers modifiers) {
        double chance = requireModifiers(modifiers).namedAdditiveModifier(CATCH_LOSS_PREVENTION_CHANCE);
        return Math.max(0.0D, Math.min(MAX_CATCH_LOSS_PREVENTION_CHANCE, chance));
    }

    /** Resolves one server-owned catch-loss protection roll. Protection can never reach 100%. */
    public static boolean preventsCatchLoss(FishingGearModifiers modifiers, DoubleSupplier unitRoll) {
        FishingGearModifiers canonical = requireModifiers(modifiers);
        Objects.requireNonNull(unitRoll, "unitRoll");
        if (canonical.namedAdditiveModifier(CATCH_LOSS_PROTECTION_SOURCES) <= 0.0D) {
            return false;
        }
        return unitRoll.getAsDouble() < catchLossPreventionChance(canonical);
    }

    private static FishingGearModifiers requireModifiers(FishingGearModifiers modifiers) {
        return Objects.requireNonNull(modifiers, "modifiers");
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
