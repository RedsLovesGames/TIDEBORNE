/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.specimen;

public enum FishSizeClass {
   RUNTY("runty"),
   SMALL("small"),
   AVERAGE("average"),
   HEFTY("hefty"),
   TROPHY("trophy"),
   LEGENDARY("legendary");

   private final String serializedName;

   FishSizeClass(String serializedName) {
      this.serializedName = serializedName;
   }

   public String serializedName() {
      return this.serializedName;
   }

   public static FishSizeClass fromPercentile(double percentile) {
      if (!Double.isFinite(percentile) || percentile < 0.0 || percentile > 100.0) {
         throw new IllegalArgumentException("Percentile must be finite and within [0, 100]");
      } else if (percentile < 10.0) {
         return RUNTY;
      } else if (percentile < 30.0) {
         return SMALL;
      } else if (percentile < 60.0) {
         return AVERAGE;
      } else if (percentile < 85.0) {
         return HEFTY;
      } else {
         return percentile < 95.0 ? TROPHY : LEGENDARY;
      }
   }
}
