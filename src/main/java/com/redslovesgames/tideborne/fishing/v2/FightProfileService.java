package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/** Normalizes species fight inputs, then applies bounded specimen size, Body Type, and canonical gear effects. */
public final class FightProfileService {
    public static final double MAX_EXTERNAL_TEMPO = 2.2;
    public static final double MAX_CANONICAL_STRENGTH = 1.1;
    public static final double MIN_CATCH_ZONE_AREA = 0.12;
    public static final double MAX_CATCH_ZONE_AREA = 0.78;
    public static final double MIN_FINAL_TEMPO = 0.035;
    public static final double MAX_FINAL_TEMPO = 0.16;
    public static final double MIN_MINIGAME_CATCH_ZONE_AREA = 0.05;
    public static final double MAX_MINIGAME_CATCH_ZONE_AREA = 1.0;
    public static final double MIN_MINIGAME_SPEED = 0.05;
    public static final double GIANT_STRENGTH_MULTIPLIER = 1.08;
    public static final double GIANT_TEMPO_MULTIPLIER = 0.95;
    public static final double DWARF_STRENGTH_MULTIPLIER = 0.92;
    public static final double DWARF_TEMPO_MULTIPLIER = 1.08;

    public FightProfile create(SpeciesProfile species, SpecimenData specimen) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(specimen, "specimen");
        if (!species.speciesId().equals(specimen.speciesId())) {
            throw new IllegalArgumentException("species profile and specimen IDs must match");
        }

