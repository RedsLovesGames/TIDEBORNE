/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.specimen.client;

import com.li64.tide.registries.blocks.entities.FishDisplayBlockEntity;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenTransfer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(FishDisplayBlockEntity.class)
public abstract class FishDisplayBlockEntityMixin {
   @Inject(method = "setRenderedEntity", at = @At("HEAD"), require = 0)
   private void tideTraits$copyDisplaySpecimen(Entity renderedEntity, CallbackInfo ci) {
      if (renderedEntity != null) {
         ItemStack displayStack = ((FishDisplayBlockEntity)(Object)this).getDisplayStack();
         if (displayStack != null && !displayStack.isEmpty() && SpecimenTransfer.hasSpecimen(displayStack)) {
            SpecimenTransfer.stackToEntity(displayStack, renderedEntity);
            SpecimenTransfer.markDisplayPreview(renderedEntity);
         }
      }
   }
}
