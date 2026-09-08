package com.redslovesgames.tideborne.client.ui;

import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Client-specific Fishing System 2.0 formatting helpers.
 *
 * <p>Canonical specimen presentation semantics live in {@link CanonicalSpecimenPresentation}.
 * Specimen-facing code must consume that layer directly rather than using a second formatting facade.
 */
public final class FishingUiFormat {
    private static final DateTimeFormatter CATCH_DATE = DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.US);

    private FishingUiFormat() {
    }

    public static String timestamp(long epochMillis) {
        return timestamp(epochMillis, ZoneId.systemDefault());
    }

    public static String timestamp(long epochMillis, ZoneId zone) {
        return epochMillis > 0L && zone != null
                ? CATCH_DATE.format(Instant.ofEpochMilli(epochMillis).atZone(zone))
                : CanonicalSpecimenPresentation.UNAVAILABLE;
    }
}
