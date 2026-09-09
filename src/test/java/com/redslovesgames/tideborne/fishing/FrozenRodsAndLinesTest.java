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
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class FrozenRodsAndLinesTest {
    @Test
    void allRodIdentitiesHaveExactHandlingAndNoDuplicatedNativeLuck() {
        assertRod("minecraft:fishing_rod", 1, 1, 1, 0, 0);
        assertRod("tide:iron_fishing_rod", 1, 1, 1.04, 0.05, 0);
        assertRod("tide:golden_fishing_rod", 1, 1, 0.97, 0.02, 0);
        assertRod("tide:diamond_fishing_rod", 0.92, 1, 1, 0.10, 0.25);
        assertRod("tide:netherite_fishing_rod", 1, 0.95, 1, 0.15, 0.10);
        assertRod("tidebound_compatibility:kujira_bone_fishing_rod", 0.88, 1.10, 1, 0, 0);
        assertEquals(0, GOLD_ROD.rodModifiers().fishingLuck());
    }

    @Test
    void diamondHandlesTrophyStrengthBetterWhileNetheriteProtectsMore() {
        var fights = new FightProfileService();
        var baseline = new FightProfile(1, 0.10, fights.catchZoneArea(1), "steady");
        var trophy = new FightProfile(1.4, 0.11, fights.catchZoneArea(1.4), "steady");
        var diamond = DIAMOND_ROD.rodModifiers();
        var netherite = NETHERITE_ROD.rodModifiers();
        assertEquals(1.30 * 0.92, fights.applyGearModifiers(fights.applyTrophyRelief(baseline, trophy, diamond), diamond).strength(), 1e-12);
        assertEquals(1.36, fights.applyGearModifiers(fights.applyTrophyRelief(baseline, trophy, netherite), netherite).strength(), 1e-12);
        assertTrue(FishingGearEffects.catchLossPreventionChance(netherite) > FishingGearEffects.catchLossPreventionChance(diamond));
    }

    @Test
    void everyLineMatchesFrozenTableAndComposesWithRod() {
        assertLine(TIDE_BASE_LINE, 1, 1, 1, 1);
        assertLine(TIDE_COPPER_LINE, 1, 0.94, 1.02, 1);
        assertLine(TIDE_IRON_LINE, 0.92, 1, 1.03, 1);
        assertLine(TIDE_GOLDEN_LINE, 1.05, 0.86, 1, 1);
        assertLine(TIDE_DIAMOND_LINE, 0.82, 1.06, 1, 1);
        assertLine(SWIFT_LINE, 1, 1, 1.16, 1.08);
        assertLine(TENTACLE_LINE, 1, 1, 1.32, 1.16);
        var composed = FishingGearModifiers.compose(DIAMOND_ROD.rodModifiers(), TideFishingLineModifiers.forProfile(TIDE_DIAMOND_LINE));
        assertEquals(0.92 * 0.82, FishingGearEffects.strengthMultiplier(composed), 1e-12);
        assertEquals(1.06, FishingGearEffects.tempoMultiplier(composed));
    }

    @Test
    void kujiraTargetsOnlyClassifiedSpeciesAndRetainsCrateTradeoff() {
        var gear = TideborneFishingGearModifiers.rod(KUJIRA_BONE_FISHING_ROD, true);
        assertEquals(1.35, FishingGearEffects.targetWeightMultiplier(species(Set.of("kujira_target")), FishingEnvironment.empty(), gear));
        assertEquals(1, FishingGearEffects.targetWeightMultiplier(species(Set.of()), FishingEnvironment.empty(), gear));
        assertEquals(0.70, FishingGearEffects.crateWeightMultiplier(gear));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.rod(KUJIRA_BONE_FISHING_ROD, false));
        var disabled = new TideboundConfig.Values();
        disabled.enableMythsCompat = false;
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.swiftLine(disabled));
        assertEquals(FishingGearModifiers.neutral(), TideborneFishingGearModifiers.tentacleLine(disabled));
    }

    private static void assertRod(String id, double strength, double tempo, double zone, double prevention, double relief) {
        var profile = FishingGearRegistry.resolveId(Identifier.of(id)).orElseThrow();
        assertEquals(FishingGearRegistry.Slot.ROD, profile.slot());
        var gear = TideborneFishingGearModifiers.rod(profile, true);
        assertEquals(strength, gear.strengthMultiplier());
        assertEquals(tempo, gear.tempoMultiplier());
        assertEquals(zone, FishingGearEffects.catchZoneAreaMultiplier(gear));
        assertEquals(prevention, FishingGearEffects.catchLossPreventionChance(gear));
        assertEquals(relief, FishingGearEffects.trophyFightRelief(gear));
        assertEquals(0, gear.fishingLuck());
        assertEquals(0, gear.traitLuck());
        for (var body : SpecimenData.BodyType.values()) assertEquals(1, gear.bodyTypeChanceMultiplier(body));
    }

    private static void assertLine(FishingGearRegistry.GearProfile profile, double strength, double tempo, double zone, double speed) {
        assertEquals(profile, FishingGearRegistry.resolveId(profile.itemId()).orElseThrow());
        var config = new TideboundConfig.Values();
        var gear = profile == SWIFT_LINE ? TideborneFishingGearModifiers.swiftLine(config)
                : profile == TENTACLE_LINE ? TideborneFishingGearModifiers.tentacleLine(config)
                : TideFishingLineModifiers.forProfile(profile);
        assertEquals(strength, gear.strengthMultiplier());
        assertEquals(tempo, gear.tempoMultiplier());
        assertEquals(zone, FishingGearEffects.catchZoneAreaMultiplier(gear));
        assertEquals(speed, FishingGearEffects.minigameSpeedMultiplier(gear));
    }

    private static SpeciesProfile species(Set<String> tags) {
        return new SpeciesProfile("tide:test", CanonicalRarity.THREE_STAR, 1, SpeciesEligibility.always(),
                1, 1, "steady", new LogNormalSizeDistribution(25, 0.4), Set.of(), Map.of(), tags);
    }
}
