package com.redslovesgames.tideborne.presentation.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class TideboundTooltipsTest {
    @Test
    void neutralColoredBobberAddsNoTideborneEffectLines() {
        assertEquals(List.of(), effects("red_bobber"));
    }

    @Test
    void duckBobberShowsOnlyItsTideborneCrateEffect() {
        assertEquals(List.of("Crate weight: 110%"), effects("duck_bobber"));
    }

    @Test
    void diamondBobberShowsCatchZoneAndProtection() {
        assertEquals(
                List.of("Catch zone: 108%", "Catch-loss protection: 10.0%"),
                effects("diamond_bobber"));
    }

    @Test
    void goldenAppleBobberShowsLuckAndLureWithoutInventedStats() {
        assertEquals(
                List.of("Fishing Luck: +2", "Lure bonus: +2"),
                effects("golden_apple_bobber"));
    }

    private static List<String> effects(String path) {
        return TideboundTooltips.bobberEffectLabels(
                FishingGearRegistry.bobberModifiers(Identifier.of("tide", path)).orElseThrow());
    }
}
