package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.specimen.TraitProbabilityService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.gear.LeviathanBaitRules;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LeviathanBaitModifiersTest {
    @Test
    void activeBaitPublishesTheCompleteCanonicalModifierSet() {
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(true);

        assertEquals(15.0, modifiers.fishingLuck(), 0.0);
        assertEquals(4.0, modifiers.traitLuck(), 0.0);
        assertEquals(1.25, modifiers.strengthMultiplier(), 0.0);
        assertEquals(1.20, modifiers.tempoMultiplier(), 0.0);
        assertTrue(LeviathanBaitRules.isFishOnlyCatchPool(modifiers));
        assertEquals(Set.of(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY), modifiers.categoryRestriction().allowedIds());
    }

    @Test
    void inactiveBaitIsFullyNeutral() {
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(false);

        assertEquals(FishingGearModifiers.neutral(), modifiers);
        assertFalse(LeviathanBaitRules.isFishOnlyCatchPool(modifiers));
    }

    @Test
    void canonicalContextKeepsFishingLuckAndTraitLuckOnSeparateAxes() {
        FishingContext base = new FishingContext(0.0, 2.0, 3.0, Map.of(), Map.of(), Map.of());
        FishingContext withBait = base.withGearModifiers(TideborneFishingGearModifiers.leviathanBait(true));

        assertEquals(17.0, withBait.fishingLuck(), 0.0);
        assertEquals(7.0, withBait.traitLuck(), 0.0);
        assertEquals(base.biteSpeed(), withBait.biteSpeed(), 0.0);
        assertEquals(base.equipmentModifiers(), withBait.equipmentModifiers());
        assertEquals(base.baitModifiers(), withBait.baitModifiers());
        assertEquals(base.environmentModifiers(), withBait.environmentModifiers());
    }

    @Test
    void fishingLuckChangesSpeciesWeightWhileTraitLuckDoesNot() {
        SpeciesProfile species = profile(CanonicalRarity.FIVE_STAR);
        SpeciesSelectionService selector = new SpeciesSelectionService();
        FishingGearModifiers bait = TideborneFishingGearModifiers.leviathanBait(true);
        FishingContext baitContext = FishingContext.neutral().withGearModifiers(bait);
        FishingContext traitOnly = new FishingContext(0.0, 0.0, baitContext.traitLuck(), Map.of(), Map.of(), Map.of());

        double baseWeight = selector.adjustedWeight(species, 0.0);
        double baitWeight = selector.adjustedWeight(species, baitContext.fishingLuck());
        double traitOnlyWeight = selector.adjustedWeight(species, traitOnly.fishingLuck());

        assertTrue(baitWeight > baseWeight);
        assertEquals(baseWeight, traitOnlyWeight, 0.0);
    }

    @Test
    void traitLuckChangesTraitProbabilityWithoutParticipatingInSpeciesWeighting() {
        SpeciesProfile species = profile(CanonicalRarity.THREE_STAR);
        TraitProbabilityService traits = new TraitProbabilityService();
        FishingContext baitContext = FishingContext.neutral().withGearModifiers(TideborneFishingGearModifiers.leviathanBait(true));

        double withoutBaitTraitLuck = traits.calculate(0.05, species, 0.0);
        double withBaitTraitLuck = traits.calculate(0.05, species, baitContext.traitLuck());

        assertTrue(withBaitTraitLuck > withoutBaitTraitLuck);
    }

    @Test
    void fightMultipliersApplyToCanonicalFightProfileAndRecomputeCatchZone() {
        FightProfileService fights = new FightProfileService();
        FightProfile base = new FightProfile(0.8, 0.08, fights.catchZoneArea(0.8), "steady");
        FightProfile modified = fights.applyGearModifiers(base, TideborneFishingGearModifiers.leviathanBait(true));

        assertEquals(0.8 * 1.25, modified.strength(), 1.0e-12);
        assertEquals(0.08 * 1.20, modified.tempo(), 1.0e-12);
        assertEquals(fights.catchZoneArea(modified.strength()), modified.catchZoneArea(), 1.0e-12);
        assertTrue(modified.catchZoneArea() < base.catchZoneArea());
        assertEquals(base.behavior(), modified.behavior());
    }

    private static SpeciesProfile profile(CanonicalRarity rarity) {
        return new SpeciesProfile(
                "tide:leviathan_test",
                rarity,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                new LogNormalSizeDistribution(25.0, 0.4),
                Set.of(),
                Map.of()
        );
    }
}
