package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BodyTypeGeneratorTest {
    private static final int SAMPLE_SIZE = 200_000;
    private static final int PIPELINE_SAMPLE_SIZE = 150_000;
    private final BodyTypeGenerator generator = new BodyTypeGenerator();
    private final TraitProbabilityService traitProbabilities = new TraitProbabilityService();
    private final SpecimenGenerator specimenGenerator = new SpecimenGenerator();
    private final SpeciesProfile species = species(CanonicalRarity.THREE_STAR);

    @Test
    void baseEventProbabilityIsExactlyFivePercent() {
        assertEquals(0.05, BodyTypeGenerator.BASE_EVENT_PROBABILITY);
    }

    @Test
    void sameSeedAndInputsAreDeterministic() {
        for (long seed = 0; seed < 10_000; seed += 97) {
            assertEquals(
                    generator.generate(seed, 37.5, species, 12.0),
                    generator.generate(seed, 37.5, species, 12.0)
            );
        }
    }

    @Test
    void oneStarZeroTraitLuckEventRateIsApproximatelyFivePercent() {
        double eventRate = eventRate(species(CanonicalRarity.ONE_STAR), 0.0, SAMPLE_SIZE);
        assertTrue(eventRate >= 0.047 && eventRate <= 0.053,
                "expected about 5% Body Type events, got " + eventRate);
    }

    @Test
    void eventRatesFollowSharedRarityAndTraitLuckPipeline() {
        ProbabilityCase[] cases = {
                new ProbabilityCase(CanonicalRarity.ONE_STAR, -5.0),
                new ProbabilityCase(CanonicalRarity.ONE_STAR, 10.0),
                new ProbabilityCase(CanonicalRarity.THREE_STAR, 0.0),
                new ProbabilityCase(CanonicalRarity.FOUR_STAR, 20.0),
                new ProbabilityCase(CanonicalRarity.FIVE_STAR, 10.0)
        };

        for (ProbabilityCase probabilityCase : cases) {
            SpeciesProfile caseSpecies = species(probabilityCase.rarity());
            double expected = traitProbabilities.calculate(
                    BodyTypeGenerator.BASE_EVENT_PROBABILITY,
                    caseSpecies,
                    probabilityCase.traitLuck()
            );
            double actual = eventRate(caseSpecies, probabilityCase.traitLuck(), PIPELINE_SAMPLE_SIZE);
            double standardError = Math.sqrt(expected * (1.0 - expected) / PIPELINE_SAMPLE_SIZE);
            double tolerance = Math.max(0.0025, 5.0 * standardError);

            assertEquals(expected, actual, tolerance,
                    "Body Type event rate must follow shared probability pipeline for "
                            + probabilityCase.rarity() + " at Trait Luck " + probabilityCase.traitLuck());
        }
    }

    @Test
    void eventProbabilityMatchesSharedPipelineExactly() {
        for (CanonicalRarity rarity : CanonicalRarity.values()) {
            SpeciesProfile caseSpecies = species(rarity);
            for (double traitLuck : new double[] {-5.0, 0.0, 10.0, 35.0}) {
                assertEquals(
                        traitProbabilities.calculate(
                                BodyTypeGenerator.BASE_EVENT_PROBABILITY,
                                caseSpecies,
                                traitLuck
                        ),
                        generator.eventProbability(caseSpecies, traitLuck, 1.0),
                        1.0e-12
                );
            }
        }
    }

    @Test
    void bodyTypeMultiplierRunsBeforeRarityAndTraitLuckAndIsBounded() {
        SpeciesProfile fiveStar = species(CanonicalRarity.FIVE_STAR);
        double expected = traitProbabilities.calculate(
                BodyTypeGenerator.BASE_EVENT_PROBABILITY,
                fiveStar,
                10.0,
                1.25
        );

        assertEquals(0.2775, expected, 1.0e-12);
        assertEquals(expected, generator.eventProbability(fiveStar, 10.0, 1.25), 1.0e-12);
        assertEquals(1.0, generator.eventProbability(fiveStar, 100.0, 10.0));
        assertEquals(0.0, generator.eventProbability(fiveStar, 10.0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> generator.eventProbability(fiveStar, 0.0, -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> generator.eventProbability(fiveStar, 0.0, Double.NaN));
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
        SpecimenData.BodyType before = generator.generate(seed, 63.0, species, 18.0);

        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_VARIANT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.PIGMENTATION_EVENT);
        TraitRandom.unitDouble(seed, TraitRandom.Salts.PIGMENTATION_VARIANT);

        assertEquals(before, generator.generate(seed, 63.0, species, 18.0));
    }

    @Test
    void giantProbabilityUsesDocumentedSmoothBias() {
        assertEquals(0.25, BodyTypeGenerator.giantProbability(0.0));
        assertEquals(0.375, BodyTypeGenerator.giantProbability(25.0));
        assertEquals(0.50, BodyTypeGenerator.giantProbability(50.0));
        assertEquals(0.625, BodyTypeGenerator.giantProbability(75.0));
        assertEquals(0.75, BodyTypeGenerator.giantProbability(100.0));
    }

    @Test
    void bodyTypeSizeMultiplierIsDeterministic() {
        long seed = 2_468_013_579L;
        assertEquals(
                generator.sizeMultiplier(seed, SpecimenData.BodyType.GIANT),
                generator.sizeMultiplier(seed, SpecimenData.BodyType.GIANT)
        );
        assertEquals(
                generator.sizeMultiplier(seed, SpecimenData.BodyType.DWARF),
                generator.sizeMultiplier(seed, SpecimenData.BodyType.DWARF)
        );
    }

    @Test
    void bodyTypeSizeMultipliersStayInsideCanonicalBounds() {
        for (long seed = 0; seed < 50_000; seed++) {
            double giant = generator.sizeMultiplier(seed, SpecimenData.BodyType.GIANT);
            double dwarf = generator.sizeMultiplier(seed, SpecimenData.BodyType.DWARF);

            assertTrue(giant >= BodyTypeGenerator.GIANT_MIN_SIZE_MULTIPLIER);
            assertTrue(giant <= BodyTypeGenerator.GIANT_MAX_SIZE_MULTIPLIER);
            assertTrue(dwarf >= BodyTypeGenerator.DWARF_MIN_SIZE_MULTIPLIER);
            assertTrue(dwarf <= BodyTypeGenerator.DWARF_MAX_SIZE_MULTIPLIER);
        }
    }

    @Test
    void normalMultiplierAndPhysicalSizeAreExactlyUnchanged() {
        SpecimenData base = specimenGenerator.generateBase(species, 13579L, SpecimenData.Provenance.generated());
        SpecimenData normal = generator.applyPhysicalSize(species, base, SpecimenData.BodyType.NORMAL);

        assertEquals(1.0, generator.sizeMultiplier(base.deterministicSeed(), SpecimenData.BodyType.NORMAL));
        assertEquals(base.baseLength(), normal.finalLength());
        assertEquals(base.basePercentile(), normal.finalPercentile());
        assertEquals(base.basePercentile(), normal.basePercentile());
        assertEquals(SpecimenData.BodyType.NORMAL, normal.bodyType());
    }

    @Test
    void giantIsAlwaysPhysicallyLargerThanBase() {
        for (long seed = 0; seed < 10_000; seed += 101) {
            SpecimenData base = specimenGenerator.generateBase(species, seed, SpecimenData.Provenance.generated());
            SpecimenData giant = generator.applyPhysicalSize(species, base, SpecimenData.BodyType.GIANT);
            assertTrue(giant.finalLength() > base.baseLength());
        }
    }

    @Test
    void dwarfIsAlwaysPhysicallySmallerThanBase() {
        for (long seed = 0; seed < 10_000; seed += 101) {
            SpecimenData base = specimenGenerator.generateBase(species, seed, SpecimenData.Provenance.generated());
            SpecimenData dwarf = generator.applyPhysicalSize(species, base, SpecimenData.BodyType.DWARF);
            assertTrue(dwarf.finalLength() < base.baseLength());
        }
    }

    @Test
    void physicalSizeAdjustmentPreservesNaturalPercentile() {
        SpecimenData base = specimenGenerator.generateBase(species, 777123L, SpecimenData.Provenance.generated());
        SpecimenData giant = generator.applyPhysicalSize(species, base, SpecimenData.BodyType.GIANT);
        SpecimenData dwarf = generator.applyPhysicalSize(species, base, SpecimenData.BodyType.DWARF);

        assertEquals(base.basePercentile(), giant.basePercentile());
        assertEquals(base.basePercentile(), dwarf.basePercentile());
        assertEquals(
                species.sizeDistribution().percentile(giant.finalLength()),
                giant.finalPercentile(),
                1.0e-10
        );
        assertEquals(
                species.sizeDistribution().percentile(dwarf.finalLength()),
                dwarf.finalPercentile(),
                1.0e-10
        );
    }

    @Test
    void noPhysicalSizeSpeciesRetainNaturalPercentile() {
        SpeciesProfile sizeless = new SpeciesProfile(
                "tide:sizeless",
                CanonicalRarity.ONE_STAR,
                1.0,
                SpeciesEligibility.always(),
                0.5,
                0.5,
                "steady",
                NoPhysicalSizeDistribution.INSTANCE,
                Set.of(),
                Map.of()
        );
        SpecimenData base = specimenGenerator.generateBase(sizeless, 9988L, SpecimenData.Provenance.generated());
        SpecimenData giant = generator.applyPhysicalSize(sizeless, base, SpecimenData.BodyType.GIANT);

        assertEquals(0.0, giant.finalLength());
        assertEquals(base.basePercentile(), giant.finalPercentile());
        assertEquals(base.basePercentile(), giant.basePercentile());
    }

    private double eventRate(SpeciesProfile caseSpecies, double traitLuck, int sampleSize) {
        int events = 0;
        for (long seed = 0; seed < sampleSize; seed++) {
            if (generator.generate(seed, 50.0, caseSpecies, traitLuck) != SpecimenData.BodyType.NORMAL) {
                events++;
            }
        }
        return events / (double) sampleSize;
    }

    private int count(SpecimenData.BodyType bodyType, double percentile) {
        int matches = 0;
        for (long seed = 0; seed < SAMPLE_SIZE; seed++) {
            if (generator.generate(seed, percentile, species, 0.0) == bodyType) {
                matches++;
            }
        }
        return matches;
    }

    private static SpeciesProfile species(CanonicalRarity rarity) {
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

    private record ProbabilityCase(CanonicalRarity rarity, double traitLuck) {
    }
}
