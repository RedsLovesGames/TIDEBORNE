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
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class AnglersSatchelScreen extends Screen {
   private static final int BACKGROUND_WIDTH = 400;
   private static final int BACKGROUND_HEIGHT = 260;
   private static final int SCREEN_MARGIN = 6;
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
   private static final int BUTTON_HEIGHT = 16;
   private static final int BADGE_STRIP_WIDTH = 124;
   private static final int FOOTER_ACTIVE_X = 136;
   private static final int FOOTER_REFRESH_X = 214;
   private static final int FOOTER_BUTTON_Y = 223;
   private static final int FOOTER_BUTTON_WIDTH = 72;
   private static final int STATUS_X = 29;
   private static final int STATUS_Y = 242;
   private static final int STATUS_MAX_WIDTH = 342;
   private static final int RECORD_VISIBLE = 16;
   private static final int RECORD_ROWS_PER_COLUMN = 8;
   private static final int RECORD_NAME_WIDTH = 91;
   private static final int RECORD_SCROLL_X = 386;
   private static final int RECORD_SCROLL_Y = 94;
   private static final int RECORD_SCROLL_HEIGHT = 120;
   private static final String ORGANIZER_REQUIRED = "Unlock and enable Tackle Organizer first";
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
      super(Text.literal("Angler's Satchel"));
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

      this.localStatus = responseStatus(updated);
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
      float scale = this.interfaceScale();
      int logicalMouseX = (int)Math.round(this.logicalMouseX(mouseX, scale));
      int logicalMouseY = (int)Math.round(this.logicalMouseY(mouseY, scale));
      graphics.getMatrices().push();

      try {
         graphics.getMatrices().translate(this.width / 2.0F, this.height / 2.0F, 0.0F);
         graphics.getMatrices().scale(scale, scale, 1.0F);
         graphics.getMatrices().translate(-this.width / 2.0F, -this.height / 2.0F, 0.0F);
         int left = this.left();
         int top = this.top();
         blit(graphics, JOURNAL_BACKGROUND, left, top, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
         graphics.drawText(this.textRenderer, this.title, left + 27, top + 28, TEXT, false);
         this.renderExperience(graphics, left, top);
         this.renderTabs(graphics, left, top, logicalMouseX, logicalMouseY);
         switch (this.tab) {
            case CONTENTS:
               this.renderContents(graphics, left, top, logicalMouseX, logicalMouseY);
               break;
            case SORTING:
               this.renderSorting(graphics, left, top, logicalMouseX, logicalMouseY);
               break;
            case UPGRADES:
               this.renderUpgrades(graphics, left, top, logicalMouseX, logicalMouseY);
               break;
            case RECORDS:
               this.renderRecords(graphics, left, top, logicalMouseX, logicalMouseY);
         }

         this.renderFooter(graphics, left, top, logicalMouseX, logicalMouseY);
         this.renderStatus(graphics, left, top, logicalMouseX, logicalMouseY);
         this.renderControlTooltip(graphics, left, top, logicalMouseX, logicalMouseY);
      } finally {
         graphics.getMatrices().pop();
      }
   }

   private void renderExperience(DrawContext graphics, int left, int top) {
      int x = left + 240;
      int y = top + 24;
      graphics.drawText(this.textRenderer, "XP " + this.view.experiencePoints(), x, y, TEXT, false);
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
         graphics.drawText(this.textRenderer, candidate.label, x + 18, y + 5, TEXT, false);
      }
   }

   private void renderContents(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      int gridX = left + 29;
      int gridY = top + 66;
      int firstSlot = this.contentScrollRow * GRID_COLUMNS;
      int visibleSlots = GRID_COLUMNS * GRID_VISIBLE_ROWS;
      int hoveredSlot = -1;

      for (int visible = 0; visible < visibleSlots; visible++) {
         int column = visible % GRID_COLUMNS;
         int row = visible / GRID_COLUMNS;
         int slot = firstSlot + visible;
         int x = gridX + column * GRID_CELL;
         int y = gridY + row * GRID_CELL;
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
      graphics.drawText(this.textRenderer, this.contents.size() + " / " + this.view.capacity() + " specimens", gridX + 5, top + 210, MUTED_TEXT, false);
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
      graphics.drawText(this.textRenderer, "Specimen", x, y, TEXT, false);
      if (this.selectedSlot >= 0 && this.selectedSlot < this.contents.size()) {
         ItemStack stack = this.contents.get(this.selectedSlot);
         Optional<SatchelSpecimenDisplay> canonical = SatchelSpecimenDisplay.from(stack);
         String fullName = stack.getName().getString();
         FittedText name = FishingUiLayout.ellipsize(fullName, 86, this.textRenderer::getWidth);
         graphics.drawText(this.textRenderer, canonical.map(SatchelSpecimenDisplay::rarityStarsLabel).orElse("?"), x, y + 10, TEXT, false);
         graphics.drawText(this.textRenderer, name.text(), x + 34, y + 10, TEXT, false);
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
               graphics.drawText(this.textRenderer, "Length " + specimen.lengthLabel(), x, y + 29, TEXT, false);
               graphics.drawText(this.textRenderer, "Percentile " + specimen.percentileLabel(), x, y + 38, TEXT, false);
               graphics.drawText(this.textRenderer, "Traits", x, y + 49, TEXT, false);
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
                  this.textRenderer, Text.literal("Canonical specimen data is unavailable for this stored fish."), x, y + 24, 120, MUTED_TEXT
               );
            }
         } else {
            graphics.drawTextWrapped(
               this.textRenderer, Text.literal("Trait Scanner locked/off. Enable it to reveal specimen details."), x, y + 24, 120, MUTED_TEXT
            );
         }

         this.renderPersonalRecordSummary(graphics, x, y + 97, this.selectedSlot);

         int extractY = top + 202;
         drawButton(graphics, x, extractY, "Take", !this.view.isProtected(this.selectedSlot), mouseX, mouseY);
         SatchelFeatureView lock = this.view.feature(SatchelFeature.TROPHY_LOCK.id());
         boolean canChangeProtection = lock != null && lock.unlocked() && (this.view.isProtected(this.selectedSlot) || lock.enabled());
         drawButton(graphics, x + 74, extractY, this.view.isProtected(this.selectedSlot) ? "Unlock" : "Protect", canChangeProtection, mouseX, mouseY);
      } else {
         graphics.drawTextWrapped(this.textRenderer, Text.literal("Select a specimen to inspect server-synchronized traits."), x, y + 16, 120, MUTED_TEXT);
      }
   }

   private void renderSorting(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      boolean available = this.sortingAvailable();
      graphics.drawText(
         this.textRenderer,
         available ? "Sort Priority" : "Tackle Organizer locked or disabled",
         left + 29,
         top + 63,
         available ? TEXT : BAD_TEXT,
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
         int labelColor = available ? (priority < 0 ? MUTED_TEXT : TEXT) : MUTED_TEXT;
         graphics.drawText(this.textRenderer, prefix + label, rowX + 17, y + 5, labelColor, false);
         if (priority >= 0) {
            String direction = this.draftSortRules.get(priority).directionId().equals("desc") ? "↓ Desc" : "↑ Asc";
            graphics.drawText(this.textRenderer, direction, rowX + 106, y + 5, available ? TEXT : MUTED_TEXT, false);
            if (available) {
               graphics.fill(rowX + 147, y, rowX + 159, y + 18, 1619751761);
               graphics.fill(rowX + 161, y, rowX + 173, y + 18, 1619751761);
               graphics.drawText(this.textRenderer, "^", rowX + 150, y + 5, TEXT, false);
               graphics.drawText(this.textRenderer, "v", rowX + 164, y + 5, TEXT, false);
            }
         }
         if (!available) {
            graphics.fill(rowX, y, rowX + 173, y + 18, 0x44E1D5BC);
         }
      }

      int buttonY = top + 118;
      if (available) {
         drawButton(graphics, left + 260, buttonY, this.sortDirty ? "Apply sort" : "Applied", this.sortDirty, mouseX, mouseY);
         graphics.drawText(this.textRenderer, "Click direction to reverse", left + 232, top + 92, MUTED_TEXT, false);
         graphics.drawText(this.textRenderer, "Use arrows to reorder", left + 239, top + 104, MUTED_TEXT, false);
      } else {
         drawButton(graphics, left + 260, buttonY, "Upgrades", true, mouseX, mouseY);
         graphics.drawText(this.textRenderer, "Enable Organizer in Upgrades", left + 226, top + 92, MUTED_TEXT, false);
      }
   }

   private void renderUpgrades(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      graphics.drawText(this.textRenderer, "Installed Upgrades", left + 29, top + 63, TEXT, false);

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
         unlocked ? TEXT : MUTED_TEXT,
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
            graphics.drawText(this.textRenderer, protectionRuleLabel(rule), x + 18, y, !enabled && !ruleEnabled ? MUTED_TEXT : TEXT, false);
         }
      }
   }

   private void renderCapacityUpgrade(DrawContext graphics, int x, int y) {
      boolean maxed = this.view.nextCapacityLevel() < 0;
      blit(graphics, upgradeIcon("capacity", !maxed), x + 2, y + 2, 16, 16);
      int level = this.view.capacityLevel();
      graphics.drawText(this.textRenderer, "Capacity " + roman(level), x + 21, y + 3, TEXT, false);
      String slots = this.view.capacity() + " slots";
      int slotsColor = level <= 1 ? -4628402 : level == 2 ? -3100082 : -10772903;
      int slotsX = x + 154 - this.textRenderer.getWidth(slots);
      graphics.drawText(this.textRenderer, slots, slotsX, y + 7, slotsColor, false);
   }

   private void renderFeatureUpgrade(DrawContext graphics, SatchelFeature feature, int x, int y) {
      SatchelFeatureView featureView = this.view.feature(feature.id());
      boolean unlocked = featureView != null && featureView.unlocked();
      boolean enabled = featureView != null && featureView.enabled();
      boolean available = featureView != null && featureView.available();
      blit(graphics, upgradeIcon(feature.id(), unlocked), x + 2, y + 2, 16, 16);
      graphics.drawText(this.textRenderer, featureLabel(feature), x + 21, y + 3, unlocked || available ? TEXT : MUTED_TEXT, false);
      if (unlocked) {
         blit(graphics, enabled ? TOGGLE_ON : TOGGLE_OFF, x + 136, y + 7, 14, 7);
         String state = enabled ? "ON" : "OFF";
         graphics.drawText(
            this.textRenderer,
            state,
            x + 132 - this.textRenderer.getWidth(state),
            y + 6,
            enabled ? GOOD_TEXT : MUTED_TEXT,
            false
         );
      } else {
         String state = available ? featureView.xpCost() + " XP" : "ADDON";
         graphics.drawText(this.textRenderer, state, x + 108, y + 7, available ? TEXT : BAD_TEXT, false);
      }
   }

   private void renderRecords(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      SatchelFeatureView keeper = this.view.feature(SatchelFeature.RECORD_KEEPER.id());
      boolean enabled = keeper != null && keeper.unlocked() && keeper.enabled();
      if (!enabled) {
         graphics.drawText(this.textRenderer, "Record Keeper is locked or disabled.", left + 30, top + 72, BAD_TEXT, false);
         graphics.drawTextWrapped(
            this.textRenderer,
            Text.literal("Unlock it in Upgrades to summarize synchronized specimen components in this satchel."),
            left + 30,
            top + 89,
            220,
            MUTED_TEXT
         );
         drawButton(graphics, left + 30, top + 134, "Upgrades", true, mouseX, mouseY);
      } else {
         int listX = left + 38;
         graphics.drawText(
            this.textRenderer,
            "Satchel Records",
            (BACKGROUND_WIDTH - this.textRenderer.getWidth("Satchel Records")) / 2 + left,
            top + 64,
            TEXT,
            false
         );
         String subtitle = "Top satchel specimens by canonical V2 FishScore";
         graphics.drawText(
            this.textRenderer,
            subtitle,
            (BACKGROUND_WIDTH - this.textRenderer.getWidth(subtitle)) / 2 + left,
            top + 75,
            MUTED_TEXT,
            false
         );
         List<ItemStack> sorted = this.contents.stream().sorted(Comparator.comparingInt(AnglersSatchelScreen::recordScoreValue).reversed()).toList();
         String hoveredName = null;

         for (int index = 0; index < RECORD_VISIBLE; index++) {
            int actual = this.recordScroll + index;
            if (actual >= sorted.size()) {
               break;
            }

            ItemStack stack = sorted.get(actual);
            int columnX = listX + index / RECORD_ROWS_PER_COLUMN * 174;
            int rowY = top + 94 + index % RECORD_ROWS_PER_COLUMN * 15;
            graphics.drawText(this.textRenderer, Integer.toString(actual + 1), columnX, rowY, MUTED_TEXT, false);
            graphics.drawItem(stack, columnX + 18, rowY - 4);
            String fullName = stack.getName().getString();
            FittedText name = FishingUiLayout.ellipsize(fullName, RECORD_NAME_WIDTH, this.textRenderer::getWidth);
            graphics.drawText(this.textRenderer, name.text(), columnX + 37, rowY, TEXT, false);
            if (name.clipped() && inside(mouseX, mouseY, columnX + 37, rowY - 1, RECORD_NAME_WIDTH, 11)) {
               hoveredName = fullName;
            }
            String score = recordScoreLabel(stack);
            graphics.drawText(
               this.textRenderer,
               score,
               FishingUiLayout.rightAlignedX(columnX + 170, this.textRenderer.getWidth(score)),
               rowY,
               CanonicalSpecimenPresentation.SCORE_COLOR,
               false
            );
         }

         if (sorted.isEmpty()) {
            graphics.drawText(this.textRenderer, "No specimens stored yet.", left + 145, top + 132, MUTED_TEXT, false);
         }

         int first = sorted.isEmpty() ? 0 : this.recordScroll + 1;
         int last = Math.min(sorted.size(), this.recordScroll + RECORD_VISIBLE);
         String showing = "Showing " + first + "-" + last + " of " + this.contents.size();
         graphics.drawText(
            this.textRenderer,
            showing,
            (BACKGROUND_WIDTH - this.textRenderer.getWidth(showing)) / 2 + left,
            top + 210,
            MUTED_TEXT,
            false
         );
         this.renderRecordScrollbar(graphics, left, top);
         if (hoveredName != null) {
            this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal(hoveredName)));
         }
      }
   }

   private void renderRecordScrollbar(DrawContext graphics, int left, int top) {
      int maximum = this.maximumRecordScroll();
      if (maximum <= 0) {
         return;
      }

      int x = left + RECORD_SCROLL_X;
      int y = top + RECORD_SCROLL_Y;
      for (int offset = 0; offset < RECORD_SCROLL_HEIGHT; offset += 48) {
         int height = Math.min(48, RECORD_SCROLL_HEIGHT - offset);
         graphics.drawTexture(SCROLL_TRACK, x, y + offset, 0.0F, 0.0F, 6, height, 6, 48);
      }

      int thumbTravel = RECORD_SCROLL_HEIGHT - 12;
      int thumbY = y + thumbTravel * this.recordScroll / maximum;
      blit(graphics, SCROLL_THUMB, x, thumbY, 6, 12);
   }

   private void renderRecordBadge(DrawContext graphics, int x, int y, String icon, String label, ItemStack stack) {
      blit(graphics, statusIcon(icon), x, y, 7, 7);
      String rawValue = stack.isEmpty()
         ? CanonicalSpecimenPresentation.UNAVAILABLE
         : stack.getName().getString() + " " + CanonicalSpecimenPresentation.length(length(stack));
      int valueWidth = Math.max(18, 155 - this.textRenderer.getWidth(label + " "));
      String value = this.textRenderer.trimToWidth(rawValue, valueWidth);
      graphics.drawText(this.textRenderer, label + " " + value, x + 11, y - 1, TEXT, false);
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
      graphics.drawText(this.textRenderer, "Records", x, y, TEXT, false);
      Optional<PersonalRecordView> record = this.view.personalRecordAt(slot);
      if (record.isEmpty()) {
         graphics.drawText(this.textRenderer, "No personal record yet", x, y + 10, MUTED_TEXT, false);
      } else {
         PersonalRecordView stats = record.get();
         graphics.drawText(this.textRenderer, "Largest " + CanonicalSpecimenPresentation.length(stats.largest()), x, y + 10, MUTED_TEXT, false);
         graphics.drawText(this.textRenderer, "Smallest " + CanonicalSpecimenPresentation.length(stats.smallest()), x, y + 19, MUTED_TEXT, false);
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
         ? GOOD_TEXT
         : (shared.availability() == SharedDiscoveryAvailability.UNKNOWN ? MUTED_TEXT : BAD_TEXT);
      blit(graphics, statusIcon("shared"), x, y, 7, 7);
      graphics.drawText(this.textRenderer, status, x + 11, y - 1, statusColor, false);
      graphics.drawText(
         this.textRenderer,
         (teamSnapshot ? "Team " : "Personal ") + totals.speciesCount() + " species",
         x,
         y + 13,
         teamSnapshot ? GOOD_TEXT : MUTED_TEXT,
         false
      );
      graphics.drawText(
         this.textRenderer,
         "Traits " + totals.mutationCount() + "  Size bands " + totals.sizeBandCount(),
         x,
         y + 26,
         teamSnapshot ? TEXT : MUTED_TEXT,
         false
      );
   }

   private void renderUpgradeTooltip(DrawContext graphics, int mouseX, int mouseY, int left, int top) {
      for (int index = 0; index <= SATCHEL_UPGRADES.size(); index++) {
         int column = index / 3;
         int row = index % 3;
         int x = left + 29 + column * 178;
         int y = top + 77 + row * 27;
         if (inside(mouseX, mouseY, x, y, 160, 20)) {
            List<Text> lines;
            if (index == 0) {
               lines = new ArrayList<>();
               lines.add(Text.literal("Capacity"));
               lines.add(Text.literal("Adds more individual specimen slots."));
               lines.add(Text.literal("Current: " + this.view.capacity() + " slots"));
               if (this.view.nextCapacityLevel() < 0) {
                  lines.add(Text.literal("Maximum capacity reached."));
               } else {
                  lines.add(Text.literal("Next: Capacity " + roman(this.view.nextCapacityLevel()) + " for " + this.view.nextCapacityCost() + " XP"));
               }
            } else {
               SatchelFeature feature = SATCHEL_UPGRADES.get(index - 1);
               lines = new ArrayList<>(upgradeTooltip(feature));
               SatchelFeatureView featureView = this.view.feature(feature.id());
               if (featureView == null) {
                  lines.add(Text.literal("Server state unavailable."));
               } else if (featureView.unlocked()) {
                  lines.add(Text.literal(featureView.enabled() ? "Enabled" : "Disabled"));
               } else if (featureView.available()) {
                  lines.add(Text.literal("Unlock cost: " + featureView.xpCost() + " XP"));
               } else {
                  lines.add(Text.literal("Optional server prerequisite unavailable."));
               }
            }
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
                     Text.literal("Contents"), Text.literal("Browse stored fish, inspect traits, and take a fish out."), Text.literal("Keyboard: arrows move selection; Tab or 1-4 changes page.")
                  );
                  case SORTING -> List.of(Text.literal("Sorting"), Text.literal("Choose the order used for stored fish."), Text.literal("Keyboard: Tab or 1-4 changes page."));
                  case UPGRADES -> List.of(Text.literal("Upgrades"), Text.literal("Spend XP to unlock satchel abilities and settings."), Text.literal("Keyboard: Tab or 1-4 changes page."));
                  case RECORDS -> List.of(Text.literal("Records"), Text.literal("Browse stored specimens and record fish."), Text.literal("Keyboard: Up/Down or Page Up/Page Down scrolls."));
               }
            );
            return;
         }
      }

      if (inside(mouseX, mouseY, left + FOOTER_ACTIVE_X, top + FOOTER_BUTTON_Y, FOOTER_BUTTON_WIDTH, BUTTON_HEIGHT)) {
         this.showTooltip(
            graphics,
            mouseX,
            mouseY,
            List.of(
               Text.literal(this.view.active() ? "Deactivate satchel" : "Make satchel active"),
               Text.literal("The active satchel receives Auto-Stow catches when several satchels qualify.")
            )
         );
      } else if (inside(mouseX, mouseY, left + FOOTER_REFRESH_X, top + FOOTER_BUTTON_Y, FOOTER_BUTTON_WIDTH, BUTTON_HEIGHT)) {
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
               if ((keeper == null || !keeper.unlocked() || !keeper.enabled()) && inside(mouseX, mouseY, left + 30, top + 134, 72, BUTTON_HEIGHT)) {
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
         if (inside(mouseX, mouseY, x, y, 48, BUTTON_HEIGHT)) {
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
         } else if (inside(mouseX, mouseY, x + 58, y, 48, BUTTON_HEIGHT)) {
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
      if (!this.sortingAvailable()) {
         if (inside(mouseX, mouseY, left + 260, top + 118, 72, BUTTON_HEIGHT)) {
            this.showTooltip(
               graphics,
               mouseX,
               mouseY,
               List.of(Text.literal("Open Upgrades"), Text.literal("Unlock and enable Tackle Organizer to edit sorting."))
            );
         } else if (inside(mouseX, mouseY, rowX, rowY, 173, SatchelSortKey.values().length * 22)) {
            this.showTooltip(
               graphics,
               mouseX,
               mouseY,
               List.of(Text.literal("Sorting unavailable"), Text.literal("Unlock and enable Tackle Organizer first."))
            );
         }
         return;
      }

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

      if (inside(mouseX, mouseY, left + 260, top + 118, 72, BUTTON_HEIGHT)) {
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

   private void renderStatus(DrawContext graphics, int left, int top, int mouseX, int mouseY) {
      if (this.localStatus.isBlank()) {
         return;
      }

      FittedText status = FishingUiLayout.ellipsize(this.localStatus, STATUS_MAX_WIDTH, this.textRenderer::getWidth);
      int color = this.pending == AnglersSatchelScreen.Pending.NONE ? BAD_TEXT : MUTED_TEXT;
      graphics.drawText(this.textRenderer, status.text(), left + STATUS_X, top + STATUS_Y, color, false);
      if (status.clipped() && inside(mouseX, mouseY, left + STATUS_X, top + STATUS_Y - 1, STATUS_MAX_WIDTH, 11)) {
         this.showTooltip(graphics, mouseX, mouseY, List.of(Text.literal(this.localStatus)));
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
      blit(graphics, BADGE_STRIP, left + 28, y, BADGE_STRIP_WIDTH, 20);
      int badgeX = left + 34;
      blit(graphics, statusIcon("active"), badgeX, y + 3, 7, 7);
      graphics.drawText(this.textRenderer, this.view.active() ? "Active" : "Inactive", badgeX + 10, y + 3, this.view.active() ? GOOD_TEXT : MUTED_TEXT, false);
      graphics.drawText(this.textRenderer, this.view.open() ? "Open" : "Closed", badgeX + 55, y + 3, this.view.open() ? GOOD_TEXT : BAD_TEXT, false);
      drawButton(
         graphics,
         left + FOOTER_ACTIVE_X,
         top + FOOTER_BUTTON_Y,
         this.view.active() ? "Deactivate" : "Make active",
         true,
         mouseX,
         mouseY
      );
      drawButton(
         graphics,
         left + FOOTER_REFRESH_X,
         top + FOOTER_BUTTON_Y,
         this.pending == AnglersSatchelScreen.Pending.NONE ? "Refresh" : "Waiting...",
         this.pending == AnglersSatchelScreen.Pending.NONE,
         mouseX,
         mouseY
      );
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double logicalMouseX = this.logicalMouseX(mouseX, this.interfaceScale());
      double logicalMouseY = this.logicalMouseY(mouseY, this.interfaceScale());
      if (button == 0 && this.pending == AnglersSatchelScreen.Pending.NONE) {
         int left = this.left();
         int top = this.top();
         int tabY = top + 39;

         for (int index = 0; index < AnglersSatchelScreen.Tab.values().length; index++) {
            int x = left + 27 + index * 87;
            if (inside(logicalMouseX, logicalMouseY, x, tabY, 82, 17)) {
               this.selectTab(AnglersSatchelScreen.Tab.values()[index]);
               return true;
            }
         }

         if (inside(logicalMouseX, logicalMouseY, left + FOOTER_ACTIVE_X, top + FOOTER_BUTTON_Y, FOOTER_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            this.dispatch(AnglersSatchelScreen.Pending.ACTIVE, SatchelClientNetworking.toggleActive(this.view));
            return true;
         }

         if (inside(logicalMouseX, logicalMouseY, left + FOOTER_REFRESH_X, top + FOOTER_BUTTON_Y, FOOTER_BUTTON_WIDTH, BUTTON_HEIGHT)) {
            this.dispatch(AnglersSatchelScreen.Pending.REFRESH, SatchelClientNetworking.refresh(this.view));
            return true;
         }

         switch (this.tab) {
            case CONTENTS:
               if (this.clickContents(logicalMouseX, logicalMouseY, left, top)) {
                  return true;
               }
               break;
            case SORTING:
               if (this.clickSorting(logicalMouseX, logicalMouseY, left, top)) {
                  return true;
               }
               break;
            case UPGRADES:
               if (this.clickUpgrades(logicalMouseX, logicalMouseY, left, top)) {
                  return true;
               }
               break;
            case RECORDS:
               if (this.clickRecords(logicalMouseX, logicalMouseY, left, top)) {
                  return true;
               }
               break;
            default:
               throw new MatchException(null, null);
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.pending == AnglersSatchelScreen.Pending.NONE) {
         if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_4) {
            this.selectTab(AnglersSatchelScreen.Tab.values()[keyCode - GLFW.GLFW_KEY_1]);
            return true;
         }
         if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.cycleTab((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? -1 : 1);
            return true;
         }
         if (this.tab == AnglersSatchelScreen.Tab.CONTENTS && this.handleContentsKey(keyCode)) {
            return true;
         }
         if (this.tab == AnglersSatchelScreen.Tab.RECORDS && this.handleRecordsKey(keyCode)) {
            return true;
         }
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   private boolean clickContents(double mouseX, double mouseY, int left, int top) {
      int gridX = left + 29;
      int gridY = top + 66;
      int gridWidth = GRID_COLUMNS * GRID_CELL;
      int gridHeight = GRID_VISIBLE_ROWS * GRID_CELL;
      if (inside(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
         int column = ((int)mouseX - gridX) / GRID_CELL;
         int row = ((int)mouseY - gridY) / GRID_CELL;
         int slot = this.contentScrollRow * GRID_COLUMNS + row * GRID_COLUMNS + column;
         if (slot < this.contents.size()) {
            this.selectedSlot = slot;
         }

         return true;
      } else if (this.selectedSlot >= 0 && this.selectedSlot < this.contents.size()) {
         int x = left + 258;
         int buttonY = top + 202;
         if (inside(mouseX, mouseY, x, buttonY, 48, BUTTON_HEIGHT)) {
            if (this.view.isProtected(this.selectedSlot)) {
               this.localStatus = "Unprotect that specimen before extracting it";
               return true;
            } else {
               this.dispatch(AnglersSatchelScreen.Pending.EXTRACT, SatchelClientNetworking.extract(this.view, this.selectedSlot));
               return true;
            }
         } else if (!inside(mouseX, mouseY, x + 58, buttonY, 48, BUTTON_HEIGHT)) {
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
      if (!this.sortingAvailable()) {
         if (inside(mouseX, mouseY, left + 260, top + 118, 72, BUTTON_HEIGHT)) {
            this.selectTab(AnglersSatchelScreen.Tab.UPGRADES);
            return true;
         }
         if (inside(mouseX, mouseY, rowX, rowY, 173, SatchelSortKey.values().length * 22)) {
            this.localStatus = ORGANIZER_REQUIRED;
            return true;
         }
         return false;
      }

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

      if (inside(mouseX, mouseY, left + 260, top + 118, 72, BUTTON_HEIGHT) && this.sortDirty) {
         this.dispatch(AnglersSatchelScreen.Pending.SORT, SatchelClientNetworking.sort(this.view, this.draftSortRules));
         return true;
      }
      return false;
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
               } else {
                  this.localStatus = "Maximum capacity reached";
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
      if (!enabled && inside(mouseX, mouseY, left + 30, top + 134, 72, BUTTON_HEIGHT)) {
         this.selectTab(AnglersSatchelScreen.Tab.UPGRADES);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      float scale = this.interfaceScale();
      double logicalMouseX = this.logicalMouseX(mouseX, scale);
      double logicalMouseY = this.logicalMouseY(mouseY, scale);
      int left = this.left();
      int top = this.top();
      if (this.tab == AnglersSatchelScreen.Tab.CONTENTS && inside(logicalMouseX, logicalMouseY, left + 28, top + 64, 211, 143)) {
         this.contentScrollRow = MathHelper.clamp(this.contentScrollRow - (int)Math.signum(verticalAmount), 0, this.maximumContentScroll());
         return true;
      } else if (this.tab == AnglersSatchelScreen.Tab.RECORDS && inside(logicalMouseX, logicalMouseY, left + 30, top + 80, 362, 136)) {
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

   private void selectTab(AnglersSatchelScreen.Tab selected) {
      if (selected == AnglersSatchelScreen.Tab.RECORDS && this.tab != AnglersSatchelScreen.Tab.RECORDS) {
         this.sharedSyncRequested = false;
      }
      this.tab = selected;
      this.localStatus = "";
      this.requestSharedDiscovery();
   }

   private void cycleTab(int direction) {
      AnglersSatchelScreen.Tab[] tabs = AnglersSatchelScreen.Tab.values();
      int next = Math.floorMod(this.tab.ordinal() + direction, tabs.length);
      this.selectTab(tabs[next]);
   }

   private boolean handleContentsKey(int keyCode) {
      if (this.contents.isEmpty()) {
         return false;
      }

      int current = this.selectedSlot >= 0 ? this.selectedSlot : 0;
      int target = switch (keyCode) {
         case GLFW.GLFW_KEY_LEFT -> current - 1;
         case GLFW.GLFW_KEY_RIGHT -> current + 1;
         case GLFW.GLFW_KEY_UP -> current - GRID_COLUMNS;
         case GLFW.GLFW_KEY_DOWN -> current + GRID_COLUMNS;
         case GLFW.GLFW_KEY_PAGE_UP -> current - GRID_COLUMNS * GRID_VISIBLE_ROWS;
         case GLFW.GLFW_KEY_PAGE_DOWN -> current + GRID_COLUMNS * GRID_VISIBLE_ROWS;
         case GLFW.GLFW_KEY_HOME -> 0;
         case GLFW.GLFW_KEY_END -> this.contents.size() - 1;
         default -> Integer.MIN_VALUE;
      };
      if (target == Integer.MIN_VALUE) {
         return false;
      }

      this.selectContentsSlot(target);
      return true;
   }

   private void selectContentsSlot(int target) {
      this.selectedSlot = MathHelper.clamp(target, 0, this.contents.size() - 1);
      int selectedRow = this.selectedSlot / GRID_COLUMNS;
      if (selectedRow < this.contentScrollRow) {
         this.contentScrollRow = selectedRow;
      } else if (selectedRow >= this.contentScrollRow + GRID_VISIBLE_ROWS) {
         this.contentScrollRow = selectedRow - GRID_VISIBLE_ROWS + 1;
      }
      this.contentScrollRow = MathHelper.clamp(this.contentScrollRow, 0, this.maximumContentScroll());
   }

   private boolean handleRecordsKey(int keyCode) {
      int delta = switch (keyCode) {
         case GLFW.GLFW_KEY_UP -> -1;
         case GLFW.GLFW_KEY_DOWN -> 1;
         case GLFW.GLFW_KEY_PAGE_UP -> -RECORD_ROWS_PER_COLUMN;
         case GLFW.GLFW_KEY_PAGE_DOWN -> RECORD_ROWS_PER_COLUMN;
         case GLFW.GLFW_KEY_HOME -> -this.recordScroll;
         case GLFW.GLFW_KEY_END -> this.maximumRecordScroll() - this.recordScroll;
         default -> 0;
      };
      if (delta == 0 && keyCode != GLFW.GLFW_KEY_HOME && keyCode != GLFW.GLFW_KEY_END) {
         return false;
      }
      this.recordScroll = MathHelper.clamp(this.recordScroll + delta, 0, this.maximumRecordScroll());
      return true;
   }

   private boolean sortingAvailable() {
      SatchelFeatureView organizer = this.view.feature(SatchelFeature.TACKLE_ORGANIZER.id());
      return organizer != null && organizer.unlocked() && organizer.enabled();
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
      int rows = (this.contents.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
      return Math.max(0, rows - GRID_VISIBLE_ROWS);
   }

   private int maximumRecordScroll() {
      return Math.max(0, this.contents.size() - RECORD_VISIBLE);
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

   private static String responseStatus(SatchelView updated) {
      return switch (updated.status()) {
         case OPENED, REFRESHED, SUCCESS -> "";
         default -> {
            String detail = updated.detail().strip();
            yield detail.isEmpty() ? TraitAxesRuntime.titleCase(updated.status().id()) : detail;
         }
      };
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
      int width = text.equals("Take") || text.equals("Unlock") || text.equals("Protect") ? 48 : 72;
      int offsetX = text.equals("Take") ? 8 : text.equals("Unlock") || text.equals("Protect") ? -8 : 0;
      Identifier texture;
      if (!enabled) {
         texture = BUTTON_LOCKED;
      } else if (inside(mouseX, mouseY, x + offsetX, y, width, BUTTON_HEIGHT)) {
         texture = BUTTON_HOVERED;
      } else {
         texture = BUTTON_NORMAL;
      }

      blit(graphics, texture, x + offsetX, y, width, BUTTON_HEIGHT);
      int color = enabled ? TEXT : MUTED_TEXT;
      TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
      graphics.drawText(renderer, text, x + offsetX + (width - renderer.getWidth(text)) / 2, y + 4, color, false);
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
      return Identifier.of("tideborne", path);
   }

   private static Identifier tide(String path) {
      return Identifier.of("tide", path);
   }

   private float interfaceScale() {
      return FishingUiLayout.fitScale(this.width, this.height, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, SCREEN_MARGIN);
   }

   private double logicalMouseX(double mouseX, float scale) {
      return FishingUiLayout.inverseCenteredCoordinate(mouseX, this.width, scale);
   }

   private double logicalMouseY(double mouseY, float scale) {
      return FishingUiLayout.inverseCenteredCoordinate(mouseY, this.height, scale);
   }

   private int left() {
      return (this.width - BACKGROUND_WIDTH) / 2;
   }

   private int top() {
      return (this.height - BACKGROUND_HEIGHT) / 2;
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
