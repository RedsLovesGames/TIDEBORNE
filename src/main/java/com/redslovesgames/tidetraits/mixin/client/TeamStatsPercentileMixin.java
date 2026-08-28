/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.data.player.FishStats;
import com.redslovesgames.tidetraits.client.gui.journal.JournalRenderContext;
import com.redslovesgames.tidetraits.fish.FishPercentileService;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.redslovesgames.tideteamjournal.client.TeamStatsComponent")
public abstract class TeamStatsPercentileMixin {
   @Shadow
   @Final
   private List<Text> lines;
   @Unique
   private FishStats tideTraits$stats;
   @Unique
   private static final FishPercentileService tideTraits$percentiles = new FishPercentileService();

   @Inject(method = "<init>(Lcom/li64/tide/data/player/FishStats;Lnet/minecraft/util/Identifier;)V", at = @At("RETURN"), require = 1)
   private void tideTraits$captureStats(FishStats var1, Identifier var2, CallbackInfo var3) {
      this.tideTraits$stats = var1;
   }

   @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIIF)V", at = @At("HEAD"), cancellable = true, require = 1)
   private void tideTraits$renderFitPercentiles(DrawContext var1, TextRenderer var2, int var3, int var4, int var5, int var6, float var7, CallbackInfo var8) {
      if (this.tideTraits$stats != null && this.lines != null) {
         int var9 = var3 + 87;
         byte var10 = 0;
         int var11 = -1;
         int var12 = -1;
         if (this.tideTraits$stats.getLargestCatch() > 0.0 && this.lines.size() >= 2) {
            var11 = this.lines.size() - 2;
            var12 = this.lines.size() - 1;
         }

         for (int var13 = 0; var13 < this.lines.size(); var13++) {
            Text var14 = this.lines.get(var13);
            if (var13 == var11) {
               var14 = this.tideTraits$withPercentile(var14, this.tideTraits$stats.getLargestCatch());
            } else if (var13 == var12) {
               var14 = this.tideTraits$withPercentile(var14, this.tideTraits$stats.getSmallestCatch());
            }

            int var15 = var2.getWidth(var14);
            short var16 = 166;
            float var17 = var15 > var16 && var15 > 0 ? (float)var16 / var15 : 1.0F;
            var1.getMatrices().push();
            var1.getMatrices().translate(var9, var4 + var10, 0.0F);
            var1.getMatrices().scale(var17, var17, 1.0F);
            var1.drawText(var2, var14, -var15 / 2, 0, 12620915, false);
            var1.getMatrices().pop();
            var10 += 11;
         }

         var8.cancel();
      }
   }

   @Unique
   private Text tideTraits$withPercentile(Text var1, double var2) {
      JournalRenderContext.Entry var4 = JournalRenderContext.current();
      if (var4 != null && var4.data() != null) {
         FishData var5 = var4.data();
         if (var5.size().isEmpty()) {
            return var1;
         }

         SizeData var6 = (SizeData)var5.size().get();
         OptionalDouble var7 = tideTraits$percentiles.percentile(var4.speciesId(), var6, var2);
         if (var7.isEmpty()) {
            return var1;
         }

         String var8 = String.format(Locale.ROOT, "  \u2022  P%.1f", var7.getAsDouble());
         return Text.literal(var1.getString() + var8);
      } else {
         return var1;
      }
   }
}
