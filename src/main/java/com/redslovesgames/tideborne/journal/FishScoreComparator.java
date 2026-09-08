/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal;

import java.util.Comparator;

public final class FishScoreComparator implements Comparator {
   @Override
   public int compare(Object var1, Object var2) {
      return Integer.compare(
         TeamProgressStore.tideborneContributorFishScore((TeamProgressStore.Contributor)var1),
         TeamProgressStore.tideborneContributorFishScore((TeamProgressStore.Contributor)var2)
      );
   }
}
