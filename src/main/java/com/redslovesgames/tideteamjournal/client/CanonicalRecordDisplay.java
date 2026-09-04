package com.redslovesgames.tideteamjournal.client;

import com.redslovesgames.tideborne.api.TideborneFishingApi;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
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

        Optional<SpecimenData> canonical = TideborneFishingApi.readTransferredSpecimen(tag);
        if (canonical.isPresent()) {
            return Optional.of(from(canonical.orElseThrow()));
        }

        // Compatibility-only fallback for historical record rows that predate the complete
        // canonical specimen transfer block. New records should always take the API path above.
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

    private static CanonicalRecordDisplay from(SpecimenData specimen) {
        return new CanonicalRecordDisplay(
                TideborneFishingApi.readFishScore(specimen),
                normalized(specimen.bodyType().name()),
                normalized(specimen.condition().name()),
                normalized(specimen.pigmentation().name()),
                normalized(specimen.specimenQuality().name()),
                specimen.finalPercentile(),
                specimen.finalLength()
        );
    }

    public String scoreLabel() {
        return CanonicalSpecimenPresentation.fishScore(score);
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
        return CanonicalSpecimenPresentation.trait(value);
    }
}
