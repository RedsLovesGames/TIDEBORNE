/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin;

import com.li64.tide.data.TideFishingManager;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook.CatchType;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalCatchStateManager;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.compat.apex.SharkScentManager;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.LeviathanBaitFishing;
import com.redslovesgames.tideboundcompatibility.fishing.LeviathanBaitHook;
import com.redslovesgames.tideboundcompatibility.fishing.SharkCatchLoss;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import com.redslovesgames.tideboundcompatibility.network.SharkCatchLossPayload;
import com.redslovesgames.tidetraits.catching.PerfectCatchTraitBoost;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TideFishingHook.class)
abstract class TideFishingHookMixin implements LeviathanBaitHook {
   @Unique
   private boolean tidebound$leviathanFishSelected;
   @Unique
   private boolean tidebound$leviathanFishOnlyRoll;
   @Unique
   private boolean tidebound$canonicalPerfectCatchHandled;

   @Override
   public boolean tidebound$isLeviathanFishSelected() {
      return this.tidebound$leviathanFishSelected;
   }

   @Override
   public void tidebound$setLeviathanFishSelected(boolean selected) {
      this.tidebound$leviathanFishSelected = selected;
   }

   @Redirect(
      method = "selectCatch",
      at = @At(
         value = "INVOKE",
         target = "Lcom/li64/tide/data/TideFishingManager;selectCatch(Lcom/li64/tide/data/fishing/FishingContext;)Lcom/li64/tide/data/fishing/CatchResult;"
      ),
      remap = false
   )
   private CatchResult tidebound$selectLeviathanFishOnly(TideFishingManager manager, FishingContext context, ItemStack rod) {
      TideFishingHook hook = (TideFishingHook)(Object)this;
      this.tidebound$leviathanFishOnlyRoll = LeviathanBaitFishing.isEnabledFor(hook, TideboundConfig.get());
      return LeviathanBaitFishing.selectCatch(manager, hook, context);
   }

   @Redirect(method = "selectCatch", at = @At(value = "INVOKE", target = "Lcom/li64/tide/data/fishing/CatchResult;isEmpty()Z"), remap = false)
   private boolean tidebound$skipNonFishFallback(CatchResult result) {
      return !this.tidebound$leviathanFishOnlyRoll && result.isEmpty();
   }

   @Inject(method = "invalidateCatch", at = @At("HEAD"), remap = false)
   private void tidebound$clearLeviathanCatchOnFailure(CallbackInfo callback) {
      CanonicalCatchStateManager.clear((TideFishingHook)(Object)this);
      this.tidebound$canonicalPerfectCatchHandled = false;
      this.tidebound$leviathanFishSelected = false;
      this.tidebound$leviathanFishOnlyRoll = false;
   }

   /**
    * Tide owns the center-zone Perfect Catch skill check and passes its server-side result into this
    * method. Capture that result at method entry so V2 post-fight trait finalization and canonical item
    * persistence happen before Tide continues into its delivery/retrieval work.
    */
   @Inject(method = "retrieve(Z)V", at = @At("HEAD"), remap = false)
   private void tidebound$capturePerfectCatchBeforeDelivery(boolean perfectCatch, CallbackInfo callback) {
      this.tidebound$canonicalPerfectCatchHandled = CanonicalCatchStateManager.capturePerfectCatch(
         (TideFishingHook)(Object)this,
         perfectCatch
      );
   }

   @Inject(method = "retrieve(Z)V", at = @At("RETURN"), remap = false)
   private void tidebound$clearLeviathanCatchOnRetrieve(boolean perfectCatch, CallbackInfo callback) {
      // Noncanonical/legacy catches retain the reconstructed 1.x late mutation behavior. Canonical V2
      // catches were already finalized before delivery and must never run this second mutation path.
      if (perfectCatch && !this.tidebound$canonicalPerfectCatchHandled) {
         PerfectCatchTraitBoost.apply(((TideFishingHook)(Object)this).getHookedItems());
      }

      CanonicalCatchStateManager.clear((TideFishingHook)(Object)this);
      this.tidebound$canonicalPerfectCatchHandled = false;
      this.tidebound$leviathanFishSelected = false;
      this.tidebound$leviathanFishOnlyRoll = false;
   }

   @Inject(
      target = @Desc(value = "retrieve", ret = int.class, args = {ItemStack.class, ServerWorld.class, PlayerEntity.class}),
      at = @At("RETURN"),
      remap = false
   )
   private void tidebound$completeTraitMomentum(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> callback) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         CanonicalCatchStateManager.completeTraitMomentum((TideFishingHook)(Object)this, serverPlayer);
      }
   }

   @Inject(method = "selectCatch", at = @At("RETURN"), remap = false)
   private void tidebound$createInitialFishingScent(ItemStack rod, CallbackInfo callback) {
      SharkScentManager.onCatchSelected((TideFishingHook)(Object)this);
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void tidebound$pulseFishingScent(CallbackInfo callback) {
      TideFishingHook hook = (TideFishingHook)(Object)this;
      if (!hook.getWorld().isClient() && hook.age % 40 == 0) {
         SharkScentManager.pulseFishingHook(hook);
      }
   }

   @Inject(target = @Desc(value = "retrieve", ret = int.class, args = {ItemStack.class, ServerWorld.class, PlayerEntity.class}), at = @At("HEAD"), remap = false)
   private void tidebound$rollAbstractSharkCatchLoss(ItemStack rod, ServerWorld level, PlayerEntity player, CallbackInfoReturnable<Integer> callback) {
      TideFishingHook hook = (TideFishingHook)(Object)this;
      TideboundConfig.Values config = TideboundConfig.get();
      if (TideboundCompatibility.isApexIntegrationActive()
         && config.enableSharkCatchLoss
         && player instanceof ServerPlayerEntity serverPlayer
         && hook.getCatchType() == CatchType.FISH
         && hook.hasHookedItem()) {
         double chance = hook.getHookedItems().stream().mapToDouble(SharkCatchLoss::probability).max().orElse(0.0);
         if (!(chance <= 0.0) && !(level.random.nextDouble() >= chance)) {
            FishingGearModifiers gear = SteelLeaderGearModifiers.forHook(hook, config);
            if (FishingGearEffects.preventsCatchLoss(gear, level.random::nextDouble)) {
               serverPlayer.sendMessage(Text.translatable("message.tidebound_compatibility.steel_leader_saved"), true);
               level.playSound(null, hook.getBlockPos(), SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.PLAYERS, 0.7F, 1.4F);
            } else {
               hook.invalidateCatch();
               level.spawnParticles(ParticleTypes.SPLASH, hook.getX(), hook.getY(), hook.getZ(), 16, 0.6, 0.25, 0.6, 0.1);
               level.playSound(null, hook.getBlockPos(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.PLAYERS, 1.1F, 0.75F);
               serverPlayer.sendMessage(Text.translatable("message.tidebound_compatibility.shark_stole_catch"), true);
               if (ServerPlayNetworking.canSend(serverPlayer, SharkCatchLossPayload.TYPE)) {
                  ServerPlayNetworking.send(serverPlayer, new SharkCatchLossPayload());
               }

               if (config.debugLogging) {
                  TideboundCompatibility.LOGGER
                     .debug("Shark catch loss for {} at {}%", serverPlayer.getGameProfile().getName(), Math.round(chance * 10000.0) / 100.0);
               }
            }
         }
      }
   }
}
