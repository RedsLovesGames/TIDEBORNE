package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import com.redslovesgames.tideboundcompatibility.fishing.TideborneFishingGearModifiers;
import org.junit.jupiter.api.Test;

class FishingGearRegistryStackingTest {
    @Test
    void canonicalProfilesPreserveCrossGearStackingSemantics() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        FishingGearModifiers combined = FishingGearModifiers.compose(
                TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE),
                TideborneFishingGearModifiers.tentacleLine(config),
                SteelLeaderGearModifiers.forAttachmentState(true, config),
                TideborneFishingGearModifiers.leviathanBait(true)
        );

        assertEquals(15.0D, combined.fishingLuck(), 1.0e-12);
        assertEquals(8.0D, combined.traitLuck(), 1.0e-12);
        assertEquals(0.75D * 1.15D, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(1.15D, combined.tempoMultiplier(), 1.0e-12);
        assertEquals(1.45D * config.steelLeaderCatchZoneMultiplier,
                FishingGearEffects.catchZoneAreaMultiplier(combined), 1.0e-12);
        assertEquals(1.18D * config.steelLeaderFishSpeedMultiplier,
                FishingGearEffects.minigameSpeedMultiplier(combined), 1.0e-12);
        assertEquals(config.steelLeaderCatchLossPreventionChance,
                FishingGearEffects.catchLossPreventionChance(combined), 1.0e-12);
        assertFishOnly(combined);
    }

    private static void assertFishOnly(FishingGearModifiers modifiers) {
        assertTrue(modifiers.categoryRestriction().allows(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY));
        assertFalse(modifiers.categoryRestriction().allows("crate"));
    }
}
