package com.redslovesgames.tideteamjournal.mixin.client;

import com.redslovesgames.tideteamjournal.client.CanonicalRecordDisplay;
import com.redslovesgames.tideteamjournal.client.TeamRecordsScreen;
import com.redslovesgames.tideteamjournal.client.TideTextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds compact canonical FishScore/axis markers without replacing the existing event badge UI. */
@Mixin(value = TeamRecordsScreen.class, remap = false)
abstract class TeamRecordsCanonicalDisplayMixin extends Screen {
   protected TeamRecordsCanonicalDisplayMixin() {
      super(Text.empty());
   }

   @Inject(method = "renderHistory", at = @At("TAIL"))
   private void tideborne$renderCanonicalHistoryMarkers(
      DrawContext graphics, NbtCompound data, int left, int top, CallbackInfo callback
   ) {
      int row = 0;
      for (NbtElement raw : data.getList("history", 10)) {
         if (raw instanceof NbtCompound tag) {
            tideborne$drawCanonicalMarkers(graphics, tag, left + 267, top + 100 + row * 14);
            if (++row >= 8) {
               break;
            }
         }
      }
   }

   @Inject(method = "renderSummary", at = @At("TAIL"))
   private void tideborne$renderCanonicalRecentMarkers(
      DrawContext graphics, NbtCompound data, int left, int top, CallbackInfo callback
   ) {
      int row = 0;
      for (NbtElement raw : data.getList("history", 10)) {
         if (raw instanceof NbtCompound tag) {
            tideborne$drawCanonicalMarkers(graphics, tag, left + 307, top + 101 + row * 23);
            if (++row >= 5) {
               break;
            }
         }
      }
   }

   @Unique
   private void tideborne$drawCanonicalMarkers(DrawContext graphics, NbtCompound tag, int x, int y) {
      CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElse(null);
      if (display == null) {
         return;
      }
      graphics.fill(x - 2, y - 1, x + 88, y + 10, 0xDDF4E7C8);
      String score = display.score().isPresent() ? Integer.toString(display.score().getAsInt()) : "--";
      TideTextRenderer.draw(graphics, this.textRenderer, "#" + score, x, y, 0x43A8D8);
      int cursor = x + 39;
      cursor = tideborne$axis(graphics, "B", display.bodyType(), cursor, y, 0xB36CE2);
      cursor = tideborne$axis(graphics, "C", display.condition(), cursor, y, 0xD36B5D);
      cursor = tideborne$axis(graphics, "P", display.pigmentation(), cursor, y, 0x4FAFD6);
      tideborne$axis(graphics, "Q", display.quality(), cursor, y, 0xD6A94F);
   }

   @Unique
   private int tideborne$axis(DrawContext graphics, String marker, String value, int x, int y, int color) {
      String text = value == null || value.isBlank() ? "·" : marker;
      TideTextRenderer.draw(graphics, this.textRenderer, text, x, y, color);
      return x + 11;
   }
}
