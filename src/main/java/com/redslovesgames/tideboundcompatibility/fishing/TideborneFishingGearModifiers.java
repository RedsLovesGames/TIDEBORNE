package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.registry.TideboundTags;
import java.util.Objects;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BiomeTags;

/** Resolves Tideborne rods, hooks, lines, leaders, bobbers, and bait into canonical fishing modifiers. */
public final class TideborneFishingGearModifiers {
    public static final String FISH_CATCH_CATEGORY = "fish";
    public static final double LEVIATHAN_FISHING_LUCK_BONUS = 15.0D;
    public static final double LEVIATHAN_TRAIT_LUCK_BONUS = 4.0D;
    public static final double LEVIATHAN_STRENGTH_MULTIPLIER = 1.25D;
    public static final double LEVIATHAN_TEMPO_MULTIPLIER = 1.20D;

    private TideborneFishingGearModifiers() {}

    public static FishingGearModifiers forMinigame(TideFishingHook hook, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (hook == null) return FishingGearModifiers.neutral();
        FishingGearRegistry.GearProfile lineProfile = FishingGearRegistry.resolve(hook.getLine()).orElse(null);
        FishingGearModifiers specialLine = FishingGearModifiers.neutral();
        if (config.enableMythsCompat && lineProfile == FishingGearRegistry.GearProfile.TENTACLE_LINE) specialLine = tentacleLine(config);
        else if (config.enableMythsCompat && lineProfile == FishingGearRegistry.GearProfile.SWIFT_LINE) specialLine = swiftLine(config);
        return FishingGearModifiers.compose(specialLine, LeaderGearModifiers.forHook(hook, config), BobberGearModifiers.forHook(hook));
    }

    public static FishingGearModifiers selectMinigameLine(boolean tentacleLine, boolean swiftLine, FishingGearModifiers fallback, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        FishingGearModifiers line = FishingGearModifiers.neutral();
        if (config.enableMythsCompat && tentacleLine) line = tentacleLine(config);
        else if (config.enableMythsCompat && swiftLine) line = swiftLine(config);
        return FishingGearModifiers.compose(line, fallback == null ? FishingGearModifiers.neutral() : fallback);
    }

    public static FishingGearModifiers forFishWeight(FishData data, FishingContext context, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (data == null || context == null || context.hook() == null) return FishingGearModifiers.neutral();
        TideFishingHook hook = context.hook();
        FishingGearRegistry.GearProfile hookProfile = FishingGearRegistry.resolve(hook.getHook()).orElse(null);
        ItemStack fish = new ItemStack((ItemConvertible)data.fish().value());
        FishingGearModifiers sharkTooth = sharkToothHook(hookProfile == FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK,
                fish.isIn(TideboundTags.PREDATORY_FISH) || fish.isIn(TideboundTags.LARGE_FISH), fish.isIn(TideboundTags.VERY_SMALL_FISH), config);
        FishingGearModifiers seafarer = seafarersHook(hookProfile == FishingGearRegistry.GearProfile.SEAFARERS_HOOK,
                fish.isIn(Items.LEGENDARY_FISH) && context.exactBiome().isIn(BiomeTags.IS_OCEAN) && context.level().isNight(), config);
        return FishingGearModifiers.compose(sharkTooth, seafarer);
    }

    public static FishingGearModifiers forCrateWeight(FishingContext context, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (context == null) return FishingGearModifiers.neutral();
        FishingGearRegistry.GearProfile rodProfile = FishingGearRegistry.resolve(context.rod()).orElse(null);
        FishingGearModifiers rod = kujiraRod(rodProfile == FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD,
                context.exactBiome().isIn(BiomeTags.IS_OCEAN), config);
        return FishingGearModifiers.compose(rod, BobberGearModifiers.forRod(context.rod()));
    }

    public static FishingGearModifiers leviathanBait(boolean active) {
        if (!active) return FishingGearModifiers.neutral();
        return FishingGearModifiers.builder().fishingLuck(LEVIATHAN_FISHING_LUCK_BONUS).traitLuck(LEVIATHAN_TRAIT_LUCK_BONUS)
                .strengthMultiplier(LEVIATHAN_STRENGTH_MULTIPLIER).tempoMultiplier(LEVIATHAN_TEMPO_MULTIPLIER)
                .restrictCategoriesTo(FISH_CATCH_CATEGORY).build();
    }
    public static FishingGearModifiers leviathanBaitCatchPool(boolean active) { return leviathanBait(active); }

    public static FishingGearModifiers tentacleLine(TideboundConfig.Values config) {
        if (!config.enableMythsCompat) return FishingGearModifiers.neutral();
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, config.tentacleCatchZoneMultiplier)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, config.tentacleFishSpeedMultiplier).build();
    }
    public static FishingGearModifiers swiftLine(TideboundConfig.Values config) {
        if (!config.enableMythsCompat) return FishingGearModifiers.neutral();
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, config.swiftCatchZoneMultiplier)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, config.swiftFishSpeedMultiplier).build();
    }
    public static FishingGearModifiers sharkToothHook(boolean equipped, boolean predatoryOrLarge, boolean verySmall, TideboundConfig.Values config) {
        if (!config.enableApexCompat || !equipped) return FishingGearModifiers.neutral();
        double multiplier=1.0D; if(predatoryOrLarge) multiplier*=config.sharkToothPredatoryWeightMultiplier; if(verySmall) multiplier*=config.sharkToothSmallFishWeightMultiplier;
        return multiplier==1.0D?FishingGearModifiers.neutral():FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.FISH_WEIGHT_MULTIPLIER,multiplier).build();
    }
    public static FishingGearModifiers seafarersHook(boolean equipped, boolean legendaryOceanNight, TideboundConfig.Values config) {
        if (!config.enableMythsCompat || !equipped || !legendaryOceanNight) return FishingGearModifiers.neutral();
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.FISH_WEIGHT_MULTIPLIER,config.seafarersRareWeightMultiplier).build();
    }
    public static FishingGearModifiers kujiraRod(boolean equipped, boolean ocean, TideboundConfig.Values config) {
        if (!config.enableMythsCompat || !equipped || !ocean) return FishingGearModifiers.neutral();
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER,config.kujiraOceanCrateMultiplier).build();
    }
}
