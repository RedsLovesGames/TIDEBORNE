package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import com.redslovesgames.tideboundcompatibility.fishing.TideborneFishingGearModifiers;
import org.junit.jupiter.api.Test;

class TideborneFishingGearModifiersTest {
    @Test
    void tentacleLinePreservesCurrentFightFacingModifiersWithoutLuck() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.tentacleLine(config);

        assertEquals(1.45D, FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0e-12);
        assertEquals(1.18D, FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0e-12);
        assertIdentityFightAndLuckAxes(modifiers);
    }

    @Test
    void swiftLinePreservesCurrentFightFacingModifiersWithoutLuck() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.swiftLine(config);

        assertEquals(1.20D, FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0e-12);
        assertEquals(1.05D, FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0e-12);
        assertIdentityFightAndLuckAxes(modifiers);
    }

    @Test
    void minigameLinePrecedenceMatchesLegacyBranching() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers steel = SteelLeaderGearModifiers.forAttachmentState(true, config);

        FishingGearModifiers tentacle = TideborneFishingGearModifiers.selectMinigameLine(true, true, steel, config);
        FishingGearModifiers swift = TideborneFishingGearModifiers.selectMinigameLine(false, true, steel, config);
        FishingGearModifiers fallback = TideborneFishingGearModifiers.selectMinigameLine(false, false, steel, config);

        assertEquals(1.45D, FishingGearEffects.catchZoneAreaMultiplier(tentacle), 1.0e-12);
        assertEquals(1.18D, FishingGearEffects.minigameSpeedMultiplier(tentacle), 1.0e-12);
        assertEquals(1.20D, FishingGearEffects.catchZoneAreaMultiplier(swift), 1.0e-12);
        assertEquals(1.05D, FishingGearEffects.minigameSpeedMultiplier(swift), 1.0e-12);
        assertEquals(0.90D, FishingGearEffects.catchZoneAreaMultiplier(fallback), 1.0e-12);
        assertEquals(1.05D, FishingGearEffects.minigameSpeedMultiplier(fallback), 1.0e-12);
    }

    @Test
    void sharkToothHookPreservesIndependentTagMultipliers() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        assertEquals(2.50D, FishingGearEffects.fishWeightMultiplier(
                TideborneFishingGearModifiers.sharkToothHook(true, true, false, config)), 1.0e-12);
        assertEquals(0.35D, FishingGearEffects.fishWeightMultiplier(
                TideborneFishingGearModifiers.sharkToothHook(true, false, true, config)), 1.0e-12);
        assertEquals(0.875D, FishingGearEffects.fishWeightMultiplier(
                TideborneFishingGearModifiers.sharkToothHook(true, true, true, config)), 1.0e-12);
        assertEquals(1.0D, FishingGearEffects.fishWeightMultiplier(
                TideborneFishingGearModifiers.sharkToothHook(true, false, false, config)), 1.0e-12);
    }

    @Test
    void seafarerAndKujiraPreserveCurrentWeightModifiers() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        FishingGearModifiers seafarer = TideborneFishingGearModifiers.seafarersHook(true, true, config);
        FishingGearModifiers kujira = TideborneFishingGearModifiers.kujiraRod(true, true, config);

        assertEquals(1.35D, FishingGearEffects.fishWeightMultiplier(seafarer), 1.0e-12);
        assertEquals(1.20D, FishingGearEffects.crateWeightMultiplier(kujira), 1.0e-12);
        assertIdentityFightAndLuckAxes(seafarer);
        assertIdentityFightAndLuckAxes(kujira);
    }

    @Test
    void inactiveOrIneligibleCustomGearIsNeutral() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.sharkToothHook(false, true, true, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.seafarersHook(true, false, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.kujiraRod(true, false, config));

        config.enableMythsCompat = false;
        config.enableApexCompat = false;
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.tentacleLine(config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.swiftLine(config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.sharkToothHook(true, true, true, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.seafarersHook(true, true, config));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.kujiraRod(true, true, config));
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
                TideborneFishingGearModifiers.seafarersHook(true, true, config),
                TideborneFishingGearModifiers.kujiraRod(true, true, config)
        );

        assertEquals(15.0D, combined.fishingLuck(), 1.0e-12);
        assertEquals(7.0D, combined.traitLuck(), 1.0e-12);
        assertEquals(1.10D, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(0.95D, combined.tempoMultiplier(), 1.0e-12);
        assertEquals(1.45D, FishingGearEffects.catchZoneAreaMultiplier(combined), 1.0e-12);
        assertEquals(1.18D, FishingGearEffects.minigameSpeedMultiplier(combined), 1.0e-12);
        assertEquals(1.35D, FishingGearEffects.fishWeightMultiplier(combined), 1.0e-12);
        assertEquals(1.20D, FishingGearEffects.crateWeightMultiplier(combined), 1.0e-12);
    }

    private static void assertIdentityFightAndLuckAxes(FishingGearModifiers modifiers) {
        assertEquals(0.0D, modifiers.fishingLuck(), 1.0e-12);
        assertEquals(0.0D, modifiers.traitLuck(), 1.0e-12);
        assertEquals(1.0D, modifiers.strengthMultiplier(), 1.0e-12);
        assertEquals(1.0D, modifiers.tempoMultiplier(), 1.0e-12);
    }
}
