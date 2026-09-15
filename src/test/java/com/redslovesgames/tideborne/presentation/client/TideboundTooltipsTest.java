package com.redslovesgames.tideborne.presentation.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class TideboundTooltipsTest {
    private static final Set<String> COLORED_BOBBERS = Set.of(
            "red_bobber",
            "orange_bobber",
            "yellow_bobber",
            "lime_bobber",
            "green_bobber",
            "cyan_bobber",
            "light_blue_bobber",
            "blue_bobber",
            "purple_bobber",
            "magenta_bobber",
            "pink_bobber",
            "white_bobber",
            "light_gray_bobber",
            "gray_bobber",
            "black_bobber",
            "brown_bobber");

    private static final Map<String, List<String>> EFFECT_BOBBERS = Map.ofEntries(
            Map.entry("golden_bobber", List.of("Fishing Luck: +2")),
            Map.entry("golden_apple_bobber", List.of("Fishing Luck: +2", "Lure Speed: +2")),
            Map.entry("enchanted_golden_apple_bobber", List.of("Fishing Luck: +5")),
            Map.entry("iron_bobber", List.of("Catch Zone: 105%", "Shark Protection: 5.0%")),
            Map.entry("diamond_bobber", List.of("Catch Zone: 108%", "Shark Protection: 10.0%")),
            Map.entry("netherite_bobber", List.of("Lure Speed: +1", "Catch Zone: 110%", "Shark Protection: 15.0%")),
            Map.entry("lichen_bobber", List.of("Lure Speed: +1", "Catch Zone: 103%")),
            Map.entry("grassy_bobber", List.of("Catch Zone: 104%")),
            Map.entry("amethyst_bobber", List.of("Trait Luck: +1", "Lure Speed: +1")),
            Map.entry("echo_bobber", List.of("Trait Luck: +2", "Lure Speed: +1", "Catch Zone: 96%")),
            Map.entry("feather_bobber", List.of("Lure Speed: +2")),
            Map.entry("chorus_bobber", List.of("Lure Speed: +3", "Catch Zone: 94%")),
            Map.entry("duck_bobber", List.of("Crates favored: 110%")),
            Map.entry("nautilus_bobber", List.of("Fishing Luck: +1", "Crates favored: 125%")),
            Map.entry("heart_bobber", List.of("Catch Zone: 95%", "Crates favored: 140%")),
            Map.entry("pearl_bobber", List.of("Fishing Luck: +1", "Lure Speed: +2")));

    @Test
    void allSupportedBobbersHaveIntentionalTidebornePresentation() {
        assertEquals(32, FishingGearRegistry.supportedBobberIds().size());
        assertEquals(32, COLORED_BOBBERS.size() + EFFECT_BOBBERS.size());

        for (String path : COLORED_BOBBERS) {
            assertEquals(List.of("Lure Speed: +1"), effects(path), path);
        }
        EFFECT_BOBBERS.forEach((path, expected) -> assertEquals(expected, effects(path), path));
    }

    private static List<String> effects(String path) {
        return TideboundTooltips.bobberEffectLabels(
                FishingGearRegistry.bobberModifiers(Identifier.of("tide", path)).orElseThrow());
    }
}
