/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import java.util.Locale;

public record SatchelSortDescriptor(
   String alphabeticalName,
   double mutationRarity,
   double percentile,
   int tideRarity,
   String region,
   double actualSize,
   String registryId,
   String mutationId,
   long deterministicSeed
) {
   public SatchelSortDescriptor {
      alphabeticalName = normalize(alphabeticalName);
      region = normalize(region);
      registryId = normalize(registryId);
      mutationId = normalize(mutationId);
      mutationRarity = finiteOr(mutationRarity, 1.7976931348623157E308);
      percentile = finiteOr(percentile, -1.0);
      actualSize = finiteOr(actualSize, 0.0);
   }

   private static String normalize(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT);
   }

   private static double finiteOr(double value, double fallback) {
      return Double.isFinite(value) ? value : fallback;
   }
}
