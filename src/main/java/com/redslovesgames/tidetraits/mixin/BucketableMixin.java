/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists Tideborne specimen state when a living fish writes itself into a bucket.
 *
 * <p>The original reconstructed hook targeted {@code Bucketable}, but Mixin 0.8.7
 * does not support injectors on interface mixins. Vanilla and Tide fish use the
 * concrete {@link FishEntity#copyDataToStack(ItemStack)} path, so targeting the
 * concrete fish base class preserves the transfer hook without an interface
 * injector.</p>
 */
@Mixin(FishEntity.class)
public abstract class BucketableMixin {
   @Inject(method = "copyDataToStack", at = @At("TAIL"))
   private void tideTraits$saveSpecimen(ItemStack bucket, CallbackInfo ci) {
      SpecimenTransfer.entityToBucket((FishEntity)(Object)this, bucket);
   }
}
