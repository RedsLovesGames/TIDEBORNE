package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.SplittableRandom;

/** Deterministically generates one natural specimen sample, then finalizes independent trait axes. */
public final class SpecimenGenerator {
    public static final int SCHEMA_VERSION = 2;
    public static final int GENERATION_VERSION = 1;
    public static final double PERFECT_CATCH_TRAIT_LUCK_BONUS = 10.0;

    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final ConditionGenerator conditions = new ConditionGenerator();
    private final PigmentationGenerator pigmentations = new PigmentationGenerator();
    private final SpecimenQualityService specimenQuality = new SpecimenQualityService();
    private final FishScoreV2Service fishScores = new FishScoreV2Service();

    public SpecimenData generate(SpeciesProfile species, long deterministicSeed, SpecimenData.Provenance provenance) {
        return generate(species, deterministicSeed, provenance, 0.0, 1.0);
    }

    public SpecimenData generate(SpeciesProfile species, long deterministicSeed, SpecimenData.Provenance provenance, double traitLuck) {
        return generate(species, deterministicSeed, provenance, traitLuck, 1.0);
    }

    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck,
            double bodyTypeEventProbabilityMultiplier
    ) {
        SpecimenData preFight = generatePreFight(species, deterministicSeed, provenance, traitLuck, bodyTypeEventProbabilityMultiplier);
        return finalizeAfterFight(species, preFight, traitLuck, false);
    }

    public SpecimenData generatePreFight(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck
    ) {
        return generatePreFight(species, deterministicSeed, provenance, traitLuck, 1.0);
    }

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
     * Finalizes every canonical specimen axis, then calculates FishScore V2 exactly once from that
     * finalized specimen. Pre-fight specimens deliberately carry no score.
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

        double postFightTraitLuck = perfectCatch ? traitLuck + PERFECT_CATCH_TRAIT_LUCK_BONUS : traitLuck;
        double bodyTypeEventMultiplier = perfectCatch ? BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER : 1.0;

        SpecimenData skillCaptured = withPerfectCatch(preFightSpecimen, perfectCatch);
        SpecimenData.BodyType finalizedBodyType = bodyTypes.generate(
                skillCaptured.deterministicSeed(),
                skillCaptured.basePercentile(),
                species,
                postFightTraitLuck,
                bodyTypeEventMultiplier
        );
        SpecimenData bodyFinalized = bodyTypes.applyPhysicalSize(species, skillCaptured, finalizedBodyType);
        SpecimenData conditionedSpecimen = conditions.apply(species, bodyFinalized, postFightTraitLuck);
        SpecimenData pigmentedSpecimen = pigmentations.apply(species, conditionedSpecimen, postFightTraitLuck);
        SpecimenData finalizedSpecimen = specimenQuality.apply(pigmentedSpecimen, postFightTraitLuck, perfectCatch);
        FishScoreV2Service.Result score = fishScores.calculate(species.rarity(), finalizedSpecimen);
        return withFishScore(finalizedSpecimen, score);
    }

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
                species.speciesId(), SCHEMA_VERSION, GENERATION_VERSION, deterministicSeed,
                percentile, length, length, percentile,
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
                specimen.speciesId(), specimen.schemaVersion(), specimen.generationVersion(), specimen.deterministicSeed(),
                specimen.basePercentile(), specimen.baseLength(), specimen.finalLength(), specimen.finalPercentile(),
                specimen.bodyType(), specimen.condition(), specimen.pigmentation(), specimen.specimenQuality(),
                perfectCatch, specimen.rawFishScore(), specimen.fishScore(), specimen.provenance()
        );
    }

    private static SpecimenData withFishScore(SpecimenData specimen, FishScoreV2Service.Result score) {
        return new SpecimenData(
                specimen.speciesId(), specimen.schemaVersion(), specimen.generationVersion(), specimen.deterministicSeed(),
                specimen.basePercentile(), specimen.baseLength(), specimen.finalLength(), specimen.finalPercentile(),
                specimen.bodyType(), specimen.condition(), specimen.pigmentation(), specimen.specimenQuality(),
                specimen.perfectCatch(), OptionalDouble.of(score.rawScore()), OptionalInt.of(score.fishScore()), specimen.provenance()
        );
    }
}
