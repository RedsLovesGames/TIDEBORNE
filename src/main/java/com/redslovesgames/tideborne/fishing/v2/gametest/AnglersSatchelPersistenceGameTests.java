package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.satchel.AnglersSatchelStorage;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

/**
 * Persistence contract between Fishing System 2.0 and the Angler's Satchel.
 *
 * <p>The Satchel deliberately treats each fish as an opaque ItemStack. Canonical specimen state is
 * therefore carried by {@link CanonicalSpecimenStorage}'s existing stack components rather than by
 * a second Satchel-specific specimen serializer.</p>
 */
public final class AnglersSatchelPersistenceGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalSpecimenSurvivesInsertReadExtractRoundTrip(TestContext helper) {
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        SpecimenData expected = specimen();
        ItemStack fish = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(fish, expected);
        fish.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Canonical Satchel Cod"));
        NbtCompound addonData = new NbtCompound();
        addonData.putString("ExampleAddonVariant", "silver_spots");
        fish.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(addonData));

        AnglersSatchelStorage.InsertionResult inserted = AnglersSatchelStorage.insert(satchel, fish);
        helper.assertTrue(inserted.fullyInserted(), "Canonical specimen was not inserted into the Angler's Satchel");
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 1, "Canonical specimen insert changed Satchel entry count");

        ItemStack stored = AnglersSatchelStorage.contents(satchel).getFirst();
        assertCanonicalMetadata(helper, expected, stored, "while stored");
        assertOpaqueStackMetadata(helper, stored, "while stored");

        AnglersSatchelStorage.ExtractionResult extraction = AnglersSatchelStorage.extractAt(satchel, 0, true);
        helper.assertTrue(extraction.status() == AnglersSatchelStorage.ExtractionStatus.SUCCESS,
                "Canonical specimen extraction failed");
        ItemStack extracted = extraction.item().orElseThrow();
        assertCanonicalMetadata(helper, expected, extracted, "after extraction");
        assertOpaqueStackMetadata(helper, extracted, "after extraction");
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 0, "Extracted canonical specimen remained duplicated in the Satchel");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacySatchelContentsRemainReadableWithoutCanonicalRegeneration(TestContext helper) {
        ItemStack legacyFish = new ItemStack(Items.COD);
        legacyFish.set(TideTraitsComponents.MUTATION, "scarred");
        legacyFish.set(TideTraitsComponents.MUTATION_SEED, 0x1234ABCD5678EF90L);
        legacyFish.set(TideTraitsComponents.SIZE_PERCENTILE, 73.25);
        TideItemData.FISH_LENGTH.set(legacyFish, 47.75);
        legacyFish.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Legacy Satchel Cod"));

        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(List.of(legacyFish)));

        helper.assertTrue(CanonicalSpecimenStorage.read(legacyFish).isEmpty(),
                "Legacy test fixture unexpectedly started canonical");
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 1, "Existing Tide SatchelContents were not readable");
        ItemStack stored = AnglersSatchelStorage.contents(satchel).getFirst();
        assertLegacyMetadata(helper, stored, "while reading existing Satchel data");
        helper.assertTrue(CanonicalSpecimenStorage.read(stored).isEmpty(),
                "Reading old Satchel data silently generated canonical specimen state");

        AnglersSatchelStorage.ExtractionResult extraction = AnglersSatchelStorage.extractAt(satchel, 0, true);
        helper.assertTrue(extraction.status() == AnglersSatchelStorage.ExtractionStatus.SUCCESS,
                "Legacy Satchel specimen extraction failed");
        ItemStack extracted = extraction.item().orElseThrow();
        assertLegacyMetadata(helper, extracted, "after extraction");
        helper.assertTrue(CanonicalSpecimenStorage.read(extracted).isEmpty(),
                "Extracting old Satchel data silently generated canonical specimen state");
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 0,
                "Extracted legacy specimen remained duplicated in the Satchel");
        helper.complete();
    }

    private static void assertCanonicalMetadata(TestContext helper, SpecimenData expected, ItemStack stack, String phase) {
        SpecimenData actual = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(expected.speciesId().equals(actual.speciesId()), "Canonical species changed " + phase);
        helper.assertTrue(expected.schemaVersion() == actual.schemaVersion(), "Canonical schema version changed " + phase);
        helper.assertTrue(expected.generationVersion() == actual.generationVersion(), "Canonical generation version changed " + phase);
        helper.assertTrue(expected.deterministicSeed() == actual.deterministicSeed(), "Canonical deterministic seed changed " + phase);
        helper.assertTrue(Double.compare(expected.basePercentile(), actual.basePercentile()) == 0,
                "Canonical natural percentile changed " + phase);
        helper.assertTrue(Double.compare(expected.baseLength(), actual.baseLength()) == 0, "Canonical base length changed " + phase);
        helper.assertTrue(Double.compare(expected.finalLength(), actual.finalLength()) == 0, "Canonical final length changed " + phase);
        helper.assertTrue(Double.compare(expected.finalPercentile(), actual.finalPercentile()) == 0,
                "Canonical final percentile changed " + phase);
        helper.assertTrue(expected.bodyType() == actual.bodyType(), "Canonical Body Type changed " + phase);
        helper.assertTrue(expected.condition() == actual.condition(), "Canonical Condition changed " + phase);
        helper.assertTrue(expected.pigmentation() == actual.pigmentation(), "Canonical Pigmentation changed " + phase);
        helper.assertTrue(expected.specimenQuality() == actual.specimenQuality(), "Canonical Specimen Quality changed " + phase);
        helper.assertTrue(expected.perfectCatch() == actual.perfectCatch(), "Canonical Perfect Catch flag changed " + phase);
        helper.assertTrue(expected.rawFishScore().equals(actual.rawFishScore()), "Canonical raw FishScore changed " + phase);
        helper.assertTrue(expected.fishScore().equals(actual.fishScore()), "Canonical FishScore changed " + phase);
    }

    private static void assertOpaqueStackMetadata(TestContext helper, ItemStack stack, String phase) {
        helper.assertTrue("Canonical Satchel Cod".equals(stack.getName().getString()), "Custom fish name changed " + phase);
        NbtComponent addonData = stack.get(DataComponentTypes.CUSTOM_DATA);
        helper.assertTrue(addonData != null && "silver_spots".equals(addonData.copyNbt().getString("ExampleAddonVariant")),
                "Unrelated custom stack data changed " + phase);
    }

    private static void assertLegacyMetadata(TestContext helper, ItemStack stack, String phase) {
        helper.assertTrue("scarred".equals(stack.get(TideTraitsComponents.MUTATION)), "Legacy mutation changed " + phase);
        helper.assertTrue(Long.valueOf(0x1234ABCD5678EF90L).equals(stack.get(TideTraitsComponents.MUTATION_SEED)),
                "Legacy mutation seed changed " + phase);
        helper.assertTrue(Double.valueOf(73.25).equals(stack.get(TideTraitsComponents.SIZE_PERCENTILE)),
                "Legacy percentile changed " + phase);
        helper.assertTrue(Double.compare(47.75, TideItemData.FISH_LENGTH.getOrDefault(stack, -1.0)) == 0,
                "Legacy Tide fish length changed " + phase);
        helper.assertTrue("Legacy Satchel Cod".equals(stack.getName().getString()), "Legacy custom name changed " + phase);
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "minecraft:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x27A761E25A7CL,
                88.5,
                39.25,
                49.75,
                94.2,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(731.125),
                OptionalInt.of(2264),
                SpecimenData.Provenance.generated()
        );
    }
}
