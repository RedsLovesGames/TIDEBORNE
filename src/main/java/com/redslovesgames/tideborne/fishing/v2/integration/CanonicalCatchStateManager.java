package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.TraitMomentumProgression;
import com.redslovesgames.tideborne.fishing.v2.TraitMomentumStorage;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** Transient server-side state tying selection, specimen identity, and minigame setup to one live hook. */
public final class CanonicalCatchStateManager {
    private static final Map<TideFishingHook, CatchState> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final SpecimenGenerator SPECIMEN_GENERATOR = new SpecimenGenerator();

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
     * Captures Tide's already-resolved center-zone Perfect Catch result, finalizes the canonical
     * post-fight specimen, and writes that final state onto the selected item before Tide delivers it.
     * Returns true when the hook belongs to the canonical V2 path so callers can bypass legacy late
     * Perfect Catch mutation logic.
     */
    public static boolean capturePerfectCatch(TideFishingHook hook, boolean perfectCatch) {
        if (hook == null) {
            return false;
        }
        CatchState state = STATES.get(hook);
        if (state == null) {
            return false;
        }

        return finalizeAndPersist(state, perfectCatch, specimen -> persistFinalSpecimen(hook, state, specimen));
    }

    /**
     * Integration seam used by runtime and tests. Canonical generation always completes before the
     * supplied persistence action receives the specimen.
     */
    static boolean finalizeAndPersist(
            CatchState state,
            boolean perfectCatch,
            Consumer<SpecimenData> persistence
    ) {
        if (state == null || persistence == null) {
            return false;
        }
        SpecimenData finalized = state.finalizeSpecimenOnce(SPECIMEN_GENERATOR, perfectCatch);
        persistence.accept(finalized);
        return true;
    }

    private static void persistFinalSpecimen(TideFishingHook hook, CatchState state, SpecimenData specimen) {
        if (hook.getHookedItems() == null) {
            return;
        }
        for (ItemStack stack : hook.getHookedItems()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            String canonicalSpecies = stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
            if (state.species().speciesId().equals(canonicalSpecies)) {
                CanonicalSpecimenStorage.write(stack, specimen);
            }
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
        private SpecimenData specimen;
        private final FightProfile fightProfile;
        private final int capturedTraitMomentum;
        private boolean specimenFinalized;
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

        public synchronized SpecimenData specimen() {
            return specimen;
        }

        public FightProfile fightProfile() {
            return fightProfile;
        }

        public int capturedTraitMomentum() {
            return capturedTraitMomentum;
        }

        public synchronized boolean specimenFinalized() {
            return specimenFinalized;
        }

        /**
         * Finalizes the canonical post-fight axes exactly once using the frozen catch context and
         * Momentum captured at selection time. Repeated calls return the same finalized specimen.
         */
        public synchronized SpecimenData finalizeSpecimenOnce(
                SpecimenGenerator generator,
                boolean perfectCatch
        ) {
            if (!specimenFinalized) {
                if (generator == null) {
                    throw new IllegalArgumentException("generator cannot be null");
                }
                double effectiveTraitLuck = TraitMomentumProgression.effectiveTraitLuck(
                        context.traitLuck(),
                        capturedTraitMomentum
                );
                specimen = generator.finalizeAfterFight(species, specimen, effectiveTraitLuck, perfectCatch);
                specimenFinalized = true;
            }
            return specimen;
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
