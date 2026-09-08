/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.config;

import com.redslovesgames.tidetraits.trait.FishMutation;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record TideTraitsConfig(
   Map<FishMutation, Double> mutationProbabilities,
   boolean redistributeIneligibleMutationOdds,
   TideTraitsConfig.DoubleRange dwarfLengthMultiplier,
   TideTraitsConfig.DoubleRange giantLengthMultiplier,
   TideTraitsConfig.DoubleRange parasiteLengthMultiplier,
   TideTraitsConfig.DoubleRange perfectSpecimenNormalPercentile
) {
   private static final double PROBABILITY_EPSILON = 1.0E-12;

   public TideTraitsConfig(
      Map<FishMutation, Double> mutationProbabilities,
      boolean redistributeIneligibleMutationOdds,
      TideTraitsConfig.DoubleRange dwarfLengthMultiplier,
      TideTraitsConfig.DoubleRange giantLengthMultiplier,
      TideTraitsConfig.DoubleRange parasiteLengthMultiplier,
      TideTraitsConfig.DoubleRange perfectSpecimenNormalPercentile
   ) {
      Objects.requireNonNull(mutationProbabilities, "mutationProbabilities");
      Objects.requireNonNull(dwarfLengthMultiplier, "dwarfLengthMultiplier");
      Objects.requireNonNull(giantLengthMultiplier, "giantLengthMultiplier");
      Objects.requireNonNull(parasiteLengthMultiplier, "parasiteLengthMultiplier");
      Objects.requireNonNull(perfectSpecimenNormalPercentile, "perfectSpecimenNormalPercentile");
      EnumMap<FishMutation, Double> probabilities = new EnumMap<>(FishMutation.class);
      double total = 0.0;

      for (FishMutation mutation : FishMutation.mutations()) {
         double probability = mutationProbabilities.getOrDefault(mutation, 0.0);
         if (!Double.isFinite(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability for " + mutation.serializedName() + " must be finite and in [0, 1]");
         }

         probabilities.put(mutation, probability);
         if (mutation != FishMutation.DWARF && mutation != FishMutation.GIANT && mutation != FishMutation.PERFECT_SPECIMEN) {
            total += probability;
         }
      }

      if (mutationProbabilities.containsKey(FishMutation.NORMAL)) {
         throw new IllegalArgumentException("NORMAL probability is derived and must not be configured");
      }

      if (total > 1.000000000001) {
         throw new IllegalArgumentException("Configured mutation probabilities total more than 1.0: " + total);
      }

      requirePositiveRange(dwarfLengthMultiplier, "dwarfLengthMultiplier");
      requirePositiveRange(giantLengthMultiplier, "giantLengthMultiplier");
      requirePositiveRange(parasiteLengthMultiplier, "parasiteLengthMultiplier");
      if (!(perfectSpecimenNormalPercentile.minInclusive() < 0.0) && !(perfectSpecimenNormalPercentile.maxInclusive() > 100.0)) {
         mutationProbabilities = Collections.unmodifiableMap(probabilities);
         this.mutationProbabilities = mutationProbabilities;
         this.redistributeIneligibleMutationOdds = redistributeIneligibleMutationOdds;
         this.dwarfLengthMultiplier = dwarfLengthMultiplier;
         this.giantLengthMultiplier = giantLengthMultiplier;
         this.parasiteLengthMultiplier = parasiteLengthMultiplier;
         this.perfectSpecimenNormalPercentile = perfectSpecimenNormalPercentile;
      } else {
         throw new IllegalArgumentException("Perfect Specimen percentile range must be within [0, 100]");
      }
   }

   public static TideTraitsConfig defaults() {
      return builder().build();
   }

   public double probability(FishMutation mutation) {
      Objects.requireNonNull(mutation, "mutation");
      return mutation == FishMutation.NORMAL ? Math.max(0.0, 1.0 - this.totalMutationProbability()) : this.mutationProbabilities.get(mutation);
   }

   public double totalMutationProbability() {
      return TraitAxesRuntime.conditionProbabilityTotal(this);
   }

   public TideTraitsConfig.Builder toBuilder() {
      return new TideTraitsConfig.Builder(this);
   }

   public static TideTraitsConfig.Builder builder() {
      return new TideTraitsConfig.Builder();
   }

   private static void requirePositiveRange(TideTraitsConfig.DoubleRange range, String name) {
      if (range.minInclusive() <= 0.0) {
         throw new IllegalArgumentException(name + " must be strictly positive");
      }
   }

   public static final class Builder {
      private final EnumMap<FishMutation, Double> mutationProbabilities = new EnumMap<>(FishMutation.class);
      private boolean redistributeIneligibleMutationOdds;
      private TideTraitsConfig.DoubleRange dwarfLengthMultiplier = new TideTraitsConfig.DoubleRange(0.55, 0.8);
      private TideTraitsConfig.DoubleRange giantLengthMultiplier = new TideTraitsConfig.DoubleRange(1.08, 1.3);
      private TideTraitsConfig.DoubleRange parasiteLengthMultiplier = new TideTraitsConfig.DoubleRange(0.9, 0.97);
      private TideTraitsConfig.DoubleRange perfectSpecimenNormalPercentile = new TideTraitsConfig.DoubleRange(95.0, 100.0);

      private Builder() {
         this.mutationProbabilities.put(FishMutation.SCARRED, 0.02857142857142857);
         this.mutationProbabilities.put(FishMutation.PARASITE_RIDDEN, 0.016666666666666666);
         this.mutationProbabilities.put(FishMutation.DWARF, 0.0125);
         this.mutationProbabilities.put(FishMutation.GIANT, 0.008333333333333333);
         this.mutationProbabilities.put(FishMutation.ALBINO, 0.004);
         this.mutationProbabilities.put(FishMutation.PERFECT_SPECIMEN, 0.0025);
         this.mutationProbabilities.put(FishMutation.IRIDESCENT, 0.0013333333333333333);
      }

      private Builder(TideTraitsConfig config) {
         this.mutationProbabilities.putAll(config.mutationProbabilities);
         this.redistributeIneligibleMutationOdds = config.redistributeIneligibleMutationOdds;
         this.dwarfLengthMultiplier = config.dwarfLengthMultiplier;
         this.giantLengthMultiplier = config.giantLengthMultiplier;
         this.parasiteLengthMultiplier = config.parasiteLengthMultiplier;
         this.perfectSpecimenNormalPercentile = config.perfectSpecimenNormalPercentile;
      }

      public TideTraitsConfig.Builder probability(FishMutation mutation, double probability) {
         if (Objects.requireNonNull(mutation, "mutation") == FishMutation.NORMAL) {
            throw new IllegalArgumentException("NORMAL probability is derived and must not be configured");
         }

         this.mutationProbabilities.put(mutation, probability);
         return this;
      }

      public TideTraitsConfig.Builder clearMutationProbabilities() {
         this.mutationProbabilities.clear();
         return this;
      }

      public TideTraitsConfig.Builder redistributeIneligibleMutationOdds(boolean redistribute) {
         this.redistributeIneligibleMutationOdds = redistribute;
         return this;
      }

      public TideTraitsConfig.Builder dwarfLengthMultiplier(double minInclusive, double maxInclusive) {
         this.dwarfLengthMultiplier = new TideTraitsConfig.DoubleRange(minInclusive, maxInclusive);
         return this;
      }

      public TideTraitsConfig.Builder giantLengthMultiplier(double minInclusive, double maxInclusive) {
         this.giantLengthMultiplier = new TideTraitsConfig.DoubleRange(minInclusive, maxInclusive);
         return this;
      }

      public TideTraitsConfig.Builder parasiteLengthMultiplier(double minInclusive, double maxInclusive) {
         this.parasiteLengthMultiplier = new TideTraitsConfig.DoubleRange(minInclusive, maxInclusive);
         return this;
      }

      public TideTraitsConfig.Builder perfectSpecimenNormalPercentile(double minInclusive, double maxInclusive) {
         this.perfectSpecimenNormalPercentile = new TideTraitsConfig.DoubleRange(minInclusive, maxInclusive);
         return this;
      }

      public TideTraitsConfig build() {
         return new TideTraitsConfig(
            this.mutationProbabilities,
            this.redistributeIneligibleMutationOdds,
            this.dwarfLengthMultiplier,
            this.giantLengthMultiplier,
            this.parasiteLengthMultiplier,
            this.perfectSpecimenNormalPercentile
         );
      }
   }

   public record DoubleRange(double minInclusive, double maxInclusive) {
      public DoubleRange {
         if (!Double.isFinite(minInclusive) || !Double.isFinite(maxInclusive)) {
            throw new IllegalArgumentException("Range endpoints must be finite");
         }

         if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("Range minimum must not exceed its maximum");
         }
      }

      public double sample(double unitValue) {
         if (Double.isFinite(unitValue) && !(unitValue < 0.0) && !(unitValue > 1.0)) {
            return this.minInclusive + (this.maxInclusive - this.minInclusive) * unitValue;
         } else {
            throw new IllegalArgumentException("Unit value must be within [0, 1]");
         }
      }
   }
}
