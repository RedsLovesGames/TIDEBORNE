/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.fishing.FishData;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ClickType;
import net.minecraft.inventory.StackReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Item.class, priority = 1100)
public abstract class TideItemBucketMixin {
   @Inject(method = "onStackClicked", at = @At("HEAD"))
   private void tideTraits$captureSlottedFish(
      ItemStack held,
      Slot slot,
      ClickType action,
      PlayerEntity player,
      CallbackInfoReturnable<Boolean> cir,
      @Share("tideTraits$fish") LocalRef<ItemStack> fish
   ) {
      ItemStack candidate = slot.getStack();
      fish.set(SpecimenTransfer.hasSpecimen(candidate) ? candidate.copy() : ItemStack.EMPTY);
   }

   @Inject(method = "onStackClicked", at = @At("RETURN"))
   private void tideTraits$copySlottedFish(
      ItemStack held,
      Slot slot,
      ClickType action,
      PlayerEntity player,
      CallbackInfoReturnable<Boolean> cir,
      @Share("tideTraits$fish") LocalRef<ItemStack> fish
   ) {
      tideTraits$copyIfTideConverted((Boolean)cir.getReturnValue(), (ItemStack)fish.get(), slot.getStack(), player.currentScreenHandler.getCursorStack(), player);
   }

   @Inject(method = "onClicked", at = @At("HEAD"))
   private void tideTraits$captureCarriedFish(
      ItemStack bucket,
      ItemStack other,
      Slot slot,
      ClickType action,
      PlayerEntity player,
      StackReference access,
      CallbackInfoReturnable<Boolean> cir,
      @Share("tideTraits$fish") LocalRef<ItemStack> fish
   ) {
      fish.set(SpecimenTransfer.hasSpecimen(other) ? other.copy() : ItemStack.EMPTY);
   }

   @Inject(method = "onClicked", at = @At("RETURN"))
   private void tideTraits$copyCarriedFish(
      ItemStack bucket,
      ItemStack other,
      Slot slot,
      ClickType action,
      PlayerEntity player,
      StackReference access,
      CallbackInfoReturnable<Boolean> cir,
      @Share("tideTraits$fish") LocalRef<ItemStack> fish
   ) {
      tideTraits$copyIfTideConverted((Boolean)cir.getReturnValue(), (ItemStack)fish.get(), slot.getStack(), player.currentScreenHandler.getCursorStack(), player);
   }

   private static void tideTraits$copyIfTideConverted(
      boolean handled, ItemStack fish, ItemStack firstCandidate, ItemStack secondCandidate, PlayerEntity player
   ) {
      if (handled && fish != null && !fish.isEmpty()) {
         if (firstCandidate != null && !firstCandidate.isEmpty() && FishData.fromBucket(firstCandidate).isPresent()) {
            SpecimenTransfer.stackToBucket(fish, firstCandidate, player.getRegistryManager());
         } else if (secondCandidate != null && !secondCandidate.isEmpty() && FishData.fromBucket(secondCandidate).isPresent()) {
            SpecimenTransfer.stackToBucket(fish, secondCandidate, player.getRegistryManager());
         }
      }
   }
}
