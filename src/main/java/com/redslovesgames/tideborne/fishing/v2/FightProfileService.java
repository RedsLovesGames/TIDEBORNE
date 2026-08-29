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
}
