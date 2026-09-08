package com.redslovesgames.tideborne.fishing.gear;

import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.util.BaitUtils;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import com.redslovesgames.tideborne.compat.TideboundCompatibility;
import com.redslovesgames.tideborne.config.TideboundConfig;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BiomeTags;

/** Resolves Tideborne rods, hooks, lines, leaders, bobbers, and bait into canonical fishing modifiers. */
public final class TideborneFishingGearModifiers {
    public static final String FISH_CATCH_CATEGORY = "fish";
    public static final double LEVIATHAN_FISHING_LUCK_BONUS = 4.0D;
    public static final double LEVIATHAN_TRAIT_LUCK_BONUS = 1.0D;
    public static final double LEVIATHAN_STRENGTH_MULTIPLIER = 1.30D;
    public static final double LEVIATHAN_TEMPO_MULTIPLIER = 1.20D;

    private static final FishingGearModifiers LEVIATHAN_BAIT = FishingGearModifiers.builder()
            .fishingLuck(LEVIATHAN_FISHING_LUCK_BONUS).traitLuck(LEVIATHAN_TRAIT_LUCK_BONUS)
            .strengthMultiplier(LEVIATHAN_STRENGTH_MULTIPLIER).tempoMultiplier(LEVIATHAN_TEMPO_MULTIPLIER)
            .targetWeight("boss", 2.00D).restrictCategoriesTo(FISH_CATCH_CATEGORY).build();

    private TideborneFishingGearModifiers() {}

    /** Read-only, context-free Satchel preview. Eligibility and conditional targets still resolve at cast. */
    public static FishingGearModifiers forLoadoutPreview(ItemStack rod, TideboundConfig.Values config) {
        ItemStack line = com.li64.tide.data.rods.CustomRodManager.getLine(rod);
        var profile = FishingGearRegistry.resolve(line).orElse(null);
        boolean myths = config.enableMythsCompat && TideboundCompatibility.isMythsIntegrationActive();
        var specialLine = selectMinigameLine(myths && profile == FishingGearRegistry.GearProfile.TENTACLE_LINE,
                myths && profile == FishingGearRegistry.GearProfile.SWIFT_LINE, FishingGearModifiers.neutral(), config);
        var tier = LeaderAttachment.tier(rod);
        if (tier == null) tier = LeaderAttachment.tierOfStack(line);
        var nativeBait = FishingGearModifiers.builder().fishingLuck(BaitUtils.getCombinedLuck(rod))
                .namedAdditiveModifier(FishingGearEffects.LURE_BONUS,
                        BaitUtils.getCombinedSpeed(rod)).build();
        var targets = hookTargets(FishingGearRegistry.resolve(com.li64.tide.data.rods.CustomRodManager.getHook(rod)).orElse(null),
                true, config.enableApexCompat && TideboundCompatibility.isApexIntegrationActive(), myths, config);
        var baitTargets = baitTargets(rod);
        return FishingGearModifiers.compose(forRod(rod, config), TideFishingLineModifiers.forLine(line), specialLine, targets, baitTargets,
                LeaderGearModifiers.forTier(tier, config.enableApexCompat),
                com.li64.tide.data.rods.CustomRodManager.hasBobber(rod) ? BobberGearModifiers.forRod(rod) : FishingGearModifiers.neutral(), nativeBait,
                leviathanBait(myths && config.leviathanBaitFishOnly
                        && BaitUtils.hasBait(com.redslovesgames.tideborne.registry.TideboundItems.LEVIATHAN_BAIT, rod)));
    }

    /** Complete canonical fight contribution. Native line physics must not also be projected by Tide. */
    public static FishingGearModifiers forCanonicalFight(TideFishingHook hook, TideboundConfig.Values config) {
        if (hook == null) return FishingGearModifiers.neutral();
        return FishingGearModifiers.compose(
                TideFishingLineModifiers.forLine(hook.getLine()),
                forMinigame(hook, config),
                forRod(hook.getRod(), config),
                leviathanBait(LeviathanBaitFishing.isEnabledFor(hook, config)));
    }

    public static FishingGearModifiers forRod(ItemStack rod, TideboundConfig.Values config) {
        return rod(FishingGearRegistry.resolve(rod).orElse(null),
                config.enableMythsCompat && TideboundCompatibility.isMythsIntegrationActive());
    }

