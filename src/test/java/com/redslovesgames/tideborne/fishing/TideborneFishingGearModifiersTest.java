package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.gear.LeaderGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.LeaderTier;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import org.junit.jupiter.api.Test;

class TideborneFishingGearModifiersTest {
    @Test
    void tentacleLineUsesRebalancedFightFacingModifiersWithoutLuck() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.tentacleLine(config);

        assertEquals(1.32D, FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0e-12);
        assertEquals(1.16D, FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0e-12);
        assertIdentityFightAndLuckAxes(modifiers);
    }

    @Test
    void swiftLineUsesRebalancedFightFacingModifiersWithoutLuck() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.swiftLine(config);

        assertEquals(1.16D, FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0e-12);
        assertEquals(1.08D, FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0e-12);
        assertIdentityFightAndLuckAxes(modifiers);
    }

    @Test
    void minigameLinesComposeWithLeaderInsteadOfReplacingIt() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers ironLeader = LeaderGearModifiers.forTier(LeaderTier.IRON, true);

        FishingGearModifiers tentacle = TideborneFishingGearModifiers.selectMinigameLine(true, true, ironLeader, config);
        FishingGearModifiers swift = TideborneFishingGearModifiers.selectMinigameLine(false, true, ironLeader, config);
        FishingGearModifiers fallback = TideborneFishingGearModifiers.selectMinigameLine(false, false, ironLeader, config);

        assertEquals(1.32D * 0.95D, FishingGearEffects.catchZoneAreaMultiplier(tentacle), 1.0e-12);
        assertEquals(1.16D * 1.03D, FishingGearEffects.minigameSpeedMultiplier(tentacle), 1.0e-12);
        assertEquals(1.16D * 0.95D, FishingGearEffects.catchZoneAreaMultiplier(swift), 1.0e-12);
        assertEquals(1.08D * 1.03D, FishingGearEffects.minigameSpeedMultiplier(swift), 1.0e-12);
        assertEquals(0.95D, FishingGearEffects.catchZoneAreaMultiplier(fallback), 1.0e-12);
        assertEquals(1.03D, FishingGearEffects.minigameSpeedMultiplier(fallback), 1.0e-12);
        assertEquals(0.55D, FishingGearEffects.catchLossPreventionChance(tentacle), 1.0e-12);
        assertEquals(0.55D, FishingGearEffects.catchLossPreventionChance(swift), 1.0e-12);
    }

    @Test
    void sharkToothHookUsesRebalancedIndependentTagMultipliers() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        assertEquals(2.0D, weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config), "heavy"), 1.0e-12);
        assertEquals(0.45D, weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config), "very_small"), 1.0e-12);
        assertEquals(0.90D, weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config), "heavy", "very_small"), 1.0e-12);
        assertEquals(1.0D, weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config)), 1.0e-12);
    }

    @Test
    void seafarerAndKujiraUseRebalancedWeightModifiers() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        FishingGearModifiers seafarer = TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SEAFARERS_HOOK, true, config.enableApexCompat, config.enableMythsCompat, config);
        FishingGearModifiers kujira = TideborneFishingGearModifiers.rod(FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD, config.enableMythsCompat);

        assertEquals(1.35D, weight(seafarer, "legendary"), 1.0e-12);
        assertEquals(0.70D, FishingGearEffects.crateWeightMultiplier(kujira), 1.0e-12);
        assertIdentityFightAndLuckAxes(seafarer);
        assertEquals(0.88D, kujira.strengthMultiplier(), 1.0e-12);
    }

    @Test
    void inactiveOrIneligibleCustomGearIsNeutral() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.hookTargets(null, false, config.enableApexCompat, config.enableMythsCompat, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SEAFARERS_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.rod(FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD, false));

        config.enableMythsCompat = false;
        config.enableApexCompat = false;
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.tentacleLine(config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.swiftLine(config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, config.enableApexCompat, config.enableMythsCompat, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SEAFARERS_HOOK, true, config.enableApexCompat, config.enableMythsCompat, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.rod(FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD, config.enableMythsCompat));
    }

    @Test
    void customWeightAndFightEffectsComposeWithoutCollapsingLuckAxes() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers luck = FishingGearModifiers.builder()
                .fishingLuck(15.0D)
                .traitLuck(7.0D)
                .strengthMultiplier(1.10D)
                .tempoMultiplier(0.95D)
                .build();
        FishingGearModifiers combined = FishingGearModifiers.compose(
                luck,
                TideborneFishingGearModifiers.tentacleLine(config),
                TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SEAFARERS_HOOK, true, config.enableApexCompat, config.enableMythsCompat, config),
                TideborneFishingGearModifiers.rod(FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD, config.enableMythsCompat)
        );

        assertEquals(15.0D, combined.fishingLuck(), 1.0e-12);
        assertEquals(7.0D, combined.traitLuck(), 1.0e-12);
        assertEquals(1.10D * 0.88D, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(0.95D * 1.10D, combined.tempoMultiplier(), 1.0e-12);
        assertEquals(1.32D, FishingGearEffects.catchZoneAreaMultiplier(combined), 1.0e-12);
        assertEquals(1.16D, FishingGearEffects.minigameSpeedMultiplier(combined), 1.0e-12);
        assertEquals(1.35D, weight(combined, "legendary"), 1.0e-12);
        assertEquals(0.70D, FishingGearEffects.crateWeightMultiplier(combined), 1.0e-12);
    }

    private static void assertIdentityFightAndLuckAxes(FishingGearModifiers modifiers) {
        assertEquals(0.0D, modifiers.fishingLuck(), 1.0e-12);
        assertEquals(0.0D, modifiers.traitLuck(), 1.0e-12);
        assertEquals(1.0D, modifiers.strengthMultiplier(), 1.0e-12);
        assertEquals(1.0D, modifiers.tempoMultiplier(), 1.0e-12);
    }
    private static double weight(FishingGearModifiers gear, String... tags) {
        var species = new SpeciesProfile("tide:target_fixture", CanonicalRarity.FIVE_STAR, 1, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25, .4), java.util.Set.of(), java.util.Map.of(), java.util.Set.of(tags));
        return FishingGearEffects.targetWeightMultiplier(species, FishingEnvironment.empty(), gear);
    }
}
