package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LogNormalSizeDistributionTest {
    private final LogNormalSizeDistribution distribution = new LogNormalSizeDistribution(40.0, 0.35);

    @Test
    void medianIsFiftiethPercentile() {
        assertEquals(0.5, distribution.cdf(40.0), 1.0e-12);
        assertEquals(40.0, distribution.quantile(0.5), 1.0e-10);
    }

    @Test
    void directCdfAndQuantileRoundTripWithoutSampleTable() {
        for (double probability : new double[]{0.001, 0.01, 0.1, 0.25, 0.75, 0.9, 0.99, 0.999}) {
            double length = distribution.quantile(probability);
            assertEquals(probability, distribution.cdf(length), 2.0e-7);
        }
    }

    @Test
    void quantilesIncreaseStrictly() {
        assertTrue(distribution.quantile(0.1) < distribution.quantile(0.5));
        assertTrue(distribution.quantile(0.5) < distribution.quantile(0.9));
    }
}

