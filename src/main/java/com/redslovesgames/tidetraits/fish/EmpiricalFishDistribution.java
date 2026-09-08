/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.fish;

import java.util.Arrays;
import java.util.Optional;

public final class EmpiricalFishDistribution {
   private final double[] samples;

   private EmpiricalFishDistribution(double[] sortedSamples) {
      this.samples = sortedSamples;
   }

   public static Optional<EmpiricalFishDistribution> fromSamples(double... sourceSamples) {
      if (sourceSamples != null && sourceSamples.length != 0) {
         double[] valid = Arrays.stream(sourceSamples).filter(sample -> Double.isFinite(sample) && sample > 0.0).sorted().toArray();
         return valid.length == 0 ? Optional.empty() : Optional.of(new EmpiricalFishDistribution(valid));
      } else {
         return Optional.empty();
      }
   }

   public int sampleCount() {
      return this.samples.length;
   }

   public double minimumLengthCm() {
      return this.samples[0];
   }

   public double maximumLengthCm() {
      return this.samples[this.samples.length - 1];
   }

   public double percentileOf(double lengthCm) {
      if (!Double.isFinite(lengthCm)) {
         throw new IllegalArgumentException("Physical length must be finite");
      }

      if (lengthCm < this.samples[0]) {
         return 0.0;
      }

      if (lengthCm >= this.samples[this.samples.length - 1]) {
         return 100.0;
      }

      int countAtOrBelow = upperBound(this.samples, lengthCm);
      return 100.0 * countAtOrBelow / this.samples.length;
   }

   public double lengthAtPercentile(double percentile) {
      if (Double.isFinite(percentile) && !(percentile < 0.0) && !(percentile > 100.0)) {
         if (this.samples.length == 1 || percentile <= 0.0) {
            return this.samples[0];
         }

         if (percentile >= 100.0) {
            return this.samples[this.samples.length - 1];
         }

         double position = percentile / 100.0 * (this.samples.length - 1);
         int lower = (int)Math.floor(position);
         int upper = Math.min(lower + 1, this.samples.length - 1);
         double fraction = position - lower;
         return this.samples[lower] + (this.samples[upper] - this.samples[lower]) * fraction;
      } else {
         throw new IllegalArgumentException("Percentile must be finite and within [0, 100]");
      }
   }

   public double[] samplesCopy() {
      return (double[])this.samples.clone();
   }

   private static int upperBound(double[] values, double target) {
      int low = 0;
      int high = values.length;

      while (low < high) {
         int middle = low + high >>> 1;
         if (values[middle] <= target) {
            low = middle + 1;
         } else {
            high = middle;
         }
      }

      return low;
   }
}
