package com.redslovesgames.tideborne.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redslovesgames.tideborne.journal.StoredFishScoreStorage;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class HistoryBadgeMetaCanonicalTest {
    @Test
    void eventMetadataPreservesCanonicalAxesAndScoreWithoutCollapsingThem() {
        UUID event = UUID.randomUUID();
        NbtCompound source = new NbtCompound();
        source.putString("body_type", "giant");
        source.putString("condition", "scarred");
        source.putString("pigmentation", "iridescent");
        source.putString("quality", "perfect_specimen");
        source.putDouble("percentile", 98.7);
        source.putDouble("length", 47.5);
        StoredFishScoreStorage.writeCanonical(source, 2888);

        HistoryBadgeMeta.capture(source);
        HistoryBadgeMeta.bindCurrent(event, "LARGEST");
        HistoryBadgeMeta.finish();

        NbtCompound projected = new NbtCompound();
        HistoryBadgeMeta.write(event, projected);

        assertEquals("giant", projected.getString("body_type"));
        assertEquals("scarred", projected.getString("condition"));
        assertEquals("scarred", projected.getString("mutation"));
        assertEquals("iridescent", projected.getString("pigmentation"));
        assertEquals("perfect_specimen", projected.getString("quality"));
        assertEquals(98.7, projected.getDouble("percentile"));
        assertEquals(47.5, projected.getDouble("length"));
        assertEquals(2888, StoredFishScoreStorage.readCanonical(projected).orElseThrow());
    }
}
