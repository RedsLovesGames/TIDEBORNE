package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/** Normalizes species fight inputs, then applies bounded specimen size effects. */
public final class FightProfileService {
    public static final double MAX_EXTERNAL_TEMPO = 2.2;
    public static final double MAX_CANONICAL_STRENGTH = 1.1;
    public static final double MIN_CATCH_ZONE_AREA = 0.12;
    public static final double MAX_CATCH_ZONE_AREA = 0.78;
    public static final double MIN_FINAL_TEMPO = 0.035;
    public static final double MAX_FINAL_TEMPO = 0.16;

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
        return new FightProfile(strength, tempo, catchZoneArea(strength), species.behavior());
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

