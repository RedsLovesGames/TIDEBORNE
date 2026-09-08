/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client.render;

import com.redslovesgames.tidetraits.client.render.MutationRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;

public final class TideborneRenderService {
   private TideborneRenderService() {
   }

   public static void init() {
      MutationRendering.initClient();
   }

   public static Identifier entityTexture(Identifier var0, Entity var1) {
      return MutationRendering.textureFor(var0, var1);
   }

   public static Identifier foreignFishTexture(Identifier var0, LivingEntity var1) {
      return MutationRendering.textureForForeignFish(var0, var1);
   }

   public static void applyLengthScale(LivingEntity var0, MatrixStack var1) {
      MutationRendering.applyLengthScale(var0, var1);
   }

   public static void clearCaches() {
      MutationRendering.clearTextureCache();
   }
}
