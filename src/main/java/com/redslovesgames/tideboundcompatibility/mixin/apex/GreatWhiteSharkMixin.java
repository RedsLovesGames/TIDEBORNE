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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Apex Waters is an optional external mod. Do not mirror its Minecraft superclass in
 * this mixin: Loom remaps the external JAR between Mojmap/intermediary/Yarn namespaces,
 * and a reconstructed source-level superclass can therefore fail Mixin hierarchy
 * validation even when the target bytecode is compatible. Shadow the inherited goal
 * selectors instead, which keeps the mixin namespace-stable.
 */
@Mixin(GreatWhiteSharkEntity.class)
abstract class GreatWhiteSharkMixin {
   @Shadow
   @Final
   protected GoalSelector goalSelector;

   @Shadow
   @Final
   protected GoalSelector targetSelector;

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
