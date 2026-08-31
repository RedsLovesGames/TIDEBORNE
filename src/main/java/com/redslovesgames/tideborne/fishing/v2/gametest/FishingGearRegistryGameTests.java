package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.registries.TideItems;
import com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/** Runtime coverage proving canonical profile IDs match the actual registered Tide/Tideborne items. */
public final class FishingGearRegistryGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void canonicalGearRegistryResolvesRegisteredRuntimeItems(TestContext helper) {
        assertGear(helper, new ItemStack(TideItems.COPPER_LINE), FishingGearRegistry.GearProfile.TIDE_COPPER_LINE);
        assertGear(helper, new ItemStack(TideItems.IRON_LINE), FishingGearRegistry.GearProfile.TIDE_IRON_LINE);
        assertGear(helper, new ItemStack(TideItems.GOLDEN_LINE), FishingGearRegistry.GearProfile.TIDE_GOLDEN_LINE);
        assertGear(helper, new ItemStack(TideItems.DIAMOND_LINE), FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE);

        assertGear(helper, new ItemStack(TideboundItems.TENTACLE_LINE), FishingGearRegistry.GearProfile.TENTACLE_LINE);
        assertGear(helper, new ItemStack(TideboundItems.SWIFT_LINE), FishingGearRegistry.GearProfile.SWIFT_LINE);
        assertGear(helper, new ItemStack(TideboundItems.COPPER_LEADER), FishingGearRegistry.GearProfile.COPPER_LEADER);
        assertGear(helper, new ItemStack(TideboundItems.IRON_LEADER), FishingGearRegistry.GearProfile.IRON_LEADER);
        assertGear(helper, new ItemStack(TideboundItems.GOLD_LEADER), FishingGearRegistry.GearProfile.GOLD_LEADER);
        assertGear(helper, new ItemStack(TideboundItems.DIAMOND_LEADER), FishingGearRegistry.GearProfile.DIAMOND_LEADER);
        assertGear(helper, new ItemStack(TideboundItems.SEAFARERS_HOOK), FishingGearRegistry.GearProfile.SEAFARERS_HOOK);
        assertGear(helper, new ItemStack(TideboundItems.SHARK_TOOTH_HOOK), FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK);
        assertGear(helper, new ItemStack(TideboundItems.KUJIRA_BONE_FISHING_ROD), FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD);
        assertGear(helper, new ItemStack(TideboundItems.LEVIATHAN_BAIT), FishingGearRegistry.GearProfile.LEVIATHAN_BAIT);
        helper.complete();
    }

    private static void assertGear(
            TestContext helper,
            ItemStack stack,
            FishingGearRegistry.GearProfile expected
    ) {
        Identifier actualId = Registries.ITEM.getId(stack.getItem());
        helper.assertTrue(
                actualId.equals(expected.itemId()),
                "Runtime item ID mismatch for " + expected + ": expected " + expected.itemId() + ", got " + actualId
        );
        helper.assertTrue(
                FishingGearRegistry.resolve(stack).filter(expected::equals).isPresent(),
                "Canonical gear registry did not resolve runtime item " + actualId + " as " + expected
        );
    }
}
