/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import java.util.function.BooleanSupplier;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;

public final class TideJournalButton extends ButtonWidget {
   private final BooleanSupplier selected;

   public TideJournalButton(int x, int y, int width, int height, Text message, PressAction onPress) {
      this(x, y, width, height, message, onPress, () -> false);
   }

   public TideJournalButton(int x, int y, int width, int height, Text message, PressAction onPress, BooleanSupplier selected) {
      super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
      this.selected = selected;
   }

   protected void renderWidget(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      int fill = this.selected.getAsBoolean() ? -1980515 : (this.isSelected() ? -990266 : -1384766);
      int border = 0xFF000000 | (this.selected.getAsBoolean() ? 3496824 : 14136724);
      int text = 0xFF000000 | (this.active ? 5477982 : 6650722);
      graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), fill);
      graphics.drawBorder(this.getX(), this.getY(), this.getWidth(), this.getHeight(), border);
      graphics.fill(this.getX() + 1, this.getY() + 1, this.getRight() - 1, this.getY() + 2, 1728053247);
      TideTextRenderer.drawCentered(
         graphics,
         MinecraftClient.getInstance().textRenderer,
         this.getMessage(),
         this.getX() + this.getWidth() / 2,
         this.getY() + (this.getHeight() - 8) / 2,
         text
      );
   }
}
