/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.Locale;

public enum SatchelSortDirection {
   ASCENDING("asc", 1),
   DESCENDING("desc", -1);

   private final String id;
   private final int sign;

   SatchelSortDirection(String id, int sign) {
      this.id = id;
      this.sign = sign;
   }

   public String id() {
      return this.id;
   }

   int apply(int comparison) {
      return this.sign * comparison;
   }

   public static SatchelSortDirection byId(String id) {
      if (id == null) {
         return ASCENDING;
      }

      return switch (id.toLowerCase(Locale.ROOT)) {
         case "desc", "descending" -> DESCENDING;
         default -> ASCENDING;
      };
   }
}
