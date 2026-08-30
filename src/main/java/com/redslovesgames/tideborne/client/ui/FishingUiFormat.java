package com.redslovesgames.tideborne.client.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.OptionalInt;

/** Canonical player-facing formatting for Fishing System 2.0 display data. */
public final class FishingUiFormat {
    public static final String UNAVAILABLE = "N/A";
    private static final DateTimeFormatter CATCH_DATE = DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.US);

    private FishingUiFormat() {
    }

    public static String fishScore(OptionalInt score) {
        return score != null && score.isPresent() && score.getAsInt() > 0
                ? Integer.toString(score.getAsInt())
                : UNAVAILABLE;
    }

    public static String fishScore(int score) {
        return score > 0 ? Integer.toString(score) : UNAVAILABLE;
    }

    public static String percentile(double percentile) {
        return Double.isFinite(percentile) && percentile >= 0.0 && percentile <= 100.0
                ? String.format(Locale.ROOT, "P%.1f", percentile)
                : UNAVAILABLE;
    }

    public static String length(double centimeters) {
        if (!Double.isFinite(centimeters) || centimeters <= 0.0) {
            return UNAVAILABLE;
        }
        return centimeters < 100.0
                ? String.format(Locale.ROOT, "%.1f cm", centimeters)
                : String.format(Locale.ROOT, "%.2f m", centimeters / 100.0);
    }

    public static String trait(Enum<?> value) {
        return value == null ? UNAVAILABLE : trait(value.name());
    }

    public static String trait(String value) {
        if (value == null || value.isBlank()) {
            return UNAVAILABLE;
        }
        StringBuilder output = new StringBuilder();
        for (String part : value.trim().toLowerCase(Locale.ROOT).replace('-', '_').split("_")) {
            if (!part.isBlank()) {
                if (!output.isEmpty()) {
                    output.append(' ');
                }
                output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return output.isEmpty() ? UNAVAILABLE : output.toString();
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
