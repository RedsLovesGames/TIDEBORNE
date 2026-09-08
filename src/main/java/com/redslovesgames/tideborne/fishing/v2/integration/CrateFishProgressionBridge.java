package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.LegacyFishMigrationService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import java.util.Optional;
import net.minecraft.item.ItemStack;

/**
 * Normalizes legitimate Tide fish that enter Tide's normal catch-accounting path without specimen
 * metadata, such as fish awarded from crates. This is deliberately a preparation step only: Tide's
 * existing logCatch path remains responsible for journal/progression/accounting.
 */
public final class CrateFishProgressionBridge {
    private static final LegacyFishMigrationService MIGRATION = new LegacyFishMigrationService();
    private static final TideSpeciesProfileAdapter PROFILES = new TideSpeciesProfileAdapter();

    private CrateFishProgressionBridge() {
    }

    public static boolean ensureCanonicalForCatchAccounting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        CanonicalSpecimenStorage.MigrationState state = CanonicalSpecimenStorage.detectMigration(stack);
        if (state == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
            return CanonicalSpecimenStorage.read(stack).isPresent();
        }
        if (state == CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY
                || state == CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA) {
            return CanonicalSpecimenStorage.read(stack).isPresent();
        }
        if (state != CanonicalSpecimenStorage.MigrationState.NONE) {
            return false;
        }

        FishData fish = FishData.get(stack).orElse(null);
        if (fish == null) {
            return false;
        }
        Object storedLength = TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
        double length = storedLength instanceof Number number ? number.doubleValue() : 0.0;
        if (!Double.isFinite(length) || length <= 0.0) {
            return false;
        }

        SpeciesProfile species = PROFILES.adaptForMigration(fish);
        SpecimenData canonical = MIGRATION.migrate(
                species,
                new LegacyFishMigrationService.LegacyFish(
                        species.speciesId(),
                        null,
                        null,
                        null,
                        length,
                        null,
                        null,
                        null
                )
        ).specimen();
        CanonicalSpecimenStorage.write(stack, canonical);
        return CanonicalSpecimenStorage.detectMigration(stack)
                == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT;
    }
}
