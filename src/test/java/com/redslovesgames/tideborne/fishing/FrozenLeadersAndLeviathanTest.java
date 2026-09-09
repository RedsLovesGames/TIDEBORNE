package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;

class FrozenLeadersAndLeviathanTest {
    @Test void everyLeaderRetainsExactTierValuesAndSteelIdentity() {
        double[][] values={{.30,.98,1.01},{.55,.95,1.03},{.75,.90,1.06},{.95,.82,1.10}};
        var profiles=List.of(COPPER_LEADER,IRON_LEADER,GOLD_LEADER,DIAMOND_LEADER);
        for(int i=0;i<values.length;i++) {
            var tier=LeaderTier.values()[i]; var gear=LeaderGearModifiers.forTier(tier,true);
            assertArrayEquals(values[i],new double[]{FishingGearEffects.catchLossPreventionChance(gear),FishingGearEffects.catchZoneAreaMultiplier(gear),FishingGearEffects.minigameSpeedMultiplier(gear)},1e-12);
            assertEquals(profiles.get(i),FishingGearRegistry.resolveId(profiles.get(i).itemId()).orElseThrow());
            assertEquals(FishingGearModifiers.neutral(),LeaderGearModifiers.forTier(tier,false));
        }
        assertEquals("tidebound_compatibility:steel_leader",IRON_LEADER.itemId().toString());
    }
    @Test void representativeHunterComposesAllSlotsAndRespectsGlobalCaps() {
        var slots=List.of(TideborneFishingGearModifiers.rod(KUJIRA_BONE_FISHING_ROD,true),
                TideFishingLineModifiers.forProfile(TIDE_DIAMOND_LINE),
                TideborneFishingGearModifiers.hookTargets(SHARK_TOOTH_HOOK,false,true,true,new TideboundConfig.Values()),
                TideborneFishingGearModifiers.leviathanBait(true),LeaderGearModifiers.forTier(LeaderTier.DIAMOND,true));
        var gear=FishingGearModifiers.compose(slots); var reverse=new ArrayList<>(slots);Collections.reverse(reverse);
        assertEquals(gear,FishingGearModifiers.compose(reverse));
        assertEquals(4,gear.fishingLuck()); assertEquals(1,gear.traitLuck());
        assertEquals(.88*.82*1.3,FishingGearEffects.strengthMultiplier(gear),1e-12);
        assertEquals(1.30,FishingGearEffects.tempoMultiplier(gear),1e-12);
        assertEquals(.95,FishingGearEffects.catchLossPreventionChance(gear));
        assertEquals(.82,FishingGearEffects.catchZoneAreaMultiplier(gear));
        assertEquals(3,FishingGearEffects.speciesWeightMultiplier(profile(Set.of("heavy","kujira_target","boss")),FishingEnvironment.empty(),gear,0));
        assertEquals(.95,FishingGearEffects.catchLossPreventionChance(FishingGearModifiers.compose(gear,NETHERITE_ROD.rodModifiers())));
        assertTrue(LeviathanBaitRules.isFishOnlyCatchPool(gear));
    }
    @Test void baitTargetsOnlyExplicitBossMetadataWithoutChangingEligibilityOrSpecimen() {
        var bait=TideborneFishingGearModifiers.leviathanBait(true);
        assertEquals(2,FishingGearEffects.targetWeightMultiplier(profile(Set.of("boss")),FishingEnvironment.empty(),bait));
        assertEquals(1,FishingGearEffects.targetWeightMultiplier(profile(Set.of("legendary","ocean","heavy")),FishingEnvironment.empty(),bait));
        assertTrue(bait.bodyTypeChanceMultipliers().isEmpty()); assertTrue(bait.namedAdditiveModifiers().isEmpty());
        assertEquals(Map.of(FishingGearEffects.TARGET_WEIGHT_PREFIX+"boss",2.0),bait.namedMultiplierModifiers());
        var species=profile(Set.of("boss")); var specimen=new SpecimenGenerator().generate(species,77,SpecimenData.Provenance.generated(),0);
        var original=specimen.toString(); new FightProfileService().create(species,specimen,bait);
        new SpeciesSelectionService().adjustedWeight(species,0,FishingEnvironment.empty(),bait);
        assertEquals(original,specimen.toString());
    }
    private static SpeciesProfile profile(Set<String> tags) {
        return new SpeciesProfile("tide:fixture",CanonicalRarity.THREE_STAR,1,SpeciesEligibility.always(),1,1,"steady",new LogNormalSizeDistribution(25,.4),Set.of(),Map.of(),tags);
    }
}
