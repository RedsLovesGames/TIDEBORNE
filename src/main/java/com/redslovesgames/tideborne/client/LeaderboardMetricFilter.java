/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import java.util.ArrayList;
import java.util.List;

public final class LeaderboardMetricFilter {
   private LeaderboardMetricFilter() {
   }

   public static List<String> withoutFishScore(List<String> var0) {
      ArrayList var1 = new ArrayList();
      if (var0 != null) {
         for (String var3 : var0) {
            if (!"fish_score".equals(var3)) {
               var1.add(var3);
            }
         }
      }

      if (var1.isEmpty()) {
         var1.add("catches");
         var1.add("species");
         var1.add("record_events");
         var1.add("active_records");
      }

      return var1;
   }

   public static String normalizeDefault(String var0) {
      return "fish_score".equals(var0) ? "catches" : var0;
   }
}
