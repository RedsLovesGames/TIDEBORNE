package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.LeaderGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.LeaderTier;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import org.junit.jupiter.api.Test;

class FishingGearRegistryStackingTest {
    @Test
    void canonicalProfilesPreserveCrossGearStackingSemantics() {
        TideboundConfig.Values config = new TideboundConfig.Values();

        FishingGearModifiers combined = FishingGearModifiers.compose(
                TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE),
                TideborneFishingGearModifiers.tentacleLine(config),
                LeaderGearModifiers.forTier(LeaderTier.IRON, true),
                TideborneFishingGearModifiers.leviathanBait(true)
        );

        assertEquals(15.0D, combined.fishingLuck(), 1.0e-12);
        assertEquals(4.0D, combined.traitLuck(), 1.0e-12);
        assertEquals(0.82D * 1.25D, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(1.06D * 1.20D, combined.tempoMultiplier(), 1.0e-12);
        assertEquals(1.32D * 0.95D,
                FishingGearEffects.catchZoneAreaMultiplier(combined), 1.0e-12);
        assertEquals(1.16D * 1.03D,
                FishingGearEffects.minigameSpeedMultiplier(combined), 1.0e-12);
        assertEquals(0.55D,
                FishingGearEffects.catchLossPreventionChance(combined), 1.0e-12);
        assertFishOnly(combined);
    }

    private static void assertFishOnly(FishingGearModifiers modifiers) {
        assertTrue(modifiers.categoryRestriction().allows(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY));
        assertFalse(modifiers.categoryRestriction().allows("crate"));
    }
}
