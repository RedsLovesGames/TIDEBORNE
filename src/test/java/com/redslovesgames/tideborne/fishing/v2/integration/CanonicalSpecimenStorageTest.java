package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class CanonicalSpecimenStorageTest {
    @Test
    void canonicalRoundTripPreservesEveryPersistedField() {
        ItemStack stack = new ItemStack(Items.COD);
        SpecimenData expected = specimen();

        CanonicalSpecimenStorage.write(stack, expected);
        Optional<SpecimenData> read = CanonicalSpecimenStorage.read(stack);

        assertTrue(read.isPresent());
        assertPersistedFields(expected, read.orElseThrow());
        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertEquals(CanonicalSpecimenStorage.PERCENTILE_DEFINITION,
                stack.get(TideTraitsComponents.SPECIMEN_PERCENTILE_DEFINITION));
    }

    @Test
    void canonicalComponentsOwnSeedPercentileAndLengthWhileLegacyValuesAreMirrorsOnly() {
        ItemStack stack = new ItemStack(Items.COD);
        SpecimenData expected = specimen();
        CanonicalSpecimenStorage.write(stack, expected);

        stack.set(TideTraitsComponents.MUTATION_SEED, -99L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 1.0);
        stack.set(TideTraitsComponents.BODY_TYPE, "normal");
        stack.set(TideTraitsComponents.MUTATION, "normal");
        TideItemData.FISH_LENGTH.set(stack, 0.5);

        SpecimenData read = CanonicalSpecimenStorage.read(stack).orElseThrow();
        assertPersistedFields(expected, read);
        assertEquals(expected.deterministicSeed(), stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED));
        assertEquals(expected.finalPercentile(), stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE));
        assertEquals(expected.finalLength(), stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH));
    }

    @Test
    void readIsSideEffectFreeAndDoesNotRepairStaleMirrors() {
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen());
        stack.set(TideTraitsComponents.BODY_TYPE, "normal");
        stack.set(TideTraitsComponents.MUTATION_SEED, 123L);

        CanonicalSpecimenStorage.read(stack).orElseThrow();

        assertEquals("normal", stack.get(TideTraitsComponents.BODY_TYPE));
        assertEquals(123L, stack.get(TideTraitsComponents.MUTATION_SEED));
    }

    @Test
    void incompleteCanonicalPayloadIsDetectedAndNeverNormalizedFromLegacyMirrors() {
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen());
        stack.remove(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED);
        stack.set(TideTraitsComponents.MUTATION_SEED, 555L);

        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_INCOMPLETE,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());
        assertEquals(555L, stack.get(TideTraitsComponents.MUTATION_SEED));
    }

    @Test
    void legacyOnlyPayloadRequiresExplicitMigration() {
        ItemStack stack = new ItemStack(Items.COD);
        stack.set(TideTraitsComponents.MUTATION_SEED, 42L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 73.0);
        stack.set(TideTraitsComponents.MUTATION, "scarred");

        assertEquals(CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());
    }

    @Test
    void olderAndNewerSchemasAreDetectedExplicitly() {
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen());

        stack.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, SpecimenGenerator.SCHEMA_VERSION - 1);
        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_SCHEMA,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());

        stack.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, SpecimenGenerator.SCHEMA_VERSION + 1);
        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_NEWER_SCHEMA,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());
    }

    @Test
    void generationVersionsAreDetectedExplicitly() {
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen());

        stack.set(TideTraitsComponents.SPECIMEN_GENERATION_VERSION, SpecimenGenerator.GENERATION_VERSION - 1);
        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_OLDER_GENERATION,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());

        stack.set(TideTraitsComponents.SPECIMEN_GENERATION_VERSION, SpecimenGenerator.GENERATION_VERSION + 1);
        assertEquals(CanonicalSpecimenStorage.MigrationState.CANONICAL_NEWER_GENERATION,
                CanonicalSpecimenStorage.detectMigration(stack));
        assertTrue(CanonicalSpecimenStorage.read(stack).isEmpty());
    }

    @Test
    void transferSerializationRoundTripPreservesCanonicalPayloadWithoutLegacyFallback() {
        SpecimenData expected = specimen();
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, expected);

        SpecimenData decoded = CanonicalSpecimenStorage.readTransferData(transfer).orElseThrow();
        assertPersistedFields(expected, decoded);

        ItemStack restored = new ItemStack(Items.COD);
        assertTrue(CanonicalSpecimenStorage.restoreTransferData(transfer, restored));
        assertPersistedFields(expected, CanonicalSpecimenStorage.read(restored).orElseThrow());
    }

    @Test
    void transferReadRejectsMissingOrUnknownCanonicalDefinitionInsteadOfGuessing() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        canonical.putString("PercentileDefinition", "natural_percentile");
        transfer.put("CanonicalSpecimen", canonical);

        assertTrue(CanonicalSpecimenStorage.readTransferData(transfer).isEmpty());
        ItemStack target = new ItemStack(Items.COD);
        assertFalse(CanonicalSpecimenStorage.restoreTransferData(transfer, target));
        assertEquals(CanonicalSpecimenStorage.MigrationState.NONE,
                CanonicalSpecimenStorage.detectMigration(target));
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
    void specimenTransferUsesCanonicalPayloadWithoutMutatingStaleLegacyMirrors() {
        ItemStack source = new ItemStack(Items.COD);
        SpecimenData expected = specimen();
        CanonicalSpecimenStorage.write(source, expected);
        source.set(TideTraitsComponents.MUTATION_SEED, -17L);
        source.set(TideTraitsComponents.SIZE_PERCENTILE, 3.0);
        source.set(TideTraitsComponents.BODY_TYPE, "normal");

        NbtCompound transfer = SpecimenTransfer.fromStack(source);

        assertEquals(-17L, source.get(TideTraitsComponents.MUTATION_SEED));
        assertEquals(3.0, source.get(TideTraitsComponents.SIZE_PERCENTILE));
        assertEquals("normal", source.get(TideTraitsComponents.BODY_TYPE));
        assertPersistedFields(expected, CanonicalSpecimenStorage.readTransferData(transfer).orElseThrow());

        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);
        assertPersistedFields(expected, CanonicalSpecimenStorage.read(restored).orElseThrow());
    }

    @Test
    void invalidCanonicalTransferNeverFallsBackToLegacyMirrors() {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        canonical.remove("DeterministicSeed");
        transfer.put("CanonicalSpecimen", canonical);
        transfer.putLong(SpecimenTransfer.SEED_KEY, 12345L);
        transfer.putString(SpecimenTransfer.MUTATION_KEY, "scarred");

        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);

        assertEquals(CanonicalSpecimenStorage.MigrationState.NONE,
                CanonicalSpecimenStorage.detectMigration(restored));
        assertEquals(null, restored.get(TideTraitsComponents.MUTATION_SEED));
        assertEquals(null, restored.get(TideTraitsComponents.MUTATION));
    }

    @Test
    void absentPreFightScoreRoundTripsAsAbsentRatherThanBeingRecalculated() {
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
        ItemStack stack = new ItemStack(Items.COD);

        CanonicalSpecimenStorage.write(stack, expected);
        SpecimenData read = CanonicalSpecimenStorage.read(stack).orElseThrow();

        assertTrue(read.rawFishScore().isEmpty());
        assertTrue(read.fishScore().isEmpty());
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
