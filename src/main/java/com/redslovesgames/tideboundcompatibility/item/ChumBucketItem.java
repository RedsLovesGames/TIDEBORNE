/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.item;

import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.compat.apex.SharkScentManager;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.entity.ChumProjectileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.item.Item.class_1793;

public final class ChumBucketItem extends Item {
   public ChumBucketItem(class_1793 properties) {
      super(properties);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      return this.throwChum(context.getWorld(), context.getPlayer(), context.getHand(), context.getStack());
   }

   public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
      ActionResult result = this.throwChum(level, player, hand, player.getStackInHand(hand));
      return new TypedActionResult(result, player.getStackInHand(hand));
   }

   private ActionResult throwChum(World level, PlayerEntity player, Hand hand, ItemStack stack) {
      if (player == null) {
         return ActionResult.PASS;
      }

      if (level.isClient()) {
         return ActionResult.SUCCESS;
      }

      ChumProjectileEntity projectile = new ChumProjectileEntity(level, player);
      projectile.setItem(stack.copyWithCount(1));
      projectile.setRefundRequired(!player.getAbilities().creativeMode);
      projectile.setPosition(player.getX(), player.getEyeY() - 0.1, player.getZ());
      projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.25F, 1.0F);
      level.spawnEntity(projectile);
      level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.7F, 0.8F);
      if (!player.getAbilities().creativeMode) {
         player.setStackInHand(hand, ItemStack.EMPTY);
      }

      return ActionResult.SUCCESS;
   }

   public static boolean activate(ServerWorld level, BlockPos pos, PlayerEntity player) {
      TideboundConfig.Values config = TideboundConfig.get();
      if (config.enableChum && config.enableApexCompat && TideboundCompatibility.isApexLoaded()) {
         if (!level.getBiome(pos).isIn(BiomeTags.IS_OCEAN)) {
            if (player != null) {
               player.sendMessage(Text.translatable("message.tidebound_compatibility.chum_ocean_only"), true);
            }

            return false;
         } else {
            Vec3d center = Vec3d.ofCenter(pos);
            SharkScentManager.addChumZone(level, center, config.chumRadius, config.chumDuration * 20L, player == null ? null : player.getUuid());
            level.spawnParticles(ParticleTypes.BUBBLE, center.x, center.y, center.z, 32, 1.5, 0.6, 1.5, 0.04);
            level.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundCategory.PLAYERS, 1.0F, 0.75F);
            return true;
         }
      } else {
         if (player != null) {
            player.sendMessage(Text.translatable("message.tidebound_compatibility.chum_disabled"), true);
         }

         return false;
      }
   }
}
