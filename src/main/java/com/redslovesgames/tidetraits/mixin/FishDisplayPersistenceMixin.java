package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.registries.blocks.entities.FishDisplayBlockEntity;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes Tide's cached display size read the canonical Fishing System 2.0 specimen when present. */
@Mixin(FishDisplayBlockEntity.class)
public abstract class FishDisplayPersistenceMixin {
    @Shadow
    private double lengthCm;

    @Inject(method = "setDisplayStack", at = @At("RETURN"), require = 0)
    private void tideborne$useCanonicalDisplayLength(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }

        CanonicalSpecimenStorage.read(stack)
                .ifPresent(specimen -> this.lengthCm = specimen.finalLength());
    }
}
