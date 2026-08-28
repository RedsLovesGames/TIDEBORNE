/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.fish;

import com.li64.tide.data.fishing.SizeData;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class FishPercentileService {
   public static final int SAMPLE_COUNT = 4096;
   private final AtomicReference<ConcurrentMap<Identifier, FishPercentileService.CachedBaseline>> baselines = new AtomicReference<>(new ConcurrentHashMap<>());

   public OptionalDouble percentile(Identifier canonicalSpeciesId, SizeData sizeData, double finalPhysicalLengthCm) {
      return this.baseline(canonicalSpeciesId, sizeData)
         .map(distribution -> OptionalDouble.of(distribution.percentileOf(finalPhysicalLengthCm)))
         .orElseGet(OptionalDouble::empty);
   }

   public Optional<EmpiricalFishDistribution> baseline(Identifier canonicalSpeciesId, SizeData sizeData) {
      Objects.requireNonNull(canonicalSpeciesId, "canonicalSpeciesId");
      Objects.requireNonNull(sizeData, "sizeData");
      FishPercentileService.CachedBaseline cached = this.baselines
         .get()
         .compute(
            canonicalSpeciesId,
            (ignored, existing) -> (FishPercentileService.CachedBaseline)(existing != null && existing.source().equals(sizeData)
               ? existing
               : new FishPercentileService.CachedBaseline(sizeData, sample(canonicalSpeciesId, sizeData)))
         );
      return cached.distribution();
   }

   public void clear() {
      this.baselines.set(new ConcurrentHashMap<>());
   }

   public void rebuild(Map<Identifier, SizeData> speciesSizes) {
      Objects.requireNonNull(speciesSizes, "speciesSizes");
      ConcurrentMap<Identifier, FishPercentileService.CachedBaseline> rebuilt = new ConcurrentHashMap<>();
      speciesSizes.forEach((id, size) -> rebuilt.put(id, new FishPercentileService.CachedBaseline(size, sample(id, size))));
      this.baselines.set(rebuilt);
   }

   public void rebuild(Collection<FishDescriptor> descriptors) {
      Objects.requireNonNull(descriptors, "descriptors");
      ConcurrentMap<Identifier, FishPercentileService.CachedBaseline> rebuilt = new ConcurrentHashMap<>();

      for (FishDescriptor descriptor : descriptors) {
         descriptor.sizeData()
            .ifPresent(
               size -> rebuilt.put(
                  descriptor.canonicalSpeciesId(), new FishPercentileService.CachedBaseline(size, sample(descriptor.canonicalSpeciesId(), size))
               )
            );
      }

      this.baselines.set(rebuilt);
   }

   public int cachedSpeciesCount() {
      return this.baselines.get().size();
   }

   public static long deterministicSpeciesSeed(Identifier canonicalSpeciesId) {
      Objects.requireNonNull(canonicalSpeciesId, "canonicalSpeciesId");
      long hash = -3750763034362895579L;

      for (byte value : canonicalSpeciesId.toString().getBytes(StandardCharsets.UTF_8)) {
         hash ^= value & 255L;
         hash *= 1099511628211L;
      }

      hash ^= hash >>> 30;
      hash *= -4658895280553007687L;
      hash ^= hash >>> 27;
      hash *= -7723592293110705685L;
      return hash ^ hash >>> 31;
   }

   private static Optional<EmpiricalFishDistribution> sample(Identifier canonicalSpeciesId, SizeData sizeData) {
      Random random = Random.create(deterministicSpeciesSeed(canonicalSpeciesId));
      double[] samples = new double[4096];

      for (int index = 0; index < samples.length; index++) {
         samples[index] = sizeData.sample(random, 0.0);
      }

      return EmpiricalFishDistribution.fromSamples(samples);
   }

   private record CachedBaseline(SizeData source, Optional<EmpiricalFishDistribution> distribution) {
      private CachedBaseline {
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(distribution, "distribution");
      }
   }
}
