/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.gear;

import com.li64.tide.registries.items.TideFishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item.Settings;

public final class KujiraBoneFishingRodItem extends TideFishingRodItem {
   private static final Identifier KUJIRA_BONE = Identifier.of("myths_of_the_sea", "bake_kujira_bone");

   public KujiraBoneFishingRodItem(int baitSlots, double durability, Settings properties) {
      super(baitSlots, durability, properties);
   }

   public boolean canRepair(ItemStack stack, ItemStack repairCandidate) {
      return repairCandidate.getItem().getRegistryEntry().matchesId(KUJIRA_BONE) || super.canRepair(stack, repairCandidate);
   }
}
