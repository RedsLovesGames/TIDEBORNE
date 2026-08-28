/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.trait;

import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class MutationSelector {
   private static final Predicate<FishMutation> ALL_ELIGIBLE = mutation -> true;

   public SpecimenData assignOrKeep(Optional<SpecimenData> explicitMarker, RandomGenerator random, TideTraitsConfig config, Predicate<FishMutation> eligibility) {
      Objects.requireNonNull(explicitMarker, "explicitMarker");
      Objects.requireNonNull(random, "random");
      Objects.requireNonNull(config, "config");
      Objects.requireNonNull(eligibility, "eligibility");
      if (explicitMarker.isPresent()) {
         return explicitMarker.get();
      }

      long identitySeed = random.nextLong();
      double roll = DeterministicValues.unitDouble(identitySeed, 6659551205387646661L);
      return SpecimenData.unclassified(identitySeed, this.select(roll, config, eligibility));
   }

   public SpecimenData assignOrKeep(Optional<SpecimenData> explicitMarker, RandomGenerator random, TideTraitsConfig config) {
      return this.assignOrKeep(explicitMarker, random, config, ALL_ELIGIBLE);
   }

   public FishMutation select(double roll, TideTraitsConfig config, Predicate<FishMutation> eligibility) {
      if (Double.isFinite(roll) && !(roll < 0.0) && !(roll >= 1.0)) {
         Objects.requireNonNull(config, "config");
         Objects.requireNonNull(eligibility, "eligibility");
         if (config.redistributeIneligibleMutationOdds()) {
            return this.selectWithRedistribution(roll, config, eligibility);
         }

         double var5 = 0.0;

         for (FishMutation var8 : FishMutation.mutations()) {
            if (var8 != FishMutation.DWARF && var8 != FishMutation.GIANT && var8 != FishMutation.PERFECT_SPECIMEN) {
               var5 += config.probability(var8);
               if (roll < var5) {
                  if (eligibility.test(var8)) {
                     return var8;
                  }

                  return FishMutation.NORMAL;
               }
            }
         }

         return FishMutation.NORMAL;
      } else {
         throw new IllegalArgumentException("Condition roll must be finite and within [0, 1)");
      }
   }

   public FishMutation select(double roll, TideTraitsConfig config) {
      return this.select(roll, config, ALL_ELIGIBLE);
   }

   private FishMutation selectWithRedistribution(double roll, TideTraitsConfig config, Predicate<FishMutation> eligibility) {
      double var5 = 0.0;

      for (FishMutation var8 : FishMutation.mutations()) {
         if (var8 != FishMutation.DWARF && var8 != FishMutation.GIANT && var8 != FishMutation.PERFECT_SPECIMEN && eligibility.test(var8)) {
            var5 += config.probability(var8);
         }
      }

      if (var5 <= 0.0) {
         return FishMutation.NORMAL;
      }

      double var9 = TraitAxesRuntime.conditionProbabilityTotal(config) / var5;
      double var11 = 0.0;

      for (FishMutation var14 : FishMutation.mutations()) {
         if (var14 != FishMutation.DWARF && var14 != FishMutation.GIANT && var14 != FishMutation.PERFECT_SPECIMEN && eligibility.test(var14)) {
            var11 += config.probability(var14) * var9;
            if (roll < var11) {
               return var14;
            }
         }
      }

      return FishMutation.NORMAL;
   }
}
