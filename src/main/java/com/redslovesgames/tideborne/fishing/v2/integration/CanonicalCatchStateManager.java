package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/** Transient server-side state tying selection, specimen identity, and minigame setup to one live hook. */
public final class CanonicalCatchStateManager {
    private static final Map<TideFishingHook, CatchState> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CanonicalCatchStateManager() {
    }

    public static void put(TideFishingHook hook, CatchState state) {
        if (hook != null && state != null) {
            STATES.put(hook, state);
        }
    }

    public static Optional<CatchState> get(TideFishingHook hook) {
        if (hook == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(STATES.get(hook));
    }

    public static void clear(TideFishingHook hook) {
        if (hook != null) {
            STATES.remove(hook);
        }
    }

    public record CatchState(
            long catchSeed,
            FishingContext context,
            FishingEnvironment environment,
            SpeciesProfile species,
            SpecimenData specimen,
            FightProfile fightProfile
    ) {
        public CatchState {
            if (context == null || environment == null || species == null || specimen == null || fightProfile == null) {
                throw new IllegalArgumentException("canonical catch state fields are required");
            }
            if (!species.speciesId().equals(specimen.speciesId())) {
                throw new IllegalArgumentException("species and specimen IDs must match");
            }
        }
    }
}
