package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/**
 * Single canonical ItemStack persistence boundary for Fishing System 2.0 specimen identity.
 * Current canonical payloads are read directly. Legacy-only and older-schema fish payloads are
 * migrated exactly once at this boundary and immediately rewritten as current canonical data.
 */
public final class CanonicalSpecimenStorage {
    public static final String PERCENTILE_DEFINITION = "size_adjusted_final_percentile";

    private static final String TRANSFER_ROOT = "CanonicalSpecimen";
    private static final String SPECIES_ID_KEY = "SpeciesId";
    private static final String SCHEMA_VERSION_KEY = "SchemaVersion";
    private static final String GENERATION_VERSION_KEY = "GenerationVersion";
    private static final String DETERMINISTIC_SEED_KEY = "DeterministicSeed";
    private static final String BASE_PERCENTILE_KEY = "BasePercentile";
    private static final String BASE_LENGTH_KEY = "BaseLength";
    private static final String FINAL_LENGTH_KEY = "FinalLength";
    private static final String FINAL_PERCENTILE_KEY = "FinalPercentile";
    private static final String PERCENTILE_DEFINITION_KEY = "PercentileDefinition";
    private static final String BODY_TYPE_KEY = "BodyType";
    private static final String CONDITION_KEY = "Condition";
    private static final String PIGMENTATION_KEY = "Pigmentation";
    private static final String QUALITY_KEY = "Quality";
    private static final String PERFECT_CATCH_KEY = "PerfectCatch";
    private static final String RAW_FISH_SCORE_KEY = "RawFishScore";
    private static final String FISH_SCORE_KEY = "FishScore";

    private CanonicalSpecimenStorage() {
    }

    public enum MigrationState {
        NONE,
        LEGACY_ONLY,
        CANONICAL_OLDER_SCHEMA,
        CANONICAL_NEWER_SCHEMA,
        CANONICAL_OLDER_GENERATION,
        CANONICAL_NEWER_GENERATION,
        CANONICAL_INCOMPLETE,
        CANONICAL_CURRENT
    }

    /** Writes every canonical specimen field, then refreshes legacy compatibility mirrors. */
    public static void write(ItemStack stack, SpecimenData specimen) {
        if (stack == null || stack.isEmpty() || specimen == null) {
            return;
        }

        String bodyType = serialized(specimen.bodyType());
        String condition = serialized(specimen.condition());
        stack.set(TideTraitsComponents.SPECIMEN_SPECIES_ID, specimen.speciesId());
        stack.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, specimen.schemaVersion());
        stack.set(TideTraitsComponents.SPECIMEN_GENERATION_VERSION, specimen.generationVersion());
        stack.set(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED, specimen.deterministicSeed());
        stack.set(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE, specimen.basePercentile());
        stack.set(TideTraitsComponents.SPECIMEN_BASE_LENGTH, specimen.baseLength());
        stack.set(TideTraitsComponents.SPECIMEN_FINAL_LENGTH, specimen.finalLength());
        stack.set(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE, specimen.finalPercentile());
        stack.set(TideTraitsComponents.SPECIMEN_PERCENTILE_DEFINITION, PERCENTILE_DEFINITION);
        stack.set(TideTraitsComponents.SPECIMEN_BODY_TYPE, bodyType);
        stack.set(TideTraitsComponents.SPECIMEN_CONDITION, condition);
        stack.set(TideTraitsComponents.SPECIMEN_PIGMENTATION, serialized(specimen.pigmentation()));
        stack.set(TideTraitsComponents.SPECIMEN_QUALITY, serialized(specimen.specimenQuality()));
        stack.set(TideTraitsComponents.SPECIMEN_PERFECT_CATCH, specimen.perfectCatch());
        setOptionalScores(stack, specimen);

