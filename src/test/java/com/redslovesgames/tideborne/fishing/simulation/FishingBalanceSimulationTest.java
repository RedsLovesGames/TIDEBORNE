package com.redslovesgames.tideborne.fishing.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenQualityService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FishingBalanceSimulationTest {
    private static final int CATCHES = 60_000;
    private static final long SEED = 0x5A17E2026L;
    private final FishingSimulator simulator = new FishingSimulator();

    @Test
    void baselineAndSubtypeRatesMatchFrozenOneStarProbabilities() {
        FishingSimulator.Result result = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, false);

        assertBetween(rate(result, notableBody(result)), 0.045, 0.055);
        assertBetween(rate(result, notableCondition(result)), 0.045, 0.055);
        assertBetween(rate(result, notablePigmentation(result)), 0.013, 0.017);
        assertBetween(subtypeRate(result.conditionCounts().get(SpecimenData.Condition.SCARRED), notableCondition(result)), 0.62, 0.68);
        assertBetween(subtypeRate(result.pigmentationCounts().get(SpecimenData.Pigmentation.ALBINO), notablePigmentation(result)), 0.66, 0.74);
        assertBetween(result.naturalPercentiles().mean(), 49.0, 51.0);
    }

    @Test
    void rarityCompensationMatchesFrozenEndpoints() {
        FishingSimulator.Result one = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result five = run(0.0, 0.0, 0.0, CanonicalRarity.FIVE_STAR, false);

        assertBetween(rate(five, notableBody(five)), 0.112, 0.128);
        assertBetween(rate(five, notableCondition(five)), 0.112, 0.128);
        assertBetween(rate(five, notablePigmentation(five)), 0.033, 0.039);
        assertTrue(notableBody(five) > notableBody(one));
        assertTrue(notableCondition(five) > notableCondition(one));
        assertTrue(notablePigmentation(five) > notablePigmentation(one));
    }

    @Test
    void traitLuckAndPerfectCatchMoveRatesInExpectedDirection() {
        FishingSimulator.Result neutral = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result ten = run(0.0, 10.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result fifteen = run(0.0, 15.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result perfect = run(0.0, 0.0, 1.0, CanonicalRarity.ONE_STAR, false);

        assertBetween(rate(ten, notableBody(ten)), 0.090, 0.105);
        assertBetween(rate(fifteen, notableBody(fifteen)), 0.112, 0.128);
        assertTrue(notableBody(fifteen) > notableBody(ten));
        assertTrue(notableCondition(fifteen) > notableCondition(ten));
        assertTrue(notablePigmentation(fifteen) > notablePigmentation(ten));
        assertTrue(perfect.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN)
                > ten.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN));
        assertEquals(CATCHES, perfect.perfectCatchCount());
        assertTrue(perfect.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN)
                > neutral.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN));
    }

    @Test
    void fishingLuckMovesEqualWeightPoolTowardHigherRarity() {
        List<SpeciesProfile> pool = equalWeightRarityPool();
        FishingSimulator.Result l0 = simulator.simulate(pool, config(0.0, 0.0, 0.0, Optional.empty(), false));
        FishingSimulator.Result l5 = simulator.simulate(pool, config(5.0, 0.0, 0.0, Optional.empty(), false));
        FishingSimulator.Result l10 = simulator.simulate(pool, config(10.0, 0.0, 0.0, Optional.empty(), false));
        FishingSimulator.Result l15 = simulator.simulate(pool, config(15.0, 0.0, 0.0, Optional.empty(), false));

        long five0 = l0.rarityCounts().get(CanonicalRarity.FIVE_STAR);
        long five5 = l5.rarityCounts().get(CanonicalRarity.FIVE_STAR);
        long five10 = l10.rarityCounts().get(CanonicalRarity.FIVE_STAR);
        long five15 = l15.rarityCounts().get(CanonicalRarity.FIVE_STAR);
        assertTrue(five0 < five5 && five5 < five10 && five10 < five15);
        assertBetween(rate(l0, five0), 0.19, 0.21);
        assertTrue(rate(l15, five15) > 0.27);
        assertTrue(l15.fishScores().mean() > l0.fishScores().mean());
    }

    @Test
    void momentumRaisesEffectiveTraitRatesWithoutPersistentPlayerState() {
        FishingSimulator.Result off = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result on = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, true);

        assertTrue(on.momentum().averageCapturedMomentum(CATCHES) > 3.0);
        assertEquals(15, on.momentum().maxCapturedMomentum());
        assertTrue(notableBody(on) > notableBody(off));
        assertTrue(notableCondition(on) > notableCondition(off));
        assertTrue(notablePigmentation(on) > notablePigmentation(off));
        assertTrue(on.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN)
                > off.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN));
    }

    @Test
    void perfectSpecimenCurveAndFishScoreRemainInsideFrozenModel() {
        SpecimenQualityService quality = new SpecimenQualityService();
        assertEquals(0.02, quality.baseProbability(95.0), 1.0e-12);
        assertEquals(0.08, quality.baseProbability(97.5), 1.0e-12);
        assertEquals(0.25, quality.baseProbability(99.0), 1.0e-12);
        assertEquals(0.60, quality.baseProbability(99.9), 1.0e-12);
        assertEquals(0.4375, quality.probability(99.0, 10.0, false), 1.0e-12);
        assertEquals(0.68359375, quality.probability(99.0, 10.0, true), 1.0e-12);

        FishingSimulator.Result one = run(0.0, 0.0, 0.0, CanonicalRarity.ONE_STAR, false);
        FishingSimulator.Result five = run(0.0, 0.0, 0.0, CanonicalRarity.FIVE_STAR, false);
        assertTrue(one.fishScores().min() >= 1 && one.fishScores().max() <= 3000);
        assertTrue(five.fishScores().min() >= 1 && five.fishScores().max() <= 3000);
        assertTrue(five.fishScores().mean() > one.fishScores().mean() + 900.0);
    }

    private FishingSimulator.Result run(double fishingLuck, double traitLuck, double perfectCatchRate,
                                        CanonicalRarity rarity, boolean momentum) {
        return simulator.simulate(equalWeightRarityPool(),
                config(fishingLuck, traitLuck, perfectCatchRate, Optional.of(rarity), momentum));
    }

    private static FishingSimulator.Config config(double fishingLuck, double traitLuck, double perfectCatchRate,
                                                   Optional<CanonicalRarity> rarity, boolean momentum) {
        return new FishingSimulator.Config(SEED, CATCHES, fishingLuck, traitLuck, perfectCatchRate, rarity, momentum);
    }

    private static List<SpeciesProfile> equalWeightRarityPool() {
        List<SpeciesProfile> profiles = new ArrayList<>();
        for (CanonicalRarity rarity : CanonicalRarity.values()) {
            profiles.add(profile("sim:" + rarity.name().toLowerCase(), rarity));
        }
        return List.copyOf(profiles);
    }

    private static SpeciesProfile profile(String id, CanonicalRarity rarity) {
        return new SpeciesProfile(id, rarity, 1.0, SpeciesEligibility.always(), 1.0, 1.0, "steady",
                new LogNormalSizeDistribution(30.0, 0.20), Set.of(), Map.of());
    }

    private static long notableBody(FishingSimulator.Result result) {
        return result.catchCount() - result.bodyTypeCounts().get(SpecimenData.BodyType.NORMAL);
    }

    private static long notableCondition(FishingSimulator.Result result) {
        return result.catchCount() - result.conditionCounts().get(SpecimenData.Condition.NORMAL);
    }

    private static long notablePigmentation(FishingSimulator.Result result) {
        return result.catchCount() - result.pigmentationCounts().get(SpecimenData.Pigmentation.NORMAL);
    }

    private static double rate(FishingSimulator.Result result, long count) {
        return (double) count / result.catchCount();
    }

    private static double subtypeRate(long count, long eventCount) {
        return eventCount == 0 ? 0.0 : (double) count / eventCount;
    }

    private static void assertBetween(double value, double minimum, double maximum) {
        assertTrue(value >= minimum && value <= maximum,
                () -> "expected " + value + " in [" + minimum + ", " + maximum + "]");
    }
}
