package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Pure deterministic Fishing System 2.0 Specimen Quality selection.
 *
 * <p>The base Perfect Specimen chance is a continuous piecewise-linear curve over the canonical
 * final specimen percentile. This service intentionally applies neither Trait Luck nor any direct
 * Perfect Catch bonus yet. Selection uses the dedicated {@link TraitRandom.Salts#PERFECT_SPECIMEN}
 * stream and never changes specimen size or percentile state.
 */
public final class SpecimenQualityService {
    public static final double MINIMUM_PERCENTILE = 95.0;
    public static final double P95_PROBABILITY = 0.02;
    public static final double P97_5_PROBABILITY = 0.08;
    public static final double P99_PROBABILITY = 0.25;
    public static final double P99_9_PROBABILITY = 0.60;

    /** Returns the frozen base probability for a canonical final percentile. */
    public double baseProbability(double finalPercentile) {
        validatePercentile(finalPercentile);

        if (finalPercentile < 95.0) {
            return 0.0;
        }
        if (finalPercentile <= 97.5) {
            return interpolate(finalPercentile, 95.0, P95_PROBABILITY, 97.5, P97_5_PROBABILITY);
        }
        if (finalPercentile <= 99.0) {
            return interpolate(finalPercentile, 97.5, P97_5_PROBABILITY, 99.0, P99_PROBABILITY);
        }
        if (finalPercentile < 99.9) {
            return interpolate(finalPercentile, 99.0, P99_PROBABILITY, 99.9, P99_9_PROBABILITY);
        }
        return P99_9_PROBABILITY;
    }

    /** Selects exactly one canonical Specimen Quality value from the specimen seed. */
    public SpecimenData.SpecimenQuality generate(long specimenSeed, double finalPercentile) {
        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PERFECT_SPECIMEN)
                < baseProbability(finalPercentile)
                ? SpecimenData.SpecimenQuality.PERFECT_SPECIMEN
                : SpecimenData.SpecimenQuality.NORMAL;
    }

    /**
     * Applies Specimen Quality from the existing canonical final percentile without rewriting any
     * size, percentile, or other trait field.
     */
    public SpecimenData apply(SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.SpecimenQuality quality = generate(
                specimen.deterministicSeed(),
                specimen.finalPercentile()
        );

        return new SpecimenData(
                specimen.speciesId(),
                specimen.schemaVersion(),
                specimen.generationVersion(),
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                specimen.baseLength(),
                specimen.finalLength(),
                specimen.finalPercentile(),
                specimen.bodyType(),
                specimen.condition(),
                specimen.pigmentation(),
                quality,
                specimen.perfectCatch(),
                specimen.rawFishScore(),
                specimen.fishScore(),
                specimen.provenance()
        );
    }

    private static double interpolate(double value, double x0, double y0, double x1, double y1) {
        double t = (value - x0) / (x1 - x0);
        return y0 + t * (y1 - y0);
    }

    private static void validatePercentile(double percentile) {
        if (!Double.isFinite(percentile) || percentile < 0.0 || percentile > 100.0) {
            throw new IllegalArgumentException("finalPercentile must be between 0 and 100");
        }
    }
}
