package com.redslovesgames.tideborne.mixin.specimen;

import com.li64.tide.registries.blocks.entities.FishDisplayBlockEntity;
import com.redslovesgames.tideborne.migration.legacy.LegacyPersistenceMigration;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes Tide fish displays migrate legacy stored fish through the canonical persistence boundary. */
@Mixin(FishDisplayBlockEntity.class)
public abstract class FishDisplayPersistenceMixin {
    @Shadow
    private double lengthCm;

    @Inject(method = "setDisplayStack", at = @At("HEAD"), require = 0)
    private void tideborne$migrateLegacyDisplayStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        LegacyPersistenceMigration.migrateStack(stack);
    }

    @Inject(method = "setDisplayStack", at = @At("RETURN"), require = 0)
    private void tideborne$useCanonicalDisplayLength(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }

        LegacyPersistenceMigration.migrateStack(stack)
                .ifPresent(specimen -> this.lengthCm = specimen.finalLength());
    }
}
