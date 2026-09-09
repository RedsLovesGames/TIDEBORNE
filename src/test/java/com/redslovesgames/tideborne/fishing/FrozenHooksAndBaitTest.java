package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;

import static org.junit.jupiter.api.Assertions.*;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FrozenHooksAndBaitTest {
    @Test void allSevenHooksKeepLuckNeutralAndNativeEffectsNative() {
        for (var hook : new FishingGearRegistry.GearProfile[]{FIERY_HOOK, PERMAFROST_HOOK, TWILIGHT_HOOK,
                LAVAPROOF_HOOK, VOID_HOOK, SEAFARERS_HOOK, SHARK_TOOTH_HOOK}) {
            assertEquals(hook, FishingGearRegistry.resolveId(hook.itemId()).orElseThrow());
            var gear = targets(hook, true);
            assertEquals(0, gear.fishingLuck()); assertEquals(0, gear.traitLuck());
            if (hook.origin() == FishingGearRegistry.Origin.TIDE) assertEquals(FishingGearModifiers.neutral(), gear);
        }
    }
    @Test void seafarerAndSharkTargetsMatchCanonicalMetadataOnly() {
        assertEquals(1.35, weight(targets(SEAFARERS_HOOK, true), "legendary"));
        assertEquals(1, weight(targets(SEAFARERS_HOOK, false), "legendary"));
        assertEquals(1, weight(targets(SEAFARERS_HOOK, true), "heavy"));
        assertEquals(2, weight(targets(SHARK_TOOTH_HOOK, false), "heavy"));
        assertEquals(0.45, weight(targets(SHARK_TOOTH_HOOK, true), "very_small"));
        assertEquals(1, weight(targets(SHARK_TOOTH_HOOK, true), "ordinary"));
    }
    @Test void normalBaitsDoNotDuplicateTideWhileSpecialBaitsTargetOnce() {
        for(var bait : new FishingGearRegistry.GearProfile[]{NORMAL_BAIT, LUCKY_BAIT, MAGNETIC_BAIT, INCANDESCENT_BAIT, ABYSS_BAIT}) {
            assertEquals(bait, FishingGearRegistry.resolveId(bait.itemId()).orElseThrow());
            assertEquals(0, bait.baitTargetModifiers().fishingLuck());
            assertEquals(0, bait.baitTargetModifiers().traitLuck());
            assertEquals(1, FishingGearEffects.crateWeightMultiplier(bait.baitTargetModifiers()));
        }
        assertEquals(1.4, weight(INCANDESCENT_BAIT.baitTargetModifiers(), "warm"));
        assertEquals(1, weight(INCANDESCENT_BAIT.baitTargetModifiers(), "deep"));
        assertEquals(1.4, weight(ABYSS_BAIT.baitTargetModifiers(), "deep"));
        assertEquals(1, weight(ABYSS_BAIT.baitTargetModifiers(), "warm"));
    }
    @Test void targetQueriesPreserveCanonicalSpecimen() {
        var species = profile("warm");
        var specimen = new SpecimenGenerator().generate(species, 91, SpecimenData.Provenance.generated(), 0);
        var original = specimen.toString();
        new SpeciesSelectionService().adjustedWeight(species, 0, FishingEnvironment.empty(), INCANDESCENT_BAIT.baitTargetModifiers());
        assertEquals(original, specimen.toString());
    }
    private static FishingGearModifiers targets(FishingGearRegistry.GearProfile hook, boolean oceanNight) {
        return TideborneFishingGearModifiers.hookTargets(hook, oceanNight, true, true, new TideboundConfig.Values());
    }
    private static double weight(FishingGearModifiers gear, String tag) {
        return FishingGearEffects.targetWeightMultiplier(profile(tag), FishingEnvironment.empty(), gear);
    }
    private static SpeciesProfile profile(String tag) {
        return new SpeciesProfile("tide:fixture", CanonicalRarity.THREE_STAR, 1, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25, 0.4), Set.of(), Map.of(), Set.of(tag));
    }
}
