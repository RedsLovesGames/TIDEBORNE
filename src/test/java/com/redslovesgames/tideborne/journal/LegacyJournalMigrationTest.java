package com.redslovesgames.tideborne.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class LegacyJournalMigrationTest {
    @Test
    void aggregateRecordLengthUsesCanonicalMigrationServiceAndRunsOnce() {
        NbtCompound root = new NbtCompound();
        NbtCompound legacy = new NbtCompound();
        legacy.putDouble("largest", 77.7);
        legacy.putDouble("smallest", 19.25);
        root.put("TidePlayerData", legacy.copy());
        root.putString("legacy_marker", "keep-me");
        SpeciesProfile species = species();

        assertTrue(JournalSpecimenStore.migrateLegacyRecord(root, species, JournalSpecimenStore.LARGEST, 77.7));
        SpecimenData migrated = JournalSpecimenStore.read(root, species.speciesId(), JournalSpecimenStore.LARGEST).orElseThrow();

        assertEquals(SpecimenGenerator.SCHEMA_VERSION, migrated.schemaVersion());
        assertEquals(77.7, migrated.finalLength(), 0.0);
        assertEquals(SpecimenData.BodyType.NORMAL, migrated.bodyType());
        assertEquals(SpecimenData.Condition.NORMAL, migrated.condition());
        assertEquals(SpecimenData.Pigmentation.NORMAL, migrated.pigmentation());
        assertEquals(SpecimenData.SpecimenQuality.NORMAL, migrated.specimenQuality());
        assertTrue(migrated.rawFishScore().isEmpty());
        assertTrue(migrated.fishScore().isEmpty());
        assertTrue(JournalSpecimenStore.read(root, species.speciesId(), JournalSpecimenStore.LATEST).isEmpty(),
                "Migration invented a historical latest specimen that legacy aggregate stats do not contain");
        assertEquals(legacy, root.getCompound("TidePlayerData"));
        assertEquals("keep-me", root.getString("legacy_marker"));

        NbtCompound once = root.copy();
        assertFalse(JournalSpecimenStore.migrateLegacyRecord(root, species, JournalSpecimenStore.LARGEST, 77.7));
        assertEquals(once, root, "Repeated journal migration modified persisted data");
    }

    @Test
    void invalidAggregateLengthsFailClosedWithoutModifyingOldWorldData() {
        NbtCompound root = new NbtCompound();
        root.putString("legacy_marker", "preserve");
        NbtCompound before = root.copy();

        assertFalse(JournalSpecimenStore.migrateLegacyRecord(root, species(), JournalSpecimenStore.SMALLEST, Double.NaN));
        assertFalse(JournalSpecimenStore.migrateLegacyRecord(root, species(), JournalSpecimenStore.SMALLEST, -4.0));
        assertEquals(before, root);
    }

    private static SpeciesProfile species() {
        return new SpeciesProfile(
                "tide:test_fish",
                CanonicalRarity.ONE_STAR,
                1.0,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                new LogNormalSizeDistribution(40.0, 0.35),
                Set.of(),
                Map.of()
        );
    }
}
