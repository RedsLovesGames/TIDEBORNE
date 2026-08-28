/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SatchelProtectionRule {
   MUTATED("mutated"),
   TROPHY_SIZE("trophy"),
   LEGENDARY_SIZE("legendary"),
   PERSONAL_LARGEST("personal_largest"),
   PERSONAL_SMALLEST("personal_smallest"),
   TIDE_LEGENDARY_RARITY("tide_legendary_rarity");

   private final String id;

   SatchelProtectionRule(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static Optional<SatchelProtectionRule> byId(String id) {
      if (id == null) {
         return Optional.empty();
      }

      String normalized = id.toLowerCase(Locale.ROOT);
      return Arrays.stream(values()).filter(rule -> rule.id.equals(normalized)).findFirst();
   }
}
