/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel.client;

import com.redslovesgames.tideborne.client.ui.FishingUiLayout;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout.FittedText;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryClient;
import com.redslovesgames.tideborne.discovery.multiplayer.SharedDiscoveryAvailability;
import com.redslovesgames.tideborne.discovery.multiplayer.SharedDiscoverySnapshot;
import com.redslovesgames.tideborne.discovery.DiscoveryClient;
import com.redslovesgames.tideborne.discovery.DiscoverySnapshot;
import com.redslovesgames.tideborne.discovery.DiscoveryTotals;
import com.redslovesgames.tideborne.satchel.SatchelFeature;
import com.redslovesgames.tideborne.satchel.SatchelProtectionRule;
import com.redslovesgames.tideborne.satchel.SatchelSortKey;
import com.redslovesgames.tideborne.satchel.SatchelSortRule;
import com.redslovesgames.tideborne.satchel.network.PersonalRecordView;
import com.redslovesgames.tideborne.satchel.network.SatchelFeatureView;
import com.redslovesgames.tideborne.satchel.network.SatchelRequestPayload;
import com.redslovesgames.tideborne.satchel.network.SatchelView;
import com.redslovesgames.tideborne.fishing.specimen.legacy.TraitAxesRuntime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType.Default;

@Environment(EnvType.CLIENT)
public final class AnglersSatchelScreen extends Screen {
   private static final int BACKGROUND_WIDTH = 400;
   private static final int BACKGROUND_HEIGHT = 260;
   private static final int TEXT = -12965349;
   private static final int MUTED_TEXT = -9282236;
   private static final int GOOD_TEXT = -13932478;
   private static final int BAD_TEXT = -6671571;
   private static final List<SatchelFeature> SATCHEL_UPGRADES = Arrays.stream(SatchelFeature.values())
      .filter(feature -> feature != SatchelFeature.SHARED_LEDGER)
      .toList();
   private static final int GRID_COLUMNS = 10;
   private static final int GRID_VISIBLE_ROWS = 7;
   private static final int GRID_CELL = 20;
   private static final Identifier JOURNAL_BACKGROUND = tide("textures/gui/journal/journal_bg.png");
   private static final Identifier BADGE_STRIP = ours("textures/gui/satchel/badge_strip.png");
   private static final Identifier BUTTON_NORMAL = ours("textures/gui/satchel/button_normal.png");
   private static final Identifier BUTTON_HOVERED = ours("textures/gui/satchel/button_hovered.png");
   private static final Identifier BUTTON_LOCKED = ours("textures/gui/satchel/button_locked.png");
   private static final Identifier SORT_ROW = ours("textures/gui/satchel/sort_row.png");
   private static final Identifier UPGRADE_ROW = ours("textures/gui/satchel/upgrade_row.png");
   private static final Identifier TOGGLE_ON = ours("textures/gui/satchel/toggle_on.png");
   private static final Identifier TOGGLE_OFF = ours("textures/gui/satchel/toggle_off.png");
   private static final Identifier SCROLL_TRACK = ours("textures/gui/satchel/scroll_track.png");
   private static final Identifier SCROLL_THUMB = ours("textures/gui/satchel/scroll_thumb.png");
   private static final Identifier XP_BACKGROUND = ours("textures/gui/satchel/xp_bg.png");
   private static final Identifier XP_FILL = ours("textures/gui/satchel/xp_fill.png");
   private SatchelView view;
   private List<ItemStack> contents;
   private List<SatchelRequestPayload.SortRuleRequest> draftSortRules;
   private AnglersSatchelScreen.Tab tab = AnglersSatchelScreen.Tab.CONTENTS;
   private AnglersSatchelScreen.Pending pending = AnglersSatchelScreen.Pending.NONE;
   private int selectedSlot = -1;
   private int contentScrollRow;
   private int recordScroll;
   private int upgradeScroll;
   private boolean draggingScrollbar;
   private int lastMouseX;
   private int lastMouseY;

