/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.data.fishing.DisplayData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.client.ui.FishingUiFormat;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout;
import com.redslovesgames.tideborne.client.ui.FishingUiLayout.FittedText;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import com.redslovesgames.tidetraits.entity.SpecimenTransfer;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public final class TopFishScreen extends Screen {
   private static final Identifier BG = Identifier.of("tide", "textures/gui/journal/journal_bg.png");
   private static final int TEXT = 5477982;
   private static final int MUTED = 6650722;
   private static final int TOP_FISH_SLOTS = 15;
   private static final int LIST_PANEL_LEFT = 28;
   private static final int LIST_PANEL_RIGHT = 196;
   private static final int LIST_PANEL_TOP = 42;
   private static final int LIST_PANEL_BOTTOM = 240;
   private static final int LIST_ROW_LEFT = 32;
   private static final int LIST_ROW_RIGHT = 192;
   private static final int LIST_FIRST_ROW_Y = 56;
   private static final int RANK_X = 34;
   private static final int ICON_X = 46;
   private static final int NAME_X = 64;
   private static final int NAME_WIDTH = 58;
   private static final int STARS_X = 126;
   private static final int SCORE_RIGHT_X = 192;
   private static final int ROW_HEIGHT = 12;
   private final Screen parent;
   private List<Text> hoverTooltip = List.of();

   public TopFishScreen(Screen parent) {
      super(Text.literal("Top Team Fish"));
      this.parent = parent;
   }

   private static ItemStack fishStack(String id) {
      Identifier parsed = Identifier.tryParse(id);
      if (parsed != null) {
         Item item = Registries.ITEM.get(parsed);
         if (item != null) {
            return new ItemStack(item);
         }
      }
      return ItemStack.EMPTY;
   }

   @Override
   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      this.hoverTooltip = List.of();
      int left = (this.width - 400) / 2;
      int backgroundTop = (this.height - 260) / 2;
      graphics.drawTexture(BG, left, backgroundTop, 0.0F, 0.0F, 400, 260, 400, 260);
      TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("TEAM RECORDS  •  TOP FISH"), left + 200, backgroundTop + 29, TEXT);

      int detailTop = backgroundTop - 12;
      int listHeadingY = backgroundTop + 45;
      int firstRowY = backgroundTop + LIST_FIRST_ROW_Y;
      graphics.fill(
         left + LIST_PANEL_LEFT,
         backgroundTop + LIST_PANEL_TOP,
         left + LIST_PANEL_RIGHT,
         backgroundTop + LIST_PANEL_BOTTOM,
         869844122
      );
      graphics.fill(left + 228, detailTop + 60, left + 372, detailTop + 250, 584631450);
      TideTextRenderer.drawCentered(
         graphics,
         this.textRenderer,
         Text.literal("TOP 15"),
         left + (LIST_PANEL_LEFT + LIST_PANEL_RIGHT) / 2,
         listHeadingY,
         MUTED
      );
      TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("CANONICAL SPECIMEN"), left + 300, detailTop + 63, MUTED);

      NbtList list = ClientTeamData.get().getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10);
      int visibleCount = Math.min(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_LIMIT, list.size());
      int selectedIndex = visibleCount > 0 ? 0 : -1;
      if (visibleCount > 0
         && mouseX >= left + LIST_ROW_LEFT
         && mouseX < left + LIST_ROW_RIGHT
         && mouseY >= firstRowY
         && mouseY < firstRowY + visibleCount * ROW_HEIGHT) {
         selectedIndex = Math.min(visibleCount - 1, Math.max(0, (mouseY - firstRowY) / ROW_HEIGHT));
      }

      try {
         graphics.enableScissor(
            left + LIST_PANEL_LEFT,
            backgroundTop + LIST_PANEL_TOP,
            left + LIST_PANEL_RIGHT,
            backgroundTop + LIST_PANEL_BOTTOM
         );
         for (int row = 0; row < TOP_FISH_SLOTS; row++) {
            int rowY = firstRowY + row * ROW_HEIGHT;
            if (row < visibleCount && row == selectedIndex) {
               graphics.fill(left + LIST_ROW_LEFT, rowY, left + LIST_ROW_RIGHT, rowY + ROW_HEIGHT, 866633688);
            }

            TideTextRenderer.draw(graphics, this.textRenderer, Integer.toString(row + 1), left + RANK_X, rowY + 2, MUTED);
            if (row >= visibleCount) {
               TideTextRenderer.draw(graphics, this.textRenderer, Text.literal("—"), left + NAME_X, rowY + 2, MUTED);
               continue;
            }

            NbtCompound tag = (NbtCompound)list.get(row);
            ItemStack stack = fishStack(tag.getString("fish"));
            if (!stack.isEmpty()) {
               CanonicalSpecimenStorage.restoreTransferData(tag, stack);
            }
            graphics.drawItem(stack, left + ICON_X, rowY - 2);
            CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElse(null);
            String name = stack.getName().getString();
            FittedText fittedName = FishingUiLayout.ellipsize(name, NAME_WIDTH, this.textRenderer::getWidth);
            TideTextRenderer.draw(graphics, this.textRenderer, fittedName.text(), left + NAME_X, rowY + 2, TEXT);
            String rarity = CanonicalSpecimenPresentation.rarityStars(tag.getInt("fish_stars"));
            TideTextRenderer.draw(graphics, this.textRenderer, rarity, left + STARS_X, rowY + 2, MUTED);
            String score = display == null ? CanonicalSpecimenPresentation.UNAVAILABLE : display.scoreLabel();
            TideTextRenderer.draw(
               graphics,
               this.textRenderer,
               score,
               FishingUiLayout.rightAlignedX(left + SCORE_RIGHT_X, this.textRenderer.getWidth(score)),
               rowY + 2,
               CanonicalSpecimenPresentation.SCORE_COLOR
            );
            if (fittedName.clipped()
               && mouseX >= left + LIST_ROW_LEFT
               && mouseX < left + LIST_ROW_RIGHT
               && mouseY >= rowY
               && mouseY < rowY + ROW_HEIGHT) {
               this.hoverTooltip = List.of(Text.literal(name));
            }
         }
      } finally {
         graphics.disableScissor();
      }

      if (selectedIndex < 0) {
         TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("No specimen selected"), left + 300, detailTop + 151, MUTED);
         return;
      }

      NbtCompound selected = (NbtCompound)list.get(selectedIndex);
      ItemStack stack = fishStack(selected.getString("fish"));
      if (!stack.isEmpty()) {
         CanonicalSpecimenStorage.restoreTransferData(selected, stack);
      }
      CanonicalRecordDisplay display = CanonicalRecordDisplay.from(selected).orElse(null);
      renderFish3D(graphics, stack, left + 300, detailTop + 86);
      this.drawValue(
         graphics,
         stack.getName().getString(),
         stack.getName().getString(),
         left + 238,
         detailTop + 108,
         124,
         TEXT,
         mouseX,
         mouseY
      );

      int x = left + 238;
      int leftColumn = x;
      int rightColumn = x + 64;
      int columnWidth = 60;

      this.section(graphics, "Specimen", x, detailTop + 120);
      String score = display == null ? CanonicalSpecimenPresentation.UNAVAILABLE : display.scoreLabel();
      String rarity = CanonicalSpecimenPresentation.rarityStars(selected.getInt("fish_stars"));
      String percentile = display == null
         ? CanonicalSpecimenPresentation.UNAVAILABLE
         : CanonicalSpecimenPresentation.percentile(display.percentile());
      double lengthValue = display != null && Double.isFinite(display.length()) ? display.length() : selected.getDouble("length");
      String length = CanonicalSpecimenPresentation.length(lengthValue);
      this.label(graphics, "FishScore", leftColumn, detailTop + 132);
      this.label(graphics, "Stars", rightColumn, detailTop + 132);
      this.drawValue(
         graphics,
         score,
         "FishScore: " + score,
         leftColumn,
         detailTop + 141,
         columnWidth,
         CanonicalSpecimenPresentation.SCORE_COLOR,
         mouseX,
         mouseY
      );
      this.drawValue(graphics, rarity, "Stars: " + rarity, rightColumn, detailTop + 141, columnWidth, MUTED, mouseX, mouseY);
      this.label(graphics, "Percentile", leftColumn, detailTop + 151);
      this.label(graphics, "Length", rightColumn, detailTop + 151);
      this.drawValue(graphics, percentile, "Percentile: " + percentile, leftColumn, detailTop + 160, columnWidth, MUTED, mouseX, mouseY);
      this.drawValue(graphics, length, "Length: " + length, rightColumn, detailTop + 160, columnWidth, MUTED, mouseX, mouseY);

      this.section(graphics, "Traits", x, detailTop + 173);
      List<TraitDisplay> traits = display == null
         ? CanonicalSpecimenPresentation.unavailableTraits()
         : display.traits();
      for (int index = 0; index < traits.size(); index++) {
         TraitDisplay trait = traits.get(index);
         int columnX = index % 2 == 0 ? leftColumn : rightColumn;
         int traitRow = index / 2;
         int labelY = detailTop + 185 + traitRow * 19;
         int valueY = labelY + 9;
         this.label(graphics, trait.label(), columnX, labelY);
         this.drawValue(
            graphics,
            trait.value(),
            trait.label() + ": " + trait.value(),
            columnX,
            valueY,
            columnWidth,
            trait.color(),
            mouseX,
            mouseY
         );
      }

      this.section(graphics, "Catch Info", x, detailTop + 226);
      String catcher = selected.getString("catcher_name");
      String catcherLabel = catcher.isBlank() ? CanonicalSpecimenPresentation.UNAVAILABLE : catcher;
      long timestampValue = selected.contains("timestamp", 99) ? selected.getLong("timestamp") : -1L;
      String timestamp = FishingUiFormat.timestamp(timestampValue);
      String date = dateOnly(timestamp);
      this.drawValue(graphics, "By " + catcherLabel, "Caught by: " + catcherLabel, leftColumn, detailTop + 238, 72, MUTED, mouseX, mouseY);
      this.drawValue(graphics, date, "Caught: " + timestamp, x + 76, detailTop + 238, 48, MUTED, mouseX, mouseY);

      if (!this.hoverTooltip.isEmpty()) {
         graphics.drawOrderedTooltip(this.textRenderer, this.hoverTooltip.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
      }
   }

   private void section(DrawContext graphics, String label, int x, int y) {
      TideTextRenderer.draw(graphics, this.textRenderer, label, x, y, TEXT);
      graphics.fill(x, y + 9, x + 124, y + 10, 0x66725F43);
   }

   private void label(DrawContext graphics, String label, int x, int y) {
      FittedText fitted = FishingUiLayout.ellipsize(label, 60, this.textRenderer::getWidth);
      TideTextRenderer.draw(graphics, this.textRenderer, fitted.text(), x, y, MUTED);
   }

   private void drawValue(
      DrawContext graphics,
      String value,
      String tooltip,
      int x,
      int y,
      int width,
      int color,
      int mouseX,
      int mouseY
   ) {
      FittedText fitted = FishingUiLayout.ellipsize(value, width, this.textRenderer::getWidth);
      TideTextRenderer.draw(graphics, this.textRenderer, fitted.text(), x, y, color);
      boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 10;
      if (hovered && (fitted.clipped() || (tooltip != null && !tooltip.equals(value)))) {
         this.hoverTooltip = List.of(Text.literal(tooltip == null ? value : tooltip));
      }
   }

   private static String dateOnly(String timestamp) {
      if (timestamp == null || timestamp.equals(CanonicalSpecimenPresentation.UNAVAILABLE)) {
         return CanonicalSpecimenPresentation.UNAVAILABLE;
      }
      int comma = timestamp.indexOf(',');
      return comma > 0 ? timestamp.substring(0, comma) : timestamp;
   }

   @Override
   public void close() {
      this.client.setScreen(this.parent);
   }

   private static void renderFish3D(DrawContext graphics, ItemStack stack, int x, int y) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null) {
         FishData fish = FishData.getExact(stack).orElse(null);
         if (fish != null) {
            DisplayData display = fish.display().orElse(null);
            if (display != null) {
               EntityType<?> type = display.entityType();
               Entity entity = type == null ? null : type.create(client.world);
               if (entity != null) {
                  Optional<NbtCompound> nbt = display.nbt();
                  nbt.ifPresent(entity::readNbt);
                  SpecimenTransfer.stackToEntity(stack, entity);
                  MatrixStack matrices = graphics.getMatrices();
                  matrices.push();
                  matrices.translate(x, y, 200.0F);
                  float largest = Math.max(Math.max(type.getWidth(), type.getHeight()), 0.5F);
                  float scale = Math.min(58.0F, 46.0F / largest);
                  matrices.scale(scale, -scale, scale);
                  float rotation = (float)(System.currentTimeMillis() % 9000L) / 9000.0F * 6.2831855F;
                  Quaternionf quaternion = new Quaternionf()
                     .rotationY(rotation)
                     .rotateZ((float)Math.toRadians(display.roll() + 90.0F))
                     .rotateX((float)Math.toRadians(display.pitch()))
                     .rotateY((float)Math.toRadians(display.yaw()));
                  matrices.multiply(quaternion);
                  EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
                  Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
                  dispatcher.setRenderShadows(false);
                  dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 0.0F, matrices, consumers, 15728880);
                  consumers.draw();
                  dispatcher.setRenderShadows(true);
                  matrices.pop();
                  return;
               }
            }
         }
      }
      graphics.drawItem(stack, x - 8, y - 8);
   }
}
