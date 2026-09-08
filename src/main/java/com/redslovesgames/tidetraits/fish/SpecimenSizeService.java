/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.fish;

import com.li64.tide.data.fishing.SizeData;
import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import com.redslovesgames.tidetraits.trait.DeterministicValues;
import com.redslovesgames.tidetraits.trait.SpecimenData;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.util.Identifier;

public final class SpecimenSizeService {
   private final FishPercentileService percentiles;

   public SpecimenSizeService(FishPercentileService percentiles) {
      this.percentiles = Objects.requireNonNull(percentiles, "percentiles");
   }

   public SpecimenSizeService.AppliedSize applyNew(SpecimenData specimen, double tideNormalLengthCm, FishDescriptor descriptor, TideTraitsConfig config) {
      Objects.requireNonNull(descriptor, "descriptor");
      return this.applyNew(specimen, tideNormalLengthCm, descriptor.canonicalSpeciesId(), descriptor.sizeData(), config);
   }

   public SpecimenSizeService.AppliedSize applyNew(
      SpecimenData specimen, double tideNormalLengthCm, Identifier canonicalSpeciesId, Optional<SizeData> sizeData, TideTraitsConfig config
   ) {
      Objects.requireNonNull(specimen, "specimen");
      Objects.requireNonNull(canonicalSpeciesId, "canonicalSpeciesId");
      Objects.requireNonNull(sizeData, "sizeData");
      Objects.requireNonNull(config, "config");
      if (!sizeData.isEmpty() && isUsablePhysicalLength(tideNormalLengthCm)) {
         Optional<EmpiricalFishDistribution> baseline = this.percentiles.baseline(canonicalSpeciesId, sizeData.get());
         if (baseline.isEmpty()) {
            return new SpecimenSizeService.AppliedSize(specimen.withoutPhysicalSizePercentile(), tideNormalLengthCm);
         }

         // Bytecode synthetic switch has only DWARF, GIANT, and PARASITE_RIDDEN
         // as reachable cases. PERFECT_SPECIMEN falls through to default.
         double finalLengthCm = switch (specimen.mutation()) {
            case DWARF -> tideNormalLengthCm * config.dwarfLengthMultiplier().sample(DeterministicValues.unitDouble(specimen.identitySeed(), 2611923443488327891L));
            case GIANT -> tideNormalLengthCm * config.giantLengthMultiplier().sample(DeterministicValues.unitDouble(specimen.identitySeed(), 1376283091369227076L));
            case PARASITE_RIDDEN -> tideNormalLengthCm
               * config.parasiteLengthMultiplier().sample(DeterministicValues.unitDouble(specimen.identitySeed(), -6626703657320631856L));
            default -> tideNormalLengthCm;
         };
         double percentile = baseline.get().percentileOf(tideNormalLengthCm);
         return new SpecimenSizeService.AppliedSize(specimen.withPhysicalSizePercentile(percentile), finalLengthCm);
      } else {
         return new SpecimenSizeService.AppliedSize(specimen.withoutPhysicalSizePercentile(), tideNormalLengthCm);
      }
   }

   public double recoverNormalLength(SpecimenData existingSpecimen, double finalLengthCm, TideTraitsConfig config) {
      Objects.requireNonNull(existingSpecimen, "existingSpecimen");
      Objects.requireNonNull(config, "config");
      if (!isUsablePhysicalLength(finalLengthCm)) {
         return finalLengthCm;
      }

      double multiplier = switch (existingSpecimen.mutation()) {
         case DWARF -> config.dwarfLengthMultiplier().sample(DeterministicValues.unitDouble(existingSpecimen.identitySeed(), 2611923443488327891L));
         case GIANT -> config.giantLengthMultiplier().sample(DeterministicValues.unitDouble(existingSpecimen.identitySeed(), 1376283091369227076L));
         case PARASITE_RIDDEN -> config.parasiteLengthMultiplier()
            .sample(DeterministicValues.unitDouble(existingSpecimen.identitySeed(), -6626703657320631856L));
         default -> 1.0;
      };
      return Double.isFinite(multiplier) && multiplier > 0.0 ? finalLengthCm / multiplier : finalLengthCm;
   }

   public SpecimenSizeService.AppliedSize classifyExistingFinalLength(
      SpecimenData specimen, double finalLengthCm, Identifier canonicalSpeciesId, Optional<SizeData> sizeData
   ) {
      Objects.requireNonNull(specimen, "specimen");
      Objects.requireNonNull(canonicalSpeciesId, "canonicalSpeciesId");
      Objects.requireNonNull(sizeData, "sizeData");
      if (!sizeData.isEmpty() && isUsablePhysicalLength(finalLengthCm)) {
         OptionalDouble percentile = this.percentiles.percentile(canonicalSpeciesId, sizeData.get(), finalLengthCm);
         return percentile.isPresent()
            ? new SpecimenSizeService.AppliedSize(specimen.withPhysicalSizePercentile(percentile.getAsDouble()), finalLengthCm)
            : new SpecimenSizeService.AppliedSize(specimen.withoutPhysicalSizePercentile(), finalLengthCm);
      } else {
         return new SpecimenSizeService.AppliedSize(specimen.withoutPhysicalSizePercentile(), finalLengthCm);
      }
   }

   private static boolean isUsablePhysicalLength(double lengthCm) {
      return Double.isFinite(lengthCm) && lengthCm > 0.0;
   }

   public record AppliedSize(SpecimenData specimen, double finalPhysicalLengthCm) {
      public AppliedSize {
         Objects.requireNonNull(specimen, "specimen");
      }

      public OptionalDouble percentile() {
         return this.specimen.physicalSizePercentile();
      }

      public Optional<FishSizeClass> sizeClass() {
         return this.percentile().isPresent() ? Optional.of(FishSizeClass.fromPercentile(this.percentile().getAsDouble())) : Optional.empty();
      }
   }
}
