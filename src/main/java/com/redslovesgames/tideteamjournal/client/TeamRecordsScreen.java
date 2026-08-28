/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.util.TideUtils;
import com.redslovesgames.tideborne.client.LeaderboardMetricFilter;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import com.redslovesgames.tideteamjournal.network.TeamDataRequestPayload;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.registry.Registries;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;

public final class TeamRecordsScreen extends Screen {
   private static final int PANEL_WIDTH = 400;
   private static final int PANEL_HEIGHT = 260;
   private static final int PAPER_TEXT = 6650722;
   private static final int PAPER_DARK = 5477982;
   private static final int PAPER_LINE = 14136724;
   private static final int TIDE_BLUE = 3496824;
   private static final Identifier JOURNAL_BACKGROUND = Identifier.of("tide", "textures/gui/journal/journal_bg.png");
   private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
   private final Screen parent;
   private TeamRecordsScreen.Tab tab;
   private int page;
   private String metric;
   private String eventType = "all";
   private String fishFilter = "";
   private TextFieldWidget fishFilterBox;

   public TeamRecordsScreen(Screen parent) {
      super(Text.translatable("screen.tide_team_journal.title"));
      this.parent = parent;
      this.tab = TeamRecordsScreen.Tab.from(ClientConfig.get().defaultTab);
      this.metric = ClientConfig.get().defaultMetric;
      this.metric = LeaderboardMetricFilter.normalizeDefault(this.metric);
   }

   public static void open(Screen parent) {
      MinecraftClient.getInstance().setScreen(new TeamRecordsScreen(parent));
   }

   protected void init() {
      int left = (this.width - 400) / 2;
      int top = (this.height - 260) / 2;
      this.addDrawableChild(
         this.journalButton(
            left + 24,
            top + 46,
            82,
            Text.translatable("screen.tide_team_journal.summary"),
            button -> this.switchTab(TeamRecordsScreen.Tab.SUMMARY),
            () -> this.tab == TeamRecordsScreen.Tab.SUMMARY
         )
      );
      this.addDrawableChild(
         this.journalButton(
            left + 114,
            top + 46,
            82,
            Text.translatable("screen.tide_team_journal.leaderboard"),
            button -> this.switchTab(TeamRecordsScreen.Tab.LEADERBOARD),
            () -> this.tab == TeamRecordsScreen.Tab.LEADERBOARD
         )
      );
      this.addDrawableChild(
         this.journalButton(
            left + 204,
            top + 46,
            82,
            Text.translatable("screen.tide_team_journal.history"),
            button -> this.switchTab(TeamRecordsScreen.Tab.HISTORY),
            () -> this.tab == TeamRecordsScreen.Tab.HISTORY
         )
      );
      if (this.tab == TeamRecordsScreen.Tab.LEADERBOARD) {
         this.addDrawableChild(
            this.journalButton(left + 50, top + 72, 126, Text.translatable("metric.tide_team_journal." + this.metric), button -> this.cycleMetric())
         );
         this.addDrawableChild(this.journalButton(left + 184, top + 72, 22, Text.literal("<"), button -> this.changePage(-1)));
         this.addDrawableChild(this.journalButton(left + 212, top + 72, 22, Text.literal(">"), button -> this.changePage(1)));
      }

      if (this.tab == TeamRecordsScreen.Tab.HISTORY) {
         this.fishFilterBox = new TextFieldWidget(this.textRenderer, left + 50, top + 72, 174, 18, Text.translatable("screen.tide_team_journal.fish_filter"));
         this.fishFilterBox.setDrawsBackground(false);
         this.fishFilterBox.setEditableColor(5477982);
         this.fishFilterBox.setPlaceholder(Text.translatable("screen.tide_team_journal.fish_filter"));
         this.fishFilterBox.setText(this.fishFilter);
         this.addDrawableChild(this.fishFilterBox);
         this.addDrawableChild(
            this.journalButton(left + 232, top + 72, 128, Text.translatable("screen.tide_team_journal.apply_filter"), button -> this.applyFishFilter())
         );
         this.addDrawableChild(this.journalButton(left + 50, top + 218, 22, Text.literal("<"), button -> this.changePage(-1)));
         this.addDrawableChild(this.journalButton(left + 78, top + 218, 22, Text.literal(">"), button -> this.changePage(1)));
         this.addDrawableChild(
            this.journalButton(
               left + 108, top + 218, 112, Text.translatable("event.tide_team_journal." + this.eventType), button -> this.cycleEventType()
            )
         );
      }

      this.addDrawableChild(
         this.journalButton(
            left + 292,
            top + 218,
            68,
            Text.translatable("screen.tide_team_journal.settings"),
            button -> this.client.setScreen(ClientConfigScreen.create(this))
         )
      );
      this.addDrawableChild(this.journalButton(left + 374, top + 6, 16, Text.literal("X"), button -> this.close()));
      this.request();
   }

