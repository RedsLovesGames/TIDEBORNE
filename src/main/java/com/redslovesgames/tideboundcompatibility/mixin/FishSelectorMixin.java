package com.redslovesgames.tideboundcompatibility.mixin;

import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.data.fishing.selector.FishSelector;
import com.redslovesgames.tideborne.fishing.v2.integration.TideSpeciesSelectionBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes only Tide's within-fish species roll through Fishing System 2.0. */
@Mixin(FishSelector.class)
abstract class FishSelectorMixin {
    @Inject(method = "getResult(Lcom/li64/tide/data/fishing/FishingContext;)Lcom/li64/tide/data/fishing/CatchResult;", at = @At("HEAD"), cancellable = true, remap = false)
    private void tideborne$selectCanonicalSpecies(FishingContext context, CallbackInfoReturnable<CatchResult> callback) {
        callback.setReturnValue(TideSpeciesSelectionBridge.INSTANCE.select(context));
    }
}
