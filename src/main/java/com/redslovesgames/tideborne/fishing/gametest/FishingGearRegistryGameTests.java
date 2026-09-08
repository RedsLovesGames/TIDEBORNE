package com.redslovesgames.tideborne.fishing.gametest;

import com.li64.tide.registries.TideItems;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.registry.TideboundItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/** Runtime coverage proving canonical profile IDs match the actual registered Tide/Tideborne items. */
public final class FishingGearRegistryGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void sevenRealLoadoutsConvergeAcrossCastFightAndSatchel(TestContext helper) {
        var config = com.redslovesgames.tideborne.config.TideboundConfig.get();
        boolean myths = com.redslovesgames.tideborne.compat.TideboundCompatibility.isMythsIntegrationActive();
        boolean apex = com.redslovesgames.tideborne.compat.TideboundCompatibility.isApexIntegrationActive();
        var player = helper.createMockCreativeServerPlayerInWorld();
        for (var build : GearArchetypeCases.builds()) {
            ItemStack rod = new ItemStack(Registries.ITEM.get(build.rod().itemId()));
            ItemStack line = new ItemStack(Registries.ITEM.get(build.line().itemId()));
            ItemStack hookItem = new ItemStack(Registries.ITEM.get(build.hook().itemId()));
            ItemStack bobber = new ItemStack(Registries.ITEM.get(build.bobberId()));
            ItemStack bait = new ItemStack(Registries.ITEM.get(build.bait().itemId()));
            ItemStack leader = new ItemStack(com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.item(build.leader()));
            var items = java.util.List.of(rod, line, hookItem, bobber, bait, leader);
            for (int i = 0; i < 6; i++) helper.assertTrue(FishingGearRegistry.accepts(
                    com.redslovesgames.tideborne.satchel.SatchelPreset.SLOTS.get(i), items.get(i)), "Unregistered/invalid gear: " + build.name() + "/" + i);
            com.li64.tide.data.rods.CustomRodManager.setLine(rod, line);
            com.li64.tide.data.rods.CustomRodManager.setHook(rod, hookItem);
            com.li64.tide.data.rods.CustomRodManager.setBobber(rod, bobber);
            com.li64.tide.data.item.TideItemData.BAIT_CONTENTS.set(rod, new com.li64.tide.data.rods.BaitContents(java.util.List.of(bait)));
            com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.set(rod, build.leader());
            helper.assertTrue(com.li64.tide.util.BaitUtils.getCombinedLuck(rod) == build.nativeBaitLuck(), "Native bait luck mismatch: " + build.name());
            helper.assertTrue(com.li64.tide.util.BaitUtils.getCombinedSpeed(rod) == build.nativeBaitLure(), "Native bait lure mismatch: " + build.name());
            var saved = rod.encode(helper.getWorld().getRegistryManager()).copy();
            // Constructor inputs are native bait + Gold luck, as supplied by Tide's rod-use path.
            int nativeGold = rod.isIn(com.li64.tide.data.TideTags.Items.LUCK_BOOSTING_RODS) ? 1 : 0;
            var hook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(
                    com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER, player, helper.getWorld(),
                    nativeGold + build.nativeBaitLuck(), build.nativeBaitLure(), 0, rod);
            var expected = build.contributions(myths, apex, config);
            var fight = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forCanonicalFight(hook, config);
            var selection = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forSelection(hook.getContext(), config);
            var preview = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forLoadoutPreview(rod, config);
            var expectedValues = GearArchetypeCases.values(expected);
            var fightValues = GearArchetypeCases.values(fight);
            var selectionValues = GearArchetypeCases.values(selection);
            var previewValues = GearArchetypeCases.values(preview);
            for (int i = 0; i < expectedValues.length; i++) {
                double runtime = i < 2 ? selectionValues[i] : i == 8 ? hook.getLureSpeed() : fightValues[i];
                helper.assertTrue(Math.abs(expectedValues[i] - runtime) < 1e-9, build.name() + " runtime axis " + i + ": " + runtime + " expected " + expectedValues[i]);
                helper.assertTrue(Math.abs(expectedValues[i] - previewValues[i]) < 1e-9, build.name() + " preview axis " + i);
            }
            var context = new com.redslovesgames.tideborne.fishing.tide.TideFishingContextAdapter().context(hook.getContext());
            helper.assertTrue(context.fishingLuck() == nativeGold, "Gear luck leaked into native input: " + build.name());
            helper.assertTrue(saved.equals(rod.encode(helper.getWorld().getRegistryManager())), "Queries changed actual gear: " + build.name());
            if (!build.name().equals("Leviathan") || myths) helper.assertTrue(
                    com.redslovesgames.tideborne.satchel.SatchelGearSummary.archetypes(preview).contains(build.name()), "Missing canonical specialization: " + build.name());
        }
        player.discard();
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void leaderPersistenceAndLeviathanRuntimeBoundaries(TestContext helper) {
        var registries = helper.getWorld().getRegistryManager();
        var rod = new ItemStack(TideItems.IRON_FISHING_ROD);
        rod.set(com.redslovesgames.tideborne.registry.TideTraitsComponents.STEEL_LEADER_ATTACHED, true);
        var legacy = ItemStack.fromNbt(registries, rod.encode(registries)).orElseThrow();
        helper.assertTrue(com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.tier(legacy)
                == com.redslovesgames.tideborne.fishing.gear.LeaderTier.IRON, "Legacy steel attachment lost during load");
        for (var tier : com.redslovesgames.tideborne.fishing.gear.LeaderTier.values()) {
            com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.set(rod, tier);
            var restored = ItemStack.fromNbt(registries, rod.encode(registries)).orElseThrow();
            helper.assertTrue(com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.tier(restored) == tier,
                    "Leader tier persistence failed: " + tier);
            helper.assertTrue(Boolean.TRUE.equals(restored.get(com.redslovesgames.tideborne.registry.TideTraitsComponents.STEEL_LEADER_ATTACHED)),
                    "Legacy compatibility marker lost: " + tier);
        }
        helper.assertTrue(Registries.ITEM.getId(TideboundItems.IRON_LEADER).toString().equals("tidebound_compatibility:steel_leader"),
                "Persisted Iron Leader item ID changed");
        com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.clear(rod);
        helper.assertTrue(!com.redslovesgames.tideborne.fishing.gear.LeaderAttachment.has(rod), "Leader removal retained attachment");
        var bait = new ItemStack(TideboundItems.LEVIATHAN_BAIT);
        helper.assertTrue(com.li64.tide.util.BaitUtils.isBait(bait), "Leviathan native bait registration missing");
        helper.assertTrue(com.li64.tide.util.BaitUtils.getBaitLuck(bait) == 0, "Native Leviathan luck duplicates canonical +4");
        com.li64.tide.data.item.TideItemData.BAIT_CONTENTS.set(rod, new com.li64.tide.data.rods.BaitContents(java.util.List.of(bait)));
        var hook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(
                com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER, helper.createMockCreativeServerPlayerInWorld(), helper.getWorld(), 0, 0, 0, rod);
        var config = com.redslovesgames.tideborne.config.TideboundConfig.get();
        if (!com.redslovesgames.tideborne.compat.TideboundCompatibility.isMythsIntegrationActive()) {
            helper.assertTrue(!com.redslovesgames.tideborne.fishing.gear.LeviathanBaitFishing.isEnabledFor(hook, config),
                    "Absent Myths integration activated Leviathan");
            var gear = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forSelection(hook.getContext(), config);
            helper.assertTrue(gear.fishingLuck() == 0 && gear.traitLuck() == 0, "Inactive Leviathan leaked bonuses");
        }
        var settings = com.redslovesgames.tideborne.network.TideboundSettingsPayload.fromValues(config, true, true).tag();
        helper.assertTrue(settings.getInt("leviathan_fish_luck") == 4 && settings.getInt("leviathan_trait_luck") == 1,
                "Display settings disagree with canonical bait luck");
        helper.assertTrue(settings.getDouble("leviathan_strength") == 1.30 && settings.getDouble("leviathan_tempo") == 1.20,
                "Display settings disagree with canonical bait fight penalties");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void allBobbersAreAppliedOnceAndCapturedFromServerConfig(TestContext helper) {
        var player = helper.createMockCreativeServerPlayerInWorld();
        var config = com.redslovesgames.tideborne.config.TideboundConfig.get();
        for (var id : FishingGearRegistry.supportedBobberIds()) {
            var rod = new ItemStack(TideItems.IRON_FISHING_ROD);
            var bobber = new ItemStack(Registries.ITEM.get(id));
            helper.assertTrue(bobber.isIn(com.li64.tide.data.TideTags.Items.BOBBERS), "Bobber is unregistered or untagged: " + id);
            com.li64.tide.data.rods.CustomRodManager.setBobber(rod, bobber);
            var hook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(
                    com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER, player, helper.getWorld(), 7, 3, 0, rod);
            var expected = FishingGearRegistry.bobberModifiers(id).orElseThrow();
            var captured = com.redslovesgames.tideborne.fishing.gear.BobberGearModifiers.forHook(hook);
            helper.assertTrue(captured.equals(expected), "Full canonical bobber differs: " + id);
            helper.assertTrue(hook.getLuck() == 7 + expected.fishingLuck(), "Native bobber luck duplicated: " + id);
            helper.assertTrue(hook.getLureSpeed() == 3 + expected.namedAdditiveModifier(com.redslovesgames.tideborne.fishing.gear.FishingGearEffects.LURE_BONUS), "Native bobber lure duplicated: " + id);
            var nativeContext = new com.redslovesgames.tideborne.fishing.tide.TideFishingContextAdapter().context(hook.getContext());
            var gear = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forSelection(hook.getContext(), config);
            helper.assertTrue(nativeContext.fishingLuck() == 7, "Bobber luck leaked into uncapped native contribution: " + id);
            helper.assertTrue(nativeContext.withGearModifiers(gear).fishingLuck() == 7 + expected.fishingLuck(), "Canonical bobber luck duplicated: " + id);
            helper.assertTrue(gear.traitLuck() == expected.traitLuck(), "Canonical bobber trait luck duplicated: " + id);
            com.li64.tide.data.rods.CustomRodManager.setBobber(rod, new ItemStack(TideItems.RED_BOBBER));
            helper.assertTrue(com.redslovesgames.tideborne.fishing.gear.BobberGearModifiers.forHook(hook).equals(captured), "Cast snapshot changed after equipment changed");
        }
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void nativeHooksBaitsAndTargetMetadataRemainAuthoritative(TestContext helper) {
        var neutral = nativeHook(helper, "fishing_hook");
        helper.assertTrue(Math.abs(nativeHook(helper, "fiery_hook").getContext().temperature() - neutral.getContext().temperature() - 0.25) < 1e-6,
                "Fiery temperature behavior changed");
        helper.assertTrue(Math.abs(nativeHook(helper, "permafrost_hook").getContext().temperature() - neutral.getContext().temperature() + 0.25) < 1e-6,
                "Permafrost temperature behavior changed");
        helper.assertTrue(nativeHook(helper, "lavaproof_hook").canFishInLava(), "Native lava eligibility missing");
        helper.assertTrue(nativeHook(helper, "void_hook").canFishInVoid(), "Native void eligibility missing");
        var day = new com.li64.tide.data.fishing.conditions.types.TimeOfDayCondition(java.util.List.of(
                new com.li64.tide.data.fishing.conditions.types.TimeRange(0, 11999)));
        helper.assertTrue(day.test(neutral.getContext()) != day.test(nativeHook(helper, "twilight_hook").getContext()),
                "Native twilight time shift missing");
        String[] baits = {"bait", "lucky_bait", "magnetic_bait", "incandescent_bait", "abyss_bait"};
        int[] lure = {2, 0, 0, 1, 2};
        for (int i = 0; i < baits.length; i++) {
            var bait = new ItemStack(Registries.ITEM.get(Identifier.of("tide", baits[i])));
            helper.assertTrue(com.li64.tide.util.BaitUtils.getBaitSpeed(bait) == lure[i], "Native bait lure changed: " + baits[i]);
            helper.assertTrue(com.li64.tide.util.BaitUtils.getBaitLuck(bait) == (i == 1 ? 2 : 0), "Native bait luck changed: " + baits[i]);
        }
        var adapter = new com.redslovesgames.tideborne.fishing.tide.TideSpeciesProfileAdapter();
        for (String[] sample : new String[][]{{"inferno_guppy", "warm"}, {"guppy", "warm"}, {"abyss_angler", "deep"}, {"voidseeker", "deep"}}) {
            var data = com.li64.tide.data.fishing.FishData.get(Registries.ITEM.get(Identifier.of("tide", sample[0]))).orElseThrow();
            helper.assertTrue(adapter.adaptForMigration(data).targetTags().contains(sample[1]), "Native species classification missing: " + sample[0]);
        }
        helper.complete();
    }

    private static com.li64.tide.registries.entities.misc.fishing.TideFishingHook nativeHook(TestContext helper, String hookId) {
        var rod = new ItemStack(TideItems.IRON_FISHING_ROD);
        com.li64.tide.data.rods.CustomRodManager.setHook(rod, new ItemStack(Registries.ITEM.get(Identifier.of("tide", hookId))));
        var hook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER,
                helper.createMockCreativeServerPlayerInWorld(), helper.getWorld(), 0, 0, 0, rod);
        hook.setPosition(helper.getAbsolutePos(net.minecraft.util.math.BlockPos.ORIGIN).toCenterPos());
        return hook;
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void frozenRodAndLineRuntimeComposition(TestContext helper) {
        var player = helper.createMockCreativeServerPlayerInWorld();
        var config = com.redslovesgames.tideborne.config.TideboundConfig.get();
        for (var profile : FishingGearRegistry.GearProfile.values()) {
            if (profile.slot() == FishingGearRegistry.Slot.ROD || profile.slot() == FishingGearRegistry.Slot.LINE)
                assertGear(helper, new ItemStack(Registries.ITEM.get(profile.itemId())), profile);
        }
        ItemStack rod = new ItemStack(TideItems.DIAMOND_FISHING_ROD);
        com.li64.tide.data.rods.CustomRodManager.setLine(rod, new ItemStack(TideItems.DIAMOND_LINE));
        var hook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(
                com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER, player, helper.getWorld(), 7, 0, 0, rod);
        var gear = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forCanonicalFight(hook, config);
        helper.assertTrue(Math.abs(gear.strengthMultiplier() - 0.92 * 0.82) < 1e-12, "Rod/line applied twice or omitted");
        helper.assertTrue(hook.getLuck() == 7, "Rod composition duplicated native luck");
        helper.assertTrue(new ItemStack(TideItems.GOLDEN_FISHING_ROD).isIn(com.li64.tide.data.TideTags.Items.LUCK_BOOSTING_RODS),
                "Tide native Gold luck tag was lost");
        if (!com.redslovesgames.tideborne.compat.TideboundCompatibility.isMythsIntegrationActive()) {
            com.li64.tide.data.rods.CustomRodManager.setLine(rod, new ItemStack(TideboundItems.TENTACLE_LINE));
            var optionalHook = new com.li64.tide.registries.entities.misc.fishing.TideFishingHook(
                    com.li64.tide.registries.TideEntityTypes.FISHING_BOBBER, player, helper.getWorld(), 0, 0, 0, rod);
            var optional = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forCanonicalFight(optionalHook, config);
            helper.assertTrue(com.redslovesgames.tideborne.fishing.gear.FishingGearEffects.catchZoneAreaMultiplier(optional) == 1,
                    "Absent optional line integration contributed gameplay effects");
        }
        helper.complete();
    }

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
