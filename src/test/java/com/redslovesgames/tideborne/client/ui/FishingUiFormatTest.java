package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class FishingUiFormatTest {
    @Test
    void catchTimestampFormattingIsDeterministicForAChosenZone() {
        long timestamp = Instant.parse("2026-08-21T04:36:00Z").toEpochMilli();
        assertEquals("8/21/26, 4:36 AM", FishingUiFormat.timestamp(timestamp, ZoneOffset.UTC));
        assertEquals("N/A", FishingUiFormat.timestamp(-1L, ZoneOffset.UTC));
    }
}
