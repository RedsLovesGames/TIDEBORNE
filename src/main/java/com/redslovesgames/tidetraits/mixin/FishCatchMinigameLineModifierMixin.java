package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.minigame.FishCatchMinigame;
import com.redslovesgames.tideborne.fishing.v2.TideFishingLineModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Routes Tide 2.1.1 line-effect constants through the canonical Fishing System 2.0 gear model. */
@Mixin(value = FishCatchMinigame.class, remap = false)
abstract class FishCatchMinigameLineModifierMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 0.90F), remap = false)
    private float tideborne$canonicalCopperTempo(float legacyMultiplier) {
        return (float) TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.COPPER)
                .tempoMultiplier();
    }

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 0.86F), remap = false)
    private float tideborne$canonicalIronStrength(float legacyMultiplier) {
        return (float) TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.IRON)
                .strengthMultiplier();
    }

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 0.95F), remap = false)
    private float tideborne$canonicalGoldenTempo(float legacyMultiplier) {
        return (float) TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.GOLDEN)
                .tempoMultiplier();
    }

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 0.75F), remap = false)
    private float tideborne$canonicalDiamondStrength(float legacyMultiplier) {
        return (float) TideFishingLineModifiers
                .forLegacyLine(TideFishingLineModifiers.LegacyLine.DIAMOND)
                .strengthMultiplier();
    }
}
