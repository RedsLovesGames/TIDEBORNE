/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin;

import com.redslovesgames.tideboundcompatibility.fishing.AnglingTableLeaderSupport;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.li64.tide.client.gui.screens.AnglingTableScreen", remap = false)
public abstract class AnglingTableScreenLeaderMixin {
   @Inject(method = "drawInvalidRecipeArrow", at = @At("TAIL"), remap = false)
   private void tideborne$drawLeaderSlot(DrawContext var1, int var2, int var3, CallbackInfo var4) {
      AnglingTableLeaderSupport.drawLeaderSlot(var1, this);
   }
}
