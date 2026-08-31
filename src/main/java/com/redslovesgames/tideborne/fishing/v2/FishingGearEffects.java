package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;
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

    private FishingGearEffects() {
    }

    public static double catchZoneAreaMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(CATCH_ZONE_AREA_MULTIPLIER);
    }

    public static double minigameSpeedMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(MINIGAME_SPEED_MULTIPLIER);
    }

    public static double fishWeightMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(FISH_WEIGHT_MULTIPLIER);
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
}
