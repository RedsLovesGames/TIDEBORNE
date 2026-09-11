package com.redslovesgames.tideborne.journal;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyPersistenceIds;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;

/** Server-to-client projection for canonical Journal specimens. */
public final class JournalSpecimenNetworkCodec {
    public static final String CLIENT_KEY = LegacyPersistenceIds.JOURNAL_CANONICAL_SPECIMENS;
    public static final String RECORD_HOLDERS_KEY = LegacyPersistenceIds.JOURNAL_RECORD_HOLDERS;
    public static final int DISPLAY_SCHEMA_VERSION = 1;

    private static final String DISPLAY_SCHEMA_KEY = "display_schema";
    private static final String SPECIES_ID_KEY = "species_id";
    private static final String FINAL_PERCENTILE_KEY = "final_percentile";
    private static final String FINAL_LENGTH_KEY = "final_length";
    private static final String BODY_TYPE_KEY = "body_type";
    private static final String CONDITION_KEY = "condition";
    private static final String PIGMENTATION_KEY = "pigmentation";
    private static final String QUALITY_KEY = "quality";
    private static final String PERFECT_CATCH_KEY = "perfect_catch";
    private static final String RAW_FISH_SCORE_KEY = "raw_fish_score";
    private static final String FISH_SCORE_KEY = "fish_score";
    private static final String[] RECORD_KINDS = {
            JournalSpecimenStore.BEST,
            JournalSpecimenStore.LATEST,
            JournalSpecimenStore.LARGEST,
            JournalSpecimenStore.SMALLEST
    };

    private JournalSpecimenNetworkCodec() {
    }

    /** Builds display data after any one-time server-side legacy journal record backfill. */
    public static NbtCompound buildDisplayData(NbtCompound journalRoot) {
        NbtCompound display = new NbtCompound();
        if (journalRoot == null) {
            return display;
        }
        JournalSpecimenStore.migrateLegacyJournal(journalRoot);
        if (!journalRoot.contains(JournalSpecimenStore.ROOT_KEY, 10)) {
            return display;
        }

        NbtCompound stored = journalRoot.getCompound(JournalSpecimenStore.ROOT_KEY);
        for (String speciesId : stored.getKeys()) {
            NbtCompound speciesDisplay = new NbtCompound();
            for (String recordKind : RECORD_KINDS) {
                JournalSpecimenStore.read(journalRoot, speciesId, recordKind)
                        .ifPresent(specimen -> speciesDisplay.put(recordKind, encode(specimen)));
            }
            if (!speciesDisplay.getKeys().isEmpty()) {
                display.put(speciesId, speciesDisplay);
            }
        }
        return display;
    }

    public static void attachDisplayData(NbtCompound packetTag, NbtCompound journalRoot) {
        if (packetTag == null) {
            return;
        }
        NbtCompound display = buildDisplayData(journalRoot);
        if (display.getKeys().isEmpty()) {
            packetTag.remove(CLIENT_KEY);
        } else {
            packetTag.put(CLIENT_KEY, display);
        }
    }

    public static NbtCompound sanitizeRecordHoldersPayload(NbtCompound source) {
        NbtCompound result = new NbtCompound();
        if (source == null) {
            return result;
        }
        if (source.contains(RECORD_HOLDERS_KEY, 10)) {
            result.put(RECORD_HOLDERS_KEY, source.getCompound(RECORD_HOLDERS_KEY).copy());
        }
        if (source.contains(CLIENT_KEY, 10)) {
            result.put(CLIENT_KEY, source.getCompound(CLIENT_KEY).copy());
        }
        return result;
    }

    public static Optional<DisplaySpecimen> readDisplay(NbtCompound packetTag, String speciesId, String recordKind) {
        if (packetTag == null || speciesId == null || speciesId.isBlank() || recordKind == null || recordKind.isBlank()) {
            return Optional.empty();
        }
        if (!packetTag.contains(CLIENT_KEY, 10)) {
            return Optional.empty();
        }
        NbtCompound allSpecies = packetTag.getCompound(CLIENT_KEY);
        if (!allSpecies.contains(speciesId, 10)) {
            return Optional.empty();
        }
        NbtCompound species = allSpecies.getCompound(speciesId);
        if (!species.contains(recordKind, 10)) {
            return Optional.empty();
        }
        return decode(species.getCompound(recordKind));
    }

