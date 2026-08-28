package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TraitProbabilityServiceTest {
    private static final double EPSILON = 1.0e-12;
    private final TraitProbabilityService service = new TraitProbabilityService();

    @Test
    void rarityCompensationMultipliersAreExactForAllFiveCanonicalRarities() {
        assertEquals(1.00, CanonicalRarity.ONE_STAR.traitProbabilityMultiplier());
        assertEquals(1.15, CanonicalRarity.TWO_STAR.traitProbabilityMultiplier());
        assertEquals(1.40, CanonicalRarity.THREE_STAR.traitProbabilityMultiplier());
        assertEquals(1.80, CanonicalRarity.FOUR_STAR.traitProbabilityMultiplier());
        assertEquals(2.40, CanonicalRarity.FIVE_STAR.traitProbabilityMultiplier());
    }

    @Test
    void zeroTraitLuckAppliesCanonicalSpeciesRarityCompensation() {
        double baseProbability = 0.05;

        assertEquals(0.0500, service.calculate(baseProbability, profile(CanonicalRarity.ONE_STAR), 0.0), EPSILON);
        assertEquals(0.0575, service.calculate(baseProbability, profile(CanonicalRarity.TWO_STAR), 0.0), EPSILON);
        assertEquals(0.0700, service.calculate(baseProbability, profile(CanonicalRarity.THREE_STAR), 0.0), EPSILON);
        assertEquals(0.0900, service.calculate(baseProbability, profile(CanonicalRarity.FOUR_STAR), 0.0), EPSILON);
        assertEquals(0.1200, service.calculate(baseProbability, profile(CanonicalRarity.FIVE_STAR), 0.0), EPSILON);
    }

    @Test
    void rarityCompensationRunsBeforeTraitLuckTransform() {
        SpeciesProfile fiveStar = profile(CanonicalRarity.FIVE_STAR);

        // 0.05 * 2.40 = 0.12, then T=10 gives 1 - (1 - 0.12)^2 = 0.2256.
        assertEquals(0.2256, service.calculate(0.05, fiveStar, 10.0), EPSILON);
    }

    @Test
    void combinedRarityAndTraitLuckMatchesKnownCases() {
        assertEquals(
                0.08499801058959444,
                service.calculate(0.05, profile(CanonicalRarity.TWO_STAR), 5.0),
                EPSILON
        );
        assertEquals(
                0.1719,
                service.calculate(0.05, profile(CanonicalRarity.FOUR_STAR), 10.0),
                EPSILON
        );
        assertEquals(
                0.2256,
                service.calculate(0.05, profile(CanonicalRarity.FIVE_STAR), 10.0),
                EPSILON
        );
    }

    @Test
    void finalProbabilityRemainsBoundedAndRetainsFrozenInputSafety() {
        SpeciesProfile fiveStar = profile(CanonicalRarity.FIVE_STAR);

        assertEquals(0.0, service.calculate(-1.0, fiveStar, 30.0));
        assertEquals(1.0, service.calculate(1.0, fiveStar, 30.0));
        assertEquals(1.0, service.calculate(Double.POSITIVE_INFINITY, fiveStar, 30.0));
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(Double.NaN, fiveStar, 30.0));
        assertThrows(NullPointerException.class,
                () -> service.calculate(0.05, null, 30.0));
    }

    private static SpeciesProfile profile(CanonicalRarity rarity) {
        return new SpeciesProfile(
                "tide:test_" + rarity.stars(),
                rarity,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                NoPhysicalSizeDistribution.INSTANCE,
                Set.of(),
                Map.of()
        );
    }
}
