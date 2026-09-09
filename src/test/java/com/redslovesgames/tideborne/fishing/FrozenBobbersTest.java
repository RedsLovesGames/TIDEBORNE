package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;

import com.redslovesgames.tideborne.fishing.gear.BobberGearModifiers;
import com.redslovesgames.tideborne.journal.BobberBonuses;
import com.redslovesgames.tideborne.journal.ServerConfig;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FrozenBobbersTest {
    @Test void all32ProfilesMatchFrozenTableAndConfiguredProjection() {
        Map<String,double[]> table = new LinkedHashMap<>();
        for (String dye : List.of("red","orange","yellow","lime","green","cyan","light_blue","blue","purple","magenta","pink","white","light_gray","gray","black","brown"))
            table.put(dye, new double[]{0,0,1,1,1,0});
        String[] names={"golden","golden_apple","enchanted_golden_apple","iron","diamond","netherite","lichen","grassy","amethyst","echo","feather","chorus","duck","nautilus","heart","pearl"};
        double[][] values={{2,0,0,1,1,0},{2,0,2,1,1,0},{5,0,0,1,1,0},{0,0,0,1.05,1,.05},{0,0,0,1.08,1,.1},{0,0,1,1.1,1,.15},{0,0,1,1.03,1,0},{0,0,0,1.04,1,0},{0,1,1,1,1,0},{0,2,1,.96,1,0},{0,0,2,1,1,0},{0,0,3,.94,1,0},{0,0,0,1,1.1,0},{1,0,0,1,1.25,0},{0,0,0,.95,1.4,0},{1,0,2,1,1,0}};
        for(int i=0;i<names.length;i++) table.put(names[i],values[i]);
        var ids=new HashSet<Identifier>();
        table.forEach((name, expected)->{
            var id=Identifier.of("tide",name+"_bobber"); ids.add(id);
            var gear=BobberGearModifiers.forId(id,true,new ServerConfig.Values());
            assertArrayEquals(expected,new double[]{gear.fishingLuck(),gear.traitLuck(),gear.namedAdditiveModifier(FishingGearEffects.LURE_BONUS),FishingGearEffects.catchZoneAreaMultiplier(gear),FishingGearEffects.crateWeightMultiplier(gear),FishingGearEffects.catchLossPreventionChance(gear)},1e-12,name);
            assertEquals(FishingGearRegistry.bobberModifiers(id).orElseThrow(),gear);
            assertTrue(gear.bodyTypeChanceMultipliers().isEmpty());
            assertTrue(gear.catchPoolRestriction().allows("fish"));
        });
        assertEquals(32,ids.size()); assertEquals(ids,FishingGearRegistry.supportedBobberIds());
    }
    @Test void thirdPartyFallbackAndServerOverridesDoNotAddTwice() {
        var config=new ServerConfig.Values(); var id=Identifier.of("thirdparty","heart_bobber");
        config.fallbackBobberBonus=new BobberBonuses.Bonus(2,3);
        var gear=BobberGearModifiers.forId(id,true,config);
        assertEquals(2,gear.fishingLuck()); assertEquals(3,gear.namedAdditiveModifier(FishingGearEffects.LURE_BONUS));
        assertEquals(1,FishingGearEffects.crateWeightMultiplier(gear)); assertEquals(0,gear.traitLuck());
        assertEquals(FishingGearModifiers.neutral(),BobberGearModifiers.forId(id,false,config));
        var golden=Identifier.of("tide","golden_bobber"); config.bobberBonuses.put(golden.toString(),new BobberBonuses.Bonus(7,4));
        assertEquals(7,BobberGearModifiers.forId(golden,true,config).fishingLuck());
        config.bobberBonusesEnabled=false;
        assertEquals(0,BobberGearModifiers.forId(golden,true,config).fishingLuck());
        assertEquals(0,BobberGearModifiers.forId(id,true,config).fishingLuck());
    }
    @Test void traitLuckIsOnlyACanonicalGenerationInput() {
        var gear=BobberGearModifiers.forId(Identifier.of("tide","echo_bobber"),true,new ServerConfig.Values());
        assertEquals(2,gear.traitLuck());
        assertEquals(Set.of(FishingGearEffects.LURE_BONUS,FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE,FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES),gear.namedAdditiveModifiers().keySet());
        var species=new SpeciesProfile("tide:fixture",CanonicalRarity.THREE_STAR,1,SpeciesEligibility.always(),1,1,"steady",new LogNormalSizeDistribution(25,.4),Set.of(),Map.of());
        var specimen=new SpecimenGenerator().generate(species,91,SpecimenData.Provenance.generated(),0);
        var original=specimen.toString(); new FightProfileService().create(species,specimen,gear);
        assertEquals(original,specimen.toString()); assertTrue(gear.bodyTypeChanceMultipliers().isEmpty());
    }
}
