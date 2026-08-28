package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PigmentationGeneratorTest {
    private static final int SAMPLE_SIZE = 200_000;
    private final PigmentationGenerator generator = new PigmentationGenerator();
    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final ConditionGenerator conditions = new ConditionGenerator();
    private final SpecimenGenerator specimens = new SpecimenGenerator();
    private final SpeciesProfile species = new SpeciesProfile(
            "tide:test_fish",
            CanonicalRarity.THREE_STAR,
            1.0,
            SpeciesEligibility.always(),
            0.8,
            1.0,
            "steady",
            new LogNormalSizeDistribution(25.0, 0.4),
            Set.of(),
            Map.of()
    );

    @Test
    void probabilitiesMatchTheBasePigmentationContract() {
        assertEquals(0.015, PigmentationGenerator.BASE_EVENT_PROBABILITY);
        assertEquals(0.70, PigmentationGenerator.ALBINO_PROBABILITY_GIVEN_EVENT);
        assertEquals(0.30, PigmentationGenerator.IRIDESCENT_PROBABILITY_GIVEN_EVENT);
        assertEquals(
                1.0,
                PigmentationGenerator.ALBINO_PROBABILITY_GIVEN_EVENT
                        + PigmentationGenerator.IRIDESCENT_PROBABILITY_GIVEN_EVENT
        );
    }

    @Test
    void sameSeedIsDeterministic() {
        for (long seed = 0; seed < 10_000; seed += 97) {
            assertEquals(generator.generate(seed), generator.generate(seed));
        }
    }

    @Test
    void pigmentationEventRateIsApproximatelyOnePointFivePercent() {
        int events = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed) != SpecimenData.Pigmentation.NORMAL) {
                events++;
            }
        }

        double eventRate = events / (double) SAMPLE_SIZE;
        assertTrue(eventRate >= 0.014 && eventRate <= 0.016,
                "expected about 1.5% Pigmentation events, got " + eventRate);
    }

    @Test
    void triggeredSubtypeSplitIsApproximatelySeventyThirty() {
        int albino = 0;
        int iridescent = 0;

        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            switch (generator.generate(seed)) {
                case ALBINO -> albino++;
                case IRIDESCENT -> iridescent++;
                case NORMAL -> {
                }
            }
        }

        int events = albino + iridescent;
        double albinoShare = albino / (double) events;
        double iridescentShare = iridescent / (double) events;
        assertTrue(albinoShare >= 0.67 && albinoShare <= 0.73,
                "expected about 70% Albino among Pigmentation events, got " + albinoShare);
        assertTrue(iridescentShare >= 0.27 && iridescentShare <= 0.33,
                "expected about 30% Iridescent among Pigmentation events, got " + iridescentShare);
    }

    @Test
    void pigmentationIsIndependentFromBodyTypeAndConditionStreams() {
        long seed = 98_765_432_101L;
        SpecimenData.Pigmentation before = generator.generate(seed);

        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_VARIANT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_VARIANT);

        assertEquals(before, generator.generate(seed));
    }

    @Test
    void giantParasiteRiddenAndIridescentCanStack() {
        long seed = 29_894L;
        SpecimenData specimen = specimens.generate(species, seed, SpecimenData.Provenance.generated());

        assertEquals(SpecimenData.BodyType.GIANT, specimen.bodyType());
        assertEquals(SpecimenData.Condition.PARASITE_RIDDEN, specimen.condition());
        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, specimen.pigmentation());
        assertEquals(bodyTypes.generate(seed, specimen.basePercentile(), species, 0.0), specimen.bodyType());
        assertEquals(conditions.generate(seed), specimen.condition());
        assertEquals(generator.generate(seed), specimen.pigmentation());
    }

    @Test
    void applyingPigmentationPreservesExistingAxesAndIsIdempotent() {
        SpecimenData base = specimens.generateBase(species, 56L, SpecimenData.Provenance.generated());
        SpecimenData giant = bodyTypes.applyPhysicalSize(species, base, SpecimenData.BodyType.GIANT);
        SpecimenData conditioned = new SpecimenData(
                giant.speciesId(),
                giant.schemaVersion(),
                giant.generationVersion(),
                giant.deterministicSeed(),
                giant.basePercentile(),
                giant.baseLength(),
                giant.finalLength(),
                giant.finalPercentile(),
                giant.bodyType(),
                SpecimenData.Condition.PARASITE_RIDDEN,
                giant.pigmentation(),
                giant.specimenQuality(),
                giant.perfectCatch(),
                giant.rawFishScore(),
                giant.fishScore(),
                giant.provenance()
        );

        SpecimenData once = generator.apply(conditioned);
        SpecimenData twice = generator.apply(once);

        assertEquals(SpecimenData.BodyType.GIANT, once.bodyType());
        assertEquals(SpecimenData.Condition.PARASITE_RIDDEN, once.condition());
        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, once.pigmentation());
        assertEquals(once.pigmentation(), twice.pigmentation());
        assertEquals(once.basePercentile(), twice.basePercentile());
        assertEquals(once.baseLength(), twice.baseLength());
        assertEquals(once.finalLength(), twice.finalLength());
    }

    @Test
    void fullCanonicalGenerationUsesPigmentationWithoutChangingNaturalIdentity() {
        long seed = 56L;
        SpecimenData base = specimens.generateBase(species, seed, SpecimenData.Provenance.generated());
        SpecimenData complete = specimens.generate(species, seed, SpecimenData.Provenance.generated());

        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, complete.pigmentation());
        assertEquals(generator.generate(seed), complete.pigmentation());
        assertEquals(base.basePercentile(), complete.basePercentile());
        assertEquals(base.baseLength(), complete.baseLength());
    }
}
