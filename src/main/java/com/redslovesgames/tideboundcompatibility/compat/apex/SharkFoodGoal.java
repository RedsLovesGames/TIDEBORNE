/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.compat.apex;

import com.acorsicanfrog.apexwaters.config.ApexWatersConfig;
import com.acorsicanfrog.apexwaters.entity.GreatWhiteSharkEntity;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.EnumSet;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.ai.goal.Goal.Control;

public final class SharkFoodGoal extends Goal {
   private final GreatWhiteSharkEntity shark;
   private Entity entityTarget;
   private SharkScentManager.ScentZone zoneTarget;
   private int activeTicks;

   public SharkFoodGoal(GreatWhiteSharkEntity shark) {
      this.shark = shark;
      this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
   }

   public boolean canStart() {
      TideboundConfig.Values config = TideboundConfig.get();
      if (config.enableApexCompat
         && config.enableSharkFishAttraction
         && this.shark.isTouchingWater()
         && this.shark.getTarget() == null
         && this.shark.getHunger() <= config.maximumFullnessForFoodTargeting
         && !ApexCompat.preyCooldownActive(this.shark)
         && (this.shark.age + this.shark.getId()) % config.sharkScanInterval == 0) {
         double radius = config.sharkFishDetectionRadius;
         double bestScore = 0.0;
         this.entityTarget = null;
         this.zoneTarget = null;

         for (ItemEntity item : this.shark
            .getWorld()
            .getEntitiesByClass(ItemEntity.class, this.shark.getBoundingBox().expand(radius), candidate -> candidate.isAlive() && candidate.isTouchingWater())) {
            double scent = SharkScentManager.fishScent(item.getStack());
            double score = this.score(scent, item.getPos());
            if (score > bestScore) {
               bestScore = score;
               this.entityTarget = item;
            }
         }

         SharkScentManager.ScentZone zone = SharkScentManager.findBestZone((ServerWorld)this.shark.getWorld(), this.shark.getPos(), radius);
         if (zone != null) {
            double score = this.score(zone.strength(), zone.position());
            if (score > bestScore) {
               this.entityTarget = null;
               this.zoneTarget = zone;
            }
         }

         Vec3d target = this.targetPosition();
         if (target == null) {
            return false;
         }

         Path path = this.shark.getNavigation().findPathTo(BlockPos.ofFloored(target), 0);
         return path != null;
      } else {
         return false;
      }
   }

   public void start() {
      this.activeTicks = 0;
      this.moveToTarget();
   }

   public boolean shouldContinue() {
      Vec3d target = this.targetPosition();
      return target != null
         && ++this.activeTicks < 300
         && this.shark.getTarget() == null
         && this.shark.getHunger() <= TideboundConfig.get().maximumFullnessForFoodTargeting
         && this.shark.getPos().squaredDistanceTo(target) <= Math.pow(TideboundConfig.get().sharkFishDetectionRadius * 1.5, 2.0);
   }

   public void tick() {
      Vec3d target = this.targetPosition();
      if (target != null) {
         this.shark.getLookControl().lookAt(target.x, target.y, target.z);
         if (this.activeTicks % 10 == 0) {
            this.moveToTarget();
         }

         double distanceSqr = this.shark.getPos().squaredDistanceTo(target);
         if (this.entityTarget instanceof ItemEntity item && distanceSqr <= 6.25) {
            this.consumeItem(item);
         } else if (this.zoneTarget != null && distanceSqr <= 16.0) {
            this.satiate(Math.max(5, (int)Math.round(this.zoneTarget.strength() * 2.0)));
            ApexCompat.setPreyCooldown(this.shark, 100L);
            this.stop();
         }
      }
   }

   public void stop() {
      this.shark.getNavigation().stop();
      this.entityTarget = null;
      this.zoneTarget = null;
      this.activeTicks = 0;
   }

   private void consumeItem(ItemEntity item) {
      ItemStack stack = item.getStack();
      double scent = SharkScentManager.fishScent(stack.copyWithCount(1));
      stack.decrement(1);
      if (stack.isEmpty()) {
         item.discard();
      } else {
         item.setStack(stack);
      }

      this.satiate(Math.max(1, (int)Math.round(TideboundConfig.get().sharkFoodSatiation * scent)));
      this.playChomp(item.getPos());
      ApexCompat.setPreyCooldown(this.shark, 80L);
      this.stop();
   }

   private void satiate(int amount) {
      this.shark.setHunger(Math.min(ApexWatersConfig.get().hungerMax(), this.shark.getHunger() + amount));
   }

   private void playChomp(Vec3d position) {
      if (this.shark.getWorld() instanceof ServerWorld level) {
         level.spawnParticles(ParticleTypes.SPLASH, position.x, position.y, position.z, 18, 0.7, 0.35, 0.7, 0.12);
         level.playSound(null, BlockPos.ofFloored(position), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.HOSTILE, 1.8F, 0.65F);
      }
   }

   private void moveToTarget() {
      Vec3d target = this.targetPosition();
      if (target != null) {
         this.shark.getNavigation().startMovingTo(target.x, target.y, target.z, 1.25);
      }
   }

   private Vec3d targetPosition() {
      if (this.entityTarget != null && this.entityTarget.isAlive()) {
         return this.entityTarget.getPos();
      } else {
         return this.zoneTarget == null ? null : this.zoneTarget.position();
      }
   }

   private double score(double scent, Vec3d position) {
      return scent / (1.0 + this.shark.getPos().distanceTo(position) * 0.08);
   }
}
