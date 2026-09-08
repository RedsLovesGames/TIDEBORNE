/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.ecosystem;

import com.redslovesgames.tideborne.ecosystem.ChumBucketItem;
import com.redslovesgames.tideborne.registry.TideboundEntities;
import com.redslovesgames.tideborne.registry.TideboundItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;

public final class ChumProjectileEntity extends ThrownItemEntity {
   private boolean refundRequired;
   private boolean resolved;

   public ChumProjectileEntity(EntityType<ChumProjectileEntity> type, World level) {
      super(type, level);
   }

   public ChumProjectileEntity(World level, LivingEntity owner) {
      super(TideboundEntities.CHUM_PROJECTILE, owner, level);
   }

   protected Item getDefaultItem() {
      return TideboundItems.CHUM_BUCKET;
   }

   public void setRefundRequired(boolean refundRequired) {
      this.refundRequired = refundRequired;
   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && !this.resolved) {
         if (this.isTouchingWater()) {
            this.resolveAtWater();
         } else if (this.age > 100) {
            this.returnChum();
         }
      }
   }

   protected void onCollision(HitResult hitResult) {
      if (!this.getWorld().isClient() && !this.resolved && !this.isTouchingWater()) {
         this.returnChum();
      } else {
         super.onCollision(hitResult);
      }
   }

   private void resolveAtWater() {
      this.resolved = true;
      PlayerEntity player = this.getOwner() instanceof PlayerEntity owner ? owner : null;
      if (ChumBucketItem.activate((ServerWorld)this.getWorld(), this.getBlockPos(), player)) {
         this.returnItem(Items.BUCKET);
      } else {
         this.returnItem(TideboundItems.CHUM_BUCKET);
      }

      this.discard();
   }

   private void returnChum() {
      if (!this.resolved) {
         this.resolved = true;
         PlayerEntity player = this.getOwner() instanceof PlayerEntity owner ? owner : null;
         if (player != null) {
            player.sendMessage(Text.translatable("message.tidebound_compatibility.chum_returned"), true);
         }

         this.returnItem(TideboundItems.CHUM_BUCKET);
         this.discard();
      }
   }

   private void returnItem(Item item) {
      if (this.refundRequired) {
         ItemStack result = new ItemStack(item);
         if (!(this.getOwner() instanceof PlayerEntity player && player.isAlive() && player.getInventory().insertStack(result))) {
            this.dropStack(result);
         }
      }
   }
}
