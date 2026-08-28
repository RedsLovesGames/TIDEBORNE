package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BodyTypeGeneratorTest {
    private static final int SAMPLE_SIZE = 200_000;
    private final BodyTypeGenerator generator = new BodyTypeGenerator();

    @Test
    void baseEventProbabilityIsExactlyFivePercent() {
        assertEquals(0.05, BodyTypeGenerator.BASE_EVENT_PROBABILITY);
    }

    @Test
    void sameSeedAndPercentileAreDeterministic() {
        for (long seed = 0; seed < 10_000; seed += 97) {
            assertEquals(generator.generate(seed, 37.5), generator.generate(seed, 37.5));
        }
    }

    @Test
    void bodyTypeEventRateIsApproximatelyFivePercent() {
        int events = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed, 50.0) != SpecimenData.BodyType.NORMAL) {
                events++;
            }
        }

        double eventRate = events / (double) SAMPLE_SIZE;
        assertTrue(eventRate >= 0.047 && eventRate <= 0.053,
                "expected about 5% Body Type events, got " + eventRate);
    }

    @Test
    void p75ProducesMoreGiantsThanP25() {
        int p25Giants = count(SpecimenData.BodyType.GIANT, 25.0);
        int p75Giants = count(SpecimenData.BodyType.GIANT, 75.0);

        assertTrue(p75Giants > p25Giants,
                "P75 should produce more Giants than P25: " + p75Giants + " <= " + p25Giants);
    }

    @Test
    void giantAndDwarfRemainPossibleAcrossPercentileRange() {
        double[] percentiles = {0.0, 25.0, 50.0, 75.0, 100.0};

        for (double percentile : percentiles) {
            int giants = count(SpecimenData.BodyType.GIANT, percentile);
            int dwarfs = count(SpecimenData.BodyType.DWARF, percentile);
            assertTrue(giants > 0, "Giant must remain possible at P" + percentile);
            assertTrue(dwarfs > 0, "Dwarf must remain possible at P" + percentile);
        }
    }

    @Test
    void p50IsRoughlyBalancedBetweenGiantAndDwarf() {
        int giants = count(SpecimenData.BodyType.GIANT, 50.0);
        int dwarfs = count(SpecimenData.BodyType.DWARF, 50.0);
        int events = giants + dwarfs;
        double giantShare = giants / (double) events;

        assertTrue(giantShare >= 0.47 && giantShare <= 0.53,
                "expected roughly balanced P50 variants, Giant share was " + giantShare);
    }

    @Test
    void bodyTypeDoesNotDependOnOtherTraitStreams() {
        long seed = 98_765_432_101L;
        SpecimenData.BodyType before = generator.generate(seed, 63.0);

        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_VARIANT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.PIGMENTATION_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.PIGMENTATION_VARIANT);

        assertEquals(before, generator.generate(seed, 63.0));
    }

    @Test
    void giantProbabilityUsesDocumentedSmoothBias() {
        assertEquals(0.25, BodyTypeGenerator.giantProbability(0.0));
        assertEquals(0.375, BodyTypeGenerator.giantProbability(25.0));
        assertEquals(0.50, BodyTypeGenerator.giantProbability(50.0));
        assertEquals(0.625, BodyTypeGenerator.giantProbability(75.0));
        assertEquals(0.75, BodyTypeGenerator.giantProbability(100.0));
    }

    private int count(SpecimenData.BodyType bodyType, double percentile) {
        int matches = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed, percentile) == bodyType) {
                matches++;
            }
        }
        return matches;
    }
}
