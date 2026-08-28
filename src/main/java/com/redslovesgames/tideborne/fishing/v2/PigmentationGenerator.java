package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Deterministic Pigmentation selection for canonical Fishing System 2.0 specimens.
 *
 * <p>Pigmentation is independent from Body Type, Condition, and every other trait axis. A 1.5%
 * event is sampled from the dedicated Pigmentation event stream. When it triggers, a second
 * dedicated Pigmentation stream selects Albino 70% of the time and Iridescent 30% of the time.
 */
public final class PigmentationGenerator {
    public static final double BASE_EVENT_PROBABILITY = 0.015;
    public static final double ALBINO_PROBABILITY_GIVEN_EVENT = 0.70;
    public static final double IRIDESCENT_PROBABILITY_GIVEN_EVENT = 0.30;

    /** Selects exactly one canonical Pigmentation value from the specimen seed. */
    public SpecimenData.Pigmentation generate(long specimenSeed) {
        if (TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PIGMENTATION_EVENT) >= BASE_EVENT_PROBABILITY) {
            return SpecimenData.Pigmentation.NORMAL;
        }

        return TraitRandom.unitDouble(specimenSeed, TraitRandom.Salts.PIGMENTATION_VARIANT)
                < ALBINO_PROBABILITY_GIVEN_EVENT
                ? SpecimenData.Pigmentation.ALBINO
                : SpecimenData.Pigmentation.IRIDESCENT;
    }

    /**
     * Applies the deterministic Pigmentation to an existing canonical specimen without changing
     * any other specimen axis or consuming another natural percentile or size sample.
     */
    public SpecimenData apply(SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        SpecimenData.Pigmentation pigmentation = generate(specimen.deterministicSeed());

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
