package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Deterministic Condition selection for canonical Fishing System 2.0 specimens.
 *
 * <p>Condition is independent from Body Type and every other trait axis. The 5% base event
 * probability is adjusted through the canonical rarity-compensation and Trait Luck pipeline before
 * the dedicated Condition event stream is evaluated. When it triggers, a second dedicated Condition
 * stream selects Scarred 65% of the time and Parasite-Ridden 35% of the time.
 */
public final class ConditionGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.05;
    public static final double SCARRED_PROBABILITY_GIVEN_EVENT = 0.65;
    public static final double PARASITE_RIDDEN_PROBABILITY_GIVEN_EVENT = 0.35;

    private final TraitProbabilityService traitProbabilities;

    public ConditionGenerator() {
        this(new TraitProbabilityService());
    }

    ConditionGenerator(TraitProbabilityService traitProbabilities) {
        this.traitProbabilities = Objects.requireNonNull(traitProbabilities, "traitProbabilities");
    }

    /** Selects exactly one canonical Condition value from the specimen seed. */
    public SpecimenData.Condition generate(long specimenSeed, SpeciesProfile species, double traitLuck) {
        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.CONDITION_EVENT)
                >= eventProbability(species, traitLuck)) {
            return SpecimenData.Condition.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.CONDITION_VARIANT)
                < SCARRED_PROBABILITY_GIVEN_EVENT
                ? SpecimenData.Condition.SCARRED
                : SpecimenData.Condition.PARASITE_RIDDEN;
    }

    /** Returns the final Condition event probability without consuming any RNG. */
    public double eventProbability(SpeciesProfile species, double traitLuck) {
        return traitProbabilities.calculate(BASE_EVENT_PROBABILITY, species, traitLuck);
    }

    /**
     * Applies the deterministic Condition to an existing canonical specimen without changing any
     * other specimen axis or consuming another natural percentile or size sample.
     */
    public SpecimenData apply(SpeciesProfile species, SpecimenData specimen, double traitLuck) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(specimen, "specimen");
        if (!species.speciesId().equals(specimen.speciesId())) {
            throw new IllegalArgumentException("species profile and specimen IDs must match");
        }
        SpecimenData.Condition condition = generate(specimen.deterministicSeed(), species, traitLuck);

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
                condition,
                specimen.pigmentation(),
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                specimen.rawFishScore(),
                specimen.fishScore(),
                specimen.provenance()
        );
    }
}
