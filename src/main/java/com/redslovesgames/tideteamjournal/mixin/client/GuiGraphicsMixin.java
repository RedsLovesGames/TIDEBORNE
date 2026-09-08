/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.mixin.client;

import com.redslovesgames.tideteamjournal.client.ClientConfig;
import com.redslovesgames.tideteamjournal.client.ClientRecordFishMarkers;
import com.redslovesgames.tideteamjournal.client.ClientServerSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class GuiGraphicsMixin {
   @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
   private void tideTeamJournal$renderRecordBadges(TextRenderer font, ItemStack stack, int x, int y, String countText, CallbackInfo callbackInfo) {
      if (ClientConfig.get().showRecordBadges && ClientServerSettings.recordBadgesEnabled) {
         ClientRecordFishMarkers.Status status = ClientRecordFishMarkers.get(stack);
         if (status.isRecord()) {
            DrawContext graphics = (DrawContext)(Object)this;
            graphics.getMatrices().push();
            graphics.getMatrices().translate(0.0F, 0.0F, 300.0F);
            if (status.largest()) {
               Text largest = Text.literal("L").styled(style -> style.withBold(true));
               graphics.drawText(font, largest, x + 2, y + 2, -16777216, false);
               graphics.drawText(font, largest, x + 1, y + 1, -6655932, false);
            }

            if (status.smallest()) {
               Text smallest = Text.literal("S").styled(style -> style.withBold(true));
               graphics.drawText(font, smallest, x + 11, y + 2, -16777216, false);
               graphics.drawText(font, smallest, x + 10, y + 1, -9019534, false);
            }

            graphics.getMatrices().pop();
         }
      }
   }
}
