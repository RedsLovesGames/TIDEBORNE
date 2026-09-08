package com.redslovesgames.tideborne.fishing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class LegacySpeciesFightRemovalTest {
    @Test
    void legacyFishWeightMixinIsNotRegisteredButCanonicalSelectorIs() throws IOException {
        String mixins = resource("/tidebound_compatibility.mixins.json");

        assertFalse(mixins.contains("FishDataMixin"));
        assertTrue(mixins.contains("FishSelectorMixin"));
        assertTrue(mixins.contains("FishCatchMinigameMixin"));
    }

    @Test
    void legacyLineConstantRewriteIsNotRegistered() throws IOException {
        String mixins = resource("/tide_traits.mixins.json");

        assertFalse(mixins.contains("FishCatchMinigameLineModifierMixin"));
    }

    private static String resource(String name) throws IOException {
        try (InputStream stream = LegacySpeciesFightRemovalTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, "missing test resource " + name);
            return new String(stream.readAllBytes(), UTF_8);
        }
    }
}
