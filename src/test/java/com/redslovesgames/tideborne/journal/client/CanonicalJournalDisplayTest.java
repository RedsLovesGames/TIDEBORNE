package com.redslovesgames.tideborne.journal.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.journal.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.journal.JournalSpecimenStore;
import com.redslovesgames.tideborne.journal.StoredFishScoreStorage;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CanonicalJournalDisplayTest {
    @AfterEach
    void clearClientCache() {
        ClientJournalSpecimens.clearForTest();
    }

    @Test
    void clientJournalReadsOnlyServerProjectedCanonicalSpecimen() {
        NbtCompound root = new NbtCompound();
        JournalSpecimenStore.capture(root, specimen(), true, true);
        NbtCompound packet = new NbtCompound();
        JournalSpecimenNetworkCodec.attachDisplayData(packet, root);
        ClientJournalSpecimens.update(packet);

        JournalSpecimenNetworkCodec.DisplaySpecimen display = ClientJournalSpecimens
                .read(Identifier.of("tide", "cod"), JournalSpecimenStore.LATEST)
                .orElseThrow();

        assertEquals(98.25, display.finalPercentile());
        assertEquals(SpecimenData.BodyType.GIANT, display.bodyType());
        assertEquals(SpecimenData.Condition.SCARRED, display.condition());
        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, display.pigmentation());
        assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, display.specimenQuality());
        assertEquals(2777, display.fishScore().orElseThrow());
    }

    @Test
    void recordDisplayNeverTreatsLegacyScoreMirrorAsCanonicalAuthority() {
        NbtCompound tag = new NbtCompound();
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, 2999);
        tag.putString("body_type", "giant");
        CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElseThrow();

        assertTrue(display.score().isEmpty());
        assertEquals("N/A", display.scoreLabel());
        assertEquals("Giant", display.bodyTypeLabel());
    }

    @Test
    void recordDisplayKeepsFourCanonicalAxesIndependent() {
        NbtCompound tag = new NbtCompound();
        StoredFishScoreStorage.writeCanonical(tag, 2444);
        tag.putString("body_type", "dwarf");
        tag.putString("condition", "parasite_ridden");
        tag.putString("pigmentation", "albino");
        tag.putString("quality", "perfect_specimen");
        tag.putDouble("percentile", 7.5);
        tag.putDouble("length", 12.0);

        CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElseThrow();
        assertEquals(2444, display.score().orElseThrow());
        assertEquals("Dwarf", display.bodyTypeLabel());
        assertEquals("Parasite Ridden", display.conditionLabel());
        assertEquals("Albino", display.pigmentationLabel());
        assertEquals("Perfect Specimen", display.qualityLabel());
        assertEquals(7.5, display.percentile());
        assertEquals(12.0, display.length());
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                12345L,
                92.0,
                35.0,
                46.0,
                98.25,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(800.0),
                OptionalInt.of(2777),
                SpecimenData.Provenance.generated()
        );
    }
}
