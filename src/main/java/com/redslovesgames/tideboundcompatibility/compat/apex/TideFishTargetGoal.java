/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.compat.apex;

import com.acorsicanfrog.apexwaters.entity.GreatWhiteSharkEntity;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.Comparator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

public final class TideFishTargetGoal extends TrackTargetGoal {
   private final GreatWhiteSharkEntity shark;
   private LivingEntity candidate;

   public TideFishTargetGoal(GreatWhiteSharkEntity shark) {
      super(shark, false, false);
      this.shark = shark;
   }

   public boolean canStart() {
      TideboundConfig.Values config = TideboundConfig.get();
      if (config.enableApexCompat
         && config.enableSharkFishPredation
         && this.shark.isTouchingWater()
         && this.shark.getTarget() == null
         && this.shark.getHunger() <= config.maximumFullnessForFoodTargeting
         && !ApexCompat.preyCooldownActive(this.shark)
         && (this.shark.age + this.shark.getId()) % config.sharkScanInterval == 0
         && !(this.shark.getRandom().nextDouble() > config.sharkPreyAcquisitionChance)) {
         double radius = config.sharkFishDetectionRadius;
         this.candidate = this.shark
            .getWorld()
            .getEntitiesByClass(
               LivingEntity.class,
               this.shark.getBoundingBox().expand(radius),
               entity -> entity != this.shark && entity.isAlive() && entity.isTouchingWater() && FishData.get(entity).isPresent()
            )
            .stream()
            .max(Comparator.comparingDouble(this::preyScore))
            .orElse(null);
         return this.candidate != null && this.shark.getNavigation().findPathTo(this.candidate, 0) != null;
      } else {
         return false;
      }
   }

   public void start() {
      this.shark.setTarget(this.candidate);
      super.start();
   }

   public boolean shouldContinue() {
      TideboundConfig.Values config = TideboundConfig.get();
      LivingEntity target = this.shark.getTarget();
      return target != null
         && target.isAlive()
         && target.isTouchingWater()
         && this.shark.getHunger() <= config.maximumFullnessForFoodTargeting
         && this.shark.squaredDistanceTo(target) <= config.sharkFishDetectionRadius * config.sharkFishDetectionRadius;
   }

   public void stop() {
      LivingEntity target = this.shark.getTarget();
      if (target != null && !target.isAlive()) {
         ApexCompat.setPreyCooldown(this.shark, TideboundConfig.get().sharkPreyCooldown);
      }

      this.candidate = null;
      super.stop();
   }

   private double preyScore(LivingEntity entity) {
      double size = FishData.get(entity).<Double>map(FishData::getAverageLength).orElse(25.0);
      return size / (1.0 + Math.sqrt(this.shark.squaredDistanceTo(entity)) * 0.35);
   }
}
