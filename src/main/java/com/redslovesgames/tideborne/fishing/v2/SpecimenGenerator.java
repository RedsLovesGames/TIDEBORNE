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
     * Generates a complete canonical specimen without a Perfect Catch. This pure convenience path
     * preserves the same two-phase ordering used by runtime: pre-fight identity first, then post-fight
     * Condition and Pigmentation finalization.
     */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck,
            double bodyTypeEventProbabilityMultiplier
    ) {
        SpecimenData preFight = generatePreFight(
                species,
                deterministicSeed,
                provenance,
                traitLuck,
                bodyTypeEventProbabilityMultiplier
        );
        return finalizeAfterFight(species, preFight, traitLuck, false);
    }

    /**
     * Generates the portion of canonical specimen identity required before Tide's minigame starts.
     * Natural percentile/base length are sampled exactly once, then Body Type and its physical-size
     * effect are finalized. Post-fight axes remain at their neutral values until Perfect Catch is known.
     */
    public SpecimenData generatePreFight(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck
    ) {
        return generatePreFight(species, deterministicSeed, provenance, traitLuck, 1.0);
    }

    /**
     * Pre-fight generation with the explicit Body Type event multiplier reserved by the shared trait
     * probability API. The multiplier remains 1.0 in the current runtime stage.
     */
    public SpecimenData generatePreFight(
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
        return bodyTypes.applyPhysicalSize(species, baseSpecimen, bodyType);
    }

    /**
     * Finalizes post-fight canonical state after Tide has resolved the center-zone skill check.
     * Perfect Catch is copied into canonical specimen state before Condition or Pigmentation are
     * generated, so later Perfect Catch probability rewards can use this lifecycle without moving the
     * persistence boundary again. This stage intentionally does not change probability math yet.
     *
     * <p>The existing pre-fight species, deterministic seed, natural percentile, base length, Body
     * Type, final physical length, and size-adjusted final percentile are preserved exactly.
     */
    public SpecimenData finalizeAfterFight(
            SpeciesProfile species,
            SpecimenData preFightSpecimen,
            double traitLuck,
            boolean perfectCatch
    ) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(preFightSpecimen, "preFightSpecimen");
        if (!species.speciesId().equals(preFightSpecimen.speciesId())) {
            throw new IllegalArgumentException("species profile and specimen IDs must match");
        }

        SpecimenData skillCaptured = withPerfectCatch(preFightSpecimen, perfectCatch);
        SpecimenData conditionedSpecimen = conditions.apply(species, skillCaptured, traitLuck);
        return pigmentations.apply(species, conditionedSpecimen, traitLuck);
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

    private static SpecimenData withPerfectCatch(SpecimenData specimen, boolean perfectCatch) {
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
                specimen.specimenQuality(),
                perfectCatch,
                specimen.rawFishScore(),
                specimen.fishScore(),
                specimen.provenance()
        );
    }
}
