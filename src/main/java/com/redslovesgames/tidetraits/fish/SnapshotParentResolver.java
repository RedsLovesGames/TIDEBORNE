/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.fish;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class SnapshotParentResolver {
   private SnapshotParentResolver() {
   }

   static <E, K> List<E> resolve(List<E> entries, Function<E, K> entryKey, Function<E, Optional<K>> parentKey) {
      Objects.requireNonNull(entries, "entries");
      Objects.requireNonNull(entryKey, "entryKey");
      Objects.requireNonNull(parentKey, "parentKey");
      LinkedHashMap<K, E> originals = new LinkedHashMap<>();

      for (E entry : entries) {
         if (parentKey.apply(entry).isEmpty()) {
            originals.put(entryKey.apply(entry), entry);
         }
      }

      ArrayList<E> canonical = new ArrayList<>(entries.size());

      for (E entry : entries) {
         Optional<K> parent = parentKey.apply(entry);
         E resolved = parent.isPresent() ? originals.get(parent.orElseThrow()) : originals.get(entryKey.apply(entry));
         if (resolved != null) {
            canonical.add(resolved);
         }
      }

      return List.copyOf(canonical);
   }
}
