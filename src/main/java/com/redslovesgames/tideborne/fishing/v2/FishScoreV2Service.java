package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/** Pure canonical Fishing System 2.0 FishScore calculation. */
public final class FishScoreV2Service {
    public static final double RAW_MIN = 50.0;
    public static final double RAW_MAX = 925.0;
    public static final int SCORE_MIN = 1;
    public static final int SCORE_MAX = 3000;

    /** Calculates both the canonical raw score and normalized 1..3000 FishScore. */
    public Result calculate(CanonicalRarity rarity, SpecimenData specimen) {
        Objects.requireNonNull(rarity, "rarity");
        Objects.requireNonNull(specimen, "specimen");

        double rawScore = speciesPoints(rarity)
                + 3.0 * specimen.finalPercentile()
                + conditionPoints(specimen.condition())
                + bodyTypePoints(specimen.bodyType())
                + pigmentationPoints(specimen.pigmentation())
                + qualityPoints(specimen.specimenQuality());

        return new Result(rawScore, normalize(rawScore));
    }

    /** Linearly maps the frozen raw-score range 50..925 onto 1..3000 and clamps the result. */
    public int normalize(double rawScore) {
        if (!Double.isFinite(rawScore)) {
            throw new IllegalArgumentException("rawScore must be finite");
        }
        double normalized = (rawScore - RAW_MIN) / (RAW_MAX - RAW_MIN);
        long rounded = Math.round(SCORE_MIN + (SCORE_MAX - SCORE_MIN) * normalized);
        return (int) Math.max(SCORE_MIN, Math.min(SCORE_MAX, rounded));
    }

    public int speciesPoints(CanonicalRarity rarity) {
        Objects.requireNonNull(rarity, "rarity");
        return switch (rarity) {
            case ONE_STAR -> 50;
            case TWO_STAR -> 100;
            case THREE_STAR -> 175;
            case FOUR_STAR -> 250;
            case FIVE_STAR -> 350;
        };
    }

    private static int conditionPoints(SpecimenData.Condition condition) {
        return switch (condition) {
            case NORMAL -> 0;
            case SCARRED -> 20;
            case PARASITE_RIDDEN -> 35;
        };
    }

    private static int bodyTypePoints(SpecimenData.BodyType bodyType) {
        return switch (bodyType) {
            case NORMAL -> 0;
            case GIANT, DWARF -> 40;
        };
    }

    private static int pigmentationPoints(SpecimenData.Pigmentation pigmentation) {
        return switch (pigmentation) {
            case NORMAL -> 0;
            case ALBINO -> 70;
            case IRIDESCENT -> 100;
        };
    }

    private static int qualityPoints(SpecimenData.SpecimenQuality quality) {
        return switch (quality) {
            case NORMAL -> 0;
            case PERFECT_SPECIMEN -> 100;
        };
    }

    public record Result(double rawScore, int fishScore) {}
}
