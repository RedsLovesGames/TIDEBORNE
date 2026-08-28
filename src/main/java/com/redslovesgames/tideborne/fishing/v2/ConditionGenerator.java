package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Deterministic Condition selection for canonical Fishing System 2.0 specimens.
 *
 * <p>Condition is independent from Body Type and every other trait axis. A 5% event is sampled
 * from the dedicated Condition event stream. When it triggers, a second dedicated Condition
 * stream selects Scarred 65% of the time and Parasite-Ridden 35% of the time.
 */
public final class ConditionGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.05;
    public static final double SCARRED_PROBABILITY_GIVEN_EVENT = 0.65;
    public static final double PARASITE_RIDDEN_PROBABILITY_GIVEN_EVENT = 0.35;

    /** Selects exactly one canonical Condition value from the specimen seed. */
    public SpecimenData.Condition generate(long specimenSeed) {
        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.CONDITION_EVENT) >= BASE_EVENT_PROBABILITY) {
            return SpecimenData.Condition.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.CONDITION_VARIANT)
                < SCARRED_PROBABILITY_GIVEN_EVENT
                ? SpecimenData.Condition.SCARRED
                : SpecimenData.Condition.PARASITE_RIDDEN;
    }

    /**
     * Applies the deterministic Condition to an existing canonical specimen without changing any
     * other specimen axis or consuming another natural percentile or size sample.
     */
    public SpecimenData apply(SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.Condition condition = generate(specimen.deterministicSeed());

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
