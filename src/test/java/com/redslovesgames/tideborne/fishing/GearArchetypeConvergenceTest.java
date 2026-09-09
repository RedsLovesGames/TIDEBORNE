package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gametest.GearArchetypeCases;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import com.redslovesgames.tideborne.satchel.SatchelGearSummary;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;

class GearArchetypeConvergenceTest {
    private final TideboundConfig.Values config = new TideboundConfig.Values();
    private static final double EPS = 1e-10;

    @Test void sevenCompleteBuildsResolveFrozenValuesAndCanonicalLabels() {
        for (var build : GearArchetypeCases.builds()) {
            var gear = build.contributions(true, true, config);
            assertArrayEquals(build.expected(), GearArchetypeCases.values(gear), EPS, build.name());
            assertTrue(SatchelGearSummary.archetypes(gear).contains(build.name()), build.name());
            assertTrue(gear.bodyTypeChanceMultipliers().isEmpty(), build.name());
        }
    }

    @Test void handlingAndTargetingReadEverySpecimenAxisWithoutMutation() {
        var species = species(Set.of("heavy", "kujira_target", "boss", "legendary", "deep"));
        var generator = new SpecimenGenerator();
        var specimen = generator.generate(species, 7741, SpecimenData.Provenance.generated(), 0);
        var before = specimen.toString(); // Includes all axes, identity, length, percentile and score.
        for (var build : GearArchetypeCases.builds()) {
            var gear = build.contributions(true, true, config);
            new FightProfileService().create(species, specimen, gear);
            new FightProfileService().projectMinigame(species, specimen, gear, 1);
            new SpeciesSelectionService().adjustedWeight(species, 0, FishingEnvironment.empty(), gear);
            assertEquals(before, specimen.toString(), build.name());
            // TL may affect canonical traits; the independent natural sample never changes.
            var generated = generator.generate(species, 7741, SpecimenData.Provenance.generated(), FishingGearEffects.traitLuck(gear));
            assertEquals(specimen.basePercentile(), generated.basePercentile(), build.name());
            assertEquals(specimen.baseLength(), generated.baseLength(), build.name());
        }
    }

    @Test void representativeStrengthsCarryMeasurableOpportunityCosts() {
        var builds = GearArchetypeCases.builds();
        var trophy = builds.get(0).contributions(true,true,config);
        var rare = builds.get(1).contributions(true,true,config);
        var traits = builds.get(2).contributions(true,true,config);
        var safe = builds.get(3).contributions(true,true,config);
        var leviathan = builds.get(4).contributions(true,true,config);
        var fast = builds.get(5).contributions(true,true,config);
        assertTrue(trophy.strengthMultiplier() < traits.strengthMultiplier());
        assertTrue(FishingGearEffects.catchZoneAreaMultiplier(trophy) > FishingGearEffects.catchZoneAreaMultiplier(traits));
        assertTrue(traits.traitLuck() > trophy.traitLuck());
        assertTrue(rare.fishingLuck() > fast.fishingLuck());
        assertTrue(FishingGearEffects.catchLossPreventionChance(safe) > FishingGearEffects.catchLossPreventionChance(fast));
        assertTrue(FishingGearEffects.catchZoneAreaMultiplier(rare) < 1);
        assertTrue(FishingGearEffects.minigameSpeedMultiplier(safe) > 1);
        assertTrue(FishingGearEffects.tempoMultiplier(leviathan) > FishingGearEffects.tempoMultiplier(trophy));
        assertTrue(FishingGearEffects.catchZoneAreaMultiplier(leviathan) < FishingGearEffects.catchZoneAreaMultiplier(trophy));
        assertEquals(.7, FishingGearEffects.crateWeightMultiplier(leviathan), EPS);
        assertEquals(3, FishingGearEffects.speciesWeightMultiplier(species(Set.of("heavy","boss","kujira_target")), FishingEnvironment.empty(), leviathan, 0));
        assertEquals(.45, FishingGearEffects.targetWeightMultiplier(species(Set.of("very_small")), FishingEnvironment.empty(), leviathan), EPS);
        assertEquals(1, FishingGearEffects.targetWeightMultiplier(species(Set.of()), FishingEnvironment.empty(), leviathan), EPS);
        assertTrue(LeviathanBaitRules.isFishOnlyCatchPool(leviathan));
    }

