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
import java.util.Iterator;
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
   private static final int BODY_COLOR = 0xB36CE2;
   private static final int CONDITION_COLOR = 0xD36B5D;
   private static final int PIGMENT_COLOR = 0x4FAFD6;
   private static final int QUALITY_COLOR = 0xD6A94F;
   private static final int SCORE_COLOR = 0x43A8D8;
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

   private static String stars(int count) {
      return "★".repeat(Math.max(0, count));
   }

   @Override
   public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      this.hoverTooltip = List.of();
      int left = (this.width - 400) / 2;
      int top = (this.height - 260) / 2;
      graphics.drawTexture(BG, left, top, 0.0F, 0.0F, 400, 260, 400, 260);
      TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("TEAM RECORDS  •  TOP FISH"), left + 200, top + 29, TEXT);
      graphics.fill(left + 28, top + 60, left + 220, top + 225, 869844122);
      graphics.fill(left + 228, top + 60, left + 372, top + 225, 584631450);
      TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("TOP 12"), left + 124, top + 63, MUTED);
      TideTextRenderer.drawCentered(graphics, this.textRenderer, Text.literal("CANONICAL SPECIMEN"), left + 300, top + 63, MUTED);

      NbtList list = ClientTeamData.get().getList("top_fish", 10);
      NbtCompound selected = null;
      int row = 0;
      for (Iterator<NbtCompound> iterator = list.stream().map(element -> (NbtCompound)element).iterator(); iterator.hasNext() && row < 12; row++) {
         NbtCompound tag = iterator.next();
         int rowY = top + 73 + row * 12;
         if (mouseX >= left + 34 && mouseX < left + 214 && mouseY >= rowY && mouseY < rowY + 12) {
            selected = tag;
            graphics.fill(left + 34, rowY, left + 214, rowY + 12, 866633688);
         }

         ItemStack stack = fishStack(tag.getString("fish"));
         TideTextRenderer.draw(graphics, this.textRenderer, Integer.toString(row + 1), left + 36, rowY + 2, MUTED);
         graphics.drawItem(stack, left + 49, rowY - 2);
         CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElse(null);
         String name = stack.getName().getString();
         FittedText fittedName = FishingUiLayout.ellipsize(name, 73, this.textRenderer::getWidth);
         TideTextRenderer.draw(graphics, this.textRenderer, fittedName.text(), left + 67, rowY + 2, TEXT);
         String rarity = stars(tag.getInt("fish_stars"));
         TideTextRenderer.draw(graphics, this.textRenderer, rarity, left + 143, rowY + 2, MUTED);
         String score = display == null ? FishingUiFormat.UNAVAILABLE : display.scoreLabel();
         TideTextRenderer.draw(
            graphics,
            this.textRenderer,
            score,
            FishingUiLayout.rightAlignedX(left + 211, this.textRenderer.getWidth(score)),
            rowY + 2,
            SCORE_COLOR
         );
         if (fittedName.clipped() && mouseX >= left + 34 && mouseX < left + 214 && mouseY >= rowY && mouseY < rowY + 12) {
            this.hoverTooltip = List.of(Text.literal(name));
         }
      }

      if (selected == null) {
         if (list.isEmpty()) {
            return;
         }
         selected = (NbtCompound)list.get(0);
      }

      ItemStack stack = fishStack(selected.getString("fish"));
      CanonicalRecordDisplay display = CanonicalRecordDisplay.from(selected).orElse(null);
      renderFish3D(graphics, stack, left + 300, top + 96);
      this.drawFitted(graphics, stack.getName().getString(), left + 238, top + 126, 124, TEXT, mouseX, mouseY);

      int x = left + 238;
      int y = top + 139;
      this.section(graphics, "Specimen", x, y);
      String score = display == null ? FishingUiFormat.UNAVAILABLE : display.scoreLabel();
      this.drawFitted(graphics, "FishScore " + score, x, y + 12, 66, SCORE_COLOR, mouseX, mouseY);
      this.drawFitted(graphics, "Stars " + stars(selected.getInt("fish_stars")), x + 70, y + 12, 58, MUTED, mouseX, mouseY);
      String percentile = display == null ? FishingUiFormat.UNAVAILABLE : FishingUiFormat.percentile(display.percentile());
      this.drawFitted(graphics, "Percentile " + percentile, x, y + 22, 66, MUTED, mouseX, mouseY);
      double lengthValue = display != null && Double.isFinite(display.length()) ? display.length() : selected.getDouble("length");
      this.drawFitted(graphics, "Length " + FishingUiFormat.length(lengthValue), x + 70, y + 22, 58, MUTED, mouseX, mouseY);

      this.section(graphics, "Traits", x, y + 35);
      this.drawFitted(graphics, "Body " + (display == null ? FishingUiFormat.UNAVAILABLE : display.bodyTypeLabel()), x, y + 47, 64, BODY_COLOR, mouseX, mouseY);
      this.drawFitted(graphics, "Condition " + (display == null ? FishingUiFormat.UNAVAILABLE : display.conditionLabel()), x + 67, y + 47, 61, CONDITION_COLOR, mouseX, mouseY);
      this.drawFitted(graphics, "Pigment " + (display == null ? FishingUiFormat.UNAVAILABLE : display.pigmentationLabel()), x, y + 57, 64, PIGMENT_COLOR, mouseX, mouseY);
      this.drawFitted(graphics, "Quality " + (display == null ? FishingUiFormat.UNAVAILABLE : display.qualityLabel()), x + 67, y + 57, 61, QUALITY_COLOR, mouseX, mouseY);

      this.section(graphics, "Catch Info", x, y + 70);
      String catcher = selected.getString("catcher_name");
      this.drawFitted(graphics, "By " + (catcher.isBlank() ? FishingUiFormat.UNAVAILABLE : catcher), x, y + 82, 64, MUTED, mouseX, mouseY);
      this.drawFitted(
         graphics,
         "Caught " + FishingUiFormat.timestamp(selected.contains("timestamp", 99) ? selected.getLong("timestamp") : -1L),
         x + 67,
         y + 82,
         61,
         MUTED,
         mouseX,
         mouseY
      );
      if (!this.hoverTooltip.isEmpty()) {
         graphics.drawOrderedTooltip(this.textRenderer, this.hoverTooltip.stream().map(Text::asOrderedText).toList(), mouseX, mouseY);
      }
   }

   private void section(DrawContext graphics, String label, int x, int y) {
      TideTextRenderer.draw(graphics, this.textRenderer, label, x, y, TEXT);
      graphics.fill(x, y + 10, x + 128, y + 11, 0x66725F43);
   }

   private void drawFitted(DrawContext graphics, String value, int x, int y, int width, int color, int mouseX, int mouseY) {
      FittedText fitted = FishingUiLayout.ellipsize(value, width, this.textRenderer::getWidth);
      TideTextRenderer.draw(graphics, this.textRenderer, fitted.text(), x, y, color);
      if (fitted.clipped() && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 10) {
         this.hoverTooltip = List.of(Text.literal(value));
      }
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
