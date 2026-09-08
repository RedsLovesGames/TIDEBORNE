package com.redslovesgames.tideborne.fishing.gametest;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.specimen.CatchTraitService;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenTransfer;
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
import net.minecraft.util.math.random.Random;

public final class PigmentationGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalPigmentationSurvivesLegacyIndividualizerWithoutReroll(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen);

        helper.assertTrue("giant".equals(stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)),
                "Canonical storage did not persist Body Type");
        helper.assertTrue("parasite_ridden".equals(stack.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Canonical storage did not persist Condition");
        helper.assertTrue("iridescent".equals(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Canonical storage did not persist Pigmentation");

        // Simulate stale legacy mutation state that used to make Pigmentation mutually exclusive
        // with Condition. Canonical V2 handling must repair only compatibility mirrors.
        stack.set(TideTraitsComponents.BODY_TYPE, "dwarf");
        stack.set(TideTraitsComponents.MUTATION, "albino");
        long seedBefore = stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE);
        double percentileBefore = stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
        CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(0x6A17C0DEL));

        helper.assertTrue("giant".equals(stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)),
                "Legacy individualizer changed canonical Body Type");
        helper.assertTrue("iridescent".equals(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Legacy individualizer changed canonical Pigmentation");
        helper.assertTrue("parasite_ridden".equals(stack.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Legacy individualizer changed canonical Condition");
        helper.assertTrue("giant".equals(stack.get(TideTraitsComponents.BODY_TYPE)),
                "Legacy individualizer did not repair the Body Type compatibility mirror");
        helper.assertTrue("parasite_ridden".equals(stack.get(TideTraitsComponents.MUTATION)),
                "Legacy individualizer did not repair the Condition compatibility mirror");
        helper.assertTrue(seedBefore == stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE),
                "Legacy individualizer rerolled canonical specimen seed");
        helper.assertTrue(Double.compare(percentileBefore,
                        stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0)) == 0,
                "Legacy individualizer rerolled canonical percentile");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void stackedCanonicalAxesSurviveEntityBucketRepresentationRoundTrip(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack original = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(original, specimen);

        CodEntity first = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        SpecimenTransfer.stackToEntity(original, first);
        ItemStack bucket = new ItemStack(Items.COD_BUCKET);
        SpecimenTransfer.entityToBucket(first, bucket);
        NbtComponent bucketData = bucket.get(DataComponentTypes.BUCKET_ENTITY_DATA);
        helper.assertTrue(bucketData != null, "Canonical specimen bucket entity data was not written");

        CodEntity second = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(2, 2, 1));
        SpecimenTransfer.bucketTagToEntity(bucketData.copyNbt(), second);
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(second, restored);

        helper.assertTrue("giant".equals(restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)),
                "Canonical Body Type did not survive item/entity/bucket/entity/item transfer");
        helper.assertTrue("parasite_ridden".equals(restored.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Canonical Condition did not survive item/entity/bucket/entity/item transfer");
        helper.assertTrue("iridescent".equals(restored.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Canonical Pigmentation did not survive item/entity/bucket/entity/item transfer");
        helper.assertTrue("giant".equals(restored.get(TideTraitsComponents.BODY_TYPE)),
                "Body Type compatibility mirror did not survive representation transfer");
        helper.assertTrue("parasite_ridden".equals(restored.get(TideTraitsComponents.MUTATION)),
                "Condition compatibility mirror did not survive representation transfer");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void stackedCanonicalAxesSurviveExplicitTransferNbtWithoutSnapshot(TestContext helper) {
        ItemStack original = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(original, specimen());

        NbtCompound transfer = SpecimenTransfer.fromStack(original);
        helper.assertTrue("giant".equals(transfer.getString(SpecimenTransfer.CANONICAL_BODY_TYPE_KEY)),
                "Transfer NBT did not persist canonical Body Type");
        helper.assertTrue("parasite_ridden".equals(transfer.getString(SpecimenTransfer.CANONICAL_CONDITION_KEY)),
                "Transfer NBT did not persist canonical Condition");
        helper.assertTrue("iridescent".equals(transfer.getString(SpecimenTransfer.CANONICAL_PIGMENTATION_KEY)),
                "Transfer NBT did not persist canonical Pigmentation");

        // This overload intentionally has no registry-backed SourceStack snapshot. The three
        // canonical axes must still round-trip through their explicit representation fields.
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);
        helper.assertTrue("giant".equals(restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)),
                "Explicit transfer NBT changed canonical Body Type");
        helper.assertTrue("parasite_ridden".equals(restored.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Explicit transfer NBT changed canonical Condition");
        helper.assertTrue("iridescent".equals(restored.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Explicit transfer NBT changed canonical Pigmentation");
        helper.assertTrue("giant".equals(restored.get(TideTraitsComponents.BODY_TYPE)),
                "Explicit transfer NBT did not restore Body Type compatibility mirror");
        helper.assertTrue("parasite_ridden".equals(restored.get(TideTraitsComponents.MUTATION)),
                "Explicit transfer NBT did not restore Condition compatibility mirror");
        helper.complete();
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:test_cod",
                2,
                1,
                29_894L,
                40.62626585448833,
                25.0,
                30.0,
                55.0,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
