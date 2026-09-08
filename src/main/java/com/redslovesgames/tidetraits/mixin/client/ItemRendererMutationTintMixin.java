/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.redslovesgames.tidetraits.client.render.MutationRendering;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.trait.DeterministicValues;
import com.redslovesgames.tidetraits.trait.FishMutation;
import java.util.Optional;
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
      method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V",
         shift = Shift.AFTER
      ),
      require = 1
   )
   private void tideTraits$literalMutationOverlay(
      ItemStack stack,
      ModelTransformationMode transformationMode,
      boolean leftHanded,
      MatrixStack matrices,
      VertexConsumerProvider consumers,
      int light,
      int overlay,
      BakedModel model,
      CallbackInfo ci
   ) {
      Optional<FishMutation> pigmentation = pigmentationMutation(stack);
      if (pigmentation.isPresent()) {
         Identifier filteredTexture = filteredItemTexture(stack, model);
         if (filteredTexture != null) {
            VertexConsumer consumer = consumers.getBuffer(RenderLayer.getEntityTranslucent(filteredTexture));
            Entry entry = matrices.peek();
            int[] white = new int[]{255, 255, 255, 255};
            drawFace(consumer, entry, 0.0F, 0.0F, 1.0F, 1.0F, 0.58F, white, light, overlay, false);
            drawFace(consumer, entry, 0.0F, 0.0F, 1.0F, 1.0F, -0.58F, white, light, overlay, true);
            return;
         }
      }

      FishMutation mutation = visualMutation(stack);
      Identifier mask = maskTexture(stack, mutation);
      if (mask != null) {
         int[] color = maskColor(mutation);
         VertexConsumer consumer = consumers.getBuffer(RenderLayer.getEntityTranslucent(mask));
         Entry entry = matrices.peek();
         drawFace(consumer, entry, 0.0F, 0.0F, 1.0F, 1.0F, 0.58F, color, light, overlay, false);
         drawFace(consumer, entry, 0.0F, 0.0F, 1.0F, 1.0F, -0.58F, color, light, overlay, true);
      }
   }

   private static Optional<FishMutation> pigmentationMutation(ItemStack stack) {
      Optional<FishMutation> canonical = FishMutation.bySerializedName(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION))
         .filter(mutation -> mutation == FishMutation.ALBINO || mutation == FishMutation.IRIDESCENT);
      if (canonical.isPresent()) {
         return canonical;
      }
      return FishMutation.bySerializedName(stack.get(TideTraitsComponents.MUTATION))
         .filter(mutation -> mutation == FishMutation.ALBINO || mutation == FishMutation.IRIDESCENT);
   }

   private static FishMutation visualMutation(ItemStack stack) {
      Optional<FishMutation> condition = FishMutation.bySerializedName(stack.get(TideTraitsComponents.SPECIMEN_CONDITION))
         .filter(mutation -> mutation == FishMutation.SCARRED || mutation == FishMutation.PARASITE_RIDDEN);
      if (condition.isPresent()) {
         return condition.get();
      }
      return FishMutation.bySerializedName(stack.getOrDefault(TideTraitsComponents.MUTATION, "normal")).orElse(FishMutation.NORMAL);
   }

   private static Identifier filteredItemTexture(ItemStack stack, BakedModel model) {
      if (model == null || model.getParticleSprite() == null || model.getParticleSprite().getContents() == null) {
         return null;
      }
      Identifier spriteId = model.getParticleSprite().getContents().getId();
      if (spriteId == null || ("minecraft".equals(spriteId.getNamespace()) && "missingno".equals(spriteId.getPath()))) {
         return null;
      }
      String path = spriteId.getPath();
      Identifier source = Identifier.of(
         spriteId.getNamespace(),
         path.startsWith("textures/") ? path : "textures/" + path + (path.endsWith(".png") ? "" : ".png")
      );
      return MutationRendering.textureForItem(source, stack);
   }

   private static Identifier maskTexture(ItemStack stack, FishMutation mutation) {
      Long canonicalSeed = stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED);
      Long legacySeed = stack.get(TideTraitsComponents.MUTATION_SEED);
      long seed = canonicalSeed != null ? canonicalSeed : (legacySeed != null ? legacySeed : 0L);
      long mixed = DeterministicValues.mix64(seed ^ VARIANT_SALT ^ mutation.ordinal());
      String file;
      if (mutation == FishMutation.SCARRED) {
         int variant = Math.floorMod(mixed, 4) + 1;
         file = "scar_0" + variant + ".png";
      } else if (mutation == FishMutation.PARASITE_RIDDEN) {
         int variant = Math.floorMod(mixed, 4) + 1;
         file = "parasite_0" + variant + ".png";
      } else {
         return null;
      }
      return Identifier.of("tide_traits", "textures/entity/traits/masks/" + file);
   }

   private static int[] maskColor(FishMutation mutation) {
      if (mutation == FishMutation.SCARRED) {
         return new int[]{255, 48, 42, 255};
      }
      return new int[]{185, 220, 95, 255};
   }

   private static void drawFace(
      VertexConsumer consumer,
      Entry entry,
      float minX,
      float minY,
      float maxX,
      float maxY,
      float z,
      int[] color,
      int light,
      int overlay,
      boolean back
   ) {
      if (!back) {
         vertex(consumer, entry, minX, maxY, z, color, 0.0F, 1.0F, overlay, light, 0.0F, 0.0F, 1.0F);
         vertex(consumer, entry, maxX, maxY, z, color, 1.0F, 1.0F, overlay, light, 0.0F, 0.0F, 1.0F);
         vertex(consumer, entry, maxX, minY, z, color, 1.0F, 0.0F, overlay, light, 0.0F, 0.0F, 1.0F);
         vertex(consumer, entry, minX, minY, z, color, 0.0F, 0.0F, overlay, light, 0.0F, 0.0F, 1.0F);
      } else {
         vertex(consumer, entry, minX, minY, z, color, 0.0F, 0.0F, overlay, light, 0.0F, 0.0F, -1.0F);
         vertex(consumer, entry, maxX, minY, z, color, 1.0F, 0.0F, overlay, light, 0.0F, 0.0F, -1.0F);
         vertex(consumer, entry, maxX, maxY, z, color, 1.0F, 1.0F, overlay, light, 0.0F, 0.0F, -1.0F);
         vertex(consumer, entry, minX, maxY, z, color, 0.0F, 1.0F, overlay, light, 0.0F, 0.0F, -1.0F);
      }
   }

   private static void vertex(
      VertexConsumer consumer,
      Entry entry,
      float x,
      float y,
      float z,
      int[] color,
      float u,
      float v,
      int overlay,
      int light,
      float normalX,
      float normalY,
      float normalZ
   ) {
      consumer.vertex(entry, x, y, z)
         .color(color[0], color[1], color[2], color[3])
         .texture(u, v)
         .overlay(overlay)
         .light(light)
         .normal(entry, normalX, normalY, normalZ);
   }
}
