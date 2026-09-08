package com.redslovesgames.tideborne.fishing.gametest;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.satchel.AnglersSatchelStorage;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import com.redslovesgames.tideborne.satchel.SatchelPreset;
import com.redslovesgames.tideborne.satchel.SatchelTackleExchange;
import com.redslovesgames.tideborne.satchel.SatchelTackleHandler;
import com.redslovesgames.tideborne.satchel.SatchelGearSummary;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.data.rods.BaitContents;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.util.Hand;
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

public final class AnglersSatchelPersistenceGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void tacklePresetDecodeReusesUnchangedStateAndInvalidatesEdits(TestContext helper) throws Exception {
        var player = (net.minecraft.server.network.ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        player.setStackInHand(Hand.MAIN_HAND, satchel);
        var handler = new SatchelTackleHandler(43, player.getInventory(), satchel, Hand.MAIN_HAND);
        handler.onButtonClick(player, SatchelTackleHandler.NEW);
        handler.sendContentUpdates();
        var field = SatchelTackleHandler.class.getDeclaredField("decodedPresets");
        field.setAccessible(true);
        Object first = field.get(handler);
        handler.sendContentUpdates();
        helper.assertTrue(first == field.get(handler), "Unchanged update decoded presets again");
        var preset = AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst();
        AnglersSatchelStorage.setPresets(satchel, List.of(preset.renamed("Changed")), player.getRegistryManager());
        handler.sendContentUpdates();
        helper.assertTrue(first != field.get(handler), "Changed state retained old preset snapshot");
        helper.assertTrue(((List<SatchelPreset>) field.get(handler)).getFirst().name().equals("Changed"), "Rename was not decoded");
        player.discard();
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void mixedSatchelMigrationPreservesOriginalsAndCopyIsolation(TestContext helper) {
        ItemStack current = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(current, specimen());
        ItemStack lengthOnly = new ItemStack(Items.COD);
        TideItemData.FISH_LENGTH.set(lengthOnly, 35.0);
        ItemStack incomplete = current.copy();
        incomplete.remove(TideTraitsComponents.SPECIMEN_GENERATION_VERSION);
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        SatchelContents original = new SatchelContents(List.of(current, lengthOnly, incomplete));
        satchel.set(TideDataComponents.SATCHEL_CONTENTS, original);
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 3, "Migration changed contents count");
        var contents = AnglersSatchelStorage.contents(satchel);
        helper.assertTrue(ItemStack.areEqual(current, contents.get(0)), "Current payload changed");
        helper.assertTrue(CanonicalSpecimenStorage.readCurrent(contents.get(1)).isPresent(), "Length-only fish was skipped");
        helper.assertTrue(ItemStack.areEqual(incomplete, contents.get(2)), "Incomplete payload changed");
        helper.assertTrue(CanonicalSpecimenStorage.readCurrent(original.items().get(1)).isEmpty(), "Migration mutated original contents");
        contents.get(0).set(DataComponentTypes.CUSTOM_NAME, Text.literal("caller mutation"));
        helper.assertTrue(ItemStack.areEqual(current, AnglersSatchelStorage.contents(satchel).get(0)), "Contents exposed stored stacks");
        var persisted = satchel.get(TideDataComponents.SATCHEL_CONTENTS);
        AnglersSatchelStorage.size(satchel);
        helper.assertTrue(persisted == satchel.get(TideDataComponents.SATCHEL_CONTENTS), "Stable read rewrote contents");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void physicalPresetsPersistStacksWithoutChangingCatchStorage(TestContext helper) {
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        ItemStack fish = new ItemStack(Items.COD);
        CanonicalSpecimenStorage.write(fish, specimen());
        AnglersSatchelStorage.insert(satchel, fish);
        var data = AnglersSatchelStorage.state(satchel).toTag();
        data.putString("future_extension", "keep");
        AnglersSatchelStorage.setState(satchel, com.redslovesgames.tideborne.satchel.SatchelState.fromTag(data));
        var items = emptyPockets();
        items.set(0, new ItemStack(Items.FISHING_ROD));
        items.get(0).set(DataComponentTypes.CUSTOM_NAME, Text.literal("My rod"));
        items.set(4, tackle("tide:bait").copyWithCount(32));
        var preset = SatchelPreset.empty("My own build").withItems(items);
        AnglersSatchelStorage.setPresets(satchel, List.of(preset), helper.getWorld().getRegistryManager());
        ItemStack restored = ItemStack.fromNbt(helper.getWorld().getRegistryManager(), satchel.encode(helper.getWorld().getRegistryManager())).orElseThrow();
        var loaded = AnglersSatchelStorage.presets(restored, helper.getWorld().getRegistryManager()).getFirst();
        helper.assertTrue(loaded.id().equals(preset.id()) && loaded.name().equals("My own build"), "Preset identity/name changed");
        helper.assertTrue(ItemStack.areEqual(loaded.items().get(0), items.get(0)) && loaded.items().get(4).getCount() == 32, "Physical stack metadata/count lost");
        loaded.items().get(4).decrement(10);
        helper.assertTrue(loaded.items().get(4).getCount() == 32, "Preset exposes mutable authoritative stacks");
        helper.assertTrue(AnglersSatchelStorage.state(restored).toTag().getString("future_extension").equals("keep"), "Unknown state key lost");
        helper.assertTrue(CanonicalSpecimenStorage.read(AnglersSatchelStorage.contents(restored).getFirst()).orElseThrow().equals(specimen()), "Preset save changed stored catch");
        var malformed = AnglersSatchelStorage.state(restored).toTag();
        malformed.putString("physical_presets", "unrecognized future encoding");
        AnglersSatchelStorage.setState(restored, com.redslovesgames.tideborne.satchel.SatchelState.fromTag(malformed));
        boolean rejected = false;
        try { AnglersSatchelStorage.presets(restored, helper.getWorld().getRegistryManager()); }
        catch (IllegalStateException expected) { rejected = true; }
        helper.assertTrue(rejected && AnglersSatchelStorage.state(restored).toTag().equals(malformed), "Malformed data silently erased");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void legacyPresetMigrationMovesUniqueStoredRodsOnceAndPreservesMissingReferences(TestContext helper) {
        var player = (net.minecraft.server.network.ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        UUID id = UUID.randomUUID(), missing = UUID.randomUUID();
        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        var data = new NbtCompound(); data.putUuid("tideborne_satchel_rod_reference", id);
        rod.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        AnglersSatchelStorage.insert(satchel, rod);
        AnglersSatchelStorage.setState(satchel, AnglersSatchelStorage.state(satchel).withPresetRod(0, id).withPresetRod(1, missing));
        AnglersSatchelStorage.migratePresets(satchel, player);
        AnglersSatchelStorage.migratePresets(satchel, player);
        var presets = AnglersSatchelStorage.presets(satchel, player.getRegistryManager());
        helper.assertTrue(presets.size() == 2 && presets.getFirst().name().equals("Trophy Hunter"), "Legacy names not migrated exactly once");
        helper.assertTrue(ItemStack.areEqual(presets.getFirst().items().getFirst(), rod) && AnglersSatchelStorage.size(satchel) == 0, "Legacy migration copied/lost rod");
        helper.assertTrue(missing.equals(presets.get(1).legacyRod()), "Missing reference discarded");
        helper.assertTrue(AnglersSatchelStorage.state(satchel).presetRod(0).orElseThrow().equals(id), "Legacy data no longer readable");
        player.discard(); helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void partialPresetChangesOnlyRequestedSlotsAndNeverExtractsVirtualDefaults(TestContext helper) {
        ItemStack rod = tackle("tide:diamond_fishing_rod");
        CustomRodManager.setLine(rod, tackle("tide:diamond_line"));
        CustomRodManager.setBobber(rod, tackle("tide:echo_bobber"));
        rod.set(TideDataComponents.BAIT_CONTENTS, new BaitContents(List.of(tackle("tide:bait").copyWithCount(8), tackle("tide:lucky_bait").copyWithCount(3))));
        ItemStack before = rod.copy();
        var encodedBefore = rod.encode(helper.getWorld().getRegistryManager()).copy();
        var pockets = emptyPockets(); pockets.set(2, tackle("tide:fiery_hook"));
        var plan = SatchelTackleExchange.plan(rod, pockets);
        helper.assertTrue(plan.success(), plan.error());
        helper.assertTrue(plan.pockets().get(2).isEmpty(), "Virtual default hook turned into a physical free item");
        helper.assertTrue(encodedBefore.equals(rod.encode(helper.getWorld().getRegistryManager())),
                "Planning mutated current equipment: " + encodedBefore + " -> " + rod.encode(helper.getWorld().getRegistryManager()));
        ItemStack expected = before.copy(); CustomRodManager.setHook(expected, pockets.get(2));
        helper.assertTrue(ItemStack.areEqual(expected, plan.rod()), "Empty preset slots changed equipment");
        pockets = emptyPockets(); pockets.set(4, tackle("tide:magnetic_bait").copyWithCount(6));
        var baitSwap = SatchelTackleExchange.plan(rod, pockets);
        helper.assertTrue(baitSwap.success() && baitSwap.pockets().get(4).getCount() == 8, "First bait stack lost");
        helper.assertTrue(baitSwap.rod().get(TideDataComponents.BAIT_CONTENTS).size() == 2,
                "Bait slots changed: original " + encodedBefore + " current " + rod.encode(helper.getWorld().getRegistryManager())
                        + " planned " + baitSwap.rod().encode(helper.getWorld().getRegistryManager()));
        helper.assertTrue(ItemStack.areEqual(baitSwap.rod().get(TideDataComponents.BAIT_CONTENTS).get(1), rod.get(TideDataComponents.BAIT_CONTENTS).get(1)), "Additional native bait slot changed");
        var smaller = emptyPockets(); smaller.set(0, new ItemStack(Items.FISHING_ROD));
        helper.assertTrue(!SatchelTackleExchange.plan(rod, smaller).success(), "Cross-capacity swap would lose bait");
        helper.assertTrue(encodedBefore.equals(rod.encode(helper.getWorld().getRegistryManager())), "Rejected capacity swap mutated rod");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void rodOnlyPresetKeepsCurrentAccessoriesAndConservesDisplacedRodAttachments(TestContext helper) {
        ItemStack held = new ItemStack(Items.FISHING_ROD), incoming = tackle("tide:diamond_fishing_rod");
        CustomRodManager.setHook(held, tackle("tide:fiery_hook"));
        CustomRodManager.setHook(incoming, tackle("tide:permafrost_hook"));
        var pockets = emptyPockets(); pockets.set(0, incoming);
        var plan = SatchelTackleExchange.plan(held, pockets);
        helper.assertTrue(plan.success(), plan.error());
        helper.assertTrue(CustomRodManager.getHook(plan.rod()).isOf(tackle("tide:fiery_hook").getItem()), "Empty hook did not retain current hook");
        helper.assertTrue(CustomRodManager.getHook(plan.pockets().get(0)).isOf(tackle("tide:permafrost_hook").getItem()), "Incoming rod's displaced hook lost");
        helper.assertTrue(plan.rod().isOf(incoming.getItem()) && plan.pockets().get(0).isOf(held.getItem()), "Rod bodies lost");
        var invalid = emptyPockets(); invalid.set(2, tackle("tide:fiery_hook")); invalid.set(5, tackle("tidebound_compatibility:steel_leader"));
        invalid.get(5).set(DataComponentTypes.CUSTOM_NAME, Text.literal("Cannot erase this name"));
        ItemStack before = held.copy();
        helper.assertTrue(!SatchelTackleExchange.plan(held, invalid).success(), "Unsafe leader customization accepted");
        helper.assertTrue(ItemStack.areEqual(before, held) && invalid.get(2).getCount() == 1, "Failed plan applied partial changes");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void vanillaPresetSlotInteractionsAutosaveAndServerEquipIsAtomic(TestContext helper) {
        var player = (net.minecraft.server.network.ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL), rod = new ItemStack(Items.FISHING_ROD);
        player.setStackInHand(Hand.OFF_HAND, satchel); player.setStackInHand(Hand.MAIN_HAND, rod);
        var handler = new SatchelTackleHandler(41, player.getInventory(), satchel, Hand.OFF_HAND);
        player.currentScreenHandler = handler;
        helper.assertTrue(handler.onButtonClick(player, SatchelTackleHandler.NEW), "Could not create custom preset");
        handler.setCursorStack(new ItemStack(Items.STONE));
        handler.onSlotClick(2, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, player);
        helper.assertTrue(handler.getSlot(2).getStack().isEmpty() && handler.getCursorStack().isOf(Items.STONE), "Invalid item accepted");
        handler.setCursorStack(tackle("tide:fiery_hook"));
        handler.onSlotClick(2, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, player);
        helper.assertTrue(handler.getCursorStack().isEmpty() && AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst().items().get(2).getCount() == 1, "Click did not immediately save physical item");
        ItemStack unchanged = rod.copy();
        handler.onButtonClick(player, 0);
        helper.assertTrue(ItemStack.areEqual(unchanged, player.getMainHandStack()), "Selecting preset changed equipment");
        var physical = AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst();
        handler.rename(player, UUID.randomUUID(), "Wrong preset");
        helper.assertTrue(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst().name().equals(physical.name()), "Stale rename accepted");
        handler.rename(player, physical.id(), "Shark Hunt");
        handler.onButtonClick(player, SatchelTackleHandler.DELETE);
        helper.assertTrue(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).size() == 1, "Delete destroyed real items");
        helper.assertTrue(handler.onButtonClick(player, SatchelTackleHandler.EQUIP), "Server equip rejected");
        helper.assertTrue(CustomRodManager.getHook(player.getMainHandStack()).isOf(tackle("tide:fiery_hook").getItem()), "Server equip did not apply hook");
        helper.assertTrue(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst().items().get(2).isEmpty(), "Equipped item duplicated in preset");
        player.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.STONE));
        helper.assertTrue(!handler.onButtonClick(player, SatchelTackleHandler.NEW), "Detached Satchel session accepted mutation");
        player.discard(); helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void presetShiftClickFiltersAndArchetypesUsePhysicalCanonicalGear(TestContext helper) {
        var player = (net.minecraft.server.network.ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        player.setStackInHand(Hand.MAIN_HAND, satchel);
        var handler = new SatchelTackleHandler(42, player.getInventory(), satchel, Hand.MAIN_HAND);
        handler.onButtonClick(player, SatchelTackleHandler.NEW);
        player.getInventory().setStack(9, tackle("tide:echo_bobber"));
        handler.onSlotClick(12, 0, net.minecraft.screen.slot.SlotActionType.QUICK_MOVE, player);
        helper.assertTrue(handler.getSlot(3).hasStack() && player.getInventory().getStack(9).isEmpty(), "Shift-click did not route to filtered bobber slot");
        var preset = AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst();
        helper.assertTrue(SatchelGearSummary.forPreset(preset.items()).contains("Trait"), "Echo partial preset does not use canonical trait modifier");
        handler.quickMove(player, 3);
        helper.assertTrue(AnglersSatchelStorage.presets(satchel, player.getRegistryManager()).getFirst().items().get(3).isEmpty(), "Shift-click extraction not saved");
        String[] ids = {"minecraft:fishing_rod", "tide:diamond_line", "tide:fiery_hook", "tide:echo_bobber", "tide:bait", "tidebound_compatibility:steel_leader"};
        for (int i = 0; i < 6; i++) for (int j = 0; j < 6; j++)
            helper.assertTrue(FishingGearRegistry.accepts(SatchelPreset.SLOTS.get(i), tackle(ids[j])) == (i == j), "Slot filter crossed ownership: " + i + "/" + j);
        helper.assertTrue(!FishingGearRegistry.accepts(FishingGearRegistry.Slot.ROD, satchel), "Nested satchel accepted");
        helper.assertTrue(SatchelGearSummary.forPreset(emptyPockets()).isEmpty(), "Empty preset grants specialization");
        player.discard(); helper.complete();
    }

    private static ArrayList<ItemStack> emptyPockets() { return new ArrayList<>(java.util.Collections.nCopies(6, ItemStack.EMPTY)); }
    private static ItemStack tackle(String id) { return new ItemStack(net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(id))); }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void tackleStorageAndViewProtocolsRoundTrip(TestContext helper) {
        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        for (String id : List.of("minecraft:fishing_rod", "tide:diamond_line", "tide:fiery_hook", "tide:echo_bobber",
                "tide:bait", "tidebound_compatibility:steel_leader")) {
            ItemStack item = new ItemStack(net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(id)));
            helper.assertTrue(AnglersSatchelStorage.insert(satchel, item).fullyInserted(), "Tackle rejected: " + id);
        }
        helper.assertTrue(!AnglersSatchelStorage.insert(satchel, satchel).insertedAny(), "Nested Satchel accepted");
        helper.assertTrue(!AnglersSatchelStorage.insert(satchel, new ItemStack(Items.STONE)).insertedAny(), "Unrelated item accepted");
        var items = AnglersSatchelStorage.contents(satchel);
        for (int protocol : List.of(2, 3)) {
            NbtCompound tackle = new NbtCompound();
            tackle.putString("test", "server projection");
            var view = new com.redslovesgames.tideborne.satchel.network.SatchelView(protocol, net.minecraft.util.Hand.OFF_HAND, 42L,
                    true, true, false, false, 0, 0, AnglersSatchelStorage.capacity(satchel), -1, -1, List.of(), List.of(), items,
                    items.stream().map(item -> com.redslovesgames.tideborne.satchel.network.PersonalRecordView.unavailable()).toList(),
                    java.util.Set.of(), java.util.Set.of(), com.redslovesgames.tideborne.satchel.network.SatchelNetworkStatus.REFRESHED, "", tackle);
            var buffer = new net.minecraft.network.RegistryByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getWorld().getRegistryManager());
            try {
                var codec = com.redslovesgames.tideborne.satchel.network.SatchelViewPayload.STREAM_CODEC;
                codec.encode(buffer, new com.redslovesgames.tideborne.satchel.network.SatchelViewPayload(view));
                var decoded = codec.decode(buffer).view();
                helper.assertTrue(decoded.size() == items.size() && decoded.stateToken() == 42L && buffer.readableBytes() == 0,
                        "View wire fields failed round trip: " + protocol);
                helper.assertTrue(protocol == 2 ? decoded.tackle().isEmpty() : decoded.tackle().equals(tackle), "Versioned tackle projection failed");
            } finally { buffer.release(); }
        }
        helper.complete();
    }







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
    public void legacySatchelContentsMigrateCommitOnceAndRemainStable(TestContext helper) {
        ItemStack legacyFish = new ItemStack(Items.COD);
        legacyFish.set(TideTraitsComponents.MUTATION, "scarred");
        legacyFish.set(TideTraitsComponents.MUTATION_SEED, 0x1234ABCD5678EF90L);
        legacyFish.set(TideTraitsComponents.SIZE_PERCENTILE, 73.25);
        TideItemData.FISH_LENGTH.set(legacyFish, 47.75);
        legacyFish.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Legacy Satchel Cod"));

        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(legacyFish)
                        == CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY,
                "Legacy Satchel fixture was not classified as legacy-only before runtime migration");

        ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
        satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(List.of(legacyFish)));

        ItemStack stored = AnglersSatchelStorage.contents(satchel).getFirst();
        SpecimenData migrated = CanonicalSpecimenStorage.read(stored).orElseThrow();
        assertMigratedLegacyMetadata(helper, migrated, "while stored");
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(stored)
                        == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Reading old Satchel contents did not migrate the specimen");
        assertLegacyCustomName(helper, stored, "after one-time Satchel migration");

        SatchelContents persistedContents = satchel.getOrDefault(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents());
        ItemStack persisted = persistedContents.items().getFirst();
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(persisted)
                        == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Satchel migration was not committed back to persisted SatchelContents");

        SpecimenData repeated = CanonicalSpecimenStorage.read(AnglersSatchelStorage.contents(satchel).getFirst()).orElseThrow();
        helper.assertTrue(migrated.equals(repeated),
                "Repeated Satchel reads reinterpreted or regenerated the migrated specimen");

        AnglersSatchelStorage.ExtractionResult extraction = AnglersSatchelStorage.extractAt(satchel, 0, true);
        helper.assertTrue(extraction.status() == AnglersSatchelStorage.ExtractionStatus.SUCCESS,
                "Migrated Satchel specimen extraction failed");
        ItemStack extracted = extraction.item().orElseThrow();
        helper.assertTrue(migrated.equals(CanonicalSpecimenStorage.read(extracted).orElseThrow()),
                "Extracted Satchel specimen changed after migration");
        assertLegacyCustomName(helper, extracted, "after extraction");
        helper.assertTrue(AnglersSatchelStorage.size(satchel) == 0,
                "Extracted migrated specimen remained duplicated in the Satchel");
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

    private static void assertMigratedLegacyMetadata(TestContext helper, SpecimenData actual, String phase) {
        helper.assertTrue(actual.schemaVersion() == SpecimenGenerator.SCHEMA_VERSION,
                "Migrated schema version was not current " + phase);
        helper.assertTrue(actual.generationVersion() == SpecimenGenerator.GENERATION_VERSION,
                "Migrated generation version was not current " + phase);
        helper.assertTrue(actual.deterministicSeed() == 0x1234ABCD5678EF90L,
                "Legacy deterministic seed was not preserved " + phase);
        helper.assertTrue(Double.compare(actual.basePercentile(), 73.25) == 0,
                "Legacy natural percentile was not preserved " + phase);
        helper.assertTrue(Double.compare(actual.finalLength(), 47.75) == 0,
                "Legacy physical length was not preserved " + phase);
        helper.assertTrue(actual.bodyType() == SpecimenData.BodyType.NORMAL,
                "Legacy specimen without size mutation did not remain Normal " + phase);
        helper.assertTrue(actual.condition() == SpecimenData.Condition.SCARRED,
                "Legacy Scarred mutation did not map to canonical Condition " + phase);
        helper.assertTrue(actual.pigmentation() == SpecimenData.Pigmentation.NORMAL,
                "Legacy Scarred mutation unexpectedly changed Pigmentation " + phase);
        helper.assertTrue(actual.specimenQuality() == SpecimenData.SpecimenQuality.NORMAL,
                "Legacy Scarred mutation unexpectedly changed Quality " + phase);
        helper.assertTrue(actual.rawFishScore().isPresent(),
                "Migrated legacy Satchel fish is missing canonical raw FishScore " + phase);
        helper.assertTrue(actual.fishScore().isPresent(),
                "Migrated legacy Satchel fish is missing canonical FishScore " + phase);
    }

    private static void assertOpaqueStackMetadata(TestContext helper, ItemStack stack, String phase) {
        helper.assertTrue("Canonical Satchel Cod".equals(stack.getName().getString()), "Custom fish name changed " + phase);
        NbtComponent addonData = stack.get(DataComponentTypes.CUSTOM_DATA);
        helper.assertTrue(addonData != null && "silver_spots".equals(addonData.copyNbt().getString("ExampleAddonVariant")),
                "Unrelated custom stack data changed " + phase);
    }

    private static void assertLegacyCustomName(TestContext helper, ItemStack stack, String phase) {
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
