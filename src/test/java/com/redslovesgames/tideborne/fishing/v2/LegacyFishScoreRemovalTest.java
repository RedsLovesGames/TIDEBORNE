package com.redslovesgames.tideborne.fishing.v2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class LegacyFishScoreRemovalTest {
    @Test
    void reconstructedLegacyCalculatorIsGuardedByProductionMixin() throws IOException {
        String mixins = resource("/tide_traits.mixins.json");
        assertTrue(mixins.contains("LegacyFishScoreCalculatorMixin"));
    }

    @Test
    void teamJournalCanonicalScorePayloadMixinsAreRegistered() throws IOException {
        String mixins = resource("/tide_team_journal.mixins.json");
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
