package com.redslovesgames.tideborne.fishing.v2.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DeterministicSpecimenDebugTest {
    private static final SpeciesProfile SPECIES = new SpeciesProfile(
            "tide:debug_fish",
            CanonicalRarity.THREE_STAR,
            12.5,
            SpeciesEligibility.always(),
            0.72,
            1.14,
            "darting",
            new LogNormalSizeDistribution(42.0, 0.24),
            Set.of(),
            Map.of()
    );

    private final DeterministicSpecimenDebug debug = new DeterministicSpecimenDebug();

    @Test
    void normalReproductionMatchesCanonicalProductionGeneratorExactly() {
        long seed = 918273645L;
        double traitLuck = 6.5;
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData expectedPreFight = generator.generatePreFight(
                SPECIES,
                seed,
                SpecimenData.Provenance.generated(),
                traitLuck
        );
        SpecimenData expected = generator.finalizeAfterFight(
                SPECIES,
                expectedPreFight,
                traitLuck,
                true
        );

        DeterministicSpecimenDebug.Reproduction actual = debug.reproduce(
                SPECIES,
                seed,
                15.0,
                traitLuck,
                true
        );

        assertEquals(expected, actual.specimen());
        assertEquals(12.5, actual.baseSpeciesWeight(), 0.0);
        assertTrue(actual.adjustedSpeciesWeight() > actual.baseSpeciesWeight());
    }

    @Test
    void fixedSpeciesFishingLuckChangesOnlyReportedSelectionWeight() {
        DeterministicSpecimenDebug.Reproduction neutral = debug.reproduce(
                SPECIES,
                44332211L,
                0.0,
                4.0,
                false
        );
        DeterministicSpecimenDebug.Reproduction lucky = debug.reproduce(
                SPECIES,
                44332211L,
                25.0,
                4.0,
                false
        );

        assertEquals(neutral.specimen(), lucky.specimen());
        assertNotEquals(neutral.adjustedSpeciesWeight(), lucky.adjustedSpeciesWeight());
    }

    @Test
    void forcedPercentileIsDeterministicAndDoesNotChangeProductionGeneration() {
        long seed = 123456789L;
        DeterministicSpecimenDebug.Reproduction productionBefore = debug.reproduce(
                SPECIES,
                seed,
                0.0,
                3.0,
                false
        );
        DeterministicSpecimenDebug.Reproduction forcedA = debug.reproduce(
                SPECIES,
                seed,
                0.0,
                3.0,
                false,
                OptionalDouble.of(87.5)
        );
        DeterministicSpecimenDebug.Reproduction forcedB = debug.reproduce(
                SPECIES,
                seed,
                0.0,
                3.0,
                false,
                OptionalDouble.of(87.5)
        );
        DeterministicSpecimenDebug.Reproduction productionAfter = debug.reproduce(
                SPECIES,
                seed,
                0.0,
                3.0,
                false
        );

        assertEquals(forcedA, forcedB);
        assertTrue(forcedA.usedForcedPercentile());
        assertEquals(87.5, forcedA.specimen().basePercentile(), 0.0);
        assertEquals(SPECIES.sizeDistribution().quantile(0.875), forcedA.specimen().baseLength(), 1.0e-12);
        assertEquals(productionBefore, productionAfter);
    }

    @Test
    void forcedPercentileRejectsP100BecauseProductionNaturalRollIsHalfOpen() {
        assertThrows(
                IllegalArgumentException.class,
                () -> debug.reproduce(
                        SPECIES,
                        1L,
                        0.0,
                        0.0,
                        false,
                        OptionalDouble.of(100.0)
                )
        );
    }
}
