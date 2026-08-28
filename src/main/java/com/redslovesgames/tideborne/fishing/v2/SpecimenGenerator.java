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

    /** Generates with zero Trait Luck and no Body Type-specific event multiplier. */
    public SpecimenData generate(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance
    ) {
        return generate(species, deterministicSeed, provenance, 0.0, 1.0);
    }

    /** Generates with canonical Trait Luck and no Body Type-specific event multiplier. */
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
     * trait finalization.
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
     * effect are finalized for the fight. Post-fight axes remain at their neutral values until the
     * Perfect Catch result is known.
     */
    public SpecimenData generatePreFight(
            SpeciesProfile species,
            long deterministicSeed,
            SpecimenData.Provenance provenance,
            double traitLuck
    ) {
        return generatePreFight(species, deterministicSeed, provenance, traitLuck, 1.0);
    }

    /** Pre-fight generation with an explicit Body Type base event multiplier. */
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
     * Finalizes canonical state after Tide has resolved the center-zone skill check.
     *
     * <p>Perfect Catch is copied into canonical specimen state before post-fight trait generation. A
     * Perfect Catch adds {@value #PERFECT_CATCH_TRAIT_LUCK_BONUS} temporary Trait Luck for this catch
     * and multiplies the Body Type base event chance by
     * {@link BodyTypeGenerator#PERFECT_CATCH_EVENT_MULTIPLIER}. Body Type is reevaluated from the same
     * deterministic event and subtype salts, so a non-perfect finalization reproduces the pre-fight
     * Body Type exactly while a Perfect Catch can cross the higher event threshold. Natural percentile
     * and base length are never sampled again. If Body Type changes, final physical size is recomputed
     * from the original base length using the canonical deterministic Body Type size salt.
     *
     * <p>The supplied Trait Luck already contains the server-owned gear/context contribution plus the
     * captured per-species Momentum contribution. No persistent Trait Luck or Momentum state is modified
     * here. The Perfect Catch Body Type event order is base chance, Perfect Catch multiplier, rarity
     * compensation, total Trait Luck, final bound. The Giant/Dwarf subtype roll remains separate.
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

        double postFightTraitLuck = perfectCatch
                ? traitLuck + PERFECT_CATCH_TRAIT_LUCK_BONUS
                : traitLuck;
        double bodyTypeEventMultiplier = perfectCatch
                ? BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER
                : 1.0;

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
        return pigmentations.apply(species, conditionedSpecimen, postFightTraitLuck);
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
