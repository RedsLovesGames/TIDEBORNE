package com.redslovesgames.tideteamjournal;

import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Canonical persisted FishScore storage for team-progress consumers.
 *
 * <p>The historical {@code fish_score} field is retained only as a compatibility mirror for
 * existing saves and client payloads. New consumers read {@code canonical_fish_score}. When an
 * old persisted record contains only {@code fish_score}, that exact integer is copied forward;
 * no legacy score formula is evaluated during migration.
 */
public final class StoredFishScoreStorage {
    public static final String CANONICAL_SCORE_KEY = "canonical_fish_score";
    public static final String LEGACY_SCORE_KEY = "fish_score";

    private static final int NUMERIC_NBT_TYPE = 99;
    private static final String CONTRIBUTORS_KEY = "contributors";
    private static final String HISTORY_KEY = "history";
    private static final String TOP_FISH_KEY = "top_fish";

    private StoredFishScoreStorage() {
    }

    public static OptionalInt readCanonical(NbtCompound tag) {
        if (tag == null || !tag.contains(CANONICAL_SCORE_KEY, NUMERIC_NBT_TYPE)) {
            return OptionalInt.empty();
        }
        int score = tag.getInt(CANONICAL_SCORE_KEY);
        return score > 0 ? OptionalInt.of(score) : OptionalInt.empty();
    }

    /** Copies an explicit old stored score into canonical storage exactly once. */
    public static boolean migrateLegacyScore(NbtCompound tag) {
        if (tag == null || readCanonical(tag).isPresent() || !tag.contains(LEGACY_SCORE_KEY, NUMERIC_NBT_TYPE)) {
            return false;
        }
        int stored = tag.getInt(LEGACY_SCORE_KEY);
        if (stored <= 0) {
            return false;
        }
        tag.putInt(CANONICAL_SCORE_KEY, stored);
        return true;
    }

    /** Makes the old field a mirror of canonical storage for unchanged clients/save readers. */
    public static boolean syncCompatibilityMirror(NbtCompound tag) {
        OptionalInt canonical = readCanonical(tag);
        if (canonical.isEmpty()) {
            return false;
        }
        int score = canonical.getAsInt();
        if (tag.contains(LEGACY_SCORE_KEY, NUMERIC_NBT_TYPE) && tag.getInt(LEGACY_SCORE_KEY) == score) {
            return false;
        }
        tag.putInt(LEGACY_SCORE_KEY, score);
        return true;
    }

    /**
     * Accepts a compatibility-field write only at a controlled write boundary whose value already
     * came from canonical catch state. This is never used as a score calculation path.
     */
    public static boolean acceptCompatibilityWrite(NbtCompound tag) {
        if (tag == null || !tag.contains(LEGACY_SCORE_KEY, NUMERIC_NBT_TYPE)) {
            return false;
        }
        int stored = tag.getInt(LEGACY_SCORE_KEY);
        if (stored <= 0) {
            return false;
        }
        OptionalInt canonical = readCanonical(tag);
        if (canonical.isPresent() && canonical.getAsInt() == stored) {
            return false;
        }
        tag.putInt(CANONICAL_SCORE_KEY, stored);
        return true;
    }

    public static void writeCanonical(NbtCompound tag, int score) {
        if (tag == null || score <= 0) {
            return;
        }
        tag.putInt(CANONICAL_SCORE_KEY, score);
        tag.putInt(LEGACY_SCORE_KEY, score);
    }

    /** Migrates every persisted team-progress score and repairs legacy mirrors from canonical data. */
    public static boolean migrateRoot(NbtCompound root) {
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (root.contains(CONTRIBUTORS_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound contributors = root.getCompound(CONTRIBUTORS_KEY);
            for (String key : contributors.getKeys()) {
                if (contributors.contains(key, NbtElement.COMPOUND_TYPE)) {
                    NbtCompound tag = contributors.getCompound(key);
                    changed |= migrateAndMirror(tag);
                    contributors.put(key, tag);
                }
            }
            root.put(CONTRIBUTORS_KEY, contributors);
        }
        changed |= migrateList(root, HISTORY_KEY, false);
        changed |= migrateList(root, TOP_FISH_KEY, false);
        return changed;
    }

    /** Captures known compatibility writes back into canonical persisted storage. */
    public static boolean acceptCompatibilityWrites(NbtCompound root) {
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (root.contains(CONTRIBUTORS_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound contributors = root.getCompound(CONTRIBUTORS_KEY);
            for (String key : contributors.getKeys()) {
                if (contributors.contains(key, NbtElement.COMPOUND_TYPE)) {
                    NbtCompound tag = contributors.getCompound(key);
                    changed |= acceptAndMirror(tag);
                    contributors.put(key, tag);
                }
            }
            root.put(CONTRIBUTORS_KEY, contributors);
        }
        changed |= migrateList(root, HISTORY_KEY, true);
        changed |= migrateList(root, TOP_FISH_KEY, true);
        return changed;
    }

    private static boolean migrateList(NbtCompound root, String key, boolean acceptWrite) {
        if (!root.contains(key, NbtElement.LIST_TYPE)) {
            return false;
        }
        NbtList list = root.getList(key, NbtElement.COMPOUND_TYPE);
        boolean changed = false;
        for (NbtElement element : list) {
            if (element instanceof NbtCompound tag) {
                changed |= acceptWrite ? acceptAndMirror(tag) : migrateAndMirror(tag);
            }
        }
        return changed;
    }

    private static boolean migrateAndMirror(NbtCompound tag) {
        boolean changed = migrateLegacyScore(tag);
        changed |= syncCompatibilityMirror(tag);
        return changed;
    }

    private static boolean acceptAndMirror(NbtCompound tag) {
        boolean changed = acceptCompatibilityWrite(tag);
        changed |= syncCompatibilityMirror(tag);
        return changed;
    }
}
