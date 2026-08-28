package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SpecimenFishScorePersistenceTest {
    @Test
    void scoreIsAbsentBeforeFinalizationAndCanonicalAfterEveryAxisIsFinal() {
        SpeciesProfile species = new SpeciesProfile(
                "tide:score_persistence_test",
                CanonicalRarity.FIVE_STAR,
                1.0,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                new LogNormalSizeDistribution(25.0, 0.4),
                Set.of(),
                Map.of()
        );
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData preFight = generator.generatePreFight(
                species,
                4_221_337L,
                SpecimenData.Provenance.generated(),
                12.0
        );

        assertFalse(preFight.rawFishScore().isPresent());
        assertFalse(preFight.fishScore().isPresent());

        SpecimenData finalized = generator.finalizeAfterFight(species, preFight, 12.0, true);
        FishScoreV2Service.Result expected = new FishScoreV2Service().calculate(species.rarity(), finalized);

        assertTrue(finalized.rawFishScore().isPresent());
        assertTrue(finalized.fishScore().isPresent());
        assertEquals(expected.rawScore(), finalized.rawFishScore().getAsDouble(), 1.0E-9);
        assertEquals(expected.fishScore(), finalized.fishScore().getAsInt());
        assertTrue(finalized.fishScore().getAsInt() >= FishScoreV2Service.SCORE_MIN);
        assertTrue(finalized.fishScore().getAsInt() <= FishScoreV2Service.SCORE_MAX);
    }

    @Test
    void repeatedFinalizationProducesTheSameStoredScore() {
        SpeciesProfile species = new SpeciesProfile(
                "tide:score_round_trip_test",
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                NoPhysicalSizeDistribution.INSTANCE,
                Set.of(),
                Map.of()
        );
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData preFight = generator.generatePreFight(
                species,
                991_337L,
                SpecimenData.Provenance.generated(),
                5.0
        );

        SpecimenData first = generator.finalizeAfterFight(species, preFight, 5.0, false);
        SpecimenData second = generator.finalizeAfterFight(species, preFight, 5.0, false);

        assertEquals(first.rawFishScore(), second.rawFishScore());
        assertEquals(first.fishScore(), second.fishScore());
        assertEquals(first, second);
    }
}
