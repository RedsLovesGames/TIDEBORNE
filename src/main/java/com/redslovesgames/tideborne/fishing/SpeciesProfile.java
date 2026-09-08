package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.SizeDistribution;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Canonical, compatibility-normalized species data consumed by Fishing System 2.0. */
public record SpeciesProfile(
        String speciesId,
        CanonicalRarity rarity,
        double encounterWeight,
        SpeciesEligibility eligibility,
        double strength,
        double tempo,
        String behavior,
        SizeDistribution sizeDistribution,
        Set<String> traitEligibility,
        Map<String, Double> traitAffinity,
        Set<String> targetTags
) {
    public SpeciesProfile {
        targetTags = targetTags == null ? Set.of() : Collections.unmodifiableSet(new TreeSet<>(targetTags));
        if (targetTags.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("target tags must not be blank");
        }
        if (speciesId == null || speciesId.isBlank() || !speciesId.contains(":")) {
            throw new IllegalArgumentException("speciesId must be a namespaced ID");
        }
        if (rarity == null) {
            throw new IllegalArgumentException("rarity is required");
        }
        if (!Double.isFinite(encounterWeight) || encounterWeight < 0.0) {
            throw new IllegalArgumentException("encounterWeight must be finite and nonnegative");
        }
        eligibility = eligibility == null ? SpeciesEligibility.always() : eligibility;
        if (!Double.isFinite(strength) || strength < 0.0) {
            throw new IllegalArgumentException("strength must be finite and nonnegative");
        }
        if (!Double.isFinite(tempo) || tempo < 0.0) {
            throw new IllegalArgumentException("tempo must be finite and nonnegative");
        }
        if (behavior == null || behavior.isBlank()) {
            throw new IllegalArgumentException("behavior is required");
        }
        if (sizeDistribution == null) {
            throw new IllegalArgumentException("sizeDistribution is required");
        }
        traitEligibility = traitEligibility == null || traitEligibility.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(new TreeSet<>(traitEligibility));
        TreeMap<String, Double> affinities = new TreeMap<>();
        if (traitAffinity != null) {
            traitAffinity.forEach((key, value) -> {
                if (key == null || key.isBlank() || value == null || !Double.isFinite(value) || value < 0.0) {
                    throw new IllegalArgumentException("trait affinities require nonblank keys and finite nonnegative values");
                }
                affinities.put(key, value);
            });
        }
        traitAffinity = affinities.isEmpty() ? Map.of() : Collections.unmodifiableMap(affinities);
    }

    public boolean isEligible(FishingEnvironment environment) {
        return eligibility.isEligible(environment);
    }

    /** Existing adapters remain source-compatible; target metadata is optional and not persisted. */
    public SpeciesProfile(String speciesId, CanonicalRarity rarity, double encounterWeight,
                          SpeciesEligibility eligibility, double strength, double tempo, String behavior,
                          SizeDistribution sizeDistribution, Set<String> traitEligibility, Map<String, Double> traitAffinity) {
        this(speciesId, rarity, encounterWeight, eligibility, strength, tempo, behavior, sizeDistribution,
                traitEligibility, traitAffinity, Set.of());
    }
}

