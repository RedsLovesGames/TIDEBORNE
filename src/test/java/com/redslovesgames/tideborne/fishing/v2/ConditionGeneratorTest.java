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
    private final ConditionGenerator generator = new ConditionGenerator();
    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
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
            assertEquals(generator.generate(seed), generator.generate(seed));
        }
    }

    @Test
    void conditionEventRateIsApproximatelyFivePercent() {
        int events = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed) != SpecimenData.Condition.NORMAL) {
                events++;
            }
        }

        double eventRate = events / (double) SAMPLE_SIZE;
        assertTrue(eventRate >= 0.047 && eventRate <= 0.053,
                "expected about 5% Condition events, got " + eventRate);
    }

    @Test
    void triggeredSubtypeSplitIsApproximatelySixtyFiveThirtyFive() {
        int scarred = 0;
        int parasiteRidden = 0;

        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            switch (generator.generate(seed)) {
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
                "expected about 65% Scarred among Condition events, got " + scarredShare);
        assertTrue(parasiteShare >= 0.33 && parasiteShare <= 0.37,
                "expected about 35% Parasite-Ridden among Condition events, got " + parasiteShare);
    }

    @Test
    void conditionIsIndependentFromBodyTypeStreams() {
        long seed = 98_765_432_101L;
        SpecimenData.Condition before = generator.generate(seed);

        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_VARIANT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);

        assertEquals(before, generator.generate(seed));
    }

    @Test
    void bodyTypeAndConditionCanStack() {
        long seed = 75L;
        SpecimenData.BodyType bodyType = bodyTypes.generate(seed, 50.0);
        SpecimenData.Condition condition = generator.generate(seed);

        assertNotEquals(SpecimenData.BodyType.NORMAL, bodyType);
        assertNotEquals(SpecimenData.Condition.NORMAL, condition);
    }

    @Test
    void applyingConditionPreservesBodyTypeAndCannotAccumulateMultipleConditions() {
        SpecimenData base = specimens.generateBase(species, 4L, SpecimenData.Provenance.generated());
        SpecimenData giant = bodyTypes.applyPhysicalSize(species, base, SpecimenData.BodyType.GIANT);
        SpecimenData once = generator.apply(giant);
        SpecimenData twice = generator.apply(once);

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

        assertEquals(generator.generate(seed), complete.condition());
        assertEquals(SpecimenData.Condition.SCARRED, complete.condition());
        assertEquals(base.basePercentile(), complete.basePercentile());
        assertEquals(base.baseLength(), complete.baseLength());
    }
}
