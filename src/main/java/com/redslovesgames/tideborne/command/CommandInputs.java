/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNamespaces;

import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;

final class CommandInputs {
   private CommandInputs() {
   }

   static Optional<FishMutation> mutation(String input) {
      if (input != null && !input.isBlank()) {
         String normalized = input.toLowerCase(Locale.ROOT);
         if (!normalized.contains(":")) {
            return FishMutation.bySerializedName(normalized);
         }

         Identifier id = Identifier.tryParse(normalized);
         return id != null && LegacyNamespaces.TIDE_TRAITS.equals(id.getNamespace()) ? FishMutation.bySerializedName(id.getPath()) : Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   static double percentilePercent(double normalized) {
      if (Double.isFinite(normalized) && !(normalized < 0.0) && !(normalized > 1.0)) {
         return normalized * 100.0;
      } else {
         throw new IllegalArgumentException("Percentile must be finite and within [0, 1]");
      }
   }

   static String validMutationIds() {
      return Arrays.stream(FishMutation.values()).map(FishMutation::serializedName).collect(Collectors.joining(", "));
   }
}
