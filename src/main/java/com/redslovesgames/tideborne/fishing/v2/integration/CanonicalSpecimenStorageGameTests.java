package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
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

        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(source)
                == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
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
        helper.assertTrue(Long.valueOf(-17L).equals(stack.get(TideTraitsComponents.MUTATION_SEED)),
                "Canonical read repaired legacy seed mirror");
        helper.assertTrue(Double.valueOf(3.0).equals(stack.get(TideTraitsComponents.SIZE_PERCENTILE)),
                "Canonical read repaired legacy percentile mirror");
        helper.assertTrue("normal".equals(stack.get(TideTraitsComponents.BODY_TYPE)),
                "Canonical read repaired legacy Body Type mirror");
        helper.assertTrue("normal".equals(stack.get(TideTraitsComponents.MUTATION)),
                "Canonical read repaired legacy Condition mirror");
        helper.assertTrue(Math.abs((Double) TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0) - 0.5) < 1.0E-9,
                "Canonical read repaired legacy length mirror");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void migrationDetectionIsExplicitAndDoesNotRepairPayloads(TestContext helper) {
        ItemStack legacy = new ItemStack(Items.COD);
        legacy.set(TideTraitsComponents.MUTATION_SEED, 42L);
        legacy.set(TideTraitsComponents.SIZE_PERCENTILE, 73.0);
        legacy.set(TideTraitsComponents.MUTATION, "scarred");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(legacy)
                == CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY,
                "Legacy stack was not classified legacy-only");
        helper.assertTrue(CanonicalSpecimenStorage.read(legacy).isEmpty(),
                "Legacy stack was silently normalized into a canonical specimen");

        ItemStack incomplete = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(incomplete, specimen());
        incomplete.remove(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED);
        incomplete.set(TideTraitsComponents.MUTATION_SEED, 555L);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(incomplete)
                == CanonicalSpecimenStorage.MigrationState.CANONICAL_INCOMPLETE,
                "Incomplete canonical stack was not classified explicitly");
        helper.assertTrue(CanonicalSpecimenStorage.read(incomplete).isEmpty(),
                "Incomplete canonical stack was repaired from a legacy mirror");
        helper.assertTrue(Long.valueOf(555L).equals(incomplete.get(TideTraitsComponents.MUTATION_SEED)),
                "Migration detection modified the legacy seed mirror");
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
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(restored)
                == CanonicalSpecimenStorage.MigrationState.NONE,
                "Invalid canonical transfer fell back into legacy migration state");
        helper.assertTrue(restored.get(TideTraitsComponents.MUTATION_SEED) == null,
                "Invalid canonical transfer restored legacy seed fallback");
        helper.assertTrue(restored.get(TideTraitsComponents.MUTATION) == null,
                "Invalid canonical transfer restored legacy mutation fallback");
        helper.complete();
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:cod",
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                0x1234_5678_9ABCL,
                97.25,
                38.5,
                47.75,
                99.125,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(812.375),
                OptionalInt.of(2711),
                SpecimenData.Provenance.generated()
        );
    }

    private static void assertPersistedFields(TestContext helper, SpecimenData expected, SpecimenData actual) {
        helper.assertTrue(actual != null, "Canonical specimen could not be read");
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
        helper.assertTrue(expected.specimenQuality() == actual.specimenQuality(), "Quality changed");
        helper.assertTrue(expected.perfectCatch() == actual.perfectCatch(), "Perfect Catch changed");
        helper.assertTrue(expected.rawFishScore().equals(actual.rawFishScore()), "Raw FishScore changed");
        helper.assertTrue(expected.fishScore().equals(actual.fishScore()), "FishScore changed");
    }
}
