package com.redslovesgames.tideborne.fishing.v2.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.TraitMomentumStorage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FishingSimulatorTest {
    private final FishingSimulator simulator = new FishingSimulator();

    @Test
    void sameInputsProduceExactlySameAggregateResult() {
        List<SpeciesProfile> species = List.of(
                profile("tide:common", CanonicalRarity.ONE_STAR, 8.0),
                profile("tide:rare", CanonicalRarity.FOUR_STAR, 2.0)
        );
        FishingSimulator.Config config = new FishingSimulator.Config(
                987654321L, 5_000, 9.0, 3.5, 0.20, Optional.empty(), true
        );

        FishingSimulator.Result first = simulator.simulate(species, config);
        FishingSimulator.Result second = simulator.simulate(species, config);

        assertEquals(first, second);
    }

    @Test
    void tracksEveryRequestedDistributionWithoutRetainingCatchObjects() {
        int catches = 20_000;
        FishingSimulator.Result result = simulator.simulate(
                List.of(
                        profile("tide:one", CanonicalRarity.ONE_STAR, 5.0),
                        profile("tide:three", CanonicalRarity.THREE_STAR, 3.0),
                        profile("tide:five", CanonicalRarity.FIVE_STAR, 1.0)
                ),
                new FishingSimulator.Config(12345L, catches, 4.0, 2.0, 0.15, Optional.empty(), true)
        );

        assertEquals(catches, sum(result.speciesCounts()));
        assertEquals(catches, sum(result.rarityCounts()));
        assertEquals(catches, sum(result.bodyTypeCounts()));
        assertEquals(catches, sum(result.conditionCounts()));
        assertEquals(catches, sum(result.pigmentationCounts()));
        assertEquals(catches, sum(result.qualityCounts()));
        assertEquals(catches, result.naturalPercentiles().buckets().stream().mapToLong(Long::longValue).sum());
        assertEquals(catches, result.finalPercentiles().buckets().stream().mapToLong(Long::longValue).sum());
        assertEquals(catches, result.fishScores().bands().stream().mapToLong(Long::longValue).sum());
        assertTrue(result.naturalPercentiles().min() >= 0.0);
        assertTrue(result.naturalPercentiles().max() < 100.0);
        assertTrue(result.finalPercentiles().min() >= 0.0);
        assertTrue(result.finalPercentiles().max() <= 100.0);
        assertTrue(result.fishScores().min() >= 1);
        assertTrue(result.fishScores().max() <= 3000);
    }

    @Test
    void fishingLuckChangesRaritySelectionThroughCanonicalSelector() {
        List<SpeciesProfile> species = List.of(
                profile("tide:one", CanonicalRarity.ONE_STAR, 1.0),
                profile("tide:five", CanonicalRarity.FIVE_STAR, 1.0)
        );
        FishingSimulator.Result neutral = simulator.simulate(
                species,
                new FishingSimulator.Config(9911L, 30_000, 0.0, 0.0, 0.0, Optional.empty(), false)
        );
        FishingSimulator.Result lucky = simulator.simulate(
                species,
                new FishingSimulator.Config(9911L, 30_000, 25.0, 0.0, 0.0, Optional.empty(), false)
        );

        assertTrue(lucky.rarityCounts().get(CanonicalRarity.FIVE_STAR)
                > neutral.rarityCounts().get(CanonicalRarity.FIVE_STAR));
        assertEquals(30_000L, sum(lucky.rarityCounts()));
    }

    @Test
    void traitLuckRaisesCanonicalNotableAxisEvents() {
        List<SpeciesProfile> species = List.of(profile("tide:test", CanonicalRarity.THREE_STAR, 1.0));
        FishingSimulator.Result neutral = simulator.simulate(
                species,
                new FishingSimulator.Config(7788L, 30_000, 0.0, 0.0, 0.0, Optional.empty(), false)
        );
        FishingSimulator.Result lucky = simulator.simulate(
                species,
                new FishingSimulator.Config(7788L, 30_000, 0.0, 20.0, 0.0, Optional.empty(), false)
        );

        assertTrue(notableAxisEvents(lucky) > notableAxisEvents(neutral));
    }

    @Test
    void rarityFilterRestrictsSimulationPool() {
        FishingSimulator.Result result = simulator.simulate(
                List.of(
                        profile("tide:one", CanonicalRarity.ONE_STAR, 100.0),
                        profile("tide:five", CanonicalRarity.FIVE_STAR, 1.0)
                ),
                new FishingSimulator.Config(
                        55L, 2_000, 0.0, 0.0, 0.0, Optional.of(CanonicalRarity.FIVE_STAR), false
                )
        );

        assertEquals(2_000L, result.speciesCounts().get("tide:five"));
        assertEquals(0L, result.rarityCounts().get(CanonicalRarity.ONE_STAR));
        assertEquals(2_000L, result.rarityCounts().get(CanonicalRarity.FIVE_STAR));
    }

    @Test
    void momentumSimulationUsesCanonicalProgressionWithoutPlayerPersistence() {
        int catches = 20_000;
        FishingSimulator.Result result = simulator.simulate(
                List.of(profile("tide:test", CanonicalRarity.ONE_STAR, 1.0)),
                new FishingSimulator.Config(712367L, catches, 0.0, 0.0, 0.0, Optional.empty(), true)
        );

        assertTrue(result.momentum().catchesUsingMomentum() > 0);
        assertTrue(result.momentum().averageCapturedMomentum(catches) > 0.0);
        assertTrue(result.momentum().maxCapturedMomentum() > 0);
        assertTrue(result.momentum().maxCapturedMomentum() <= TraitMomentumStorage.MAX_MOMENTUM);
        assertTrue(result.momentum().increments() > 0);
        assertTrue(result.momentum().resets() > 0);
        assertTrue(result.momentum().finalMomentumBySpecies().get("tide:test") >= 0);
        assertTrue(result.momentum().finalMomentumBySpecies().get("tide:test") <= TraitMomentumStorage.MAX_MOMENTUM);
    }

    @Test
    void perfectCatchRateSupportsExactAlwaysAndNeverFlags() {
        List<SpeciesProfile> species = List.of(profile("tide:test", CanonicalRarity.TWO_STAR, 1.0));
        FishingSimulator.Result never = simulator.simulate(
                species,
                new FishingSimulator.Config(88L, 2_000, 0.0, 0.0, 0.0, Optional.empty(), false)
        );
        FishingSimulator.Result always = simulator.simulate(
                species,
                new FishingSimulator.Config(88L, 2_000, 0.0, 0.0, 1.0, Optional.empty(), false)
        );

        assertEquals(0L, never.perfectCatchCount());
        assertEquals(2_000L, always.perfectCatchCount());
    }

    private static long notableAxisEvents(FishingSimulator.Result result) {
        return result.catchCount() - result.bodyTypeCounts().get(SpecimenData.BodyType.NORMAL)
                + result.catchCount() - result.conditionCounts().get(SpecimenData.Condition.NORMAL)
                + result.catchCount() - result.pigmentationCounts().get(SpecimenData.Pigmentation.NORMAL)
                + result.catchCount() - result.qualityCounts().get(SpecimenData.SpecimenQuality.NORMAL);
    }

    private static long sum(Map<?, Long> values) {
        return values.values().stream().mapToLong(Long::longValue).sum();
    }

    private static SpeciesProfile profile(String id, CanonicalRarity rarity, double weight) {
        return new SpeciesProfile(
                id,
                rarity,
                weight,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                new LogNormalSizeDistribution(30.0, 0.20),
                Set.of(),
                Map.of()
        );
    }
}
