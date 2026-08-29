/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import java.util.ArrayList;
import java.util.List;

/** Keeps configured leaderboard metrics while ensuring canonical FishScore remains available. */
public final class LeaderboardMetricFilter {
   private LeaderboardMetricFilter() {
   }

   /** Historical method name retained for binary/source compatibility. FishScore is no longer filtered out. */
   public static List<String> withoutFishScore(List<String> metrics) {
      ArrayList<String> result = new ArrayList<>();
      if (metrics != null) {
         for (String metric : metrics) {
            if (metric != null && !metric.isBlank() && !result.contains(metric)) {
               result.add(metric);
            }
         }
      }

      if (result.isEmpty()) {
         result.add("catches");
         result.add("species");
         result.add("record_events");
         result.add("active_records");
         result.add("fish_score");
      }
      return List.copyOf(result);
   }

   public static String normalizeDefault(String metric) {
      return metric == null || metric.isBlank() ? "catches" : metric;
   }
}
