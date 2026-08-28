package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/**
 * Canonical Fishing System 2.0 trait-event probability path.
 *
 * <p>The required order is base trait probability, any axis-specific event multiplier, canonical
 * species rarity compensation, Trait Luck transformation, then a final probability bound. This
 * service performs no random selection and never participates in species selection or Fishing Luck
 * weighting.
 */
public final class TraitProbabilityService {
    private final TraitLuckProbabilityService traitLuckProbabilityService;

    public TraitProbabilityService() {
        this(new TraitLuckProbabilityService());
    }

    TraitProbabilityService(TraitLuckProbabilityService traitLuckProbabilityService) {
        this.traitLuckProbabilityService = Objects.requireNonNull(
                traitLuckProbabilityService,
                "traitLuckProbabilityService"
        );
    }

    /** Calculates the final event probability with no axis-specific event multiplier. */
    public double calculate(double baseProbability, SpeciesProfile species, double traitLuck) {
        return calculate(baseProbability, species, traitLuck, 1.0);
    }

    /**
     * Calculates the final event probability for a canonical species.
     *
     * <p>The axis-specific event multiplier is applied directly to the base event chance before
     * rarity compensation and Trait Luck. Rarity is always read from {@link SpeciesProfile#rarity()}
     * so callers cannot provide a second rarity value that disagrees with the selected species. The
     * existing Trait Luck service provides the frozen input safety behavior before this method
     * performs the final defensive bound.
     */
    public double calculate(
            double baseProbability,
            SpeciesProfile species,
            double traitLuck,
            double eventProbabilityMultiplier
    ) {
        Objects.requireNonNull(species, "species");
        if (!Double.isFinite(eventProbabilityMultiplier) || eventProbabilityMultiplier < 0.0) {
            throw new IllegalArgumentException("eventProbabilityMultiplier must be finite and nonnegative");
        }

        double multipliedBase = eventProbabilityMultiplier == 0.0
                ? 0.0
                : baseProbability * eventProbabilityMultiplier;
        double rarityCompensated = multipliedBase * species.rarity().traitProbabilityMultiplier();
        double adjusted = traitLuckProbabilityService.adjustProbability(rarityCompensated, traitLuck);
        return Math.max(0.0, Math.min(1.0, adjusted));
    }
}
