package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

        helper.assertTrue("iridescent".equals(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Canonical storage did not persist Pigmentation");

        // Simulate stale legacy mutation state that used to make Pigmentation mutually exclusive
        // with Condition. Canonical V2 handling must repair only the Condition compatibility mirror.
        stack.set(TideTraitsComponents.MUTATION, "albino");
        long seedBefore = stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE);
        double percentileBefore = stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
        CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(0x6A17C0DEL));

        helper.assertTrue("iridescent".equals(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Legacy individualizer changed canonical Pigmentation");
        helper.assertTrue("parasite_ridden".equals(stack.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Legacy individualizer changed canonical Condition");
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
    public void canonicalPigmentationSurvivesEntityRepresentationRoundTrip(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack original = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(original, specimen);

        CodEntity entity = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        SpecimenTransfer.stackToEntity(original, entity);
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(entity, restored);

        helper.assertTrue("giant".equals(restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)),
                "Canonical Body Type did not survive representation transfer");
        helper.assertTrue("parasite_ridden".equals(restored.get(TideTraitsComponents.SPECIMEN_CONDITION)),
                "Canonical Condition did not survive representation transfer");
        helper.assertTrue("iridescent".equals(restored.get(TideTraitsComponents.SPECIMEN_PIGMENTATION)),
                "Canonical Pigmentation did not survive representation transfer");
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