   public AnglersSatchelScreen(SatchelView view) {
      super(Text.translatable("screen.tide_traits.anglers_satchel"));
      this.view = view;
      this.contents = view.contents().stream().map(ItemStack::copy).toList();
      this.draftSortRules = new ArrayList<>(view.sortRules().stream().map(rule -> new SatchelRequestPayload.SortRuleRequest(rule.key(), rule.direction())).toList());
   }

   public void update(SatchelView view) {
      this.view = view;
      this.contents = view.contents().stream().map(ItemStack::copy).toList();
      this.draftSortRules = new ArrayList<>(view.sortRules().stream().map(rule -> new SatchelRequestPayload.SortRuleRequest(rule.key(), rule.direction())).toList());
      this.pending = AnglersSatchelScreen.Pending.NONE;
   }

   @Override
   protected void init() {
      super.init();
      this.contentScrollRow = clampContentScroll(this.contentScrollRow);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.lastMouseX = mouseX;
      this.lastMouseY = mouseY;
      this.renderBackground(context, mouseX, mouseY, delta);
      int left = (this.width - 400) / 2;
      int top = (this.height - 260) / 2;
      context.drawTexture(JOURNAL_BACKGROUND, left, top, 0.0F, 0.0F, 400, 260, 400, 260);
      context.drawText(this.textRenderer, this.title, left + 18, top + 13, TEXT, false);
      switch (this.tab) {
         case CONTENTS -> renderContents(context, left, top, mouseX, mouseY);
         case RECORDS -> renderRecords(context, left, top);
         case UPGRADES -> renderUpgrades(context, left, top, mouseX, mouseY);
         case SETTINGS -> renderSettings(context, left, top, mouseX, mouseY);
      }
      super.render(context, mouseX, mouseY, delta);
   }

   private void renderContents(DrawContext context, int left, int top, int mouseX, int mouseY) {
      int start = this.contentScrollRow * GRID_COLUMNS;
      int end = Math.min(this.contents.size(), start + GRID_COLUMNS * GRID_VISIBLE_ROWS);
      for (int index = start; index < end; index++) {
         int local = index - start;
         int x = left + 18 + local % GRID_COLUMNS * GRID_CELL;
         int y = top + 43 + local / GRID_COLUMNS * GRID_CELL;
         ItemStack stack = this.contents.get(index);
         context.drawItem(stack, x, y);
         context.drawStackOverlay(this.textRenderer, stack, x, y);
      }
      context.drawText(this.textRenderer, Text.literal(this.contents.size() + " / " + this.view.capacity()), left + 18, top + 226, MUTED_TEXT, false);
   }

   private void renderRecords(DrawContext context, int left, int top) {
      context.drawText(this.textRenderer, Text.translatable("screen.tide_traits.satchel.records"), left + 18, top + 42, TEXT, false);
   }

   private void renderUpgrades(DrawContext context, int left, int top, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, Text.translatable("screen.tide_traits.satchel.upgrades"), left + 18, top + 42, TEXT, false);
   }

   private void renderSettings(DrawContext context, int left, int top, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, Text.translatable("screen.tide_traits.satchel.settings"), left + 18, top + 42, TEXT, false);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (this.tab == AnglersSatchelScreen.Tab.CONTENTS) {
         this.contentScrollRow = clampContentScroll(this.contentScrollRow - (int)Math.signum(verticalAmount));
         return true;
      }
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   private int clampContentScroll(int value) {
      int rows = Math.max(0, (this.contents.size() + GRID_COLUMNS - 1) / GRID_COLUMNS - GRID_VISIBLE_ROWS);
      return MathHelper.clamp(value, 0, rows);
   }

   private static Identifier ours(String path) {
      return Identifier.of("tide_traits", path);
   }

   private static Identifier tide(String path) {
      return Identifier.of("tide", path);
   }

   private enum Tab { CONTENTS, RECORDS, UPGRADES, SETTINGS }
   private enum Pending { NONE, REFRESH, MUTATION }
}
