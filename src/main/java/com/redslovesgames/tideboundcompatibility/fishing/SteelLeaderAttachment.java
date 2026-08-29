/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import net.minecraft.item.ItemStack;

public final class SteelLeaderAttachment {
   private SteelLeaderAttachment() {
   }

   public static boolean has(Object value) {
      if (!(value instanceof ItemStack stack)) {
         return false;
      }
      return Boolean.TRUE.equals(stack.get(TideTraitsComponents.STEEL_LEADER_ATTACHED));
   }

   public static void set(Object value, boolean attached) {
      if (!(value instanceof ItemStack stack)) {
         return;
      }
      if (attached) {
         stack.set(TideTraitsComponents.STEEL_LEADER_ATTACHED, Boolean.TRUE);
      } else {
         stack.remove(TideTraitsComponents.STEEL_LEADER_ATTACHED);
      }
   }

   public static boolean isSteelLeaderStack(Object value) {
      return value instanceof ItemStack stack && stack.isOf(TideboundItems.STEEL_LEADER);
   }

   public static boolean hasOnHook(Object value) {
      if (!(value instanceof TideFishingHook hook)) {
         return false;
      }

      ItemStack rod = hook.getRod();
      if (has(rod)) {
         return true;
      }

      return isSteelLeaderStack(CustomRodManager.getLine(rod));
   }

   public static Object steelLeaderItem() {
      return TideboundItems.STEEL_LEADER;
   }
}
