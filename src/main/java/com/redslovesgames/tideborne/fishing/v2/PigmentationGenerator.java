package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Deterministic Pigmentation selection for canonical Fishing System 2.0 specimens.
 *
 * <p>Pigmentation is independent from Body Type, Condition, and every other trait axis. The 1.5%
 * base event probability is adjusted through the canonical rarity-compensation and Trait Luck
 * pipeline before the dedicated Pigmentation event stream is evaluated. When it triggers, a second
 * dedicated Pigmentation stream selects Albino 70% of the time and Iridescent 30% of the time.
 */
public final class PigmentationGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.015;
    public static final double ALBINO_PROBABILITY_GIVEN_EVENT = 0.70;
    public static final double IRIDESCENT_PROBABILITY_GIVEN_EVENT = 0.30;

    private final TraitProbabilityService traitProbabilities;

    public PigmentationGenerator() {
        this(new TraitProbabilityService());
    }

    PigmentationGenerator(TraitProbabilityService traitProbabilities) {
        this.traitProbabilities = Objects.requireNonNull(traitProbabilities, "traitProbabilities");
    }

    /** Selects exactly one canonical Pigmentation value from the specimen seed. */
    public SpecimenData.Pigmentation generate(long specimenSeed, SpeciesProfile species, double traitLuck) {
        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PIGMENTATION_EVENT)
                >= eventProbability(species, traitLuck)) {
            return SpecimenData.Pigmentation.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PIGMENTATION_VARIANT)
                < ALBINO_PROBABILITY_GIVEN_EVENT
                ? SpecimenData.Pigmentation.ALBINO
                : SpecimenData.Pigmentation.IRIDESCENT;
    }

    /** Returns the final Pigmentation event probability without consuming any RNG. */
    public double eventProbability(SpeciesProfile species, double traitLuck) {
        return traitProbabilities.calculate(BASE_EVENT_PROBABILITY, species, traitLuck);
    }

    /**
     * Applies the deterministic Pigmentation to an existing canonical specimen without changing
     * any other specimen axis or consuming another natural percentile or size sample.
     */
    public SpecimenData apply(SpeciesProfile species, SpecimenData specimen, double traitLuck) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(specimen, "specimen");
        if (!species.speciesId().equals(specimen.speciesId())) {
            throw new IllegalArgumentException("species profile and specimen IDs must match");
        }
        SpecimenData.Pigmentation pigmentation = generate(specimen.deterministicSeed(), species, traitLuck);

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
                pigmentation,
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                specimen.rawFishScore(),
                specimen.fishScore(),
                specimen.provenance()
        );
    }
}
