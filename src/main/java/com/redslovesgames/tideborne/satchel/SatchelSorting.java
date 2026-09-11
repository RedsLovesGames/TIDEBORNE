/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/** Owns Satchel sorting mechanics and sorting-only metadata. */
public final class SatchelSorting {
   private SatchelSorting() {
   }

   public static <T> List<T> sortedCopy(List<T> values, SatchelSortConfiguration configuration, Function<? super T, Descriptor> descriptorResolver) {
      return sortedEntries(values, configuration, descriptorResolver).stream().map(IndexedValue::value).toList();
   }

   static <T> List<IndexedValue<T>> sortedEntries(
      List<T> values, SatchelSortConfiguration configuration, Function<? super T, Descriptor> descriptorResolver
   ) {
      Objects.requireNonNull(values, "values");
      Objects.requireNonNull(configuration, "configuration");
      Objects.requireNonNull(descriptorResolver, "descriptorResolver");
      List<IndexedValue<T>> entries = new ArrayList<>(values.size());

      for (int index = 0; index < values.size(); index++) {
         T value = values.get(index);
         entries.add(new IndexedValue<>(value, descriptorResolver.apply(value), index));
      }

      entries.sort(comparator(configuration));
      return List.copyOf(entries);
   }

   private static <T> Comparator<IndexedValue<T>> comparator(SatchelSortConfiguration configuration) {
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

   private static int compareRule(Descriptor left, Descriptor right, SatchelSortKey key) {
      return switch (key) {
         case ALPHABETICAL -> left.alphabeticalName().compareTo(right.alphabeticalName());
         case MUTATION_RARITY -> Double.compare(left.mutationRarity(), right.mutationRarity());
         case PERCENTILE -> Double.compare(left.percentile(), right.percentile());
         case RARITY -> Integer.compare(left.tideRarity(), right.tideRarity());
         case REGION -> left.region().compareTo(right.region());
         case SIZE -> Double.compare(left.actualSize(), right.actualSize());
      };
   }

   record IndexedValue<T>(T value, Descriptor descriptor, int originalIndex) {
   }

   public record Descriptor(
      String alphabeticalName,
      double mutationRarity,
      double percentile,
      int tideRarity,
      String region,
      double actualSize,
      String registryId,
      String mutationId,
      long deterministicSeed
   ) {
      public Descriptor {
         alphabeticalName = normalize(alphabeticalName);
         region = normalize(region);
         registryId = normalize(registryId);
         mutationId = normalize(mutationId);
         mutationRarity = finiteOr(mutationRarity, Double.MAX_VALUE);
         percentile = finiteOr(percentile, -1.0);
         actualSize = finiteOr(actualSize, 0.0);
      }

      private static String normalize(String value) {
         return value == null ? "" : value.toLowerCase(Locale.ROOT);
      }

      private static double finiteOr(double value, double fallback) {
         return Double.isFinite(value) ? value : fallback;
      }
   }

   public record TraitData(String mutationId, double configuredMutationProbability, double percentile, long deterministicSeed) {
      public static final TraitData NORMAL = new TraitData("normal", Double.MAX_VALUE, -1.0, 0L);
   }

   public static final class TideMetadataResolver implements Function<ItemStack, Descriptor> {
      private final Function<ItemStack, TraitData> traitResolver;

      public TideMetadataResolver(Function<ItemStack, TraitData> traitResolver) {
         this.traitResolver = Objects.requireNonNull(traitResolver, "traitResolver");
      }

      public static TideMetadataResolver tideOnly() {
         return new TideMetadataResolver(stack -> TraitData.NORMAL);
      }

      @Override
      public Descriptor apply(ItemStack stack) {
         TraitData trait = Objects.requireNonNullElse(this.traitResolver.apply(stack), TraitData.NORMAL);
         FishData data = (FishData)FishData.get(stack).orElse(null);
         int rarity = data == null ? -1 : data.profile().rarity().ordinal();
         String region = data == null ? "other / unknown" : data.profile().location().orElse("other / unknown");
         double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
         String registryId = Registries.ITEM.getId(stack.getItem()).toString();
         return new Descriptor(
            stack.getName().getString().toLowerCase(Locale.ROOT),
            trait.configuredMutationProbability(),
            trait.percentile(),
            rarity,
            region,
            length,
            registryId,
            trait.mutationId(),
            trait.deterministicSeed()
         );
      }
   }
}
