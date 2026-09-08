package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CatchSeedDeriverTest {
    @Test
    void seedRolesAreStableAndIndependent() {
        long catchSeed = 0x1234ABCD5678EF90L;
        assertEquals(CatchSeedDeriver.selectionSeed(catchSeed), CatchSeedDeriver.selectionSeed(catchSeed));
        assertEquals(CatchSeedDeriver.specimenSeed(catchSeed), CatchSeedDeriver.specimenSeed(catchSeed));
        assertNotEquals(CatchSeedDeriver.selectionSeed(catchSeed), CatchSeedDeriver.specimenSeed(catchSeed));
    }

    @Test
    void adjacentCatchSeedsDoNotCollapseToSameDerivedSeeds() {
        assertNotEquals(CatchSeedDeriver.selectionSeed(10L), CatchSeedDeriver.selectionSeed(11L));
        assertNotEquals(CatchSeedDeriver.specimenSeed(10L), CatchSeedDeriver.specimenSeed(11L));
    }
}
