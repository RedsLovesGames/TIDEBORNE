package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.NoPhysicalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
}
