/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.discovery;

import java.util.Objects;

public record DiscoveryTotals(int speciesCount, int mutationCount, int sizeBandCount) {
   public DiscoveryTotals(int speciesCount, int mutationCount, int sizeBandCount) {
      if (speciesCount >= 0 && mutationCount >= 0 && sizeBandCount >= 0) {
         this.speciesCount = speciesCount;
         this.mutationCount = mutationCount;
         this.sizeBandCount = sizeBandCount;
      } else {
         throw new IllegalArgumentException("Discovery totals cannot be negative");
      }
   }

   public static DiscoveryTotals from(DiscoverySnapshot snapshot) {
      Objects.requireNonNull(snapshot, "snapshot");
      int species = 0;
      int mutations = 0;
      int sizeBands = 0;

      for (DiscoverySnapshot.SpeciesDiscoveries discoveries : snapshot.species().values()) {
         int speciesMutations = discoveries.mutations().size();
         int speciesSizeBands = discoveries.sizeBands().size();
         if (speciesMutations > 0 || speciesSizeBands > 0) {
            species++;
         }

         mutations += speciesMutations;
         sizeBands += speciesSizeBands;
      }

      return new DiscoveryTotals(species, mutations, sizeBands);
   }
}
