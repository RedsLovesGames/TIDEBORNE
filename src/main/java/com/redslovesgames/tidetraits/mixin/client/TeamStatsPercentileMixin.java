/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import com.redslovesgames.tideteamjournal.client.ClientJournalSpecimens;
import java.util.List;
import java.util.Locale;
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
      if (this.tideTraits$stats.getLargestCatch() > 0.0 && this.lines.size() >= 2) {
         largestIndex = this.lines.size() - 2;
         smallestIndex = this.lines.size() - 1;
      }

      for (int index = 0; index < this.lines.size(); index++) {
         Text line = this.lines.get(index);
         if (index == largestIndex) {
            line = this.tideTraits$withCanonicalPercentile(line, JournalSpecimenStore.LARGEST);
         } else if (index == smallestIndex) {
            line = this.tideTraits$withCanonicalPercentile(line, JournalSpecimenStore.SMALLEST);
         }
         drawCenteredFit(graphics, font, line, center, y + cursorY, 12620915);
         cursorY += 11;
      }

      Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> latest = ClientJournalSpecimens.read(this.tideTraits$speciesId, JournalSpecimenStore.LATEST);
      if (latest.isPresent()) {
         JournalSpecimenNetworkCodec.DisplaySpecimen specimen = latest.get();
         cursorY += 2;
         drawCenteredFit(graphics, font, Text.literal("Body Type: " + label(specimen.bodyType())), center, y + cursorY, 0xB36CE2);
         cursorY += 11;
         drawCenteredFit(graphics, font, Text.literal("Condition: " + label(specimen.condition())), center, y + cursorY, 0xD36B5D);
         cursorY += 11;
         drawCenteredFit(graphics, font, Text.literal("Pigmentation: " + label(specimen.pigmentation())), center, y + cursorY, 0x4FAFD6);
         cursorY += 11;
         drawCenteredFit(graphics, font, Text.literal("Quality: " + label(specimen.specimenQuality())), center, y + cursorY, 0xD6A94F);
         cursorY += 11;
         String score = specimen.fishScore().isPresent() ? Integer.toString(specimen.fishScore().getAsInt()) : "--";
         drawCenteredFit(graphics, font, Text.literal("FishScore: " + score), center, y + cursorY, 0x43A8D8);
      }

      callback.cancel();
   }

   @Inject(method = "getRequiredHeight", at = @At("HEAD"), cancellable = true, require = 1)
   private void tideTraits$canonicalHeight(CallbackInfoReturnable<Integer> callback) {
      if (this.lines != null && ClientJournalSpecimens.read(this.tideTraits$speciesId, JournalSpecimenStore.LATEST).isPresent()) {
         callback.setReturnValue(this.lines.size() * 11 + 57);
      }
   }

   @Unique
   private Text tideTraits$withCanonicalPercentile(Text line, String recordKind) {
      return ClientJournalSpecimens.read(this.tideTraits$speciesId, recordKind)
         .<Text>map(specimen -> Text.literal(line.getString() + String.format(Locale.ROOT, "  •  P%.1f", specimen.finalPercentile())))
         .orElse(line);
   }

   @Unique
   private static void drawCenteredFit(DrawContext graphics, TextRenderer font, Text line, int center, int y, int color) {
      int width = font.getWidth(line);
      int available = 166;
      float scale = width > available && width > 0 ? (float)available / width : 1.0F;
      graphics.getMatrices().push();
      graphics.getMatrices().translate(center, y, 0.0F);
      graphics.getMatrices().scale(scale, scale, 1.0F);
      graphics.drawText(font, line, -width / 2, 0, color, false);
      graphics.getMatrices().pop();
   }

   @Unique
   private static String label(Enum<?> value) {
      StringBuilder output = new StringBuilder();
      for (String part : value.name().toLowerCase(Locale.ROOT).split("_")) {
         if (!output.isEmpty()) {
            output.append(' ');
         }
         output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
      }
      return output.toString();
   }
}
