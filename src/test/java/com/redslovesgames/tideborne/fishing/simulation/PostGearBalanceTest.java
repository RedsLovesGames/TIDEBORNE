package com.redslovesgames.tideborne.fishing.simulation;

import com.redslovesgames.tideborne.fishing.*;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostGearBalanceTest {
    @Test void pairedNaturalRollsRemainExactWhileCanonicalTraitsChange() {
        var pool=List.of(PostGearBalanceScenarios.pool(false).getFirst());
        var simulator=new FishingSimulator();
        var cfg=PostGearBalanceScenarios.config(PostGearBalanceScenarios.SEEDS[0],3000,0,0);
        var base=simulator.simulate(pool,FishingEnvironment.empty(),cfg);
        var traits=simulator.simulate(pool,FishingEnvironment.empty(),cfg,FishingGearModifiers.builder().traitLuck(2).build());
        assertEquals(base.naturalPercentiles(),traits.naturalPercentiles());
        assertTrue(traits.fishScores().min()>=1 && traits.fishScores().max()<=3000);
        assertEquals(traits,simulator.simulate(pool,FishingEnvironment.empty(),cfg,FishingGearModifiers.builder().traitLuck(2).build()));
    }
    @Test void targetingShiftsNormalizedShareAndRespectsCombinedCap() {
        var pool=PostGearBalanceScenarios.pool(true);
        var selector=new SpeciesSelectionService();
        for(var scenario:PostGearBalanceScenarios.builds()) for(var species:pool)
            assertTrue(FishingGearEffects.speciesWeightMultiplier(species,FishingEnvironment.empty(),scenario.gear(),scenario.nativeLuck())<=3);
        var gear=PostGearBalanceScenarios.ablations().getFirst().gear();
        var simulator=new FishingSimulator();var cfg=PostGearBalanceScenarios.config(77,4000,0,0);
        var baseline=simulator.simulate(pool,FishingEnvironment.empty(),cfg);
        var targeted=simulator.simulate(pool,FishingEnvironment.empty(),cfg,gear);
        assertTrue(share(targeted,"heavy")>share(baseline,"heavy")+.10);
        assertTrue(share(targeted,"very_small")<share(baseline,"very_small")-.05);
        var context=new FishingContext(0,0,0,Map.of(),Map.of(),Map.of());
        var weights=selector.eligibleSpecies(pool,context,FishingEnvironment.empty(),gear);
        for(int i=0;i<50;i++) assertEquals(selector.select(pool,context,FishingEnvironment.empty(),gear,new SplittableRandom(i)),
                selector.selectWeighted(weights,new SplittableRandom(i)));
    }
    @Test void nativeTimingIsBoundedAndSwiftDoesNotAddLure() {
        assertEquals(400,PostGearBalanceScenarios.expectedWait(0));
        assertEquals(301,PostGearBalanceScenarios.expectedWait(1));
        assertTrue(PostGearBalanceScenarios.expectedWait(5)<40);
        assertEquals(10,PostGearBalanceScenarios.expectedWait(100));
        assertEquals(0,com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.swiftLine(PostGearBalanceScenarios.CONFIG)
                .namedAdditiveModifier(FishingGearEffects.LURE_BONUS));
    }
    static double share(FishingSimulator.Result result,String role) {
        return result.speciesCounts().entrySet().stream().filter(e->e.getKey().endsWith("_"+role)).mapToLong(Map.Entry::getValue).sum()/(double)result.catchCount();
    }
}
