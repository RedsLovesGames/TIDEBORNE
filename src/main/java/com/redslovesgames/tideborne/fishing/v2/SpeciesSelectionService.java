package com.redslovesgames.tideborne.fishing.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Selects one eligible fish species while preserving overall fish-catch probability. */
public final class SpeciesSelectionService {
    public SpeciesProfile select(
            List<SpeciesProfile> species,
            FishingContext context,
            FishingEnvironment environment,
            RandomGenerator random
    ) {
        Objects.requireNonNull(random, "random");

        List<WeightedSpecies> eligible = eligibleSpecies(species, context, environment);
        double totalWeight = 0.0;
        for (WeightedSpecies candidate : eligible) {
            totalWeight += candidate.adjustedWeight();
        }

        if (!Double.isFinite(totalWeight) || totalWeight <= 0.0) {
            throw new IllegalArgumentException("eligible species pool must contain positive finite encounter weight");
        }

        double target = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (WeightedSpecies candidate : eligible) {
            cumulative += candidate.adjustedWeight();
            if (target < cumulative) {
                return candidate.profile();
            }
        }
        return eligible.get(eligible.size() - 1).profile();
    }

    /**
     * Returns the exact eligible weighted pool used by canonical species selection.
     * This is pure and is also the supported read-only debug projection for selection weights.
     */
    public List<WeightedSpecies> eligibleSpecies(
            List<SpeciesProfile> species,
            FishingContext context,
            FishingEnvironment environment
    ) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(environment, "environment");

        List<WeightedSpecies> eligible = new ArrayList<>();
        for (SpeciesProfile profile : species) {
            if (profile == null || !profile.isEligible(environment) || profile.encounterWeight() <= 0.0) {
                continue;
            }
            double weight = adjustedWeight(profile, context.fishingLuck());
            if (!Double.isFinite(weight)) {
                throw new IllegalArgumentException("adjusted encounter weight overflowed for " + profile.speciesId());
            }
            if (weight > 0.0) {
                eligible.add(new WeightedSpecies(profile, profile.encounterWeight(), weight));
            }
        }

        if (eligible.isEmpty()) {
            throw new IllegalArgumentException("eligible species pool must contain positive finite encounter weight");
        }
        return List.copyOf(eligible);
    }

    public double adjustedWeight(SpeciesProfile profile, double fishingLuck) {
        Objects.requireNonNull(profile, "profile");
        return profile.encounterWeight() * profile.rarity().fishingLuckMultiplier(fishingLuck);
    }

    /** Immutable view of one candidate's canonical pre-luck and post-luck encounter weights. */
    public record WeightedSpecies(SpeciesProfile profile, double baseWeight, double adjustedWeight) {
        public WeightedSpecies {
            Objects.requireNonNull(profile, "profile");
            if (!Double.isFinite(baseWeight) || baseWeight <= 0.0) {
                throw new IllegalArgumentException("baseWeight must be positive and finite");
            }
            if (!Double.isFinite(adjustedWeight) || adjustedWeight <= 0.0) {
                throw new IllegalArgumentException("adjustedWeight must be positive and finite");
            }
        }
    }
}
