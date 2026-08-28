package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SpecimenGeneratorTest {
    private final SpecimenGenerator generator = new SpecimenGenerator();
    private final SpeciesProfile species = new SpeciesProfile(
            "tide:test_fish",
            CanonicalRarity.THREE_STAR,
            1.0,
            SpeciesEligibility.always(),
            0.8,
            1.0,
            "steady",
            new LogNormalSizeDistribution(25.0, 0.4),
            Set.of(),
            Map.of()
    );

    @Test
    void sameSeedProducesSameCanonicalIdentity() {
        SpecimenData first = generator.generateBase(species, 991234L, SpecimenData.Provenance.generated());
        SpecimenData second = generator.generateBase(species, 991234L, SpecimenData.Provenance.generated());

        assertEquals(first, second);
        assertEquals(first.baseLength(), first.finalLength());
        assertEquals(first.basePercentile(), first.finalPercentile());
        assertEquals(
                first.basePercentile(),
                species.sizeDistribution().percentile(first.baseLength()),
                2.0e-5
        );
        assertTrue(first.rawFishScore().isEmpty());
        assertTrue(first.fishScore().isEmpty());
    }

    @Test
    void differentSeedsProduceDifferentNaturalSpecimens() {
        SpecimenData first = generator.generateBase(species, 1L, SpecimenData.Provenance.generated());
        SpecimenData second = generator.generateBase(species, 2L, SpecimenData.Provenance.generated());

        assertNotEquals(first.basePercentile(), second.basePercentile());
        assertNotEquals(first.baseLength(), second.baseLength());
    }

    @Test
    void fullGenerationPreservesTheSingleNaturalSample() {
        long seed = 4_221_337L;
        SpecimenData base = generator.generateBase(species, seed, SpecimenData.Provenance.generated());
        SpecimenData finalized = generator.generate(species, seed, SpecimenData.Provenance.generated());

        assertEquals(base.basePercentile(), finalized.basePercentile());
        assertEquals(base.baseLength(), finalized.baseLength());
        assertEquals(seed, finalized.deterministicSeed());
        assertEquals(
                new BodyTypeGenerator().generate(seed, base.basePercentile(), species, 0.0),
                finalized.bodyType()
        );
    }

    @Test
    void traitLuckIsForwardedIntoBodyTypeGeneration() {
        long seed = 1_234_567L;
        SpecimenData base = generator.generateBase(species, seed, SpecimenData.Provenance.generated());
        SpecimenData finalized = generator.generate(species, seed, SpecimenData.Provenance.generated(), 25.0);

        assertEquals(
                new BodyTypeGenerator().generate(seed, base.basePercentile(), species, 25.0),
                finalized.bodyType()
        );
        assertEquals(base.basePercentile(), finalized.basePercentile());
        assertEquals(base.baseLength(), finalized.baseLength());
    }

    @Test
    void completeGenerationSamplesBaseSizeExactlyOnce() {
        CountingSizeDistribution counting = new CountingSizeDistribution(new LogNormalSizeDistribution(25.0, 0.4));
        SpeciesProfile countedSpecies = new SpeciesProfile(
                "tide:counted_fish",
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                0.8,
                1.0,
                "steady",
                counting,
                Set.of(),
                Map.of()
        );

        SpecimenData specimen = generator.generate(countedSpecies, 741852963L, SpecimenData.Provenance.generated());

        assertEquals(1, counting.quantileCalls,
                "canonical natural/base size must be sampled exactly once");
        assertTrue(counting.cdfCalls <= 1,
                "Body Type may deterministically derive finalPercentile with at most one CDF evaluation");
        assertEquals(
                specimen.basePercentile(),
                100.0 * counting.delegate.cdf(specimen.baseLength()),
                2.0e-5
        );
    }

    private static final class CountingSizeDistribution implements SizeDistribution {
        private final SizeDistribution delegate;
        private int quantileCalls;
        private int cdfCalls;

        private CountingSizeDistribution(SizeDistribution delegate) {
            this.delegate = delegate;
        }

        @Override
        public double cdf(double length) {
            cdfCalls++;
            return delegate.cdf(length);
        }

        @Override
        public double quantile(double probability) {
            quantileCalls++;
            return delegate.quantile(probability);
        }
    }
}
