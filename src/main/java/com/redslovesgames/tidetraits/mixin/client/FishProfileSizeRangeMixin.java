/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.client.gui.screens.journal.FishProfile;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import com.redslovesgames.tidetraits.client.gui.journal.JournalRenderContext;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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

   @Inject(method = "render(Lnet/minecraft/DrawContext;IIF)V", at = @At("HEAD"), require = 1)
   private void tideTraits$beginJournalContext(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      JournalRenderContext.begin(this.data);
   }

   @Inject(method = "render(Lnet/minecraft/DrawContext;IIF)V", at = @At("TAIL"), require = 1)
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
            int var15 = TeamProgressStore.tideborneFishStarsFromData(this.data);
            double var16 = TeamProgressStore.tideborneFishScoreFromParts(0.0, var15, "normal", var11 * 1.0, var13);
            double var18 = TraitAxesRuntime.scoreFromParts(100.0, var15, "perfect_specimen", "giant", var13 * 1.3, var13);
            String var20 = TideUtils.getFormattedLength(var11 * 0.55).getString() + " - " + TideUtils.getFormattedLength(var13 * 1.3).getString();
            this.tideTraits$drawCenteredFit(var1, Text.literal(var20), var8, var9, 160);
            String var21 = "Fish Score " + TeamProgressStore.tideborneFormatScore(var16) + " - " + TeamProgressStore.tideborneFormatScore(var18);
            this.tideTraits$drawCenteredFit(var1, Text.literal(var21), var8, var9 + 11, 160);
         } else {
            this.tideTraits$drawCenteredFit(var1, Text.literal("Size range unavailable"), var8, var9, 160);
         }
      } finally {
         JournalRenderContext.end();
      }
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
