package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/** Canonical per-species Trait Momentum progression and temporary Trait Luck contribution. */
public final class TraitMomentumProgression {
    public static final int NORMAL_CATCH_GAIN = 1;

    private TraitMomentumProgression() {
    }

    /**
     * Adds the Momentum captured before specimen generation to persistent/server-owned Trait Luck.
     * The captured value is validated here so catch generation cannot silently use an invalid value.
     */
    public static double effectiveTraitLuck(double traitLuck, int capturedMomentum) {
        if (!Double.isFinite(traitLuck)) {
            throw new IllegalArgumentException("traitLuck must be finite");
        }
        if (capturedMomentum < 0 || capturedMomentum > TraitMomentumStorage.MAX_MOMENTUM) {
            throw new IllegalArgumentException("capturedMomentum must be between 0 and "
                    + TraitMomentumStorage.MAX_MOMENTUM);
        }
        return traitLuck + capturedMomentum;
    }

    /**
     * A fully normal catch has no notable value on any canonical specimen trait axis.
     * Percentile/size and the Perfect Catch skill result are intentionally not trait axes.
     */
    public static boolean isFullyNormal(SpecimenData specimen) {
        Objects.requireNonNull(specimen, "specimen");
        return specimen.bodyType() == SpecimenData.BodyType.NORMAL
                && specimen.condition() == SpecimenData.Condition.NORMAL
                && specimen.pigmentation() == SpecimenData.Pigmentation.NORMAL
                && specimen.specimenQuality() == SpecimenData.SpecimenQuality.NORMAL;
    }

    /** Applies exactly one completed-catch outcome to a mutable player Momentum state. */
    static int applyCompletedCatch(TraitMomentumState state, SpecimenData specimen) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(specimen, "specimen");
        if (isFullyNormal(specimen)) {
            return state.add(specimen.speciesId(), NORMAL_CATCH_GAIN);
        }
        state.clear(specimen.speciesId());
        return 0;
    }
}
