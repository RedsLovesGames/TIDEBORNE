package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConditionGeneratorTest {
    private static final int SAMPLE_SIZE = 200_000;
    private static final double EPSILON = 1.0e-12;
    private final ConditionGenerator generator = new ConditionGenerator();
    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final SpecimenGenerator specimens = new SpecimenGenerator();
    private final SpeciesProfile species = profile(CanonicalRarity.ONE_STAR);
    private final SpeciesProfile fiveStarSpecies = profile(CanonicalRarity.FIVE_STAR);

    @Test
    void probabilitiesMatchTheBaseConditionContract() {
        assertEquals(0.05, ConditionGenerator.BASE_EVENT_PROBABILITY);
        assertEquals(0.65, ConditionGenerator.SCARRED_PROBABILITY_GIVEN_EVENT);
        assertEquals(0.35, ConditionGenerator.PARASITE_RIDDEN_PROBABILITY_GIVEN_EVENT);
        assertEquals(
                1.0,
                ConditionGenerator.SCARRED_PROBABILITY_GIVEN_EVENT
                        + ConditionGenerator.PARASITE_RIDDEN_PROBABILITY_GIVEN_EVENT
        );
    }

    @Test
    void sameSeedIsDeterministic() {
        for (long seed = 0; seed < 10_000; seed += 97) {
            assertEquals(
                    generator.generate(seed, species, 0.0),
                    generator.generate(seed, species, 0.0)
            );
        }
    }

    @Test
    void oneStarZeroTraitLuckEventRateIsApproximatelyFivePercent() {
        int events = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed, species, 0.0) != SpecimenData.Condition.NORMAL) {
                events++;
            }
        }

        double eventRate = events / (double) SAMPLE_SIZE;
        assertTrue(eventRate >= 0.047 && eventRate <= 0.053,
                "expected about 5% Condition events, got " + eventRate);
    }

    @Test
    void rarityCompensationIncreasesConditionEventProbability() {
        double oneStar = generator.eventProbability(species, 0.0);
        double fiveStar = generator.eventProbability(fiveStarSpecies, 0.0);

        assertEquals(0.05, oneStar, EPSILON);
        assertEquals(0.12, fiveStar, EPSILON);
        assertTrue(fiveStar > oneStar);
    }

    @Test
    void traitLuckIncreasesConditionEventProbabilityAfterRarityCompensation() {
        double withoutTraitLuck = generator.eventProbability(fiveStarSpecies, 0.0);
        double withTraitLuck = generator.eventProbability(fiveStarSpecies, 10.0);

        assertEquals(0.12, withoutTraitLuck, EPSILON);
        assertEquals(0.2256, withTraitLuck, EPSILON);
        assertTrue(withTraitLuck > withoutTraitLuck);
    }

    @Test
    void triggeredSubtypeSplitIsNotDistortedByRarityAndTraitLuck() {
        int scarred = 0;
        int parasiteRidden = 0;

        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            switch (generator.generate(seed, fiveStarSpecies, 10.0)) {
                case SCARRED -> scarred++;
                case PARASITE_RIDDEN -> parasiteRidden++;
                case NORMAL -> {
                }
            }
        }

        int events = scarred + parasiteRidden;
        double scarredShare = scarred / (double) events;
        double parasiteShare = parasiteRidden / (double) events;
        assertTrue(scarredShare >= 0.63 && scarredShare <= 0.67,
                "expected about 65% Scarred among adjusted Condition events, got " + scarredShare);
        assertTrue(parasiteShare >= 0.33 && parasiteShare <= 0.37,
                "expected about 35% Parasite-Ridden among adjusted Condition events, got " + parasiteShare);
    }

    @Test
    void conditionIsIndependentFromBodyTypeStreams() {
        long seed = 98_765_432_101L;
        SpecimenData.Condition before = generator.generate(seed, species, 0.0);

        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_VARIANT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);

        assertEquals(before, generator.generate(seed, species, 0.0));
    }

    @Test
    void bodyTypeAndConditionCanStack() {
        long seed = 75L;
        SpecimenData.BodyType bodyType = bodyTypes.generate(seed, 50.0, species, 0.0);
        SpecimenData.Condition condition = generator.generate(seed, species, 0.0);

        assertNotEquals(SpecimenData.BodyType.NORMAL, bodyType);
        assertNotEquals(SpecimenData.Condition.NORMAL, condition);
    }

    @Test
    void applyingConditionPreservesBodyTypeAndCannotAccumulateMultipleConditions() {
        SpecimenData base = specimens.generateBase(species, 4L, SpecimenData.Provenance.generated());
        SpecimenData giant = bodyTypes.applyPhysicalSize(species, base, SpecimenData.BodyType.GIANT);
        SpecimenData once = generator.apply(species, giant, 0.0);
        SpecimenData twice = generator.apply(species, once, 0.0);

        assertEquals(SpecimenData.BodyType.GIANT, once.bodyType());
        assertEquals(SpecimenData.BodyType.GIANT, twice.bodyType());
        assertEquals(SpecimenData.Condition.SCARRED, once.condition());
        assertEquals(once.condition(), twice.condition());
        assertTrue(EnumSet.allOf(SpecimenData.Condition.class).contains(twice.condition()));
    }

    @Test
    void fullCanonicalGenerationUsesTheConditionAxisWithoutChangingNaturalIdentity() {
        long seed = 4L;
        SpecimenData base = specimens.generateBase(species, seed, SpecimenData.Provenance.generated());
        SpecimenData complete = specimens.generate(species, seed, SpecimenData.Provenance.generated());

        assertEquals(generator.generate(seed, species, 0.0), complete.condition());
        assertEquals(SpecimenData.Condition.SCARRED, complete.condition());
        assertEquals(base.basePercentile(), complete.basePercentile());
        assertEquals(base.baseLength(), complete.baseLength());
    }

    private static SpeciesProfile profile(CanonicalRarity rarity) {
        return new SpeciesProfile(
                "tide:test_fish",
                rarity,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                new LogNormalSizeDistribution(25.0, 0.4),
                Set.of(),
                Map.of()
        );
    }
}
