/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import java.util.Objects;

public record SatchelSortRule(SatchelSortKey key, SatchelSortDirection direction) {
   public SatchelSortRule {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(direction, "direction");
   }
}
