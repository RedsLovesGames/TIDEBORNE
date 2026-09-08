/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SatchelSortKey {
   ALPHABETICAL("alphabetical"),
   MUTATION_RARITY("mutation_rarity"),
   PERCENTILE("percentile"),
   RARITY("rarity"),
   REGION("region"),
   SIZE("size");

   private final String id;

   SatchelSortKey(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static Optional<SatchelSortKey> byId(String id) {
      if (id == null) {
         return Optional.empty();
      }

      String normalized = id.toLowerCase(Locale.ROOT);
      if (normalized.equals("actual_size")) {
         normalized = SIZE.id;
      } else if (normalized.equals("size_percentile")) {
         normalized = PERCENTILE.id;
      } else if (normalized.equals("tide_rarity")) {
         normalized = RARITY.id;
      } else if (normalized.equals("alphabetical_name")) {
         normalized = ALPHABETICAL.id;
      }

      String finalNormalized = normalized;
      return Arrays.stream(values()).filter(value -> value.id.equals(finalNormalized)).findFirst();
   }
}
