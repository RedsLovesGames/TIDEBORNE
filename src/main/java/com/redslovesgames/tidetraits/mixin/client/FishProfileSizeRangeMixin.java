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
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
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
   private void tideTraits$beginJournalContext(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      JournalRenderContext.begin(this.data);
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"), require = 1)
   private void tideTraits$renderSizeRange(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      int var6 = (var1.getScaledWindowWidth() - 400) / 2;
      int var7 = (var1.getScaledWindowHeight() - 260) / 2;
      int var8 = var6 + 290;
      int var9 = var7 + 200;

      try {
         if (this.data != null && this.data.size().isPresent()) {
            SizeData var10 = (SizeData)this.data.size().get();
            double var11 = var10.recordLowCm().orElse(var10.typicalLowCm() * 0.6);
            double var13 = var10.recordHighCm();
            String var20 = FishingUiFormat.length(var11 * 0.55) + " - " + FishingUiFormat.length(var13 * 1.3);
            this.tideTraits$drawCenteredFit(var1, Text.literal(var20), var8, var9, 160);
            String var21 = "FishScore: " + this.tideTraits$recordedFishScore();
            this.tideTraits$drawCenteredFit(var1, Text.literal(var21), var8, var9 + 11, 160);
         } else {
            this.tideTraits$drawCenteredFit(var1, Text.literal("Size range unavailable"), var8, var9, 160);
         }
      } finally {
         JournalRenderContext.end();
      }
   }

   @Unique
   private String tideTraits$recordedFishScore() {
      Identifier speciesId = Registries.ITEM.getId(this.data.fish().value());
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
   private void tideTraits$drawCenteredFit(DrawContext var1, Text var2, int var3, int var4, int var5) {
      int var6 = this.font.getWidth(var2);
      float var7 = var6 > var5 && var6 > 0 ? (float)var5 / var6 : 1.0F;
      var1.getMatrices().push();
      var1.getMatrices().translate(var3, var4, 0.0F);
      var1.getMatrices().scale(var7, var7, 1.0F);
      var1.drawText(this.font, var2, -var6 / 2, 0, 12620915, false);
      var1.getMatrices().pop();
   }
}
