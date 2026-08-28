/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook.CatchType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redslovesgames.tidetraits.TideTraits;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.satchel.AnglersSatchelStorage;
import com.redslovesgames.tidetraits.satchel.SatchelAutomaticProtection;
import com.redslovesgames.tidetraits.satchel.SatchelFeature;
import com.redslovesgames.tidetraits.satchel.SatchelService;
import com.redslovesgames.tidetraits.satchel.SatchelState;
import com.redslovesgames.tidetraits.satchel.SatchelTraitSortData;
import com.redslovesgames.tidetraits.satchel.TideSatchelSortMetadataResolver;
import com.redslovesgames.tidetraits.trait.FishMutation;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TideFishingHook.class)
public abstract class TideFishingHookMixin {
   @Shadow
   protected List<ItemStack> hookedItems;
   @Unique
   private ItemEntity tideTraits$deferredFish;

   @Inject(method = "selectCatch", at = @At("TAIL"))
   private void tideTraits$assignSelectedCatch(ItemStack rod, CallbackInfo ci) {
      TideFishingHook hook = (TideFishingHook)this;
      this.hookedItems = CatchTraitService.INSTANCE.individualizeNewCatches(hook.getHookedItems(), hook.getRandom());
   }

   @Inject(method = "replacePrimaryCatch", at = @At("TAIL"))
   private void tideTraits$assignReplacement(ItemStack stack, CallbackInfo ci) {
      TideFishingHook hook = (TideFishingHook)this;
      this.hookedItems = CatchTraitService.INSTANCE.individualizeNewCatches(hook.getHookedItems(), hook.getRandom());
   }

   @Inject(method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I", at = @At("HEAD"))
   private void tideTraits$beginRetrieve(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> cir) {
      if (this.tideTraits$deferredFish != null) {
         this.tideTraits$spawnDeferred();
      }
   }

   @WrapOperation(
      method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
   )
   private boolean tideTraits$deferEligibleFishEntity(World level, Entity entity, Operation<Boolean> original) {
      TideFishingHook hook = (TideFishingHook)this;
      PlayerEntity owner = hook.getPlayerOwner();
      if (entity instanceof ItemEntity itemEntity
         && owner instanceof ServerPlayerEntity
         && hook.getCatchType() == CatchType.FISH
         && FishData.get(itemEntity.getStack()).isPresent()
         && SatchelService.findAutoStowTarget(owner).isPresent()) {
         if (this.tideTraits$deferredFish != null) {
            this.tideTraits$spawnDeferred();
         }

         this.tideTraits$deferredFish = itemEntity;
         return true;
      } else {
         return (Boolean)original.call(new Object[]{level, entity});
      }
   }

   @WrapOperation(
      method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I",
      at = @At(value = "INVOKE", target = "Lcom/li64/tide/util/TideUtils;tryLogCatch(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/network/ServerPlayerEntity;)Z")
   )
   private boolean tideTraits$stowAfterNativeLog(ItemStack stack, ServerPlayerEntity player, Operation<Boolean> original) {
      boolean nativeCallCompleted = false;

      try {
         boolean logged = (Boolean)original.call(new Object[]{stack, player});
         nativeCallCompleted = true;
         return logged;
      } finally {
         this.tideTraits$finishDeferred(nativeCallCompleted, player);
      }
   }

   @Inject(method = "retrieve(Lnet/minecraft/item/ItemStack;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/player/PlayerEntity;)I", at = @At("RETURN"))
   private void tideTraits$neverStrandDeferred(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> cir) {
      this.tideTraits$spawnDeferred();
   }

   @Unique
   private void tideTraits$finishDeferred(boolean nativeCallCompleted, ServerPlayerEntity player) {
      ItemEntity deferred = this.tideTraits$deferredFish;
      if (deferred != null) {
         this.tideTraits$deferredFish = null;
         if (!nativeCallCompleted) {
            tideTraits$spawn(deferred);
         } else {
            Optional<ItemStack> target = SatchelService.findAutoStowTarget(player);
            if (target.isEmpty()) {
               tideTraits$spawn(deferred);
            } else {
               ItemStack satchel = target.get();
               ItemStack caught = deferred.getStack();

               AnglersSatchelStorage.InsertionResult result;
               try {
                  result = AnglersSatchelStorage.insert(satchel, caught);
               } catch (RuntimeException exception) {
                  tideTraits$spawn(deferred);
                  return;
               }

               if (!result.insertedAny()) {
                  tideTraits$spawn(deferred);
               } else {
                  int firstInsertedSlot = AnglersSatchelStorage.size(satchel) - result.insertedCount();

                  try {
                     SatchelAutomaticProtection.applyInsertedRange(satchel, caught, player, firstInsertedSlot, result.insertedCount());
                     this.tideTraits$sortAfterStow(satchel);
                  } catch (RuntimeException exception) {
                     TideTraits.LOGGER.warn("Auto-Stow committed a catch but could not finish protection/sorting", exception);
                  }

                  player.getInventory().markDirty();
                  if (!result.remainder().isEmpty()) {
                     deferred.setStack(result.remainder());
                     tideTraits$spawn(deferred);
                  }
               }
            }
         }
      }
   }

   @Unique
   private void tideTraits$sortAfterStow(ItemStack satchel) {
      SatchelState state = AnglersSatchelStorage.state(satchel);
      if (state.isFeatureUnlocked(SatchelFeature.TACKLE_ORGANIZER)
         && state.isFeatureEnabled(SatchelFeature.TACKLE_ORGANIZER)
         && !state.sortConfiguration().rules().isEmpty()) {
         AnglersSatchelStorage.sort(satchel, state.sortConfiguration(), new TideSatchelSortMetadataResolver(this::tideTraits$sortTraits));
      }
   }

   @Unique
   private SatchelTraitSortData tideTraits$sortTraits(ItemStack stack) {
      String id = TraitAxesRuntime.condition(stack);
      double probability = FishMutation.bySerializedName(id).map(CatchTraitService.INSTANCE.config()::probability).orElse(1.7976931348623157E308);
      return new SatchelTraitSortData(
         id,
         probability,
         (Double)stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0),
         (Long)stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, 0L)
      );
   }

   @Unique
   private void tideTraits$spawnDeferred() {
      ItemEntity deferred = this.tideTraits$deferredFish;
      this.tideTraits$deferredFish = null;
      if (deferred != null) {
         tideTraits$spawn(deferred);
      }
   }

   @Unique
   private static void tideTraits$spawn(ItemEntity entity) {
      entity.getWorld().spawnEntity(entity);
   }
}
