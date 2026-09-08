package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TideFishingLineModifiersTest {
    @Test
    void materialLinesMapToRebalancedCanonicalMultipliers() {
        FishingGearModifiers copper = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER);
        FishingGearModifiers iron = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.IRON);
        FishingGearModifiers golden = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.GOLDEN);
        FishingGearModifiers diamond = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.DIAMOND);

        assertEquals(1.0D, copper.strengthMultiplier());
        assertEquals(0.94D, copper.tempoMultiplier());
        assertEquals(1.02D, FishingGearEffects.catchZoneAreaMultiplier(copper), 1.0e-12);

        assertEquals(0.92D, iron.strengthMultiplier());
        assertEquals(1.0D, iron.tempoMultiplier());
        assertEquals(1.03D, FishingGearEffects.catchZoneAreaMultiplier(iron), 1.0e-12);

        assertEquals(1.05D, golden.strengthMultiplier());
        assertEquals(0.86D, golden.tempoMultiplier());

        assertEquals(0.82D, diamond.strengthMultiplier());
        assertEquals(1.06D, diamond.tempoMultiplier());
    }

    @Test
    void nullOrUnrecognizedLineModelIsNeutral() {
        assertEquals(FishingGearModifiers.neutral(), TideFishingLineModifiers.forLegacyLine(null));
    }

    @Test
    void lineModifiersComposeThroughCanonicalModel() {
        FishingGearModifiers existing = FishingGearModifiers.builder()
                .strengthMultiplier(1.20D)
                .tempoMultiplier(1.10D)
                .fishingLuck(4.0D)
                .build();

        FishingGearModifiers copper = FishingGearModifiers.compose(
                existing,
                TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER)
        );
        FishingGearModifiers diamond = FishingGearModifiers.compose(
                existing,
                TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.DIAMOND)
        );

        assertEquals(1.20D, copper.strengthMultiplier(), 1.0e-12);
        assertEquals(1.034D, copper.tempoMultiplier(), 1.0e-12);
        assertEquals(1.02D, FishingGearEffects.catchZoneAreaMultiplier(copper), 1.0e-12);
        assertEquals(4.0D, copper.fishingLuck());

        assertEquals(0.984D, diamond.strengthMultiplier(), 1.0e-12);
        assertEquals(1.166D, diamond.tempoMultiplier(), 1.0e-12);
        assertEquals(4.0D, diamond.fishingLuck());
    }

    @Test
    void oneApplicationMatchesRebalancedValueAndDoubleApplicationDoesNot() {
        double baseTempo = 0.50D;
        double copper = TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER)
                .tempoMultiplier();

        double once = baseTempo * copper;
        double twice = once * copper;

        assertEquals(0.47D, once, 1.0e-12);
        assertNotEquals(once, twice, 1.0e-12);
    }
}
