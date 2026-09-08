package com.redslovesgames.tideborne.fishing.specimen;

import com.redslovesgames.tideborne.fishing.SpeciesProfile;

import java.util.Objects;

/**
 * Deterministic Body Type selection and physical-size finalization for canonical Fishing System 2.0 specimens.
 *
 * <p>Body Type starts from a 5% base event probability. Any Body Type-specific event multiplier is
 * applied to that base chance before canonical species rarity compensation and Trait Luck through
 * {@link TraitProbabilityService}. The deterministic Body Type event stream is then evaluated. When
 * the event triggers, the natural percentile smoothly biases the Giant-versus-Dwarf split without
 * making either variant impossible anywhere in the valid percentile range. Giant and Dwarf physical
 * multipliers use their own deterministic trait stream and never consume or replace the specimen's
 * natural percentile or base-size sample.
 */
public final class BodyTypeGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.05;
    public static final double PERFECT_CATCH_EVENT_MULTIPLIER = 1.25;
    public static final double GIANT_MIN_SIZE_MULTIPLIER = 1.10;
    public static final double GIANT_MAX_SIZE_MULTIPLIER = 1.30;
    public static final double DWARF_MIN_SIZE_MULTIPLIER = 0.60;
    public static final double DWARF_MAX_SIZE_MULTIPLIER = 0.82;

    private final TraitProbabilityService traitProbabilities;

    public BodyTypeGenerator() {
        this(new TraitProbabilityService());
    }

    BodyTypeGenerator(TraitProbabilityService traitProbabilities) {
        this.traitProbabilities = Objects.requireNonNull(traitProbabilities, "traitProbabilities");
    }

    /** Selects the canonical Body Type using species rarity and Trait Luck. */
    public SpecimenData.BodyType generate(
            long specimenSeed,
            double naturalPercentile,
            SpeciesProfile species,
            double traitLuck
    ) {
        return generate(specimenSeed, naturalPercentile, species, traitLuck, 1.0);
    }

    /**
     * Selects the canonical Body Type with an axis-specific base event multiplier.
     *
     * <p>The multiplier is applied only to the Body Type event chance before rarity compensation and
     * Trait Luck. It never participates in the independent Giant/Dwarf conditional split.
     */
    public SpecimenData.BodyType generate(
            long specimenSeed,
            double naturalPercentile,
            SpeciesProfile species,
            double traitLuck,
            double eventProbabilityMultiplier
    ) {
        validatePercentile(naturalPercentile);
        double eventProbability = eventProbability(species, traitLuck, eventProbabilityMultiplier);

        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.BODY_TYPE_EVENT) >= eventProbability) {
            return SpecimenData.BodyType.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.BODY_TYPE_VARIANT)
                < giantProbability(naturalPercentile)
                ? SpecimenData.BodyType.GIANT
                : SpecimenData.BodyType.DWARF;
    }

    /** Returns the final Body Type event probability without consuming any RNG. */
    public double eventProbability(
            SpeciesProfile species,
            double traitLuck,
            double eventProbabilityMultiplier
    ) {
        return traitProbabilities.calculate(
                BASE_EVENT_PROBABILITY,
                species,
                traitLuck,
                eventProbabilityMultiplier
        );
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

    /** Selects Body Type at zero Trait Luck and applies its physical-size effect to a base specimen. */
    public SpecimenData applyPhysicalSize(SpeciesProfile species, SpecimenData specimen) {
        return applyPhysicalSize(species, specimen, 0.0, 1.0);
    }

    /** Selects Body Type through the canonical probability pipeline and applies its physical-size effect. */
    public SpecimenData applyPhysicalSize(
            SpeciesProfile species,
            SpecimenData specimen,
            double traitLuck,
            double eventProbabilityMultiplier
    ) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.BodyType bodyType = generate(
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                species,
                traitLuck,
                eventProbabilityMultiplier
        );
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
