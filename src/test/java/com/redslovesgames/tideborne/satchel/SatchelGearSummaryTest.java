package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SatchelGearSummaryTest {
    @Test void rolesFollowCanonicalContributionsWithoutMutatingThem() {
        var gear = FishingGearModifiers.compose(FishingGearRegistry.GearProfile.DIAMOND_ROD.rodModifiers(),
                FishingGearModifiers.builder().traitLuck(20).fishingLuck(20).build());
        var labels = SatchelGearSummary.archetypes(gear);
        assertTrue(labels.containsAll(List.of("Trophy", "Heavy Fish", "Trait", "Rare Species")));
        assertEquals(20, gear.traitLuck());
    }
    @Test void neutralEquipmentHasNoStrongArchetype() {
        assertTrue(SatchelGearSummary.archetypes(FishingGearModifiers.neutral()).isEmpty());
    }
    @Test void allSevenRolesAreInferredFromEffectsRatherThanPresetNames() {
        var gear = FishingGearModifiers.builder().fishingLuck(1).traitLuck(1).strengthMultiplier(.9).trophyFightRelief(.25)
                .targetWeight("boss", 2).namedAdditiveModifier(FishingGearEffects.LURE_BONUS, 2)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, .3).build();
        assertEquals(List.of("Trophy", "Rare Species", "Trait", "Safe", "Leviathan", "Fast", "Heavy Fish"), SatchelGearSummary.archetypes(gear));
    }
    @Test void handlingPenaltyDoesNotPretendToImproveCatchSpeed() {
        var gear = FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, 1.1).build();
        assertFalse(SatchelGearSummary.archetypes(gear).contains("Fast"));
    }
    @Test void targetEffectsCanIdentifyPartialBuilds() {
        assertTrue(SatchelGearSummary.archetypes(FishingGearModifiers.builder().targetWeight("heavy", 2).build()).contains("Heavy Fish"));
        assertTrue(SatchelGearSummary.archetypes(FishingGearModifiers.builder().targetWeight("legendary", 1.35).build()).contains("Rare Species"));
    }
}
