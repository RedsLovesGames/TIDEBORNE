package com.redslovesgames.tideteamjournal;

import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Canonical persisted FishScore storage for team-progress consumers.
 *
 * <p>{@code canonical_fish_score} is the only production score field read by Tideborne. The
 * historical {@code fish_score} field is consulted only by explicit migration when canonical
 * storage is absent. Compatibility payloads may still emit the historical field name for older
 * readers, but production code must not promote later compatibility writes back into canonical
 * state.
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

    /** Copies an explicit pre-V2 stored score forward exactly once, without recalculation. */
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

    /** Writes only canonical storage. Compatibility output fields are owned by their serializers. */
    public static void writeCanonical(NbtCompound tag, int score) {
        if (tag == null || score <= 0) {
            return;
        }
        tag.putInt(CANONICAL_SCORE_KEY, score);
    }

    /** Migrates persisted contributor/history/top-fish score fields once. */
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
                    changed |= migrateLegacyScore(tag);
                    contributors.put(key, tag);
                }
            }
            root.put(CONTRIBUTORS_KEY, contributors);
        }
        changed |= migrateList(root, HISTORY_KEY);
        changed |= migrateList(root, TOP_FISH_KEY);
        return changed;
    }

    private static boolean migrateList(NbtCompound root, String key) {
        if (!root.contains(key, NbtElement.LIST_TYPE)) {
            return false;
        }
        NbtList list = root.getList(key, NbtElement.COMPOUND_TYPE);
        boolean changed = false;
        for (NbtElement element : list) {
            if (element instanceof NbtCompound tag) {
                changed |= migrateLegacyScore(tag);
            }
        }
        return changed;
    }
}
