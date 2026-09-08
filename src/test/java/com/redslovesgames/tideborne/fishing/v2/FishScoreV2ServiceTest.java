package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class FishScoreV2ServiceTest {
    private static final double EPSILON = 1.0e-12;
    private final FishScoreV2Service service = new FishScoreV2Service();

    @Test
    void minimumCanonicalIncandescentLarvaCaseMapsToOne() {
        FishScoreV2Service.Result result = service.calculate(
                CanonicalRarity.ONE_STAR,
                specimen(0.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                        SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL)
        );

        assertEquals(50.0, result.rawScore(), EPSILON);
        assertEquals(1, result.fishScore());
    }

    @Test
    void maximumDragonFishP100CompatibleTraitCombinationMapsToThreeThousand() {
        FishScoreV2Service.Result result = service.calculate(
                CanonicalRarity.FIVE_STAR,
                specimen(100.0, SpecimenData.BodyType.GIANT, SpecimenData.Condition.PARASITE_RIDDEN,
                        SpecimenData.Pigmentation.IRIDESCENT, SpecimenData.SpecimenQuality.PERFECT_SPECIMEN)
        );

        assertEquals(925.0, result.rawScore(), EPSILON);
        assertEquals(3000, result.fishScore());
    }

    @Test
    void intermediateValuesFollowTheFrozenLinearNormalizationExactly() {
        assertScore(250.0, 686,
                CanonicalRarity.TWO_STAR, 50.0,
                SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL);

        assertScore(345.0, 1012,
                CanonicalRarity.THREE_STAR, 50.0,
                SpecimenData.BodyType.NORMAL, SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL);

        assertScore(545.0, 1698,
                CanonicalRarity.FOUR_STAR, 75.0,
                SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.ALBINO, SpecimenData.SpecimenQuality.NORMAL);
    }

    @Test
    void everyFrozenSpeciesPointValueIsExact() {
        assertEquals(50, service.speciesPoints(CanonicalRarity.ONE_STAR));
        assertEquals(100, service.speciesPoints(CanonicalRarity.TWO_STAR));
        assertEquals(175, service.speciesPoints(CanonicalRarity.THREE_STAR));
        assertEquals(250, service.speciesPoints(CanonicalRarity.FOUR_STAR));
        assertEquals(350, service.speciesPoints(CanonicalRarity.FIVE_STAR));
    }

    @Test
    void eachTraitAxisUsesOnlyItsCanonicalValue() {
        assertRawDelta(20.0, specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(35.0, specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(40.0, specimen(50.0, SpecimenData.BodyType.GIANT, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(40.0, specimen(50.0, SpecimenData.BodyType.DWARF, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(70.0, specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.ALBINO, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(100.0, specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.IRIDESCENT, SpecimenData.SpecimenQuality.NORMAL));
        assertRawDelta(100.0, specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.PERFECT_SPECIMEN));
    }

    @Test
    void normalizationClampsOutsideFrozenRawBoundaries() {
        assertEquals(1, service.normalize(-1000.0));
        assertEquals(1, service.normalize(49.999));
        assertEquals(3000, service.normalize(925.001));
        assertEquals(3000, service.normalize(10_000.0));
    }

    @Test
    void normalizationRejectsNonFiniteRawValues() {
        assertThrows(IllegalArgumentException.class, () -> service.normalize(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> service.normalize(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> service.normalize(Double.NEGATIVE_INFINITY));
    }

    private void assertScore(
            double expectedRaw,
            int expectedScore,
            CanonicalRarity rarity,
            double finalPercentile,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality quality
    ) {
        FishScoreV2Service.Result result = service.calculate(
                rarity,
                specimen(finalPercentile, bodyType, condition, pigmentation, quality)
        );
        assertEquals(expectedRaw, result.rawScore(), EPSILON);
        assertEquals(expectedScore, result.fishScore());
    }

    private void assertRawDelta(double expectedDelta, SpecimenData traitSpecimen) {
        double baseline = service.calculate(
                CanonicalRarity.ONE_STAR,
                specimen(50.0, SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                        SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL)
        ).rawScore();
        double withTrait = service.calculate(CanonicalRarity.ONE_STAR, traitSpecimen).rawScore();
        assertEquals(expectedDelta, withTrait - baseline, EPSILON);
    }

    private static SpecimenData specimen(
            double finalPercentile,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality quality
    ) {
        return new SpecimenData(
                "tide:fish_score_test",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                123456789L,
                finalPercentile,
                10.0,
                10.0,
                finalPercentile,
                bodyType,
                condition,
                pigmentation,
                quality,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
