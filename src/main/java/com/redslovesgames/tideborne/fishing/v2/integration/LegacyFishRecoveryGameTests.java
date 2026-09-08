package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class LegacyFishRecoveryGameTests implements FabricGameTest {
    private final LegacyFishRecoveryService recovery = new LegacyFishRecoveryService();

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void repairUsesDeterministicLegacyMigrationAndPreservesIdentity(TestContext helper) {
        ItemStack first = legacyFish(42L, 73.0, 51.25, "giant", "scarred");
        ItemStack second = first.copy();
        LegacyFishRecoveryService.Result firstResult = recovery.repair(first);
        LegacyFishRecoveryService.Result secondResult = recovery.repair(second);
        helper.assertTrue(firstResult.status() == LegacyFishRecoveryService.Status.REPAIRED, "Legacy fish was not repaired");
        helper.assertTrue(secondResult.status() == LegacyFishRecoveryService.Status.REPAIRED, "Identical legacy fish was not repaired");
        SpecimenData a = firstResult.specimen().orElseThrow();
        SpecimenData b = secondResult.specimen().orElseThrow();
        helper.assertTrue(a.equals(b), "Legacy repair was not deterministic");
        helper.assertTrue(a.deterministicSeed() == 42L, "Repair did not preserve deterministic seed");
        helper.assertTrue(a.basePercentile() == 73.0, "Repair did not preserve percentile");
        helper.assertTrue(a.finalLength() == 51.25, "Repair did not preserve physical length");
        helper.assertTrue(a.bodyType() == SpecimenData.BodyType.GIANT, "Repair did not preserve Giant body type");
        helper.assertTrue(a.condition() == SpecimenData.Condition.SCARRED, "Repair did not preserve condition");
        helper.assertTrue(a.fishScore().isPresent(), "Repair did not produce canonical FishScore");
        helper.assertTrue(recovery.repair(first).status() == LegacyFishRecoveryService.Status.ALREADY_CURRENT,
                "Repair was not idempotent after canonical rewrite");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void rerollRequiresConfirmationWithoutMutatingLegacyFish(TestContext helper) {
        ItemStack stack = legacyFish(7L, 12.5, 33.0, "dwarf", "albino");
        LegacyFishRecoveryService.Result rejected = recovery.reroll(stack, 99L, false);
        helper.assertTrue(rejected.status() == LegacyFishRecoveryService.Status.CONFIRMATION_REQUIRED,
                "Unconfirmed reroll was not rejected");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY,
                "Unconfirmed reroll mutated or repaired legacy state");
        helper.assertTrue(Long.valueOf(7L).equals(stack.get(TideTraitsComponents.MUTATION_SEED)),
                "Unconfirmed reroll changed legacy identity");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void confirmedRerollReplacesIdentityWithDeterministicCanonicalData(TestContext helper) {
        ItemStack first = legacyFish(7L, 12.5, 33.0, "dwarf", "albino");
        ItemStack second = first.copy();
        SpecimenData a = recovery.reroll(first, 123456789L, true).specimen().orElseThrow();
        SpecimenData b = recovery.reroll(second, 123456789L, true).specimen().orElseThrow();
        helper.assertTrue(a.equals(b), "Explicit reroll was not deterministic for an explicit seed");
        helper.assertTrue(a.deterministicSeed() == 123456789L, "Reroll did not use the requested new specimen seed");
        helper.assertTrue(a.speciesId().equals(b.speciesId()), "Reroll did not preserve species identity");
        helper.assertTrue(a.fishScore().isPresent(), "Reroll did not finalize canonical FishScore");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(first) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Confirmed reroll did not write current canonical data");
        helper.complete();
    }

    private static ItemStack legacyFish(long seed, double percentile, double length, String body, String mutation) {
        FishData data = TideData.FISH.get().values().stream()
                .filter(fish -> fish.size().isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tide sized-fish registry is empty during GameTest"));
        ItemStack stack = new ItemStack((Item) data.fish().value());
        stack.set(TideTraitsComponents.MUTATION_SEED, seed);
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, percentile);
        stack.set(TideTraitsComponents.BODY_TYPE, body);
        stack.set(TideTraitsComponents.MUTATION, mutation);
        TideItemData.FISH_LENGTH.set(stack, length);
        return stack;
    }
}
