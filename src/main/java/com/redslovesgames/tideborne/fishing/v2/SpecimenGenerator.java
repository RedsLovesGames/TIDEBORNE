package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.SplittableRandom;

/** Deterministically generates one natural specimen sample, then finalizes independent trait axes. */
public final class SpecimenGenerator {
    public static final int SCHEMA_VERSION = 2;
    public static final int GENERATION_VERSION = 1;

    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final ConditionGenerator conditions = new ConditionGenerator();
    private final PigmentationGenerator pigmentations = new PigmentationGenerator();

    /**
     * Generates with zero Trait Luck and no Body Type-specific event multiplier.
     */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance
    ) {
        return generate(species, deterministicSeed, provenance, 0.0, 1.0);
    }

    /**
     * Generates with canonical Trait Luck and no Body Type-specific event multiplier.
     */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck
    ) {
        return generate(species, deterministicSeed, provenance, traitLuck, 1.0);
    }

    /**
     * Generates the complete canonical specimen state currently implemented through the independent
     * Pigmentation axis. Natural percentile and base length are sampled exactly once. Body Type,
     * Condition, and Pigmentation are each derived once from independent deterministic trait streams.
     *
     * <p>The Body Type event uses the shared base -> rarity -> Trait Luck probability path. The
     * explicit Body Type event multiplier is currently {@code 1.0}; it reserves the call shape needed
     * by the later Perfect Catch Body Type multiplier without introducing another probability path.
     */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck,
            double bodyTypeEventProbabilityMultiplier
    ) {
        SpecimenData baseSpecimen = generateBase(species, deterministicSeed, provenance);
        SpecimenData.BodyType bodyType = bodyTypes.generate(
                baseSpecimen.deterministicSeed(),
                baseSpecimen.basePercentile(),
                species,
                traitLuck,
                bodyTypeEventProbabilityMultiplier
        );
        SpecimenData sizedSpecimen = bodyTypes.applyPhysicalSize(species, baseSpecimen, bodyType);
        SpecimenData conditionedSpecimen = conditions.apply(sizedSpecimen);
        return pigmentations.apply(conditionedSpecimen);
    }

    /** Generates only the natural percentile and base length exactly once. */
    public SpecimenData generateBase(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance
    ) {
        Objects.requireNonNull(species, "species");
        SplittableRandom random = new SplittableRandom(deterministicSeed);
        double probability = random.nextDouble();
        double percentile = probability * 100.0;
        double length = species.sizeDistribution().quantile(probability);

        return new SpecimenData(
                species.speciesId(),
                SCHEMA_VERSION,
                GENERATION_VERSION,
                deterministicSeed,
                percentile,
                length,
                length,
                percentile,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                provenance
        );
    }
}
