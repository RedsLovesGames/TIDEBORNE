package com.redslovesgames.tideborne.migration.legacy;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.tide.TideSpeciesProfileAdapter;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.migration.legacy.LegacyFishMigrationService;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Runtime persistence bridge for legacy fish stacks that do not carry enough Tideborne markers for
 * the normal canonical ItemStack read path to classify them, most notably old length-only fish.
 * All actual specimen interpretation remains owned by {@link LegacyFishMigrationService}.
 */
public final class LegacyPersistenceMigration {
    private static final LegacyFishMigrationService MIGRATION = new LegacyFishMigrationService();
    private static final TideSpeciesProfileAdapter PROFILE_ADAPTER = new TideSpeciesProfileAdapter();

    private LegacyPersistenceMigration() {
    }

    /**
     * Returns a current canonical specimen when one exists or can be migrated safely. Current,
     * trait-bearing legacy, and older-schema stacks first use {@link CanonicalSpecimenStorage#read}.
     * A registered fish with only a valid persisted Tide length is then migrated through the same
     * canonical migration service. Invalid canonical payloads never fall back to legacy data.
     */
    public static Optional<SpecimenData> migrateStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        CanonicalSpecimenStorage.MigrationState before = CanonicalSpecimenStorage.detectMigration(stack);
        Optional<SpecimenData> existing = CanonicalSpecimenStorage.read(stack);
        if (existing.isPresent()) {
            return existing;
        }
        if (before != CanonicalSpecimenStorage.MigrationState.NONE) {
            return Optional.empty();
        }

        Double length = legacyLength(stack);
        if (length == null || !Double.isFinite(length) || length <= 0.0) {
            return Optional.empty();
        }

        try {
            SpeciesProfile species = resolveSpecies(stack).orElse(null);
            if (species == null) {
                return Optional.empty();
            }
            LegacyFishMigrationService.LegacyFish legacy = new LegacyFishMigrationService.LegacyFish(
                    species.speciesId(),
                    null,
                    stack.get(TideTraitsComponents.MUTATION_SEED),
                    stack.get(TideTraitsComponents.SIZE_PERCENTILE),
                    length,
                    stack.get(TideTraitsComponents.MUTATION),
                    stack.get(TideTraitsComponents.BODY_TYPE),
                    null
            );
            SpecimenData migrated = MIGRATION.migrate(species, legacy).specimen();
            CanonicalSpecimenStorage.write(stack, migrated);
            return CanonicalSpecimenStorage.read(stack);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    private static Optional<SpeciesProfile> resolveSpecies(ItemStack stack) {
        Item item = stack.getItem();
        for (FishData data : TideData.FISH.get().values()) {
            if (data != null && data.fish().value() == item) {
                return Optional.of(PROFILE_ADAPTER.adaptForMigration(data));
            }
        }
        return Optional.empty();
    }

    private static Double legacyLength(ItemStack stack) {
        Object value = TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
