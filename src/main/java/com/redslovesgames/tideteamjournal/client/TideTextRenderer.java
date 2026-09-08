/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public final class TideTextRenderer {
   private TideTextRenderer() {
   }

   public static void draw(DrawContext graphics, TextRenderer font, Text text, int x, int y, int color) {
      graphics.drawText(font, text, x, y, color, false);
   }

   public static void draw(DrawContext graphics, TextRenderer font, String text, int x, int y, int color) {
      graphics.drawText(font, text, x, y, color, false);
   }

   public static void draw(DrawContext graphics, TextRenderer font, OrderedText text, int x, int y, int color) {
      graphics.drawText(font, text, x, y, color, false);
   }

   public static void drawCentered(DrawContext graphics, TextRenderer font, Text text, int centerX, int y, int color) {
      draw(graphics, font, text, centerX - font.getWidth(text) / 2, y, color);
   }
}
