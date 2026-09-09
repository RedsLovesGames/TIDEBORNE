package com.redslovesgames.tideborne.journal.client;

import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout.FittedText;
import com.redslovesgames.tideborne.journal.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.journal.JournalSpecimenStore;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import com.redslovesgames.tideborne.journal.RecordHolderStore;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TeamStatsComponent extends ProfileComponent {
   private static final int BASE_LINE_STEP = 9;
   private static final int BEST_SECTION_HEIGHT = 43;
   private static final int LABEL_COLOR = 0x5A4634;
   private static final int MUTED_COLOR = 0x8C715A;
   private static final int VALUE_COLOR = 0x4FA8D8;
   private static final int HIGHLIGHT_COLOR = 0xD6A94F;
   private static final int DIVIDER_COLOR = 0x66725F43;
   private final FishStats stats;
   private final Identifier speciesId;
   private final Text totalCaught;
   private final boolean hasSizeRecords;

   public TeamStatsComponent(FishStats stats, Identifier speciesId) {
      this.stats = stats;
      this.speciesId = speciesId;
      this.totalCaught = Text.translatable("journal.info.stats.total", stats.getAmountCaught());
      this.hasSizeRecords = !stats.isEmpty() && stats.getLargestCatch() > 0.0;
   }

   // A single catch is already described by Best Specimen; without that payload its size records remain visible.
   private boolean showSizeRecords(boolean hasBest) {
      return hasSizeRecords && (stats.getAmountCaught() != 1 || !hasBest);
   }

   @Override
   public void render(DrawContext graphics, TextRenderer font, int x, int y, int mouseX, int mouseY, float partialTick) {
      Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> best = ClientJournalSpecimens.read(speciesId, JournalSpecimenStore.BEST);
      int center = x + 87;
      int cursorY = BASE_LINE_STEP;
      String total = totalCaught.getString();
      String hoveredTooltip = drawCentered(graphics, font, total, total, center, y, 166, MUTED_COLOR, mouseX, mouseY);

      if (showSizeRecords(best.isPresent())) {
         RecordHolderStore.RecordNames names = ClientRecordHolders.get(speciesId);
         String tooltip = drawRecordColumn(graphics, font, "L", "Largest", stats.getLargestCatch(), names.largest(),
            JournalSpecimenStore.LARGEST, x + 4, y + cursorY, mouseX, mouseY);
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         tooltip = drawRecordColumn(graphics, font, "S", "Smallest", stats.getSmallestCatch(), names.smallest(),
            JournalSpecimenStore.SMALLEST, x + 88, y + cursorY, mouseX, mouseY);
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += BASE_LINE_STEP;
      }

      if (best.isPresent()) {
         JournalSpecimenNetworkCodec.DisplaySpecimen specimen = best.orElseThrow();
         List<TraitDisplay> traits = CanonicalSpecimenPresentation.traits(
            specimen.bodyType(), specimen.condition(), specimen.pigmentation(), specimen.specimenQuality()
         );
         cursorY += 2;
         graphics.fill(x + 4, y + cursorY, x + 170, y + cursorY + 1, DIVIDER_COLOR);
         cursorY += 4;

         String tooltip = drawColumn(
            graphics,
            font,
            "Best Specimen",
            "Best Specimen",
            x + 4,
            y + cursorY,
            100,
            LABEL_COLOR,
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;

         String perfectCatch = "PC " + (specimen.perfectCatch() ? "Yes" : "No");
         tooltip = drawRight(
            graphics,
            font,
            perfectCatch,
            "Perfect Catch: " + (specimen.perfectCatch() ? "Yes" : "No"),
            x + 170,
            y + cursorY,
            62,
            specimen.perfectCatch() ? HIGHLIGHT_COLOR : MUTED_COLOR,
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += 10;

         String score = CanonicalSpecimenPresentation.fishScore(specimen.fishScore());
         String summary = CanonicalSpecimenPresentation.length(specimen.finalLength())
            + "  •  "
            + CanonicalSpecimenPresentation.percentile(specimen.finalPercentile())
            + "  •  Score "
            + score;
         tooltip = drawCentered(
            graphics, font, summary, summary, center, y + cursorY, 166, VALUE_COLOR, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += 9;

         tooltip = drawTrait(
            graphics, font, traits.get(0), x + 4, y + cursorY, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         tooltip = drawTrait(
            graphics, font, traits.get(1), x + 88, y + cursorY, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += 9;

         tooltip = drawTrait(
            graphics, font, traits.get(2), x + 4, y + cursorY, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         tooltip = drawTrait(
            graphics, font, traits.get(3), x + 88, y + cursorY, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
      }

      if (hoveredTooltip != null) {
         graphics.drawTooltip(font, Text.literal(hoveredTooltip), mouseX, mouseY);
      }
   }

   @Override
   public int getRequiredHeight() {
      boolean hasBest = ClientJournalSpecimens.read(speciesId, JournalSpecimenStore.BEST).isPresent();
      return (showSizeRecords(hasBest) ? 2 : 1) * BASE_LINE_STEP + (hasBest ? BEST_SECTION_HEIGHT : 0);
   }

   private String drawRecordColumn(
      DrawContext graphics,
      TextRenderer font,
      String shortLabel,
      String fullLabel,
      double length,
      String holder,
      String recordKind,
      int x,
      int y,
      int mouseX,
      int mouseY
   ) {
      String percentile = ClientJournalSpecimens.read(this.speciesId, recordKind)
         .map(specimen -> CanonicalSpecimenPresentation.percentile(specimen.finalPercentile()))
         .orElse(CanonicalSpecimenPresentation.UNAVAILABLE);
      String formattedLength = CanonicalSpecimenPresentation.length(length);
      String visible = shortLabel + " " + formattedLength + " " + percentile;
      String full = holder == null || holder.isBlank()
         ? fullLabel + ": " + formattedLength + "  •  " + percentile
         : fullLabel + ": " + formattedLength + "  •  " + holder + "  •  " + percentile;
      return drawColumn(graphics, font, visible, full, x, y, 80, MUTED_COLOR, mouseX, mouseY);
   }

   private static String drawTrait(
      DrawContext graphics,
      TextRenderer font,
      TraitDisplay trait,
      int x,
      int y,
      int mouseX,
      int mouseY
   ) {
      return drawColumn(
         graphics,
         font,
         trait.shortLabel() + " " + trait.value(),
         trait.label() + ": " + trait.value(),
         x,
         y,
         80,
         trait.color(),
         mouseX,
         mouseY
      );
   }

   private static String drawCentered(
      DrawContext graphics,
      TextRenderer font,
      String visible,
      String tooltip,
      int center,
      int y,
      int width,
      int color,
      int mouseX,
      int mouseY
   ) {
      FittedText fitted = FishingUiLayout.ellipsize(visible, width, font::getWidth);
      int textWidth = font.getWidth(fitted.text());
      int drawX = center - textWidth / 2;
      graphics.drawText(font, Text.literal(fitted.text()), drawX, y, color, false);
      boolean hovered = mouseX >= center - width / 2 && mouseX < center + width / 2 && mouseY >= y && mouseY < y + 9;
      return hovered && (fitted.clipped() || !tooltip.equals(visible)) ? tooltip : null;
   }

   private static String drawColumn(
      DrawContext graphics,
      TextRenderer font,
      String visible,
      String tooltip,
      int x,
      int y,
      int width,
      int color,
      int mouseX,
      int mouseY
   ) {
      FittedText fitted = FishingUiLayout.ellipsize(visible, width, font::getWidth);
      graphics.drawText(font, Text.literal(fitted.text()), x, y, color, false);
      boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 9;
      return hovered && (fitted.clipped() || !tooltip.equals(visible)) ? tooltip : null;
   }

   private static String drawRight(
      DrawContext graphics,
      TextRenderer font,
      String visible,
      String tooltip,
      int rightEdge,
      int y,
      int width,
      int color,
      int mouseX,
      int mouseY
   ) {
      FittedText fitted = FishingUiLayout.ellipsize(visible, width, font::getWidth);
      int textWidth = font.getWidth(fitted.text());
      int drawX = rightEdge - textWidth;
      graphics.drawText(font, Text.literal(fitted.text()), drawX, y, color, false);
      boolean hovered = mouseX >= rightEdge - width && mouseX < rightEdge && mouseY >= y && mouseY < y + 9;
      return hovered && (fitted.clipped() || !tooltip.equals(visible)) ? tooltip : null;
   }
}
