package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class JournalSpecimenStoreTest {
    @Test
    void canonicalJournalSnapshotsRoundTripWithoutRecalculation() {
        NbtCompound root = new NbtCompound();
        SpecimenData specimen = specimen("tide:cod", 98.75, 44.5, 2876);

        assertTrue(JournalSpecimenStore.capture(root, specimen, true, true));

        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).orElseThrow());
        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).orElseThrow());
    }

    @Test
    void personalAndTeamJournalScoreConsumersReadPersistedCanonicalScoreExactly() {
        NbtCompound personalRoot = new NbtCompound();
        personalRoot.put("TidePlayerData", new NbtCompound());
        NbtCompound teamRoot = new NbtCompound();
        teamRoot.put("journal", new NbtCompound());
        SpecimenData intentionallyInconsistentWithOldFormula = specimen("tide:cod", 99.9, 88.0, 37);

        JournalSpecimenStore.capture(personalRoot, intentionallyInconsistentWithOldFormula, true, true);
        JournalSpecimenStore.capture(teamRoot, intentionallyInconsistentWithOldFormula, true, true);

        assertEquals(37, JournalSpecimenStore.readFishScore(personalRoot, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertEquals(37, JournalSpecimenStore.readFishScore(teamRoot, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
    }

    @Test
    void scorelessCanonicalJournalRecordStaysScorelessInsteadOfBeingRecalculated() {
        NbtCompound root = new NbtCompound();
        SpecimenData scoreless = scorelessSpecimen("tide:cod", 100.0, 95.0);

        JournalSpecimenStore.capture(root, scoreless, true, false);

        assertTrue(JournalSpecimenStore.readFishScore(root, "tide:cod", JournalSpecimenStore.LARGEST).isEmpty());
    }

    @Test
    void laterCatchRefreshesLatestWithoutOverwritingUnwonRecords() {
        NbtCompound root = new NbtCompound();
        SpecimenData record = specimen("tide:cod", 99.0, 50.0, 2900);
        SpecimenData ordinary = specimen("tide:cod", 55.0, 31.0, 1400);

        JournalSpecimenStore.capture(root, record, true, true);
        JournalSpecimenStore.capture(root, ordinary, false, false);

        assertSpecimen(ordinary, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).orElseThrow());
        assertSpecimen(record, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertSpecimen(record, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).orElseThrow());
    }

    @Test
    void legacyJournalDataIsPreservedAndNeverSynthesizedIntoCanonicalData() {
        NbtCompound root = new NbtCompound();
        NbtCompound legacyJournal = new NbtCompound();
        legacyJournal.putInt("amount_caught", 42);
        legacyJournal.putDouble("largest", 77.7);
        root.put("TidePlayerData", legacyJournal.copy());
        root.putString("legacy_team_marker", "keep-me");

        assertTrue(JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).isEmpty());
        assertFalse(root.contains(JournalSpecimenStore.ROOT_KEY));

        JournalSpecimenStore.capture(root, specimen("tide:cod", 70.0, 35.0, 1700), false, false);

        assertEquals(legacyJournal, root.getCompound("TidePlayerData"));
        assertEquals("keep-me", root.getString("legacy_team_marker"));
    }

    @Test
    void repeatedCaptureIsIdempotentAndSpeciesAreIsolated() {
        NbtCompound root = new NbtCompound();
        SpecimenData cod = specimen("tide:cod", 80.0, 38.0, 2000);
        SpecimenData tuna = specimen("tide:tuna", 90.0, 72.0, 2400);

        JournalSpecimenStore.capture(root, cod, true, false);
        NbtCompound once = root.copy();
        JournalSpecimenStore.capture(root, cod, true, false);
        assertEquals(once, root);

        JournalSpecimenStore.capture(root, tuna, false, true);
        assertSpecimen(cod, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertTrue(JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).isEmpty());
        assertSpecimen(tuna, JournalSpecimenStore.read(root, "tide:tuna", JournalSpecimenStore.SMALLEST).orElseThrow());
    }

    private static SpecimenData specimen(String species, double percentile, double length, int score) {
        return new SpecimenData(
                species,
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x1234_5678_9ABCL + score,
                percentile - 3.0,
                length - 2.5,
                length,
                percentile,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(score / 3.0),
                OptionalInt.of(score),
                SpecimenData.Provenance.generated()
        );
    }

    private static SpecimenData scorelessSpecimen(String species, double percentile, double length) {
        return new SpecimenData(
                species,
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x5C0E_1E55L,
                percentile,
                length,
                length,
                percentile,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }

    private static void assertSpecimen(SpecimenData expected, SpecimenData actual) {
        assertEquals(expected.speciesId(), actual.speciesId());
        assertEquals(expected.schemaVersion(), actual.schemaVersion());
        assertEquals(expected.generationVersion(), actual.generationVersion());
        assertEquals(expected.deterministicSeed(), actual.deterministicSeed());
        assertEquals(expected.basePercentile(), actual.basePercentile());
        assertEquals(expected.baseLength(), actual.baseLength());
        assertEquals(expected.finalLength(), actual.finalLength());
        assertEquals(expected.finalPercentile(), actual.finalPercentile());
        assertEquals(expected.bodyType(), actual.bodyType());
        assertEquals(expected.condition(), actual.condition());
        assertEquals(expected.pigmentation(), actual.pigmentation());
        assertEquals(expected.specimenQuality(), actual.specimenQuality());
        assertEquals(expected.perfectCatch(), actual.perfectCatch());
        assertEquals(expected.rawFishScore(), actual.rawFishScore());
        assertEquals(expected.fishScore(), actual.fishScore());
    }
}
