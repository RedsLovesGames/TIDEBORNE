package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.FishLengthHolder;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class CanonicalEntityTransferGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalStackEntityStackPreservesEveryFieldAcrossTraitCombinations(TestContext helper) {
        List<SpecimenData> fixtures = fixtures();
        for (int index = 0; index < fixtures.size(); index++) {
            SpecimenData expected = fixtures.get(index);
            ItemStack source = new ItemStack(Items.COD);
            CanonicalSpecimenStorage.write(source, expected);

            CodEntity fish = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1 + index, 2, 1));
            EntityType<?> originalType = fish.getType();
            SpecimenTransfer.stackToEntity(source, fish);

            helper.assertTrue(fish.getType() == originalType && fish.getType() == EntityType.COD,
                    "Canonical transfer changed the living fish entity type");
            helper.assertTrue(fish instanceof FishLengthHolder,
                    "Tide fish length behavior was not present on the living fish entity");
            FishLengthHolder holder = (FishLengthHolder) fish;
            helper.assertTrue(holder.tide$getLength() == expected.finalLength(),
                    "Canonical final length was not applied to Tide's living entity length holder");

            holder.tide$setLength(expected.finalLength() + 137.0 + index);
            ItemStack restored = new ItemStack(Items.COD);
            SpecimenTransfer.entityToStack(fish, restored);

            assertEveryCanonicalField(helper, expected, CanonicalSpecimenStorage.read(restored).orElse(null));
            helper.assertTrue(fish.getType() == originalType,
                    "Exporting the specimen changed the living fish entity type");
        }
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalStackEntityBucketEntityStackPreservesEveryFieldAcrossTraitCombinations(TestContext helper) {
        List<SpecimenData> fixtures = fixtures();
        for (int index = 0; index < fixtures.size(); index++) {
            SpecimenData expected = fixtures.get(index);
            ItemStack source = new ItemStack(Items.COD);
            CanonicalSpecimenStorage.write(source, expected);

            CodEntity first = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1 + index, 2, 1));
            SpecimenTransfer.stackToEntity(source, first);
            ((FishLengthHolder) first).tide$setLength(expected.finalLength() + 211.0 + index);

            ItemStack bucket = new ItemStack(Items.COD_BUCKET);
            // Exercise the real FishEntity#copyDataToStack capture hook used by bucket pickup.
            first.copyDataToStack(bucket);
            helper.assertTrue(bucket.isOf(Items.COD_BUCKET),
                    "Specimen persistence changed the vanilla bucket item identity");
            NbtComponent bucketData = bucket.get(DataComponentTypes.BUCKET_ENTITY_DATA);
            helper.assertTrue(bucketData != null, "Canonical specimen bucket entity data was not written");
            if (bucketData == null) {
                continue;
            }

            CodEntity second = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1 + index, 2, 3));
            EntityType<?> secondType = second.getType();
            NbtCompound bucketTag = bucketData.copyNbt();
            // This is the same release adapter invoked by MobBucketItemMixin after vanilla restores its own data.
            SpecimenTransfer.bucketTagToEntity(bucketTag, second);

            helper.assertTrue(second.getType() == secondType && second.getType() == EntityType.COD,
                    "Bucket restoration changed the living fish species/entity type");
            helper.assertTrue(((FishLengthHolder) second).tide$getLength() == expected.finalLength(),
                    "Bucket restoration did not reapply the canonical final length to Tide behavior");

            ItemStack restored = new ItemStack(Items.COD);
            SpecimenTransfer.entityToStack(second, restored);
            assertEveryCanonicalField(helper, expected, CanonicalSpecimenStorage.read(restored).orElse(null));
        }
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void bucketRoundTripWithoutSpecimenDoesNotGenerateCanonicalState(TestContext helper) {
        CodEntity first = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        ItemStack bucket = new ItemStack(Items.COD_BUCKET);

        // The capture mixin is allowed to observe vanilla bucket pickup, but a fish with no specimen
        // must remain specimen-free. In particular, transfer must never become a generation trigger.
        first.copyDataToStack(bucket);
        NbtComponent bucketData = bucket.get(DataComponentTypes.BUCKET_ENTITY_DATA);
        NbtCompound bucketTag = bucketData == null ? new NbtCompound() : bucketData.copyNbt();
        helper.assertTrue(!bucketTag.contains(SpecimenTransfer.ENTITY_KEY),
                "Bucket capture generated specimen state for an uninitialized fish");

        CodEntity second = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 3));
        SpecimenTransfer.bucketTagToEntity(bucketTag, second);
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(second, restored);

        helper.assertTrue(CanonicalSpecimenStorage.read(restored).isEmpty(),
                "Bucket release generated canonical specimen state");
        helper.assertTrue(!SpecimenTransfer.hasSpecimen(restored),
                "Bucket release generated a legacy specimen compatibility identity");
        helper.assertTrue(bucket.isOf(Items.COD_BUCKET),
                "No-specimen transfer changed normal bucket behavior");
        helper.complete();
    }

    private static List<SpecimenData> fixtures() {
        return List.of(
                specimen(0x1234_5678_9ABCL, 97.25, 38.5, 47.75, 99.125,
                        SpecimenData.BodyType.GIANT, SpecimenData.Condition.SCARRED,
                        SpecimenData.Pigmentation.IRIDESCENT, SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                        true, 812.375, 2711),
                specimen(-0x1020_3040_5060_708L, 4.75, 31.25, 21.0, 1.625,
                        SpecimenData.BodyType.DWARF, SpecimenData.Condition.PARASITE_RIDDEN,
                        SpecimenData.Pigmentation.ALBINO, SpecimenData.SpecimenQuality.NORMAL,
                        false, 146.5, 487),
                specimen(0x55AA_1357_2468_ACEFL, 50.0, 34.0, 34.0, 50.0,
                        SpecimenData.BodyType.NORMAL, SpecimenData.Condition.NORMAL,
                        SpecimenData.Pigmentation.NORMAL, SpecimenData.SpecimenQuality.NORMAL,
                        true, 401.0, 1500)
        );
    }

    private static SpecimenData specimen(
            long seed,
            double basePercentile,
            double baseLength,
            double finalLength,
            double finalPercentile,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition,
            SpecimenData.Pigmentation pigmentation,
            SpecimenData.SpecimenQuality quality,
            boolean perfectCatch,
            double rawFishScore,
            int fishScore
    ) {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                basePercentile,
                baseLength,
                finalLength,
                finalPercentile,
                bodyType,
                condition,
                pigmentation,
                quality,
                perfectCatch,
                OptionalDouble.of(rawFishScore),
                OptionalInt.of(fishScore),
                SpecimenData.Provenance.generated()
        );
    }

    private static void assertEveryCanonicalField(TestContext helper, SpecimenData expected, SpecimenData actual) {
        helper.assertTrue(actual != null, "Canonical specimen could not be read after entity transfer");
        if (actual == null) {
            return;
        }
        helper.assertTrue(expected.speciesId().equals(actual.speciesId()), "Species ID changed");
        helper.assertTrue(expected.schemaVersion() == actual.schemaVersion(), "Schema version changed");
        helper.assertTrue(expected.generationVersion() == actual.generationVersion(), "Generation version changed");
        helper.assertTrue(expected.deterministicSeed() == actual.deterministicSeed(), "Deterministic seed changed");
        helper.assertTrue(expected.basePercentile() == actual.basePercentile(), "Base percentile changed");
        helper.assertTrue(expected.baseLength() == actual.baseLength(), "Base length changed");
        helper.assertTrue(expected.finalLength() == actual.finalLength(), "Final length changed");
        helper.assertTrue(expected.finalPercentile() == actual.finalPercentile(), "Final percentile changed");
        helper.assertTrue(expected.bodyType() == actual.bodyType(), "Body Type changed");
        helper.assertTrue(expected.condition() == actual.condition(), "Condition changed");
        helper.assertTrue(expected.pigmentation() == actual.pigmentation(), "Pigmentation changed");
        helper.assertTrue(expected.specimenQuality() == actual.specimenQuality(), "Specimen Quality changed");
        helper.assertTrue(expected.perfectCatch() == actual.perfectCatch(), "Perfect Catch changed");
        helper.assertTrue(expected.rawFishScore().equals(actual.rawFishScore()), "Raw FishScore changed");
        helper.assertTrue(expected.fishScore().equals(actual.fishScore()), "FishScore changed");
        helper.assertTrue(expected.provenance().equals(actual.provenance()), "Provenance changed");
    }
}
