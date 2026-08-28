package com.redslovesgames.tideborne.fishing.v2;

/**
 * Deterministic Body Type selection for canonical Fishing System 2.0 specimens.
 *
 * <p>Body Type is a 5% event. When the event triggers, the natural percentile smoothly biases
 * the Giant-versus-Dwarf split without making either variant impossible anywhere in the valid
 * percentile range.
 */
public final class BodyTypeGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.05;

    /**
     * Selects the canonical Body Type from the specimen seed and natural percentile.
     */
    public SpecimenData.BodyType generate(long specimenSeed, double naturalPercentile) {
        validatePercentile(naturalPercentile);

        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.BODY_TYPE_EVENT) >= BASE_EVENT_PROBABILITY) {
            return SpecimenData.BodyType.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.BODY_TYPE_VARIANT)
                < giantProbability(naturalPercentile)
                ? SpecimenData.BodyType.GIANT
                : SpecimenData.BodyType.DWARF;
    }

    /**
     * Smooth Giant bias used after a Body Type event has triggered.
     *
     * <p>{@code giantProbability = 0.25 + 0.50 * (percentile / 100.0)}. This yields 25% Giant at
     * P0, 50% at P50, and 75% at P100, so Giant and Dwarf both remain possible at every
     * percentile with no hard threshold.
     */
    public static double giantProbability(double naturalPercentile) {
        validatePercentile(naturalPercentile);
        return 0.25 + 0.50 * (naturalPercentile / 100.0);
    }

    private static void validatePercentile(double naturalPercentile) {
        if (!Double.isFinite(naturalPercentile) || naturalPercentile < 0.0 || naturalPercentile > 100.0) {
            throw new IllegalArgumentException("naturalPercentile must be between 0 and 100");
        }
    }
}
