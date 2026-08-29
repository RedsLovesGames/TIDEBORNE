package com.redslovesgames.tideteamjournal.client;

import com.redslovesgames.tideteamjournal.StoredFishScoreStorage;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;

/** Read-only UI projection for persisted team history/top-fish canonical metadata. */
public record CanonicalRecordDisplay(
        OptionalInt score,
        String bodyType,
        String condition,
        String pigmentation,
        String quality,
        double percentile,
        double length
) {
    public static Optional<CanonicalRecordDisplay> from(NbtCompound tag) {
        if (tag == null) {
            return Optional.empty();
        }
        OptionalInt score = StoredFishScoreStorage.readCanonical(tag);
        String body = normalized(tag.getString("body_type"));
        String condition = normalized(tag.getString("condition"));
        String pigmentation = normalized(tag.getString("pigmentation"));
        String quality = normalized(tag.getString("quality"));
        double percentile = tag.contains("percentile", 99) ? tag.getDouble("percentile") : Double.NaN;
        double length = tag.contains("length", 99) ? tag.getDouble("length")
                : (tag.contains("new_size", 99) ? tag.getDouble("new_size") : Double.NaN);
        if (score.isEmpty() && body.isEmpty() && condition.isEmpty() && pigmentation.isEmpty() && quality.isEmpty()
                && !Double.isFinite(percentile) && !Double.isFinite(length)) {
            return Optional.empty();
        }
        return Optional.of(new CanonicalRecordDisplay(score, body, condition, pigmentation, quality, percentile, length));
    }

    public String scoreLabel() {
        return score.isPresent() ? Integer.toString(score.getAsInt()) : "--";
    }

    public String bodyTypeLabel() {
        return label(bodyType);
    }

    public String conditionLabel() {
        return label(condition);
    }

    public String pigmentationLabel() {
        return label(pigmentation);
    }

    public String qualityLabel() {
        return label(quality);
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String label(String value) {
        if (value == null || value.isBlank()) {
            return "--";
        }
        StringBuilder output = new StringBuilder();
        for (String part : value.replace('-', '_').split("_")) {
            if (part.isBlank()) {
                continue;
            }
            if (!output.isEmpty()) {
                output.append(' ');
            }
            output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return output.toString();
    }
}
