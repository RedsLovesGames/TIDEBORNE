package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PerfectCatchBodyTypeTest {
    private static final double EPSILON = 1.0e-12;
    private final BodyTypeGenerator bodyTypes = new BodyTypeGenerator();
    private final SpecimenGenerator specimens = new SpecimenGenerator();
    private final SpeciesProfile threeStar = species(CanonicalRarity.THREE_STAR);

    @Test
    void perfectCatchBodyTypeMultiplierIsExactlyOnePointTwoFive() {
        assertEquals(1.25, BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER);
    }

    @Test
    void perfectCatchMultiplierRunsBeforeRarityAndTotalTraitLuck() {
        SpeciesProfile fiveStar = species(CanonicalRarity.FIVE_STAR);

        // Base 0.05 * Perfect Catch 1.25 * rarity 2.40 = 0.15.
        // T=10 then gives 1 - (1 - 0.15)^2 = 0.2775.
        assertEquals(
                0.2775,
                bodyTypes.eventProbability(
                        fiveStar,
                        10.0,
                        BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER
                ),
                EPSILON
        );
    }

    @Test
    void requiredOrderDiffersFromOldPostPipelineMultiplication() {
        SpeciesProfile fiveStar = species(CanonicalRarity.FIVE_STAR);
        TraitProbabilityService probabilities = new TraitProbabilityService();
        double oldPostPipeline = probabilities.calculate(
                BodyTypeGenerator.BASE_EVENT_PROBABILITY,
                fiveStar,
                10.0
        ) * BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER;
        double canonical = bodyTypes.eventProbability(
                fiveStar,
                10.0,
                BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER
        );

        assertEquals(0.282, oldPostPipeline, EPSILON);
        assertEquals(0.2775, canonical, EPSILON);
        assertNotEquals(oldPostPipeline, canonical);
    }

    @Test
    void perfectCatchCanPromoteBodyTypeUsingSameDeterministicEventRoll() {
        long seed = 21L;
        SpecimenData preFight = specimens.generatePreFight(
                threeStar,
                seed,
                SpecimenData.Provenance.generated(),
                0.0
        );
        double eventRoll = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        double ordinaryChance = bodyTypes.eventProbability(threeStar, 0.0, 1.0);
        double perfectChance = bodyTypes.eventProbability(
                threeStar,
                SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS,
                BodyTypeGenerator.PERFECT_CATCH_EVENT_MULTIPLIER
        );

        assertEquals(0.07, ordinaryChance, EPSILON);
        assertEquals(0.16734375, perfectChance, EPSILON);
        assertTrue(eventRoll >= ordinaryChance && eventRoll < perfectChance);
        assertEquals(SpecimenData.BodyType.NORMAL, preFight.bodyType());

        SpecimenData finalized = specimens.finalizeAfterFight(threeStar, preFight, 0.0, true);

        assertNotEquals(SpecimenData.BodyType.NORMAL, finalized.bodyType());
        assertEquals(preFight.deterministicSeed(), finalized.deterministicSeed());
        assertEquals(preFight.basePercentile(), finalized.basePercentile());
        assertEquals(preFight.baseLength(), finalized.baseLength());
        assertTrue(finalized.perfectCatch());
    }

    @Test
    void perfectCatchDoesNotAlterGiantVersusDwarfBiasFunction() {
        assertEquals(0.25, BodyTypeGenerator.giantProbability(0.0), EPSILON);
        assertEquals(0.50, BodyTypeGenerator.giantProbability(50.0), EPSILON);
        assertEquals(0.75, BodyTypeGenerator.giantProbability(100.0), EPSILON);
    }

    private static SpeciesProfile species(CanonicalRarity rarity) {
        return new SpeciesProfile(
                "tide:perfect_body_" + rarity.stars(),
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
