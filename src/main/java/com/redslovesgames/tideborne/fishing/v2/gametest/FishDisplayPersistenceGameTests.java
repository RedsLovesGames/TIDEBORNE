package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.item.TideItemData;
import com.li64.tide.registries.TideBlocks;
import com.li64.tide.registries.blocks.entities.FishDisplayBlockEntity;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class FishDisplayPersistenceGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalDisplayUsesCanonicalSizeAndPreservesSpecimen(TestContext helper) {
        SpecimenData expected = specimen();
        ItemStack fish = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(fish, expected);

        TideItemData.FISH_LENGTH.set(fish, expected.finalLength() + 100.0);

        FishDisplayBlockEntity display = new FishDisplayBlockEntity(
                BlockPos.ORIGIN, TideBlocks.FISH_DISPLAY.getDefaultState());
        helper.assertTrue(display.setDisplayStack(fish), "Tide rejected the canonical fish display stack");
        helper.assertTrue(Double.compare(expected.finalLength(), display.getFishLength()) == 0,
                "Fish display did not use canonical finalLength");

        SpecimenData displayed = CanonicalSpecimenStorage.read(display.getDisplayStack()).orElseThrow();
        assertCanonicalMetadata(helper, expected, displayed, "while displayed");
        helper.assertTrue(display.getDisplayData() != null,
                "Canonical display migration replaced Tide display model metadata");

        ItemStack removed = display.takeDisplayStack();
        SpecimenData restored = CanonicalSpecimenStorage.read(removed).orElseThrow();
        assertCanonicalMetadata(helper, expected, restored, "after display removal");
        helper.assertTrue(display.isEmpty(), "Display did not return to Tide's normal empty state");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyLengthOnlyDisplayMigratesOnceAndPreservesSavedSize(TestContext helper) {
        ItemStack legacyFish = new ItemStack(Items.COD);
        double legacyLength = 61.25;
        TideItemData.FISH_LENGTH.set(legacyFish, legacyLength);

        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(legacyFish)
                        == CanonicalSpecimenStorage.MigrationState.NONE,
                "Length-only legacy display fixture unexpectedly carried a canonical or trait marker");

        FishDisplayBlockEntity display = new FishDisplayBlockEntity(
                BlockPos.ORIGIN, TideBlocks.FISH_DISPLAY.getDefaultState());
        helper.assertTrue(display.setDisplayStack(legacyFish), "Tide rejected a legacy fish display stack");
        helper.assertTrue(Double.compare(legacyLength, display.getFishLength()) == 0,
                "Legacy display migration changed the saved Tide fish length");

        ItemStack stored = display.getDisplayStack();
        SpecimenData migrated = CanonicalSpecimenStorage.read(stored).orElseThrow();
        helper.assertTrue(migrated.schemaVersion() == SpecimenGenerator.SCHEMA_VERSION,
                "Legacy display was not rewritten to the current canonical schema");
        helper.assertTrue(Double.compare(legacyLength, migrated.finalLength()) == 0,
                "Legacy display migration did not preserve physical length");
        helper.assertTrue(migrated.rawFishScore().isPresent(),
                "Legacy display migration did not persist canonical raw FishScore");
        helper.assertTrue(migrated.fishScore().isPresent(),
                "Legacy display migration did not persist canonical FishScore");
        long seed = migrated.deterministicSeed();

        ItemStack removed = display.takeDisplayStack();
        SpecimenData restored = CanonicalSpecimenStorage.read(removed).orElseThrow();
        helper.assertTrue(restored.deterministicSeed() == seed,
                "Legacy display was migrated a second time with a different deterministic seed");
        helper.assertTrue(Double.compare(legacyLength, restored.finalLength()) == 0,
                "Legacy display removal changed migrated physical length");
        helper.assertTrue(migrated.equals(restored),
                "Legacy display removal changed migrated traits, size, or FishScore");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(removed)
                        == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Removed legacy display did not remain canonical");
        helper.complete();
    }

    private static void assertCanonicalMetadata(
            TestContext helper, SpecimenData expected, SpecimenData actual, String phase) {
        helper.assertTrue(expected.speciesId().equals(actual.speciesId()),
                "Canonical species changed " + phase);
        helper.assertTrue(Double.compare(expected.finalLength(), actual.finalLength()) == 0,
                "Canonical final length changed " + phase);
        helper.assertTrue(expected.bodyType() == actual.bodyType(),
                "Canonical Body Type changed " + phase);
        helper.assertTrue(expected.condition() == actual.condition(),
                "Canonical Condition changed " + phase);
        helper.assertTrue(expected.pigmentation() == actual.pigmentation(),
                "Canonical Pigmentation changed " + phase);
        helper.assertTrue(expected.specimenQuality() == actual.specimenQuality(),
                "Canonical Specimen Quality changed " + phase);
        helper.assertTrue(expected.rawFishScore().equals(actual.rawFishScore()),
                "Canonical raw FishScore changed " + phase);
        helper.assertTrue(expected.fishScore().equals(actual.fishScore()),
                "Canonical FishScore changed " + phase);
        helper.assertTrue(expected.deterministicSeed() == actual.deterministicSeed(),
                "Canonical deterministic seed changed " + phase);
        helper.assertTrue(Double.compare(expected.basePercentile(), actual.basePercentile()) == 0,
                "Canonical natural percentile changed " + phase);
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "minecraft:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x26D15A17E5L,
                96.75,
                42.0,
                52.5,
                99.1,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(842.75),
                OptionalInt.of(2476),
                SpecimenData.Provenance.generated()
        );
    }
}
