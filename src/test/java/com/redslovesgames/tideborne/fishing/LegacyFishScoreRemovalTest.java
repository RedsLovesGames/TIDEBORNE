package com.redslovesgames.tideborne.fishing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.specimen.legacy.TraitAxesRuntime;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class LegacyFishScoreRemovalTest {
    @Test
    void reconstructedLegacyCalculatorStaysDisabledWithoutSelfMixin() throws IOException {
        String mixins = resource("/tideborne.mixins.json");
        assertFalse(mixins.contains("LegacyFishScoreCalculatorMixin"));
        assertEquals(-1.0, TraitAxesRuntime.score(null, 5, 99.0, 50.0, 40.0));
        assertEquals(-1.0, TraitAxesRuntime.scoreFromParts(99.0, 5, "scarred", "giant", 50.0, 40.0));
    }

    @Test
    void teamJournalCanonicalScorePayloadMixinsAreRegistered() throws IOException {
        String mixins = resource("/tideborne.mixins.json");
        assertTrue(mixins.contains("TeamProgressCanonicalJournalMixin"));
        assertTrue(mixins.contains("ContributorCanonicalScorePayloadMixin"));
        assertTrue(mixins.contains("RecordEventCanonicalScorePayloadMixin"));
    }

    private static String resource(String name) throws IOException {
        try (InputStream stream = LegacyFishScoreRemovalTest.class.getResourceAsStream(name)) {
            assertNotNull(stream, "missing test resource " + name);
            return new String(stream.readAllBytes(), UTF_8);
        }
    }
}
