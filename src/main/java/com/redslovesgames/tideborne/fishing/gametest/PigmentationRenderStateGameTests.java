package com.redslovesgames.tideborne.fishing.gametest;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.journal.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenEntity;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenTransfer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/** Server-safe assertions for the exact canonical state consumed by client pigmentation rendering. */
public final class PigmentationRenderStateGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void iridescentCanonicalStateSurvivesStackEntityAndRecordPreviewPaths(TestContext helper) {
        SpecimenData iridescent = specimen(SpecimenData.Pigmentation.IRIDESCENT, 0x11AA22BB33CCL);
        ItemStack source = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(source, iridescent);

        CodEntity worldFish = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        SpecimenTransfer.stackToEntity(source, worldFish);
        assertPigmentation(helper, worldFish, "iridescent", "world fish");

        NbtCompound record = CanonicalSpecimenRecordIndexer.project(iridescent);
        ItemStack previewStack = new ItemStack(Items.COD);
        helper.assertTrue(CanonicalSpecimenStorage.restoreTransferData(record, previewStack),
                "Team record projection did not restore canonical specimen data to preview stack");
        CodEntity previewFish = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(3, 2, 1));
        SpecimenTransfer.stackToEntity(previewStack, previewFish);
        assertPigmentation(helper, previewFish, "iridescent", "record preview fish");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void normalAndIridescentUseDistinctCanonicalRenderKeys(TestContext helper) {
        ItemStack normalStack = new ItemStack(Items.COD);
        ItemStack iridescentStack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(normalStack, specimen(SpecimenData.Pigmentation.NORMAL, 1L));
        CanonicalSpecimenStorage.write(iridescentStack, specimen(SpecimenData.Pigmentation.IRIDESCENT, 2L));

        CodEntity normal = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        CodEntity iridescent = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(3, 2, 1));
        SpecimenTransfer.stackToEntity(normalStack, normal);
        SpecimenTransfer.stackToEntity(iridescentStack, iridescent);

        assertPigmentation(helper, normal, "normal", "normal fish");
        assertPigmentation(helper, iridescent, "iridescent", "iridescent fish");
        helper.assertTrue(!renderKey(normal).equals(renderKey(iridescent)),
                "Normal and Iridescent specimens collapsed to the same canonical render key");
        helper.complete();
    }

    private static void assertPigmentation(TestContext helper, CodEntity entity, String expected, String context) {
        helper.assertTrue(entity instanceof SpecimenEntity, context + " does not expose specimen render state");
        helper.assertTrue(expected.equals(renderKey(entity)),
                context + " canonical pigmentation render key was not " + expected);
    }

    private static String renderKey(CodEntity entity) {
        NbtCompound tag = ((SpecimenEntity) entity).tideTraits$getSpecimenTag();
        return tag.getString(SpecimenTransfer.CANONICAL_PIGMENTATION_KEY);
    }

    private static SpecimenData specimen(SpecimenData.Pigmentation pigmentation, long seed) {
        return new SpecimenData(
                "minecraft:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                88.0,
                40.0,
                44.0,
                92.0,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                pigmentation,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.of(600.0),
                OptionalInt.of(1800),
                SpecimenData.Provenance.generated()
        );
    }
}
