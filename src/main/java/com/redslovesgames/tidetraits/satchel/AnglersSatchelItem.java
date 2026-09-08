/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.registries.items.FishSatchelItem;
import com.redslovesgames.tidetraits.TideTraits;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item.Settings;

public final class AnglersSatchelItem extends Item {
   private static volatile AnglersSatchelItem.ScreenOpener screenOpener = (player, hand, stack) -> false;

   public AnglersSatchelItem(Settings properties) {
      super(properties);
   }

   public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
      ItemStack stack = player.getStackInHand(hand);
      if (!level.isClient && player instanceof ServerPlayerEntity serverPlayer) {
         if (player.isSneaking()) {
            SatchelService.toggleExclusiveActive(player, stack);
            boolean active = AnglersSatchelStorage.state(stack).isActive();
            serverPlayer.sendMessage(Text.translatable(active ? "message.tide_traits.satchel_active" : "message.tide_traits.satchel_inactive"), true);
         } else if (!screenOpener.open(serverPlayer, hand, stack)) {
            serverPlayer.sendMessage(Text.translatable("message.tide_traits.satchel_screen_unavailable"), true);
         }
      }

      return TypedActionResult.success(stack, level.isClient);
   }

   public boolean canBeNested() {
      return false;
   }

   public boolean onStackClicked(ItemStack satchel, Slot slot, ClickType action, PlayerEntity player) {
      if (action == ClickType.RIGHT && SatchelRegistration.isAnglersSatchel(satchel)) {
         ItemStack source = slot.getStack();
         if (source.isEmpty()) {
            return extractIntoSlot(satchel, slot, player);
         }

         if (!canStore(source)) {
            return false;
         }

         int available = Math.max(0, AnglersSatchelStorage.capacity(satchel) - AnglersSatchelStorage.size(satchel));
         int requested = Math.min(source.getCount(), available);
         if (requested <= 0) {
            return false;
         }

         ItemStack removed = slot.takeStackRange(1, requested, player);
         if (removed.isEmpty()) {
            return false;
         }

         AnglersSatchelStorage.InsertionResult result;
         try {
            result = AnglersSatchelStorage.insert(satchel, removed);
         } catch (RuntimeException exception) {
            restoreToSlotOrPlayer(slot, removed, player);
            TideTraits.LOGGER.warn("Manual satchel insertion failed before commit; restored source stack", exception);
            return false;
         }

         restoreToSlotOrPlayer(slot, result.remainder(), player);
         if (!result.insertedAny()) {
            return false;
         }

         protectInsertedSpecimens(satchel, removed, player, result.insertedCount());
         playInsertSound(player);
         return true;
      } else {
         return false;
      }
   }

   public boolean onClicked(ItemStack satchel, ItemStack carried, Slot slot, ClickType action, PlayerEntity player, StackReference carriedAccess) {
      if (action != ClickType.RIGHT || !SatchelRegistration.isAnglersSatchel(satchel) || !slot.canTakePartial(player)) {
         return false;
      }

      if (carried.isEmpty()) {
         AnglersSatchelStorage.ExtractionResult result = AnglersSatchelStorage.extractFirstUnprotected(satchel);
         ItemStack extracted = result.item().orElse(ItemStack.EMPTY);
         if (extracted.isEmpty()) {
            return false;
         } else if (!carriedAccess.set(extracted)) {
            restoreToSatchelOrPlayer(satchel, extracted, player);
            return false;
         } else {
            playRemoveSound(player);
            return true;
         }
      } else {
         if (!canStore(carried)) {
            return false;
         }

         ItemStack specimen = carried.copyWithCount(1);
         AnglersSatchelStorage.InsertionResult result = AnglersSatchelStorage.moveInto(satchel, carried);
         if (!result.insertedAny()) {
            return false;
         }

         protectInsertedSpecimens(satchel, specimen, player, result.insertedCount());
         playInsertSound(player);
         return true;
      }
   }

   public boolean isItemBarVisible(ItemStack stack) {
      return SatchelRegistration.isAnglersSatchel(stack) && AnglersSatchelStorage.size(stack) > 0;
   }

   public int getItemBarStep(ItemStack stack) {
      int capacity = AnglersSatchelStorage.capacity(stack);
      int size = AnglersSatchelStorage.size(stack);
      return Math.min(13, 1 + 12 * size / Math.max(1, capacity));
   }

   private static boolean extractIntoSlot(ItemStack satchel, Slot slot, PlayerEntity player) {
      List<ItemStack> contents = AnglersSatchelStorage.contents(satchel);
      int sourceSlot = -1;

      for (int index = 0; index < contents.size(); index++) {
         ItemStack candidate = contents.get(index);
         if (!AnglersSatchelStorage.isProtected(satchel, index) && slot.canInsert(candidate) && candidate.getCount() <= slot.getMaxItemCount(candidate)) {
            sourceSlot = index;
            break;
         }
      }

      if (sourceSlot < 0) {
         return false;
      }

      AnglersSatchelStorage.ExtractionResult result = AnglersSatchelStorage.extractAt(satchel, sourceSlot, false);
      ItemStack extracted = result.item().orElse(ItemStack.EMPTY);
      if (extracted.isEmpty()) {
         return false;
      }

      int extractedCount = extracted.getCount();
      ItemStack remainder = slot.insertStack(extracted);
      if (!remainder.isEmpty()) {
         restoreToSatchelOrPlayer(satchel, remainder, player);
      }

      if (remainder.getCount() == extractedCount) {
         return false;
      }

      playRemoveSound(player);
      return true;
   }

   private static boolean canStore(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem().canBeNested() && FishSatchelItem.canPutInSatchel(stack);
   }

   private static void protectInsertedSpecimens(ItemStack satchel, ItemStack specimen, PlayerEntity player, int insertedCount) {
      int firstSlot = AnglersSatchelStorage.size(satchel) - insertedCount;

      try {
         SatchelAutomaticProtection.applyInsertedRange(satchel, specimen, player, firstSlot, insertedCount);
      } catch (RuntimeException exception) {
         TideTraits.LOGGER.warn("Manual satchel insertion committed but automatic Trophy Lock failed", exception);
      }
   }

   private static void restoreToSlotOrPlayer(Slot slot, ItemStack stack, PlayerEntity player) {
      if (!stack.isEmpty()) {
         ItemStack remainder = slot.insertStack(stack);
         if (!remainder.isEmpty() && !player.getWorld().isClient) {
            player.getInventory().offer(remainder, false);
         }
      }
   }

   private static void restoreToSatchelOrPlayer(ItemStack satchel, ItemStack stack, PlayerEntity player) {
      if (!stack.isEmpty()) {
         AnglersSatchelStorage.InsertionResult rollback = AnglersSatchelStorage.insert(satchel, stack);
         if (!rollback.remainder().isEmpty() && !player.getWorld().isClient) {
            player.getInventory().offer(rollback.remainder(), false);
         }
      }
   }

   private static void playInsertSound(PlayerEntity player) {
      player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
   }

   private static void playRemoveSound(PlayerEntity player) {
      player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
   }

   public static void registerScreenOpener(AnglersSatchelItem.ScreenOpener opener) {
      screenOpener = Objects.requireNonNull(opener, "opener");
   }

   @FunctionalInterface
   public interface ScreenOpener {
      boolean open(ServerPlayerEntity var1, Hand var2, ItemStack var3);
   }
}
