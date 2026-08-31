package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import com.redslovesgames.tideboundcompatibility.fishing.TideborneFishingGearModifiers;
import com.redslovesgames.tideteamjournal.BobberBonuses;
import com.redslovesgames.tideteamjournal.ServerConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FishingGearBalanceMatrixTest {
    @Test
    void tideLineFightProfilesRemainDistinct() {
        FishingGearModifiers copper = TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_COPPER_LINE);
        FishingGearModifiers iron = TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_IRON_LINE);
        FishingGearModifiers golden = TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_GOLDEN_LINE);
        FishingGearModifiers diamond = TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE);

        assertEquals(0.90D, copper.tempoMultiplier(), 1.0e-12);
        assertEquals(1.0D, copper.strengthMultiplier(), 1.0e-12);
        assertEquals(0.86D, iron.strengthMultiplier(), 1.0e-12);
        assertEquals(1.0D, iron.tempoMultiplier(), 1.0e-12);
        assertEquals(0.95D, golden.tempoMultiplier(), 1.0e-12);
        assertEquals(0.75D, diamond.strengthMultiplier(), 1.0e-12);
    }

    @Test
    void leviathanAndDiamondTradeSelectionPowerForHarderFight() {
        FishingGearModifiers combined = FishingGearModifiers.compose(
                TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE),
                TideborneFishingGearModifiers.leviathanBait(true)
        );

        assertEquals(15.0D, combined.fishingLuck(), 1.0e-12);
        assertEquals(8.0D, combined.traitLuck(), 1.0e-12);
        assertEquals(0.75D * 1.15D, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(1.15D, combined.tempoMultiplier(), 1.0e-12);
        assertTrue(combined.categoryRestriction().allows(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY));
        assertFalse(combined.categoryRestriction().allows("crate"));
        assertFalse(combined.categoryRestriction().allows("item"));
    }

    @Test
    void optionalLineAndLeaderStacksRetainMeaningfulTradeoffs() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers tentacleLeader = FishingGearModifiers.compose(
                TideborneFishingGearModifiers.tentacleLine(config),
                SteelLeaderGearModifiers.forAttachmentState(true, config)
        );
        FishingGearModifiers swiftLeader = FishingGearModifiers.compose(
                TideborneFishingGearModifiers.swiftLine(config),
                SteelLeaderGearModifiers.forAttachmentState(true, config)
        );

        assertEquals(1.45D * 0.90D, FishingGearEffects.catchZoneAreaMultiplier(tentacleLeader), 1.0e-12);
        assertEquals(1.18D * 1.05D, FishingGearEffects.minigameSpeedMultiplier(tentacleLeader), 1.0e-12);
        assertEquals(1.20D * 0.90D, FishingGearEffects.catchZoneAreaMultiplier(swiftLeader), 1.0e-12);
        assertEquals(1.05D * 1.05D, FishingGearEffects.minigameSpeedMultiplier(swiftLeader), 1.0e-12);
        assertEquals(0.90D, FishingGearEffects.catchLossPreventionChance(tentacleLeader), 1.0e-12);
        assertEquals(0.90D, FishingGearEffects.catchLossPreventionChance(swiftLeader), 1.0e-12);
    }

    @Test
    void hookAndRodSelectionModifiersStaySituational() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers predatory = TideborneFishingGearModifiers.sharkToothHook(true, true, false, config);
        FishingGearModifiers tiny = TideborneFishingGearModifiers.sharkToothHook(true, false, true, config);
        FishingGearModifiers seafarer = TideborneFishingGearModifiers.seafarersHook(true, true, config);
        FishingGearModifiers kujira = TideborneFishingGearModifiers.kujiraRod(true, true, config);

        assertEquals(2.5D, FishingGearEffects.fishWeightMultiplier(predatory), 1.0e-12);
        assertEquals(0.35D, FishingGearEffects.fishWeightMultiplier(tiny), 1.0e-12);
        assertEquals(1.35D, FishingGearEffects.fishWeightMultiplier(seafarer), 1.0e-12);
        assertEquals(1.20D, FishingGearEffects.crateWeightMultiplier(kujira), 1.0e-12);
    }

    @Test
    void defaultBobberMatrixUsesOnlyLuckAndLureSpeedAxes() {
        ServerConfig.Values config = new ServerConfig.Values();
        Map<String, BobberBonuses.Bonus> bobbers = config.bobberBonuses;

        assertEquals(32, bobbers.size());
        assertEquals(new BobberBonuses.Bonus(0, 1), bobbers.get("tide:red_bobber"));
        assertEquals(new BobberBonuses.Bonus(2, 2), bobbers.get("tide:enchanted_golden_apple_bobber"));
        assertEquals(new BobberBonuses.Bonus(3, 0), bobbers.get("tide:heart_bobber"));
        assertEquals(new BobberBonuses.Bonus(0, 3), bobbers.get("tide:echo_bobber"));
        assertEquals(new BobberBonuses.Bonus(0, 3), bobbers.get("tide:feather_bobber"));
    }

    @Test
    void noCanonicalGearModifierCanDirectlyAddFishScore() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        for (FishingGearModifiers modifiers : new FishingGearModifiers[]{
                TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_COPPER_LINE),
                TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE),
                TideborneFishingGearModifiers.tentacleLine(config),
                TideborneFishingGearModifiers.swiftLine(config),
                SteelLeaderGearModifiers.forAttachmentState(true, config),
                TideborneFishingGearModifiers.sharkToothHook(true, true, false, config),
                TideborneFishingGearModifiers.seafarersHook(true, true, config),
                TideborneFishingGearModifiers.kujiraRod(true, true, config),
                TideborneFishingGearModifiers.leviathanBait(true)
        }) {
            assertFalse(modifiers.namedAdditiveModifiers().containsKey("fish_score"));
            assertFalse(modifiers.namedMultiplierModifiers().containsKey("fish_score"));
            assertFalse(modifiers.namedAdditiveModifiers().containsKey("canonical_fish_score"));
            assertFalse(modifiers.namedMultiplierModifiers().containsKey("canonical_fish_score"));
        }
    }
}
