package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class CrateFishProgressionBridgeGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void lengthBearingTideAwardBecomesCanonicalBeforeAccounting(TestContext helper) {
        ItemStack stack = registeredSizedFishStack();
        TideItemData.FISH_LENGTH.set(stack, 48.25);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Fixture unexpectedly started with specimen metadata");
        helper.assertTrue(CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(stack),
                "Length-bearing Tide award was not prepared for normal catch accounting");
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(specimen.finalLength() == 48.25, "Crate/accounting bridge did not preserve awarded physical length");
        helper.assertTrue(specimen.fishScore().isPresent(), "Crate/accounting bridge did not create canonical FishScore");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stack) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Prepared crate fish was not current canonical data");
        SpecimenData second = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(specimen.equals(second), "Prepared crate fish changed on repeated canonical read");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void bridgeDoesNotInventSpecimenForInvalidAward(TestContext helper) {
        ItemStack nonFish = new ItemStack(Items.STONE);
        helper.assertTrue(!CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(nonFish),
                "Non-fish award was accepted as canonical progression data");
        ItemStack fishWithoutLength = registeredSizedFishStack();
        helper.assertTrue(!CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(fishWithoutLength),
                "Fish without recoverable physical identity was fabricated into a specimen");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(fishWithoutLength) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Failed preparation mutated an incomplete award");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void existingCanonicalSpecimenIsNeverRerolledByBridge(TestContext helper) {
        ItemStack stack = registeredSizedFishStack();
        TideItemData.FISH_LENGTH.set(stack, 61.0);
        helper.assertTrue(CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(stack), "Initial preparation failed");
        SpecimenData before = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(stack), "Current canonical fish was rejected");
        SpecimenData after = CanonicalSpecimenStorage.read(stack).orElseThrow();
        helper.assertTrue(before.equals(after), "Accounting bridge rerolled or mutated current canonical specimen state");
        helper.complete();
    }

    private static ItemStack registeredSizedFishStack() {
        FishData data = TideData.FISH.get().values().stream()
                .filter(fish -> fish.size().isPresent())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Tide sized-fish registry is empty during GameTest"));
        return new ItemStack((Item) data.fish().value());
    }
}
