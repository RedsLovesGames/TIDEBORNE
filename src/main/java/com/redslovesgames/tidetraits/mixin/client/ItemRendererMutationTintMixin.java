/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.trait.DeterministicValues;
import com.redslovesgames.tidetraits.trait.FishMutation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMutationTintMixin {
   private static final long VARIANT_SALT = -3335678366873096957L;

   @Inject(
      method = "renderItem(Lnet/minecraft/ItemStack;Lnet/minecraft/ModelTransformationMode;ZLnet/minecraft/MatrixStack;Lnet/minecraft/VertexConsumerProvider;IILnet/minecraft/BakedModel;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/ItemRenderer;renderBakedItemModel(Lnet/minecraft/BakedModel;Lnet/minecraft/ItemStack;IILnet/minecraft/MatrixStack;Lnet/minecraft/VertexConsumer;)V",
         shift = Shift.AFTER
      ),
      require = 1
   )
   private void tideTraits$literalMaskOverlay(
      ItemStack var1, ModelTransformationMode var2, boolean var3, MatrixStack var4, VertexConsumerProvider var5, int var6, int var7, BakedModel var8, CallbackInfo var9
   ) {
      FishMutation var10 = FishMutation.bySerializedName((String)var1.getOrDefault(TideTraitsComponents.MUTATION, "normal")).orElse(FishMutation.NORMAL);
      Identifier var11 = maskTexture(var1, var10);
      if (var11 != null) {
         int[] var12 = maskColor(var10);
         VertexConsumer var13 = var5.getBuffer(RenderLayer.getEntityTranslucent(var11));
         Entry var14 = var4.peek();
         drawFace(var13, var14, 0.0F, 0.0F, 1.0F, 1.0F, 0.58F, var12, var6, var7, false);
         drawFace(var13, var14, 0.0F, 0.0F, 1.0F, 1.0F, -0.58F, var12, var6, var7, true);
      }
   }

   private static Identifier maskTexture(ItemStack var0, FishMutation var1) {
      long var2 = (Long)var0.getOrDefault(TideTraitsComponents.MUTATION_SEED, 0L);
      long var4 = DeterministicValues.mix64(var2 ^ -3335678366873096957L ^ var1.ordinal());
      String var6;
      if (var1 == FishMutation.SCARRED) {
         int var7 = Math.floorMod(var4, 4) + 1;
         var6 = "scar_0" + var7 + ".png";
      } else if (var1 == FishMutation.PARASITE_RIDDEN) {
         int var8 = Math.floorMod(var4, 4) + 1;
         var6 = "parasite_0" + var8 + ".png";
      } else {
         if (var1 != FishMutation.IRIDESCENT) {
            return null;
         }

         int var9 = Math.floorMod(var4, 2) + 1;
         var6 = "sparkle_0" + var9 + ".png";
      }

      return Identifier.of("tide_traits", "textures/entity/traits/masks/" + var6);
   }

   private static int[] maskColor(FishMutation var0) {
      if (var0 == FishMutation.SCARRED) {
         return new int[]{255, 48, 42, 255};
      } else {
         return var0 == FishMutation.PARASITE_RIDDEN ? new int[]{185, 220, 95, 255} : new int[]{235, 250, 255, 255};
      }
   }

   private static void drawFace(
      VertexConsumer var0, Entry var1, float var2, float var3, float var4, float var5, float var6, int[] var7, int var8, int var9, boolean var10
   ) {
      if (!var10) {
         vertex(var0, var1, var2, var5, var6, var7, 0.0F, 1.0F, var9, var8, 0.0F, 0.0F, 1.0F);
         vertex(var0, var1, var4, var5, var6, var7, 1.0F, 1.0F, var9, var8, 0.0F, 0.0F, 1.0F);
         vertex(var0, var1, var4, var3, var6, var7, 1.0F, 0.0F, var9, var8, 0.0F, 0.0F, 1.0F);
         vertex(var0, var1, var2, var3, var6, var7, 0.0F, 0.0F, var9, var8, 0.0F, 0.0F, 1.0F);
      } else {
         vertex(var0, var1, var2, var3, var6, var7, 0.0F, 0.0F, var9, var8, 0.0F, 0.0F, -1.0F);
         vertex(var0, var1, var4, var3, var6, var7, 1.0F, 0.0F, var9, var8, 0.0F, 0.0F, -1.0F);
         vertex(var0, var1, var4, var5, var6, var7, 1.0F, 1.0F, var9, var8, 0.0F, 0.0F, -1.0F);
         vertex(var0, var1, var2, var5, var6, var7, 0.0F, 1.0F, var9, var8, 0.0F, 0.0F, -1.0F);
      }
   }

   private static void vertex(
      VertexConsumer var0,
      Entry var1,
      float var2,
      float var3,
      float var4,
      int[] var5,
      float var6,
      float var7,
      int var8,
      int var9,
      float var10,
      float var11,
      float var12
   ) {
      var0.vertex(var1, var2, var3, var4)
         .color(var5[0], var5[1], var5[2], var5[3])
         .texture(var6, var7)
         .overlay(var8)
         .light(var9)
         .normal(var1, var10, var11, var12);
   }
}
