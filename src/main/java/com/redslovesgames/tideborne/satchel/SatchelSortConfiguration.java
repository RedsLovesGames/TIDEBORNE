/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.List;

public record SatchelSortConfiguration(List<SatchelSortRule> rules) {
   public static final SatchelSortConfiguration EMPTY = new SatchelSortConfiguration(List.of());

   public SatchelSortConfiguration {
      rules = List.copyOf(rules);
   }
}
