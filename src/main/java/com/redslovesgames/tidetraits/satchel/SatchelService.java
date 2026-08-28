/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.data.item.TideDataComponents;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public final class SatchelService {
   private SatchelService() {
   }

   public static boolean toggleExclusiveActive(PlayerEntity player, ItemStack target) {
      if (!SatchelRegistration.isAnglersSatchel(target)) {
         return false;
      }

      PlayerInventory inventory = player.getInventory();
      boolean targetFound = false;

      for (int slot = 0; slot < inventory.size(); slot++) {
         if (inventory.getStack(slot) == target) {
            targetFound = true;
            break;
         }
      }

      if (!targetFound) {
         return false;
      }

      boolean activate = !AnglersSatchelStorage.state(target).isActive();

      for (int slot = 0; slot < inventory.size(); slot++) {
         ItemStack candidate = inventory.getStack(slot);
         if (SatchelRegistration.isAnglersSatchel(candidate)) {
            SatchelState state = AnglersSatchelStorage.state(candidate);
            boolean shouldBeActive = activate && candidate == target;
            if (state.isActive() != shouldBeActive) {
               AnglersSatchelStorage.setState(candidate, state.withActive(shouldBeActive));
            }
         }
      }

      inventory.markDirty();
      return activate;
   }

   public static void setOpen(ItemStack satchel, boolean open) {
      if (SatchelRegistration.isAnglersSatchel(satchel)) {
         AnglersSatchelStorage.setState(satchel, AnglersSatchelStorage.state(satchel).withOpen(open));
         satchel.set(TideDataComponents.FISH_SATCHEL_OPENED, open);
      }
   }

   public static boolean toggleOpen(ItemStack satchel) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return false;
      }

      boolean open = !AnglersSatchelStorage.state(satchel).isOpen();
      setOpen(satchel, open);
      return open;
   }

   public static Optional<ItemStack> findActive(PlayerEntity player) {
      PlayerInventory inventory = player.getInventory();

      for (int slot = 0; slot < inventory.size(); slot++) {
         ItemStack candidate = inventory.getStack(slot);
         if (SatchelRegistration.isAnglersSatchel(candidate) && AnglersSatchelStorage.state(candidate).isActive()) {
            return Optional.of(candidate);
         }
      }

      return Optional.empty();
   }

   public static Optional<ItemStack> findEnabledFeatureTarget(PlayerEntity player, SatchelFeature feature) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(feature, "feature");
      PlayerInventory inventory = player.getInventory();
      ItemStack fallback = ItemStack.EMPTY;

      for (int slot = 0; slot < inventory.size(); slot++) {
         ItemStack candidate = inventory.getStack(slot);
         if (SatchelRegistration.isAnglersSatchel(candidate)) {
            SatchelState state = AnglersSatchelStorage.state(candidate);
            if (state.isFeatureUnlocked(feature) && state.isFeatureEnabled(feature)) {
               if (state.isActive()) {
                  return Optional.of(candidate);
               }

               if (fallback.isEmpty()) {
                  fallback = candidate;
               }
            }
         }
      }

      return fallback.isEmpty() ? Optional.empty() : Optional.of(fallback);
   }

   public static Optional<ItemStack> findAutoStowTarget(PlayerEntity player) {
      return findEnabledFeatureTarget(player, SatchelFeature.AUTO_STOW);
   }
}