        // Compatibility mirrors only. Current canonical reads never fall back to these values.
        stack.set(TideTraitsComponents.MUTATION_SEED, specimen.deterministicSeed());
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, specimen.finalPercentile());
        stack.set(TideTraitsComponents.BODY_TYPE, bodyType);
        stack.set(TideTraitsComponents.MUTATION, condition);
        if (Double.isFinite(specimen.finalLength()) && specimen.finalLength() > 0.0) {
            TideItemData.FISH_LENGTH.set(stack, specimen.finalLength());
        }
    }

    /**
     * Reads the canonical specimen. Legacy-only and older-schema registered fish stacks are migrated
     * once and rewritten only after a complete successful conversion. All other non-current states
     * fail closed without modifying the stack.
     */
    public static Optional<SpecimenData> read(ItemStack stack) {
        MigrationState state = detectMigration(stack);
        if (state == MigrationState.CANONICAL_CURRENT) {
            return decode(new ComponentSource(stack));
        }
        if (state != MigrationState.LEGACY_ONLY && state != MigrationState.CANONICAL_OLDER_SCHEMA) {
            return Optional.empty();
        }

        Optional<SpecimenData> migrated = LegacyItemStackMigration.migrate(stack, state);
        if (migrated.isEmpty()) {
            return Optional.empty();
        }
        write(stack, migrated.get());
        // Provenance is runtime-only; return the normalized persisted representation on every read.
        return decode(new ComponentSource(stack));
    }

    /** Decode-only access: never migrates or modifies the supplied stack. */
    public static Optional<SpecimenData> readCurrent(ItemStack stack) {
        return detectMigration(stack) == MigrationState.CANONICAL_CURRENT
                ? decode(new ComponentSource(stack)) : Optional.empty();
    }

    /** Explicitly classifies whether a stack requires migration without modifying it. */
    public static MigrationState detectMigration(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return MigrationState.NONE;
        }

        Integer schema = stack.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION);
        String species = stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
        boolean hasCanonicalMarker = schema != null || (species != null && !species.isBlank());
        if (!hasCanonicalMarker) {
            return hasLegacySpecimen(stack) ? MigrationState.LEGACY_ONLY : MigrationState.NONE;
        }
        if (schema == null || species == null || species.isBlank()) {
            return MigrationState.CANONICAL_INCOMPLETE;
        }
        if (schema < SpecimenGenerator.SCHEMA_VERSION) {
            return MigrationState.CANONICAL_OLDER_SCHEMA;
        }
        if (schema > SpecimenGenerator.SCHEMA_VERSION) {
            return MigrationState.CANONICAL_NEWER_SCHEMA;
        }
        Integer generation = stack.get(TideTraitsComponents.SPECIMEN_GENERATION_VERSION);
        if (generation == null) {
            return MigrationState.CANONICAL_INCOMPLETE;
        }
        if (generation < SpecimenGenerator.GENERATION_VERSION) {
            return MigrationState.CANONICAL_OLDER_GENERATION;
        }
        if (generation > SpecimenGenerator.GENERATION_VERSION) {
            return MigrationState.CANONICAL_NEWER_GENERATION;
        }
        return hasCompleteCurrentPayload(stack)
                ? MigrationState.CANONICAL_CURRENT
                : MigrationState.CANONICAL_INCOMPLETE;
    }

    /** Stores the same canonical payload inside transfer NBT for entity/bucket round trips. */
    public static void writeTransferData(NbtCompound target, SpecimenData specimen) {
        if (target == null || specimen == null) {
            return;
        }
        NbtCompound canonical = new NbtCompound();
        canonical.putString(SPECIES_ID_KEY, specimen.speciesId());
        canonical.putInt(SCHEMA_VERSION_KEY, specimen.schemaVersion());
        canonical.putInt(GENERATION_VERSION_KEY, specimen.generationVersion());
        canonical.putLong(DETERMINISTIC_SEED_KEY, specimen.deterministicSeed());
        canonical.putDouble(BASE_PERCENTILE_KEY, specimen.basePercentile());
        canonical.putDouble(BASE_LENGTH_KEY, specimen.baseLength());
        canonical.putDouble(FINAL_LENGTH_KEY, specimen.finalLength());
        canonical.putDouble(FINAL_PERCENTILE_KEY, specimen.finalPercentile());
        canonical.putString(PERCENTILE_DEFINITION_KEY, PERCENTILE_DEFINITION);
        canonical.putString(BODY_TYPE_KEY, serialized(specimen.bodyType()));
        canonical.putString(CONDITION_KEY, serialized(specimen.condition()));
        canonical.putString(PIGMENTATION_KEY, serialized(specimen.pigmentation()));
        canonical.putString(QUALITY_KEY, serialized(specimen.specimenQuality()));
        canonical.putBoolean(PERFECT_CATCH_KEY, specimen.perfectCatch());
        specimen.rawFishScore().ifPresent(value -> canonical.putDouble(RAW_FISH_SCORE_KEY, value));
        specimen.fishScore().ifPresent(value -> canonical.putInt(FISH_SCORE_KEY, value));
        target.put(TRANSFER_ROOT, canonical);
    }

    /** Returns whether transfer NBT declares a canonical specimen payload, valid or not. */
    public static boolean hasTransferPayload(NbtCompound source) {
        return source != null && source.contains(TRANSFER_ROOT, 10);
    }

    /** Reads transfer NBT without mutation or fallback to legacy transfer keys. */
    public static Optional<SpecimenData> readTransferData(NbtCompound source) {
        if (!hasTransferPayload(source)) {
            return Optional.empty();
        }
        NbtCompound canonical = source.getCompound(TRANSFER_ROOT);
        if (!hasCompleteTransferPayload(canonical)
                || canonical.getInt(SCHEMA_VERSION_KEY) != SpecimenGenerator.SCHEMA_VERSION
                || canonical.getInt(GENERATION_VERSION_KEY) != SpecimenGenerator.GENERATION_VERSION
                || !PERCENTILE_DEFINITION.equals(canonical.getString(PERCENTILE_DEFINITION_KEY))) {
            return Optional.empty();
        }
        return decode(new NbtSource(canonical));
    }

    /** Explicit restoration step used by transfer code. */
    public static boolean restoreTransferData(NbtCompound source, ItemStack stack) {
        Optional<SpecimenData> specimen = readTransferData(source);
        if (specimen.isEmpty()) {
            return false;
        }
        write(stack, specimen.get());
        return true;
    }

    private static Optional<SpecimenData> decode(Source source) {
        try {
            String percentileDefinition = source.string(PERCENTILE_DEFINITION_KEY);
            if (!PERCENTILE_DEFINITION.equals(percentileDefinition)) {
                return Optional.empty();
            }
            return Optional.of(new SpecimenData(
                    source.string(SPECIES_ID_KEY),
                    source.integer(SCHEMA_VERSION_KEY),
                    source.integer(GENERATION_VERSION_KEY),
                    source.longValue(DETERMINISTIC_SEED_KEY),
                    source.doubleValue(BASE_PERCENTILE_KEY),
                    source.doubleValue(BASE_LENGTH_KEY),
                    source.doubleValue(FINAL_LENGTH_KEY),
                    source.doubleValue(FINAL_PERCENTILE_KEY),
                    enumValue(SpecimenData.BodyType.class, source.string(BODY_TYPE_KEY)),
                    enumValue(SpecimenData.Condition.class, source.string(CONDITION_KEY)),
                    enumValue(SpecimenData.Pigmentation.class, source.string(PIGMENTATION_KEY)),
                    enumValue(SpecimenData.SpecimenQuality.class, source.string(QUALITY_KEY)),
                    source.bool(PERFECT_CATCH_KEY),
                    source.optionalDouble(RAW_FISH_SCORE_KEY),
                    source.optionalInt(FISH_SCORE_KEY),
                    SpecimenData.Provenance.generated()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    private static boolean hasCompleteTransferPayload(NbtCompound canonical) {
        return canonical.contains(SPECIES_ID_KEY)
                && !canonical.getString(SPECIES_ID_KEY).isBlank()
                && canonical.contains(SCHEMA_VERSION_KEY)
                && canonical.contains(GENERATION_VERSION_KEY)
                && canonical.contains(DETERMINISTIC_SEED_KEY)
                && canonical.contains(BASE_PERCENTILE_KEY)
                && canonical.contains(BASE_LENGTH_KEY)
                && canonical.contains(FINAL_LENGTH_KEY)
                && canonical.contains(FINAL_PERCENTILE_KEY)
                && canonical.contains(PERCENTILE_DEFINITION_KEY)
                && canonical.contains(BODY_TYPE_KEY)
                && canonical.contains(CONDITION_KEY)
                && canonical.contains(PIGMENTATION_KEY)
                && canonical.contains(QUALITY_KEY)
                && canonical.contains(PERFECT_CATCH_KEY);
    }

    private static boolean hasCompleteCurrentPayload(ItemStack stack) {
        return stack.get(TideTraitsComponents.SPECIMEN_GENERATION_VERSION) != null
                && stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED) != null
                && stack.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE) != null
                && stack.get(TideTraitsComponents.SPECIMEN_BASE_LENGTH) != null
                && stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH) != null
                && stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE) != null
                && PERCENTILE_DEFINITION.equals(stack.get(TideTraitsComponents.SPECIMEN_PERCENTILE_DEFINITION))
                && stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE) != null
                && stack.get(TideTraitsComponents.SPECIMEN_CONDITION) != null
                && stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION) != null
                && stack.get(TideTraitsComponents.SPECIMEN_QUALITY) != null
                && stack.get(TideTraitsComponents.SPECIMEN_PERFECT_CATCH) != null;
    }

    private static boolean hasLegacySpecimen(ItemStack stack) {
        String mutation = stack.get(TideTraitsComponents.MUTATION);
        return (mutation != null && !mutation.isBlank())
                || stack.get(TideTraitsComponents.MUTATION_SEED) != null
                || stack.get(TideTraitsComponents.SIZE_PERCENTILE) != null;
    }

    private static void setOptionalScores(ItemStack stack, SpecimenData specimen) {
        if (specimen.rawFishScore().isPresent()) {
            stack.set(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE, specimen.rawFishScore().getAsDouble());
        } else {
            stack.remove(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE);
        }
        if (specimen.fishScore().isPresent()) {
            stack.set(TideTraitsComponents.SPECIMEN_FISH_SCORE, specimen.fishScore().getAsInt());
        } else {
            stack.remove(TideTraitsComponents.SPECIMEN_FISH_SCORE);
        }
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
    }

    private interface Source {
        String string(String key);
        int integer(String key);
        long longValue(String key);
        double doubleValue(String key);
        boolean bool(String key);
        OptionalDouble optionalDouble(String key);
        OptionalInt optionalInt(String key);
    }

    private static final class ComponentSource implements Source {
        private final ItemStack stack;

        private ComponentSource(ItemStack stack) {
            this.stack = stack;
        }

        @Override public String string(String key) {
            return switch (key) {
                case SPECIES_ID_KEY -> stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
                case PERCENTILE_DEFINITION_KEY -> stack.get(TideTraitsComponents.SPECIMEN_PERCENTILE_DEFINITION);
                case BODY_TYPE_KEY -> stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE);
                case CONDITION_KEY -> stack.get(TideTraitsComponents.SPECIMEN_CONDITION);
                case PIGMENTATION_KEY -> stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION);
                case QUALITY_KEY -> stack.get(TideTraitsComponents.SPECIMEN_QUALITY);
                default -> throw new IllegalArgumentException("unknown string key " + key);
            };
        }

        @Override public int integer(String key) {
            Integer value = switch (key) {
                case SCHEMA_VERSION_KEY -> stack.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION);
                case GENERATION_VERSION_KEY -> stack.get(TideTraitsComponents.SPECIMEN_GENERATION_VERSION);
                default -> throw new IllegalArgumentException("unknown integer key " + key);
            };
            return require(value, key);
        }

        @Override public long longValue(String key) {
            return require(stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED), key);
        }

        @Override public double doubleValue(String key) {
            Double value = switch (key) {
                case BASE_PERCENTILE_KEY -> stack.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE);
                case BASE_LENGTH_KEY -> stack.get(TideTraitsComponents.SPECIMEN_BASE_LENGTH);
                case FINAL_LENGTH_KEY -> stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH);
                case FINAL_PERCENTILE_KEY -> stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE);
                default -> throw new IllegalArgumentException("unknown double key " + key);
            };
            return require(value, key);
        }

        @Override public boolean bool(String key) {
            return require(stack.get(TideTraitsComponents.SPECIMEN_PERFECT_CATCH), key);
        }

        @Override public OptionalDouble optionalDouble(String key) {
            Double value = stack.get(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE);
            return value == null ? OptionalDouble.empty() : OptionalDouble.of(value);
        }

        @Override public OptionalInt optionalInt(String key) {
            Integer value = stack.get(TideTraitsComponents.SPECIMEN_FISH_SCORE);
            return value == null ? OptionalInt.empty() : OptionalInt.of(value);
        }
    }

    private static final class NbtSource implements Source {
        private final NbtCompound nbt;

        private NbtSource(NbtCompound nbt) {
            this.nbt = nbt;
        }

        @Override public String string(String key) { return nbt.getString(key); }
        @Override public int integer(String key) { return nbt.getInt(key); }
        @Override public long longValue(String key) { return nbt.getLong(key); }
        @Override public double doubleValue(String key) { return nbt.getDouble(key); }
        @Override public boolean bool(String key) { return nbt.getBoolean(key); }
        @Override public OptionalDouble optionalDouble(String key) {
            return nbt.contains(key) ? OptionalDouble.of(nbt.getDouble(key)) : OptionalDouble.empty();
        }
        @Override public OptionalInt optionalInt(String key) {
            return nbt.contains(key) ? OptionalInt.of(nbt.getInt(key)) : OptionalInt.empty();
        }
    }

    private static <T> T require(T value, String key) {
        if (value == null) {
            throw new IllegalArgumentException("missing canonical field " + key);
        }
        return value;
    }
}
