/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.specimen;

import java.util.List;
import net.minecraft.item.ItemStack;

/**
 * Compatibility shim for the removed Tideborne 1.x Perfect Catch trait boost.
 *
 * <p>Fishing System 2.0 resolves Perfect Catch inside the canonical two-phase specimen generator.
 * The legacy implementation used to rewrite percentile, reroll Giant/Dwarf gates, and recompute fish
 * length after the fight. Those mutations are permanently disabled. The method remains because old
 * reconstructed call sites may still invoke it, but it intentionally performs no state mutation for
 * either canonical or legacy stacks.
 */
public final class PerfectCatchTraitBoost {
   private PerfectCatchTraitBoost() {
   }

   public static void apply(List<ItemStack> catches) {
      // Intentionally no-op. Perfect Catch rewards are canonical V2 state only.
   }
}