    public static FishingGearModifiers rod(FishingGearRegistry.GearProfile profile, boolean mythsActive) {
        if (profile == null || (profile == FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD && !mythsActive))
            return FishingGearModifiers.neutral();
        return profile.rodModifiers();
    }

    /** One raw encounter composition, consumed only after native candidate eligibility succeeds. */
    public static FishingGearModifiers forSelection(FishingContext context, TideboundConfig.Values config) {
        if (context == null || context.hook() == null) return FishingGearModifiers.neutral();
        TideFishingHook hook = context.hook();
        var hookProfile = FishingGearRegistry.resolve(hook.getHook()).orElse(null);
        var target = hookTargets(hookProfile, context.exactBiome().isIn(BiomeTags.IS_OCEAN) && context.level().isNight(),
                config.enableApexCompat && TideboundCompatibility.isApexIntegrationActive(),
                config.enableMythsCompat && TideboundCompatibility.isMythsIntegrationActive(), config);
        var bait = baitTargets(context.rod());
        return FishingGearModifiers.compose(forRod(context.rod(), config), target, bait,
                FishingGearModifiers.builder().fishingLuck(BaitUtils.getCombinedLuck(context.rod())).build(),
                BobberGearModifiers.forHook(hook), leviathanBait(LeviathanBaitFishing.isEnabledFor(hook, config)));
    }

    private static FishingGearModifiers baitTargets(ItemStack rod) {
        return FishingGearModifiers.compose(BaitUtils.getBaitItems(rod).stream()
                .map(stack -> FishingGearRegistry.resolve(stack).map(FishingGearRegistry.GearProfile::baitTargetModifiers)
                        .orElseGet(FishingGearModifiers::neutral)).toList());
    }

    public static FishingGearModifiers hookTargets(FishingGearRegistry.GearProfile hookProfile, boolean oceanNight,
                                                   boolean apexActive, boolean mythsActive, TideboundConfig.Values config) {
        var target = FishingGearModifiers.builder();
        if (hookProfile == FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK
                && apexActive) {
            target.targetWeight("heavy", config.sharkToothPredatoryWeightMultiplier)
                    .targetWeight("very_small", config.sharkToothSmallFishWeightMultiplier);
        }
        if (hookProfile == FishingGearRegistry.GearProfile.SEAFARERS_HOOK
                && mythsActive && oceanNight) {
            target.targetWeight("legendary", config.seafarersRareWeightMultiplier);
        }
        return target.build();
    }

    public static FishingGearModifiers forMinigame(TideFishingHook hook, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (hook == null) return FishingGearModifiers.neutral();
        FishingGearRegistry.GearProfile lineProfile = FishingGearRegistry.resolve(hook.getLine()).orElse(null);
        FishingGearModifiers specialLine = FishingGearModifiers.neutral();
        if (TideboundCompatibility.isMythsIntegrationActive() && lineProfile == FishingGearRegistry.GearProfile.TENTACLE_LINE) specialLine = tentacleLine(config);
        else if (TideboundCompatibility.isMythsIntegrationActive() && lineProfile == FishingGearRegistry.GearProfile.SWIFT_LINE) specialLine = swiftLine(config);
        return FishingGearModifiers.compose(specialLine, LeaderGearModifiers.forHook(hook, config), BobberGearModifiers.forHook(hook));
    }

    public static FishingGearModifiers selectMinigameLine(boolean tentacleLine, boolean swiftLine, FishingGearModifiers fallback, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        FishingGearModifiers line = FishingGearModifiers.neutral();
        if (config.enableMythsCompat && tentacleLine) line = tentacleLine(config);
        else if (config.enableMythsCompat && swiftLine) line = swiftLine(config);
        return FishingGearModifiers.compose(line, fallback == null ? FishingGearModifiers.neutral() : fallback);
    }

    public static FishingGearModifiers forCrateWeight(FishingContext context, TideboundConfig.Values config) {
        Objects.requireNonNull(config, "config");
        if (context == null) return FishingGearModifiers.neutral();
        FishingGearModifiers rod = forRod(context.rod(), config);
        return FishingGearModifiers.compose(rod, context.hook() == null ? BobberGearModifiers.forRod(context.rod()) : BobberGearModifiers.forHook(context.hook()));
    }

    public static FishingGearModifiers leviathanBait(boolean active) {
        return active ? LEVIATHAN_BAIT : FishingGearModifiers.neutral();
    }

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
}
