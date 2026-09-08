/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.tide;

import com.li64.tide.client.gui.menus.AnglingTableMenu;
import com.redslovesgames.tideborne.fishing.gear.AnglingTableLeaderSupport;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.li64.tide.client.gui.screens.AnglingTableScreen", remap = false)
public abstract class AnglingTableScreenLeaderMixin extends ForgingScreen<AnglingTableMenu> {
   protected AnglingTableScreenLeaderMixin(AnglingTableMenu handler, PlayerInventory playerInventory, Text title, Identifier texture) {
      super(handler, playerInventory, title, texture);
   }

   @Inject(method = "drawInvalidRecipeArrow", at = @At("TAIL"), remap = true)
   private void tideborne$drawLeaderSlot(DrawContext context, int mouseX, int mouseY, CallbackInfo callbackInfo) {
      AnglingTableLeaderSupport.drawLeaderSlot(context, this.x, this.y);
   }
}
