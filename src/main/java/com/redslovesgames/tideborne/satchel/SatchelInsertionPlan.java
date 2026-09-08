/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SatchelInsertionPlan<T>(List<T> contents, List<T> remainder, int insertedCount) {
   public SatchelInsertionPlan {
      contents = List.copyOf(contents);
      remainder = List.copyOf(remainder);
      if (insertedCount < 0) {
         throw new IllegalArgumentException("insertedCount must be non-negative");
      }
   }

   public static <T> SatchelInsertionPlan<T> create(List<T> current, List<T> incoming, int capacity) {
      Objects.requireNonNull(current, "current");
      Objects.requireNonNull(incoming, "incoming");
      if (capacity < 0) {
         throw new IllegalArgumentException("capacity must be non-negative");
      }

      int available = Math.max(0, capacity - current.size());
      int inserted = Math.min(available, incoming.size());
      List<T> result = new ArrayList<>(current.size() + inserted);
      result.addAll(current);
      result.addAll(incoming.subList(0, inserted));
      return new SatchelInsertionPlan<>(result, incoming.subList(inserted, incoming.size()), inserted);
   }

   public boolean fullyInserted() {
      return this.remainder.isEmpty();
   }
}
