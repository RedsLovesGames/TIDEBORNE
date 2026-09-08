/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public final class TideboundGuideScreen extends Screen {
   public TideboundGuideScreen() {
      super(Text.literal("Tidebound Field Guide"));
   }

   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
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
         ? List.of(Text.literal("Waiting for server settings. Open this after joining a world."))
         : List.of(
            Text.literal(
               "[Fishing Gear] Tentacle Line widens the catch zone but preserves each fish's native movement. Abaia Line adds a nimble current tradeoff. Seafarer's Hook favors legendary ocean fish only at night; no habitat rules are bypassed."
            ),
            Text.literal(
               "Kujira Bone Rod has 3 bait slots, 512 durability, and " + TideboundTooltips.multiplier("kujira_crates") + " ocean crate weight."
            ),
            Text.translatable(
               "guide.tidebound_compatibility.leviathan_bait",
               new Object[]{
                  ClientTideboundSettings.integer("leviathan_fish_luck"),
                  TideboundTooltips.multiplier("leviathan_speed"),
                  TideboundTooltips.multiplier("leviathan_zone")
               }
            ),
            Text.literal(
               "[Shark Risk] Catches can be lost to a sudden shark bite without a nearby entity. Steel Leader has "
                  + TideboundTooltips.percent("steel_prevent")
                  + " protection. Real Great Whites still hunt living and dropped Tide fish."
            ),
            Text.literal(
               "[Chum] Ocean-only scent lasts "
                  + ClientTideboundSettings.integer("chum_duration")
                  + " seconds over "
                  + ClientTideboundSettings.integer("chum_radius")
                  + " blocks. It creates a dense visible chum cloud and can optionally call sharks under native spawn rules."
            ),
            Text.literal("[Compatibility] Master integrations are chosen at server startup. /tideboundcompat reload updates balance values only.")
         );
   }
}
