/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import com.redslovesgames.tidetraits.discovery.DiscoverySnapshot;
import java.util.Objects;

public record SharedDiscoverySnapshot(SharedDiscoveryAvailability availability, DiscoverySnapshot discoveries) {
   private static final SharedDiscoverySnapshot UNKNOWN = new SharedDiscoverySnapshot(SharedDiscoveryAvailability.UNKNOWN, DiscoverySnapshot.empty());

   public SharedDiscoverySnapshot {
      Objects.requireNonNull(availability, "availability");
      Objects.requireNonNull(discoveries, "discoveries");
      if (!availability.isAvailable() && !discoveries.species().isEmpty()) {
         discoveries = DiscoverySnapshot.empty();
      }
   }

   public static SharedDiscoverySnapshot unknown() {
      return UNKNOWN;
   }

   public static SharedDiscoverySnapshot available(DiscoverySnapshot discoveries) {
      return new SharedDiscoverySnapshot(SharedDiscoveryAvailability.AVAILABLE, discoveries);
   }

   public static SharedDiscoverySnapshot unavailable(SharedDiscoveryAvailability availability) {
      if (Objects.requireNonNull(availability, "availability").isAvailable()) {
         throw new IllegalArgumentException("Use available(...) for an available shared snapshot");
      } else {
         return availability == SharedDiscoveryAvailability.UNKNOWN ? UNKNOWN : new SharedDiscoverySnapshot(availability, DiscoverySnapshot.empty());
      }
   }

   public boolean isAvailable() {
      return this.availability.isAvailable();
   }
}
