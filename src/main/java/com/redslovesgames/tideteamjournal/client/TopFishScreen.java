/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.data.fishing.DisplayData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.util.TideUtils;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Quaternionf;

public final class TopFishScreen extends Screen {
   private final Screen parent;
   private static final Identifier BG = Identifier.of("tide", "textures/gui/journal/journal_bg.png");

   public TopFishScreen(Screen var1) {
      super(Text.literal("Top Team Fish"));
      this.parent = var1;
   }

   private static ItemStack fishStack(String var0) {
      Identifier var1 = Identifier.tryParse(var0);
      if (var1 != null) {
         Item var2 = (Item)Registries.ITEM.get(var1);
         if (var2 != null) {
            return new ItemStack(var2);
         }
      }

      return ItemStack.EMPTY;
   }

   private static String stars(int var0) {
      return "\u2605".repeat(Math.max(0, var0));
   }

   private static String prettyMutation(String var0) {
      return TraitAxesRuntime.titleCase(var0);
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      int var5 = (this.width - 400) / 2;
      int var6 = (this.height - 260) / 2;
      var1.drawTexture(BG, var5, var6, 0.0F, 0.0F, 400, 260, 400, 260);
      TideTextRenderer.drawCentered(var1, this.textRenderer, Text.literal("TEAM RECORDS  \u2022  TOP FISH"), var5 + 200, var6 + 29, 5477982);
      var1.fill(var5 + 28, var6 + 60, var5 + 220, var6 + 225, 869844122);
      var1.fill(var5 + 228, var6 + 60, var5 + 372, var6 + 225, 584631450);
      TideTextRenderer.drawCentered(var1, this.textRenderer, Text.literal("TOP 12"), var5 + 124, var6 + 63, 6650722);
      TideTextRenderer.drawCentered(var1, this.textRenderer, Text.literal("HOVER PREVIEW"), var5 + 300, var6 + 63, 6650722);
      NbtList var7 = ClientTeamData.get().getList("top_fish", 10);
      NbtCompound var8 = null;
      int var9 = 0;

      for (Iterator var10 = var7.iterator(); var10.hasNext() && var9 < 12; var9++) {
         NbtCompound var11 = (NbtCompound)var10.next();
         int var12 = var6 + 73 + var9 * 12;
         if (var2 >= var5 + 34 && var2 < var5 + 214 && var3 >= var12 && var3 < var12 + 12) {
            var8 = var11;
            var1.fill(var5 + 34, var12, var5 + 214, var12 + 12, 866633688);
         }

         ItemStack var13 = fishStack(var11.getString("fish"));
         var1.drawItem(var13, var5 + 38, var12 + -2);
         String var14 = var13.getName().getString();
         String var15 = var9 + 1 + ". " + var14 + "  " + stars(var11.getInt("fish_stars")) + "  " + var11.getInt("fish_score");
         TideTextRenderer.draw(var1, this.textRenderer, var15, var5 + 58, var12 + 2, 5477982);
      }

      if (var8 == null) {
         if (var7.isEmpty()) {
            return;
         }

         var8 = (NbtCompound)var7.iterator().next();
      }

      ItemStack var16 = fishStack(var8.getString("fish"));
      renderFish3D(var1, var16, var5 + 300, var6 + 100);
      TideTextRenderer.drawCentered(var1, this.textRenderer, var16.getName(), var5 + 300, var6 + 134, 5477982);
      String var18 = "Score " + var8.getInt("fish_score");
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 146, 6650722);
      var18 = "Stars " + stars(var8.getInt("fish_stars"));
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 157, 6650722);
      var18 = "Percentile " + Math.round(var8.getDouble("percentile") * 10.0) / 10.0 + "%";
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 168, 6650722);
      var18 = "Body Type " + prettyMutation(var8.getString("body_type"));
      TideTextRenderer.draw(var1, super.textRenderer, var18, var5 + 240, var6 + 179, 6650722);
      var18 = "Condition " + prettyMutation(var8.getString("mutation"));
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 190, 6650722);
      var18 = "Caught by " + var8.getString("catcher_name");
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 212, 6650722);
      var18 = TideUtils.getFormattedLength(var8.getDouble("length")).getString();
      var18 = "Length " + var18;
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 201, 6650722);
      var18 = new SimpleDateFormat("M/d/yy").format(new Date(var8.getLong("timestamp")));
      var18 = "Caught " + var18;
      TideTextRenderer.draw(var1, this.textRenderer, var18, var5 + 240, var6 + 223, 6650722);
   }

   public void close() {
      this.client.setScreen(this.parent);
   }

   private static void renderFish3D(DrawContext var0, ItemStack var1, int var2, int var3) {
      MinecraftClient var4 = MinecraftClient.getInstance();
      if (var4.world != null) {
         FishData var5 = (FishData)FishData.getExact(var1).orElse(null);
         if (var5 != null) {
            DisplayData var6 = (DisplayData)var5.display().orElse(null);
            if (var6 != null) {
               EntityType var7 = var6.entityType();
               if (var7 != null) {
                  Entity var8 = var7.create(var4.world);
                  if (var8 != null) {
                     Optional var9 = var6.nbt();
                     if (var9.isPresent()) {
                        var8.readNbt((NbtCompound)var9.get());
                     }

                     MatrixStack var10 = var0.getMatrices();
                     var10.push();
                     var10.translate(var2, var3, 200.0F);
                     float var11 = Math.max(Math.max(var7.getWidth(), var7.getHeight()), 0.5F);
                     float var12 = Math.min(58.0F, 46.0F / var11);
                     var10.scale(var12, -var12, var12);
                     float var13 = (float)(System.currentTimeMillis() % 9000L) / 9000.0F * 6.2831855F;
                     Quaternionf var14 = new Quaternionf()
                        .rotationY(var13)
                        .rotateZ((float)Math.toRadians(var6.roll() + 90.0F))
                        .rotateX((float)Math.toRadians(var6.pitch()))
                        .rotateY((float)Math.toRadians(var6.yaw()));
                     var10.multiply(var14);
                     EntityRenderDispatcher var15 = var4.getEntityRenderDispatcher();
                     Immediate var16 = var4.getBufferBuilders().getEntityVertexConsumers();
                     var15.setRenderShadows(false);
                     var15.render(var8, 0.0, 0.0, 0.0, 0.0F, 0.0F, var10, var16, 15728880);
                     var16.draw();
                     var15.setRenderShadows(true);
                     var10.pop();
                     return;
                  }
               }
            }
         }
      }

      var0.drawItem(var1, var2 - 8, var3 - 8);
   }
}
