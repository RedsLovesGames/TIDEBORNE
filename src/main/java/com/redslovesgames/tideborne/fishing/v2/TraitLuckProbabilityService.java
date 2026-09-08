package com.redslovesgames.tideborne.fishing.v2;

/**
 * Pure Fishing System 2.0 probability adjustment for Trait Luck.
 *
 * <p>This service performs no random selection and has no dependency on Fishing Luck or species
 * selection. Callers supply a base event probability and separately compare the adjusted result
 * against the deterministic random stream for the relevant trait axis.
 */
public final class TraitLuckProbabilityService {
    public static final double MIN_TRAIT_LUCK = -10.0;

    /**
     * Applies the canonical Trait Luck formula {@code 1 - (1 - P)^(1 + T / 10)}.
     *
     * <p>Base probabilities below zero clamp to zero and values above one clamp to one. NaN base
     * probabilities are rejected because they cannot represent an event chance. Trait Luck below
     * {@value #MIN_TRAIT_LUCK} clamps to that floor so the exponent never becomes negative. NaN
     * Trait Luck behaves as zero, negative infinity reaches the floor, and positive infinity
     * saturates any nonzero, nonunit probability to one.
     */
    public double adjustProbability(double probability, double traitLuck) {
        double baseProbability = clampProbability(probability);
        if (baseProbability == 0.0 || baseProbability == 1.0) {
            return baseProbability;
        }

        double effectiveTraitLuck = normalizeTraitLuck(traitLuck);
        if (effectiveTraitLuck == 0.0) {
            return baseProbability;
        }

        double exponent = 1.0 + effectiveTraitLuck / 10.0;
        if (exponent <= 0.0) {
            return 0.0;
        }
        if (Double.isInfinite(exponent)) {
            return 1.0;
        }

        double adjusted = -Math.expm1(exponent * Math.log1p(-baseProbability));
        return Math.max(0.0, Math.min(1.0, adjusted));
    }

    private static double clampProbability(double probability) {
        if (Double.isNaN(probability)) {
            throw new IllegalArgumentException("probability must not be NaN");
        }
        return Math.max(0.0, Math.min(1.0, probability));
    }

    private static double normalizeTraitLuck(double traitLuck) {
        if (Double.isNaN(traitLuck)) {
            return 0.0;
        }
        return Math.max(MIN_TRAIT_LUCK, traitLuck);
    }
}
