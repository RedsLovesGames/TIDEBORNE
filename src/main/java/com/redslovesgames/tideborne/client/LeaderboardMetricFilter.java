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

   public static List<String> withoutFishScore(List<String> metrics) {
      ArrayList<String> filtered = new ArrayList<>();
      if (metrics != null) {
         for (String metric : metrics) {
            if (!"fish_score".equals(metric)) {
               filtered.add(metric);
            }
         }
      }

      if (filtered.isEmpty()) {
         filtered.add("catches");
         filtered.add("species");
         filtered.add("record_events");
         filtered.add("active_records");
      }

      return filtered;
   }

   public static String normalizeDefault(String metric) {
      return "fish_score".equals(metric) ? "catches" : metric;
   }
}
