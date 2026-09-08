package com.redslovesgames.tideborne.fishing.v2;

/** Dependency-free direct normal CDF and inverse CDF calculations. */
final class NormalDistributionMath {
    private static final double SQRT_TWO = Math.sqrt(2.0);

    private NormalDistributionMath() {
    }

    static double cdf(double value) {
        if (value == Double.NEGATIVE_INFINITY) {
            return 0.0;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return 1.0;
        }
        if (!Double.isFinite(value)) {
            return Double.NaN;
        }
        if (value == 0.0) {
            return 0.5;
        }
        return 0.5 * (1.0 + erf(value / SQRT_TWO));
    }

    /** Abramowitz and Stegun 7.1.26, with maximum absolute error near 1.5e-7. */
    private static double erf(double value) {
        double sign = Math.copySign(1.0, value);
        double x = Math.abs(value);
        double t = 1.0 / (1.0 + 0.3275911 * x);
        double polynomial = (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t;
        return sign * (1.0 - polynomial * Math.exp(-x * x));
    }

    /** Peter J. Acklam's inverse-normal rational approximation. */
    static double inverseCdf(double probability) {
        if (probability < 0.0 || probability > 1.0 || Double.isNaN(probability)) {
            throw new IllegalArgumentException("probability must be between 0 and 1");
        }
        if (probability == 0.0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (probability == 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        double[] a = {
                -3.969683028665376e+01,
                2.209460984245205e+02,
                -2.759285104469687e+02,
                1.383577518672690e+02,
                -3.066479806614716e+01,
                2.506628277459239e+00
        };
        double[] b = {
                -5.447609879822406e+01,
                1.615858368580409e+02,
                -1.556989798598866e+02,
                6.680131188771972e+01,
                -1.328068155288572e+01
        };
        double[] c = {
                -7.784894002430293e-03,
                -3.223964580411365e-01,
                -2.400758277161838e+00,
                -2.549732539343734e+00,
                4.374664141464968e+00,
                2.938163982698783e+00
        };
        double[] d = {
                7.784695709041462e-03,
                3.224671290700398e-01,
                2.445134137142996e+00,
                3.754408661907416e+00
        };

        double lower = 0.02425;
        double upper = 1.0 - lower;
        if (probability < lower) {
            double q = Math.sqrt(-2.0 * Math.log(probability));
            return (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                    / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        }
        if (probability > upper) {
            double q = Math.sqrt(-2.0 * Math.log(1.0 - probability));
            return -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5])
                    / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        }

        double q = probability - 0.5;
        double r = q * q;
        return (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q
                / (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1.0);
    }
}

