package com.redslovesgames.tideborne.fishing.tide;

import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;

import net.minecraft.item.ItemStack;

/** Canonical Fishing System 2.0 representation of Tide 2.1.1 fishing-line fight modifiers. */
public final class TideFishingLineModifiers {
    public enum LegacyLine { COPPER, IRON, GOLDEN, DIAMOND }

    private static final FishingGearModifiers COPPER = FishingGearModifiers.builder()
            .tempoMultiplier(0.94D)
            .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 1.02D)
            .build();
    private static final FishingGearModifiers IRON = FishingGearModifiers.builder()
            .strengthMultiplier(0.92D)
            .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 1.03D)
            .build();
    private static final FishingGearModifiers GOLDEN = FishingGearModifiers.builder()
            .strengthMultiplier(1.05D)
            .tempoMultiplier(0.86D)
            .build();
    private static final FishingGearModifiers DIAMOND = FishingGearModifiers.builder()
            .strengthMultiplier(0.82D)
            .tempoMultiplier(1.06D)
            .build();

    private TideFishingLineModifiers() {}

    public static FishingGearModifiers forLegacyLine(LegacyLine line) {
        if (line == null) return FishingGearModifiers.neutral();
        return switch (line) {
            case COPPER -> COPPER;
            case IRON -> IRON;
            case GOLDEN -> GOLDEN;
            case DIAMOND -> DIAMOND;
        };
    }

    public static FishingGearModifiers forLine(ItemStack line) {
        if (line == null || line.isEmpty()) return FishingGearModifiers.neutral();
        return FishingGearRegistry.resolve(line).map(TideFishingLineModifiers::forProfile)
                .orElseGet(FishingGearModifiers::neutral);
    }

    public static FishingGearModifiers forProfile(FishingGearRegistry.GearProfile profile) {
        if (profile == null) return FishingGearModifiers.neutral();
        return switch (profile) {
            case TIDE_COPPER_LINE -> COPPER;
            case TIDE_IRON_LINE -> IRON;
            case TIDE_GOLDEN_LINE -> GOLDEN;
            case TIDE_DIAMOND_LINE -> DIAMOND;
            default -> FishingGearModifiers.neutral();
        };
    }
}
