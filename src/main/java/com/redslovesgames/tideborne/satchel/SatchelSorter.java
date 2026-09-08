/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SatchelSorter {
   private SatchelSorter() {
   }

   public static <T> List<T> sortedCopy(List<T> values, SatchelSortConfiguration configuration, Function<? super T, SatchelSortDescriptor> descriptorResolver) {
      return sortedEntries(values, configuration, descriptorResolver).stream().map(SatchelSorter.IndexedValue::value).toList();
   }

   static <T> List<SatchelSorter.IndexedValue<T>> sortedEntries(
      List<T> values, SatchelSortConfiguration configuration, Function<? super T, SatchelSortDescriptor> descriptorResolver
   ) {
      Objects.requireNonNull(values, "values");
      Objects.requireNonNull(configuration, "configuration");
      Objects.requireNonNull(descriptorResolver, "descriptorResolver");
      List<SatchelSorter.IndexedValue<T>> entries = new ArrayList<>(values.size());

      for (int index = 0; index < values.size(); index++) {
         T value = values.get(index);
         entries.add(new SatchelSorter.IndexedValue<>(value, descriptorResolver.apply(value), index));
      }

      entries.sort(comparator(configuration));
      return List.copyOf(entries);
   }

   private static <T> Comparator<SatchelSorter.IndexedValue<T>> comparator(SatchelSortConfiguration configuration) {
      return (left, right) -> {
         for (SatchelSortRule rule : configuration.rules()) {
            int comparison = compareRule(left.descriptor(), right.descriptor(), rule.key());
            if (comparison != 0) {
               return rule.direction().apply(comparison);
            }
         }

         int comparison = left.descriptor().registryId().compareTo(right.descriptor().registryId());
         if (comparison == 0) {
            comparison = left.descriptor().mutationId().compareTo(right.descriptor().mutationId());
         }

         if (comparison == 0) {
            comparison = Double.compare(left.descriptor().actualSize(), right.descriptor().actualSize());
         }

         if (comparison == 0) {
            comparison = Long.compare(left.descriptor().deterministicSeed(), right.descriptor().deterministicSeed());
         }

         if (comparison == 0) {
            comparison = Integer.compare(left.originalIndex(), right.originalIndex());
         }

         return comparison;
      };
   }

   private static int compareRule(SatchelSortDescriptor left, SatchelSortDescriptor right, SatchelSortKey key) {
      return switch (key) {
         case ALPHABETICAL -> left.alphabeticalName().compareTo(right.alphabeticalName());
         case MUTATION_RARITY -> Double.compare(left.mutationRarity(), right.mutationRarity());
         case PERCENTILE -> Double.compare(left.percentile(), right.percentile());
         case RARITY -> Integer.compare(left.tideRarity(), right.tideRarity());
         case REGION -> left.region().compareTo(right.region());
         case SIZE -> Double.compare(left.actualSize(), right.actualSize());
      };
   }

   record IndexedValue<T>(T value, SatchelSortDescriptor descriptor, int originalIndex) {
   }
}
