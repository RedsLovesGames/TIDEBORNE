/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.gametest;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.registries.TideItems;
import com.li64.tide.registries.items.FishSatchelItem;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import com.redslovesgames.tidetraits.satchel.AnglersSatchelStorage;
import com.redslovesgames.tidetraits.satchel.SatchelAutomaticProtection;
import com.redslovesgames.tidetraits.satchel.SatchelFeature;
import com.redslovesgames.tidetraits.satchel.SatchelProtectionRule;
import com.redslovesgames.tidetraits.satchel.SatchelPurchaseService;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import com.redslovesgames.tidetraits.satchel.SatchelService;
import com.redslovesgames.tidetraits.satchel.SatchelState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.ClickType;
import net.minecraft.inventory.StackReference;
import net.minecraft.util.math.random.Random;
import net.minecraft.test.GameTest;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.DataComponentTypes;

public final class TideTraitsGameTests implements FabricGameTest {
   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void specimenSurvivesStackEntityBucketEntityStack(TestContext helper) {
      ItemStack original = specimenStack();
      original.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Addon-Named Scarred Cod"));
      NbtCompound addonData = new NbtCompound();
      addonData.putString("ExampleAddonVariant", "blue_spots");
      original.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(addonData));
      original.set(TideTraitsComponents.PROTECTED, true);
      CodEntity first = (CodEntity)helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 1));
      SpecimenTransfer.stackToEntity(original, first);
      NbtCompound savedEntity = first.writeNbt(new NbtCompound());
      CodEntity reloaded = (CodEntity)helper.spawnEntity(EntityType.COD, new BlockPos(1, 2, 2));
      reloaded.readNbt(savedEntity);
      ItemStack bucket = new ItemStack(Items.COD_BUCKET);
      SpecimenTransfer.entityToBucket(reloaded, bucket);
      NbtComponent bucketData = (NbtComponent)bucket.get(DataComponentTypes.BUCKET_ENTITY_DATA);
      helper.assertTrue(bucketData != null, "Specimen bucket entity data was not written");
      CodEntity second = (CodEntity)helper.spawnEntity(EntityType.COD, new BlockPos(2, 2, 1));
      NbtCompound bucketTag = bucketData.copyNbt();
      SpecimenTransfer.bucketTagToEntity(bucketTag, second);
      ItemStack restored = new ItemStack(Items.COD);
      SpecimenTransfer.entityToStack(second, restored);
      SpecimenData canonical = CanonicalSpecimenStorage.read(restored).orElseThrow();
      helper.assertTrue(canonical.condition() == SpecimenData.Condition.SCARRED,
         "Legacy Scarred mutation did not survive as canonical Condition");
      helper.assertTrue(
         canonical.deterministicSeed() == 81985529216486895L,
         "Mutation seed did not survive the representation round trip"
      );
      helper.assertTrue(
         Double.compare(canonical.basePercentile(), 91.4) == 0,
         "Natural percentile did not survive canonical migration and the representation round trip"
      );
      helper.assertTrue(
         Math.abs(canonical.finalLength() - 171.2) < 1.0E-9, "Canonical final length did not survive the representation round trip"
      );
      helper.assertTrue(
         Math.abs((Double)TideItemData.FISH_LENGTH.getOrDefault(restored, 0.0) - 171.2) < 1.0E-9, "Tide length did not survive the representation round trip"
      );
      helper.assertTrue("Addon-Named Scarred Cod".equals(restored.getName().getString()), "Custom name did not survive the representation round trip");
      NbtComponent restoredAddonData = (NbtComponent)restored.get(DataComponentTypes.CUSTOM_DATA);
      helper.assertTrue(
         restoredAddonData != null && "blue_spots".equals(restoredAddonData.copyNbt().getString("ExampleAddonVariant")),
         "An addon-style custom data component did not survive the representation round trip"
      );
      helper.assertTrue(
         Boolean.TRUE.equals(restored.get(TideTraitsComponents.PROTECTED)), "Protection component did not survive the representation round trip"
      );
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void multiCountCatchSplitsWithoutLegacyTraitGeneration(TestContext helper) {
      ItemStack threeFish = new ItemStack(Items.COD, 3);
      Random observed = Random.create(1592639710L);
      Random control = Random.create(1592639710L);
      List<ItemStack> individualized = CatchTraitService.INSTANCE.individualizeNewCatches(List.of(threeFish), observed);
      helper.assertTrue(individualized.size() == 3, "Three caught fish were not represented by three compatibility entries");
      helper.assertTrue(individualized.stream().allMatch(stack -> stack.getCount() == 1), "An individualized catch retained a multi-item count");
      helper.assertTrue(individualized.stream().allMatch(stack -> stack.get(TideTraitsComponents.MUTATION) == null),
         "Compatibility splitting generated a superseded mutually exclusive mutation");
      helper.assertTrue(individualized.stream().allMatch(stack -> stack.get(TideTraitsComponents.MUTATION_SEED) == null),
         "Compatibility splitting generated superseded legacy identity seeds");
      helper.assertTrue(individualized.stream().allMatch(stack -> stack.get(TideTraitsComponents.SIZE_PERCENTILE) == null),
         "Compatibility splitting generated superseded legacy percentiles");
      helper.assertTrue(observed.nextLong() == control.nextLong(),
         "Compatibility splitting consumed RNG after legacy trait generation was removed");
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void populatedTideSatchelConvertsWithoutLosingSpecimenComponents(TestContext helper) {
      ItemStack source = new ItemStack(TideItems.FISH_SATCHEL);
      ItemStack specimen = specimenStack();
      source.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(List.of(specimen)));
      source.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Old Reliable"));
      ItemStack converted = SatchelPurchaseService.convertedCopy(source).orElse(ItemStack.EMPTY);
      helper.assertTrue(converted.isOf(SatchelRegistration.ANGLERS_SATCHEL), "Tide satchel did not convert to the single Angler's Satchel item");
      helper.assertTrue(converted.getName().getString().equals("Old Reliable"), "Custom satchel name was not preserved");
      helper.assertTrue(AnglersSatchelStorage.size(converted) == 1, "Converted satchel contents count changed");
      ItemStack nested = AnglersSatchelStorage.contents(converted).getFirst();
      helper.assertTrue("scarred".equals(nested.get(TideTraitsComponents.MUTATION)), "Nested specimen mutation was not preserved");
      helper.assertTrue(
         Long.valueOf(81985529216486895L).equals(nested.get(TideTraitsComponents.MUTATION_SEED)), "Nested specimen seed was not preserved"
      );
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void manualInventoryClicksPreserveCapacityRemaindersAndSpecimenData(TestContext helper) {
      PlayerEntity player = helper.createMockCreativeServerPlayerInWorld();
      ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      AnglersSatchelStorage.setState(
         satchel,
         AnglersSatchelStorage.state(satchel)
            .withFeatureUnlocked(SatchelFeature.TROPHY_LOCK)
            .withFeatureEnabled(SatchelFeature.TROPHY_LOCK, true)
            .withProtectionRule(SatchelProtectionRule.MUTATED.id(), true)
      );
      int capacity = AnglersSatchelStorage.capacity(satchel);
      List<ItemStack> existing = new ArrayList<>(capacity - 1);

      for (int index = 0; index < capacity - 1; index++) {
         existing.add(specimenStack());
      }

      satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(existing));
      ItemStack source = specimenStack().copyWithCount(3);
      source.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Manual Deposit"));
      helper.assertTrue(FishSatchelItem.canPutInSatchel(source), "GameTest fish must be recognized by Tide");
      SimpleInventory fishContainer = new SimpleInventory(new ItemStack[]{source});
      Slot fishSlot = new Slot(fishContainer, 0, 0, 0);
      boolean primaryHandled = satchel.getItem().onStackClicked(satchel, fishSlot, ClickType.LEFT, player);
      helper.assertTrue(
         !primaryHandled && fishSlot.getStack().getCount() == 3 && AnglersSatchelStorage.size(satchel) == capacity - 1,
         "Primary click was hijacked from normal inventory behavior"
      );
      boolean handled = satchel.getItem().onStackClicked(satchel, fishSlot, ClickType.RIGHT, player);
      helper.assertTrue(handled, "Satchel-on-fish click was not handled");
      helper.assertTrue(fishSlot.getStack().getCount() == 2, "Partial insertion did not leave the exact source remainder");
      helper.assertTrue(AnglersSatchelStorage.size(satchel) == capacity, "Manual insertion did not use exactly the available capacity");
      ItemStack nested = AnglersSatchelStorage.contents(satchel).getLast();
      helper.assertTrue(nested.getCount() == 1, "Manual insertion did not store the specimen individually");
      SpecimenData nestedCanonical = CanonicalSpecimenStorage.read(nested).orElseThrow();
      helper.assertTrue(nestedCanonical.condition() == SpecimenData.Condition.SCARRED,
         "Manual insertion lost the migrated canonical Condition");
      helper.assertTrue(nestedCanonical.deterministicSeed() == 81985529216486895L,
         "Manual insertion lost the migrated specimen identity seed");
      helper.assertTrue("Manual Deposit".equals(nested.getName().getString()),
         "Manual insertion lost unrelated ItemStack metadata");
      helper.assertTrue(AnglersSatchelStorage.isProtected(satchel, capacity - 1), "Manual insertion did not apply the enabled mutated-fish Trophy Lock rule");
      ItemStack secondSatchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      SimpleInventory satchelContainer = new SimpleInventory(new ItemStack[]{secondSatchel});
      Slot satchelSlot = new Slot(satchelContainer, 0, 0, 0);
      ItemStack carried = specimenStack().copyWithCount(3);
      handled = secondSatchel.getItem().onClicked(secondSatchel, carried, satchelSlot, ClickType.RIGHT, player, StackReference.EMPTY);
      helper.assertTrue(handled, "Fish-on-satchel cursor click was not handled");
      helper.assertTrue(carried.isEmpty(), "Cursor insertion did not consume the committed fish");
      helper.assertTrue(AnglersSatchelStorage.size(secondSatchel) == 3, "Cursor insertion did not preserve every individual specimen");
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void manualInventoryClicksNeverExtractProtectedEntries(TestContext helper) {
      PlayerEntity player = helper.createMockCreativeServerPlayerInWorld();
      ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      ItemStack protectedFish = specimenStack();
      protectedFish.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Protected Specimen"));
      ItemStack unprotectedFish = specimenStack();
      unprotectedFish.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Unprotected Specimen"));
      satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(List.of(protectedFish, unprotectedFish)));
      AnglersSatchelStorage.setState(satchel, AnglersSatchelStorage.state(satchel).withFeatureUnlocked(SatchelFeature.TROPHY_LOCK));
      helper.assertTrue(AnglersSatchelStorage.setProtected(satchel, 0, true), "Test setup could not protect the first specimen");
      SimpleInventory satchelContainer = new SimpleInventory(new ItemStack[]{satchel});
      Slot satchelSlot = new Slot(satchelContainer, 0, 0, 0);
      AtomicReference<ItemStack> carried = new AtomicReference<>(ItemStack.EMPTY);
      StackReference carriedAccess = StackReference.of(carried::get, carried::set);
      boolean handled = satchel.getItem().onClicked(satchel, carried.get(), satchelSlot, ClickType.RIGHT, player, carriedAccess);
      helper.assertTrue(handled, "Empty-cursor extraction did not find an unprotected specimen");
      helper.assertTrue(
         "Unprotected Specimen".equals(carried.get().getName().getString()), "Empty-cursor extraction removed the protected specimen"
      );
      helper.assertTrue(
         AnglersSatchelStorage.size(satchel) == 1 && AnglersSatchelStorage.isProtected(satchel, 0), "Protected specimen or its protection marker was changed"
      );
      carried.set(ItemStack.EMPTY);
      handled = satchel.getItem().onClicked(satchel, carried.get(), satchelSlot, ClickType.RIGHT, player, carriedAccess);
      helper.assertTrue(!handled && carried.get().isEmpty(), "Cursor extraction bypassed protection on the last specimen");
      SimpleInventory emptyContainer = new SimpleInventory(1);
      Slot emptySlot = new Slot(emptyContainer, 0, 0, 0);
      handled = satchel.getItem().onStackClicked(satchel, emptySlot, ClickType.RIGHT, player);
      helper.assertTrue(!handled && emptySlot.getStack().isEmpty(), "Satchel-on-empty-slot extraction bypassed protection");
      helper.assertTrue(
         AnglersSatchelStorage.size(satchel) == 1 && AnglersSatchelStorage.isProtected(satchel, 0),
         "Protected specimen changed after blocked manual extraction"
      );
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void enabledFeatureTargetPrefersActiveAndIgnoresLockedOrDisabledSatchels(TestContext helper) {
      PlayerEntity player = helper.createMockCreativeServerPlayerInWorld();
      ItemStack fallback = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      ItemStack lockedActive = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      ItemStack active = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      AnglersSatchelStorage.setState(
         fallback,
         AnglersSatchelStorage.state(fallback).withFeatureUnlocked(SatchelFeature.SHARED_LEDGER).withFeatureEnabled(SatchelFeature.SHARED_LEDGER, true)
      );
      AnglersSatchelStorage.setState(
         lockedActive, AnglersSatchelStorage.state(lockedActive).withFeatureEnabled(SatchelFeature.SHARED_LEDGER, true).withActive(true)
      );
      AnglersSatchelStorage.setState(
         active,
         AnglersSatchelStorage.state(active)
            .withFeatureUnlocked(SatchelFeature.SHARED_LEDGER)
            .withFeatureEnabled(SatchelFeature.SHARED_LEDGER, true)
            .withActive(true)
      );
      player.getInventory().setStack(0, fallback);
      player.getInventory().setStack(1, lockedActive);
      player.getInventory().setStack(2, active);
      helper.assertTrue(
         SatchelService.findEnabledFeatureTarget(player, SatchelFeature.SHARED_LEDGER).filter(found -> found == active).isPresent(),
         "Later active eligible satchel did not win over deterministic fallback"
      );
      AnglersSatchelStorage.setState(active, AnglersSatchelStorage.state(active).withActive(false));
      helper.assertTrue(
         SatchelService.findEnabledFeatureTarget(player, SatchelFeature.SHARED_LEDGER).filter(found -> found == fallback).isPresent(),
         "First enabled satchel was not used as deterministic fallback"
      );
      AnglersSatchelStorage.setState(fallback, AnglersSatchelStorage.state(fallback).withFeatureEnabled(SatchelFeature.SHARED_LEDGER, false));
      AnglersSatchelStorage.setState(active, AnglersSatchelStorage.state(active).withFeatureEnabled(SatchelFeature.SHARED_LEDGER, false));
      helper.assertTrue(
         SatchelService.findEnabledFeatureTarget(player, SatchelFeature.SHARED_LEDGER).isEmpty(),
         "Locked or disabled satchel incorrectly enabled Shared Ledger"
      );
      helper.complete();
   }

   @GameTest(templateName = "fabric-gametest-api-v1:empty")
   public void personalTideRecordsStayNativeWithOrWithoutMultiplayerExtras(TestContext helper) {
      PlayerEntity mock = helper.createMockCreativeServerPlayerInWorld();
      helper.assertTrue(mock instanceof ServerPlayerEntity, "GameTest did not provide a server player");
      ServerPlayerEntity player = (ServerPlayerEntity)mock;
      ItemStack largest = specimenStack();
      largest.set(TideTraitsComponents.MUTATION, "normal");
      largest.set(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
      TideItemData.FISH_LENGTH.set(largest, 1000000.0);
      int before = PersonalTideJournal.statsFor(player, largest).<Integer>map(FishStats::getAmountCaught).orElse(0);
      TidePlayerData routed = TidePlayerData.getOrCreate(player);
      if (FabricLoader.getInstance().isModLoaded("tide_team_journal")) {
         helper.assertTrue(PersonalTideJournal.extrasTrackerActive(), "Multiplayer Extras loaded without activating the team/native route tracker");
         helper.assertTrue(PersonalTideJournal.isExtrasTeamData(routed), "Resolved Multiplayer Extras journal was not identified as team-backed");
      }

      routed.logCatch(largest, player, player.getWorld());
      routed.syncTo(player);
      ItemStack smallest = largest.copy();
      TideItemData.FISH_LENGTH.set(smallest, 0.001);
      routed = TidePlayerData.getOrCreate(player);
      routed.logCatch(smallest, player, player.getWorld());
      routed.syncTo(player);
      FishStats personal = PersonalTideJournal.statsFor(player, largest).orElse(null);
      helper.assertTrue(personal != null, "Native personal Tide stats were not persisted");
      helper.assertTrue(personal.getAmountCaught() == before + 2, "Personal catches were skipped or double-counted through the Extras route");
      helper.assertTrue(Math.abs(personal.getLargestCatch() - 1000000.0) < 1.0E-9, "Personal largest record was replaced by effective-team authority");
      helper.assertTrue(Math.abs(personal.getSmallestCatch() - 0.001) < 1.0E-12, "Personal smallest record was replaced by effective-team authority");
      ItemStack satchel = new ItemStack(SatchelRegistration.ANGLERS_SATCHEL);
      TideTraitsGameTests.SatchelStateBuilder.configurePersonalRecordRules(satchel);
      satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(List.of(largest, smallest)));
      helper.assertTrue(
         SatchelAutomaticProtection.applyInsertedRange(satchel, largest, player, 0, 1), "Personal-largest Trophy rule did not use native Tide records"
      );
      helper.assertTrue(
         SatchelAutomaticProtection.applyInsertedRange(satchel, smallest, player, 1, 1), "Personal-smallest Trophy rule did not use native Tide records"
      );
      helper.assertTrue(
         AnglersSatchelStorage.isProtected(satchel, 0) && AnglersSatchelStorage.isProtected(satchel, 1), "Personal record specimens were not protected"
      );
      helper.complete();
   }

   private static ItemStack specimenStack() {
      ItemStack stack = new ItemStack(Items.COD);
      stack.set(TideTraitsComponents.MUTATION, "scarred");
      stack.set(TideTraitsComponents.MUTATION_SEED, 81985529216486895L);
      stack.set(TideTraitsComponents.SIZE_PERCENTILE, 91.4);
      TideItemData.FISH_LENGTH.set(stack, 171.2);
      return stack;
   }

   private static final class SatchelStateBuilder {
      private static void configurePersonalRecordRules(ItemStack satchel) {
         SatchelState state = AnglersSatchelStorage.state(satchel)
            .withFeatureUnlocked(SatchelFeature.TROPHY_LOCK)
            .withFeatureEnabled(SatchelFeature.TROPHY_LOCK, true);

         for (SatchelProtectionRule rule : SatchelProtectionRule.values()) {
            state = state.withProtectionRule(rule.id(), false);
         }

         state = state.withProtectionRule(SatchelProtectionRule.PERSONAL_LARGEST.id(), true)
            .withProtectionRule(SatchelProtectionRule.PERSONAL_SMALLEST.id(), true);
         AnglersSatchelStorage.setState(satchel, state);
      }
   }
}
