package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Deterministic Body Type selection and physical-size finalization for canonical Fishing System 2.0 specimens.
 *
 * <p>Body Type is a 5% event. When the event triggers, the natural percentile smoothly biases
 * the Giant-versus-Dwarf split without making either variant impossible anywhere in the valid
 * percentile range. Giant and Dwarf physical multipliers use their own deterministic trait stream
 * and never consume or replace the specimen's natural percentile or base-size sample.
 */
public final class BodyTypeGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.05;
    public static final double GIANT_MIN_SIZE_MULTIPLIER = 1.10;
    public static final double GIANT_MAX_SIZE_MULTIPLIER = 1.30;
    public static final double DWARF_MIN_SIZE_MULTIPLIER = 0.60;
    public static final double DWARF_MAX_SIZE_MULTIPLIER = 0.82;

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
     * Returns the deterministic physical-size multiplier for the selected Body Type.
     *
     * <p>Normal is exactly {@code 1.0}. Giant and Dwarf use a dedicated Body Type size stream,
     * linearly mapped across their canonical uniform ranges.
     */
    public double sizeMultiplier(long specimenSeed, SpecimenData.BodyType bodyType) {
        Objects.requireNonNull(bodyType, "bodyType");
        if (bodyType == SpecimenData.BodyType.NORMAL) {
            return 1.0;
        }

        double unit = TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.BODY_TYPE_SIZE);
        return switch (bodyType) {
            case GIANT -> interpolate(GIANT_MIN_SIZE_MULTIPLIER, GIANT_MAX_SIZE_MULTIPLIER, unit);
            case DWARF -> interpolate(DWARF_MIN_SIZE_MULTIPLIER, DWARF_MAX_SIZE_MULTIPLIER, unit);
            case NORMAL -> 1.0;
        };
    }

    /**
     * Selects Body Type and applies its physical-size effect to a base canonical specimen.
     */
    public SpecimenData applyPhysicalSize(SpeciesProfile species, SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.BodyType bodyType = generate(specimen.deterministicSeed(), specimen.basePercentile());
        return applyPhysicalSize(species, specimen, bodyType);
    }

    /**
     * Applies one already-selected Body Type without sampling percentile or base length again.
     *
     * <p>{@code finalPercentile} is the percentile implied by {@code finalLength} in the same
     * species size distribution. This is a deterministic CDF transform, not another random
     * specimen percentile. The original natural percentile remains in {@code basePercentile}.
     * Species with no physical-size distribution retain the natural percentile because an adjusted
     * physical percentile is not meaningful for them.
     */
    public SpecimenData applyPhysicalSize(
            SpeciesProfile species,
            SpecimenData specimen,
            SpecimenData.BodyType bodyType
    ) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(specimen, "specimen");
        Objects.requireNonNull(bodyType, "bodyType");
        if (!species.speciesId().equals(specimen.speciesId())) {
            throw new IllegalArgumentException("species profile and specimen IDs must match");
        }

        double multiplier = sizeMultiplier(specimen.deterministicSeed(), bodyType);
        double finalLength = specimen.baseLength() * multiplier;
        double finalPercentile = adjustedFinalPercentile(species, specimen, bodyType, finalLength);

        return new SpecimenData(
                specimen.speciesId(),
                specimen.schemaVersion(),
                specimen.generationVersion(),
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                specimen.baseLength(),
                finalLength,
                finalPercentile,
                bodyType,
                specimen.condition(),
                specimen.pigmentation(),
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                specimen.rawFishScore(),
                specimen.fishScore(),
                specimen.provenance()
        );
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

    private static double adjustedFinalPercentile(
            SpeciesProfile species,
            SpecimenData specimen,
            SpecimenData.BodyType bodyType,
            double finalLength
    ) {
        if (bodyType == SpecimenData.BodyType.NORMAL
                || species.sizeDistribution() == NoPhysicalSizeDistribution.INSTANCE) {
            return specimen.basePercentile();
        }

        double percentile = species.sizeDistribution().percentile(finalLength);
        if (!Double.isFinite(percentile)) {
            throw new IllegalArgumentException("size distribution produced a non-finite final percentile");
        }
        return Math.max(0.0, Math.min(100.0, percentile));
    }

    private static double interpolate(double minimum, double maximum, double unit) {
        return minimum + (maximum - minimum) * unit;
    }

    private static void validatePercentile(double naturalPercentile) {
        if (!Double.isFinite(naturalPercentile) || naturalPercentile < 0.0 || naturalPercentile > 100.0) {
            throw new IllegalArgumentException("naturalPercentile must be between 0 and 100");
        }
    }
}
