/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideborne.journal.TeamProgressStore;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.Registries;
import net.minecraft.client.render.RenderTickCounter;

public final class RecordScoreboard {
   private static Text title;
   private static Text catcher;
   private static Text fish;
   private static Text length;
   private static Text improvement;
   private static ItemStack fishStack = ItemStack.EMPTY;
   private static int accent;
   private static long shownAt;
   private static long hideAt;

   private RecordScoreboard() {
   }

   public static void register() {
      HudRenderCallback.EVENT.register(RecordScoreboard::render);
   }

   public static void show(TeamProgressStore.RecordEvent event) {
      MinecraftClient minecraft = MinecraftClient.getInstance();
      ClientConfig.Values config = ClientConfig.get();
      title = Text.translatable("scoreboard.tideborne.team_journal." + event.type().name().toLowerCase())
         .styled(style -> style.withBold(true).withColor(5477982));
      catcher = Text.translatable(
            "scoreboard.tideborne.team_journal.caught_by",
            new Object[]{Text.literal(event.targetName()).styled(style -> style.withColor(3496824))}
         )
         .styled(style -> style.withColor(6650722));
      fishStack = fishStack(event.fish());
      fish = Text.literal(fishStack.isEmpty() ? event.fish() : fishStack.getName().getString())
         .styled(style -> style.withBold(true).withColor(5477982));
      length = Text.translatable(
            "scoreboard.tideborne.team_journal.length",
            new Object[]{TideUtils.getFormattedLength(event.newSize()).styled(style -> style.withColor(5207921))}
         )
         .styled(style -> style.withColor(6650722));

      improvement = switch (event.type()) {
         case LARGEST -> Text.translatable(
               "scoreboard.tideborne.team_journal.improvement",
               new Object[]{
                  TideUtils.getFormattedLength(Math.abs(event.newSize() - event.previousSize())).styled(style -> style.withColor(6847056)),
                  Text.translatable("scoreboard.tideborne.team_journal.larger").styled(style -> style.withColor(6847056))
               }
            )
            .styled(style -> style.withColor(6650722));
         case SMALLEST -> Text.translatable(
               "scoreboard.tideborne.team_journal.improvement",
               new Object[]{
                  TideUtils.getFormattedLength(Math.abs(event.newSize() - event.previousSize())).styled(style -> style.withColor(6847056)),
                  Text.translatable("scoreboard.tideborne.team_journal.smaller").styled(style -> style.withColor(6847056))
               }
            )
            .styled(style -> style.withColor(6650722));
         case DISCOVERY, REPAIR -> null;
      };

      accent = switch (event.type()) {
         case LARGEST -> TidePalette.calmAccent(config.largestColor);
         case SMALLEST -> TidePalette.calmAccent(config.smallestColor);
         case DISCOVERY -> TidePalette.calmAccent(config.discoveryColor);
         case REPAIR -> 7956829;
      };
      shownAt = System.currentTimeMillis();
      hideAt = shownAt + config.toastDurationSeconds * 1000L;
      Text chatPlayer = Text.literal(event.targetName()).styled(style -> style.withColor(9415088));
      Text chatFish = Text.literal(fish.getString()).styled(style -> style.withColor(14074789));
      Text chatLength = TideUtils.getFormattedLength(event.newSize()).styled(style -> style.withColor(8628900));
      Text chatTitle = Text.literal(title.getString()).styled(style -> style.withBold(true).withColor(14074789));
      Text chat = improvement == null
         ? Text.translatable("chat.tideborne.team_journal.catch", new Object[]{chatPlayer, chatFish, chatLength, chatTitle})
         : Text.translatable(
            "chat.tideborne.team_journal.catch_improved",
            new Object[]{
               chatPlayer,
               chatFish,
               chatLength,
               chatTitle,
               TideUtils.getFormattedLength(Math.abs(event.newSize() - event.previousSize())).styled(style -> style.withColor(10201722)),
               Text.translatable(
                     event.type() == TeamProgressStore.EventType.LARGEST ? "scoreboard.tideborne.team_journal.larger" : "scoreboard.tideborne.team_journal.smaller"
                  )
                  .styled(style -> style.withColor(10201722))
            }
         );
      chat = chat.copy().styled(style -> style.withColor(14074789));
      chat = TeamProgressStore.tideborneEnrichChat(chat, event);
      minecraft.inGameHud.getChatHud().addMessage(chat);
      if (config.toastSound) {
         minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.4F, 0.35F));
      }
   }

   private static ItemStack fishStack(String fishId) {
      Identifier id = Identifier.tryParse(fishId);
      Item item = id == null ? null : (Item)Registries.ITEM.get(id);
      return item == null ? ItemStack.EMPTY : new ItemStack(item);
   }

   private static void render(DrawContext graphics, RenderTickCounter tickCounter) {
      if (title != null && System.currentTimeMillis() < hideAt) {
         MinecraftClient minecraft = MinecraftClient.getInstance();
         long now = System.currentTimeMillis();
         float entrance = Math.min(1.0F, (float)(now - shownAt) / 180.0F);
         float fade = Math.min(1.0F, Math.max(0.0F, (float)(hideAt - now) / 400.0F));
         int alpha = Math.max(4, Math.round(255.0F * fade));
         int padding = 7;
         int iconColumn = fishStack.isEmpty() ? 0 : 23;
         int contentWidth = Math.max(
            minecraft.textRenderer.getWidth(title) + 14,
            Math.max(
               minecraft.textRenderer.getWidth(catcher) + iconColumn,
               Math.max(minecraft.textRenderer.getWidth(fish) + iconColumn, minecraft.textRenderer.getWidth(length) + iconColumn)
            )
         );
         if (improvement != null) {
            contentWidth = Math.max(contentWidth, minecraft.textRenderer.getWidth(improvement) + iconColumn);
         }

         int width = Math.max(178, contentWidth + padding * 2);
         int x = graphics.getScaledWindowWidth() - width - 7 + Math.round((1.0F - entrance) * 18.0F);
         int y = graphics.getScaledWindowHeight() / 3;
         int headerHeight = 19;
         int row = 9 + 2;
         int bodyRows = improvement == null ? 3 : 4;
         int height = headerHeight + padding * 2 + bodyRows * row;
         int textX = x + padding + iconColumn;
         int bodyY = y + headerHeight + padding;
         graphics.fill(x, y, x + width, y + height, argb(alpha * 242 / 255, 15392450));
         graphics.fill(x, y, x + width, y + headerHeight, argb(alpha * 248 / 255, 14796701));
         graphics.fill(x, y, x + width, y + 2, argb(alpha, accent));
         graphics.fill(x, y + 2, x + 2, y + height, argb(alpha * 210 / 255, accent));
         graphics.drawBorder(x, y, width, height, argb(alpha * 220 / 255, 14136724));
         int textColor = argb(alpha, 5477982);
         TideTextRenderer.draw(graphics, minecraft.textRenderer, title, x + padding, y + 6, textColor);
         if (!fishStack.isEmpty()) {
            graphics.drawItem(fishStack, x + padding, bodyY + 4);
         }

         TideTextRenderer.draw(graphics, minecraft.textRenderer, catcher, textX, bodyY, textColor);
         TideTextRenderer.draw(graphics, minecraft.textRenderer, fish, textX, bodyY + row, textColor);
         TideTextRenderer.draw(graphics, minecraft.textRenderer, length, textX, bodyY + row * 2, textColor);
         if (improvement != null) {
            TideTextRenderer.draw(graphics, minecraft.textRenderer, improvement, textX, bodyY + row * 3, textColor);
         }
      } else {
         title = null;
      }
   }

   private static int argb(int alpha, int rgb) {
      return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 16777215;
   }
}