    @Test void diamondAndNetheriteRetainDifferentAdvantagesOnTheSameSpecimen() {
        var species = species(Set.of());
        var specimen = new SpecimenGenerator().generate(species, 42, SpecimenData.Provenance.generated(), 0);
        var fights = new FightProfileService();
        var diamond = fights.create(species, specimen, DIAMOND_ROD.rodModifiers());
        var netherite = fights.create(species, specimen, NETHERITE_ROD.rodModifiers());
        assertTrue(diamond.strength() < netherite.strength());
        assertEquals(.25, FishingGearEffects.trophyFightRelief(DIAMOND_ROD.rodModifiers()));
        assertEquals(.10, FishingGearEffects.trophyFightRelief(NETHERITE_ROD.rodModifiers()));
        assertTrue(FishingGearEffects.catchLossPreventionChance(NETHERITE_ROD.rodModifiers()) > FishingGearEffects.catchLossPreventionChance(DIAMOND_ROD.rodModifiers()));
        assertEquals(0, GOLD_ROD.rodModifiers().fishingLuck()); // Native Gold luck is separate.
    }

    @Test void completeStackCapsRetainRawCostsAndNativeLuckProvenance() {
        var build = GearArchetypeCases.builds().get(4);
        // One bobber, two native bait slots (Leviathan + Lucky), and the existing six equipment roles.
        var twoBaits = new GearArchetypeCases.Build(build.name(), build.rod(), build.line(), build.hook(),
                "enchanted_golden_apple", build.bait(), build.leader(), 2, 0, new double[0]);
        var gear = twoBaits.contributions(true,true,config);
        assertEquals(11, gear.fishingLuck());
        assertEquals(8, FishingGearEffects.fishingLuck(gear));
        assertEquals(1, FishingGearEffects.traitLuck(gear));
        assertEquals(1.3992, gear.tempoMultiplier(), EPS);
        assertEquals(1.3, FishingGearEffects.tempoMultiplier(gear));
        assertEquals(.95, FishingGearEffects.catchLossPreventionChance(gear));
        assertEquals(.95, gear.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE), EPS);
        assertEquals(33, new FishingContext(0,25,0,Map.of(),Map.of(),Map.of()).withGearModifiers(gear).fishingLuck());
    }

    @Test void frozenLineDominanceIsRecordedWithoutRetuningValues() {
        var copper = TideFishingLineModifiers.forProfile(TIDE_COPPER_LINE);
        var base = TideFishingLineModifiers.forProfile(TIDE_BASE_LINE);
        assertTrue(copper.tempoMultiplier() < base.tempoMultiplier());
        assertTrue(FishingGearEffects.catchZoneAreaMultiplier(copper) > FishingGearEffects.catchZoneAreaMultiplier(base));
        assertEquals(copper.strengthMultiplier(), base.strengthMultiplier());
        // Material upgrades dominate the neutral base; non-base lines retain different handling costs.
        assertTrue(TideFishingLineModifiers.forProfile(TIDE_GOLDEN_LINE).strengthMultiplier() > 1);
        assertTrue(TideFishingLineModifiers.forProfile(TIDE_DIAMOND_LINE).tempoMultiplier() > 1);
        assertTrue(FishingGearEffects.minigameSpeedMultiplier(TideborneFishingGearModifiers.swiftLine(config)) > 1);
    }

    @Test void bobberSpecialtiesHaveDifferentMaximaAndSafetyCanMaskLeaderZoneCosts() {
        String[] winners = {"enchanted_golden_apple", "echo", "chorus", "netherite", "heart"};
        java.util.List<java.util.function.ToDoubleFunction<FishingGearModifiers>> metrics = List.of(
                FishingGearEffects::fishingLuck, FishingGearEffects::traitLuck,
                gear -> gear.namedAdditiveModifier(FishingGearEffects.LURE_BONUS),
                FishingGearEffects::catchLossPreventionChance, FishingGearEffects::crateWeightMultiplier);
        for (int i = 0; i < winners.length; i++) {
            var metric = metrics.get(i);
            var winner = FishingGearRegistry.bobberModifiers(net.minecraft.util.Identifier.of("tide", winners[i] + "_bobber")).orElseThrow();
            double max = FishingGearRegistry.supportedBobberIds().stream().map(id -> FishingGearRegistry.bobberModifiers(id).orElseThrow())
                    .mapToDouble(metric).max().orElseThrow();
            assertEquals(max, metric.applyAsDouble(winner), EPS);
        }
        // A balance finding, not permission to retune: insurance can cap despite net-positive zone area.
        var safe = GearArchetypeCases.builds().get(3).contributions(true,true,config);
        assertEquals(.95, FishingGearEffects.catchLossPreventionChance(safe));
        assertTrue(FishingGearEffects.catchZoneAreaMultiplier(safe) > 1);
        assertEquals(1.06, FishingGearEffects.minigameSpeedMultiplier(safe));
    }

    private static SpeciesProfile species(Set<String> tags) {
        return new SpeciesProfile("tide:archetype_fixture", CanonicalRarity.FIVE_STAR, 1, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25,.4), Set.of(), Map.of(), tags);
    }
}
