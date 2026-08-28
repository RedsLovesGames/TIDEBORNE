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

public final class CanonicalFishingGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalSpecimenSurvivesEntityRepresentationRoundTrip(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack original = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(original, specimen);

        CodEntity entity = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        SpecimenTransfer.stackToEntity(original, entity);
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(entity, restored);

        helper.assertTrue(specimen.speciesId().equals(restored.get(TideTraitsComponents.SPECIMEN_SPECIES_ID)), "Canonical species ID did not survive representation transfer");
        helper.assertTrue(Integer.valueOf(specimen.schemaVersion()).equals(restored.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION)), "Schema version did not survive representation transfer");
        helper.assertTrue(Integer.valueOf(specimen.generationVersion()).equals(restored.get(TideTraitsComponents.SPECIMEN_GENERATION_VERSION)), "Generation version did not survive representation transfer");
        helper.assertTrue(Double.valueOf(specimen.basePercentile()).equals(restored.get(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE)), "Base percentile did not survive representation transfer");
        helper.assertTrue(Double.valueOf(specimen.baseLength()).equals(restored.get(TideTraitsComponents.SPECIMEN_BASE_LENGTH)), "Base length did not survive representation transfer");
        helper.assertTrue(Double.valueOf(specimen.finalLength()).equals(restored.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH)), "Final length did not survive representation transfer");
        helper.assertTrue(Long.valueOf(specimen.deterministicSeed()).equals(restored.get(TideTraitsComponents.MUTATION_SEED)), "Compatibility seed did not survive representation transfer");
        helper.assertTrue(Double.valueOf(specimen.finalPercentile()).equals(restored.get(TideTraitsComponents.SIZE_PERCENTILE)), "Canonical final percentile did not survive representation transfer");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyCatchIndividualizerDoesNotRerollCanonicalSpecimen(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen);

        long seedBefore = stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE);
        double percentileBefore = stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
        CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(0x51A2B3C4L));

        helper.assertTrue(seedBefore == stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE), "Legacy individualizer rerolled canonical specimen seed");
        helper.assertTrue(Double.compare(percentileBefore, stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0)) == 0, "Legacy individualizer rerolled canonical percentile");
        helper.assertTrue(specimen.speciesId().equals(stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID)), "Legacy individualizer removed canonical specimen identity");
        helper.complete();
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:test_cod",
                2,
                1,
                0x123456789ABCDEFL,
                83.25,
                44.5,
                44.5,
                83.25,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
