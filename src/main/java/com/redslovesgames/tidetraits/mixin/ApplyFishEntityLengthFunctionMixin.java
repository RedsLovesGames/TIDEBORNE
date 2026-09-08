/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.loot.ApplyFishEntityLengthFunction;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyFishEntityLengthFunction.class)
public abstract class ApplyFishEntityLengthFunctionMixin {
   @Inject(method = "apply(Lnet/minecraft/item/ItemStack;Lnet/minecraft/loot/context/LootContext;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
   private void tideTraits$copySpecimen(ItemStack input, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
      Entity entity = (Entity)context.get(LootContextParameters.THIS_ENTITY);
      if (entity != null) {
         SpecimenTransfer.entityToStack(entity, (ItemStack)cir.getReturnValue());
      }
   }
}
