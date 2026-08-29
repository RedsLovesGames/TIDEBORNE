/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.data.fishing.DisplayData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.util.TideUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
         graphics.drawItem(stack, left + 38, rowY - 2);
         CanonicalRecordDisplay display = CanonicalRecordDisplay.from(tag).orElse(null);
         String score = display == null ? "--" : display.scoreLabel();
         String line = (row + 1) + ". " + stack.getName().getString() + "  " + stars(tag.getInt("fish_stars")) + "  " + score;
         TideTextRenderer.draw(graphics, this.textRenderer, this.textRenderer.trimToWidth(line, 154), left + 58, rowY + 2, TEXT);
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
      TideTextRenderer.drawCentered(graphics, this.textRenderer, stack.getName(), left + 300, top + 126, TEXT);

      int x = left + 238;
      int y = top + 139;
      String score = display == null ? "--" : display.scoreLabel();
      TideTextRenderer.draw(graphics, this.textRenderer, "FishScore " + score, x, y, SCORE_COLOR);
      TideTextRenderer.draw(graphics, this.textRenderer, "Stars " + stars(selected.getInt("fish_stars")), x, y + 10, MUTED);
      String percentile = display != null && Double.isFinite(display.percentile()) ? String.format(java.util.Locale.ROOT, "P%.1f", display.percentile()) : "--";
      TideTextRenderer.draw(graphics, this.textRenderer, "Percentile " + percentile, x, y + 20, MUTED);
      TideTextRenderer.draw(graphics, this.textRenderer, "Body " + (display == null ? "--" : display.bodyTypeLabel()), x, y + 30, BODY_COLOR);
      TideTextRenderer.draw(graphics, this.textRenderer, "Condition " + (display == null ? "--" : display.conditionLabel()), x, y + 40, CONDITION_COLOR);
      TideTextRenderer.draw(graphics, this.textRenderer, "Pigment " + (display == null ? "--" : display.pigmentationLabel()), x, y + 50, PIGMENT_COLOR);
      TideTextRenderer.draw(graphics, this.textRenderer, "Quality " + (display == null ? "--" : display.qualityLabel()), x, y + 60, QUALITY_COLOR);

      double lengthValue = display != null && Double.isFinite(display.length()) ? display.length() : selected.getDouble("length");
      String length = lengthValue > 0.0 ? TideUtils.getFormattedLength(lengthValue).getString() : "--";
      TideTextRenderer.draw(graphics, this.textRenderer, "Length " + length, x, y + 70, MUTED);
      String catcher = selected.getString("catcher_name");
      TideTextRenderer.draw(graphics, this.textRenderer, this.textRenderer.trimToWidth("By " + catcher, 126), x, y + 80, MUTED);
      if (selected.contains("timestamp", 99)) {
         String date = new SimpleDateFormat("M/d/yy").format(new Date(selected.getLong("timestamp")));
         TideTextRenderer.draw(graphics, this.textRenderer, "Caught " + date, x, y + 90, MUTED);
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
