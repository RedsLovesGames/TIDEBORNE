package com.redslovesgames.tideborne.presentation;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Canonical player-facing presentation semantics for Fishing System 2.0 specimens.
 *
 * <p>This is deliberately a read-only projection over {@link SpecimenData}. Screens, tooltips,
 * commands, and other presentation consumers should use this class instead of rebuilding trait
 * names, trait order, colors, size/percentile labels, FishScore labels, or rarity stars locally.
 */
public final class CanonicalSpecimenPresentation {
    public static final String UNAVAILABLE = "N/A";

    public static final int BODY_TYPE_COLOR = 0xB36CE2;
    public static final int CONDITION_COLOR = 0xD36B5D;
    public static final int PIGMENTATION_COLOR = 0x4FAFD6;
    public static final int QUALITY_COLOR = 0xD6A94F;
    public static final int SCORE_COLOR = 0x43A8D8;

    private CanonicalSpecimenPresentation() {
    }

    /** Stable display order, naming, abbreviation, and color for every canonical specimen trait axis. */
    public enum TraitAxis {
        BODY_TYPE("Body Type", "Body", BODY_TYPE_COLOR),
        CONDITION("Condition", "Cond", CONDITION_COLOR),
        PIGMENTATION("Pigmentation", "Pig", PIGMENTATION_COLOR),
        QUALITY("Quality", "Qual", QUALITY_COLOR);

        private final String label;
        private final String shortLabel;
        private final int color;

        TraitAxis(String label, String shortLabel, int color) {
            this.label = label;
            this.shortLabel = shortLabel;
            this.color = color;
        }

        public String label() {
            return label;
        }

        public String shortLabel() {
            return shortLabel;
        }

        public int color() {
            return color;
        }
    }

    /** One trait value in canonical display order with its shared label and semantic color. */
    public record TraitDisplay(TraitAxis axis, String value) {
        public TraitDisplay {
            Objects.requireNonNull(axis, "axis");
            value = value == null || value.isBlank() ? UNAVAILABLE : value;
        }

        public String label() {
            return axis.label();
        }

        public String shortLabel() {
            return axis.shortLabel();
        }

        public int color() {
            return axis.color();
        }
    }

    /** Complete read-only specimen presentation used by UI and inspection surfaces. */
    public record View(
            String speciesId,
            String fishScore,
            String percentile,
            String length,
            String rarityStars,
            List<TraitDisplay> traits,
            boolean perfectCatch
    ) {
        public View {
            Objects.requireNonNull(speciesId, "speciesId");
            fishScore = normalizedLabel(fishScore);
            percentile = normalizedLabel(percentile);
            length = normalizedLabel(length);
            rarityStars = normalizedLabel(rarityStars);
            traits = List.copyOf(Objects.requireNonNull(traits, "traits"));
        }

        public TraitDisplay trait(TraitAxis axis) {
            return traitForAxis(traits, axis);
        }
    }

    public static View present(SpecimenData specimen, CanonicalRarity rarity) {
        Objects.requireNonNull(rarity, "rarity");
        return present(specimen, rarity.stars());
    }

    public static View present(SpecimenData specimen, int rarityStars) {
        Objects.requireNonNull(specimen, "specimen");
        return new View(
                specimen.speciesId(),
                fishScore(specimen.fishScore()),
                percentile(specimen.finalPercentile()),
                length(specimen.finalLength()),
                rarityStars(rarityStars),
                traits(specimen),
                specimen.perfectCatch()
        );
    }

    public static List<TraitDisplay> traits(SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        return traits(specimen.bodyType(), specimen.condition(), specimen.pigmentation(), specimen.specimenQuality());
    }

    /**
     * Builds the same canonical trait projection for synchronized display records that intentionally
     * carry only presentation-safe specimen fields rather than a full {@link SpecimenData} record.
     */
    public static List<TraitDisplay> traits(
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality quality
    ) {
        return List.of(
                new TraitDisplay(TraitAxis.BODY_TYPE, trait(bodyType)),
                new TraitDisplay(TraitAxis.CONDITION, trait(condition)),
                new TraitDisplay(TraitAxis.PIGMENTATION, trait(pigmentation)),
                new TraitDisplay(TraitAxis.QUALITY, trait(quality))
        );
    }

    /**
     * Canonical trait projection for historical synchronized records that only contain string values.
     * Keeping this compatibility path here prevents Journal/record UIs from reconstructing labels,
     * ordering, or semantic colors themselves.
     */
    public static List<TraitDisplay> traits(
            String bodyType,
            String condition,
            String pigmentation,
            String quality
    ) {
        return List.of(
                new TraitDisplay(TraitAxis.BODY_TYPE, trait(bodyType)),
                new TraitDisplay(TraitAxis.CONDITION, trait(condition)),
                new TraitDisplay(TraitAxis.PIGMENTATION, trait(pigmentation)),
                new TraitDisplay(TraitAxis.QUALITY, trait(quality))
        );
    }

    /** Canonical four-axis placeholder projection for missing/legacy specimen presentation. */
    public static List<TraitDisplay> unavailableTraits() {
        return traits((String)null, null, null, null);
    }

    public static TraitDisplay traitForAxis(List<TraitDisplay> traits, TraitAxis axis) {
        Objects.requireNonNull(traits, "traits");
        Objects.requireNonNull(axis, "axis");
        return traits.stream()
                .filter(value -> value.axis() == axis)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing trait axis: " + axis));
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

    public static String rarityStars(CanonicalRarity rarity) {
        return rarity == null ? UNAVAILABLE : rarityStars(rarity.stars());
    }

    public static String rarityStars(int stars) {
        return stars >= 1 && stars <= 5 ? "★".repeat(stars) : UNAVAILABLE;
    }

    private static String normalizedLabel(String value) {
        return value == null || value.isBlank() ? UNAVAILABLE : value;
    }
}