        double canonicalStrength = normalizeStrength(species.strength());
        double canonicalTempo = normalizeTempo(species.tempo());
        double strength = canonicalStrength * strengthMultiplier(specimen.finalPercentile());
        double tempo = clamp(
                canonicalTempo * tempoMultiplier(specimen.finalPercentile()),
                MIN_FINAL_TEMPO,
                MAX_FINAL_TEMPO
        );
        FightProfile percentileScaled = new FightProfile(
                strength,
                tempo,
                catchZoneArea(strength),
                species.behavior()
        );
        return applyBodyType(percentileScaled, specimen.bodyType());
    }

    /** Applies first-class canonical gear/bait fight multipliers after species, size, and Body Type scaling. */
    public FightProfile applyGearModifiers(FightProfile profile, FishingGearModifiers modifiers) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(modifiers, "modifiers");
        double strength = profile.strength() * modifiers.strengthMultiplier();
        double tempo = clamp(
                profile.tempo() * modifiers.tempoMultiplier(),
                MIN_FINAL_TEMPO,
                MAX_FINAL_TEMPO
        );
        return new FightProfile(strength, tempo, catchZoneArea(strength), profile.behavior());
    }

    /**
     * Projects the one canonical fight profile into Tide's minigame values.
     *
     * <p>Line strength/tempo, compatibility catch-zone/speed modifiers, and the Tide difficulty
     * multiplier are intentionally resolved here so runtime adapters never duplicate fight math.
     */
    public MinigameProjection projectMinigame(
            FightProfile profile,
            FishingGearModifiers modifiers,
            double difficultyMultiplier
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(modifiers, "modifiers");
        requireFinite("difficultyMultiplier", difficultyMultiplier);
        if (difficultyMultiplier < 0.0) {
            throw new IllegalArgumentException("difficultyMultiplier must be nonnegative");
        }

        double strength = profile.strength() * modifiers.strengthMultiplier();
        double tempo = profile.tempo() * modifiers.tempoMultiplier();
        double area = catchZoneArea(strength);
        double speed = Math.max(MIN_FINAL_TEMPO, tempo * difficultyMultiplier);
        return finishMinigameProjection(area, speed, modifiers);
    }

    /**
     * Compatibility-only projection for a non-V2 Tide minigame. No species, strength, tempo, or
     * catch-zone formula is regenerated; only retained named gear modifiers are applied to Tide's
     * already-computed values.
     */
    public MinigameProjection projectCompatibilityMinigame(
            double tideCatchZoneArea,
            double tideSpeed,
            FishingGearModifiers modifiers
    ) {
        requireFinite("tideCatchZoneArea", tideCatchZoneArea);
        requireFinite("tideSpeed", tideSpeed);
        Objects.requireNonNull(modifiers, "modifiers");
        return finishMinigameProjection(tideCatchZoneArea, tideSpeed, modifiers);
    }

    public double normalizeTempo(double externalTempo) {
        requireFinite("externalTempo", externalTempo);
        double s = clamp(externalTempo, 0.0, MAX_EXTERNAL_TEMPO);
        return 0.04 + 0.085 * Math.log1p(s) / Math.log(3.2);
    }

    public double normalizeStrength(double externalStrength) {
        requireFinite("externalStrength", externalStrength);
        return clamp(externalStrength, 0.0, MAX_CANONICAL_STRENGTH);
    }

    public double catchZoneArea(double strength) {
        requireFinite("strength", strength);
        double nonnegativeStrength = Math.max(strength, 0.0);
        double area = 0.78 - 0.58 * Math.pow(nonnegativeStrength, 1.25);
        return clamp(area, MIN_CATCH_ZONE_AREA, MAX_CATCH_ZONE_AREA);
    }

    public double strengthMultiplier(double percentile) {
        return 1.0 + 0.12 * normalizedPercentileOffset(percentile);
    }

    public double tempoMultiplier(double percentile) {
        return 1.0 - 0.06 * normalizedPercentileOffset(percentile);
    }

    private FightProfile applyBodyType(FightProfile profile, SpecimenData.BodyType bodyType) {
        double strengthMultiplier;
        double tempoMultiplier;
        switch (bodyType) {
            case GIANT -> {
                strengthMultiplier = GIANT_STRENGTH_MULTIPLIER;
                tempoMultiplier = GIANT_TEMPO_MULTIPLIER;
            }
            case DWARF -> {
                strengthMultiplier = DWARF_STRENGTH_MULTIPLIER;
                tempoMultiplier = DWARF_TEMPO_MULTIPLIER;
            }
            case NORMAL -> {
                strengthMultiplier = 1.0;
                tempoMultiplier = 1.0;
            }
            default -> throw new IllegalStateException("Unhandled Body Type: " + bodyType);
        }

        double strength = profile.strength() * strengthMultiplier;
        double tempo = clamp(
                profile.tempo() * tempoMultiplier,
                MIN_FINAL_TEMPO,
                MAX_FINAL_TEMPO
        );
        return new FightProfile(strength, tempo, catchZoneArea(strength), profile.behavior());
    }

    private MinigameProjection finishMinigameProjection(
            double catchZoneArea,
            double speed,
            FishingGearModifiers modifiers
    ) {
        double adjustedArea = catchZoneArea * FishingGearEffects.catchZoneAreaMultiplier(modifiers);
        double adjustedSpeed = speed * FishingGearEffects.minigameSpeedMultiplier(modifiers);
        return new MinigameProjection(
                clamp(adjustedArea, MIN_MINIGAME_CATCH_ZONE_AREA, MAX_MINIGAME_CATCH_ZONE_AREA),
                Math.max(MIN_MINIGAME_SPEED, adjustedSpeed)
        );
    }

    private static double normalizedPercentileOffset(double percentile) {
        if (!Double.isFinite(percentile) || percentile < 0.0 || percentile > 100.0) {
            throw new IllegalArgumentException("percentile must be between 0 and 100");
        }
        return (percentile - 50.0) / 50.0;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    public record MinigameProjection(double catchZoneArea, double speed) {
        public MinigameProjection {
            if (!Double.isFinite(catchZoneArea)
                    || catchZoneArea < MIN_MINIGAME_CATCH_ZONE_AREA
                    || catchZoneArea > MAX_MINIGAME_CATCH_ZONE_AREA) {
                throw new IllegalArgumentException("catchZoneArea must be within the Tide minigame bounds");
            }
            if (!Double.isFinite(speed) || speed < MIN_MINIGAME_SPEED) {
                throw new IllegalArgumentException("speed must be finite and at least the Tide minigame floor");
            }
        }
    }
}
