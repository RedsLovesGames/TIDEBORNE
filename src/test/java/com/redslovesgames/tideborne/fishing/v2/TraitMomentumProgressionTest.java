package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class TraitMomentumProgressionTest {
    @Test
    void repeatedFullyNormalCatchesIncreaseMomentumByOne() {
        TraitMomentumState state = new TraitMomentumState();
        SpecimenData normal = specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL, false);

        assertEquals(1, TraitMomentumProgression.applyCompletedCatch(state, normal));
        assertEquals(2, TraitMomentumProgression.applyCompletedCatch(state, normal));
        assertEquals(3, TraitMomentumProgression.applyCompletedCatch(state, normal));
        assertEquals(3, state.get("tide:trout"));
    }

    @Test
    void repeatedNormalCatchesStopAtMomentumCap() {
        TraitMomentumState state = new TraitMomentumState();
        SpecimenData normal = specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL, false);

        for (int i = 0; i < 40; i++) {
            TraitMomentumProgression.applyCompletedCatch(state, normal);
        }

        assertEquals(TraitMomentumStorage.MAX_MOMENTUM, state.get("tide:trout"));
    }

    @Test
    void anyNotableCanonicalAxisResetsMomentum() {
        TraitMomentumState state = new TraitMomentumState();

        SpecimenData[] notable = {
                specimen("tide:trout", SpecimenData.BodyType.GIANT,
                        SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                        SpecimenData.SpecimenQuality.NORMAL, false),
                specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                        SpecimenData.Condition.SCARRED, SpecimenData.Pigmentation.NORMAL,
                        SpecimenData.SpecimenQuality.NORMAL, false),
                specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                        SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.IRIDESCENT,
                        SpecimenData.SpecimenQuality.NORMAL, false),
                specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                        SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                        SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, false)
        };

        for (SpecimenData specimen : notable) {
            state.set("tide:trout", 12);
            assertEquals(0, TraitMomentumProgression.applyCompletedCatch(state, specimen));
            assertEquals(0, state.get("tide:trout"));
        }
    }

    @Test
    void speciesProgressionIsIsolated() {
        TraitMomentumState state = new TraitMomentumState();
        state.set("tide:bass", 9);
        SpecimenData normalTrout = specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL, false);
        SpecimenData notableTrout = specimen("tide:trout", SpecimenData.BodyType.DWARF,
                SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL, false);

        TraitMomentumProgression.applyCompletedCatch(state, normalTrout);
        assertEquals(1, state.get("tide:trout"));
        assertEquals(9, state.get("tide:bass"));

        TraitMomentumProgression.applyCompletedCatch(state, notableTrout);
        assertEquals(0, state.get("tide:trout"));
        assertEquals(9, state.get("tide:bass"));
    }

    @Test
    void capturedMomentumAddsTemporaryTraitLuck() {
        assertEquals(13.5, TraitMomentumProgression.effectiveTraitLuck(4.5, 9));
    }

    @Test
    void fullyNormalUsesCanonicalAxesRatherThanSizeOrPerfectCatchFlag() {
        SpecimenData normalAxes = specimen("tide:trout", SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL, SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL, true);

        assertTrue(TraitMomentumProgression.isFullyNormal(normalAxes));
        assertFalse(TraitMomentumProgression.isFullyNormal(specimen(
                "tide:trout", SpecimenData.BodyType.NORMAL, SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL, false)));
    }

    private static SpecimenData specimen(
            String speciesId,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality quality,
            boolean perfectCatch
    ) {
        return new SpecimenData(
                speciesId,
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                1234L,
                88.0,
                24.0,
                24.0,
                88.0,
                bodyType,
                condition,
                pigmentation,
                quality,
                perfectCatch,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
