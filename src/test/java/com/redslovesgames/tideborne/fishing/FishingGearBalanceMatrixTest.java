package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.*;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.gear.LeaderGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.LeaderTier;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import com.redslovesgames.tideborne.journal.BobberBonuses;
import com.redslovesgames.tideborne.journal.ServerConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FishingGearBalanceMatrixTest {
 @Test void tideLineFightProfilesScaleAcrossProgression(){var c=TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_COPPER_LINE);var i=TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_IRON_LINE);var g=TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_GOLDEN_LINE);var d=TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE);assertEquals(.94,c.tempoMultiplier(),1e-12);assertEquals(1.02,FishingGearEffects.catchZoneAreaMultiplier(c),1e-12);assertEquals(.92,i.strengthMultiplier(),1e-12);assertEquals(1.03,FishingGearEffects.catchZoneAreaMultiplier(i),1e-12);assertEquals(.86,g.tempoMultiplier(),1e-12);assertEquals(1.05,g.strengthMultiplier(),1e-12);assertEquals(.82,d.strengthMultiplier(),1e-12);assertEquals(1.06,d.tempoMultiplier(),1e-12);}
 @Test void leviathanTradesSelectionPowerForHarderFight(){var combined=FishingGearModifiers.compose(TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE),TideborneFishingGearModifiers.leviathanBait(true));assertEquals(4,combined.fishingLuck(),1e-12);assertEquals(1,combined.traitLuck(),1e-12);assertEquals(.82*1.30,combined.strengthMultiplier(),1e-12);assertEquals(1.06*1.20,combined.tempoMultiplier(),1e-12);assertTrue(combined.categoryRestriction().allows("fish"));assertFalse(combined.categoryRestriction().allows("crate"));}
 @Test void leaderTiersScaleToNinetyFivePercentButNeverOneHundred(){assertLeader(LeaderTier.COPPER,.30,.98,1.01);assertLeader(LeaderTier.IRON,.55,.95,1.03);assertLeader(LeaderTier.GOLD,.75,.90,1.06);assertLeader(LeaderTier.DIAMOND,.95,.82,1.10);var stacked=FishingGearModifiers.compose(LeaderGearModifiers.forTier(LeaderTier.DIAMOND,true),FishingGearModifiers.builder().namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE,.5).namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES,1).build());assertEquals(.95,FishingGearEffects.catchLossPreventionChance(stacked),1e-12);}
 @Test void optionalLinesComposeWithIronLeader(){TideboundConfig.Values cfg=new TideboundConfig.Values();var tent=FishingGearModifiers.compose(TideborneFishingGearModifiers.tentacleLine(cfg),LeaderGearModifiers.forTier(LeaderTier.IRON,true));var swift=FishingGearModifiers.compose(TideborneFishingGearModifiers.swiftLine(cfg),LeaderGearModifiers.forTier(LeaderTier.IRON,true));assertEquals(1.32*.95,FishingGearEffects.catchZoneAreaMultiplier(tent),1e-12);assertEquals(1.16*1.03,FishingGearEffects.minigameSpeedMultiplier(tent),1e-12);assertEquals(1.16*.95,FishingGearEffects.catchZoneAreaMultiplier(swift),1e-12);assertEquals(1.08*1.03,FishingGearEffects.minigameSpeedMultiplier(swift),1e-12);}
 @Test void hookAndRodModifiersStaySituational(){TideboundConfig.Values c=new TideboundConfig.Values();assertEquals(2.0,weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, c.enableApexCompat, c.enableMythsCompat, c), "heavy"),1e-12);assertEquals(.45,weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, c.enableApexCompat, c.enableMythsCompat, c), "very_small"),1e-12);assertEquals(1.35,weight(TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SEAFARERS_HOOK, true, c.enableApexCompat, c.enableMythsCompat, c), "legendary"),1e-12);assertEquals(0.70,FishingGearEffects.crateWeightMultiplier(TideborneFishingGearModifiers.rod(FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD, true)),1e-12);}
 @Test void defaultBobberMatrixUsesProgressionLuckAndLure(){ServerConfig.Values c=new ServerConfig.Values();Map<String,BobberBonuses.Bonus>b=c.bobberBonuses;assertEquals(32,b.size());assertEquals(new BobberBonuses.Bonus(0,1),b.get("tide:red_bobber"));assertEquals(new BobberBonuses.Bonus(5,0),b.get("tide:enchanted_golden_apple_bobber"));assertEquals(new BobberBonuses.Bonus(0,0),b.get("tide:heart_bobber"));assertEquals(new BobberBonuses.Bonus(0,1),b.get("tide:echo_bobber"));assertEquals(new BobberBonuses.Bonus(0,2),b.get("tide:feather_bobber"));}
 @Test void noCanonicalGearDirectlyAddsFishScore(){TideboundConfig.Values c=new TideboundConfig.Values();for(var m:new FishingGearModifiers[]{TideFishingLineModifiers.forProfile(FishingGearRegistry.GearProfile.TIDE_COPPER_LINE),TideborneFishingGearModifiers.tentacleLine(c),LeaderGearModifiers.forTier(LeaderTier.DIAMOND,true),TideborneFishingGearModifiers.hookTargets(FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK, false, c.enableApexCompat, c.enableMythsCompat, c),TideborneFishingGearModifiers.leviathanBait(true)}){assertFalse(m.namedAdditiveModifiers().containsKey("fish_score"));assertFalse(m.namedMultiplierModifiers().containsKey("fish_score"));}}
 private static void assertLeader(LeaderTier t,double p,double z,double s){var m=LeaderGearModifiers.forTier(t,true);assertEquals(p,FishingGearEffects.catchLossPreventionChance(m),1e-12);assertEquals(z,FishingGearEffects.catchZoneAreaMultiplier(m),1e-12);assertEquals(s,FishingGearEffects.minigameSpeedMultiplier(m),1e-12);}
    private static double weight(FishingGearModifiers gear, String... tags) {
        var species = new SpeciesProfile("tide:target_fixture", CanonicalRarity.FIVE_STAR, 1, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25, .4), java.util.Set.of(), java.util.Map.of(), java.util.Set.of(tags));
        return FishingGearEffects.targetWeightMultiplier(species, FishingEnvironment.empty(), gear);
    }
}
