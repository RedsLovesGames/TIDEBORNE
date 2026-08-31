/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.client.gui.journal;

import com.li64.tide.Tide;
import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.li64.tide.data.player.CatchTimestamp;
import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tidetraits.discovery.DiscoveryClient;
import com.redslovesgames.tidetraits.discovery.DiscoverySnapshot;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class DiscoveryBadgesComponent extends ProfileComponent {
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
   private static final int CONTENT_TOP = 24;
   private static final int CONTENT_BOTTOM = 244;
   private static final int REQUIRED_HEIGHT = 34;
   private static final int ICON_SIZE = 10;
   private static final int ICON_STEP = 11;
   private static final int LABEL_GAP = 3;
   private static final int GROUP_GAP = 6;
   private static final int LABEL_COLOR = 0x5A4634;
   private static final int MUTED_COLOR = 0x8C715A;
   private static final float FIRST_CATCH_SCALE = 0.75F;

   private static final List<Badge> BODY_TYPE = List.of(
      mutation("Dwarf", "dwarf"),
      mutation("Giant", "giant")
   );
   private static final List<Badge> CONDITION = List.of(
      mutation("Scarred", "scarred"),
      mutation("Parasite-Ridden", "parasite_ridden")
   );
   private static final List<Badge> PIGMENTATION = List.of(
      mutation("Albino", "albino"),
      mutation("Iridescent", "iridescent")
   );
   private static final List<Badge> QUALITY = List.of(
      mutation("Perfect Specimen", "perfect_specimen")
   );
   private static final List<Badge> SIZE_BANDS = List.of(
      sizeBand("Runty", "runty"),
      sizeBand("Small", "small"),
      sizeBand("Average", "average"),
      sizeBand("Hefty", "hefty"),
      sizeBand("Trophy", "trophy"),
      sizeBand("Legendary", "legendary")
   );

   private final Identifier speciesId;
   private final FishStats stats;

   public DiscoveryBadgesComponent(Identifier speciesId, FishStats stats) {
      this.speciesId = Objects.requireNonNull(speciesId, "speciesId");
      this.stats = stats == null ? new FishStats() : stats;
   }

   public void render(DrawContext graphics, TextRenderer font, int x, int y, int mouseX, int mouseY, float partialTick) {
      int profileTop = (graphics.getScaledWindowHeight() - 260) / 2;
      int clipTop = profileTop + CONTENT_TOP;
      int clipBottom = profileTop + CONTENT_BOTTOM;
      if (y >= clipBottom || y + REQUIRED_HEIGHT <= clipTop) {
         return;
      }

      DiscoverySnapshot snapshot = DiscoveryClient.snapshot();
      HoveredBadge hovered = null;
      int contentX = x + 8;

      try {
         graphics.enableScissor(x, clipTop, x + 174, clipBottom);

         GroupRender body = this.renderGroup(
            graphics, font, "Body Type", BODY_TYPE, BadgeKind.TRAIT, snapshot, contentX, y, mouseX, mouseY, clipTop, clipBottom
         );
         GroupRender condition = this.renderGroup(
            graphics,
            font,
            "Condition",
            CONDITION,
            BadgeKind.TRAIT,
            snapshot,
            body.nextX() + GROUP_GAP,
            y,
            mouseX,
            mouseY,
            clipTop,
            clipBottom
         );
         hovered = prefer(condition.hovered(), body.hovered());

         GroupRender pigment = this.renderGroup(
            graphics,
            font,
            "Pigmentation",
            PIGMENTATION,
            BadgeKind.TRAIT,
            snapshot,
            contentX,
            y + 11,
            mouseX,
            mouseY,
            clipTop,
            clipBottom
         );
         GroupRender quality = this.renderGroup(
            graphics,
            font,
            "Quality",
            QUALITY,
            BadgeKind.TRAIT,
            snapshot,
            pigment.nextX() + GROUP_GAP,
            y + 11,
            mouseX,
            mouseY,
            clipTop,
            clipBottom
         );
         hovered = prefer(quality.hovered(), prefer(pigment.hovered(), hovered));

         int sizeY = y + 22;
         GroupRender size = this.renderGroup(
            graphics,
            font,
            "Size",
            SIZE_BANDS,
            BadgeKind.SIZE,
            snapshot,
            contentX,
            sizeY,
            mouseX,
            mouseY,
            clipTop,
            clipBottom
         );
         hovered = prefer(size.hovered(), hovered);

         String firstCatch = this.firstCatchLabel();
         if (firstCatch != null) {
            drawScaledRight(graphics, font, firstCatch, x + 168, sizeY + 2, FIRST_CATCH_SCALE, MUTED_COLOR);
         }
      } finally {
         graphics.disableScissor();
      }

      if (hovered != null) {
         String state = hovered.discovered() ? "Discovered: " : "Locked: ";
         graphics.drawTooltip(font, Text.literal(hovered.category() + " • " + state + hovered.badge().label()), mouseX, mouseY);
      }
   }

   public int getRequiredHeight() {
      return REQUIRED_HEIGHT;
   }

   private String firstCatchLabel() {
      if (this.stats.getInitialCatchDate().isEmpty()) {
         return null;
      }

      CatchTimestamp timestamp = this.stats.getInitialCatchDate().orElseThrow();
      if (Tide.CLIENT_CONFIG.journal.useRealDate) {
         Instant instant = timestamp.date();
         ZonedDateTime localTime = instant.atZone(ZoneId.systemDefault());
         return "FC " + localTime.format(DATE_FORMAT);
      }
      return "FC Day " + (int)(timestamp.ticks() / 24000L);
   }

   private GroupRender renderGroup(
      DrawContext graphics,
      TextRenderer font,
      String category,
      List<Badge> badges,
      BadgeKind kind,
      DiscoverySnapshot snapshot,
      int x,
      int rowY,
      int mouseX,
      int mouseY,
      int clipTop,
      int clipBottom
   ) {
      graphics.drawText(font, Text.literal(category), x, rowY + 1, LABEL_COLOR, false);
      int iconX = x + font.getWidth(category) + LABEL_GAP;
      HoveredBadge hovered = null;

      for (Badge badge : badges) {
         boolean discovered = kind == BadgeKind.TRAIT
            ? snapshot.hasMutation(this.speciesId, badge.discoveryId())
            : snapshot.hasSizeBand(this.speciesId, badge.discoveryId());
         graphics.drawTexture(discovered ? badge.unlockedTexture() : badge.lockedTexture(), iconX, rowY, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
         if (mouseX >= iconX
            && mouseX < iconX + ICON_SIZE
            && mouseY >= rowY
            && mouseY < rowY + ICON_SIZE
            && mouseY >= clipTop
            && mouseY < clipBottom) {
            hovered = new HoveredBadge(category, badge, discovered);
         }
         iconX += ICON_STEP;
      }

      return new GroupRender(iconX - 1, hovered);
   }

   private static void drawScaledRight(DrawContext graphics, TextRenderer font, String text, int rightEdge, int y, float scale, int color) {
      int width = font.getWidth(text);
      graphics.getMatrices().push();
      graphics.getMatrices().translate(rightEdge - width * scale, y, 0.0F);
      graphics.getMatrices().scale(scale, scale, 1.0F);
      graphics.drawText(font, Text.literal(text), 0, 0, color, false);
      graphics.getMatrices().pop();
   }

   private static HoveredBadge prefer(HoveredBadge preferred, HoveredBadge fallback) {
      return preferred != null ? preferred : fallback;
   }

   private static Badge mutation(String label, String id) {
      return badge(label, id, "mutations");
   }

   private static Badge sizeBand(String label, String id) {
      return badge(label, id, "sizes");
   }

   private static Badge badge(String label, String id, String directory) {
      Identifier discoveryId = Identifier.of("tide_traits", id);
      String base = "textures/gui/journal/" + directory + "/";
      return new Badge(
         label,
         discoveryId,
         Identifier.of("tide_traits", base + "locked/" + id + "_locked.png"),
         Identifier.of("tide_traits", base + "unlocked/" + id + "_unlocked.png")
      );
   }

   @Environment(EnvType.CLIENT)
   private enum BadgeKind {
      TRAIT,
      SIZE
   }

   @Environment(EnvType.CLIENT)
   private record Badge(String label, Identifier discoveryId, Identifier lockedTexture, Identifier unlockedTexture) {
   }

   @Environment(EnvType.CLIENT)
   private record HoveredBadge(String category, Badge badge, boolean discovered) {
   }

   @Environment(EnvType.CLIENT)
   private record GroupRender(int nextX, HoveredBadge hovered) {
   }
}
