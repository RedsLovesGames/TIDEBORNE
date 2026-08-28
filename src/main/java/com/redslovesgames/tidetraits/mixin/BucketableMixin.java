/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Bucketable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bucketable.class)
public interface BucketableMixin {
   @Inject(method = "copyDataToStack", at = @At("TAIL"))
   private static void tideTraits$saveSpecimen(MobEntity mob, ItemStack bucket, CallbackInfo ci) {
      SpecimenTransfer.entityToBucket(mob, bucket);
   }
}
