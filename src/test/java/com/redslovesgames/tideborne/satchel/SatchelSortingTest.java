package com.redslovesgames.tideborne.satchel;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SatchelSortingTest {
   @Test
   void sortsByConfiguredRulesThenDeterministicFallbacks() {
      List<String> values = List.of("large", "later", "earlier");
      Map<String, SatchelSorting.Descriptor> descriptors = Map.of(
         "large", descriptor("large", 3.0, "tide:z", 3L),
         "later", descriptor("same", 2.0, "tide:z", 2L),
         "earlier", descriptor("same", 2.0, "tide:a", 1L)
      );
      SatchelSortConfiguration configuration = new SatchelSortConfiguration(
         List.of(new SatchelSortRule(SatchelSortKey.SIZE, SatchelSortDirection.DESCENDING))
      );

      assertEquals(List.of("large", "earlier", "later"), SatchelSorting.sortedCopy(values, configuration, descriptors::get));
   }

   @Test
   void descriptorNormalizationPreservesLegacyFallbacks() {
      SatchelSorting.Descriptor descriptor = new SatchelSorting.Descriptor(
         "SALMON",
         Double.NaN,
         Double.POSITIVE_INFINITY,
         2,
         null,
         Double.NEGATIVE_INFINITY,
         "TIDE:SALMON",
         null,
         9L
      );

      assertEquals("salmon", descriptor.alphabeticalName());
      assertEquals(Double.MAX_VALUE, descriptor.mutationRarity());
      assertEquals(-1.0, descriptor.percentile());
      assertEquals("", descriptor.region());
      assertEquals(0.0, descriptor.actualSize());
      assertEquals("tide:salmon", descriptor.registryId());
      assertEquals("", descriptor.mutationId());
   }

   private static SatchelSorting.Descriptor descriptor(String name, double size, String registryId, long seed) {
      return new SatchelSorting.Descriptor(name, 1.0, 0.5, 1, "river", size, registryId, "normal", seed);
   }
}
