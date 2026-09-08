/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.journal;

import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.journal.BobberBonuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
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
abstract class TideFishingHookMixin {
   @Shadow
   @Final
   @Mutable
   private int luck;
   @Shadow
   @Final
   @Mutable
   private int lureSpeed;

   @Inject(
      method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;IIFLnet/minecraft/world/item/ItemStack;)V",
      at = @At("RETURN")
   )
   private void tideTeamJournal$applyBobberBonus(
      EntityType<? extends TideFishingHook> hookType,
      PlayerEntity player,
      World level,
      int luck,
      int lureSpeed,
      float charge,
      ItemStack rod,
      CallbackInfo callback
   ) {
      BobberBonuses.Bonus bonus = BobberBonuses.get(CustomRodManager.getBobber(rod));
      this.luck = this.luck + bonus.luck();
      this.lureSpeed = this.lureSpeed + bonus.lureSpeed();
   }
}
