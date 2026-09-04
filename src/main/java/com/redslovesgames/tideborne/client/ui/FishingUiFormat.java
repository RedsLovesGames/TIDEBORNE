package com.redslovesgames.tideborne.client.ui;

import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.OptionalInt;

/**
 * Compatibility facade for existing Fishing System 2.0 UI formatting call sites.
 *
 * <p>Canonical specimen presentation semantics live in {@link CanonicalSpecimenPresentation}.
 * New specimen-facing code should consume that layer directly.
 */
public final class FishingUiFormat {
    public static final String UNAVAILABLE = CanonicalSpecimenPresentation.UNAVAILABLE;
    private static final DateTimeFormatter CATCH_DATE = DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.US);

    private FishingUiFormat() {
    }

    public static String fishScore(OptionalInt score) {
        return CanonicalSpecimenPresentation.fishScore(score);
    }

    public static String fishScore(int score) {
        return CanonicalSpecimenPresentation.fishScore(score);
    }

    public static String percentile(double percentile) {
        return CanonicalSpecimenPresentation.percentile(percentile);
    }

    public static String length(double centimeters) {
        return CanonicalSpecimenPresentation.length(centimeters);
    }

    public static String trait(Enum<?> value) {
        return CanonicalSpecimenPresentation.trait(value);
    }

    public static String trait(String value) {
        return CanonicalSpecimenPresentation.trait(value);
    }

    public static String timestamp(long epochMillis) {
        return timestamp(epochMillis, ZoneId.systemDefault());
    }

    public static String timestamp(long epochMillis, ZoneId zone) {
        return epochMillis > 0L && zone != null
                ? CATCH_DATE.format(Instant.ofEpochMilli(epochMillis).atZone(zone))
                : UNAVAILABLE;
    }
}
