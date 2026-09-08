/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.li64.tide.Tide;
import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.li64.tide.data.player.CatchTimestamp;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideteamjournal.RecordHolderStore;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;

public final class TeamStatsComponent extends ProfileComponent {
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
   private static final int HORIZONTAL_PADDING = 4;
   private final List<Text> lines;

   public TeamStatsComponent(FishStats stats, Identifier fish) {
      Builder<Text> builder = ImmutableList.builder();
      builder.add(Text.translatable("journal.info.stats.total", new Object[]{stats.getAmountCaught()}));
      if (!stats.isEmpty()) {
         if (stats.getInitialCatchDate().isPresent()) {
            CatchTimestamp timestamp = (CatchTimestamp)stats.getInitialCatchDate().orElseThrow();
            Text formatted;
            if (Tide.CLIENT_CONFIG.journal.useRealDate) {
               Instant instant = timestamp.date();
               ZonedDateTime localTime = instant.atZone(ZoneId.systemDefault());
               formatted = Text.literal(localTime.format(DATE_FORMAT));
            } else {
               formatted = Text.translatable("journal.info.stats.day", new Object[]{(int)(timestamp.ticks() / 24000L)});
            }

            builder.add(Text.translatable("journal.info.stats.first", new Object[]{formatted}));
         }

         if (stats.getLargestCatch() > 0.0) {
            RecordHolderStore.RecordNames names = ClientRecordHolders.get(fish);
            builder.add(recordLine("journal.info.stats.largest", "journal.tide_team_journal.largest", stats.getLargestCatch(), names.largest()));
            builder.add(recordLine("journal.info.stats.smallest", "journal.tide_team_journal.smallest", stats.getSmallestCatch(), names.smallest()));
         }
      }

      this.lines = builder.build();
   }

   private static Text recordLine(String tideKey, String teamKey, double length, String holder) {
      Text formattedLength = TideUtils.getFormattedLength(length);
      return holder.isBlank()
         ? Text.translatable(tideKey, new Object[]{formattedLength})
         : Text.translatable(teamKey, new Object[]{formattedLength, holder});
   }

   public void render(@NotNull DrawContext graphics, TextRenderer font, int x, int y, int mouseX, int mouseY, float partialTick) {
      int center = x + 87;
      int cursorY = 0;

      for (Text line : this.lines) {
         int lineWidth = font.getWidth(line);
         float scale = fitScale(lineWidth);
         graphics.getMatrices().push();
         graphics.getMatrices().translate(center, y + cursorY, 0.0F);
         graphics.getMatrices().scale(scale, scale, 1.0F);
         TideTextRenderer.draw(graphics, font, line, -lineWidth / 2, 0, 12620915);
         graphics.getMatrices().pop();
         cursorY += 11;
      }
   }

   static float fitScale(int lineWidth) {
      int availableWidth = 166;
      return lineWidth > availableWidth && lineWidth > 0 ? (float)availableWidth / lineWidth : 1.0F;
   }

   public int getRequiredHeight() {
      return this.lines.size() * 11;
   }
}
