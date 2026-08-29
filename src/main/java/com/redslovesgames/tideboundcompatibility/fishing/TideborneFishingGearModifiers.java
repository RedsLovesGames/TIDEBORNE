package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import com.redslovesgames.tideboundcompatibility.registry.TideboundTags;
import java.util.Objects;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BiomeTags;

/** Resolves currently active Tideborne rods, hooks, lines, and bait into canonical fishing gear modifiers. */
public final class TideborneFishingGearModifiers {
    public static final String FISH_CATCH_CATEGORY = "fish";

    private TideborneFishingGearModifiers() {
    }

    public static FishingGearModifiers forMinigame(TideFishingHook hook, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (hook == null) {
            return FishingGearModifiers.neutral();
        }

        ItemStack line = hook.getLine();
        return selectMinigameLine(
                line != null && line.isOf(TideboundItems.TENTACLE_LINE),
                line != null && line.isOf(TideboundItems.SWIFT_LINE),
                SteelLeaderGearModifiers.forHook(hook, config),
                config
        );
    }

    /** Preserves the legacy precedence Tentacle, then Swift, then Steel Leader fallback. */
    public static FishingGearModifiers selectMinigameLine(
            boolean tentacleLine,
            boolean swiftLine,
            FishingGearModifiers fallback,
            TideboundConfig.Values config
    ) {
        Objects.requireNonNull(config, "config");
        FishingGearModifiers safeFallback = fallback == null ? FishingGearModifiers.neutral() : fallback;
        if (config.enableMythsCompat && tentacleLine) {
            return tentacleLine(config);
        }
        if (config.enableMythsCompat && swiftLine) {
            return swiftLine(config);
        }
        return safeFallback;
    }

    public static FishingGearModifiers forFishWeight(
            FishData data,
            FishingContext context,
            TideboundConfig.Values config
    ) {
        Objects.requireNonNull(config, "config");
        if (data == null || context == null || context.hook() == null) {
            return FishingGearModifiers.neutral();
        }

        TideFishingHook hook = context.hook();
        ItemStack fish = new ItemStack((ItemConvertible)data.fish().value());
        FishingGearModifiers sharkTooth = sharkToothHook(
                hook.getHook().isOf(TideboundItems.SHARK_TOOTH_HOOK),
                fish.isIn(TideboundTags.PREDATORY_FISH) || fish.isIn(TideboundTags.LARGE_FISH),
                fish.isIn(TideboundTags.VERY_SMALL_FISH),
                config
        );
        FishingGearModifiers seafarer = seafarersHook(
                hook.getHook().isOf(TideboundItems.SEAFARERS_HOOK),
                fish.isIn(Items.LEGENDARY_FISH)
                        && context.exactBiome().isIn(BiomeTags.IS_OCEAN)
                        && context.level().isNight(),
                config
        );
        return FishingGearModifiers.compose(sharkTooth, seafarer);
    }

    public static FishingGearModifiers forCrateWeight(FishingContext context, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (context == null) {
            return FishingGearModifiers.neutral();
        }
        return kujiraRod(
                context.rod() != null && context.rod().isOf(TideboundItems.KUJIRA_BONE_FISHING_ROD),
                context.exactBiome().isIn(BiomeTags.IS_OCEAN),
                config
        );
    }

    /** Leviathan Bait replaces the eligible catch categories with the normal Tide fish category only. */
    public static FishingGearModifiers leviathanBaitCatchPool(boolean active) {
        if (!active) {
            return FishingGearModifiers.neutral();
        }
        return FishingGearModifiers.builder()
                .restrictCategoriesTo(FISH_CATCH_CATEGORY)
                .build();
    }

    public static FishingGearModifiers tentacleLine(TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (!config.enableMythsCompat) {
            return FishingGearModifiers.neutral();
        }
        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, config.tentacleCatchZoneMultiplier)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, config.tentacleFishSpeedMultiplier)
                .build();
    }

    public static FishingGearModifiers swiftLine(TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (!config.enableMythsCompat) {
            return FishingGearModifiers.neutral();
        }
        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, config.swiftCatchZoneMultiplier)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, config.swiftFishSpeedMultiplier)
                .build();
    }

    public static FishingGearModifiers sharkToothHook(
            boolean equipped,
            boolean predatoryOrLarge,
            boolean verySmall,
            TideboundConfig.Values config
    ) {
        Objects.requireNonNull(config, "config");
        if (!config.enableApexCompat || !equipped) {
            return FishingGearModifiers.neutral();
        }
        double multiplier = 1.0D;
        if (predatoryOrLarge) {
            multiplier *= config.sharkToothPredatoryWeightMultiplier;
        }
        if (verySmall) {
            multiplier *= config.sharkToothSmallFishWeightMultiplier;
        }
        return multiplier == 1.0D
                ? FishingGearModifiers.neutral()
                : FishingGearModifiers.builder()
                        .namedMultiplierModifier(FishingGearEffects.FISH_WEIGHT_MULTIPLIER, multiplier)
                        .build();
    }

    public static FishingGearModifiers seafarersHook(
            boolean equipped,
            boolean legendaryOceanNight,
            TideboundConfig.Values config
    ) {
        Objects.requireNonNull(config, "config");
        if (!config.enableMythsCompat || !equipped || !legendaryOceanNight) {
            return FishingGearModifiers.neutral();
        }
        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.FISH_WEIGHT_MULTIPLIER, config.seafarersRareWeightMultiplier)
                .build();
    }

    public static FishingGearModifiers kujiraRod(
            boolean equipped,
            boolean ocean,
            TideboundConfig.Values config
    ) {
        Objects.requireNonNull(config, "config");
        if (!config.enableMythsCompat || !equipped || !ocean) {
            return FishingGearModifiers.neutral();
        }
        return FishingGearModifiers.builder()
                .namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER, config.kujiraOceanCrateMultiplier)
                .build();
    }
}
