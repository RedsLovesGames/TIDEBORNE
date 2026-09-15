/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public final class TideboundGuideScreen extends Screen {
   public TideboundGuideScreen() {
      super(Text.literal("Tideborne Field Guide"));
   }

   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      graphics.fill(0, 0, this.width, this.height, 0x55000000);
      int width = Math.min(430, this.width - 24);
      int left = (this.width - width) / 2;
      int y = 24;
      graphics.fill(left - 8, y - 8, left + width + 8, this.height - 20, -535288016);
      graphics.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, y, 16777215);
      y += 22;

      for (Text row : rows()) {
         if (y > this.height - 32) {
            break;
         }

         graphics.drawTextWrapped(this.textRenderer, row, left, y, width, row.getString().startsWith("[") ? 8379391 : 15395562);
         y += this.textRenderer.getWrappedLinesHeight(row, width) + 4;
      }

      super.render(graphics, mouseX, mouseY, partialTick);
   }

   public boolean shouldPause() {
      return false;
   }

   private static List<Text> rows() {
      return !ClientTideboundSettings.available()
         ? List.of(Text.literal("Fishing stats are still syncing from the server."))
         : List.of(
            Text.literal(
               "[Fishing Gear] Tentacle Line widens the Catch Zone without changing Fish Movement. Abaia Line trades a smaller Catch Zone for faster movement. Seafarer's Hook favors legendary ocean fish at night without bypassing habitat rules."
            ),
            Text.literal(
               "[Kujira Bone Rod] Holds 3 bait, has 512 durability, and favors ocean crates at " + TideboundTooltips.multiplier("kujira_crates") + " of their normal selection value."
            ),
            Text.translatable(
               "guide.tideborne.fishing.leviathan_bait",
               new Object[]{
                  ClientTideboundSettings.integer("leviathan_fish_luck"),
                  TideboundTooltips.multiplier("leviathan_speed"),
                  TideboundTooltips.multiplier("leviathan_zone")
               }
            ),
            Text.literal(
               "[Shark Risk] A shark can steal a catch before it is reeled in. Steel Leader gives "
                  + TideboundTooltips.percent("steel_prevent")
                  + " Shark Protection. Great Whites can also hunt living or dropped Tide fish."
            ),
            Text.literal(
               "[Chum] Works only in ocean water. Shark scent lasts "
                  + ClientTideboundSettings.integer("chum_duration")
                  + " seconds across "
                  + ClientTideboundSettings.integer("chum_radius")
                  + " blocks. The chum cloud can attract sharks when shark attraction is enabled."
            ),
            Text.literal("[Integrations] Myths and Apex are enabled when the server starts. /tideboundcompat reload refreshes balance values only.")
         );
   }
}
