package com.redslovesgames.tidetraits.gametest;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.catching.PerfectCatchTraitBoost;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.random.Random;

/** Runtime regression coverage for the Stage 50 legacy size/percentile removal. */
public final class LegacySizeGenerationGameTests implements FabricGameTest {
   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void canonicalSpecimenIgnoresEveryLegacySizeHook(TestContext helper) {
      ItemStack stack = new ItemStack(Items.COD);
      SpecimenData original = new SpecimenData(
         "minecraft:cod",
         SpecimenGenerator.SCHEMA_VERSION,
         SpecimenGenerator.GENERATION_VERSION,
         0x123456789ABCDEFL,
         99.25,
         42.0,
         27.0,
         18.5,
         SpecimenData.BodyType.DWARF,
         SpecimenData.Condition.SCARRED,
         SpecimenData.Pigmentation.IRIDESCENT,
         SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
         true,
         OptionalDouble.empty(),
         OptionalInt.empty(),
         SpecimenData.Provenance.generated()
      );
      CanonicalSpecimenStorage.write(stack, original);

      boolean assigned = CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(987654321L));
      PerfectCatchTraitBoost.apply(List.of(stack));

      SpecimenData after = CanonicalSpecimenStorage.read(stack).orElseThrow();
      helper.assertTrue(!assigned, "Legacy catch individualizer claimed a canonical V2 specimen");
      helper.assertTrue(after.equals(original), "A legacy hook changed canonical specimen identity, percentile, size, or traits");
      helper.assertTrue("dwarf".equals(stack.get(TideTraitsComponents.BODY_TYPE)),
         "High-percentile canonical Dwarf was rewritten by a legacy Giant gate");
      helper.assertTrue(Double.compare((Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE), 18.5) == 0,
         "Legacy compatibility percentile mirror changed");
      helper.assertTrue(Math.abs((Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0) - 27.0) < 1.0E-9,
         "Legacy hook changed the one canonical final physical size");
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void noncanonicalNewCatchNoLongerGeneratesLegacySizeOrPercentile(TestContext helper) {
      ItemStack stack = new ItemStack(Items.COD);
      double initialLength = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);

      boolean assigned = CatchTraitService.INSTANCE.assignIfAbsent(stack, Random.create(1592639710L));
      helper.assertTrue(assigned, "Legacy identity compatibility marker was not assigned");
      helper.assertTrue(stack.get(TideTraitsComponents.MUTATION_SEED) != null,
         "Legacy compatibility identity seed was not retained");
      helper.assertTrue(stack.get(TideTraitsComponents.MUTATION) != null,
         "Legacy compatibility Condition marker was not retained");
      helper.assertTrue(stack.get(TideTraitsComponents.SIZE_PERCENTILE) == null,
         "Superseded legacy percentile generation still ran for a new catch");
      helper.assertTrue(Double.compare((Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0), initialLength) == 0,
         "Superseded second fish-length roll still ran for a new catch");

      PerfectCatchTraitBoost.apply(List.of(stack));
      helper.assertTrue(stack.get(TideTraitsComponents.SIZE_PERCENTILE) == null,
         "Legacy Perfect Catch recreated a removed percentile");
      helper.assertTrue(Double.compare((Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0), initialLength) == 0,
         "Legacy Perfect Catch rewrote fish length");
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void oldWorldLegacySpecimenStillMigratesToCanonical(TestContext helper) {
      ItemStack stack = new ItemStack(Items.COD);
      stack.set(TideTraitsComponents.MUTATION, "scarred");
      stack.set(TideTraitsComponents.MUTATION_SEED, 81985529216486895L);
      stack.set(TideTraitsComponents.SIZE_PERCENTILE, 91.4);
      TideItemData.FISH_LENGTH.set(stack, 171.2);

      SpecimenData migrated = CanonicalSpecimenStorage.read(stack).orElseThrow();
      helper.assertTrue(migrated.condition() == SpecimenData.Condition.SCARRED,
         "Legacy Scarred state did not migrate to canonical Condition");
      helper.assertTrue(migrated.deterministicSeed() == 81985529216486895L,
         "Legacy identity seed was not preserved by migration");
      helper.assertTrue(Double.compare(migrated.basePercentile(), 91.4) == 0,
         "Legacy persisted percentile was not preserved during migration");
      helper.assertTrue(Math.abs(migrated.finalLength() - 171.2) < 1.0E-9,
         "Legacy persisted physical length was not preserved during migration");
      helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
         "Migrated old-world specimen was not rewritten as current canonical data");
      helper.complete();
   }
}
