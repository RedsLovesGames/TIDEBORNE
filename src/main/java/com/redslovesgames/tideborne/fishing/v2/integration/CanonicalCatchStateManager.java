package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.TraitMomentumStorage;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.server.network.ServerPlayerEntity;

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

    /**
     * Applies Momentum progression once for a successfully completed canonical catch.
     * A failed storage write leaves the guard open so the same server-side completion path can retry.
     */
    public static boolean completeTraitMomentum(TideFishingHook hook, ServerPlayerEntity player) {
        if (hook == null || player == null) {
            return false;
        }
        CatchState state = STATES.get(hook);
        return state != null && state.updateTraitMomentumOnce(
                () -> TraitMomentumStorage.applyCompletedCatch(player, state.specimen())
        );
    }

    public static final class CatchState {
        private final long catchSeed;
        private final FishingContext context;
        private final FishingEnvironment environment;
        private final SpeciesProfile species;
        private final SpecimenData specimen;
        private final FightProfile fightProfile;
        private final int capturedTraitMomentum;
        private boolean traitMomentumUpdated;

        public CatchState(
                long catchSeed,
                FishingContext context,
                FishingEnvironment environment,
                SpeciesProfile species,
                SpecimenData specimen,
                FightProfile fightProfile
        ) {
            this(catchSeed, context, environment, species, specimen, fightProfile, 0);
        }

        public CatchState(
                long catchSeed,
                FishingContext context,
                FishingEnvironment environment,
                SpeciesProfile species,
                SpecimenData specimen,
                FightProfile fightProfile,
                int capturedTraitMomentum
        ) {
            if (context == null || environment == null || species == null || specimen == null || fightProfile == null) {
                throw new IllegalArgumentException("canonical catch state fields are required");
            }
            if (!species.speciesId().equals(specimen.speciesId())) {
                throw new IllegalArgumentException("species and specimen IDs must match");
            }
            if (capturedTraitMomentum < 0 || capturedTraitMomentum > TraitMomentumStorage.MAX_MOMENTUM) {
                throw new IllegalArgumentException("capturedTraitMomentum must be between 0 and "
                        + TraitMomentumStorage.MAX_MOMENTUM);
            }
            this.catchSeed = catchSeed;
            this.context = context;
            this.environment = environment;
            this.species = species;
            this.specimen = specimen;
            this.fightProfile = fightProfile;
            this.capturedTraitMomentum = capturedTraitMomentum;
        }

        public long catchSeed() {
            return catchSeed;
        }

        public FishingContext context() {
            return context;
        }

        public FishingEnvironment environment() {
            return environment;
        }

        public SpeciesProfile species() {
            return species;
        }

        public SpecimenData specimen() {
            return specimen;
        }

        public FightProfile fightProfile() {
            return fightProfile;
        }

        public int capturedTraitMomentum() {
            return capturedTraitMomentum;
        }

        public synchronized boolean traitMomentumUpdated() {
            return traitMomentumUpdated;
        }

        public synchronized boolean updateTraitMomentumOnce(Runnable update) {
            if (traitMomentumUpdated) {
                return false;
            }
            if (update == null) {
                throw new IllegalArgumentException("update cannot be null");
            }
            update.run();
            traitMomentumUpdated = true;
            return true;
        }
    }
}
