package com.redslovesgames.tidetraits.mixin;

import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Binary-compatibility guard for reconstructed pre-V2 FishScore entry points.
 *
 * <p>The methods remain present because reconstructed callers link against them, but their old
 * arithmetic is never allowed to execute in production. All migrated consumers enter through the
 * canonical specimen FishScore boundary instead.
 */
@Mixin(value = TraitAxesRuntime.class, remap = false)
abstract class LegacyFishScoreCalculatorMixin {
    @Inject(method = "score", at = @At("HEAD"), cancellable = true)
    private static void tideborne$disableLegacyItemScore(
            ItemStack stack,
            int rarityStars,
            double percentile,
            double length,
            double recordHigh,
            CallbackInfoReturnable<Double> callback
    ) {
        callback.setReturnValue(-1.0D);
    }

    @Inject(method = "scoreFromParts", at = @At("HEAD"), cancellable = true)
    private static void tideborne$disableLegacyPartsScore(
            double percentile,
            int rarityStars,
            String condition,
            String bodyType,
            double length,
            double recordHigh,
            CallbackInfoReturnable<Double> callback
    ) {
        callback.setReturnValue(-1.0D);
    }
}
