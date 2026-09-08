/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel.network;

import java.util.Objects;

public record SatchelFeatureView(String id, int xpCost, boolean unlocked, boolean enabled, boolean available) {
   public SatchelFeatureView {
      id = Objects.requireNonNull(id, "id");
      if (xpCost < 0) {
         throw new IllegalArgumentException("xpCost must be non-negative");
      }
   }
}
