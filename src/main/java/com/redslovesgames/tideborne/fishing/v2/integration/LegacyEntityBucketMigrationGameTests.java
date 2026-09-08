package com.redslovesgames.tideborne.fishing.v2.integration;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tidetraits.entity.SpecimenEntity;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class LegacyEntityBucketMigrationGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyEntitySpecimenMigratesInPlaceAndIsIdempotent(TestContext helper) {
        CodEntity fish = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
        SpecimenEntity specimenEntity = (SpecimenEntity) fish;
        specimenEntity.tideTraits$setSpecimenTag(legacyTag(0x12345678ABCDEFL, 73.0, 51.25, "scarred", "giant"));

        helper.assertTrue(SpecimenTransfer.migrateEntitySpecimen(fish),
                "Legacy living fish entity was not migrated through the canonical service");
        NbtCompound migratedTag = specimenEntity.tideTraits$getSpecimenTag();
        helper.assertTrue(CanonicalSpecimenStorage.hasTransferPayload(migratedTag),
                "Migrated entity did not receive canonical transfer data");

        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(fish, restored);
        SpecimenData migrated = CanonicalSpecimenStorage.read(restored).orElseThrow();
        assertLegacyIdentity(helper, migrated, 0x12345678ABCDEFL, 73.0, 51.25,
                SpecimenData.BodyType.GIANT, SpecimenData.Condition.SCARRED);

        helper.assertTrue(!SpecimenTransfer.migrateEntitySpecimen(fish),
                "Current canonical entity was migrated a second time");
        ItemStack repeatedStack = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(fish, repeatedStack);
        helper.assertTrue(migrated.equals(CanonicalSpecimenStorage.read(repeatedStack).orElseThrow()),
                "Repeated entity transfer changed the canonical migrated specimen");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyBucketPayloadMigratesWhenReleased(TestContext helper) {
        NbtCompound bucketTag = new NbtCompound();
        bucketTag.put(SpecimenTransfer.ENTITY_KEY,
                legacyTag(0x55AA77112233L, 18.5, 24.75, "albino", "dwarf"));

        CodEntity released = (CodEntity) helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 3));
        SpecimenTransfer.bucketTagToEntity(bucketTag, released);
        NbtCompound migratedTag = ((SpecimenEntity) released).tideTraits$getSpecimenTag();
        helper.assertTrue(CanonicalSpecimenStorage.hasTransferPayload(migratedTag),
                "Legacy bucket payload was not upgraded on release");

        ItemStack restored = new ItemStack(Items.COD);
        SpecimenTransfer.entityToStack(released, restored);
        SpecimenData migrated = CanonicalSpecimenStorage.read(restored).orElseThrow();
        assertLegacyIdentity(helper, migrated, 0x55AA77112233L, 18.5, 24.75,
                SpecimenData.BodyType.DWARF, SpecimenData.Condition.NORMAL);
        helper.assertTrue(migrated.pigmentation() == SpecimenData.Pigmentation.ALBINO,
                "Legacy Albino bucket mutation did not migrate to Pigmentation");
        helper.complete();
    }

    private static NbtCompound legacyTag(long seed, double percentile, double length, String mutation, String bodyType) {
        NbtCompound tag = new NbtCompound();
        tag.putInt(SpecimenTransfer.VERSION_KEY, 5);
        tag.putLong(SpecimenTransfer.SEED_KEY, seed);
        tag.putDouble(SpecimenTransfer.PERCENTILE_KEY, percentile);
        tag.putDouble(SpecimenTransfer.LENGTH_KEY, length);
        tag.putString(SpecimenTransfer.MUTATION_KEY, mutation);
        tag.putString(SpecimenTransfer.BODY_TYPE_KEY, bodyType);
        return tag;
    }

    private static void assertLegacyIdentity(
            TestContext helper,
            SpecimenData actual,
            long seed,
            double percentile,
            double length,
            SpecimenData.BodyType bodyType,
            SpecimenData.Condition condition
    ) {
        helper.assertTrue(actual.schemaVersion() == SpecimenGenerator.SCHEMA_VERSION,
                "Migrated entity schema version was not current");
        helper.assertTrue(actual.deterministicSeed() == seed, "Legacy entity seed changed during migration");
        helper.assertTrue(Double.compare(actual.basePercentile(), percentile) == 0,
                "Legacy entity percentile changed during migration");
        helper.assertTrue(Double.compare(actual.finalLength(), length) == 0,
                "Legacy entity physical length changed during migration");
        helper.assertTrue(actual.bodyType() == bodyType, "Legacy entity Body Type changed during migration");
        helper.assertTrue(actual.condition() == condition, "Legacy entity Condition changed during migration");
        helper.assertTrue(actual.rawFishScore().isPresent(), "Migrated entity is missing canonical raw FishScore");
        helper.assertTrue(actual.fishScore().isPresent(), "Migrated entity is missing canonical FishScore");
    }
}
