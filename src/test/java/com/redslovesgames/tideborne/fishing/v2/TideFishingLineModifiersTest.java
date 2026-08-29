package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TideFishingLineModifiersTest {
    @Test
    void legacyLinesMapToExactTide211Multipliers() {
        FishingGearModifiers copper = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER);
        FishingGearModifiers iron = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.IRON);
        FishingGearModifiers golden = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.GOLDEN);
        FishingGearModifiers diamond = TideFishingLineModifiers.forLegacyLine(TideFishingLineModifiers.LegacyLine.DIAMOND);

        assertEquals(1.0D, copper.strengthMultiplier());
        assertEquals(0.90D, copper.tempoMultiplier());
        assertEquals(0.86D, iron.strengthMultiplier());
        assertEquals(1.0D, iron.tempoMultiplier());
        assertEquals(1.0D, golden.strengthMultiplier());
        assertEquals(0.95D, golden.tempoMultiplier());
        assertEquals(0.75D, diamond.strengthMultiplier());
        assertEquals(1.0D, diamond.tempoMultiplier());
    }

    @Test
    void nullOrUnrecognizedLineModelIsNeutral() {
        assertEquals(FishingGearModifiers.neutral(), TideFishingLineModifiers.forLegacyLine(null));
    }

    @Test
    void lineModifiersComposeThroughCanonicalStage33Model() {
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
        assertEquals(0.99D, copper.tempoMultiplier(), 1.0e-12);
        assertEquals(4.0D, copper.fishingLuck());
        assertEquals(0.90D, diamond.strengthMultiplier(), 1.0e-12);
        assertEquals(1.10D, diamond.tempoMultiplier(), 1.0e-12);
        assertEquals(4.0D, diamond.fishingLuck());
    }

    @Test
    void oneApplicationMatchesLegacyAndDoubleApplicationDoesNot() {
        double baseTempo = 0.50D;
        double copper = TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER)
                .tempoMultiplier();

        double once = baseTempo * copper;
        double twice = once * copper;

        assertEquals(0.45D, once, 1.0e-12);
        assertNotEquals(once, twice, 1.0e-12);
    }
}
