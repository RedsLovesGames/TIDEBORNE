package com.redslovesgames.tideborne.gametest;

import com.redslovesgames.tideborne.registry.TideborneItemGroups;
import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import com.redslovesgames.tideborne.registry.TideboundItems;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/** Runtime coverage for the dedicated Tideborne creative-mode tab. */
public final class TideborneItemGroupsGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void tideborneCreativeTabIsRegisteredWithSatchelIcon(TestContext helper) {
        ItemGroup registered = Registries.ITEM_GROUP.get(TideborneItemGroups.TIDEBORNE_ID);
        helper.assertTrue(registered == TideborneItemGroups.TIDEBORNE,
                "Dedicated Tideborne creative tab was not registered at tideborne:tideborne");
        helper.assertTrue(registered.getIcon().isOf(SatchelRegistration.ANGLERS_SATCHEL),
                "Tideborne creative tab icon is not the Angler's Satchel");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void creativeTabCompatibilityMatrixHasExactStableOrder(TestContext helper) {
        assertItems(helper, TideborneItemGroups.creativeItems(false, false),
                SatchelRegistration.ANGLERS_SATCHEL);

        assertItems(helper, TideborneItemGroups.creativeItems(true, false),
                SatchelRegistration.ANGLERS_SATCHEL,
                TideboundItems.KUJIRA_BONE_FISHING_ROD,
                TideboundItems.TENTACLE_LINE,
                TideboundItems.SWIFT_LINE,
                TideboundItems.SEAFARERS_HOOK,
                TideboundItems.LEVIATHAN_BAIT);

        assertItems(helper, TideborneItemGroups.creativeItems(false, true),
                SatchelRegistration.ANGLERS_SATCHEL,
                TideboundItems.COPPER_LEADER,
                TideboundItems.IRON_LEADER,
                TideboundItems.GOLD_LEADER,
                TideboundItems.DIAMOND_LEADER,
                TideboundItems.SHARK_TOOTH_HOOK,
                TideboundItems.CHUM_BUCKET,
                TideboundItems.SHARK_TOOTH);

        assertItems(helper, TideborneItemGroups.creativeItems(true, true),
                SatchelRegistration.ANGLERS_SATCHEL,
                TideboundItems.KUJIRA_BONE_FISHING_ROD,
                TideboundItems.TENTACLE_LINE,
                TideboundItems.SWIFT_LINE,
                TideboundItems.COPPER_LEADER,
                TideboundItems.IRON_LEADER,
                TideboundItems.GOLD_LEADER,
                TideboundItems.DIAMOND_LEADER,
                TideboundItems.SEAFARERS_HOOK,
                TideboundItems.SHARK_TOOTH_HOOK,
                TideboundItems.LEVIATHAN_BAIT,
                TideboundItems.CHUM_BUCKET,
                TideboundItems.SHARK_TOOTH);

        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void currentCreativeItemsFollowActiveIntegrationFlags(TestContext helper) {
        boolean mythsActive = FishingGameplayInitializer.isMythsIntegrationActive();
        boolean apexActive = FishingGameplayInitializer.isApexIntegrationActive();
        List<Item> current = TideborneItemGroups.currentCreativeItems();

        helper.assertTrue(current.equals(TideborneItemGroups.creativeItems(mythsActive, apexActive)),
                "Current Tideborne creative items did not match active optional-integration flags");
        helper.assertTrue(current.contains(TideboundItems.KUJIRA_BONE_FISHING_ROD) == mythsActive,
                "Kujira rod visibility did not follow Myths integration state");
        helper.assertTrue(current.contains(TideboundItems.LEVIATHAN_BAIT) == mythsActive,
                "Leviathan Bait visibility did not follow Myths integration state");
        helper.assertTrue(current.contains(TideboundItems.COPPER_LEADER) == apexActive,
                "Copper Leader visibility did not follow Apex integration state");
        helper.assertTrue(current.contains(TideboundItems.IRON_LEADER) == apexActive,
                "Iron Leader visibility did not follow Apex integration state");
        helper.assertTrue(current.contains(TideboundItems.GOLD_LEADER) == apexActive,
                "Gold Leader visibility did not follow Apex integration state");
        helper.assertTrue(current.contains(TideboundItems.DIAMOND_LEADER) == apexActive,
                "Diamond Leader visibility did not follow Apex integration state");
        helper.assertTrue(current.contains(TideboundItems.CHUM_BUCKET) == apexActive,
                "Chum Bucket visibility did not follow Apex integration state");
        helper.complete();
    }

    private static void assertItems(TestContext helper, List<Item> actual, Item... expected) {
        helper.assertTrue(actual.size() == expected.length,
                "Unexpected Tideborne creative item count: expected " + expected.length + ", got " + actual.size());
        for (int index = 0; index < expected.length; index++) {
            helper.assertTrue(actual.get(index) == expected[index],
                    "Unexpected Tideborne creative item at index " + index);
        }
    }
}
