package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/** Canonical named fishing-gear effects shared by runtime integrations. */
public final class FishingGearEffects {
    public static final String CATCH_ZONE_AREA_MULTIPLIER = "catch_zone_area";
    public static final String MINIGAME_SPEED_MULTIPLIER = "minigame_speed";
    public static final String CATCH_LOSS_PREVENTION_CHANCE = "catch_loss_prevention_chance";
    public static final String CATCH_LOSS_PROTECTION_SOURCES = "catch_loss_protection_sources";

    private FishingGearEffects() {
    }

    public static double catchZoneAreaMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(CATCH_ZONE_AREA_MULTIPLIER);
    }

    public static double minigameSpeedMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).namedMultiplierModifier(MINIGAME_SPEED_MULTIPLIER);
    }

    public static double catchLossPreventionChance(FishingGearModifiers modifiers) {
        double chance = requireModifiers(modifiers).namedAdditiveModifier(CATCH_LOSS_PREVENTION_CHANCE);
        return Math.max(0.0, Math.min(1.0, chance));
    }

    /**
     * Resolves a server-owned catch-loss protection roll from canonical gear state.
     * A protection source consumes exactly one supplied roll, matching legacy Steel Leader behavior.
     */
    public static boolean preventsCatchLoss(FishingGearModifiers modifiers, DoubleSupplier unitRoll) {
        FishingGearModifiers canonical = requireModifiers(modifiers);
        Objects.requireNonNull(unitRoll, "unitRoll");
        if (canonical.namedAdditiveModifier(CATCH_LOSS_PROTECTION_SOURCES) <= 0.0) {
            return false;
        }
        return unitRoll.getAsDouble() < catchLossPreventionChance(canonical);
    }

    private static FishingGearModifiers requireModifiers(FishingGearModifiers modifiers) {
        return Objects.requireNonNull(modifiers, "modifiers");
    }
}
