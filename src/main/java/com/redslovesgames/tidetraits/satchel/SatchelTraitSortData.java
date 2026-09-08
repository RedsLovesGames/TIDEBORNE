/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

public record SatchelTraitSortData(String mutationId, double configuredMutationProbability, double percentile, long deterministicSeed) {
   public static final SatchelTraitSortData NORMAL = new SatchelTraitSortData("normal", 1.7976931348623157E308, -1.0, 0L);
}
