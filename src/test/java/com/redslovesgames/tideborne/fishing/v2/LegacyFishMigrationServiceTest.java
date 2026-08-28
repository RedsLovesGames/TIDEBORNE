package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LegacyFishMigrationServiceTest {
    private final LegacyFishMigrationService migration = new LegacyFishMigrationService();
    private final SpeciesProfile species = species(new LogNormalSizeDistribution(40.0, 0.35));

    @Test
    void mapsLegacyBodyTypes() {
        assertEquals(SpecimenData.BodyType.GIANT, migrate("normal", "Giant").bodyType());
        assertEquals(SpecimenData.BodyType.DWARF, migrate("normal", "dwarf").bodyType());
        assertEquals(SpecimenData.BodyType.GIANT, migrate("giant", null).bodyType());
        assertEquals(SpecimenData.BodyType.DWARF, migrate("dwarf", null).bodyType());
    }

    @Test
    void mapsLegacyConditionPigmentationAndQuality() {
        assertEquals(SpecimenData.Condition.SCARRED, migrate("Scarred", null).condition());
        assertEquals(SpecimenData.Condition.PARASITE_RIDDEN, migrate("Parasite", null).condition());
        assertEquals(SpecimenData.Condition.PARASITE_RIDDEN, migrate("Parasite-Ridden", null).condition());
        assertEquals(SpecimenData.Pigmentation.ALBINO, migrate("Albino", null).pigmentation());
        assertEquals(SpecimenData.Pigmentation.IRIDESCENT, migrate("Iridescent", null).pigmentation());
        assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, migrate("Perfect Specimen", null).specimenQuality());
    }

    @Test
    void preservesLegacyPhysicalLengthAndInfersPercentileFromSpeciesDistribution() {
        SpecimenData migrated = migration.migrate(
                species,
                legacy(null, null, 40.0, "normal", null)
        ).specimen();

        assertEquals(40.0, migrated.baseLength(), 1.0e-12);
        assertEquals(40.0, migrated.finalLength(), 1.0e-12);
        assertEquals(50.0, migrated.basePercentile(), 1.0e-10);
        assertEquals(50.0, migrated.finalPercentile(), 1.0e-10);
        assertEquals("species-cdf", migrated.provenance().attributes().get("percentileSource"));
        assertEquals("legacy-physical-length", migrated.provenance().attributes().get("sizeSource"));
    }

    @Test
    void preservesNaturalPercentileWhileKeepingLegacyFinalPhysicalSize() {
        SpecimenData migrated = migration.migrate(
                species,
                legacy(25.0, 77L, 40.0, "Giant", null)
        ).specimen();

        assertEquals(25.0, migrated.basePercentile(), 0.0);
        assertEquals(species.sizeDistribution().quantile(0.25), migrated.baseLength(), 1.0e-10);
        assertEquals(40.0, migrated.finalLength(), 0.0);
        assertEquals(50.0, migrated.finalPercentile(), 1.0e-10);
        assertEquals(SpecimenData.BodyType.GIANT, migrated.bodyType());
    }

    @Test
    void derivesMissingSeedDeterministicallyWithoutGeneratingSpecimenTraits() {
        LegacyFishMigrationService.LegacyFish input = legacy(62.5, null, 48.0, "Albino", null);

        SpecimenData first = migration.migrate(species, input).specimen();
        SpecimenData second = migration.migrate(species, input).specimen();

        assertEquals(first, second);
        assertEquals(first.deterministicSeed(), second.deterministicSeed());
        assertEquals(SpecimenData.Pigmentation.ALBINO, first.pigmentation());
        assertEquals(SpecimenData.Condition.NORMAL, first.condition());
        assertEquals(SpecimenData.BodyType.NORMAL, first.bodyType());
        assertEquals("derived", first.provenance().attributes().get("seedSource"));
    }

    @Test
    void preservesLegacySeedWhenPresent() {
        SpecimenData migrated = migration.migrate(
                species,
                legacy(40.0, 0x1234ABCDL, 35.0, "scarred", null)
        ).specimen();

        assertEquals(0x1234ABCDL, migrated.deterministicSeed());
        assertEquals("legacy", migrated.provenance().attributes().get("seedSource"));
    }

    @Test
    void writesCanonicalSchemaAndSecondPassIsExactNoOp() {
        LegacyFishMigrationService.MigrationResult first = migration.migrate(
                species,
                legacy(null, null, 40.0, "Iridescent", "Dwarf")
        );
        LegacyFishMigrationService.MigrationResult second = migration.migrate(
                species,
                LegacyFishMigrationService.LegacyFish.canonical(first.specimen())
        );

        assertTrue(first.migrated());
        assertEquals(SpecimenGenerator.SCHEMA_VERSION, first.specimen().schemaVersion());
        assertFalse(second.migrated());
        assertSame(first.specimen(), second.specimen());
        assertEquals(first.specimen(), second.specimen());
    }

    @Test
    void canonicalSpecimenNeverTouchesDistributionOrRegenerates() {
        SpecimenData canonical = canonicalSpecimen();
        SpeciesProfile throwingSpecies = species(new SizeDistribution() {
            @Override
            public double cdf(double length) {
                throw new AssertionError("canonical migration must not inspect size distribution");
            }

            @Override
            public double quantile(double probability) {
                throw new AssertionError("canonical migration must not inspect size distribution");
            }
        });

        LegacyFishMigrationService.MigrationResult result = migration.migrate(
                throwingSpecies,
                LegacyFishMigrationService.LegacyFish.canonical(canonical)
        );

        assertFalse(result.migrated());
        assertSame(canonical, result.specimen());
    }

    @Test
    void schemaV2MarkerWithoutCanonicalDataIsNeverRegenerated() {
        LegacyFishMigrationService.LegacyFish incompleteCanonical = new LegacyFishMigrationService.LegacyFish(
                species.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                12L,
                50.0,
                40.0,
                "scarred",
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> migration.migrate(species, incompleteCanonical));
    }

    @Test
    void speciesWithoutPhysicalSizeKeepsZeroLengthAndDeterministicPercentile() {
        SpeciesProfile sizeless = species(NoPhysicalSizeDistribution.INSTANCE);
        LegacyFishMigrationService.LegacyFish input = legacy(null, null, 55.0, "Parasite", null);

        SpecimenData first = migration.migrate(sizeless, input).specimen();
        SpecimenData second = migration.migrate(sizeless, input).specimen();

        assertEquals(0.0, first.baseLength(), 0.0);
        assertEquals(0.0, first.finalLength(), 0.0);
        assertEquals(first.basePercentile(), first.finalPercentile(), 0.0);
        assertEquals(first, second);
    }

    private SpecimenData migrate(String mutation, String bodyType) {
        return migration.migrate(species, legacy(50.0, 123L, 40.0, mutation, bodyType)).specimen();
    }

    private LegacyFishMigrationService.LegacyFish legacy(
            Double percentile,
            Long seed,
            Double length,
            String mutation,
            String bodyType
    ) {
        return new LegacyFishMigrationService.LegacyFish(
                species.speciesId(),
                null,
                seed,
                percentile,
                length,
                mutation,
                bodyType,
                null
        );
    }

    private SpeciesProfile species(SizeDistribution distribution) {
        return new SpeciesProfile(
                "tide:test_fish",
                CanonicalRarity.ONE_STAR,
                1.0,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                distribution,
                Set.of(),
                Map.of()
        );
    }

    private SpecimenData canonicalSpecimen() {
        return new SpecimenData(
                species.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                991L,
                73.0,
                45.0,
                54.0,
                86.0,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(500.0),
                OptionalInt.of(1200),
                new SpecimenData.Provenance("test", "test", Map.of("kept", "true"))
        );
    }
}
