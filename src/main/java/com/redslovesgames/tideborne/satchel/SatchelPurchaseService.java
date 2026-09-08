/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.registries.TideItems;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SatchelPurchaseService {
   private SatchelPurchaseService() {
   }

   public static SatchelPurchaseResult purchaseHeldFeature(ServerPlayerEntity player, Hand hand, SatchelFeature feature) {
      return purchaseHeldFeature(player, hand, feature, FabricLoader.getInstance().isModLoaded("tideborne"));
   }

   public static SatchelPurchaseResult purchaseHeldFeature(ServerPlayerEntity player, Hand hand, SatchelFeature feature, boolean multiplayerExtrasAvailable) {
      if (player != null && hand != null && feature != null) {
         ItemStack satchel = player.getStackInHand(hand);
         if (!SatchelRegistration.isAnglersSatchel(satchel)) {
            return result(SatchelPurchaseResult.Status.INVALID_SATCHEL, feature.xpCost());
         }

         SatchelState state = AnglersSatchelStorage.state(satchel);
         if (state.isFeatureUnlocked(feature)) {
            return result(SatchelPurchaseResult.Status.ALREADY_OWNED, feature.xpCost());
         }

         if (feature.requiresMultiplayerExtras() && !multiplayerExtrasAvailable) {
            return result(SatchelPurchaseResult.Status.PREREQUISITE_MISSING, feature.xpCost());
         }

         AtomicXpTransaction.Result transaction = AtomicXpTransaction.apply(stateStore(satchel), experienceAccount(player), feature.xpCost(), before -> {
            SatchelState updated = before.withFeatureUnlocked(feature).withFeatureEnabled(feature, true);
            if (feature == SatchelFeature.TROPHY_LOCK) {
               updated = updated.withProtectionDefaults(TideTraitsConfigManager.current().satchel().protectionDefaults());
            }

            return updated;
         });
         SatchelPurchaseResult purchaseResult = map(transaction, feature.xpCost());
         if (purchaseResult.succeeded()) {
            player.getInventory().markDirty();
         }

         return purchaseResult;
      } else {
         return result(SatchelPurchaseResult.Status.INVALID_REQUEST, 0);
      }
   }

   public static SatchelPurchaseResult purchaseHeldCapacityLevel(ServerPlayerEntity player, Hand hand, int targetLevel) {
      if (player != null && hand != null) {
         Optional<SatchelCapacityLevel> target = SatchelCapacityLevel.byLevel(targetLevel);
         if (!target.isEmpty() && targetLevel != SatchelCapacityLevel.BASE.level()) {
            int cost = target.get().xpCost();
            ItemStack satchel = player.getStackInHand(hand);
            if (!SatchelRegistration.isAnglersSatchel(satchel)) {
               return result(SatchelPurchaseResult.Status.INVALID_SATCHEL, cost);
            }

            int currentLevel = AnglersSatchelStorage.state(satchel).capacityLevel();
            if (targetLevel <= currentLevel) {
               return result(SatchelPurchaseResult.Status.ALREADY_OWNED, cost);
            }

            if (targetLevel != currentLevel + 1) {
               return result(SatchelPurchaseResult.Status.PREVIOUS_LEVEL_REQUIRED, cost);
            }

            AtomicXpTransaction.Result transaction = AtomicXpTransaction.apply(
               stateStore(satchel), experienceAccount(player), cost, before -> before.withCapacityLevel(targetLevel)
            );
            SatchelPurchaseResult purchaseResult = map(transaction, cost);
            if (purchaseResult.succeeded()) {
               player.getInventory().markDirty();
            }

            return purchaseResult;
         } else {
            return result(SatchelPurchaseResult.Status.INVALID_REQUEST, 0);
         }
      } else {
         return result(SatchelPurchaseResult.Status.INVALID_REQUEST, 0);
      }
   }

   public static boolean setHeldFeatureEnabled(ServerPlayerEntity player, Hand hand, SatchelFeature feature, boolean enabled) {
      if (player != null && hand != null && feature != null) {
         ItemStack satchel = player.getStackInHand(hand);
         if (!SatchelRegistration.isAnglersSatchel(satchel)) {
            return false;
         }

         SatchelState state = AnglersSatchelStorage.state(satchel);
         if (!state.isFeatureUnlocked(feature)) {
            return false;
         }

         AnglersSatchelStorage.setState(satchel, state.withFeatureEnabled(feature, enabled));
         player.getInventory().markDirty();
         return true;
      } else {
         return false;
      }
   }

   public static Optional<ItemStack> convertedCopy(ItemStack source) {
      if (source != null && !source.isEmpty() && source.getItem() == TideItems.FISH_SATCHEL) {
         SatchelContents beforeContents = (SatchelContents)source.getOrDefault(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents());
         ItemStack converted = source.copyComponentsToNewStack(SatchelRegistration.ANGLERS_SATCHEL, source.getCount());
         SatchelContents afterContents = (SatchelContents)converted.getOrDefault(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents());
         if (!beforeContents.equals(afterContents)) {
            return Optional.empty();
         }

         boolean opened = (Boolean)source.getOrDefault(TideDataComponents.FISH_SATCHEL_OPENED, false);
         AnglersSatchelStorage.setState(converted, SatchelState.empty().withOpen(opened));
         converted.set(TideDataComponents.FISH_SATCHEL_OPENED, opened);
         return Optional.of(converted);
      } else {
         return Optional.empty();
      }
   }

   public static SatchelPurchaseResult convertHeldTideSatchel(ServerPlayerEntity player, Hand hand, int xpCost) {
      if (player != null && hand != null && xpCost >= 0) {
         ItemStack source = player.getStackInHand(hand);
         if (source.getItem() == SatchelRegistration.ANGLERS_SATCHEL) {
            return result(SatchelPurchaseResult.Status.ALREADY_OWNED, xpCost);
         }

         Optional<ItemStack> converted = convertedCopy(source);
         if (converted.isEmpty()) {
            return result(SatchelPurchaseResult.Status.INVALID_SATCHEL, xpCost);
         }

         if (player.totalExperience < xpCost) {
            return result(SatchelPurchaseResult.Status.INSUFFICIENT_XP, xpCost);
         }

         int originalXp = player.totalExperience;
         ItemStack replacement = converted.get();
         player.setStackInHand(hand, replacement);

         try {
            player.addExperience(-xpCost);
            if (player.totalExperience != originalXp - xpCost) {
               throw new IllegalStateException("Minecraft did not deduct the exact XP cost");
            }
         } catch (RuntimeException exception) {
            player.setStackInHand(hand, source);
            restoreExperience(player, originalXp);
            return result(SatchelPurchaseResult.Status.COMMIT_FAILED, xpCost);
         }

         player.getInventory().markDirty();
         return result(SatchelPurchaseResult.Status.SUCCESS, xpCost);
      } else {
         return result(SatchelPurchaseResult.Status.INVALID_REQUEST, Math.max(0, xpCost));
      }
   }

   private static AtomicXpTransaction.StateStore<SatchelState> stateStore(ItemStack satchel) {
      return new AtomicXpTransaction.StateStore<SatchelState>() {
         public SatchelState get() {
            return AnglersSatchelStorage.state(satchel);
         }

         public void set(SatchelState state) {
            AnglersSatchelStorage.setState(satchel, state);
         }
      };
   }

   private static AtomicXpTransaction.ExperienceAccount experienceAccount(ServerPlayerEntity player) {
      return new AtomicXpTransaction.ExperienceAccount() {
         @Override
         public int points() {
            return player.totalExperience;
         }

         @Override
         public boolean deductExactly(int amount) {
            if (amount >= 0 && player.totalExperience >= amount) {
               int before = player.totalExperience;
               player.addExperience(-amount);
               return player.totalExperience == before - amount;
            } else {
               return false;
            }
         }

         @Override
         public void restorePoints(int points) {
            SatchelPurchaseService.restoreExperience(player, points);
         }
      };
   }

   private static void restoreExperience(ServerPlayerEntity player, int points) {
      int difference = points - player.totalExperience;
      if (difference != 0) {
         player.addExperience(difference);
      }
   }

   private static SatchelPurchaseResult map(AtomicXpTransaction.Result transaction, int cost) {
      return switch (transaction) {
         case SUCCESS -> result(SatchelPurchaseResult.Status.SUCCESS, cost);
         case INSUFFICIENT_XP -> result(SatchelPurchaseResult.Status.INSUFFICIENT_XP, cost);
         case NO_CHANGE -> result(SatchelPurchaseResult.Status.ALREADY_OWNED, cost);
         default -> result(SatchelPurchaseResult.Status.COMMIT_FAILED, cost);
      };
   }

   private static SatchelPurchaseResult result(SatchelPurchaseResult.Status status, int cost) {
      return new SatchelPurchaseResult(status, cost);
   }
}
