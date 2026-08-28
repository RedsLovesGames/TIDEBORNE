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
}

