package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NoPhysicalSizeDistributionTest {
    @Test
    void unavailablePhysicalSizeAlwaysGeneratesZeroLength() {
        assertEquals(0.0, NoPhysicalSizeDistribution.INSTANCE.quantile(0.0));
        assertEquals(0.0, NoPhysicalSizeDistribution.INSTANCE.quantile(0.5));
        assertEquals(0.0, NoPhysicalSizeDistribution.INSTANCE.quantile(1.0));
    }

    @Test
    void rejectsInvalidProbability() {
        assertThrows(IllegalArgumentException.class, () -> NoPhysicalSizeDistribution.INSTANCE.quantile(-0.01));
        assertThrows(IllegalArgumentException.class, () -> NoPhysicalSizeDistribution.INSTANCE.quantile(1.01));
    }
}
