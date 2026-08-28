/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.discovery;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.Identifier;

public record DiscoverySnapshot(Map<Identifier, DiscoverySnapshot.SpeciesDiscoveries> species) {
   private static final DiscoverySnapshot EMPTY = new DiscoverySnapshot(Map.of());

   public DiscoverySnapshot {
      Objects.requireNonNull(species, "species");
      Map<Identifier, DiscoverySnapshot.SpeciesDiscoveries> copy = new LinkedHashMap<>();
      species.forEach((id, discoveries) -> copy.put(Objects.requireNonNull(id, "species id"), Objects.requireNonNull(discoveries, "species discoveries")));
      species = Collections.unmodifiableMap(copy);
   }

   public static DiscoverySnapshot empty() {
      return EMPTY;
   }

   public DiscoverySnapshot.SpeciesDiscoveries forSpecies(Identifier speciesId) {
      Objects.requireNonNull(speciesId, "speciesId");
      return this.species.getOrDefault(speciesId, DiscoverySnapshot.SpeciesDiscoveries.empty());
   }

   public boolean hasMutation(Identifier speciesId, Identifier mutationId) {
      Objects.requireNonNull(mutationId, "mutationId");
      return this.forSpecies(speciesId).mutations().contains(mutationId);
   }

   public boolean hasSizeBand(Identifier speciesId, Identifier sizeBandId) {
      Objects.requireNonNull(sizeBandId, "sizeBandId");
      return this.forSpecies(speciesId).sizeBands().contains(sizeBandId);
   }

   public record SpeciesDiscoveries(Set<Identifier> mutations, Set<Identifier> sizeBands) {
      private static final DiscoverySnapshot.SpeciesDiscoveries EMPTY = new DiscoverySnapshot.SpeciesDiscoveries(Set.of(), Set.of());

      public SpeciesDiscoveries {
         Objects.requireNonNull(mutations, "mutations");
         Objects.requireNonNull(sizeBands, "sizeBands");
         mutations = immutableCopy(mutations, "mutation id");
         sizeBands = immutableCopy(sizeBands, "size-band id");
      }

      public static DiscoverySnapshot.SpeciesDiscoveries empty() {
         return EMPTY;
      }

      private static Set<Identifier> immutableCopy(Set<Identifier> ids, String label) {
         Set<Identifier> copy = new LinkedHashSet<>();

         for (Identifier id : ids) {
            copy.add(Objects.requireNonNull(id, label));
         }

         return Collections.unmodifiableSet(copy);
      }
   }
}
