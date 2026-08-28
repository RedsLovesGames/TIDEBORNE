/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import java.util.Locale;

public enum SharedDiscoveryAvailability {
   UNKNOWN("unknown"),
   AVAILABLE("available"),
   MISSING_MODS("missing_mods"),
   TEAM_UNRESOLVED("team_unresolved"),
   ERROR("error");

   private final String serializedName;

   SharedDiscoveryAvailability(String serializedName) {
      this.serializedName = serializedName;
   }

   public String serializedName() {
      return this.serializedName;
   }

   public boolean isAvailable() {
      return this == AVAILABLE;
   }

   public static SharedDiscoveryAvailability fromSerializedName(String value) {
      if (value == null) {
         return UNKNOWN;
      }

      String normalized = value.toLowerCase(Locale.ROOT);

      for (SharedDiscoveryAvailability availability : values()) {
         if (availability.serializedName.equals(normalized)) {
            return availability;
         }
      }

      return UNKNOWN;
   }
}
