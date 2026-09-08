package com.redslovesgames.tideborne.fishing.specimen;

import java.util.Objects;

/**
 * Pure deterministic Fishing System 2.0 Specimen Quality selection.
 *
 * <p>The Perfect Specimen chance starts from the frozen continuous piecewise-linear curve over the
 * canonical final specimen percentile. The Quality-specific frozen rule then applies total Trait
 * Luck followed by the direct Perfect Catch Quality bonus. Rarity compensation is intentionally not
 * applied here because the authoritative Perfect Specimen rule names only the percentile curve,
 * Trait Luck, and Perfect Catch. Selection uses the dedicated
 * {@link TraitRandom.Salts#PERFECT_SPECIMEN} stream and never changes size or percentile state.
 */
public final class SpecimenQualityService {
    public static final double MINIMUM_PERCENTILE = 95.0;
    public static final double P95_PROBABILITY = 0.02;
    public static final double P97_5_PROBABILITY = 0.08;
    public static final double P99_PROBABILITY = 0.25;
    public static final double P99_9_PROBABILITY = 0.60;

    /**
     * Direct Perfect Catch Quality reward. Squaring the miss probability is equivalent to giving a
     * second independent opportunity at the same already-luck-adjusted chance. It is substantial,
     * keeps a zero base chance at zero, and remains strictly below certainty for ordinary finite
     * probabilities.
     */
    public static final double PERFECT_CATCH_MISS_EXPONENT = 2.0;

    private static final double MAX_NON_GUARANTEED_PROBABILITY = Math.nextDown(1.0);

    private final TraitLuckProbabilityService traitLuckProbabilities;

    public SpecimenQualityService() {
        this(new TraitLuckProbabilityService());
    }

    SpecimenQualityService(TraitLuckProbabilityService traitLuckProbabilities) {
        this.traitLuckProbabilities = Objects.requireNonNull(
                traitLuckProbabilities,
                "traitLuckProbabilities"
        );
    }

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

    /**
     * Returns the final Perfect Specimen event probability.
     *
     * <p>Order is canonical percentile curve, total Trait Luck, direct Perfect Catch Quality bonus,
     * then the final non-guaranteed bound for Perfect Catch. A zero base chance remains exactly zero,
     * so modifiers cannot create Perfect Specimens below P95.
     */
    public double probability(double finalPercentile, double totalTraitLuck, boolean perfectCatch) {
        double baseProbability = baseProbability(finalPercentile);
        if (baseProbability == 0.0) {
            return 0.0;
        }

        double adjustedProbability = traitLuckProbabilities.adjustProbability(
                baseProbability,
                totalTraitLuck
        );
        if (!perfectCatch || adjustedProbability == 0.0) {
            return adjustedProbability;
        }

        double missProbability = 1.0 - adjustedProbability;
        double boostedProbability = 1.0 - Math.pow(
                missProbability,
                PERFECT_CATCH_MISS_EXPONENT
        );
        return Math.min(MAX_NON_GUARANTEED_PROBABILITY, boostedProbability);
    }

    /** Selects exactly one canonical Specimen Quality value from the specimen seed. */
    public SpecimenData.SpecimenQuality generate(long specimenSeed, double finalPercentile) {
        return generate(specimenSeed, finalPercentile, 0.0, false);
    }

    /** Selects Quality after the frozen modifier pipeline without consuming another RNG stream. */
    public SpecimenData.SpecimenQuality generate(
            long specimenSeed,
            double finalPercentile,
            double totalTraitLuck,
            boolean perfectCatch
    ) {
        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PERFECT_SPECIMEN)
                < probability(finalPercentile, totalTraitLuck, perfectCatch)
                ? SpecimenData.SpecimenQuality.PERFECT_SPECIMEN
                : SpecimenData.SpecimenQuality.NORMAL;
    }

    /** Applies unmodified base Quality for compatibility with the Stage 19 pure call shape. */
    public SpecimenData apply(SpecimenData specimen) {
        return apply(specimen, 0.0, false);
    }

    /**
     * Applies Specimen Quality from the existing canonical final percentile without rewriting any
     * size, percentile, or other trait field.
     */
    public SpecimenData apply(
            SpecimenData specimen,
            double totalTraitLuck,
            boolean perfectCatch
    ) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.SpecimenQuality quality = generate(
                specimen.deterministicSeed(),
                specimen.finalPercentile(),
                totalTraitLuck,
                perfectCatch
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
