package com.redslovesgames.tideborne.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
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
        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertSpecimen(specimen, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).orElseThrow());
    }

    @Test
    void bestSpecimenUsesScoreThenPercentileThenLengthThenStableSeed() {
        NbtCompound root = new NbtCompound();
        SpecimenData highScore = specimen("tide:cod", 72.0, 42.0, 2200, 50L);
        SpecimenData lowerScoreLater = specimen("tide:cod", 99.0, 70.0, 2199, 40L);
        SpecimenData equalScoreHigherPercentile = specimen("tide:cod", 80.0, 43.0, 2200, 60L);
        SpecimenData equalScorePercentileLonger = specimen("tide:cod", 80.0, 44.0, 2200, 70L);
        SpecimenData exactTieLowerSeed = specimen("tide:cod", 80.0, 44.0, 2200, 10L);

        JournalSpecimenStore.capture(root, highScore, false, false);
        JournalSpecimenStore.capture(root, lowerScoreLater, false, false);
        assertSpecimen(highScore, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertSpecimen(lowerScoreLater, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).orElseThrow());

        JournalSpecimenStore.capture(root, equalScoreHigherPercentile, false, false);
        assertSpecimen(equalScoreHigherPercentile, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());

        JournalSpecimenStore.capture(root, equalScorePercentileLonger, false, false);
        assertSpecimen(equalScorePercentileLonger, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());

        JournalSpecimenStore.capture(root, exactTieLowerSeed, false, false);
        assertSpecimen(exactTieLowerSeed, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
    }

    @Test
    void recoveredBestIndexIsIdempotentAndDoesNotRewriteLatestOrSizeRecords() {
        NbtCompound root = new NbtCompound();
        SpecimenData oldCatch = specimen("tide:cod", 65.0, 34.0, 1500, 100L);
        SpecimenData recovered = specimen("tide:cod", 90.0, 48.0, 2500, 200L);

        JournalSpecimenStore.capture(root, oldCatch, true, true);
        assertTrue(JournalSpecimenStore.indexBest(root, recovered));
        NbtCompound once = root.copy();
        assertFalse(JournalSpecimenStore.indexBest(root, recovered));
        assertEquals(once, root);

        assertSpecimen(recovered, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertSpecimen(oldCatch, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).orElseThrow());
        assertSpecimen(oldCatch, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertSpecimen(oldCatch, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).orElseThrow());
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

        assertEquals(37, JournalSpecimenStore.readFishScore(personalRoot, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertEquals(37, JournalSpecimenStore.readFishScore(teamRoot, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
    }

    @Test
    void scorelessCanonicalJournalRecordStaysScorelessInsteadOfBeingRecalculated() {
        NbtCompound root = new NbtCompound();
        SpecimenData scoreless = scorelessSpecimen("tide:cod", 100.0, 95.0);

        JournalSpecimenStore.capture(root, scoreless, true, false);

        assertTrue(JournalSpecimenStore.readFishScore(root, "tide:cod", JournalSpecimenStore.LARGEST).isEmpty());
        assertTrue(JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).isEmpty());
    }

    @Test
    void laterCatchRefreshesLatestWithoutOverwritingBestOrUnwonRecords() {
        NbtCompound root = new NbtCompound();
        SpecimenData record = specimen("tide:cod", 99.0, 50.0, 2900);
        SpecimenData ordinary = specimen("tide:cod", 55.0, 31.0, 1400);

        JournalSpecimenStore.capture(root, record, true, true);
        JournalSpecimenStore.capture(root, ordinary, false, false);

        assertSpecimen(ordinary, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LATEST).orElseThrow());
        assertSpecimen(record, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
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

        assertTrue(JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).isEmpty());
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
        assertSpecimen(cod, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.BEST).orElseThrow());
        assertSpecimen(tuna, JournalSpecimenStore.read(root, "tide:tuna", JournalSpecimenStore.BEST).orElseThrow());
        assertSpecimen(cod, JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.LARGEST).orElseThrow());
        assertTrue(JournalSpecimenStore.read(root, "tide:cod", JournalSpecimenStore.SMALLEST).isEmpty());
        assertSpecimen(tuna, JournalSpecimenStore.read(root, "tide:tuna", JournalSpecimenStore.SMALLEST).orElseThrow());
    }

    private static SpecimenData specimen(String species, double percentile, double length, int score) {
        return specimen(species, percentile, length, score, 0x1234_5678_9ABCL + score);
    }

    private static SpecimenData specimen(String species, double percentile, double length, int score, long seed) {
        return new SpecimenData(
                species,
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                Math.max(0.0, percentile - 3.0),
                Math.max(0.0, length - 2.5),
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
