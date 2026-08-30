package com.redslovesgames.tideborne.gametest;

import com.redslovesgames.tideborne.registry.TideborneItemGroups;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
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
}
