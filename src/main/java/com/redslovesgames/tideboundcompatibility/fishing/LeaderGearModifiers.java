package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.Objects;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Canonical material-leader fight and catch-loss effects. */
public final class LeaderGearModifiers {
    private static final Map<LeaderTier, FishingGearModifiers> TIER_MODIFIERS = Arrays.stream(LeaderTier.values())
            .collect(Collectors.toUnmodifiableMap(tier -> tier, LeaderGearModifiers::tierModifiers));

    private LeaderGearModifiers() {}

    public static FishingGearModifiers forHook(TideFishingHook hook, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        return forTier(hook == null ? null : LeaderAttachment.tierOnHook(hook), config.enableApexCompat);
    }

    public static FishingGearModifiers forTier(LeaderTier tier, boolean enabled) {
        return enabled && tier != null ? TIER_MODIFIERS.get(tier) : FishingGearModifiers.neutral();
    }

    private static FishingGearModifiers tierModifiers(LeaderTier tier) {
        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, tier.catchZoneMultiplier())
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, tier.fishSpeedMultiplier())
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, tier.protection())
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES, 1.0D)
                .build();
    }
}
