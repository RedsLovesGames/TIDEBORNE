package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Server-side recovery boundary for legacy repair and explicit destructive specimen rerolls. */
public final class LegacyFishRecoveryService {
    private static final SpecimenGenerator GENERATOR = new SpecimenGenerator();
    private static final TideSpeciesProfileAdapter PROFILES = new TideSpeciesProfileAdapter();

    public enum Status {
        REPAIRED,
        REROLLED,
        ALREADY_CURRENT,
        CONFIRMATION_REQUIRED,
        NOT_RECOVERABLE
    }

    public record Result(Status status, Optional<SpecimenData> specimen) {
        public Result {
            specimen = specimen == null ? Optional.empty() : specimen;
        }

        public boolean changed() {
            return status == Status.REPAIRED || status == Status.REROLLED;
        }
    }

    public Result repair(ItemStack stack) {
        CanonicalSpecimenStorage.MigrationState state = CanonicalSpecimenStorage.detectMigration(stack);
        if (state == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
            return new Result(Status.ALREADY_CURRENT, CanonicalSpecimenStorage.read(stack));
        }
        if (state != CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY
                && state != CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA) {
            return new Result(Status.NOT_RECOVERABLE, Optional.empty());
        }

        Optional<SpecimenData> repaired = CanonicalSpecimenStorage.read(stack);
        if (repaired.isEmpty()
                || CanonicalSpecimenStorage.detectMigration(stack) != CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
            return new Result(Status.NOT_RECOVERABLE, Optional.empty());
        }
        return new Result(Status.REPAIRED, repaired);
    }

    /**
     * Replaces specimen identity with a newly generated canonical specimen. Confirmation is checked
     * before any migration read or write, so a rejected reroll cannot even repair the legacy stack as
     * a side effect.
     */
    public Result reroll(ItemStack stack, long deterministicSeed, boolean confirmed) {
        if (!confirmed) {
            return new Result(Status.CONFIRMATION_REQUIRED, Optional.empty());
        }
        Optional<SpeciesProfile> species = resolveSpecies(stack);
        if (species.isEmpty()) {
            return new Result(Status.NOT_RECOVERABLE, Optional.empty());
        }

        SpecimenData rerolled = GENERATOR.generate(
                species.get(),
                deterministicSeed,
                SpecimenData.Provenance.generated()
        );
        CanonicalSpecimenStorage.write(stack, rerolled);
        return new Result(Status.REROLLED, CanonicalSpecimenStorage.read(stack));
    }

    private static Optional<SpeciesProfile> resolveSpecies(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Item item = stack.getItem();
        for (FishData data : TideData.FISH.get().values()) {
            if (data != null && data.fish().value() == item) {
                return Optional.of(PROFILES.adaptForMigration(data));
            }
        }
        return Optional.empty();
    }
}
