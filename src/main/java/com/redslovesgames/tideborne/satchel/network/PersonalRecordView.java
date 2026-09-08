/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel.network;

import com.li64.tide.data.player.FishStats;

public record PersonalRecordView(boolean available, double largest, double smallest) {
   private static final PersonalRecordView UNAVAILABLE = new PersonalRecordView(false, 0.0, 0.0);

   public PersonalRecordView {
      if (!available || validLength(largest) && validLength(smallest)) {
         if (!available) {
            largest = 0.0;
            smallest = 0.0;
         }
      } else {
         throw new IllegalArgumentException("Available personal records require positive finite lengths");
      }
   }

   public static PersonalRecordView unavailable() {
      return UNAVAILABLE;
   }

   public static PersonalRecordView from(FishStats stats) {
      if (stats != null && !stats.isEmpty()) {
         double largest = stats.getLargestCatch();
         double smallest = stats.getSmallestCatch();
         return validLength(largest) && validLength(smallest) ? new PersonalRecordView(true, largest, smallest) : unavailable();
      } else {
         return unavailable();
      }
   }

   private static boolean validLength(double value) {
      return Double.isFinite(value) && value > 0.0;
   }
}
