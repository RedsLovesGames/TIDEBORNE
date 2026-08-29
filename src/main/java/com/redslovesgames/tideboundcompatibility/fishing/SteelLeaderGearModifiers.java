package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.Objects;

/** Adapts the persisted Steel Leader attachment into canonical Fishing System 2.0 gear modifiers. */
public final class SteelLeaderGearModifiers {
    private SteelLeaderGearModifiers() {
    }

    public static FishingGearModifiers forHook(TideFishingHook hook, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        return forAttachmentState(hook != null && SteelLeaderAttachment.hasOnHook(hook), config);
    }

    public static FishingGearModifiers forAttachmentState(boolean attached, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (!attached || !config.enableApexCompat) {
            return FishingGearModifiers.neutral();
        }

        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, config.steelLeaderCatchZoneMultiplier)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, config.steelLeaderFishSpeedMultiplier)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, config.steelLeaderCatchLossPreventionChance)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES, 1.0)
                .build();
    }
}
