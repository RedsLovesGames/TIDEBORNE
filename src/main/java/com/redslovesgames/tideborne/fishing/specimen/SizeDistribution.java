package com.redslovesgames.tideborne.fishing.specimen;

/** Direct continuous size distribution. Implementations must not use sampled lookup tables. */
public interface SizeDistribution {
    double cdf(double length);

    double quantile(double probability);

    default double percentile(double length) {
        return 100.0 * cdf(length);
    }
}

