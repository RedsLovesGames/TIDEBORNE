package com.redslovesgames.tideborne.fishing.v2;

/**
 * Explicit sentinel for species that Tide exposes without physical size data.
 * Natural specimen percentile can still exist, while physical length remains zero.
 */
public enum NoPhysicalSizeDistribution implements SizeDistribution {
    INSTANCE;

    @Override
    public double cdf(double length) {
        if (Double.isNaN(length)) {
            throw new IllegalArgumentException("length must not be NaN");
        }
        return length < 0.0 ? 0.0 : 1.0;
    }

    @Override
    public double quantile(double probability) {
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be between 0 and 1");
        }
        return 0.0;
    }
}
