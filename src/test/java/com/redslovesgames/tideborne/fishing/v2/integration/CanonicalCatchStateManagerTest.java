package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.ConditionGenerator;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.NoPhysicalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.PigmentationGenerator;
import com.redslovesgames.tideborne.fishing.v2.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.TraitMomentumProgression;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CanonicalCatchStateManagerTest {
    @Test
    void capturedMomentumIsFrozenAndCompletedUpdateRunsExactlyOnce() {
        SpeciesProfile species = new SpeciesProfile(
                "tide:test_fish",
                CanonicalRarity.ONE_STAR,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                NoPhysicalSizeDistribution.INSTANCE,
                Set.of(),
                Map.of()
        );
        SpecimenData specimen = new SpecimenData(
                species.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                77L,
                50.0,
                0.0,
                0.0,
                50.0,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
        CanonicalCatchStateManager.CatchState state = new CanonicalCatchStateManager.CatchState(
                99L,
                FishingContext.neutral(),
                FishingEnvironment.empty(),
                species,
                specimen,
                new FightProfile(0.8, 1.0, 0.5, "steady"),
                11
        );
        AtomicInteger updates = new AtomicInteger();

        assertEquals(11, state.capturedTraitMomentum());
        assertFalse(state.traitMomentumUpdated());
        assertTrue(state.updateTraitMomentumOnce(updates::incrementAndGet));
        assertFalse(state.updateTraitMomentumOnce(updates::incrementAndGet));
        assertEquals(1, updates.get());
        assertTrue(state.traitMomentumUpdated());
        assertEquals(11, state.capturedTraitMomentum());
    }

    @Test
    void perfectCatchReachesCanonicalGenerationBeforePersistence() {
        SpeciesProfile species = new SpeciesProfile(
                "tide:post_fight_test_fish",
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                new LogNormalSizeDistribution(25.0, 0.4),
                Set.of(),
                Map.of()
        );
        FishingContext context = new FishingContext(0.0, 0.0, 7.0, Map.of(), Map.of(), Map.of());
        int capturedMomentum = 5;
        double effectiveTraitLuck = TraitMomentumProgression.effectiveTraitLuck(
                context.traitLuck(),
                capturedMomentum
        );
        long specimenSeed = 4_221_337L;
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData preFight = generator.generatePreFight(
                species,
                specimenSeed,
                SpecimenData.Provenance.generated(),
                effectiveTraitLuck
        );
        CanonicalCatchStateManager.CatchState state = new CanonicalCatchStateManager.CatchState(
                123L,
                context,
                FishingEnvironment.empty(),
                species,
                preFight,
                new FightProfile(0.8, 1.0, 0.5, "steady"),
                capturedMomentum
        );
        AtomicReference<SpecimenData> persisted = new AtomicReference<>();

        assertFalse(preFight.perfectCatch());
        assertEquals(SpecimenData.Condition.NORMAL, preFight.condition());
        assertEquals(SpecimenData.Pigmentation.NORMAL, preFight.pigmentation());
        assertFalse(state.specimenFinalized());

        assertTrue(CanonicalCatchStateManager.finalizeAndPersist(state, true, persisted::set));

        SpecimenData delivered = persisted.get();
        assertNotNull(delivered);
        assertTrue(state.specimenFinalized());
        assertTrue(delivered.perfectCatch(), "Perfect Catch must be canonical before persistence receives the specimen");
        assertEquals(state.specimen(), delivered);
        assertEquals(preFight.speciesId(), delivered.speciesId());
        assertEquals(preFight.deterministicSeed(), delivered.deterministicSeed());
        assertEquals(preFight.basePercentile(), delivered.basePercentile());
        assertEquals(preFight.baseLength(), delivered.baseLength());
        assertEquals(preFight.finalLength(), delivered.finalLength());
        assertEquals(preFight.finalPercentile(), delivered.finalPercentile());
        assertEquals(preFight.bodyType(), delivered.bodyType());
        assertEquals(
                new ConditionGenerator().generate(specimenSeed, species, effectiveTraitLuck),
                delivered.condition()
        );
        assertEquals(
                new PigmentationGenerator().generate(specimenSeed, species, effectiveTraitLuck),
                delivered.pigmentation()
        );

        AtomicReference<SpecimenData> repeatedPersistence = new AtomicReference<>();
        assertTrue(CanonicalCatchStateManager.finalizeAndPersist(state, false, repeatedPersistence::set));
        assertEquals(delivered, repeatedPersistence.get(), "post-fight specimen finalization must be exactly once");
        assertTrue(repeatedPersistence.get().perfectCatch(), "a later call cannot overwrite the captured skill result");
    }
}
