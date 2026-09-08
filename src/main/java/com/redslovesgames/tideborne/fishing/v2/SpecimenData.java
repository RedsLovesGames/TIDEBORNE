package com.redslovesgames.tideborne.fishing.v2;

import java.util.Collections;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeMap;

/** Canonical immutable specimen identity. Downstream consumers must not reroll these fields. */
public record SpecimenData(
        String speciesId,
        int schemaVersion,
        int generationVersion,
        long deterministicSeed,
        double basePercentile,
        double baseLength,
        double finalLength,
        double finalPercentile,
        BodyType bodyType,
        Condition condition,
        Pigmentation pigmentation,
        SpecimenQuality specimenQuality,
        boolean perfectCatch,
        OptionalDouble rawFishScore,
        OptionalInt fishScore,
        Provenance provenance
) {
    public SpecimenData {
        if (speciesId == null || speciesId.isBlank() || !speciesId.contains(":")) {
            throw new IllegalArgumentException("speciesId must be a namespaced ID");
        }
        if (schemaVersion <= 0 || generationVersion <= 0) {
            throw new IllegalArgumentException("schema and generation versions must be positive");
        }
        validatePercentile("basePercentile", basePercentile);
        validatePercentile("finalPercentile", finalPercentile);
        validateLength("baseLength", baseLength);
        validateLength("finalLength", finalLength);
        if (bodyType == null || condition == null || pigmentation == null || specimenQuality == null) {
            throw new IllegalArgumentException("all specimen axes are required");
        }
        rawFishScore = rawFishScore == null ? OptionalDouble.empty() : rawFishScore;
        fishScore = fishScore == null ? OptionalInt.empty() : fishScore;
        if (rawFishScore.isPresent() && (!Double.isFinite(rawFishScore.getAsDouble()) || rawFishScore.getAsDouble() < 0.0)) {
            throw new IllegalArgumentException("rawFishScore must be finite and nonnegative when present");
        }
        if (fishScore.isPresent() && (fishScore.getAsInt() < 1 || fishScore.getAsInt() > 3000)) {
            throw new IllegalArgumentException("fishScore must be between 1 and 3000 when present");
        }
        provenance = provenance == null ? Provenance.generated() : provenance;
    }

    public enum BodyType {
        NORMAL,
        GIANT,
        DWARF
    }

    public enum Condition {
        NORMAL,
        SCARRED,
        PARASITE_RIDDEN
    }

    public enum Pigmentation {
        NORMAL,
        ALBINO,
        IRIDESCENT
    }

    public enum SpecimenQuality {
        NORMAL,
        PERFECT_SPECIMEN
    }

    public record Provenance(String source, String generator, Map<String, String> attributes) {
        public Provenance {
            if (source == null || source.isBlank() || generator == null || generator.isBlank()) {
                throw new IllegalArgumentException("provenance source and generator are required");
            }
            attributes = attributes == null || attributes.isEmpty()
                    ? Map.of()
                    : Collections.unmodifiableMap(new TreeMap<>(attributes));
        }

        public static Provenance generated() {
            return new Provenance("new-catch", "fishing-system-2", Map.of());
        }
    }

    private static void validatePercentile(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException(name + " must be between 0 and 100");
        }
    }

    private static void validateLength(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and nonnegative");
        }
    }
}

