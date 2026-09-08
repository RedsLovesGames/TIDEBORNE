package com.redslovesgames.tideborne.fishing.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

/**
 * Mutable, bounded per-species Trait Momentum values for one player.
 *
 * <p>This class owns only the small serializable value map. Runtime player persistence is handled by
 * {@link TraitMomentumStorage} so catches, fish items, and client state never become Momentum authority.
 */
final class TraitMomentumState {
    static final int DATA_VERSION = 1;
    static final int MAX_MOMENTUM = 15;
    private static final int MAX_SPECIES = 1024;
    private static final int MAX_SPECIES_ID_LENGTH = 256;
    private static final String DATA_VERSION_KEY = "DataVersion";
    private static final String SPECIES_KEY = "Species";

    private final LinkedHashMap<String, Integer> values = new LinkedHashMap<>();

    int get(String speciesId) {
        return values.getOrDefault(requireSpeciesId(speciesId), 0);
    }

    int set(String speciesId, int momentum) {
        String canonicalSpeciesId = requireSpeciesId(speciesId);
        int bounded = clampMomentum(momentum);
        if (bounded == 0) {
            values.remove(canonicalSpeciesId);
        } else {
            values.put(canonicalSpeciesId, bounded);
        }
        return bounded;
    }

    int add(String speciesId, int delta) {
        String canonicalSpeciesId = requireSpeciesId(speciesId);
        long candidate = (long) values.getOrDefault(canonicalSpeciesId, 0) + delta;
        int bounded = clampMomentum(candidate);
        if (bounded == 0) {
            values.remove(canonicalSpeciesId);
        } else {
            values.put(canonicalSpeciesId, bounded);
        }
        return bounded;
    }

    void clear(String speciesId) {
        values.remove(requireSpeciesId(speciesId));
    }

    Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();
        root.putInt(DATA_VERSION_KEY, DATA_VERSION);
        NbtCompound species = new NbtCompound();
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(MAX_SPECIES)
                .forEach(entry -> species.putInt(entry.getKey(), entry.getValue()));
        root.put(SPECIES_KEY, species);
        return root;
    }

    static TraitMomentumState fromNbt(NbtCompound root) {
        TraitMomentumState state = new TraitMomentumState();
        if (root == null || root.isEmpty()) {
            return state;
        }

        NbtCompound species = null;
        NbtElement storedSpecies = root.get(SPECIES_KEY);
        if (storedSpecies instanceof NbtCompound compound) {
            species = compound;
        } else if (!root.contains(DATA_VERSION_KEY)) {
            // Tolerate an unversioned early/legacy direct-map shape. Invalid keys are ignored below.
            species = root;
        }

        if (species == null) {
            return state;
        }

        List<String> keys = new ArrayList<>(species.getKeys());
        keys.sort(String::compareTo);
        int accepted = 0;
        for (String rawSpeciesId : keys) {
            if (accepted >= MAX_SPECIES || !isValidSpeciesId(rawSpeciesId) || !species.contains(rawSpeciesId, 3)) {
                continue;
            }

            int bounded = clampMomentum(species.getInt(rawSpeciesId));
            if (bounded > 0) {
                state.values.put(rawSpeciesId, bounded);
                accepted++;
            }
        }
        return state;
    }

    private static String requireSpeciesId(String speciesId) {
        if (!isValidSpeciesId(speciesId)) {
            throw new IllegalArgumentException("speciesId must be a valid namespaced ID");
        }
        return speciesId;
    }

    private static boolean isValidSpeciesId(String speciesId) {
        return speciesId != null
                && !speciesId.isEmpty()
                && speciesId.length() <= MAX_SPECIES_ID_LENGTH
                && Identifier.tryParse(speciesId) != null;
    }

    private static int clampMomentum(long momentum) {
        if (momentum <= 0L) {
            return 0;
        }
        if (momentum >= MAX_MOMENTUM) {
            return MAX_MOMENTUM;
        }
        return (int) momentum;
    }
}
