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

class CanonicalSpecimenStorageTest {
    @Test
    void transferSerializationRoundTripPreservesEveryCanonicalField() {
        SpecimenData expected = specimen();
        NbtCompound transfer = new NbtCompound();

        CanonicalSpecimenStorage.writeTransferData(transfer, expected);
        SpecimenData decoded = CanonicalSpecimenStorage.readTransferData(transfer).orElseThrow();

        assertPersistedFields(expected, decoded);
    }

    @Test
    void transferPayloadDeclaresCanonicalPercentileDefinition() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());

        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        assertEquals(CanonicalSpecimenStorage.PERCENTILE_DEFINITION,
                canonical.getString("PercentileDefinition"));
        assertEquals(specimen().basePercentile(), canonical.getDouble("BasePercentile"));
        assertEquals(specimen().finalPercentile(), canonical.getDouble("FinalPercentile"));
    }

    @Test
    void transferReadRejectsUnknownPercentileDefinitionInsteadOfNormalizing() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        canonical.putString("PercentileDefinition", "natural_percentile");
        transfer.put("CanonicalSpecimen", canonical);

        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());
    }

    @Test
    void transferReadRejectsIncompletePayloadInsteadOfSynthesizingDefaults() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        canonical.remove("DeterministicSeed");
        transfer.put("CanonicalSpecimen", canonical);

        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());
    }

    @Test
    void transferReadRejectsOlderAndNewerSchemaVersions() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");

        canonical.putInt("SchemaVersion", SpecimenGenerator.SCHEMA_VERSION - 1);
        transfer.put("CanonicalSpecimen", canonical);
        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());

        canonical.putInt("SchemaVersion", SpecimenGenerator.SCHEMA_VERSION + 1);
        transfer.put("CanonicalSpecimen", canonical);
        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());
    }

    @Test
    void transferReadRejectsOlderAndNewerGenerationVersions() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");

        canonical.putInt("GenerationVersion", SpecimenGenerator.GENERATION_VERSION - 1);
        transfer.put("CanonicalSpecimen", canonical);
        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());

        canonical.putInt("GenerationVersion", SpecimenGenerator.GENERATION_VERSION + 1);
        transfer.put("CanonicalSpecimen", canonical);
        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());
    }

    @Test
    void absentPreFightScoresRoundTripAsAbsentRatherThanBeingCalculated() {
        SpecimenData expected = new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                99887766L,
                44.0,
                31.0,
                31.0,
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
        NbtCompound transfer = new NbtCompound();

        CanonicalSpecimenStorage.writeTransferData(transfer, expected);
        SpecimenData decoded = CanonicalSpecimenStorage.readTransferData(transfer).orElseThrow();

        assertFalse(decoded.rawFishScore().isPresent());
        assertFalse(decoded.fishScore().isPresent());
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x1234_5678_9ABCL,
                97.25,
                38.5,
                47.75,
                99.125,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(812.375),
                OptionalInt.of(2711),
                SpecimenData.Provenance.generated()
        );
    }

    private static void assertPersistedFields(SpecimenData expected, SpecimenData actual) {
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
