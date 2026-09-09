package com.redslovesgames.tideborne.fishing.simulation;

import com.redslovesgames.tideborne.fishing.*;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import com.redslovesgames.tideborne.fishing.gametest.GearArchetypeCases;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import java.util.*;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;

/** Controlled validation scenarios; not a gear resolver or shipped species catalog. */
final class PostGearBalanceScenarios {
    static final long[] SEEDS = {0x504F535447454152L, 0x42414C414E434532L};
    static final TideboundConfig.Values CONFIG = new TideboundConfig.Values();
    record Scenario(String name, FishingGearModifiers gear, double nativeLuck) {}
    static List<Scenario> builds() {
        var result = new ArrayList<Scenario>();
        // Wood / Base line / Base hook / Red bobber / no bait / no leader.
        result.add(new Scenario("Baseline", FishingGearRegistry.bobberModifiers(net.minecraft.util.Identifier.of("tide","red_bobber")).orElseThrow(), 0));
        for (var build : GearArchetypeCases.builds()) result.add(new Scenario(build.name(), build.contributions(true,true,CONFIG), build.rod()==GOLD_ROD?1:0));
        return List.copyOf(result);
    }
    static List<Scenario> ablations() {
        var base = builds().getFirst().gear();
        return List.of(
                new Scenario("Shark only", FishingGearModifiers.compose(base,TideborneFishingGearModifiers.hookTargets(SHARK_TOOTH_HOOK,true,true,true,CONFIG)),0),
                new Scenario("Kujira only", FishingGearModifiers.compose(base,KUJIRA_BONE_FISHING_ROD.rodModifiers()),0),
                new Scenario("Kujira + Shark", FishingGearModifiers.compose(base,KUJIRA_BONE_FISHING_ROD.rodModifiers(),TideborneFishingGearModifiers.hookTargets(SHARK_TOOTH_HOOK,true,true,true,CONFIG)),0),
                new Scenario("Seafarer only", FishingGearModifiers.compose(base,TideborneFishingGearModifiers.hookTargets(SEAFARERS_HOOK,true,true,true,CONFIG)),0),
                new Scenario("Incandescent only", FishingGearModifiers.compose(base,INCANDESCENT_BAIT.baitTargetModifiers(),FishingGearModifiers.builder().namedAdditiveModifier(FishingGearEffects.LURE_BONUS,1).build()),0),
                new Scenario("Abyss only", FishingGearModifiers.compose(base,ABYSS_BAIT.baitTargetModifiers(),FishingGearModifiers.builder().namedAdditiveModifier(FishingGearEffects.LURE_BONUS,2).build()),0),
                new Scenario("Lucky only", FishingGearModifiers.compose(base,FishingGearModifiers.builder().fishingLuck(2).build()),0),
                new Scenario("Leviathan bait only", FishingGearModifiers.compose(base,TideborneFishingGearModifiers.leviathanBait(true)),0));
    }
    static List<SpeciesProfile> pool(boolean hypotheticalBoss) {
        var pool = new ArrayList<SpeciesProfile>();
        for (var rarity : CanonicalRarity.values()) {
            for (String role : List.of("ordinary","very_small","heavy","warm","deep")) {
                var tags = new HashSet<String>();
                if (!role.equals("ordinary")) tags.add(role);
                if (role.equals("heavy")) tags.add("kujira_target");
                if (rarity==CanonicalRarity.FIVE_STAR) tags.add("legendary");
                // Isolated target sensitivity ONLY; the shipped boss tag is empty.
                if (hypotheticalBoss && role.equals("heavy") && rarity==CanonicalRarity.FIVE_STAR) tags.add("boss");
                pool.add(species(rarity.stars()+"_"+role,rarity,tags,Math.pow(.5,rarity.stars()-1),1,1));
            }
        }
        return List.copyOf(pool);
    }
    static SpeciesProfile species(String id, CanonicalRarity rarity, Set<String> tags, double weight, double strength, double tempo) {
        return new SpeciesProfile("balance:"+id,rarity,weight,SpeciesEligibility.always(),strength,tempo,"steady",
                new LogNormalSizeDistribution(30,.4),Set.of(),Map.of(),tags);
    }
    static FishingSimulator.Config config(long seed,int samples,double nativeLuck,double perfectRate) {
        return new FishingSimulator.Config(seed,samples,nativeLuck,0,perfectRate,Optional.empty(),false);
    }
    /** Exact enumeration of Tide 2.1.1's 401 water wait draws; see report's bytecode provenance. */
    static double expectedWait(int lure) {
        int reduction = (int)(2000/(1+Math.exp(-.2*lure))-1000);
        double total=0;
        for(int draw=200;draw<=600;draw++) total+=Math.max(10,draw-reduction);
        return total/401;
    }
}
