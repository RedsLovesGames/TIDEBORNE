package com.redslovesgames.tideborne.fishing.gear;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FishingGearModifiersTest {
    @Test
    void neutralModifierIsCompositionIdentity() {
        FishingGearModifiers gear = FishingGearModifiers.builder()
                .fishingLuck(15.0)
                .traitLuck(6.0)
                .strengthMultiplier(1.15)
                .tempoMultiplier(0.90)
                .restrictCategoriesTo("fish")
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT, 1.25)
                .namedAdditiveModifier("hook_control", 2.0)
                .namedMultiplierModifier("catch_zone_area", 1.10)
                .build();

        assertEquals(gear, FishingGearModifiers.compose(FishingGearModifiers.neutral(), gear));
    }

    @Test
    void composesLuckFightBodyTypeAndNamedModifiers() {
        FishingGearModifiers first = FishingGearModifiers.builder()
                .fishingLuck(15.0)
                .traitLuck(4.0)
                .strengthMultiplier(1.15)
                .tempoMultiplier(0.90)
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT, 1.25)
                .namedAdditiveModifier("hook_control", 2.0)
                .namedMultiplierModifier("catch_zone_area", 1.10)
                .build();
        FishingGearModifiers second = FishingGearModifiers.builder()
                .fishingLuck(-2.0)
                .traitLuck(6.0)
                .strengthMultiplier(0.80)
                .tempoMultiplier(1.20)
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT, 0.50)
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.DWARF, 1.40)
                .namedAdditiveModifier("hook_control", 3.0)
                .namedMultiplierModifier("catch_zone_area", 0.50)
                .build();

        FishingGearModifiers combined = FishingGearModifiers.compose(first, second);

        assertEquals(13.0, combined.fishingLuck());
        assertEquals(10.0, combined.traitLuck());
        assertEquals(0.92, combined.strengthMultiplier(), 1.0e-12);
        assertEquals(1.08, combined.tempoMultiplier(), 1.0e-12);
        assertEquals(0.625, combined.bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT));
        assertEquals(1.40, combined.bodyTypeChanceMultiplier(SpecimenData.BodyType.DWARF));
        assertEquals(1.0, combined.bodyTypeChanceMultiplier(SpecimenData.BodyType.NORMAL));
        assertEquals(5.0, combined.namedAdditiveModifier("hook_control"));
        assertEquals(0.55, combined.namedMultiplierModifier("catch_zone_area"));
        assertEquals(0.0, combined.namedAdditiveModifier("unused"));
        assertEquals(1.0, combined.namedMultiplierModifier("unused"));
    }

    @Test
    void compositionIsIndependentOfInputIterationOrder() {
        FishingGearModifiers huge = FishingGearModifiers.builder()
                .fishingLuck(1.0e16)
                .namedAdditiveModifier("precision", 1.0e16)
                .strengthMultiplier(1.10)
                .build();
        FishingGearModifiers negativeHuge = FishingGearModifiers.builder()
                .fishingLuck(-1.0e16)
                .namedAdditiveModifier("precision", -1.0e16)
                .strengthMultiplier(0.75)
                .build();
        FishingGearModifiers one = FishingGearModifiers.builder()
                .fishingLuck(1.0)
                .namedAdditiveModifier("precision", 1.0)
                .strengthMultiplier(1.20)
                .build();

        FishingGearModifiers forward = FishingGearModifiers.compose(List.of(huge, negativeHuge, one));
        FishingGearModifiers reordered = FishingGearModifiers.compose(List.of(one, huge, negativeHuge));

        assertEquals(forward, reordered);
        assertEquals(1.0, forward.fishingLuck());
        assertEquals(1.0, forward.namedAdditiveModifier("precision"));
        assertEquals(0.99, forward.strengthMultiplier(), 1.0e-12);
    }

    @Test
    void categoryAndCatchPoolRestrictionsComposeCanonically() {
        FishingGearModifiers first = FishingGearModifiers.builder()
                .restrictCategoriesTo("fish", "treasure")
                .restrictCatchPoolsTo("tide:ocean", "tide:river")
                .build();
        FishingGearModifiers second = FishingGearModifiers.builder()
                .restrictCategoriesTo("fish", "junk")
                .excludeCatchPools("tide:ocean")
                .build();

        FishingGearModifiers combined = FishingGearModifiers.compose(first, second);

        assertEquals(List.of("fish"), new ArrayList<>(combined.categoryRestriction().allowedIds()));
        assertTrue(combined.categoryRestriction().allows("fish"));
        assertFalse(combined.categoryRestriction().allows("junk"));
        assertTrue(combined.catchPoolRestriction().allows("tide:river"));
        assertFalse(combined.catchPoolRestriction().allows("tide:ocean"));
        assertEquals(List.of("tide:ocean"), new ArrayList<>(combined.catchPoolRestriction().deniedIds()));
    }

    @Test
    void disjointAllowlistsRemainRestrictedAndAllowNothing() {
        FishingGearModifiers combined = FishingGearModifiers.compose(
                FishingGearModifiers.builder().restrictCategoriesTo("fish").build(),
                FishingGearModifiers.builder().restrictCategoriesTo("junk").build()
        );

        assertTrue(combined.categoryRestriction().allowListActive());
        assertTrue(combined.categoryRestriction().allowedIds().isEmpty());
        assertFalse(combined.categoryRestriction().allows("fish"));
        assertFalse(combined.categoryRestriction().allows("junk"));
    }

    @Test
    void identifierMapsAndSetsUseDeterministicOrderAndAreImmutable() {
        FishingGearModifiers modifiers = FishingGearModifiers.builder()
                .restrictCategoriesTo("treasure", "fish", "junk")
                .namedAdditiveModifier("zeta", 1.0)
                .namedAdditiveModifier("alpha", 2.0)
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.DWARF, 1.2)
                .bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT, 0.8)
                .build();

        assertEquals(List.of("fish", "junk", "treasure"), new ArrayList<>(modifiers.categoryRestriction().allowedIds()));
        assertEquals(List.of("alpha", "zeta"), new ArrayList<>(modifiers.namedAdditiveModifiers().keySet()));
        assertEquals(
                List.of(SpecimenData.BodyType.GIANT, SpecimenData.BodyType.DWARF),
                new ArrayList<>(modifiers.bodyTypeChanceMultipliers().keySet())
        );
        assertThrows(UnsupportedOperationException.class,
                () -> modifiers.namedAdditiveModifiers().put("beta", 3.0));
        assertThrows(UnsupportedOperationException.class,
                () -> modifiers.categoryRestriction().allowedIds().add("crate"));
    }

    @Test
    void validatesMultiplierAndNamedModifierInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> FishingGearModifiers.builder().strengthMultiplier(-0.01).build());
        assertThrows(IllegalArgumentException.class,
                () -> FishingGearModifiers.builder().tempoMultiplier(Double.NaN).build());
        assertThrows(IllegalArgumentException.class,
                () -> FishingGearModifiers.builder().bodyTypeChanceMultiplier(SpecimenData.BodyType.GIANT, -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> FishingGearModifiers.builder().namedAdditiveModifier(" ", 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FishingGearModifiers(
                        0.0,
                        0.0,
                        1.0,
                        1.0,
                        null,
                        null,
                        Map.of(),
                        Map.of(),
                        Map.of("bad", Double.POSITIVE_INFINITY)
                ));
    }
}
