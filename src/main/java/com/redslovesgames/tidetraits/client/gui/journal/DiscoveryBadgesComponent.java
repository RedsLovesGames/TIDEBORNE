/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.client.gui.journal;

import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.redslovesgames.tidetraits.discovery.DiscoveryClient;
import com.redslovesgames.tidetraits.discovery.DiscoverySnapshot;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

@Environment(EnvType.CLIENT)
public final class DiscoveryBadgesComponent extends ProfileComponent {
   private static final int PROFILE_HEIGHT = 260;
   private static final int CONTENT_TOP = 24;
   private static final int CONTENT_BOTTOM = 244;
   private static final int REQUIRED_HEIGHT = 14;
   private static final int ICON_SIZE = 10;
   private static final int ICON_GAP = 1;
   private static final int GROUP_GAP = 5;
   private static final List<DiscoveryBadgesComponent.Badge> MUTATIONS = List.of(
      mutation("Albino", "albino"),
      mutation("Scarred", "scarred"),
      mutation("Parasite-Ridden", "parasite_ridden"),
      mutation("Iridescent", "iridescent"),
      mutation("Dwarf", "dwarf"),
      mutation("Giant", "giant"),
      mutation("Perfect Specimen", "perfect_specimen")
   );
   private static final List<DiscoveryBadgesComponent.Badge> SIZE_BANDS = List.of(
      sizeBand("Runty", "runty"),
      sizeBand("Small", "small"),
      sizeBand("Average", "average"),
      sizeBand("Hefty", "hefty"),
      sizeBand("Trophy", "trophy"),
      sizeBand("Legendary", "legendary")
   );
   private final Identifier speciesId;

   public DiscoveryBadgesComponent(Identifier speciesId) {
      this.speciesId = Objects.requireNonNull(speciesId, "speciesId");
   }

   public void render(DrawContext graphics, TextRenderer font, int x, int y, int mouseX, int mouseY, float partialTick) {
      int var8 = (graphics.getScaledWindowHeight() - 260) / 2;
      int var9 = var8 + 24;
      int var10 = var8 + 244;
      if (y < var10 && y + 14 > var9) {
         DiscoverySnapshot var11 = DiscoveryClient.snapshot();
         DiscoveryBadgesComponent.HoveredBadge var12 = null;
         int var13 = x + 14;

         try {
            graphics.enableScissor(x, var9, x + 174, var10);
            var12 = this.renderBadges(graphics, MUTATIONS, true, var11, var13, y, mouseX, mouseY, var9, var10);
            int var14 = var13 + 87;
            DiscoveryBadgesComponent.HoveredBadge var15 = this.renderBadges(graphics, SIZE_BANDS, false, var11, var14, y, mouseX, mouseY, var9, var10);
            if (var15 != null) {
               var12 = var15;
            }
         } finally {
            graphics.disableScissor();
         }

         if (var12 != null) {
            String var20 = var12.discovered() ? "Discovered: " : "Locked: ";
            String var21 = var20 + var12.badge().label();
            graphics.drawTooltip(font, Text.literal(var21), mouseX, mouseY);
         }
      }
   }

   public int getRequiredHeight() {
      return 14;
   }

   private DiscoveryBadgesComponent.HoveredBadge renderBadges(
      DrawContext graphics,
      List<DiscoveryBadgesComponent.Badge> badges,
      boolean mutations,
      DiscoverySnapshot snapshot,
      int x,
      int rowY,
      int mouseX,
      int mouseY,
      int clipTop,
      int clipBottom
   ) {
      DiscoveryBadgesComponent.HoveredBadge hovered = null;

      for (int index = 0; index < badges.size(); index++) {
         DiscoveryBadgesComponent.Badge badge = badges.get(index);
         boolean discovered = mutations ? snapshot.hasMutation(this.speciesId, badge.discoveryId()) : snapshot.hasSizeBand(this.speciesId, badge.discoveryId());
         int iconX = x + index * 11;
         if (mutations && index >= 4) {
            iconX += 5;
         }

         graphics.drawTexture(discovered ? badge.unlockedTexture() : badge.lockedTexture(), iconX, rowY, 0.0F, 0.0F, 10, 10, 10, 10);
         if (mouseX >= iconX && mouseX < iconX + 10 && mouseY >= rowY && mouseY < rowY + 10 && mouseY >= clipTop && mouseY < clipBottom) {
            hovered = new DiscoveryBadgesComponent.HoveredBadge(badge, discovered);
         }
      }

      return hovered;
   }

   private static DiscoveryBadgesComponent.Badge mutation(String label, String id) {
      return badge(label, id, "mutations");
   }

   private static DiscoveryBadgesComponent.Badge sizeBand(String label, String id) {
      return badge(label, id, "sizes");
   }

   private static DiscoveryBadgesComponent.Badge badge(String label, String id, String directory) {
      Identifier discoveryId = Identifier.of("tide_traits", id);
      String base = "textures/gui/journal/" + directory + "/";
      return new DiscoveryBadgesComponent.Badge(
         label,
         discoveryId,
         Identifier.of("tide_traits", base + "locked/" + id + "_locked.png"),
         Identifier.of("tide_traits", base + "unlocked/" + id + "_unlocked.png")
      );
   }

   @Environment(EnvType.CLIENT)
   private record Badge(String label, Identifier discoveryId, Identifier lockedTexture, Identifier unlockedTexture) {
   }

   @Environment(EnvType.CLIENT)
   private record HoveredBadge(DiscoveryBadgesComponent.Badge badge, boolean discovered) {
   }
}
