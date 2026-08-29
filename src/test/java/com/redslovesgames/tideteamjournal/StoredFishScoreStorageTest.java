package com.redslovesgames.tideteamjournal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Test;

class StoredFishScoreStorageTest {
    @Test
    void explicitLegacyScoreMigratesWithoutRecalculation() {
        NbtCompound tag = new NbtCompound();
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 1876);
        tag.putDouble("length", 1.0);
        tag.putDouble("percentile", 100.0);
        tag.putString("mutation", "perfect_specimen");

        assertTrue(StoredFishScoreStorage.migrateLegacyScore(tag));
        assertEquals(OptionalInt.of(1876), StoredFishScoreStorage.readCanonical(tag));
        assertEquals(1876, tag.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY));
    }

    @Test
    void canonicalScoreWinsWithoutReadingOrRepairingCompatibilityField() {
        NbtCompound tag = new NbtCompound();
        tag.putInt(StoredFishScoreStorage.CANONICAL_SCORE_KEY, 2400);
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 17);

        assertFalse(StoredFishScoreStorage.migrateLegacyScore(tag));
        assertEquals(OptionalInt.of(2400), StoredFishScoreStorage.readCanonical(tag));
        assertEquals(17, tag.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY));
    }

    @Test
    void canonicalWritesDoNotCreateLegacyMirrors() {
        NbtCompound tag = new NbtCompound();

        StoredFishScoreStorage.writeCanonical(tag, 1250);

        assertEquals(OptionalInt.of(1250), StoredFishScoreStorage.readCanonical(tag));
        assertFalse(tag.contains(StoredFishScoreStorage.LEGACY_SCORE_KEY));
    }

    @Test
    void laterLegacyWriteCannotReplaceCanonicalScore() {
        NbtCompound tag = new NbtCompound();
        StoredFishScoreStorage.writeCanonical(tag, 1250);
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 2900);

        assertFalse(StoredFishScoreStorage.migrateLegacyScore(tag));
        assertEquals(OptionalInt.of(1250), StoredFishScoreStorage.readCanonical(tag));
    }

    @Test
    void rootMigrationCoversContributorsHistoryAndTopFishButNotMissingScores() {
        NbtCompound root = new NbtCompound();
        NbtCompound contributors = new NbtCompound();
        NbtCompound contributor = new NbtCompound();
        contributor.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 900);
        contributors.put(UUID.randomUUID().toString(), contributor);
        root.put("contributors", contributors);

        NbtList history = new NbtList();
        NbtCompound scoredEvent = new NbtCompound();
        scoredEvent.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 1200);
        history.add(scoredEvent);
        NbtCompound scorelessRepair = new NbtCompound();
        scorelessRepair.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, -1);
        history.add(scorelessRepair);
        root.put("history", history);

        NbtList topFish = new NbtList();
        NbtCompound fish = new NbtCompound();
        fish.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 2100);
        topFish.add(fish);
        root.put("top_fish", topFish);

        assertTrue(StoredFishScoreStorage.migrateRoot(root));
        NbtCompound migratedContributor = root.getCompound("contributors")
                .getCompound(root.getCompound("contributors").getKeys().iterator().next());
        assertEquals(900, StoredFishScoreStorage.readCanonical(migratedContributor).orElseThrow());
        assertEquals(1200, StoredFishScoreStorage.readCanonical((NbtCompound) root.getList("history", 10).get(0)).orElseThrow());
        assertTrue(StoredFishScoreStorage.readCanonical((NbtCompound) root.getList("history", 10).get(1)).isEmpty());
        assertEquals(2100, StoredFishScoreStorage.readCanonical((NbtCompound) root.getList("top_fish", 10).get(0)).orElseThrow());
    }

    @Test
    void rootMigrationLeavesExistingCanonicalScoresUntouched() {
        NbtCompound root = new NbtCompound();
        NbtList history = new NbtList();
        NbtCompound event = new NbtCompound();
        event.putInt(StoredFishScoreStorage.CANONICAL_SCORE_KEY, 2666);
        event.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 42);
        history.add(event);
        root.put("history", history);

        assertFalse(StoredFishScoreStorage.migrateRoot(root));
        assertEquals(2666, StoredFishScoreStorage.readCanonical(event).orElseThrow());
        assertEquals(42, event.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY));
    }

    @Test
    void migratingStoredScoresPreservesDescendingRecordOrder() {
        List<NbtCompound> records = new ArrayList<>();
        records.add(record("first", 1700));
        records.add(record("second", 2900));
        records.add(record("third", 2200));

        List<String> before = records.stream()
                .sorted(Comparator.comparingInt((NbtCompound tag) -> tag.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY)).reversed())
                .map(tag -> tag.getString("id"))
                .toList();
        records.forEach(StoredFishScoreStorage::migrateLegacyScore);
        List<String> after = records.stream()
                .sorted(Comparator.comparingInt((NbtCompound tag) -> StoredFishScoreStorage.readCanonical(tag).orElse(0)).reversed())
                .map(tag -> tag.getString("id"))
                .toList();

        assertEquals(List.of("second", "third", "first"), before);
        assertEquals(before, after);
    }

    private static NbtCompound record(String id, int score) {
        NbtCompound tag = new NbtCompound();
        tag.putString("id", id);
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, score);
        return tag;
    }
}
