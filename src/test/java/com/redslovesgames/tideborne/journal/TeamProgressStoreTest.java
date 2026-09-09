package com.redslovesgames.tideborne.journal;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeamProgressStoreTest {
    @Test
    void mergeKeepsTwoContributorsAndSortsDeduplicatedHistoryWithItsMetadata() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        NbtCompound target = root(first, 800, 9000);
        NbtCompound source = root(second, 0, 700);
        target.putLong("tracking_started_ms", 200);
        source.putLong("tracking_started_ms", 100);
        NbtCompound firstContribution = target.getCompound("contributors").getCompound(first.toString());
        firstContribution.putInt("catches", Integer.MAX_VALUE);
        firstContribution.putInt("record_events", 3);
        NbtCompound sourceContribution = source.getCompound("contributors").getCompound(second.toString());
        sourceContribution.putInt("catches", 7);
        sourceContribution.putString("name", "second");
        NbtCompound old = historyRow(first, 10, 256, 9999);
        old.putString("quality", "pristine");
        NbtCompound recent = historyRow(second, 20, 512, 1);
        NbtList targetHistory = new NbtList();
        targetHistory.add(old);
        target.put("history", targetHistory);
        NbtList sourceHistory = new NbtList();
        sourceHistory.add(recent);
        sourceHistory.add(old.copy());
        source.put("history", sourceHistory);

        TeamProgressStore.mergeTrackedDataOnce(target, source, 20);

        assertEquals(100, target.getLong("tracking_started_ms"));
        NbtCompound contributors = target.getCompound("contributors");
        assertEquals(2, contributors.getKeys().size());
        assertEquals(Integer.MAX_VALUE, contributors.getCompound(first.toString()).getInt("catches"));
        assertEquals(3, contributors.getCompound(first.toString()).getInt("record_events"));
        assertEquals(7, contributors.getCompound(second.toString()).getInt("catches"));
        assertEquals(700, contributors.getCompound(second.toString()).getInt("canonical_fish_score"));
        NbtList history = target.getList("history", 10);
        assertEquals(2, history.size());
        assertEquals(20, history.getCompound(0).getLong("timestamp"));
        assertEquals(512, history.getCompound(0).getInt("fish_score"));
        assertEquals(256, history.getCompound(1).getInt("canonical_fish_score"));
        assertEquals(256, history.getCompound(1).getInt("fish_score"));
        assertEquals("pristine", history.getCompound(1).getString("quality"));
        assertEquals(110, history.getCompound(1).getLong("game_time"));
        assertFalse(history.getCompound(1).contains("tideborne_specimen"));
        TeamProgressStore.trim(target, 1);
        assertEquals(recent.getUuid("id"), target.getList("history", 10).getCompound(0).getUuid("id"));
    }

    @Test
    void scoreOnlyRowsMigrateOnceWithoutSynthesizingSpecimensOrTouchingOtherRows() {
        UUID player = UUID.randomUUID();
        NbtCompound root = root(player, 0, 701);
        NbtCompound scored = new NbtCompound();
        scored.putInt("fish_score", 500);
        NbtCompound scoreless = new NbtCompound();
        scoreless.putDouble("length", 999);
        NbtList top = new NbtList();
        top.add(scored);
        top.add(scoreless);
        root.put("top_fish", top);
        assertTrue(TeamProgressStore.ensureInitialized(root));
        assertEquals(java.util.Set.of("fish_score", "canonical_fish_score"), scored.getKeys());
        assertEquals(java.util.Set.of("length"), scoreless.getKeys());
        scored.putInt("fish_score", 9999);
        assertTrue(TeamProgressStore.ensureInitialized(root));
        assertEquals(500, scored.getInt("fish_score"));
        assertFalse(TeamProgressStore.ensureInitialized(root));
        NbtCompound missing = root(UUID.randomUUID(), 0, 0);
        TeamProgressStore.mergeTrackedDataOnce(missing, new NbtCompound(), 20);
        assertFalse(missing.getCompound("contributors").getCompound(
                missing.getCompound("contributors").getKeys().iterator().next()).contains("canonical_fish_score"));
    }

    private static NbtCompound historyRow(UUID player, long timestamp, int canonical, int legacy) {
        NbtCompound tag = new TeamProgressStore.RecordEvent(UUID.randomUUID(), TeamProgressStore.EventType.REPAIR,
                player, "angler", player, "angler", "minecraft:cod", 35, 30, timestamp, timestamp + 100).toTag();
        StoredFishScoreStorage.writeCanonical(tag, canonical);
        tag.putInt("fish_score", legacy);
        return tag;
    }

    @Test
    void directMergeUsesCanonicalScoresAndPreservesLegacyMirrors() {
        UUID id = UUID.randomUUID();
        NbtCompound target = root(id, 2001, 9001);
        NbtCompound source = root(id, 1501, 9901);
        TeamProgressStore.mergeTrackedDataOnce(target, source, 20);
        NbtCompound contributor = target.getCompound("contributors").getCompound(id.toString());
        assertEquals(2001, contributor.getInt("canonical_fish_score"));
        assertEquals(2001, contributor.getInt("fish_score"));
        assertEquals(1501, source.getCompound("contributors").getCompound(id.toString()).getInt("fish_score"));
        TeamProgressStore.tideborneRegisterContributorFishScore(id, contributor);
        NbtCompound payload = new TeamProgressStore.Contributor(id, "angler", 2, 1, 0, 0, false).toTag();
        assertEquals(2001, payload.getInt("canonical_fish_score"));
        assertEquals(2001, payload.getInt("fish_score"));
        assertFalse(TeamProgressStore.ensureInitialized(target));
    }

    @Test
    void initializationReportsMigrationAndMirrorChangesOnce() {
        NbtCompound root = root(UUID.randomUUID(), 701, 999);
        assertTrue(TeamProgressStore.ensureInitialized(root));
        assertFalse(TeamProgressStore.ensureInitialized(root));
        assertTrue(TeamProgressStore.tideborneFishScore(null) < 0);
    }

    @Test
    void legacyHistoryRoundTripsScoresAndTraitsWithoutInventingMissingScores() {
        UUID id = UUID.randomUUID();
        TeamProgressStore.RecordEvent event = new TeamProgressStore.RecordEvent(UUID.randomUUID(),
                TeamProgressStore.EventType.REPAIR, id, "angler", id, "angler", "minecraft:cod", 35, 30, 42, 43);
        NbtCompound legacy = event.toTag();
        legacy.remove("canonical_fish_score");
        legacy.putInt("fish_score", 987);
        legacy.putString("condition", "scarred");
        legacy.putDouble("percentile", 77.5);
        NbtList history = new NbtList();
        history.add(legacy);
        NbtCompound root = root(id, 0, 0);
        root.put("history", history);
        NbtCompound serialized = TeamProgressStore.readHistory(root).getFirst().toTag();
        assertEquals(987, serialized.getInt("canonical_fish_score"));
        assertEquals(987, serialized.getInt("fish_score"));
        assertEquals("scarred", serialized.getString("condition"));
        assertEquals(77.5, serialized.getDouble("percentile"));
        NbtCompound source = root.copy();
        TeamProgressStore.mergeTrackedDataOnce(root, source, 20);
        assertEquals(1, root.getList("history", 10).size());
        NbtCompound missing = new TeamProgressStore.RecordEvent(UUID.randomUUID(),
                TeamProgressStore.EventType.REPAIR, id, "angler", id, "angler", "minecraft:cod", 1, 0, 44, 45).toTag();
        assertFalse(missing.contains("canonical_fish_score"));
    }

    private static NbtCompound root(UUID id, int canonical, int legacy) {
        NbtCompound contributor = new NbtCompound();
        StoredFishScoreStorage.writeCanonical(contributor, canonical);
        contributor.putInt("fish_score", legacy);
        NbtCompound contributors = new NbtCompound();
        contributors.put(id.toString(), contributor);
        NbtCompound root = new NbtCompound();
        root.put("contributors", contributors);
        return root;
    }
}