    private static NbtCompound encode(SpecimenData specimen) {
        NbtCompound tag = new NbtCompound();
        tag.putInt(DISPLAY_SCHEMA_KEY, DISPLAY_SCHEMA_VERSION);
        tag.putString(SPECIES_ID_KEY, specimen.speciesId());
        tag.putDouble(FINAL_PERCENTILE_KEY, specimen.finalPercentile());
        tag.putDouble(FINAL_LENGTH_KEY, specimen.finalLength());
        tag.putString(BODY_TYPE_KEY, serialized(specimen.bodyType()));
        tag.putString(CONDITION_KEY, serialized(specimen.condition()));
        tag.putString(PIGMENTATION_KEY, serialized(specimen.pigmentation()));
        tag.putString(QUALITY_KEY, serialized(specimen.specimenQuality()));
        tag.putBoolean(PERFECT_CATCH_KEY, specimen.perfectCatch());
        specimen.rawFishScore().ifPresent(value -> tag.putDouble(RAW_FISH_SCORE_KEY, value));
        specimen.fishScore().ifPresent(value -> tag.putInt(FISH_SCORE_KEY, value));
        return tag;
    }

    private static Optional<DisplaySpecimen> decode(NbtCompound tag) {
        try {
            if (tag.getInt(DISPLAY_SCHEMA_KEY) != DISPLAY_SCHEMA_VERSION
                    || !tag.contains(SPECIES_ID_KEY)
                    || tag.getString(SPECIES_ID_KEY).isBlank()
                    || !tag.contains(FINAL_PERCENTILE_KEY)
                    || !tag.contains(FINAL_LENGTH_KEY)
                    || !tag.contains(BODY_TYPE_KEY)
                    || !tag.contains(CONDITION_KEY)
                    || !tag.contains(PIGMENTATION_KEY)
                    || !tag.contains(QUALITY_KEY)
                    || !tag.contains(PERFECT_CATCH_KEY)) {
                return Optional.empty();
            }

            double finalPercentile = tag.getDouble(FINAL_PERCENTILE_KEY);
            double finalLength = tag.getDouble(FINAL_LENGTH_KEY);
            if (!Double.isFinite(finalPercentile) || finalPercentile < 0.0 || finalPercentile > 100.0
                    || !Double.isFinite(finalLength) || finalLength <= 0.0) {
                return Optional.empty();
            }

            return Optional.of(new DisplaySpecimen(
                    tag.getString(SPECIES_ID_KEY),
                    finalPercentile,
                    finalLength,
                    enumValue(SpecimenData.BodyType.class, tag.getString(BODY_TYPE_KEY)),
                    enumValue(SpecimenData.Condition.class, tag.getString(CONDITION_KEY)),
                    enumValue(SpecimenData.Pigmentation.class, tag.getString(PIGMENTATION_KEY)),
                    enumValue(SpecimenData.SpecimenQuality.class, tag.getString(QUALITY_KEY)),
                    tag.getBoolean(PERFECT_CATCH_KEY),
                    tag.contains(RAW_FISH_SCORE_KEY) ? OptionalDouble.of(tag.getDouble(RAW_FISH_SCORE_KEY)) : OptionalDouble.empty(),
                    tag.contains(FISH_SCORE_KEY) ? OptionalInt.of(tag.getInt(FISH_SCORE_KEY)) : OptionalInt.empty()
            ));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
    }

    public record DisplaySpecimen(
            String speciesId,
            double finalPercentile,
            double finalLength,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality specimenQuality,
            boolean perfectCatch,
            OptionalDouble rawFishScore,
            OptionalInt fishScore
    ) {
    }
}
