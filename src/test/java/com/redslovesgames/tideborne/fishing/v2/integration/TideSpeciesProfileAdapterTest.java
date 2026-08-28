package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.li64.tide.data.fishing.SizeData;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import org.junit.jupiter.api.Test;

class TideSpeciesProfileAdapterTest {
    @Test
    void tideTypicalBoundsRemainCanonicalP10AndP90() {
        LogNormalSizeDistribution distribution = TideSpeciesProfileAdapter.sizeDistribution(new SizeData(12.5, 47.0, 88.0));

        assertEquals(12.5, distribution.quantile(0.10), 1.0e-9);
        assertEquals(47.0, distribution.quantile(0.90), 1.0e-9);
        // The direct quantile path is exact for the frozen P10/P90 anchors. The reverse
        // CDF intentionally uses a fast approximation, so validate it within numerical tolerance.
        assertEquals(10.0, distribution.percentile(12.5), 5.0e-4);
        assertEquals(90.0, distribution.percentile(47.0), 5.0e-4);
    }
}
