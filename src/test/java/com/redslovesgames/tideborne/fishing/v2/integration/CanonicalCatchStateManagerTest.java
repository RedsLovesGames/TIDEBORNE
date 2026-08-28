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
    void identicalCatchGetsExactlyTenTemporaryTraitLuckWhenPerfect() {
        SpeciesProfile species = new SpeciesProfile(
                "tide:perfect_trait_luck_test_fish",
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                NoPhysicalSizeDistribution.INSTANCE,
                Set.of(),
                Map.of()
        );
        FishingContext context = new FishingContext(0.0, 0.0, 7.0, Map.of(), Map.of(), Map.of());
        int capturedMomentum = 5;
        double nonPerfectTraitLuck = TraitMomentumProgression.effectiveTraitLuck(
                context.traitLuck(),
                capturedMomentum
        );
        long specimenSeed = 78L;
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData preFight = generator.generatePreFight(
                species,
                specimenSeed,
                SpecimenData.Provenance.generated(),
                nonPerfectTraitLuck
        );
        FightProfile fightProfile = new FightProfile(0.8, 1.0, 0.5, "steady");
        CanonicalCatchStateManager.CatchState ordinaryState = new CanonicalCatchStateManager.CatchState(
                123L,
                context,
                FishingEnvironment.empty(),
                species,
                preFight,
                fightProfile,
                capturedMomentum
        );
        CanonicalCatchStateManager.CatchState perfectState = new CanonicalCatchStateManager.CatchState(
                123L,
                context,
                FishingEnvironment.empty(),
                species,
                preFight,
                fightProfile,
                capturedMomentum
        );

        SpecimenData ordinary = ordinaryState.finalizeSpecimenOnce(generator, false);
        SpecimenData perfect = perfectState.finalizeSpecimenOnce(generator, true);
        double perfectTraitLuck = nonPerfectTraitLuck + SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS;

        assertEquals(10.0, SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS);
        assertEquals(12.0, nonPerfectTraitLuck);
        assertEquals(22.0, perfectTraitLuck);
        assertFalse(ordinary.perfectCatch());
        assertTrue(perfect.perfectCatch());
        assertEquals(SpecimenData.Condition.NORMAL, ordinary.condition());
        assertEquals(SpecimenData.Condition.SCARRED, perfect.condition(),
                "seed 78 sits between the non-perfect and +10 Trait Luck Condition thresholds");
        assertEquals(
                new ConditionGenerator().generate(specimenSeed, species, nonPerfectTraitLuck),
                ordinary.condition()
        );
        assertEquals(
                new ConditionGenerator().generate(specimenSeed, species, perfectTraitLuck),
                perfect.condition()
        );
        assertEquals(
                new PigmentationGenerator().generate(specimenSeed, species, nonPerfectTraitLuck),
                ordinary.pigmentation()
        );
        assertEquals(
                new PigmentationGenerator().generate(specimenSeed, species, perfectTraitLuck),
                perfect.pigmentation()
        );

        assertEquals(ordinary.speciesId(), perfect.speciesId());
        assertEquals(ordinary.deterministicSeed(), perfect.deterministicSeed());
        assertEquals(ordinary.basePercentile(), perfect.basePercentile());
        assertEquals(ordinary.baseLength(), perfect.baseLength());
        assertEquals(ordinary.finalLength(), perfect.finalLength());
        assertEquals(ordinary.finalPercentile(), perfect.finalPercentile());
        assertEquals(ordinary.bodyType(), perfect.bodyType(),
                "Perfect Catch Trait Luck must not retroactively reroll the pre-fight Body Type");
        assertEquals(7.0, context.traitLuck(), "gear/context Trait Luck is read-only for the catch");
        assertEquals(5, ordinaryState.capturedTraitMomentum());
        assertEquals(5, perfectState.capturedTraitMomentum(),
                "Perfect Catch must not permanently modify captured or stored Momentum");
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
        double perfectTraitLuck = effectiveTraitLuck + SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS;
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
                new ConditionGenerator().generate(specimenSeed, species, perfectTraitLuck),
                delivered.condition()
        );
        assertEquals(
                new PigmentationGenerator().generate(specimenSeed, species, perfectTraitLuck),
                delivered.pigmentation()
        );
        assertEquals(7.0, state.context().traitLuck());
        assertEquals(5, state.capturedTraitMomentum());

        AtomicReference<SpecimenData> repeatedPersistence = new AtomicReference<>();
        assertTrue(CanonicalCatchStateManager.finalizeAndPersist(state, false, repeatedPersistence::set));
        assertEquals(delivered, repeatedPersistence.get(), "post-fight specimen finalization must be exactly once");
        assertTrue(repeatedPersistence.get().perfectCatch(), "a later call cannot overwrite the captured skill result");
    }
}
