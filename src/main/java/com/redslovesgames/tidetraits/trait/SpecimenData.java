/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public record SpecimenData(long identitySeed, FishMutation mutation, Optional<Double> sizePercentile) {
   public static final Codec<SpecimenData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.LONG.fieldOf("identity_seed").forGetter(SpecimenData::identitySeed),
            FishMutation.CODEC.fieldOf("mutation").forGetter(SpecimenData::mutation),
            Codec.DOUBLE.optionalFieldOf("size_percentile").forGetter(SpecimenData::sizePercentile)
         )
         .apply(instance, SpecimenData::new)
   );

   public SpecimenData {
      Objects.requireNonNull(mutation, "mutation");
      Objects.requireNonNull(sizePercentile, "sizePercentile");
      sizePercentile.ifPresent(SpecimenData::validatePercentile);
   }

   public static SpecimenData unclassified(long identitySeed, FishMutation mutation) {
      return new SpecimenData(identitySeed, mutation, Optional.empty());
   }

   public static SpecimenData classified(long identitySeed, FishMutation mutation, double percentile) {
      return new SpecimenData(identitySeed, mutation, Optional.of(percentile));
   }

   public long mutationSeed() {
      return this.identitySeed;
   }

   public boolean hasPhysicalSize() {
      return this.sizePercentile.isPresent();
   }

   public OptionalDouble physicalSizePercentile() {
      return this.sizePercentile.isPresent() ? OptionalDouble.of(this.sizePercentile.get()) : OptionalDouble.empty();
   }

   public SpecimenData withPhysicalSizePercentile(double percentile) {
      return classified(this.identitySeed, this.mutation, percentile);
   }

   public SpecimenData withoutPhysicalSizePercentile() {
      return this.sizePercentile.isEmpty() ? this : unclassified(this.identitySeed, this.mutation);
   }

   private static void validatePercentile(double percentile) {
      if (!Double.isFinite(percentile) || percentile < 0.0 || percentile > 100.0) {
         throw new IllegalArgumentException("Size percentile must be finite and within [0, 100]");
      }
   }
}
