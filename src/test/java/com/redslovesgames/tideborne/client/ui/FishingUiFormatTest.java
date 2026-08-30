package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class FishingUiFormatTest {
    @Test
    void formatsCanonicalPlayerFacingValues() {
        assertEquals("2777", FishingUiFormat.fishScore(OptionalInt.of(2777)));
        assertEquals("P97.7", FishingUiFormat.percentile(97.7));
        assertEquals("89.9 cm", FishingUiFormat.length(89.9));
        assertEquals("1.72 m", FishingUiFormat.length(171.6));
        assertEquals("Perfect Specimen", FishingUiFormat.trait("perfect_specimen"));
    }

    @Test
    void invalidSentinelsAndMissingTraitsAreUnavailable() {
        assertEquals("N/A", FishingUiFormat.fishScore(OptionalInt.empty()));
        assertEquals("N/A", FishingUiFormat.fishScore(-1));
        assertEquals("N/A", FishingUiFormat.percentile(-1.0));
        assertEquals("N/A", FishingUiFormat.length(Double.NaN));
        assertEquals("N/A", FishingUiFormat.trait((String) null));
    }

    @Test
    void catchTimestampFormattingIsDeterministicForAChosenZone() {
        long timestamp = Instant.parse("2026-08-21T04:36:00Z").toEpochMilli();
        assertEquals("8/21/26, 4:36 AM", FishingUiFormat.timestamp(timestamp, ZoneOffset.UTC));
        assertEquals("N/A", FishingUiFormat.timestamp(-1L, ZoneOffset.UTC));
    }
}
