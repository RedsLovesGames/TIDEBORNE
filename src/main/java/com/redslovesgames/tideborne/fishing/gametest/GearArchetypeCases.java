package com.redslovesgames.tideborne.fishing.gametest;

import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.gear.LeaderGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.LeaderTier;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import java.util.List;
import net.minecraft.util.Identifier;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;

/** Shared validation fixtures, never used by gameplay or preset creation. Native bait inputs are test expectations. */
public final class GearArchetypeCases {
    private GearArchetypeCases() {}
    public record Build(String name, FishingGearRegistry.GearProfile rod, FishingGearRegistry.GearProfile line,
                        FishingGearRegistry.GearProfile hook, String bobber, FishingGearRegistry.GearProfile bait,
                        LeaderTier leader, int nativeBaitLuck, int nativeBaitLure, double[] expected) {
        public Identifier bobberId() { return Identifier.of("tide", bobber + "_bobber"); }

        /** Compose the same canonical contributions independently of a live hook to check runtime wiring. */
        public FishingGearModifiers contributions(boolean myths, boolean apex, TideboundConfig.Values config) {
            return FishingGearModifiers.compose(
                    TideborneFishingGearModifiers.rod(rod, myths && config.enableMythsCompat),
                    TideFishingLineModifiers.forProfile(line),
                    TideborneFishingGearModifiers.selectMinigameLine(myths && line == TENTACLE_LINE,
                            myths && line == SWIFT_LINE, FishingGearModifiers.neutral(), config),
                    TideborneFishingGearModifiers.hookTargets(hook, true, apex, myths, config),
                    FishingGearRegistry.bobberModifiers(bobberId()).orElseThrow(),
                    bait.baitTargetModifiers(),
                    FishingGearModifiers.builder().fishingLuck(nativeBaitLuck)
                            .namedAdditiveModifier(FishingGearEffects.LURE_BONUS, nativeBaitLure).build(),
                    TideborneFishingGearModifiers.leviathanBait(myths && config.enableMythsCompat && bait == LEVIATHAN_BAIT),
                    LeaderGearModifiers.forTier(leader, config.enableApexCompat));
        }
    }

    // Expected: FL, TL, Strength, Tempo, zone area, minigame speed, prevention, relief, lure.
    public static List<Build> builds() {
        return List.of(
                new Build("Trophy", DIAMOND_ROD, TIDE_DIAMOND_LINE, TIDE_BASE_HOOK, "diamond", NORMAL_BAIT, LeaderTier.COPPER, 0, 2,
                        new double[]{0,0,.7544,1.06,1.0584,1.01,.50,.25,2}),
                new Build("Rare Species", GOLD_ROD, TIDE_COPPER_LINE, SEAFARERS_HOOK, "enchanted_golden_apple", LUCKY_BAIT, LeaderTier.COPPER, 2, 0,
                        new double[]{7,0,1,.94,.969612,1.01,.32,0,0}),
                new Build("Trait", IRON_ROD, TIDE_IRON_LINE, TWILIGHT_HOOK, "echo", NORMAL_BAIT, LeaderTier.COPPER, 0, 2,
                        new double[]{0,2,.92,1,1.00778496,1.01,.35,0,3}),
                new Build("Safe", NETHERITE_ROD, TIDE_COPPER_LINE, TIDE_BASE_HOOK, "netherite", NORMAL_BAIT, LeaderTier.GOLD, 0, 2,
                        new double[]{0,0,1,.893,1.0098,1.06,.95,.10,3}),
                new Build("Leviathan", KUJIRA_BONE_FISHING_ROD, TIDE_DIAMOND_LINE, SHARK_TOOTH_HOOK, "iron", LEVIATHAN_BAIT, LeaderTier.DIAMOND, 0, 0,
                        new double[]{4,1,.93808,1.30,.861,1.10,.95,0,0}),
                new Build("Fast", GOLD_ROD, SWIFT_LINE, TIDE_BASE_HOOK, "chorus", NORMAL_BAIT, LeaderTier.COPPER, 0, 2,
                        new double[]{0,0,1,1,1.03653424,1.0908,.32,0,5}),
                new Build("Heavy Fish", DIAMOND_ROD, TIDE_DIAMOND_LINE, SHARK_TOOTH_HOOK, "diamond", ABYSS_BAIT, LeaderTier.IRON, 0, 2,
                        new double[]{0,0,.7544,1.06,1.026,1.03,.75,.25,2}));
    }

    public static double[] values(FishingGearModifiers gear) {
        return new double[]{FishingGearEffects.fishingLuck(gear), FishingGearEffects.traitLuck(gear),
                FishingGearEffects.strengthMultiplier(gear), FishingGearEffects.tempoMultiplier(gear),
                FishingGearEffects.catchZoneAreaMultiplier(gear), FishingGearEffects.minigameSpeedMultiplier(gear),
                FishingGearEffects.catchLossPreventionChance(gear), FishingGearEffects.trophyFightRelief(gear),
                gear.namedAdditiveModifier(FishingGearEffects.LURE_BONUS)};
    }
}
