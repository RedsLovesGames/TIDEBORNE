/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import com.redslovesgames.tideboundcompatibility.registry.TideboundTags;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.tag.BiomeTags;

public final class FishingModifiers {
   private FishingModifiers() {
   }

   public static double modifyFishWeight(FishData data, FishingContext context, double original) {
      TideFishingHook hook = context.hook();
      if (hook == null) {
         return original;
      }

      ItemStack fish = new ItemStack((ItemConvertible)data.fish().comp_349());
      double result = original;
      TideboundConfig.Values config = TideboundConfig.get();
      if (config.enableApexCompat && hook.getHook().isOf(TideboundItems.SHARK_TOOTH_HOOK)) {
         if (fish.isIn(TideboundTags.PREDATORY_FISH) || fish.isIn(TideboundTags.LARGE_FISH)) {
            result *= config.sharkToothPredatoryWeightMultiplier;
         }

         if (fish.isIn(TideboundTags.VERY_SMALL_FISH)) {
            result *= config.sharkToothSmallFishWeightMultiplier;
         }
      }

      if (config.enableMythsCompat
         && hook.getHook().isOf(TideboundItems.SEAFARERS_HOOK)
         && fish.isIn(Items.LEGENDARY_FISH)
         && context.exactBiome().isIn(BiomeTags.IS_OCEAN)
         && context.level().isNight()) {
         result *= config.seafarersRareWeightMultiplier;
      }

      return result;
   }

   public static double modifyCrateWeight(FishingContext context, double original) {
      return TideboundConfig.get().enableMythsCompat
            && context.rod() != null
            && context.rod().isOf(TideboundItems.KUJIRA_BONE_FISHING_ROD)
            && context.exactBiome().isIn(BiomeTags.IS_OCEAN)
         ? original * TideboundConfig.get().kujiraOceanCrateMultiplier
         : original;
   }

   public static FishingModifiers.MinigameValues modifyMinigame(TideFishingHook hook, byte behavior, float area, float speed) {
      TideboundConfig.Values config = TideboundConfig.get();
      ItemStack line = hook.getLine();
      if (config.enableMythsCompat && line.isOf(TideboundItems.TENTACLE_LINE)) {
         area *= (float)config.tentacleCatchZoneMultiplier;
         speed *= (float)config.tentacleFishSpeedMultiplier;
      } else if (config.enableMythsCompat && line.isOf(TideboundItems.SWIFT_LINE)) {
         area *= (float)config.swiftCatchZoneMultiplier;
         speed *= (float)config.swiftFishSpeedMultiplier;
      } else if (config.enableApexCompat && SteelLeaderAttachment.hasOnHook(hook)) {
         area *= (float)config.steelLeaderCatchZoneMultiplier;
         speed *= (float)config.steelLeaderFishSpeedMultiplier;
      }

      if (hook instanceof LeviathanBaitHook leviathan && leviathan.tidebound$isLeviathanFishSelected()) {
         area = LeviathanBaitRules.catchZone(area, config.leviathanBaitCatchZoneMultiplier);
         speed = LeviathanBaitRules.fishSpeed(speed, config.leviathanBaitMinigameSpeedMultiplier);
         return new FishingModifiers.MinigameValues(behavior, area, speed);
      } else {
         return new FishingModifiers.MinigameValues(behavior, MathHelper.clamp(area, 0.05F, 1.0F), Math.max(0.05F, speed));
      }
   }

   public record MinigameValues(byte behavior, float area, float speed) {
   }
}
