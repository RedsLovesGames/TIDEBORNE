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

class JournalSpecimenNetworkCodecTest {
    @Test
    void canonicalJournalProjectionRoundTripsDisplayFieldsOnly() {
        NbtCompound serverRoot = new NbtCompound();
        SpecimenData specimen = specimen();
        JournalSpecimenStore.capture(serverRoot, specimen, true, true);

        NbtCompound packet = new NbtCompound();
        NbtCompound holders = new NbtCompound();
        holders.putString("tide:cod", "holder");
        packet.put(JournalSpecimenNetworkCodec.RECORD_HOLDERS_KEY, holders);
        JournalSpecimenNetworkCodec.attachDisplayData(packet, serverRoot);

        JournalSpecimenNetworkCodec.DisplaySpecimen display = JournalSpecimenNetworkCodec
                .readDisplay(packet, "tide:cod", JournalSpecimenStore.LARGEST)
                .orElseThrow();

        assertEquals(specimen.speciesId(), display.speciesId());
        assertEquals(specimen.finalPercentile(), display.finalPercentile());
        assertEquals(specimen.finalLength(), display.finalLength());
        assertEquals(specimen.bodyType(), display.bodyType());
        assertEquals(specimen.condition(), display.condition());
        assertEquals(specimen.pigmentation(), display.pigmentation());
        assertEquals(specimen.specimenQuality(), display.specimenQuality());
        assertEquals(specimen.perfectCatch(), display.perfectCatch());
        assertEquals(specimen.rawFishScore(), display.rawFishScore());
        assertEquals(specimen.fishScore(), display.fishScore());

        NbtCompound encoded = packet.getCompound(JournalSpecimenNetworkCodec.CLIENT_KEY)
                .getCompound("tide:cod")
                .getCompound(JournalSpecimenStore.LARGEST);
        assertFalse(encoded.contains("DeterministicSeed"));
        assertFalse(encoded.contains("BasePercentile"));
        assertFalse(encoded.contains("BaseLength"));
        assertFalse(encoded.contains("SchemaVersion"));
        assertFalse(encoded.contains("GenerationVersion"));
        assertFalse(encoded.contains("CanonicalSpecimen"));
    }

    @Test
    void dedicatedPayloadDropsDuplicateJournalAndServerPersistenceData() {
        NbtCompound source = new NbtCompound();
        source.putString("legacy_journal_field", "must-not-be-duplicated");
        source.put(JournalSpecimenStore.ROOT_KEY, new NbtCompound());
        NbtCompound holders = new NbtCompound();
        holders.putString("tide:cod", "holder");
        source.put(JournalSpecimenNetworkCodec.RECORD_HOLDERS_KEY, holders);

        NbtCompound display = new NbtCompound();
        display.put("tide:cod", new NbtCompound());
        source.put(JournalSpecimenNetworkCodec.CLIENT_KEY, display);

        NbtCompound sanitized = JournalSpecimenNetworkCodec.sanitizeRecordHoldersPayload(source);

        assertEquals(2, sanitized.getKeys().size());
        assertTrue(sanitized.contains(JournalSpecimenNetworkCodec.RECORD_HOLDERS_KEY, 10));
        assertTrue(sanitized.contains(JournalSpecimenNetworkCodec.CLIENT_KEY, 10));
        assertFalse(sanitized.contains("legacy_journal_field"));
        assertFalse(sanitized.contains(JournalSpecimenStore.ROOT_KEY));
    }

    @Test
    void legacyAndIncompleteStoredDataNeverSynthesizesClientSpecimen() {
        NbtCompound legacyRoot = new NbtCompound();
        legacyRoot.putString("legacy", "preserve");
        NbtCompound packet = new NbtCompound();
        JournalSpecimenNetworkCodec.attachDisplayData(packet, legacyRoot);
        assertFalse(packet.contains(JournalSpecimenNetworkCodec.CLIENT_KEY));
        assertTrue(JournalSpecimenNetworkCodec.readDisplay(packet, "tide:cod", JournalSpecimenStore.LATEST).isEmpty());

        NbtCompound incompleteRoot = new NbtCompound();
        NbtCompound allSpecies = new NbtCompound();
        NbtCompound cod = new NbtCompound();
        NbtCompound incompleteSnapshot = new NbtCompound();
        incompleteSnapshot.putString("not_a_canonical_specimen", "old-data");
        cod.put(JournalSpecimenStore.LATEST, incompleteSnapshot);
        allSpecies.put("tide:cod", cod);
        incompleteRoot.put(JournalSpecimenStore.ROOT_KEY, allSpecies);

        JournalSpecimenNetworkCodec.attachDisplayData(packet, incompleteRoot);
        assertFalse(packet.contains(JournalSpecimenNetworkCodec.CLIENT_KEY));
    }

    @Test
    void missingOptionalScoresRemainMissingWithoutRecalculation() {
        SpecimenData unscored = new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                42L,
                40.0,
                20.0,
                21.0,
                44.0,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
        NbtCompound root = new NbtCompound();
        JournalSpecimenStore.capture(root, unscored, false, false);
        NbtCompound packet = new NbtCompound();
        JournalSpecimenNetworkCodec.attachDisplayData(packet, root);

        JournalSpecimenNetworkCodec.DisplaySpecimen display = JournalSpecimenNetworkCodec
                .readDisplay(packet, "tide:cod", JournalSpecimenStore.LATEST)
                .orElseThrow();
        assertTrue(display.rawFishScore().isEmpty());
        assertTrue(display.fishScore().isEmpty());
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x55AA_1234_7788L,
                93.0,
                38.0,
                44.5,
                98.75,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(958.6666666667),
                OptionalInt.of(2876),
                SpecimenData.Provenance.generated()
        );
    }
}
