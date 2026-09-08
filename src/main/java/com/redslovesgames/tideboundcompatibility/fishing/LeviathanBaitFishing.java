/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideFishingManager;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishingContext;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.util.BaitUtils;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;

public final class LeviathanBaitFishing {
   private LeviathanBaitFishing() {
   }

   public static CatchResult selectCatch(TideFishingManager manager, TideFishingHook hook, FishingContext context) {
      TideboundConfig.Values config = TideboundConfig.get();
      FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(isEnabledFor(hook, config));
      boolean fishOnly = LeviathanBaitRules.isFishOnlyCatchPool(modifiers);
      CatchResult result = fishOnly ? manager.getFishSelector().getResult(context) : manager.selectCatch(context);
      if (!fishOnly) {
         return result;
      }

      ((LeviathanBaitHook)hook).tidebound$setLeviathanFishSelected(result.isFish());
      return result;
   }

   public static boolean isEnabledFor(TideFishingHook hook, TideboundConfig.Values config) {
      return hook != null && config.enableMythsCompat && TideboundCompatibility.isMythsIntegrationActive()
         && config.leviathanBaitFishOnly
         && BaitUtils.hasBait(TideboundItems.LEVIATHAN_BAIT, hook.getRod());
   }
}
