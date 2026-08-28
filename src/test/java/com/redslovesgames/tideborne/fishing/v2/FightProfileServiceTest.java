package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FightProfileServiceTest {
    private final FightProfileService service = new FightProfileService();

    @Test
    void tempoNormalizationMatchesDesignTargets() {
        assertEquals(0.044, service.normalizeTempo(0.05), 0.002);
        assertEquals(0.056, service.normalizeTempo(0.25), 0.002);
        assertEquals(0.070, service.normalizeTempo(0.50), 0.002);
        assertEquals(0.091, service.normalizeTempo(1.00), 0.002);
        assertEquals(0.106, service.normalizeTempo(1.50), 0.002);
        assertEquals(0.119, service.normalizeTempo(2.00), 0.002);
        assertEquals(0.125, service.normalizeTempo(2.20), 0.002);
    }

    @Test
    void externalFightOutliersClampSafely() {
        assertEquals(service.normalizeTempo(0.0), service.normalizeTempo(-10.0));
        assertEquals(service.normalizeTempo(2.2), service.normalizeTempo(100.0));
        assertEquals(0.0, service.normalizeStrength(-10.0));
        assertEquals(1.1, service.normalizeStrength(100.0));
    }

    @Test
    void canonicalStrengthValuesRemainMeaningfullyDifferent() {
        double area07 = service.catchZoneArea(0.7);
        double area08 = service.catchZoneArea(0.8);
        double area09 = service.catchZoneArea(0.9);
        double area10 = service.catchZoneArea(1.0);

        assertTrue(area07 > area08);
        assertTrue(area08 > area09);
        assertTrue(area09 > area10);
        assertTrue(area07 - area08 > 0.05);
        assertTrue(area08 - area09 > 0.05);
        assertTrue(area09 - area10 > 0.05);
    }

    @Test
    void sizeScalesFightWithoutReplacingSpeciesProfile() {
        SpeciesProfile species = profile(0.8, 1.0);
        FightProfile small = service.create(species, specimen(0.0));
        FightProfile median = service.create(species, specimen(50.0));
        FightProfile large = service.create(species, specimen(100.0));

        assertEquals(0.88, small.strength() / median.strength(), 1.0e-12);
        assertEquals(1.12, large.strength() / median.strength(), 1.0e-12);
        assertEquals(1.06, small.tempo() / median.tempo(), 1.0e-12);
        assertEquals(0.94, large.tempo() / median.tempo(), 1.0e-12);
        assertTrue(small.catchZoneArea() > median.catchZoneArea());
        assertTrue(median.catchZoneArea() > large.catchZoneArea());
    }

    @Test
    void bodyTypeModifiesAlreadyPercentileScaledCanonicalFightProfile() {
        SpeciesProfile species = profile(0.8, 1.0);
        double percentile = 65.0;
        FightProfile normal = service.create(species, specimen(percentile, SpecimenData.BodyType.NORMAL));
        FightProfile giant = service.create(species, specimen(percentile, SpecimenData.BodyType.GIANT));
        FightProfile dwarf = service.create(species, specimen(percentile, SpecimenData.BodyType.DWARF));

        assertEquals(FightProfileService.GIANT_STRENGTH_MULTIPLIER, giant.strength() / normal.strength(), 1.0e-12);
        assertEquals(FightProfileService.GIANT_TEMPO_MULTIPLIER, giant.tempo() / normal.tempo(), 1.0e-12);
        assertEquals(FightProfileService.DWARF_STRENGTH_MULTIPLIER, dwarf.strength() / normal.strength(), 1.0e-12);
        assertEquals(FightProfileService.DWARF_TEMPO_MULTIPLIER, dwarf.tempo() / normal.tempo(), 1.0e-12);
        assertEquals(service.catchZoneArea(giant.strength()), giant.catchZoneArea(), 1.0e-12);
        assertEquals(service.catchZoneArea(dwarf.strength()), dwarf.catchZoneArea(), 1.0e-12);
        assertTrue(giant.catchZoneArea() < normal.catchZoneArea());
        assertTrue(dwarf.catchZoneArea() > normal.catchZoneArea());
        assertEquals(normal.behavior(), giant.behavior());
        assertEquals(normal.behavior(), dwarf.behavior());
    }

    @Test
    void normalBodyTypeLeavesPercentileFightScalingUnchanged() {
        SpeciesProfile species = profile(0.8, 1.0);
        double percentile = 65.0;
        FightProfile normal = service.create(species, specimen(percentile, SpecimenData.BodyType.NORMAL));

        double expectedStrength = service.normalizeStrength(species.strength()) * service.strengthMultiplier(percentile);
        double expectedTempo = service.normalizeTempo(species.tempo()) * service.tempoMultiplier(percentile);
        assertEquals(expectedStrength, normal.strength(), 1.0e-12);
        assertEquals(expectedTempo, normal.tempo(), 1.0e-12);
    }

    private static SpeciesProfile profile(double strength, double tempo) {
        return new SpeciesProfile(
                "tide:test_fish",
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                strength,
                tempo,
                "steady",
                new LogNormalSizeDistribution(25.0, 0.4),
                Set.of(),
                Map.of()
        );
    }

    private static SpecimenData specimen(double percentile) {
        return specimen(percentile, SpecimenData.BodyType.NORMAL);
    }

    private static SpecimenData specimen(double percentile, SpecimenData.BodyType bodyType) {
        return new SpecimenData(
                "tide:test_fish",
                2,
                1,
                42L,
                percentile,
                25.0,
                25.0,
                percentile,
                bodyType,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                SpecimenData.Provenance.generated()
        );
    }
}
