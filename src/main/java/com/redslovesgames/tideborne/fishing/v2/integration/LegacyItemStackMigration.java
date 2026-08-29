package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.FishScoreV2Service;
import com.redslovesgames.tideborne.fishing.v2.LegacyFishMigrationService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** One-time guarded bridge from legacy ItemStack fish data into the canonical specimen schema. */
final class LegacyItemStackMigration {
    private static final LegacyFishMigrationService MIGRATION = new LegacyFishMigrationService();
    private static final TideSpeciesProfileAdapter PROFILE_ADAPTER = new TideSpeciesProfileAdapter();
    private static final FishScoreV2Service FISH_SCORE = new FishScoreV2Service();

    private LegacyItemStackMigration() {
    }

    static Optional<SpecimenData> migrate(ItemStack stack, CanonicalSpecimenStorage.MigrationState state) {
        if (stack == null || stack.isEmpty()
                || (state != CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY
                && state != CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA)) {
            return Optional.empty();
        }

        try {
            SpeciesProfile species = resolveSpecies(stack).orElse(null);
            if (species == null) {
                return Optional.empty();
            }

            String declaredSpecies = stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
            if (state == CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA
                    && declaredSpecies != null
                    && !declaredSpecies.isBlank()
                    && !species.speciesId().equals(declaredSpecies)) {
                return Optional.empty();
            }

            Integer schema = state == CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA
                    ? stack.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION)
                    : null;
            Long seed = firstNonNull(
                    stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED),
                    stack.get(TideTraitsComponents.MUTATION_SEED)
            );
            Double percentile = firstUsablePercentile(
                    stack.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE),
                    stack.get(TideTraitsComponents.SIZE_PERCENTILE)
            );
            Double physicalLength = firstUsableLength(
                    stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH),
                    legacyLength(stack)
            );
            String bodyType = firstNonBlank(
                    stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE),
                    stack.get(TideTraitsComponents.BODY_TYPE)
            );

            LegacyFishMigrationService.LegacyFish legacy = new LegacyFishMigrationService.LegacyFish(
                    species.speciesId(),
                    schema,
                    seed,
                    percentile,
                    physicalLength,
                    stack.get(TideTraitsComponents.MUTATION),
                    bodyType,
                    null
            );
            SpecimenData migrated = MIGRATION.migrate(species, legacy).specimen();
            SpecimenData preserved = preserveOlderCanonicalValues(stack, state, migrated);
            return Optional.of(scoreFinalPreservedSpecimenIfAbsent(stack, species, preserved));
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

    private static SpecimenData preserveOlderCanonicalValues(
            ItemStack stack,
            CanonicalSpecimenStorage.MigrationState state,
            SpecimenData migrated
    ) {
        if (state != CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA) {
            return migrated;
        }

        double basePercentile = usablePercentile(stack.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE))
                ? stack.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE)
                : migrated.basePercentile();
        double baseLength = usableNonnegative(stack.get(TideTraitsComponents.SPECIMEN_BASE_LENGTH))
                ? stack.get(TideTraitsComponents.SPECIMEN_BASE_LENGTH)
                : migrated.baseLength();
        double finalLength = usableNonnegative(stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH))
                ? stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH)
                : migrated.finalLength();
        double finalPercentile = usablePercentile(stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE))
                ? stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE)
                : migrated.finalPercentile();

        return new SpecimenData(
                migrated.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                firstNonNull(stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED), migrated.deterministicSeed()),
                basePercentile,
                baseLength,
                finalLength,
                finalPercentile,
                enumOrDefault(SpecimenData.BodyType.class,
                        stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE), migrated.bodyType()),
                enumOrDefault(SpecimenData.Condition.class,
                        stack.get(TideTraitsComponents.SPECIMEN_CONDITION), migrated.condition()),
                enumOrDefault(SpecimenData.Pigmentation.class,
                        stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION), migrated.pigmentation()),
                enumOrDefault(SpecimenData.SpecimenQuality.class,
                        stack.get(TideTraitsComponents.SPECIMEN_QUALITY), migrated.specimenQuality()),
                firstNonNull(stack.get(TideTraitsComponents.SPECIMEN_PERFECT_CATCH), migrated.perfectCatch()),
                optionalFinite(stack.get(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE), OptionalDouble.empty()),
                optionalInt(stack.get(TideTraitsComponents.SPECIMEN_FISH_SCORE), OptionalInt.empty()),
                migrated.provenance()
        );
    }

    private static SpecimenData scoreFinalPreservedSpecimenIfAbsent(
            ItemStack stack,
            SpeciesProfile species,
            SpecimenData specimen
    ) {
        Double savedRaw = stack.get(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE);
        Integer savedScore = stack.get(TideTraitsComponents.SPECIMEN_FISH_SCORE);
        if ((savedRaw != null && Double.isFinite(savedRaw)) || savedScore != null) {
            return specimen;
        }
        if (specimen.rawFishScore().isPresent() || specimen.fishScore().isPresent()) {
            return specimen;
        }

        FishScoreV2Service.Result score = FISH_SCORE.calculate(species.rarity(), specimen);
        return new SpecimenData(
                specimen.speciesId(),
                specimen.schemaVersion(),
                specimen.generationVersion(),
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                specimen.baseLength(),
                specimen.finalLength(),
                specimen.finalPercentile(),
                specimen.bodyType(),
                specimen.condition(),
                specimen.pigmentation(),
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                OptionalDouble.of(score.rawScore()),
                OptionalInt.of(score.fishScore()),
                specimen.provenance()
        );
    }

    private static Double legacyLength(ItemStack stack) {
        Object value = TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private static Double firstUsablePercentile(Double preferred, Double fallback) {
        if (usablePercentile(preferred)) {
            return preferred;
        }
        return usablePercentile(fallback) ? fallback : null;
    }

    private static Double firstUsableLength(Double preferred, Double fallback) {
        if (usableLength(preferred)) {
            return preferred;
        }
        return usableLength(fallback) ? fallback : null;
    }

    private static boolean usablePercentile(Double value) {
        return value != null && Double.isFinite(value) && value >= 0.0 && value <= 100.0;
    }

    private static boolean usableLength(Double value) {
        return value != null && Double.isFinite(value) && value > 0.0;
    }

    private static boolean usableNonnegative(Double value) {
        return value != null && Double.isFinite(value) && value >= 0.0;
    }

    private static OptionalDouble optionalFinite(Double value, OptionalDouble fallback) {
        return value != null && Double.isFinite(value) ? OptionalDouble.of(value) : fallback;
    }

    private static OptionalInt optionalInt(Integer value, OptionalInt fallback) {
        return value == null ? fallback : OptionalInt.of(value);
    }

    private static String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return fallback;
    }

    private static <T> T firstNonNull(T preferred, T fallback) {
        return preferred != null ? preferred : fallback;
    }

    private static <E extends Enum<E>> E enumOrDefault(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }
}
