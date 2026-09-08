package com.redslovesgames.tideborne.fishing;

import java.util.Set;

/** Pure compatibility boundary for habitat and other species eligibility rules. */
@FunctionalInterface
public interface SpeciesEligibility {
    boolean isEligible(FishingEnvironment environment);

    static SpeciesEligibility always() {
        return environment -> true;
    }

    static SpeciesEligibility anyHabitat(Set<String> acceptedHabitats) {
        Set<String> accepted = Set.copyOf(acceptedHabitats);
        return environment -> environment.habitatTags().stream().anyMatch(accepted::contains);
    }
}