   private TideJournalButton journalButton(int x, int y, int buttonWidth, Text message, PressAction onPress) {
      return new TideJournalButton(x, y, buttonWidth, 18, message, onPress);
   }

   private TideJournalButton journalButton(int x, int y, int buttonWidth, Text message, PressAction onPress, BooleanSupplier selected) {
      return new TideJournalButton(x, y, buttonWidth, 18, message, onPress, selected);
   }

   private void switchTab(TeamRecordsScreen.Tab next) {
      this.tab = next;
      this.page = 0;
      this.clearAndInit();
   }

   private void cycleMetric() {
      NbtList configured = ClientTeamData.get().getList("visible_metrics", 8);
      List<String> metrics = configured.isEmpty()
         ? List.of("catches", "species", "record_events", "active_records", "fish_score")
         : configured.stream().<String>map(NbtElement::asString).toList();
      metrics = LeaderboardMetricFilter.withoutFishScore(metrics);
      int index = metrics.indexOf(this.metric);
      this.metric = metrics.get((index + 1 + metrics.size()) % metrics.size());
      this.clearAndInit();
   }

   private void cycleEventType() {
      this.eventType = switch (this.eventType) {
         case "all" -> "discovery";
         case "discovery" -> "largest";
         case "largest" -> "smallest";
         case "smallest" -> "repair";
         default -> "all";
      };
      this.page = 0;
      this.clearAndInit();
   }

   private void changePage(int delta) {
      NbtCompound data = ClientTeamData.get();
      int pages = this.tab == TeamRecordsScreen.Tab.LEADERBOARD
         ? Math.max(1, (this.visibleContributorCount(data) + 7) / 8)
         : Math.max(1, data.getInt("pages"));
      this.page = Math.max(0, Math.min(pages - 1, this.page + delta));
      this.request();
   }

   private void applyFishFilter() {
      this.fishFilter = this.fishFilterBox == null ? "" : this.fishFilterBox.getText().trim();
      this.page = 0;
      this.request();
   }

   private int visibleContributorCount(NbtCompound data) {
      int count = 0;

      for (NbtElement raw : data.getList("contributors", 10)) {
         if (!((NbtCompound)raw).getBoolean("former") || ClientConfig.get().showFormerMembers) {
            count++;
         }
      }

      return count;
   }

