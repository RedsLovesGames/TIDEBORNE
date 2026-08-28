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
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(random, "random");

        List<WeightedSpecies> eligible = new ArrayList<>();
        double totalWeight = 0.0;
        for (SpeciesProfile profile : species) {
            if (profile == null || !profile.isEligible(environment) || profile.encounterWeight() <= 0.0) {
                continue;
            }
            double weight = adjustedWeight(profile, context.fishingLuck());
            if (!Double.isFinite(weight)) {
                throw new IllegalArgumentException("adjusted encounter weight overflowed for " + profile.speciesId());
            }
            if (weight > 0.0) {
                eligible.add(new WeightedSpecies(profile, weight));
                totalWeight += weight;
            }
        }

        if (eligible.isEmpty() || !Double.isFinite(totalWeight) || totalWeight <= 0.0) {
            throw new IllegalArgumentException("eligible species pool must contain positive finite encounter weight");
        }

        double target = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (WeightedSpecies candidate : eligible) {
            cumulative += candidate.weight;
            if (target < cumulative) {
                return candidate.profile;
            }
        }
        return eligible.get(eligible.size() - 1).profile;
    }

    public double adjustedWeight(SpeciesProfile profile, double fishingLuck) {
        Objects.requireNonNull(profile, "profile");
        return profile.encounterWeight() * profile.rarity().fishingLuckMultiplier(fishingLuck);
    }

    private record WeightedSpecies(SpeciesProfile profile, double weight) {
    }
}

