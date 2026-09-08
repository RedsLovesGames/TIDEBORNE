package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical SpeciesProfile audit boundary for the supported Apex Waters 1.1.1 integration.
 *
 * <p>Apex Waters 1.1.1 registers a Great White Shark entity, its spawn egg, and raw/cooked shark
 * meat. It does not register Tide FishData or a Tide-catchable fish item. The official catchable
 * species set is therefore intentionally empty. Keeping that result explicit prevents compatibility
 * code from inventing a synthetic shark SpeciesProfile merely because the optional mod is installed.
 *
 * <p>This class deliberately has no Apex Waters class dependency so the species-profile boundary is
 * safe to load when the optional mod is absent.
 */
public final class ApexWatersSpeciesProfileAdapter {
    public static final String MOD_ID = "apexwaters";
    public static final String GREAT_WHITE_SHARK_ENTITY_ID = "apexwaters:great_white_shark";
    public static final String GREAT_WHITE_SHARK_SPAWN_EGG_ID = "apexwaters:great_white_shark_spawn_egg";
    public static final String RAW_SHARK_MEAT_ID = "apexwaters:great_white_shark_raw";
    public static final String COOKED_SHARK_MEAT_ID = "apexwaters:great_white_shark_cooked";

    private static final Set<String> TIDE_CATCHABLE_SPECIES_IDS = Set.of();

    public Set<String> tideCatchableSpeciesIds() {
        return TIDE_CATCHABLE_SPECIES_IDS;
    }

    public boolean supports(String speciesId) {
        requireNamespacedId(speciesId);
        return TIDE_CATCHABLE_SPECIES_IDS.contains(speciesId);
    }

    /**
     * Returns the canonical Apex profile when an official Tide-catchable Apex species exists.
     * Apex Waters 1.1.1 has none, so every valid ID intentionally returns empty.
     */
    public Optional<SpeciesProfile> adapt(String speciesId) {
        requireNamespacedId(speciesId);
        return Optional.empty();
    }

    private static void requireNamespacedId(String speciesId) {
        if (speciesId == null || speciesId.isBlank() || !speciesId.contains(":")) {
            throw new IllegalArgumentException("speciesId must be a namespaced ID");
        }
    }
}
