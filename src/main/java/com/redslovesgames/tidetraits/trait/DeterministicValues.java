/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.trait;

public final class DeterministicValues {
   public static final long MUTATION_SELECTION_SALT = 6659551205387646661L;
   public static final long DWARF_LENGTH_SALT = 2611923443488327891L;
   public static final long GIANT_LENGTH_SALT = 1376283091369227076L;
   public static final long PARASITE_LENGTH_SALT = -6626703657320631856L;
   public static final long PERFECT_PERCENTILE_SALT = 589684135938649225L;

   private DeterministicValues() {
   }

   public static double unitDouble(long identitySeed, long salt) {
      return (mix64(identitySeed ^ salt) >>> 11) * 1.1102230246251565E-16;
   }

   public static long mix64(long value) {
      value = (value ^ value >>> 30) * -4658895280553007687L;
      value = (value ^ value >>> 27) * -7723592293110705685L;
      return value ^ value >>> 31;
   }
}