   private void request() {
      if (ClientPlayNetworking.canSend(TeamDataRequestPayload.TYPE)) {
         ClientPlayNetworking.send(
            new TeamDataRequestPayload(
               this.page,
               this.metric,
               this.tab == TeamRecordsScreen.Tab.HISTORY ? this.fishFilter : "",
               this.tab == TeamRecordsScreen.Tab.HISTORY ? this.eventType : "all"
            )
         );
      }
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      int left = (this.width - 400) / 2;
      int top = (this.height - 260) / 2;
      graphics.drawTexture(JOURNAL_BACKGROUND, left, top, 0.0F, 0.0F, 400, 260, 400, 260);
      NbtCompound data = ClientTeamData.get();
      this.tideborne$renderTopFishTab(graphics, left, top, mouseX, mouseY);
      TideTextRenderer.drawCentered(graphics, this.textRenderer, this.title, left + 205, top + 29, 5477982);
      graphics.fill(left + 201, top + 72, left + 202, top + 212, 14136724);
      if (this.tab == TeamRecordsScreen.Tab.HISTORY) {
         graphics.fill(left + 48, top + 70, left + 226, top + 92, 1440860061);
         graphics.drawBorder(left + 48, top + 70, 178, 22, 14136724);
      }

      switch (this.tab) {
         case SUMMARY:
            this.renderSummary(graphics, data, left, top);
            break;
         case LEADERBOARD:
            this.renderLeaderboard(graphics, data, left, top);
            break;
         case HISTORY:
            this.renderHistory(graphics, data, left, top);
      }

      for (Element child : this.children()) {
         if (child instanceof Drawable renderable) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
         }
      }
   }

   private void renderSummary(DrawContext graphics, NbtCompound data, int left, int top) {
      int leftPage = left + 50;
      int rightPage = left + 215;
      int y = top + 79;
      if (!data.contains("tracking_started", 4)) {
         TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.loading"), leftPage, y, 6650722);
      } else {
         this.sectionTitle(graphics, Text.translatable("screen.tide_team_journal.progress"), leftPage, y, 140);
         this.sectionTitle(graphics, Text.translatable("screen.tide_team_journal.recent"), rightPage, y, 145);
         String started = DATE.format(Instant.ofEpochMilli(data.getLong("tracking_started")).atZone(ZoneId.systemDefault()));
         this.drawWrapped(
            graphics,
            Text.translatable("screen.tide_team_journal.completion", new Object[]{data.getInt("discovered"), data.getInt("available")}),
            leftPage,
            y + 22,
            140,
            5477982,
            11,
            2
         );
         this.drawWrapped(
            graphics,
            Text.translatable("screen.tide_team_journal.total_catches", new Object[]{data.getLong("total_catches")}),
            leftPage,
            y + 48,
            140,
            5477982,
            11,
            2
         );
         this.drawWrapped(
            graphics,
            Text.translatable("screen.tide_team_journal.contributors", new Object[]{data.getList("contributors", 10).size()}),
            leftPage,
            y + 74,
            140,
            5477982,
            11,
            2
         );
         this.drawWrapped(
            graphics, Text.translatable("screen.tide_team_journal.tracked_since", new Object[]{started}), leftPage, y + 105, 140, 6650722, 11, 3
         );
         int row = 0;

         for (NbtElement raw : data.getList("history", 10)) {
            TeamProgressStore.RecordEvent event = TeamProgressStore.RecordEvent.fromTag((NbtCompound)raw);
            if (event != null) {
               Text eventName = Text.translatable("event.tide_team_journal." + event.type().name().toLowerCase());
               int eventY = y + 22 + row * 23;
               graphics.fill(rightPage, eventY, rightPage + 2, eventY + 9, this.color(event.type()));
               String line = this.trimToWidth(event.targetName() + " \u2014 " + eventName.getString(), 137);
               TideTextRenderer.draw(graphics, this.textRenderer, line, rightPage + 6, eventY, 5477982);
               Identifier fishId = Identifier.tryParse(event.fish());
               Item fishItem = fishId == null ? null : (Item)Registries.ITEM.get(fishId);
               String fishName = fishItem == null ? event.fish() : new ItemStack(fishItem).getName().getString();
               TideTextRenderer.draw(
                  graphics,
                  this.textRenderer,
                  this.trimToWidth(fishName + " \u00b7 " + TideUtils.getFormattedLength(event.newSize()).getString(), 143),
                  rightPage + 6,
                  eventY + 11,
                  6650722
               );
               if (++row >= 5) {
                  break;
               }
            }
         }

         if (row == 0) {
            TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.no_history"), rightPage, y + 22, 6650722);
         }
      }
   }

   private void renderLeaderboard(DrawContext graphics, NbtCompound data, int left, int top) {
      int x = left + 50;
      int y = top + 96;
      if (!data.getBoolean("leaderboard_enabled")) {
         TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.disabled"), x, y, 6650722);
      } else {
         TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.member_header"), x, y, 6650722);
         TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("metric.tide_team_journal." + this.metric), left + 304, y, 6650722);
         graphics.fill(x, y + 12, left + 360, y + 13, 14136724);
         NbtList list = data.getList("contributors", 10);
         int row = 0;
         int visibleIndex = 0;

         for (NbtElement raw : list) {
            NbtCompound entry = (NbtCompound)raw;
            if ((!entry.getBoolean("former") || ClientConfig.get().showFormerMembers) && visibleIndex++ >= this.page * 8) {
               String name = entry.getString("name")
                  + (entry.getBoolean("former") ? " " + Text.translatable("screen.tide_team_journal.former_suffix").getString() : "");
               int value = entry.getInt(this.metric);
               TideTextRenderer.draw(graphics, this.textRenderer, this.trimToWidth(this.page * 8 + row + 1 + ". " + name, 230), x, y + 17 + row * 13, 5477982);
               TideTextRenderer.draw(graphics, this.textRenderer, Integer.toString(value), left + 340, y + 17 + row * 13, 3496824);
               if (++row >= 8) {
                  break;
               }
            }
         }

         if (row == 0) {
            TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.no_tracking"), x, y + 20, 6650722);
         }

         int pages = Math.max(1, (this.visibleContributorCount(data) + 7) / 8);
         TideTextRenderer.draw(
            graphics,
            this.textRenderer,
            Text.translatable("screen.tide_team_journal.page", new Object[]{this.page + 1, pages}),
            left + 270,
            top + 77,
            6650722
         );
      }
   }

   private void renderHistory(DrawContext graphics, NbtCompound data, int left, int top) {
      int x = left + 50;
      int y = top + 100;
      if (!data.getBoolean("history_enabled")) {
         TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.disabled"), x, y, 6650722);
      } else {
         NbtList list = data.getList("history", 10);
         int row = 0;

         for (NbtElement raw : list) {
            TeamProgressStore.RecordEvent event = TeamProgressStore.RecordEvent.fromTag((NbtCompound)raw);
            if (event != null) {
               Identifier id = Identifier.tryParse(event.fish());
               Item item = id == null ? null : (Item)Registries.ITEM.get(id);
               Text fish = (Text)(item == null ? Text.literal(event.fish()) : new ItemStack(item).getName());
               Text type = Text.translatable("event.tide_team_journal." + event.type().name().toLowerCase());
               String line = event.targetName()
                  + " \u2014 "
                  + type.getString()
                  + " \u2014 "
                  + fish.getString()
                  + " \u00b7 "
                  + TideUtils.getFormattedLength(event.newSize()).getString();
               int rowY = y + row * 14;
               graphics.fill(x, rowY, x + 2, rowY + 9, this.color(event.type()));
               TideTextRenderer.draw(graphics, this.textRenderer, this.trimToWidth(line, 304), x + 6, rowY, 5477982);
               if (++row >= 8) {
                  break;
               }
            }
         }

         if (row == 0) {
            TideTextRenderer.draw(graphics, this.textRenderer, Text.translatable("screen.tide_team_journal.no_history"), x, y, 6650722);
         }

         TideTextRenderer.draw(
            graphics,
            this.textRenderer,
            Text.translatable("screen.tide_team_journal.page", new Object[]{data.getInt("page") + 1, Math.max(1, data.getInt("pages"))}),
            left + 229,
            top + 223,
            6650722
         );
      }
   }

   private void sectionTitle(DrawContext graphics, Text title, int x, int y, int width) {
      TideTextRenderer.draw(graphics, this.textRenderer, title, x, y, 5477982);
      graphics.fill(x, y + 13, x + width, y + 14, 14136724);
   }

   private void drawWrapped(DrawContext graphics, Text text, int x, int y, int width, int color, int lineHeight, int maxLines) {
      int line = 0;

      for (OrderedText part : this.textRenderer.wrapLines(text, width)) {
         if (line >= maxLines) {
            break;
         }

         TideTextRenderer.draw(graphics, this.textRenderer, part, x, y + line * lineHeight, color);
         line++;
      }
   }

   private String trimToWidth(String text, int width) {
      return this.textRenderer.getWidth(text) <= width
         ? text
         : this.textRenderer.trimToWidth(text, Math.max(0, width - this.textRenderer.getWidth("..."))) + "...";
   }

   private int color(TeamProgressStore.EventType type) {
      return switch (type) {
         case DISCOVERY -> 5207921;
         case LARGEST -> 10121284;
         case SMALLEST -> 7757682;
         case REPAIR -> 7956829;
      };
   }

   private void tideborne$renderTopFishTab(DrawContext var1, int var2, int var3, int var4, int var5) {
      var1.fill(var2 + 294, var3 + 46, var2 + 376, var3 + 64, -1980515);
      var1.drawBorder(var2 + 294, var3 + 46, 82, 18, -13280392);
      var1.fill(var2 + 295, var3 + 47, var2 + 375, var3 + 48, 1728053247);
      TideTextRenderer.drawCentered(var1, this.textRenderer, Text.literal("Top Fish"), var2 + 335, var3 + 51, 5477982);
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0
         && var1 >= (this.width - 400) / 2 + 294
         && var1 < (this.width - 400) / 2 + 376
         && var3 >= (this.height - 260) / 2 + 46
         && var3 < (this.height - 260) / 2 + 64) {
         MinecraftClient.getInstance().setScreen(new TopFishScreen(this));
         return true;
      } else {
         return super.mouseClicked(var1, var3, var5);
      }
   }

   private enum Tab {
      SUMMARY,
      LEADERBOARD,
      HISTORY;

      static TeamRecordsScreen.Tab from(String value) {
         try {
            return valueOf(value.toUpperCase());
         } catch (IllegalArgumentException exception) {
            return SUMMARY;
         }
      }
   }
}
