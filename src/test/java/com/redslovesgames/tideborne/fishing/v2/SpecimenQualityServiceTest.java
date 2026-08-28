package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class SpecimenQualityServiceTest {
    private static final double EPSILON = 1.0e-12;
    private final SpecimenQualityService service = new SpecimenQualityService();

    @Test
    void frozenAnchorsAreExact() {
        assertEquals(0.0, service.baseProbability(94.999999), EPSILON);
        assertEquals(0.02, service.baseProbability(95.0), EPSILON);
        assertEquals(0.08, service.baseProbability(97.5), EPSILON);
        assertEquals(0.25, service.baseProbability(99.0), EPSILON);
        assertEquals(0.60, service.baseProbability(99.9), EPSILON);
        assertEquals(0.60, service.baseProbability(100.0), EPSILON);
    }

    @Test
    void noProbabilityExistsBelowP95() {
        assertEquals(0.0, service.baseProbability(0.0), EPSILON);
        assertEquals(0.0, service.baseProbability(50.0), EPSILON);
        assertEquals(0.0, service.baseProbability(94.0), EPSILON);
        assertEquals(0.0, service.baseProbability(Math.nextDown(95.0)), EPSILON);
    }

    @Test
    void belowP95RemainsImpossibleAfterTraitLuckAndPerfectCatch() {
        assertEquals(0.0, service.probability(94.999, 250.0, false), EPSILON);
        assertEquals(0.0, service.probability(94.999, 250.0, true), EPSILON);

        for (long seed = 0; seed < 10_000; seed++) {
            assertEquals(
                    SpecimenData.SpecimenQuality.NORMAL,
                    service.generate(seed, 94.999, 250.0, true)
            );
        }
    }

    @Test
    void firstSegmentInterpolatesLinearly() {
        assertEquals(0.05, service.baseProbability(96.25), EPSILON);
        assertEquals(0.032, service.baseProbability(95.5), EPSILON);
        assertEquals(0.068, service.baseProbability(97.0), EPSILON);
    }

    @Test
    void secondSegmentInterpolatesLinearly() {
        assertEquals(0.165, service.baseProbability(98.25), EPSILON);
        assertEquals(0.13666666666666666, service.baseProbability(98.0), EPSILON);
        assertEquals(0.22166666666666668, service.baseProbability(98.75), EPSILON);
    }

    @Test
    void thirdSegmentInterpolatesLinearly() {
        assertEquals(0.425, service.baseProbability(99.45), EPSILON);
        assertEquals(0.3277777777777778, service.baseProbability(99.2), EPSILON);
        assertEquals(0.5416666666666666, service.baseProbability(99.75), EPSILON);
    }

    @Test
    void probabilityIsCappedAtSixtyPercentFromP99Point9UpwardBeforeModifiers() {
        assertEquals(0.60, service.baseProbability(99.9), EPSILON);
        assertEquals(0.60, service.baseProbability(99.95), EPSILON);
        assertEquals(0.60, service.baseProbability(100.0), EPSILON);
    }

    @Test
    void traitLuckUsesTheCanonicalProbabilityTransformAtEveryAnchor() {
        TraitLuckProbabilityService luck = new TraitLuckProbabilityService();
        double traitLuck = 10.0;

        assertEquals(
                luck.adjustProbability(0.02, traitLuck),
                service.probability(95.0, traitLuck, false),
                EPSILON
        );
        assertEquals(
                luck.adjustProbability(0.08, traitLuck),
                service.probability(97.5, traitLuck, false),
                EPSILON
        );
        assertEquals(
                luck.adjustProbability(0.25, traitLuck),
                service.probability(99.0, traitLuck, false),
                EPSILON
        );
        assertEquals(
                luck.adjustProbability(0.60, traitLuck),
                service.probability(99.9, traitLuck, false),
                EPSILON
        );
    }

    @Test
    void perfectCatchAppliesASecondMissChanceAfterTraitLuck() {
        assertEquals(0.25, service.probability(99.0, 0.0, false), EPSILON);
        assertEquals(0.4375, service.probability(99.0, 0.0, true), EPSILON);

        double withPerfectCatchTraitLuck = service.probability(99.9, 10.0, true);
        assertEquals(0.9744, withPerfectCatchTraitLuck, EPSILON);
        assertTrue(withPerfectCatchTraitLuck > service.probability(99.9, 10.0, false));
        assertTrue(withPerfectCatchTraitLuck < 1.0);
    }

    @Test
    void perfectCatchCanChangeTheOutcomeWithoutChangingTheDeterministicRoll() {
        double normalProbability = service.probability(99.0, 0.0, false);
        double perfectProbability = service.probability(99.0, 0.0, true);
        long seed = findSeedBetween(normalProbability, perfectProbability);

        assertEquals(
                SpecimenData.SpecimenQuality.NORMAL,
                service.generate(seed, 99.0, 0.0, false)
        );
        assertEquals(
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                service.generate(seed, 99.0, 0.0, true)
        );
    }

    @Test
    void invalidPercentilesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.baseProbability(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> service.baseProbability(-0.001));
        assertThrows(IllegalArgumentException.class, () -> service.baseProbability(100.001));
    }

    @Test
    void deterministicSelectionUsesDedicatedPerfectSpecimenSalt() {
        long seed = 123456789L;
        double percentile = 99.45;
        double roll = TraitRandom.unitDouble(seed, TraitRandom.Salts.PERFECT_SPECIMEN);
        SpecimenData.SpecimenQuality expected = roll < service.baseProbability(percentile)
                ? SpecimenData.SpecimenQuality.PERFECT_SPECIMEN
                : SpecimenData.SpecimenQuality.NORMAL;

        assertEquals(expected, service.generate(seed, percentile));
        assertEquals(expected, service.generate(seed, percentile));
    }

    @Test
    void finalPercentileIsTheOnlyPercentileUsedForQuality() {
        long seed = findSeedBelow(0.60);
        SpecimenData highBaseLowFinal = specimen(seed, 99.99, 94.99, 40.0, 20.0);
        SpecimenData lowBaseHighFinal = specimen(seed, 5.0, 99.9, 10.0, 40.0);

        assertEquals(SpecimenData.SpecimenQuality.NORMAL, service.apply(highBaseLowFinal).specimenQuality());
        assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, service.apply(lowBaseHighFinal).specimenQuality());
    }

    @Test
    void perfectSpecimenStacksWithOtherAxesWithoutRewritingCanonicalSize() {
        double totalPerfectCatchTraitLuck = 10.0;
        double chance = service.probability(99.9, totalPerfectCatchTraitLuck, true);
        long seed = findSeedBelow(chance);
        SpecimenData specimen = specimen(seed, 42.0, 99.9, 18.5, 29.75);

        SpecimenData result = service.apply(specimen, totalPerfectCatchTraitLuck, true);

        assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, result.specimenQuality());
        assertEquals(SpecimenData.BodyType.GIANT, result.bodyType());
        assertEquals(SpecimenData.Condition.SCARRED, result.condition());
        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, result.pigmentation());
        assertTrue(result.perfectCatch());
        assertEquals(specimen.basePercentile(), result.basePercentile(), EPSILON);
        assertEquals(specimen.finalPercentile(), result.finalPercentile(), EPSILON);
        assertEquals(specimen.baseLength(), result.baseLength(), EPSILON);
        assertEquals(specimen.finalLength(), result.finalLength(), EPSILON);
    }

    @Test
    void applyingQualityNeverRewritesIdentityOrOtherAxes() {
        long seed = findSeedBelow(0.60);
        SpecimenData specimen = specimen(seed, 42.0, 99.9, 18.5, 29.75);

        SpecimenData result = service.apply(specimen);

        assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, result.specimenQuality());
        assertEquals(specimen.speciesId(), result.speciesId());
        assertEquals(specimen.deterministicSeed(), result.deterministicSeed());
        assertEquals(specimen.basePercentile(), result.basePercentile(), EPSILON);
        assertEquals(specimen.finalPercentile(), result.finalPercentile(), EPSILON);
        assertEquals(specimen.baseLength(), result.baseLength(), EPSILON);
        assertEquals(specimen.finalLength(), result.finalLength(), EPSILON);
        assertEquals(specimen.bodyType(), result.bodyType());
        assertEquals(specimen.condition(), result.condition());
        assertEquals(specimen.pigmentation(), result.pigmentation());
        assertEquals(specimen.perfectCatch(), result.perfectCatch());
    }

    @Test
    void directPerfectCatchBonusNeverGuaranteesPerfectSpecimen() {
        double chance = service.probability(99.9, 10.0, true);
        assertTrue(chance > service.probability(99.9, 10.0, false));
        assertTrue(chance < 1.0);
    }

    private static long findSeedBelow(double threshold) {
        for (long seed = 0; seed < 1_000_000; seed++) {
            if (TraitRandom.unitDouble(seed, TraitRandom.Salts.PERFECT_SPECIMEN) < threshold) {
                return seed;
            }
        }
        throw new AssertionError("expected to find deterministic seed below threshold");
    }

    private static long findSeedBetween(double lowerInclusive, double upperExclusive) {
        for (long seed = 0; seed < 1_000_000; seed++) {
            double roll = TraitRandom.unitDouble(seed, TraitRandom.Salts.PERFECT_SPECIMEN);
            if (roll >= lowerInclusive && roll < upperExclusive) {
                return seed;
            }
        }
        throw new AssertionError("expected to find deterministic seed inside probability interval");
    }

    private static SpecimenData specimen(
            long seed,
            double basePercentile,
            double finalPercentile,
            double baseLength,
            double finalLength
    ) {
        return new SpecimenData(
                "tide:quality_test",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                basePercentile,
                baseLength,
                finalLength,
                finalPercentile,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.NORMAL,
                true,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
