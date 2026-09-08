package com.redslovesgames.tideborne.fishing.v2;

/** Direct lognormal CDF and quantile model defined by median length and log-space sigma. */
public record LogNormalSizeDistribution(double medianLength, double sigma) implements SizeDistribution {
    public LogNormalSizeDistribution {
        if (!Double.isFinite(medianLength) || medianLength <= 0.0) {
            throw new IllegalArgumentException("medianLength must be finite and positive");
        }
        if (!Double.isFinite(sigma) || sigma <= 0.0) {
            throw new IllegalArgumentException("sigma must be finite and positive");
        }
    }

    @Override
    public double cdf(double length) {
        if (Double.isNaN(length)) {
            throw new IllegalArgumentException("length must not be NaN");
        }
        if (length <= 0.0) {
            return 0.0;
        }
        double z = (Math.log(length) - Math.log(medianLength)) / sigma;
        return NormalDistributionMath.cdf(z);
    }

    @Override
    public double quantile(double probability) {
        if (probability < 0.0 || probability > 1.0 || Double.isNaN(probability)) {
            throw new IllegalArgumentException("probability must be between 0 and 1");
        }
        if (probability == 0.0) {
            return 0.0;
        }
        if (probability == 1.0) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.exp(Math.log(medianLength) + sigma * NormalDistributionMath.inverseCdf(probability));
    }
}

