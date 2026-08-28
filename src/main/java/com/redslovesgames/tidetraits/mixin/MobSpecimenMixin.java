/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.redslovesgames.tidetraits.entity.SpecimenEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Builder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobSpecimenMixin implements SpecimenEntity {
   @Unique
   private static final TrackedData<NbtCompound> TIDE_TRAITS$SPECIMEN = DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

   @Inject(method = "initDataTracker", at = @At("TAIL"))
   private void tideTraits$defineSpecimenData(Builder builder, CallbackInfo ci) {
      builder.add(TIDE_TRAITS$SPECIMEN, new NbtCompound());
   }

   @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
   private void tideTraits$saveSpecimen(NbtCompound entityTag, CallbackInfo ci) {
      NbtCompound specimen = this.tideTraits$getSpecimenTag();
      if (!specimen.isEmpty()) {
         specimen.remove("DisplayPreview");
         entityTag.put("TideTraits", specimen);
      }
   }

   @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
   private void tideTraits$loadSpecimen(NbtCompound entityTag, CallbackInfo ci) {
      if (entityTag.contains("TideTraits")) {
         this.tideTraits$setSpecimenTag(entityTag.getCompound("TideTraits"));
      }
   }

   @Override
   public NbtCompound tideTraits$getSpecimenTag() {
      MobEntity self = (MobEntity)this;
      return ((NbtCompound)self.getDataTracker().get(TIDE_TRAITS$SPECIMEN)).copy();
   }

   @Override
   public void tideTraits$setSpecimenTag(NbtCompound tag) {
      MobEntity self = (MobEntity)this;
      self.getDataTracker().set(TIDE_TRAITS$SPECIMEN, tag == null ? new NbtCompound() : tag.copy());
   }
}
