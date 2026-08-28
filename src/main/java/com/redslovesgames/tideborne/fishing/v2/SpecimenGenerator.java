package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.SplittableRandom;

/** Deterministically generates one natural specimen sample, then finalizes Body Type and Condition. */
public final class SpecimenGenerator {
    public static final int SCHEMA_VERSION = 2;
    public static final int GENERATION_VERSION = 1;

    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final ConditionGenerator conditions = new ConditionGenerator();

    /**
     * Generates the complete canonical specimen state currently implemented through the independent
     * Condition axis. Natural percentile and base length are sampled exactly once. Body Type and
     * Condition are each derived once from their independent deterministic trait streams.
     */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance
    ) {
        SpecimenData baseSpecimen = generateBase(species, deterministicSeed, provenance);
        SpecimenData.BodyType bodyType = bodyTypes.generate(
                baseSpecimen.deterministicSeed(),
                baseSpecimen.basePercentile()
        );
        SpecimenData sizedSpecimen = bodyTypes.applyPhysicalSize(species, baseSpecimen, bodyType);
        return conditions.apply(sizedSpecimen);
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
