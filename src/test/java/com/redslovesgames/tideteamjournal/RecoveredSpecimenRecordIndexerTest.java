package com.redslovesgames.tideteamjournal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.junit.jupiter.api.Test;

class RecoveredSpecimenRecordIndexerTest {
    @Test
    void recoveryCanImproveDerivedRecordsWithoutReplayingProgression() {
        UUID playerId = UUID.fromString("12345678-1234-5678-1234-567812345678");
        NbtCompound root = existingProgress(playerId);
        NbtCompound historyBefore = root.getList("history", 10).copy();
        NbtCompound contributorBefore = root.getCompound("contributors").getCompound(playerId.toString()).copy();
        SpecimenData recovered = specimen(0xABCDEF01L, 2750, 96.0, 61.0);

        assertTrue(RecoveredSpecimenRecordIndexer.index(root, recovered, playerId, "RecoveredAngler", 4));

        NbtCompound contributor = root.getCompound("contributors").getCompound(playerId.toString());
        assertEquals(contributorBefore.getInt("catches"), contributor.getInt("catches"));
        assertEquals(contributorBefore.getInt("record_events"), contributor.getInt("record_events"));
        assertEquals(contributorBefore.getList("species", 8), contributor.getList("species", 8));
        assertEquals(historyBefore, root.getList("history", 10));
        assertEquals(777, root.getInt("momentum_sentinel"));
        assertEquals(2750, StoredFishScoreStorage.readCanonical(contributor).orElseThrow());
        assertEquals(1, root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10).size());
        assertEquals(2750, JournalSpecimenStore.readFishScore(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
    }

    @Test
    void recoveryReindexIsIdempotentAndNeverCreatesDuplicateTopEntries() {
        UUID playerId = UUID.fromString("12345678-1234-5678-1234-567812345678");
        NbtCompound root = existingProgress(playerId);
        SpecimenData recovered = specimen(0xABCDEF01L, 2750, 96.0, 61.0);

        assertTrue(RecoveredSpecimenRecordIndexer.index(root, recovered, playerId, "RecoveredAngler", 4));
        NbtCompound once = root.copy();
        assertFalse(RecoveredSpecimenRecordIndexer.index(root, recovered, playerId, "RecoveredAngler", 4));
        assertEquals(once, root);
        assertEquals(1, root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10).size());
    }

    @Test
    void lowerRecoveredScoreCannotReplaceExistingPlayerOrBestSpecimenRecords() {
        UUID playerId = UUID.fromString("12345678-1234-5678-1234-567812345678");
        NbtCompound root = existingProgress(playerId);
        SpecimenData high = specimen(10L, 2800, 90.0, 60.0);
        SpecimenData low = specimen(20L, 1900, 99.0, 70.0);

        assertTrue(RecoveredSpecimenRecordIndexer.index(root, high, playerId, "RecoveredAngler", 5));
        RecoveredSpecimenRecordIndexer.index(root, low, playerId, "RecoveredAngler", 5);

        NbtCompound contributor = root.getCompound("contributors").getCompound(playerId.toString());
        assertEquals(2800, StoredFishScoreStorage.readCanonical(contributor).orElseThrow());
        assertEquals(2800, JournalSpecimenStore.readFishScore(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertEquals(2, root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10).size());
    }

    private static NbtCompound existingProgress(UUID playerId) {
        NbtCompound root = new NbtCompound();
        root.put("journal", new NbtCompound());
        root.putInt("momentum_sentinel", 777);

        NbtCompound contributor = new NbtCompound();
        contributor.putString("name", "OldName");
        contributor.putInt("catches", 42);
        contributor.putInt("record_events", 7);
        NbtList species = new NbtList();
        species.add(NbtString.of("tide:cod"));
        contributor.put("species", species);
        StoredFishScoreStorage.writeCanonical(contributor, 1400);
        contributor.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 1400);

        NbtCompound contributors = new NbtCompound();
        contributors.put(playerId.toString(), contributor);
        root.put("contributors", contributors);

        NbtCompound event = new NbtCompound();
        event.putString("event", "existing-history");
        event.putLong("timestamp", 111L);
        NbtList history = new NbtList();
        history.add(event);
        root.put("history", history);
        return root;
    }

    private static SpecimenData specimen(long seed, int score, double percentile, double length) {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                percentile - 2.0,
                length - 1.5,
                length,
                percentile,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                false,
                OptionalDouble.of(score / 3.0),
                OptionalInt.of(score),
                SpecimenData.Provenance.generated()
        );
    }
}
