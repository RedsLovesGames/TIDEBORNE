/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.journal;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TideFishingHook.class)
/** Required Tide hook constructor adapter: no Fabric cast event exposes native luck/lure fields.
 * Signature/field changes in Tide fail mixin application. The server captures one full bobber contribution.
 */
abstract class TideFishingHookMixin implements com.redslovesgames.tideborne.fishing.gear.BobberGearModifiers.HookSnapshot {
   @org.spongepowered.asm.mixin.Unique
   private com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers tideborne$bobber = com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers.neutral();

   @Override
   public com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers tideborne$bobberModifiers() { return tideborne$bobber; }
   @Shadow
   @Final
   @Mutable
   private int luck;
   @Shadow
   @Final
   @Mutable
   private int lureSpeed;

   @Inject(
      method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;IILnet/minecraft/item/ItemStack;)V",
      at = @At("RETURN")
   )
   private void tideTeamJournal$applyBobberBonus(
      EntityType<? extends TideFishingHook> hookType,
      World level,
      int luck,
      int lureSpeed,
      ItemStack rod,
      CallbackInfo callback
   ) {
      if (level.isClient()) return;
      tideborne$bobber = com.redslovesgames.tideborne.fishing.gear.BobberGearModifiers.forRod(rod);
      this.luck = this.luck + (int) tideborne$bobber.fishingLuck();
      this.lureSpeed = this.lureSpeed + (int) tideborne$bobber.namedAdditiveModifier(com.redslovesgames.tideborne.fishing.gear.FishingGearEffects.LURE_BONUS);
   }
}
