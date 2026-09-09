package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FrozenGearArchitectureTest {
    private static final double EPS = 1e-12;

    @Test
    void limitsApplyAfterTradeoffsComposeAndDoNotChangeRawProfiles() {
        var boost = FishingGearModifiers.builder().fishingLuck(12).traitLuck(6)
                .strengthMultiplier(0.5).tempoMultiplier(2)
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 2)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, 2).build();
        var cost = FishingGearModifiers.builder().fishingLuck(-6).traitLuck(-4)
                .strengthMultiplier(2).tempoMultiplier(0.5)
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 0.5)
                .namedMultiplierModifier(FishingGearEffects.MINIGAME_SPEED_MULTIPLIER, 0.5).build();
        assertEquals(8, FishingGearEffects.fishingLuck(boost));
        assertEquals(3, FishingGearEffects.traitLuck(boost));
        assertEquals(0.70, FishingGearEffects.strengthMultiplier(boost));
        assertEquals(1.30, FishingGearEffects.tempoMultiplier(boost));
        assertEquals(1.40, FishingGearEffects.catchZoneAreaMultiplier(boost));
        assertEquals(1.25, FishingGearEffects.minigameSpeedMultiplier(boost));
        assertEquals(1.35, FishingGearEffects.strengthMultiplier(cost));
        assertEquals(0.80, FishingGearEffects.tempoMultiplier(cost));
        assertEquals(0.70, FishingGearEffects.catchZoneAreaMultiplier(cost));
        assertEquals(0.80, FishingGearEffects.minigameSpeedMultiplier(cost));
        var combined = FishingGearModifiers.compose(boost, cost);
        assertEquals(combined, FishingGearModifiers.compose(cost, boost));
        assertEquals(6, FishingGearEffects.fishingLuck(combined));
        assertEquals(2, FishingGearEffects.traitLuck(combined));
        assertEquals(1, FishingGearEffects.strengthMultiplier(combined));
        assertEquals(1, FishingGearEffects.tempoMultiplier(combined));
        assertEquals(1, FishingGearEffects.catchZoneAreaMultiplier(combined));
        assertEquals(1, FishingGearEffects.minigameSpeedMultiplier(combined));
        assertEquals(12, boost.fishingLuck());
        var context = new FishingContext(0, 20, 10, Map.of(), Map.of(), Map.of()).withGearModifiers(boost);
        assertEquals(28, context.fishingLuck());
        assertEquals(13, context.traitLuck());
    }

    @Test
    void matchingTargetWeightsComposeWithEnvironmentAndLuckBeforeOneCap() {
        var species = species(Set.of("large", "predatory"));
        var ocean = new FishingEnvironment(Set.of("ocean"), Map.of());
        var a = FishingGearModifiers.builder().targetWeight("large", 2).targetWeight("warm", 100).build();
        var b = FishingGearModifiers.builder().targetWeight("predatory", 1.35).environmentWeight("ocean", 1.4).build();
        var c = FishingGearModifiers.builder().targetWeight("large", 0.5).fishingLuck(8).build();
        var all = FishingGearModifiers.compose(List.of(a, b, c));
        assertEquals(all, FishingGearModifiers.compose(List.of(c, a, b)));
        assertEquals(1.89, FishingGearEffects.targetWeightMultiplier(species, ocean, all), EPS);
        assertEquals(3, FishingGearEffects.speciesWeightMultiplier(species, ocean, all, 0));
        assertEquals(1, FishingGearEffects.speciesWeightMultiplier(species(Set.of()), FishingEnvironment.empty(), a, 0));
        double nativeLuck = 25;
        double expected = species.rarity().fishingLuckMultiplier(nativeLuck + 8)
                / species.rarity().fishingLuckMultiplier(nativeLuck) * 1.89;
        assertEquals(expected, FishingGearEffects.speciesWeightMultiplier(species, ocean, all, nativeLuck), EPS);
        assertEquals(species.encounterWeight() * species.rarity().fishingLuckMultiplier(nativeLuck) * expected,
                new SpeciesSelectionService().adjustedWeight(species, nativeLuck, ocean, all), EPS);
    }

    @Test
    void reliefChangesOnlyPositiveSpecimenSurchargeBeforeOtherGear() {
        var service = new FightProfileService();
        var baseline = new FightProfile(1, 0.1, service.catchZoneArea(1), "steady");
        var large = new FightProfile(1.4, 0.12, service.catchZoneArea(1.4), "steady");
        var gear = FishingGearModifiers.builder().trophyFightRelief(0.25).strengthMultiplier(0.92).build();
        var relieved = service.applyTrophyRelief(baseline, large, gear);
        assertEquals(1.30, relieved.strength(), EPS);
        assertEquals(0.115, relieved.tempo(), EPS);
        assertEquals(1.30 * 0.92, service.applyGearModifiers(relieved, gear).strength(), EPS);
        var small = new FightProfile(0.8, 0.08, service.catchZoneArea(0.8), "steady");
        assertEquals(small, service.applyTrophyRelief(baseline, small, gear));
        assertEquals(baseline, service.applyTrophyRelief(baseline, baseline, gear));
        assertEquals(1.4, large.strength());
    }

    @Test
    void gearFightAndSelectionNeverMutateOrRegenerateSpecimen() {
        var species = species(Set.of("large"));
        var generator = new SpecimenGenerator();
        var specimen = generator.generate(species, 77, SpecimenData.Provenance.generated(), 0);
        var before = specimen.toString();
        var gear = FishingGearModifiers.builder().trophyFightRelief(0.25).strengthMultiplier(0.92)
                .traitLuck(3).targetWeight("large", 2).build();
        new FightProfileService().create(species, specimen, gear);
        new FightProfileService().projectMinigame(species, specimen, gear, 1);
        new SpeciesSelectionService().adjustedWeight(species, 0, FishingEnvironment.empty(), gear);
        assertEquals(before, specimen.toString());
        assertEquals(specimen, generator.generate(species, 77, SpecimenData.Provenance.generated(), 0));
        for (var body : SpecimenData.BodyType.values()) assertEquals(1, gear.bodyTypeChanceMultiplier(body));
    }

    private static SpeciesProfile species(Set<String> tags) {
        return new SpeciesProfile("tide:test", CanonicalRarity.FIVE_STAR, 4, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25, 0.4), Set.of(), Map.of(), tags);
    }
}
