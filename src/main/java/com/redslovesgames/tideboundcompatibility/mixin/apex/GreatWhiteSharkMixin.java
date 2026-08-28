/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin.apex;

import com.acorsicanfrog.apexwaters.entity.GreatWhiteSharkEntity;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideboundcompatibility.compat.apex.SharkFoodGoal;
import com.redslovesgames.tideboundcompatibility.compat.apex.TideFishTargetGoal;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Apex Waters is remapped into Yarn by Loom before this mixin is processed. */
@Mixin(GreatWhiteSharkEntity.class)
abstract class GreatWhiteSharkMixin extends WaterCreatureEntity {
   protected GreatWhiteSharkMixin(EntityType<? extends WaterCreatureEntity> type, World level) {
      super(type, level);
   }

   @Inject(method = "registerGoals", at = @At("TAIL"))
   private void tidebound$registerFoodGoals(CallbackInfo callback) {
      GreatWhiteSharkEntity shark = (GreatWhiteSharkEntity)(Object)this;
      this.goalSelector.add(2, new SharkFoodGoal(shark));
      this.targetSelector.add(2, new TideFishTargetGoal(shark));
   }

   @Inject(method = "shouldTarget", at = @At("HEAD"), cancellable = true, remap = false)
   private void tidebound$leaveTideFishToSizedGoal(LivingEntity target, CallbackInfoReturnable<Boolean> callback) {
      if (TideboundConfig.get().enableApexCompat && FishData.get(target).isPresent()) {
         callback.setReturnValue(false);
      }
   }
}
