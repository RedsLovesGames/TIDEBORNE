package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class CanonicalSpecimenStorageGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalItemStackRoundTripPreservesEveryField(TestContext helper) {
        SpecimenData expected = specimen();
        ItemStack source = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(source, expected);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(source) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Fresh canonical stack was not classified current");
        assertPersistedFields(helper, expected, CanonicalSpecimenStorage.read(source).orElse(null));
        NbtCompound transfer = SpecimenTransfer.fromStack(source);
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);
        assertPersistedFields(helper, expected, CanonicalSpecimenStorage.read(restored).orElse(null));
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalReadIgnoresAndDoesNotRepairLegacyMirrors(TestContext helper) {
        SpecimenData expected = specimen();
        ItemStack stack = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(stack, expected);
        stack.set(TideTraitsComponents.MUTATION_SEED, -17L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 3.0);
        stack.set(TideTraitsComponents.BODY_TYPE, "normal");
        stack.set(TideTraitsComponents.MUTATION, "normal");
        TideItemData.FISH_LENGTH.set(stack, 0.5);
        assertPersistedFields(helper, expected, CanonicalSpecimenStorage.read(stack).orElse(null));
        helper.assertTrue(Long.valueOf(-17L).equals(stack.get(TideTraitsComponents.MUTATION_SEED)), "Current canonical read repaired legacy seed mirror");
        helper.assertTrue(Double.valueOf(3.0).equals(stack.get(TideTraitsComponents.SIZE_PERCENTILE)), "Current canonical read repaired legacy percentile mirror");
        helper.assertTrue("normal".equals(stack.get(TideTraitsComponents.BODY_TYPE)), "Current canonical read repaired legacy Body Type mirror");
        helper.assertTrue("normal".equals(stack.get(TideTraitsComponents.MUTATION)), "Current canonical read repaired legacy Condition mirror");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void giantScarredLegacyFishMigratesOnce(TestContext helper) {
        ItemStack stack = registeredSizedFishStack();
        stack.set(TideTraitsComponents.MUTATION_SEED, 42L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 73.0);
        stack.set(TideTraitsComponents.BODY_TYPE, "giant");
        stack.set(TideTraitsComponents.MUTATION, "scarred");
        TideItemData.FISH_LENGTH.set(stack, 51.25);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY,
                "Legacy fish was not classified legacy-only");
        SpecimenData first = CanonicalSpecimenStorage.read(stack).orElse(null);
        helper.assertTrue(first != null, "Legacy Giant + Scarred fish did not migrate");
        if (first == null) { helper.complete(); return; }
        helper.assertTrue(first.bodyType() == SpecimenData.BodyType.GIANT, "Legacy Giant was not preserved");
        helper.assertTrue(first.condition() == SpecimenData.Condition.SCARRED, "Legacy Scarred was not preserved");
        helper.assertTrue(first.deterministicSeed() == 42L, "Legacy deterministic seed was not preserved");
        helper.assertTrue(first.basePercentile() == 73.0, "Legacy percentile was not preserved");
        helper.assertTrue(first.finalLength() == 51.25, "Legacy physical length was not preserved");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Migrated stack was not rewritten as current canonical data");
        stack.set(TideTraitsComponents.BODY_TYPE, "dwarf");
        stack.set(TideTraitsComponents.MUTATION, "albino");
        SpecimenData second = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(second.bodyType() == SpecimenData.BodyType.GIANT, "Current canonical stack was migrated a second time from legacy mirrors");
        helper.assertTrue(second.condition() == SpecimenData.Condition.SCARRED, "Current canonical condition changed on repeated read");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void dwarfAlbinoAndPerfectLegacyTraitsMigrate(TestContext helper) {
        ItemStack dwarfAlbino = registeredSizedFishStack();
        dwarfAlbino.set(TideTraitsComponents.MUTATION_SEED, 7L);
        dwarfAlbino.set(TideTraitsComponents.SIZE_PERCENTILE, 12.5);
        dwarfAlbino.set(TideTraitsComponents.BODY_TYPE, "dwarf");
        dwarfAlbino.set(TideTraitsComponents.MUTATION, "albino");
        SpecimenData albino = CanonicalSpecimenStorage.read(dwarfAlbino).orElseThrow();
        helper.assertTrue(albino.bodyType() == SpecimenData.BodyType.DWARF, "Legacy Dwarf was not preserved");
        helper.assertTrue(albino.pigmentation() == SpecimenData.Pigmentation.ALBINO, "Legacy Albino was not mapped to pigmentation");
        ItemStack perfect = registeredSizedFishStack();
        perfect.set(TideTraitsComponents.MUTATION_SEED, 9L);
        perfect.set(TideTraitsComponents.SIZE_PERCENTILE, 88.0);
        perfect.set(TideTraitsComponents.MUTATION, "perfect_specimen");
        SpecimenData quality = CanonicalSpecimenStorage.read(perfect).orElseThrow();
        helper.assertTrue(quality.specimenQuality() == SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                "Legacy Perfect Specimen was not mapped to Quality");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void olderSchemaPreservesUsableCanonicalValues(TestContext helper) {
        ItemStack stack = registeredSizedFishStack();
        stack.set(TideTraitsComponents.MUTATION_SEED, 11L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 21.0);
        stack.set(TideTraitsComponents.MUTATION, "scarred");
        SpecimenData baseline = CanonicalSpecimenStorage.read(stack).orElseThrow();
        long preservedSeed = 987654321L;
        double preservedPercentile = 64.0;
        double preservedLength = baseline.finalLength();
        stack.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, SpecimenGenerator.SCHEMA_VERSION - 1);
        stack.set(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED, preservedSeed);
        stack.set(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE, preservedPercentile);
        stack.set(TideTraitsComponents.SPECIMEN_FINAL_LENGTH, preservedLength);
        stack.set(TideTraitsComponents.SPECIMEN_CONDITION, "parasite_ridden");
        stack.set(TideTraitsComponents.MUTATION_SEED, -4L);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, 1.0);
        stack.set(TideTraitsComponents.MUTATION, "albino");
        SpecimenData migrated = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(migrated.deterministicSeed() == preservedSeed, "Older canonical seed was not preferred");
        helper.assertTrue(migrated.basePercentile() == preservedPercentile, "Older canonical base percentile was not preferred");
        helper.assertTrue(migrated.finalLength() == preservedLength, "Older canonical final length was not preserved");
        helper.assertTrue(migrated.condition() == SpecimenData.Condition.PARASITE_RIDDEN, "Usable older canonical orthogonal trait was not preserved");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void malformedAndNonFishLegacyStacksFailWithoutMutation(TestContext helper) {
        ItemStack malformed = registeredSizedFishStack();
        malformed.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, SpecimenGenerator.SCHEMA_VERSION - 1);
        malformed.set(TideTraitsComponents.SPECIMEN_SPECIES_ID, "example:not_this_fish");
        malformed.set(TideTraitsComponents.MUTATION_SEED, 123L);
        malformed.set(TideTraitsComponents.MUTATION, "scarred");
        helper.assertTrue(CanonicalSpecimenStorage.read(malformed).isEmpty(), "Malformed older-schema fish was migrated");
        helper.assertTrue(Integer.valueOf(SpecimenGenerator.SCHEMA_VERSION - 1).equals(malformed.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION)),
                "Malformed fish was modified after migration failure");
        ItemStack nonFish = new ItemStack(Items.STONE);
        nonFish.set(TideTraitsComponents.MUTATION_SEED, 55L);
        nonFish.set(TideTraitsComponents.SIZE_PERCENTILE, 50.0);
        nonFish.set(TideTraitsComponents.MUTATION, "albino");
        helper.assertTrue(CanonicalSpecimenStorage.read(nonFish).isEmpty(), "Non-fish item was migrated");
        helper.assertTrue(nonFish.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION) == null, "Non-fish item received canonical specimen data");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void incompleteCanonicalPayloadStillFailsClosed(TestContext helper) {
        ItemStack incomplete = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(incomplete, specimen());
        incomplete.remove(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED);
        incomplete.set(TideTraitsComponents.MUTATION_SEED, 555L);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(incomplete) == CanonicalSpecimenStorage.MigrationState.CANONICAL_INCOMPLETE,
                "Incomplete canonical stack was not classified explicitly");
        helper.assertTrue(CanonicalSpecimenStorage.read(incomplete).isEmpty(), "Incomplete current-schema stack was repaired from a legacy mirror");
        helper.assertTrue(Long.valueOf(555L).equals(incomplete.get(TideTraitsComponents.MUTATION_SEED)), "Failed canonical read modified the legacy seed mirror");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void invalidCanonicalTransferNeverFallsBackToLegacyKeys(TestContext helper) {
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen());
        NbtCompound canonical = transfer.getCompound("CanonicalSpecimen");
        canonical.remove("DeterministicSeed");
        transfer.put("CanonicalSpecimen", canonical);
        transfer.putLong(SpecimenTransfer.SEED_KEY, 12345L);
        transfer.putString(SpecimenTransfer.MUTATION_KEY, "scarred");
        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.toStack(transfer, restored);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(restored) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Invalid canonical transfer fell back into legacy migration state");
        helper.assertTrue(restored.get(TideTraitsComponents.MUTATION_SEED) == null, "Invalid canonical transfer restored legacy seed fallback");
        helper.assertTrue(restored.get(TideTraitsComponents.MUTATION) == null, "Invalid canonical transfer restored legacy mutation fallback");
        helper.complete();
    }

    private static ItemStack registeredSizedFishStack() {
        FishData data = TideData.FISH.get().values().stream()
                .filter(fish -> fish.size().isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tide sized-fish registry is empty during GameTest"));
        return new ItemStack((Item) data.fish().value());
    }

    private static SpecimenData specimen() {
        return new SpecimenData("tide:cod", SpecimenGenerator.SCHEMA_VERSION, SpecimenGenerator.GENERATION_VERSION,
                0x1234_5678_9ABCL, 97.25, 38.5, 47.75, 99.125, SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED, SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, true, OptionalDouble.of(812.375),
                OptionalInt.of(2711), SpecimenData.Provenance.generated());
    }

    private static void assertPersistedFields(TestContext helper, SpecimenData expected, SpecimenData actual) {
        helper.assertTrue(actual != null, "Canonical specimen could not be read");
        if (actual == null) return;
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
        helper.assertTrue(expected.specimenQuality() == actual.specimenQuality(), "Quality changed");
        helper.assertTrue(expected.perfectCatch() == actual.perfectCatch(), "Perfect Catch changed");
        helper.assertTrue(expected.rawFishScore().equals(actual.rawFishScore()), "Raw FishScore changed");
        helper.assertTrue(expected.fishScore().equals(actual.fishScore()), "FishScore changed");
    }
}
