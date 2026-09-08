package com.redslovesgames.tideborne.fishing.specimen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CanonicalRarityTest {
    @Test
    void fishingLuckFifteenMatchesDesignTargets() {
        assertEquals(1.00, CanonicalRarity.ONE_STAR.fishingLuckMultiplier(15.0), 0.005);
        assertEquals(1.31, CanonicalRarity.TWO_STAR.fishingLuckMultiplier(15.0), 0.005);
        assertEquals(1.62, CanonicalRarity.THREE_STAR.fishingLuckMultiplier(15.0), 0.005);
        assertEquals(1.93, CanonicalRarity.FOUR_STAR.fishingLuckMultiplier(15.0), 0.005);
        assertEquals(2.24, CanonicalRarity.FIVE_STAR.fishingLuckMultiplier(15.0), 0.005);
    }

    @Test
    void negativeFishingLuckIsNeutral() {
        assertEquals(1.0, CanonicalRarity.FIVE_STAR.fishingLuckMultiplier(-100.0));
    }
}

