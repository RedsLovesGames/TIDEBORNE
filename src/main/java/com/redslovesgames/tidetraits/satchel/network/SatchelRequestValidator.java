/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel.network;

import com.redslovesgames.tidetraits.satchel.SatchelSortConfiguration;
import com.redslovesgames.tidetraits.satchel.SatchelSortDirection;
import com.redslovesgames.tidetraits.satchel.SatchelSortKey;
import com.redslovesgames.tidetraits.satchel.SatchelSortRule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class SatchelRequestValidator {
   private SatchelRequestValidator() {
   }

   public static Optional<SatchelSortConfiguration> sortConfiguration(List<SatchelRequestPayload.SortRuleRequest> requestedRules) {
      if (requestedRules != null && requestedRules.size() <= 6) {
         EnumSet<SatchelSortKey> seen = EnumSet.noneOf(SatchelSortKey.class);
         List<SatchelSortRule> rules = new ArrayList<>(requestedRules.size());

         for (SatchelRequestPayload.SortRuleRequest requested : requestedRules) {
            if (requested == null) {
               return Optional.empty();
            }

            String rawKey = requested.keyId().toLowerCase(Locale.ROOT);
            SatchelSortKey key = SatchelSortKey.byId(rawKey).orElse(null);
            if (key == null || !key.id().equals(rawKey) || !seen.add(key)) {
               return Optional.empty();
            }
            SatchelSortDirection direction = switch (requested.directionId().toLowerCase(Locale.ROOT)) {
               case "asc" -> SatchelSortDirection.ASCENDING;
               case "desc" -> SatchelSortDirection.DESCENDING;
               default -> null;
            };
            if (direction == null) {
               return Optional.empty();
            }

            rules.add(new SatchelSortRule(key, direction));
         }

         return Optional.of(new SatchelSortConfiguration(rules));
      } else {
         return Optional.empty();
      }
   }
}
