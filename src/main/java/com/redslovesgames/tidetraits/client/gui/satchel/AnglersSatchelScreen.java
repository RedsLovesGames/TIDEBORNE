/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.client.gui.satchel;

import com.redslovesgames.tideborne.client.ui.FishingUiLayout;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout.FittedText;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import com.redslovesgames.tidetraits.compat.multiplayer.MultiplayerDiscoveryClient;
import com.redslovesgames.tidetraits.compat.multiplayer.SharedDiscoveryAvailability;
import com.redslovesgames.tidetraits.compat.multiplayer.SharedDiscoverySnapshot;
import com.redslovesgames.tidetraits.discovery.DiscoveryClient;
import com.redslovesgames.tidetraits.discovery.DiscoverySnapshot;
import com.redslovesgames.tidetraits.discovery.DiscoveryTotals;
import com.redslovesgames.tidetraits.satchel.SatchelFeature;
import com.redslovesgames.tidetraits.satchel.SatchelProtectionRule;
import com.redslovesgames.tidetraits.satchel.SatchelSortKey;
import com.redslovesgames.tidetraits.satchel.SatchelSortRule;
import com.redslovesgames.tidetraits.satchel.network.PersonalRecordView;
import com.redslovesgames.tidetraits.satchel.network.SatchelFeatureView;
import com.redslovesgames.tidetraits.satchel.network.SatchelRequestPayload;
import com.redslovesgames.tidetraits.satchel.network.SatchelView;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
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
   private boolean sortDirty;
   private boolean closeSent;
   private boolean closedFromServer;
   private boolean sharedSyncRequested;
   private String localStatus = "";

   public AnglersSatchelScreen(SatchelView initialView) {
      super(Text.literal(""));
      this.view = initialView;
      this.contents = initialView.contents();
      this.draftSortRules = wireRules(initialView.sortRules());
   }

   public Hand hand() {
      return this.view.hand();
   }

   public void updateFromServer(SatchelView updated) {
      AnglersSatchelScreen.Pending completed = this.pending;
      this.pending = AnglersSatchelScreen.Pending.NONE;
      this.view = updated;
      this.contents = updated.contents();
      this.selectedSlot = this.contents.isEmpty() ? -1 : MathHelper.clamp(this.selectedSlot, -1, this.contents.size() - 1);
      this.contentScrollRow = MathHelper.clamp(this.contentScrollRow, 0, this.maximumContentScroll());
      this.recordScroll = MathHelper.clamp(this.recordScroll, 0, this.maximumRecordScroll());
      if (!this.sortDirty || completed == AnglersSatchelScreen.Pending.SORT) {
         this.draftSortRules = wireRules(updated.sortRules());
         this.sortDirty = false;
      }

      this.localStatus = "";
      this.requestSharedDiscovery();
   }

   public void closeFromServer() {
      this.closedFromServer = true;
   }

   public boolean shouldPause() {
      return false;
   }

   protected void init() {
      super.init();
      this.requestSharedDiscovery();
   }

   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics, mouseX, mouseY, partialTick);
      int left = this.left();
      int top = this.top();
      blit(graphics, JOURNAL_BACKGROUND, left, top, 400, 260);
      graphics.drawText(this.textRenderer, this.title, left + 27, top + 32, -12965349, false);
      graphics.drawText(this.textRenderer, this.view.hand() == Hand.MAIN_HAND ? "" : "", left + 27, top + 29, -9282236, false);
      this.renderExperience(graphics, left, top);
      this.renderTabs(graphics, left, top, mouseX, mouseY);
      switch (this.tab) {
         case CONTENTS:
            this.renderContents(graphics, left, top, mouseX, mouseY);
            break;
         case SORTING:
            this.renderSorting(graphics, left, top, mouseX, mouseY);
            break;
         case UPGRADES:
            this.renderUpgrades(graphics, left, top, mouseX, mouseY);
            break;
         case RECORDS:
            this.renderRecords(graphics, left, top, mouseX, mouseY);
      }

      this.renderFooter(graphics, left, top, mouseX, mouseY);
      this.renderControlTooltip(graphics, left, top, mouseX, mouseY);
   }

   private void renderExperience(DrawContext graphics, int left, int top) {
      int x = left + 240;
      int y = top + 24;
      graphics.drawText(this.textRenderer, "XP " + this.view.experiencePoints(), x, y, -12965349, false);
      blit(graphics, XP_BACKGROUND, x + 40, y + 2, 96, 5);
      int target = this.nextMeaningfulCost();
      int filled = target <= 0 ? 96 : MathHelper.clamp((int)Math.round(96.0 * this.view.experiencePoints() / target), 0, 96);
      if (filled > 0) {
         graphics.drawTexture(XP_FILL, x + 40, y + 2, 0.0F, 0.0F, filled, 5, 96, 5);
      }
   }

   private void renderTabs(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int y = top + 39;

      for (int index = 0; index < AnglersSatchelScreen.Tab.values().length; index++) {
         AnglersSatchelScreen.Tab candidate = AnglersSatchelScreen.Tab.values()[index];
         int x = left + 27 + index * 87;
         boolean hovered = inside(mouseX, mouseY, x, y, 82, 17);
         int color = candidate == this.tab ? -1193885024 : (hovered ? -2132289352 : 1169398646);
         graphics.fill(x, y, x + 82, y + 17, color);
         if (candidate == this.tab) {
            graphics.drawHorizontalLine(x, x + 81, y + 16, -9612748);
         }

         blit(graphics, candidate.icon, x + 5, y + 4, 9, 9);
         graphics.drawText(this.textRenderer, candidate.label, x + 18, y + 5, -12965349, false);
      }
   }

   private void renderContents(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int gridX = left + 29;
      int gridY = top + 66;
      int firstSlot = this.contentScrollRow * 10;
      int visibleSlots = 70;
      int hoveredSlot = -1;

      for (int visible = 0; visible < visibleSlots; visible++) {
         int column = visible % 10;
         int row = visible / 10;
         int slot = firstSlot + visible;
         int x = gridX + column * 20;
         int y = gridY + row * 20;
         boolean hovered = inside(mouseX, mouseY, x, y, 18, 18);
         int fill = hovered ? -1594628408 : 1892531096;
         graphics.fill(x, y, x + 18, y + 18, fill);
         graphics.drawBorder(x, y, 18, 18, slot == this.selectedSlot ? -11829661 : -7703981);
         if (slot < this.contents.size()) {
            ItemStack stack = this.contents.get(slot);
            graphics.drawItem(stack, x + 1, y + 1);
            if (this.view.isProtected(slot)) {
               graphics.drawBorder(x, y, 18, 18, -4961470);
               blit(graphics, statusIcon("protected"), x + 10, y + 1, 7, 7);
            }

            this.renderPersonalRecordMarkers(graphics, x, y, slot);
            if (hovered) {
               hoveredSlot = slot;
            }
         }
      }

      this.renderContentScrollbar(graphics, left, top);
      this.renderSpecimenDetails(graphics, left, top, mouseX, mouseY);
      graphics.drawText(this.textRenderer, this.contents.size() + " / " + this.view.capacity() + " specimens", gridX + 5, top + 210, -9282236, false);
      if (hoveredSlot >= 0) {
         this.renderSpecimenTooltip(graphics, this.contents.get(hoveredSlot), mouseX, mouseY);
      }
   }

   private void renderContentScrollbar(DrawContext graphics, int left, int top) {
      int x = left + 232;
      int y = top + 66;

      for (int offset = 0; offset < 140; offset += 48) {
         int height = Math.min(48, 140 - offset);
         graphics.drawTexture(SCROLL_TRACK, x, y + offset, 0.0F, 0.0F, 6, height, 6, 48);
      }

      int maximum = this.maximumContentScroll();
      int thumbY = y + (maximum == 0 ? 0 : 128 * this.contentScrollRow / maximum);
      blit(graphics, SCROLL_THUMB, x, thumbY, 6, 12);
   }

   private void renderSpecimenDetails(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int x = left + 250;
      int y = top + 68;
      graphics.drawText(this.textRenderer, "Specimen", x, y, -12965349, false);
      if (this.selectedSlot >= 0 && this.selectedSlot < this.contents.size()) {
         ItemStack stack = this.contents.get(this.selectedSlot);
         Optional<SatchelSpecimenDisplay> canonical = SatchelSpecimenDisplay.from(stack);
         String fullName = stack.getName().getString();
         FittedText name = FishingUiLayout.ellipsize(fullName, 86, this.textRenderer::getWidth);
         graphics.drawText(this.textRenderer, canonical.map(SatchelSpecimenDisplay::rarityStarsLabel).orElse("?"), x, y + 10, -12965349, false);
         graphics.drawText(this.textRenderer, name.text(), x + 34, y + 10, -12965349, false);
         if (this.view.isProtected(this.selectedSlot)) {
            blit(graphics, statusIcon("protected"), x + 112, y + 1, 7, 7);
         }
         if (name.clipped() && inside(mouseX, mouseY, x + 34, y + 10, 86, 10)) {
            this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal(fullName)));
         }
         SatchelFeatureView scanner = this.view.feature(SatchelFeature.TRAIT_SCANNER.id());
         boolean scannerEnabled = scanner != null && scanner.unlocked() && scanner.enabled();
         if (scannerEnabled) {
            if (canonical.isPresent()) {
               SatchelSpecimenDisplay specimen = canonical.get();
               graphics.drawText(this.textRenderer, "FishScore " + specimen.scoreLabel(), x, y + 20, CanonicalSpecimenPresentation.SCORE_COLOR, false);
               graphics.drawText(this.textRenderer, "Length " + specimen.lengthLabel(), x, y + 29, -12965349, false);
               graphics.drawText(this.textRenderer, "Percentile " + specimen.percentileLabel(), x, y + 38, -12965349, false);
               graphics.drawText(this.textRenderer, "Traits", x, y + 49, -12965349, false);
               List<TraitDisplay> traits = specimen.traits();
               for (int index = 0; index < traits.size(); index++) {
                  TraitDisplay trait = traits.get(index);
                  graphics.drawText(
                     this.textRenderer,
                     trait.shortLabel() + " " + trait.value(),
                     x,
                     y + 59 + index * 9,
                     trait.color(),
                     false
                  );
               }
            } else {
               graphics.drawTextWrapped(
                  this.textRenderer, Text.literal("Canonical specimen data is unavailable for this stored fish."), x, y + 24, 120, -9282236
               );
            }
         } else {
            graphics.drawTextWrapped(
               this.textRenderer, Text.literal("Trait Scanner locked/off. Enable it to reveal specimen details."), x, y + 24, 120, -9282236
            );
         }

         this.renderPersonalRecordSummary(graphics, x, y + 97, this.selectedSlot);

         int extractY = top + 202;
         drawButton(graphics, x, extractY, "Take", !this.view.isProtected(this.selectedSlot), mouseX, mouseY);
         SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
         boolean canChangeProtection = lock != null && lock.unlocked() && (this.view.isProtected(this.selectedSlot) || lock.enabled());
         drawButton(graphics, x + 74, extractY, this.view.isProtected(this.selectedSlot) ? "Unlock" : "Protect", canChangeProtection, mouseX, mouseY);
      } else {
         graphics.drawTextWrapped(this.textRenderer, Text.literal("Select a specimen to inspect server-synchronized traits."), x, y + 16, 120, -9282236);
      }
   }

   private void renderSorting(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      SatchelFeatureView organizer = this.view.feature(SatchelFeature.TACKLE_ORGANIZER.id());
      boolean available = organizer != null && organizer.unlocked() && organizer.enabled();
      graphics.drawText(
         this.textRenderer,
         available ? "Sort Priority" : "Tackle Organizer must be unlocked and enabled",
         left + 29,
         top + 63,
         available ? -12965349 : -6671571,
         false
      );
      int rowX = left + 38;
      int rowY = top + 72;

      for (int index = 0; index < SatchelSortKey.values().length; index++) {
         SatchelSortKey key = SatchelSortKey.values()[index];
         int y = rowY + index * 22;
         blit(graphics, SORT_ROW, rowX, y, 144, 18);
         blit(graphics, sortIcon(key), rowX + 4, y + 4, 9, 9);
         int priority = this.draftIndex(key);
         String label = TraitAxesRuntime.titleCase(key.id());
         String prefix = priority < 0 ? "+ " : priority + 1 + ". ";
         graphics.drawText(this.textRenderer, prefix + label, rowX + 17, y + 5, priority < 0 ? -9282236 : -12965349, false);
         if (priority >= 0) {
            String direction = this.draftSortRules.get(priority).directionId().equals("desc") ? "↓ Desc" : "↑ Asc";
            graphics.drawText(this.textRenderer, direction, rowX + 106, y + 5, -12965349, false);
            graphics.fill(rowX + 147, y, rowX + 159, y + 18, 1619751761);
            graphics.fill(rowX + 161, y, rowX + 173, y + 18, 1619751761);
            graphics.drawText(this.textRenderer, "^", rowX + 150, y + 5, -12965349, false);
            graphics.drawText(this.textRenderer, "v", rowX + 164, y + 5, -12965349, false);
         }
      }

      int buttonY = top + 118;
      drawButton(graphics, left + 260, buttonY, this.sortDirty ? "Apply sort" : "Applied", available && this.sortDirty, mouseX, mouseY);
      graphics.drawText(this.textRenderer, "Click direction to reverse", left + 232, top + 92, -9282236, false);
      graphics.drawText(this.textRenderer, "Use arrows to reorder", left + 239, top + 104, -9282236, false);
   }

   private void renderUpgrades(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      graphics.drawText(this.textRenderer, "Installed Upgrades", left + 29, top + 63, -12965349, false);

      for (int index = 0; index <= SATCHEL_UPGRADES.size(); index++) {
         int column = index / 3;
         int row = index % 3;
         int x = left + 29 + column * 178;
         int y = top + 77 + row * 27;
         blit(graphics, UPGRADE_ROW, x, y, 160, 20);
         if (index == 0) {
            this.renderCapacityUpgrade(graphics, x, y);
         } else {
            this.renderFeatureUpgrade(graphics, SATCHEL_UPGRADES.get(index - 1), x, y);
         }
      }

      this.renderProtectionRules(graphics, left, top);
      this.renderUpgradeTooltip(graphics, mouseX, mouseY, left, top);
   }

   private void renderProtectionRules(DrawContext graphics, int left, int top) {
      SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
      boolean unlocked = lock != null && lock.unlocked();
      boolean enabled = unlocked && lock.enabled();
      graphics.drawText(
         this.textRenderer,
         unlocked ? "Trophy Lock Automatic Rules" : "Unlock Trophy Lock to configure automatic rules.",
         left + 29,
         top + 187,
         unlocked ? -12965349 : -9282236,
         false
      );
      if (unlocked) {
         SatchelProtectionRule[] rules = SatchelProtectionRule.values();

         for (int index = 0; index < rules.length; index++) {
            SatchelProtectionRule rule = rules[index];
            int column = index % 3;
            int row = index / 3;
            int x = left + 29 + column * 116;
            int y = top + 198 + row * 14;
            boolean ruleEnabled = this.view.protectionRuleEnabled(rule.id());
            blit(graphics, ruleEnabled ? TOGGLE_ON : TOGGLE_OFF, x, y + 1, 14, 7);
            graphics.drawText(this.textRenderer, protectionRuleLabel(rule), x + 18, y, !enabled && !ruleEnabled ? -9282236 : -12965349, false);
         }
      }
   }

   private void renderCapacityUpgrade(DrawContext graphics, int x, int y) {
      boolean var4;
      if (this.view.nextCapacityLevel() < 0) {
         var4 = true;
      } else {
         var4 = false;
      }

      blit(graphics, upgradeIcon("capacity", !var4), x + 2, y + 2, 16, 16);
      int var5 = this.view.capacityLevel();
      graphics.drawText(this.textRenderer, "Capacity " + roman(var5), x + 21, y + 3, -12965349, false);
      int var6 = this.view.capacity();
      String var7 = var6 + " slots";
      int var8;
      if (var5 <= 1) {
         var8 = -4628402;
      } else if (var5 == 2) {
         var8 = -3100082;
      } else {
         var8 = -10772903;
      }

      int var9 = x + 154 - this.textRenderer.getWidth(var7);
      graphics.drawText(this.textRenderer, var7, var9, y + 7, var8, false);
   }

   private void renderFeatureUpgrade(DrawContext graphics, SatchelFeature feature, int x, int y) {
      SatchelFeatureView featureView = this.view.feature(feature.id());
      boolean unlocked = featureView != null && featureView.unlocked();
      boolean enabled = featureView != null && featureView.enabled();
      boolean available = featureView != null && featureView.available();
      blit(graphics, upgradeIcon(feature.id(), unlocked), x + 2, y + 2, 16, 16);
      graphics.drawText(this.textRenderer, featureLabel(feature), x + 21, y + 3, available ? -12965349 : -9282236, false);
      if (unlocked) {
         blit(graphics, enabled ? TOGGLE_ON : TOGGLE_OFF, x + 136, y + 7, 14, 7);
         graphics.drawText(
            this.textRenderer,
            enabled ? "ON" : "OFF",
            x + 132 - MinecraftClient.getInstance().textRenderer.getWidth(enabled ? "ON" : "OFF"),
            y + 6,
            enabled ? -13932478 : -9282236,
            false
         );
      } else {
         String state = available ? featureView.xpCost() + " XP" : "ADDON";
         graphics.drawText(this.textRenderer, state, x + 108, y + 7, available ? -12965349 : -6671571, false);
      }
   }

   private void renderRecords(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      SatchelFeatureView keeper = this.view.feature(SatchelFeature.RECORD_KEEPER.id());
      boolean enabled = keeper != null && keeper.unlocked() && keeper.enabled();
      if (!enabled) {
         graphics.drawText(this.textRenderer, "Record Keeper is locked or disabled.", left + 30, top + 72, -6671571, false);
         graphics.drawTextWrapped(
            this.textRenderer,
            Text.literal("Unlock it in Upgrades to summarize synchronized specimen components in this satchel."),
            left + 30,
            top + 89,
            220,
            -9282236
         );
         drawButton(graphics, left + 30, top + 134, "Upgrades", true, mouseX, mouseY);
      } else {
         int listX = left + 38;
         graphics.drawText(
            this.textRenderer, "Satchel Records", (400 - this.textRenderer.getWidth("Satchel Records")) / 2 + left, top + 64, -12965349, false
         );
         graphics.drawText(
            this.textRenderer,
            "Top satchel specimens by canonical V2 FishScore",
            (400 - this.textRenderer.getWidth("Top satchel specimens by canonical V2 FishScore")) / 2 + left,
            top + 75,
            -9282236,
            false
         );
         List<ItemStack> sorted = this.contents.stream().sorted(Comparator.comparingInt(AnglersSatchelScreen::recordScoreValue).reversed()).toList();
         byte visible = 16;

         for (int index = 0; index < visible; index++) {
            int actual = this.recordScroll + index;
            if (actual >= sorted.size()) {
               break;
            }

            ItemStack stack = sorted.get(actual);
            int columnX = listX + index / 8 * 174;
            int rowY = top + 94 + index % 8 * 15;
            graphics.drawText(this.textRenderer, Integer.toString(actual + 1), columnX, rowY, -9282236, false);
            graphics.drawItem(stack, columnX + 18, rowY - 4);
            FittedText name = FishingUiLayout.ellipsize(stack.getName().getString(), 91, this.textRenderer::getWidth);
            graphics.drawText(
               this.textRenderer, name.text(), columnX + 37, rowY, -12965349, false
            );
            String score = recordScoreLabel(stack);
            graphics.drawText(
               this.textRenderer, score, FishingUiLayout.rightAlignedX(columnX + 170, this.textRenderer.getWidth(score)), rowY, 0xFF43A8D8, false
            );
         }
      }
   }

   private void renderRecordBadge(DrawContext graphics, int x, int y, String icon, String label, ItemStack stack) {
      blit(graphics, statusIcon(icon), x, y, 7, 7);
      String rawValue = stack.isEmpty()
         ? CanonicalSpecimenPresentation.UNAVAILABLE
         : stack.getName().getString() + " " + CanonicalSpecimenPresentation.length(length(stack));
      int valueWidth = Math.max(18, 155 - this.textRenderer.getWidth(label + " "));
      String value = this.textRenderer.trimToWidth(rawValue, valueWidth);
      graphics.drawText(this.textRenderer, label + " " + value, x + 11, y - 1, -12965349, false);
   }

   private void renderPersonalRecordMarkers(DrawContext graphics, int x, int y, int slot) {
      graphics.getMatrices().push();

      try {
         graphics.getMatrices().translate(0.0F, 0.0F, 250.0F);
         if (this.isPersonalLargest(slot)) {
            graphics.fill(x + 1, y + 1, x + 9, y + 10, -793556158);
            graphics.drawText(this.textRenderer, "L", x + 2, y + 1, -1, false);
         }

         if (this.isPersonalSmallest(slot)) {
            graphics.fill(x + 9, y + 1, x + 17, y + 10, -801020011);
            graphics.drawText(this.textRenderer, "S", x + 10, y + 1, -1, false);
         }
      } finally {
         graphics.getMatrices().pop();
      }
   }

   private void renderSpecimenTooltip(DrawContext graphics, ItemStack stack, int mouseX, int mouseY) {
      List<Text> lines = new ArrayList<>(stack.getTooltip(TooltipContext.DEFAULT, MinecraftClient.getInstance().player, Default.BASIC));
      graphics.drawOrderedTooltip(this.textRenderer, lines.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
   }

   private void renderPersonalRecordSummary(DrawContext graphics, int x, int y, int slot) {
      graphics.drawText(this.textRenderer, "Records", x, y, -12965349, false);
      Optional<PersonalRecordView> record = this.view.personalRecordAt(slot);
      if (record.isEmpty()) {
         graphics.drawText(this.textRenderer, "No personal record yet", x, y + 10, -9282236, false);
      } else {
         PersonalRecordView stats = record.get();
         graphics.drawText(this.textRenderer, "Personal largest: " + CanonicalSpecimenPresentation.length(stats.largest()), x, y + 10, -9282236, false);
         graphics.drawText(this.textRenderer, "Personal smallest: " + CanonicalSpecimenPresentation.length(stats.smallest()), x, y + 19, -9282236, false);
      }
   }

   private boolean isPersonalLargest(int slot) {
      return slot >= 0
         && slot < this.contents.size()
         && this.view.personalRecordAt(slot).map(record -> sameLength(length(this.contents.get(slot)), record.largest())).orElse(false);
   }

   private boolean isPersonalSmallest(int slot) {
      return slot >= 0
         && slot < this.contents.size()
         && this.view.personalRecordAt(slot).map(record -> sameLength(length(this.contents.get(slot)), record.smallest())).orElse(false);
   }

   private void renderSharedLedger(DrawContext graphics, int x, int y) {
      SharedDiscoverySnapshot shared = MultiplayerDiscoveryClient.snapshot();
      boolean teamSnapshot = shared.isAvailable();
      DiscoverySnapshot discoveries = teamSnapshot ? shared.discoveries() : DiscoveryClient.snapshot();
      DiscoveryTotals totals = DiscoveryTotals.from(discoveries);

      String status = switch (shared.availability()) {
         case UNKNOWN -> "Team journal syncing";
         case AVAILABLE -> "Team journal synced";
         case MISSING_MODS -> "Team journal add-on missing";
         case TEAM_UNRESOLVED -> "No active team journal";
         case ERROR -> "Team journal sync error";
      };
      int statusColor = shared.availability() == SharedDiscoveryAvailability.AVAILABLE
         ? -13932478
         : (shared.availability() == SharedDiscoveryAvailability.UNKNOWN ? -9282236 : -6671571);
      blit(graphics, statusIcon("shared"), x, y, 7, 7);
      graphics.drawText(this.textRenderer, status, x + 11, y - 1, statusColor, false);
      graphics.drawText(
         this.textRenderer, (teamSnapshot ? "Team " : "Personal ") + totals.speciesCount() + " species", x, y + 13, teamSnapshot ? -13932478 : -9282236, false
      );
      graphics.drawText(
         this.textRenderer, "Traits " + totals.mutationCount() + "  Size bands " + totals.sizeBandCount(), x, y + 26, teamSnapshot ? -12965349 : -9282236, false
      );
   }

   private void renderUpgradeTooltip(DrawContext graphics, int mouseX, int mouseY, int left, int top) {
      for (int index = 0; index <= SATCHEL_UPGRADES.size(); index++) {
         int column = index / 3;
         int row = index % 3;
         int x = left + 29 + column * 178;
         int y = top + 77 + row * 27;
         if (inside(mouseX, mouseY, x, y, 160, 20)) {
            List<Text> lines = index == 0
               ? List.of(
                  Text.literal("Capacity"),
                  Text.literal("Adds more individual specimen slots."),
                  Text.literal("Current: " + this.view.capacity() + " slots")
               )
               : upgradeTooltip(SATCHEL_UPGRADES.get(index - 1));
            graphics.drawOrderedTooltip(this.textRenderer, lines.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
            return;
         }
      }
   }

   private void renderControlTooltip(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int tabY = top + 39;

      for (int index = 0; index < AnglersSatchelScreen.Tab.values().length; index++) {
         if (inside(mouseX, mouseY, left + 27 + index * 87, tabY, 82, 17)) {
            AnglersSatchelScreen.Tab hovered = AnglersSatchelScreen.Tab.values()[index];

            this.showTooltip(
               graphics,
               mouseX,
               mouseY,
               switch (hovered) {
                  case CONTENTS -> List.of(
                     Text.literal("Contents"), Text.literal("Browse stored fish, inspect traits, and take a fish out.")
                  );
                  case SORTING -> List.of(Text.literal("Sorting"), Text.literal("Choose the order used for stored fish."));
                  case UPGRADES -> List.of(Text.literal("Upgrades"), Text.literal("Spend XP to unlock satchel abilities and settings."));
                  case RECORDS -> List.of(Text.literal("Records"), Text.literal("Browse stored specimens and record fish."));
               }
            );
            return;
         }
      }

      int footerY = top + 222;
      if (inside(mouseX, mouseY, left + 125, footerY + 1, 72, 16)) {
         this.showTooltip(
            graphics,
            mouseX,
            mouseY,
            List.of(
               Text.literal(this.view.active() ? "Deactivate satchel" : "Make satchel active"),
               Text.literal("The active satchel receives Auto-Stow catches when several satchels qualify.")
            )
         );
      } else if (inside(mouseX, mouseY, left + 203, footerY + 1, 72, 16)) {
         this.showTooltip(
            graphics,
            mouseX,
            mouseY,
            List.of(Text.literal("Refresh from server"), Text.literal("Reload the current contents, records, and upgrade state."))
         );
      } else {
         switch (this.tab) {
            case CONTENTS:
               this.renderContentsControlTooltip(graphics, left, top, mouseX, mouseY);
               break;
            case SORTING:
               this.renderSortingControlTooltip(graphics, left, top, mouseX, mouseY);
               break;
            case UPGRADES:
               this.renderUpgradeControlTooltip(graphics, left, top, mouseX, mouseY);
               break;
            case RECORDS:
               SatchelFeatureView keeper = this.view.feature(SatchelFeature.RECORD_KEEPER.id());
               if ((keeper == null || !keeper.unlocked() || !keeper.enabled()) && inside(mouseX, mouseY, left + 30, top + 134, 72, 16)) {
                  this.showTooltip(
                     graphics,
                     mouseX,
                     mouseY,
                     List.of(Text.literal("Open Upgrades"), Text.literal("Unlock and enable Record Keeper to use this page."))
                  );
               }
         }
      }
   }

   private void renderContentsControlTooltip(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      if (this.selectedSlot >= 0 && this.selectedSlot < this.contents.size()) {
         int x = left + 258;
         int y = top + 202;
         if (inside(mouseX, mouseY, x, y, 48, 16)) {
            this.showTooltip(
               graphics,
               mouseX,
               mouseY,
               List.of(
                  Text.literal("Take fish"),
                  Text.literal(
                     this.view.isProtected(this.selectedSlot)
                        ? "This trophy is protected. Unlock it first."
                        : "Move this fish from the satchel to your inventory."
                  )
               )
            );
         } else if (inside(mouseX, mouseY, x + 58, y, 48, 16)) {
            boolean protectedFish = this.view.isProtected(this.selectedSlot);
            this.showTooltip(
               graphics,
               mouseX,
               mouseY,
               List.of(
                  Text.literal(protectedFish ? "Unlock trophy" : "Protect trophy"),
                  Text.literal(
                     protectedFish ? "Allow this fish to be taken from the satchel." : "Prevent this fish from being taken until you unlock it."
                  )
               )
            );
         }
      }
   }

   private void renderSortingControlTooltip(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int rowX = left + 38;
      int rowY = top + 72;

      for (int index = 0; index < SatchelSortKey.values().length; index++) {
         SatchelSortKey key = SatchelSortKey.values()[index];
         int y = rowY + index * 22;
         int priority = this.draftIndex(key);
         if (priority >= 0 && inside(mouseX, mouseY, rowX + 147, y, 12, 18)) {
            this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal("Move this rule earlier")));
            return;
         }

         if (priority >= 0 && inside(mouseX, mouseY, rowX + 161, y, 12, 18)) {
            this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal("Move this rule later")));
            return;
         }

         if (inside(mouseX, mouseY, rowX, y, 144, 18)) {
            String action = priority < 0 ? "Add this sorting rule" : "Remove this sorting rule";
            if (priority >= 0 && mouseX >= rowX + 105) {
               action = "Reverse ascending/descending order";
            }

            this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal(TraitAxesRuntime.titleCase(key.id())), Text.literal(action)));
            return;
         }
      }

      if (inside(mouseX, mouseY, left + 260, top + 118, 72, 16)) {
         this.showTooltip(
            graphics,
            mouseX,
            mouseY,
            List.of(Text.literal("Apply sorting"), Text.literal("Send the current sorting rules to the server."))
         );
      }
   }

   private void renderUpgradeControlTooltip(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
      if (lock != null && lock.unlocked()) {
         SatchelProtectionRule[] rules = SatchelProtectionRule.values();

         for (int index = 0; index < rules.length; index++) {
            int x = left + 29 + index % 3 * 116;
            int y = top + 198 + index / 3 * 14;
            if (inside(mouseX, mouseY, x, y, 110, 10)) {
               SatchelProtectionRule rule = rules[index];
               this.showTooltip(
                  graphics,
                  mouseX,
                  mouseY,
                  List.of(
                     Text.literal(protectionRuleLabel(rule)),
                     Text.literal(protectionRuleDescription(rule)),
                     Text.literal("When matched, newly stored fish are protected automatically.")
                  )
               );
               return;
            }
         }
      }
   }

   private void showTooltip(DrawContext graphics, int mouseX, int mouseY, List<Text> lines) {
      graphics.drawOrderedTooltip(this.textRenderer, lines.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
   }

   private static List<Text> upgradeTooltip(SatchelFeature feature) {
      return switch (feature) {
         case TACKLE_ORGANIZER -> List.of(Text.literal("Tackle Organizer"), Text.literal("Enables custom multi-rule satchel sorting."));
         case AUTO_STOW -> List.of(
            Text.literal("Auto-Stow"),
            Text.literal("Stores eligible catches in this satchel automatically."),
            Text.literal("Make it active to choose it when several qualify.")
         );
         case RECORD_KEEPER -> List.of(
            Text.literal("Record Keeper"),
            Text.literal("Shows this player's native Tide largest and smallest records."),
            Text.literal("It never replaces team-journal records.")
         );
         case TRAIT_SCANNER -> List.of(
            Text.literal("Trait Scanner"),
            Text.literal("Reveals canonical percentile, Body Type, Condition, Pigmentation, Quality, and FishScore."),
            Text.literal("Select a specimen in Contents to inspect it.")
         );
         case TROPHY_LOCK -> List.of(
            Text.literal("Trophy Lock"),
            Text.literal("Protects selected or automatically matched specimens from extraction."),
            Text.literal("Configure its automatic rules below.")
         );
         case SHARED_LEDGER -> List.of();
      };
   }

   private void renderFooter(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int y = top + 220;
      blit(graphics, BADGE_STRIP, left + 28, y, 0, 20);
      int badgeX = left + 34;
      blit(graphics, statusIcon("active"), badgeX, y + 3, 7, 7);
      graphics.drawText(this.textRenderer, this.view.active() ? "Active" : "Inactive", badgeX + 10, y + 3, this.view.active() ? -13932478 : -9282236, false);
      graphics.drawText(this.textRenderer, this.view.open() ? "Open" : "Closed", badgeX + 55, y + 3, this.view.open() ? -13932478 : -6671571, false);
      drawButton(graphics, left + 136, y + 3, this.view.active() ? "Deactivate" : "Make active", true, mouseX, mouseY);
      drawButton(
         graphics,
         left + 214,
         y + 3,
         this.pending == AnglersSatchelScreen.Pending.NONE ? "Refresh" : "Waiting...",
         this.pending == AnglersSatchelScreen.Pending.NONE,
         mouseX,
         mouseY
      );
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.pending == AnglersSatchelScreen.Pending.NONE) {
         int left = this.left();
         int top = this.top();
         int tabY = top + 39;

         for (int index = 0; index < AnglersSatchelScreen.Tab.values().length; index++) {
            int x = left + 27 + index * 87;
            if (inside(mouseX, mouseY, x, tabY, 82, 17)) {
               AnglersSatchelScreen.Tab selected = AnglersSatchelScreen.Tab.values()[index];
               if (selected == AnglersSatchelScreen.Tab.RECORDS && this.tab != AnglersSatchelScreen.Tab.RECORDS) {
                  this.sharedSyncRequested = false;
               }

               this.tab = selected;
               this.requestSharedDiscovery();
               return true;
            }
         }

         if (inside(mouseX, mouseY, left + 125, top + 223, 72, 16)) {
            this.dispatch(AnglersSatchelScreen.Pending.ACTIVE, SatchelClientNetworking.toggleActive(this.view));
            return true;
         }

         if (inside(mouseX, mouseY, left + 203, top + 223, 72, 16)) {
            this.dispatch(AnglersSatchelScreen.Pending.REFRESH, SatchelClientNetworking.refresh(this.view));
            return true;
         }

         switch (this.tab) {
            case CONTENTS:
               if (this.clickContents(mouseX, mouseY, left, top)) {
                  return true;
               }
               break;
            case SORTING:
               if (this.clickSorting(mouseX, mouseY, left, top)) {
                  return true;
               }
               break;
            case UPGRADES:
               if (this.clickUpgrades(mouseX, mouseY, left, top)) {
                  return true;
               }
               break;
            case RECORDS:
               if (this.clickRecords(mouseX, mouseY, left, top)) {
                  return true;
               }
               break;
            default:
               throw new MatchException(null, null);
         }

         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private boolean clickContents(double mouseX, double mouseY, int left, int top) {
      int gridX = left + 29;
      int gridY = top + 66;
      if (inside(mouseX, mouseY, gridX, gridY, 200, 140)) {
         int column = ((int)mouseX - gridX) / 20;
         int row = ((int)mouseY - gridY) / 20;
         int slot = this.contentScrollRow * 10 + row * 10 + column;
         if (slot < this.contents.size()) {
            this.selectedSlot = slot;
         }

         return true;
      } else if (this.selectedSlot >= 0 && this.selectedSlot < this.contents.size()) {
         int x = left + 258;
         int buttonY = top + 202;
         if (inside(mouseX, mouseY, x, buttonY, 48, 16)) {
            if (this.view.isProtected(this.selectedSlot)) {
               this.localStatus = "Unprotect that specimen before extracting it";
               return true;
            } else {
               this.dispatch(AnglersSatchelScreen.Pending.EXTRACT, SatchelClientNetworking.extract(this.view, this.selectedSlot));
               return true;
            }
         } else if (!inside(mouseX, mouseY, x + 58, buttonY, 48, 16)) {
            return false;
         } else {
            SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
            boolean canChange = lock != null && lock.unlocked() && (this.view.isProtected(this.selectedSlot) || lock.enabled());
            if (!canChange) {
               this.localStatus = "Unlock and enable Trophy Lock first";
               return true;
            } else {
               this.dispatch(
                  AnglersSatchelScreen.Pending.PROTECT,
                  SatchelClientNetworking.setProtected(this.view, this.selectedSlot, !this.view.isProtected(this.selectedSlot))
               );
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean clickSorting(double mouseX, double mouseY, int left, int top) {
      int rowX = left + 38;
      int rowY = top + 72;

      for (int index = 0; index < SatchelSortKey.values().length; index++) {
         SatchelSortKey key = SatchelSortKey.values()[index];
         int y = rowY + index * 22;
         int priority = this.draftIndex(key);
         if (priority >= 0 && inside(mouseX, mouseY, rowX + 147, y, 12, 18)) {
            this.moveDraft(priority, -1);
            return true;
         }

         if (priority >= 0 && inside(mouseX, mouseY, rowX + 161, y, 12, 18)) {
            this.moveDraft(priority, 1);
            return true;
         }

         if (inside(mouseX, mouseY, rowX, y, 144, 18)) {
            if (priority < 0) {
               this.draftSortRules.add(new SatchelRequestPayload.SortRuleRequest(key.id(), "asc"));
            } else if (mouseX >= rowX + 105) {
               SatchelRequestPayload.SortRuleRequest old = this.draftSortRules.get(priority);
               String direction = old.directionId().equals("asc") ? "desc" : "asc";
               this.draftSortRules.set(priority, new SatchelRequestPayload.SortRuleRequest(old.keyId(), direction));
            } else {
               this.draftSortRules.remove(priority);
            }

            this.sortDirty = true;
            return true;
         }
      }

      if (inside(mouseX, mouseY, left + 260, top + 118, 72, 16) && this.sortDirty) {
         SatchelFeatureView organizer = this.view.feature(SatchelFeature.TACKLE_ORGANIZER.id());
         if (organizer != null && organizer.unlocked() && organizer.enabled()) {
            this.dispatch(AnglersSatchelScreen.Pending.SORT, SatchelClientNetworking.sort(this.view, this.draftSortRules));
            return true;
         } else {
            this.localStatus = "Unlock and enable Tackle Organizer first";
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean clickUpgrades(double mouseX, double mouseY, int left, int top) {
      for (int index = 0; index <= SATCHEL_UPGRADES.size(); index++) {
         int column = index / 3;
         int row = index % 3;
         int x = left + 29 + column * 178;
         int y = top + 77 + row * 27;
         if (inside(mouseX, mouseY, x, y, 160, 20)) {
            if (index == 0) {
               if (this.view.nextCapacityLevel() >= 0) {
                  this.dispatch(AnglersSatchelScreen.Pending.PURCHASE, SatchelClientNetworking.purchaseNextCapacity(this.view));
               }

               return true;
            }

            SatchelFeature feature = SATCHEL_UPGRADES.get(index - 1);
            SatchelFeatureView featureView = this.view.feature(feature.id());
            if (featureView == null) {
               this.localStatus = "The server did not provide this upgrade state";
               return true;
            }

            if (featureView.available() || featureView.unlocked() && featureView.enabled()) {
               if (featureView.unlocked()) {
                  this.dispatch(AnglersSatchelScreen.Pending.TOGGLE, SatchelClientNetworking.toggleFeature(this.view, feature.id(), !featureView.enabled()));
               } else {
                  this.dispatch(AnglersSatchelScreen.Pending.PURCHASE, SatchelClientNetworking.purchaseFeature(this.view, feature.id()));
               }

               return true;
            }

            this.localStatus = "This upgrade's optional server prerequisite is unavailable";
            return true;
         }
      }

      SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
      if (lock != null && lock.unlocked()) {
         SatchelProtectionRule[] rules = SatchelProtectionRule.values();

         for (int index = 0; index < rules.length; index++) {
            int column = index % 3;
            int row = index / 3;
            int x = left + 29 + column * 116;
            int y = top + 198 + row * 14;
            if (inside(mouseX, mouseY, x, y, 110, 10)) {
               SatchelProtectionRule rule = rules[index];
               boolean currentlyEnabled = this.view.protectionRuleEnabled(rule.id());
               if (!lock.enabled() && !currentlyEnabled) {
                  this.localStatus = "Enable Trophy Lock before enabling automatic rules";
                  return true;
               }

               this.dispatch(AnglersSatchelScreen.Pending.PROTECTION_RULE, SatchelClientNetworking.setProtectionRule(this.view, rule.id(), !currentlyEnabled));
               return true;
            }
         }
      }

      return false;
   }

   private boolean clickRecords(double mouseX, double mouseY, int left, int top) {
      SatchelFeatureView keeper = this.view.feature(SatchelFeature.RECORD_KEEPER.id());
      boolean enabled = keeper != null && keeper.unlocked() && keeper.enabled();
      if (!enabled && inside(mouseX, mouseY, left + 30, top + 134, 72, 16)) {
         this.tab = AnglersSatchelScreen.Tab.UPGRADES;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int left = this.left();
      int top = this.top();
      if (this.tab == AnglersSatchelScreen.Tab.CONTENTS && inside(mouseX, mouseY, left + 28, top + 64, 211, 143)) {
         this.contentScrollRow = MathHelper.clamp(this.contentScrollRow - (int)Math.signum(verticalAmount), 0, this.maximumContentScroll());
         return true;
      } else if (this.tab == AnglersSatchelScreen.Tab.RECORDS && inside(mouseX, mouseY, left + 30, top + 80, 340, 136)) {
         this.recordScroll = MathHelper.clamp(this.recordScroll - (int)Math.signum(verticalAmount), 0, this.maximumRecordScroll());
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public void close() {
      this.sendCloseOnce();
      super.close();
   }

   public void removed() {
      this.sendCloseOnce();
      super.removed();
   }

   private void sendCloseOnce() {
      if (!this.closedFromServer && !this.closeSent) {
         this.closeSent = true;
         SatchelClientNetworking.close(this.view.hand(), this.view.stateToken());
      }
   }

   private void dispatch(AnglersSatchelScreen.Pending action, boolean sent) {
      if (sent) {
         this.pending = action;
         this.localStatus = "Waiting for server...";
      } else {
         this.localStatus = "Satchel networking is unavailable";
      }
   }

   private void moveDraft(int index, int direction) {
      int target = index + direction;
      if (target >= 0 && target < this.draftSortRules.size()) {
         SatchelRequestPayload.SortRuleRequest moved = this.draftSortRules.remove(index);
         this.draftSortRules.add(target, moved);
         this.sortDirty = true;
      }
   }

   private int draftIndex(SatchelSortKey key) {
      for (int index = 0; index < this.draftSortRules.size(); index++) {
         if (this.draftSortRules.get(index).keyId().equals(key.id())) {
            return index;
         }
      }

      return -1;
   }

   private int maximumContentScroll() {
      int rows = (this.contents.size() + 10 - 1) / 10;
      return Math.max(0, rows - 7);
   }

   private int maximumRecordScroll() {
      return Math.max(0, this.contents.size() - 16);
   }

   private void requestSharedDiscovery() {
      if (!this.sharedSyncRequested) {
         this.sharedSyncRequested = MultiplayerDiscoveryClient.requestSync();
      }
   }

   private int nextMeaningfulCost() {
      int target = Math.max(0, this.view.nextCapacityCost());

      for (SatchelFeatureView feature : this.view.features()) {
         if (!SatchelFeature.SHARED_LEDGER.id().equals(feature.id()) && !feature.unlocked() && feature.available()) {
            target = target == 0 ? feature.xpCost() : Math.min(target, feature.xpCost());
         }
      }

      return target;
   }

   private static double length(ItemStack stack) {
      return SatchelSpecimenDisplay.from(stack).map(SatchelSpecimenDisplay::length).orElse(0.0);
   }

   private static int recordScoreValue(ItemStack stack) {
      return SatchelSpecimenDisplay.from(stack).map(SatchelSpecimenDisplay::scoreOrMissing).orElse(-1);
   }

   private static String recordScoreLabel(ItemStack stack) {
      return SatchelSpecimenDisplay.from(stack).map(SatchelSpecimenDisplay::scoreLabel).orElse(CanonicalSpecimenPresentation.UNAVAILABLE);
   }

   private static boolean sameLength(double first, double second) {
      return Double.isFinite(first) && Double.isFinite(second) && Math.abs(first - second) <= Math.max(1.0E-6, Math.ulp(second) * 4.0);
   }

   private static List<SatchelRequestPayload.SortRuleRequest> wireRules(List<SatchelSortRule> rules) {
      return new ArrayList<>(rules.stream().map(rule -> new SatchelRequestPayload.SortRuleRequest(rule.key().id(), rule.direction().id())).toList());
   }

   private static void drawButton(DrawContext graphics, int x, int y, String text, boolean enabled, int mouseX, int mouseY) {
      byte var7 = 72;
      if (text.equals("Take") || text.equals("Unlock") || text.equals("Protect")) {
         var7 = 48;
      }

      byte var8 = 0;
      if (!text.equals("Take")) {
         if (text.equals("Unlock") || text.equals("Protect")) {
            var8 = -8;
         }
      } else {
         var8 = 8;
      }

      Identifier var9;
      if (!enabled) {
         var9 = BUTTON_LOCKED;
      } else if (inside(mouseX, mouseY, x + var8, y, var7, 16)) {
         var9 = BUTTON_HOVERED;
      } else {
         var9 = BUTTON_NORMAL;
      }

      blit(graphics, var9, x + var8, y, var7, 16);
      int var10;
      if (enabled) {
         var10 = -12965349;
      } else {
         var10 = -9282236;
      }

      TextRenderer var11 = MinecraftClient.getInstance().textRenderer;
      graphics.drawText(var11, text, x + var8 + (var7 - var11.getWidth(text)) / 2, y + 4, var10, false);
   }

   private static void blit(DrawContext graphics, Identifier texture, int x, int y, int width, int height) {
      graphics.drawTexture(texture, x, y, 0.0F, 0.0F, width, height, width, height);
   }

   private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   private static String featureLabel(SatchelFeature feature) {
      return switch (feature) {
         case TACKLE_ORGANIZER -> "Organizer";
         case AUTO_STOW -> "Auto-Stow";
         case RECORD_KEEPER -> "Records";
         case TRAIT_SCANNER -> "Trait Scanner";
         case TROPHY_LOCK -> "Trophy Lock";
         case SHARED_LEDGER -> "Shared Ledger";
      };
   }

   private static String protectionRuleLabel(SatchelProtectionRule rule) {
      return switch (rule) {
         case MUTATED -> "Mutated";
         case TROPHY_SIZE -> "Trophy size";
         case LEGENDARY_SIZE -> "Legendary size";
         case PERSONAL_LARGEST -> "Largest";
         case PERSONAL_SMALLEST -> "Smallest";
         case TIDE_LEGENDARY_RARITY -> "Tide rarity";
      };
   }

   private static String protectionRuleDescription(SatchelProtectionRule rule) {
      return switch (rule) {
         case MUTATED -> "Protect fish with any special body type or condition.";
         case TROPHY_SIZE -> "Protect fish in the Trophy size band.";
         case LEGENDARY_SIZE -> "Protect fish in the Legendary size band.";
         case PERSONAL_LARGEST -> "Protect a fish that matches your personal largest record.";
         case PERSONAL_SMALLEST -> "Protect a fish that matches your personal smallest record.";
         case TIDE_LEGENDARY_RARITY -> "Protect fish with Tide's legendary rarity.";
      };
   }

   private static String roman(int level) {
      return switch (level) {
         case 1 -> "I";
         case 2 -> "II";
         case 3 -> "III";
         default -> "Base";
      };
   }

   private static Identifier upgradeIcon(String id, boolean unlocked) {
      return ours("textures/gui/upgrades/" + (unlocked ? "unlocked/" : "locked/") + id + "_" + (unlocked ? "unlocked" : "locked") + ".png");
   }

   private static Identifier sortIcon(SatchelSortKey key) {
      return ours("textures/gui/sort/" + key.id() + ".png");
   }

   private static Identifier statusIcon(String id) {
      return ours("textures/gui/status/" + id + ".png");
   }

   private static Identifier ours(String path) {
      return Identifier.of("tide_traits", path);
   }

   private static Identifier tide(String path) {
      return Identifier.of("tide", path);
   }

   private int left() {
      return (this.width - 400) / 2;
   }

   private int top() {
      return (this.height - 260) / 2;
   }

   @Environment(EnvType.CLIENT)
   private enum Pending {
      NONE,
      REFRESH,
      PURCHASE,
      TOGGLE,
      SORT,
      EXTRACT,
      PROTECT,
      PROTECTION_RULE,
      ACTIVE;
   }

   @Environment(EnvType.CLIENT)
   private enum Tab {
      CONTENTS("Contents", AnglersSatchelScreen.ours("textures/gui/tabs/contents.png")),
      SORTING("Sorting", AnglersSatchelScreen.ours("textures/gui/tabs/sorting.png")),
      UPGRADES("Upgrades", AnglersSatchelScreen.ours("textures/gui/tabs/upgrades.png")),
      RECORDS("Records", AnglersSatchelScreen.ours("textures/gui/tabs/records.png"));

      private final String label;
      private final Identifier icon;

      Tab(String label, Identifier icon) {
         this.label = label;
         this.icon = icon;
      }
   }
}
