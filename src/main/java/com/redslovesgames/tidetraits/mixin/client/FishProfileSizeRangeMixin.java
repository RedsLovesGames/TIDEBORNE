/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.client.gui.screens.journal.FishProfile;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.redslovesgames.tideborne.client.ui.FishingUiFormat;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenNetworkCodec;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import com.redslovesgames.tideteamjournal.client.ClientJournalSpecimens;
import com.redslovesgames.tidetraits.client.gui.journal.JournalRenderContext;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishProfile.class)
public abstract class FishProfileSizeRangeMixin {
   @Shadow
   @Final
   private FishData data;
   @Shadow
   @Final
   private TextRenderer font;

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), require = 1)
   private void tideTraits$beginJournalContext(DrawContext graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
      JournalRenderContext.begin(this.data);
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"), require = 1)
   private void tideTraits$renderSizeRange(DrawContext graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
      try {
         if (this.data == null) {
            return;
         }

         Identifier speciesId = Registries.ITEM.getId(this.data.fish().value());
         Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> latest = ClientJournalSpecimens.read(speciesId, JournalSpecimenStore.LATEST);
         if (latest.isPresent()) {
            return;
         }

         int left = (graphics.getScaledWindowWidth() - 400) / 2;
         int top = (graphics.getScaledWindowHeight() - 260) / 2;
         int center = left + 290;
         int y = top + 204;

         if (this.data.size().isPresent()) {
            SizeData size = this.data.size().orElseThrow();
            double low = size.recordLowCm().orElse(size.typicalLowCm() * 0.6);
            double high = size.recordHighCm();
            String range = "Possible size: " + FishingUiFormat.length(low * 0.55) + " - " + FishingUiFormat.length(high * 1.3);
            this.tideTraits$drawCenteredFit(graphics, Text.literal(range), center, y, 160);

            String score = this.tideTraits$recordedFishScore(speciesId);
            String scoreLine = FishingUiFormat.UNAVAILABLE.equals(score)
               ? "No canonical specimen recorded"
               : "Recorded FishScore: " + score;
            this.tideTraits$drawCenteredFit(graphics, Text.literal(scoreLine), center, y + 11, 160);
         } else {
            this.tideTraits$drawCenteredFit(graphics, Text.literal("Size range unavailable"), center, y, 160);
            this.tideTraits$drawCenteredFit(graphics, Text.literal("No canonical specimen recorded"), center, y + 11, 160);
         }
      } finally {
         JournalRenderContext.end();
      }
   }

   @Unique
   private String tideTraits$recordedFishScore(Identifier speciesId) {
      OptionalInt largest = tideTraits$score(speciesId, JournalSpecimenStore.LARGEST);
      OptionalInt smallest = tideTraits$score(speciesId, JournalSpecimenStore.SMALLEST);
      if (largest.isEmpty() && smallest.isEmpty()) {
         return FishingUiFormat.UNAVAILABLE;
      }
      int low = largest.isPresent() && smallest.isPresent()
         ? Math.min(largest.getAsInt(), smallest.getAsInt())
         : (largest.isPresent() ? largest.getAsInt() : smallest.getAsInt());
      int high = largest.isPresent() && smallest.isPresent()
         ? Math.max(largest.getAsInt(), smallest.getAsInt())
         : low;
      return low == high ? FishingUiFormat.fishScore(low) : FishingUiFormat.fishScore(low) + " - " + FishingUiFormat.fishScore(high);
   }

   @Unique
   private static OptionalInt tideTraits$score(Identifier speciesId, String recordKind) {
      Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> specimen = ClientJournalSpecimens.read(speciesId, recordKind);
      return specimen.isPresent() ? specimen.orElseThrow().fishScore() : OptionalInt.empty();
   }

   @Unique
   private void tideTraits$drawCenteredFit(DrawContext graphics, Text text, int center, int y, int availableWidth) {
      int width = this.font.getWidth(text);
      float scale = width > availableWidth && width > 0 ? (float)availableWidth / width : 1.0F;
      graphics.getMatrices().push();
      graphics.getMatrices().translate(center, y, 0.0F);
      graphics.getMatrices().scale(scale, scale, 1.0F);
      graphics.drawText(this.font, text, -width / 2, 0, 12620915, false);
      graphics.getMatrices().pop();
   }
}
