package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.catching.PerfectCatchTraitBoost;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.List;
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
        helper.assertTrue("dwarf".equals(restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)), "Canonical Body Type did not survive representation transfer");
        helper.assertTrue("scarred".equals(restored.get(TideTraitsComponents.SPECIMEN_CONDITION)), "Canonical Condition did not survive representation transfer");
        helper.assertTrue("dwarf".equals(restored.get(TideTraitsComponents.BODY_TYPE)), "Legacy Body Type mirror did not survive representation transfer");
        helper.assertTrue("scarred".equals(restored.get(TideTraitsComponents.MUTATION)), "Legacy Condition mirror did not survive representation transfer");
        helper.assertTrue(Long.valueOf(specimen.deterministicSeed()).equals(restored.get(TideTraitsComponents.MUTATION_SEED)), "Compatibility seed did not survive representation transfer");
        helper.assertTrue(Double.valueOf(specimen.finalPercentile()).equals(restored.get(TideTraitsComponents.SIZE_PERCENTILE)), "Canonical final percentile did not survive representation transfer");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalBodyTypeSurvivesTransferNbtWithoutReroll(TestContext helper) {
        ItemStack original = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(original, specimen());

        NbtCompound transfer = SpecimenTransfer.fromStack(original);
        helper.assertTrue("dwarf".equals(transfer.getString(SpecimenTransfer.CANONICAL_BODY_TYPE_KEY)), "Transfer NBT did not persist canonical Body Type");
        helper.assertTrue("scarred".equals(transfer.getString(SpecimenTransfer.MUTATION_KEY)), "Transfer NBT did not preserve the Condition compatibility mirror");

        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);
        helper.assertTrue("dwarf".equals(restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)), "Transfer NBT changed canonical Body Type");
        helper.assertTrue("dwarf".equals(restored.get(TideTraitsComponents.BODY_TYPE)), "Transfer NBT did not mirror canonical Body Type for compatibility");
        helper.assertTrue("scarred".equals(restored.get(TideTraitsComponents.MUTATION)), "Transfer NBT changed the Condition compatibility mirror");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyCatchIndividualizerDoesNotRerollCanonicalSpecimen(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen);

        // Simulate stale compatibility state. The old P97 Giant gate and legacy mutation selector
        // must not override canonical Body Type or Condition.
        stack.set(TideTraitsComponents.BODY_TYPE, "giant");
        stack.remove(TideTraitsComponents.MUTATION);
        long seedBefore = stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE);
        double percentileBefore = stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
        CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(0x51A2B3C4L));

        helper.assertTrue(seedBefore == stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, Long.MIN_VALUE), "Legacy individualizer rerolled canonical specimen seed");
        helper.assertTrue(Double.compare(percentileBefore, stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0)) == 0, "Legacy individualizer rerolled canonical percentile");
        helper.assertTrue(specimen.speciesId().equals(stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID)), "Legacy individualizer removed canonical specimen identity");
        helper.assertTrue("dwarf".equals(stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)), "Legacy individualizer changed canonical Body Type");
        helper.assertTrue("scarred".equals(stack.get(TideTraitsComponents.SPECIMEN_CONDITION)), "Legacy individualizer changed canonical Condition");
        helper.assertTrue("scarred".equals(stack.get(TideTraitsComponents.MUTATION)), "Legacy individualizer did not repair Condition mirror from canonical state");
        helper.assertTrue("dwarf".equals(TraitAxesRuntime.bodyType(stack)), "Legacy P97 Body Type gate overrode canonical Body Type");
        helper.assertTrue("dwarf".equals(stack.get(TideTraitsComponents.BODY_TYPE)), "Legacy Body Type mirror was not repaired from canonical state");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyPerfectCatchBoostDoesNotRewriteCanonicalSize(TestContext helper) {
        SpecimenData specimen = specimen();
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, specimen);

        double percentileBefore = stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
        double lengthBefore = TideItemData.FISH_LENGTH.getOrDefault(stack, -1.0);
        PerfectCatchTraitBoost.apply(List.of(stack));

        helper.assertTrue(Double.compare(percentileBefore, stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0)) == 0, "Legacy Perfect Catch boost rewrote canonical percentile");
        helper.assertTrue(Double.compare(lengthBefore, TideItemData.FISH_LENGTH.getOrDefault(stack, -1.0)) == 0, "Legacy Perfect Catch boost rewrote canonical length");
        helper.assertTrue(Double.valueOf(specimen.finalLength()).equals(stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH)), "Legacy Perfect Catch boost changed canonical final length identity");
        helper.assertTrue("dwarf".equals(stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE)), "Legacy Perfect Catch boost changed canonical Body Type");
        helper.assertTrue("scarred".equals(stack.get(TideTraitsComponents.SPECIMEN_CONDITION)), "Legacy Perfect Catch boost changed canonical Condition");
        helper.complete();
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:test_cod",
                2,
                1,
                0x123456789ABCDEFL,
                99.25,
                44.5,
                33.0,
                99.25,
                SpecimenData.BodyType.DWARF,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
