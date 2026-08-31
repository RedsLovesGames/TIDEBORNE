/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tideborne.client.ui.FishingUiFormat;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout.FittedText;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import com.redslovesgames.tideteamjournal.RecordHolderStore;
import com.redslovesgames.tideteamjournal.client.ClientJournalSpecimens;
import com.redslovesgames.tideteamjournal.client.ClientRecordHolders;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.redslovesgames.tideteamjournal.client.TeamStatsComponent")
public abstract class TeamStatsPercentileMixin {
   @Unique private static final int BASE_LINE_STEP = 9;
   @Unique private static final int BEST_SECTION_HEIGHT = 43;
   @Unique private static final int LABEL_COLOR = 0x5A4634;
   @Unique private static final int MUTED_COLOR = 0x8C715A;
   @Unique private static final int VALUE_COLOR = 0x4FA8D8;
   @Unique private static final int HIGHLIGHT_COLOR = 0xD6A94F;
   @Unique private static final int DIVIDER_COLOR = 0x66725F43;
   @Shadow @Final private List<Text> lines;
   @Unique private FishStats tideTraits$stats;
   @Unique private Identifier tideTraits$speciesId;

   @Inject(method = "<init>(Lcom/li64/tide/data/player/FishStats;Lnet/minecraft/util/Identifier;)V", at = @At("RETURN"), require = 1)
   private void tideTraits$captureStats(FishStats stats, Identifier speciesId, CallbackInfo callback) {
      this.tideTraits$stats = stats;
      this.tideTraits$speciesId = speciesId;
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIIF)V", at = @At("HEAD"), cancellable = true, require = 1)
   private void tideTraits$renderCanonicalJournalData(
      DrawContext graphics, TextRenderer font, int x, int y, int mouseX, int mouseY, float partialTick, CallbackInfo callback
   ) {
      if (this.tideTraits$stats == null || this.lines == null) {
         return;
      }

      int center = x + 87;
      int cursorY = 0;
      int largestIndex = -1;
      int smallestIndex = -1;
      int firstCatchIndex = this.tideTraits$stats.getInitialCatchDate().isPresent() && this.lines.size() > 1 ? 1 : -1;
      Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> best = ClientJournalSpecimens.read(this.tideTraits$speciesId, JournalSpecimenStore.BEST);
      if (this.tideTraits$stats.getLargestCatch() > 0.0 && this.lines.size() >= 2) {
         largestIndex = this.lines.size() - 2;
         smallestIndex = this.lines.size() - 1;
      }
      boolean singleCatch = this.tideTraits$stats.getAmountCaught() == 1 && largestIndex >= 0 && best.isPresent();
      RecordHolderStore.RecordNames names = ClientRecordHolders.get(this.tideTraits$speciesId);
      String hoveredTooltip = null;

      for (int index = 0; index < this.lines.size(); index++) {
         if (index == firstCatchIndex) {
            continue;
         }
         if (singleCatch && (index == largestIndex || index == smallestIndex)) {
            continue;
         }

         if (index == largestIndex && smallestIndex == largestIndex + 1) {
            String tooltip = this.tideTraits$drawRecordColumn(
               graphics,
               font,
               "L",
               "Largest",
               this.tideTraits$stats.getLargestCatch(),
               names.largest(),
               JournalSpecimenStore.LARGEST,
               x + 4,
               y + cursorY,
               mouseX,
               mouseY
            );
            hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
            tooltip = this.tideTraits$drawRecordColumn(
               graphics,
               font,
               "S",
               "Smallest",
               this.tideTraits$stats.getSmallestCatch(),
               names.smallest(),
               JournalSpecimenStore.SMALLEST,
               x + 88,
               y + cursorY,
               mouseX,
               mouseY
            );
            hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
            cursorY += BASE_LINE_STEP;
            index = smallestIndex;
            continue;
         }

         String visible = this.lines.get(index).getString();
         String tooltip = tideTraits$drawCentered(
            graphics, font, visible, visible, center, y + cursorY, 166, MUTED_COLOR, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += BASE_LINE_STEP;
      }

      if (best.isPresent()) {
         JournalSpecimenNetworkCodec.DisplaySpecimen specimen = best.orElseThrow();
         cursorY += 2;
         graphics.fill(x + 4, y + cursorY, x + 170, y + cursorY + 1, DIVIDER_COLOR);
         cursorY += 4;

         String tooltip = tideTraits$drawColumn(
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
         tooltip = tideTraits$drawRight(
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

         String score = FishingUiFormat.fishScore(specimen.fishScore());
         String summary = FishingUiFormat.length(specimen.finalLength())
            + "  •  "
            + FishingUiFormat.percentile(specimen.finalPercentile())
            + "  •  Score "
            + score;
         tooltip = tideTraits$drawCentered(
            graphics, font, summary, summary, center, y + cursorY, 166, VALUE_COLOR, mouseX, mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += 9;

         tooltip = tideTraits$drawColumn(
            graphics,
            font,
            "Body " + FishingUiFormat.trait(specimen.bodyType()),
            "Body Type: " + FishingUiFormat.trait(specimen.bodyType()),
            x + 4,
            y + cursorY,
            80,
            tideTraits$traitColor(specimen.bodyType() != SpecimenData.BodyType.NORMAL),
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         tooltip = tideTraits$drawColumn(
            graphics,
            font,
            "Cond " + FishingUiFormat.trait(specimen.condition()),
            "Condition: " + FishingUiFormat.trait(specimen.condition()),
            x + 88,
            y + cursorY,
            80,
            tideTraits$traitColor(specimen.condition() != SpecimenData.Condition.NORMAL),
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         cursorY += 9;

         tooltip = tideTraits$drawColumn(
            graphics,
            font,
            "Pig " + FishingUiFormat.trait(specimen.pigmentation()),
            "Pigmentation: " + FishingUiFormat.trait(specimen.pigmentation()),
            x + 4,
            y + cursorY,
            80,
            tideTraits$traitColor(specimen.pigmentation() != SpecimenData.Pigmentation.NORMAL),
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
         tooltip = tideTraits$drawColumn(
            graphics,
            font,
            "Qual " + FishingUiFormat.trait(specimen.specimenQuality()),
            "Quality: " + FishingUiFormat.trait(specimen.specimenQuality()),
            x + 88,
            y + cursorY,
            80,
            tideTraits$traitColor(specimen.specimenQuality() != SpecimenData.SpecimenQuality.NORMAL),
            mouseX,
            mouseY
         );
         hoveredTooltip = tooltip != null ? tooltip : hoveredTooltip;
      }

      if (hoveredTooltip != null) {
         graphics.drawTooltip(font, Text.literal(hoveredTooltip), mouseX, mouseY);
      }
      callback.cancel();
   }

   @Inject(method = "getRequiredHeight", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private void tideTraits$canonicalHeight(CallbackInfoReturnable<Integer> callback) {
      if (this.lines == null || this.tideTraits$stats == null) {
         return;
      }

      Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> best = ClientJournalSpecimens.read(this.tideTraits$speciesId, JournalSpecimenStore.BEST);
      int visibleBaseRows = this.lines.size();
      if (this.tideTraits$stats.getInitialCatchDate().isPresent() && visibleBaseRows > 1) {
         visibleBaseRows -= 1;
      }
      boolean hasRecordPair = this.tideTraits$stats.getLargestCatch() > 0.0 && this.lines.size() >= 2;
      if (this.tideTraits$stats.getAmountCaught() == 1 && hasRecordPair && best.isPresent()) {
         visibleBaseRows -= 2;
      } else if (hasRecordPair) {
         visibleBaseRows -= 1;
      }
      callback.setReturnValue(Math.max(0, visibleBaseRows) * BASE_LINE_STEP + (best.isPresent() ? BEST_SECTION_HEIGHT : 0));
   }

   @Unique
   private String tideTraits$drawRecordColumn(
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
      String percentile = ClientJournalSpecimens.read(this.tideTraits$speciesId, recordKind)
         .map(specimen -> FishingUiFormat.percentile(specimen.finalPercentile()))
         .orElse(FishingUiFormat.UNAVAILABLE);
      String visible = shortLabel + " " + FishingUiFormat.length(length) + " " + percentile;
      String full = holder == null || holder.isBlank()
         ? fullLabel + ": " + FishingUiFormat.length(length) + "  •  " + percentile
         : fullLabel + ": " + FishingUiFormat.length(length) + "  •  " + holder + "  •  " + percentile;
      return tideTraits$drawColumn(graphics, font, visible, full, x, y, 80, MUTED_COLOR, mouseX, mouseY);
   }

   @Unique
   private static int tideTraits$traitColor(boolean special) {
      return special ? HIGHLIGHT_COLOR : VALUE_COLOR;
   }

   @Unique
   private static String tideTraits$drawCentered(
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

   @Unique
   private static String tideTraits$drawColumn(
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

   @Unique
   private static String tideTraits$drawRight(
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
