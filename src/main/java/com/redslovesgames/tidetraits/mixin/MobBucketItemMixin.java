/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.minecraft.entity.Entity;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Bucketable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityBucketItem.class)
public abstract class MobBucketItemMixin {
   @WrapOperation(method = "spawnEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Bucketable;copyDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
   private void tideTraits$loadSpecimen(Bucketable bucketable, NbtCompound tag, Operation<Void> original) {
      original.call(new Object[]{bucketable, tag});
      if (bucketable instanceof Entity entity) {
         SpecimenTransfer.bucketTagToEntity(tag, entity);
      }
   }
}
