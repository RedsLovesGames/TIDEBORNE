package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class SpeciesSelectionServiceTest {
    private final SpeciesSelectionService selection = new SpeciesSelectionService();

    @Test
    void fishingLuckChangesRelativeRarityWithoutCreatingANoCatchResult() {
        SpeciesProfile common = profile("tide:common", CanonicalRarity.ONE_STAR, SpeciesEligibility.always());
        SpeciesProfile rare = profile("tide:rare", CanonicalRarity.FIVE_STAR, SpeciesEligibility.always());

        int baselineRare = rareSelections(List.of(common, rare), FishingContext.neutral(), 80_000, 8128L);
        FishingContext lucky = new FishingContext(0.0, 15.0, 0.0, Map.of(), Map.of(), Map.of());
        int luckyRare = rareSelections(List.of(common, rare), lucky, 80_000, 8128L);

        assertTrue(baselineRare > 38_000 && baselineRare < 42_000);
        assertTrue(luckyRare > 53_000 && luckyRare < 57_000);
    }

    @Test
    void traitLuckDoesNotAffectSpeciesSelection() {
        List<SpeciesProfile> profiles = List.of(
                profile("tide:one", CanonicalRarity.ONE_STAR, SpeciesEligibility.always()),
                profile("tide:three", CanonicalRarity.THREE_STAR, SpeciesEligibility.always()),
                profile("tide:five", CanonicalRarity.FIVE_STAR, SpeciesEligibility.always())
        );
        FishingContext noTraitLuck = new FishingContext(0.0, 7.0, 0.0, Map.of(), Map.of(), Map.of());
        FishingContext extremeTraitLuck = new FishingContext(0.0, 7.0, 1_000_000.0, Map.of(), Map.of(), Map.of());
        SplittableRandom first = new SplittableRandom(92821L);
        SplittableRandom second = new SplittableRandom(92821L);

        for (int i = 0; i < 10_000; i++) {
            assertEquals(
                    selection.select(profiles, noTraitLuck, FishingEnvironment.empty(), first).speciesId(),
                    selection.select(profiles, extremeTraitLuck, FishingEnvironment.empty(), second).speciesId()
            );
        }
    }

    @Test
    void seededSelectionIsDeterministic() {
        List<SpeciesProfile> profiles = List.of(
                profile("tide:one", CanonicalRarity.ONE_STAR, SpeciesEligibility.always()),
                profile("tide:five", CanonicalRarity.FIVE_STAR, SpeciesEligibility.always())
        );
        FishingContext context = new FishingContext(0.0, 15.0, 0.0, Map.of(), Map.of(), Map.of());
        SplittableRandom first = new SplittableRandom(42L);
        SplittableRandom second = new SplittableRandom(42L);

        for (int i = 0; i < 1_000; i++) {
            assertEquals(
                    selection.select(profiles, context, FishingEnvironment.empty(), first).speciesId(),
                    selection.select(profiles, context, FishingEnvironment.empty(), second).speciesId()
            );
        }
    }

    @Test
    void filtersByCanonicalEligibility() {
        SpeciesProfile ocean = profile(
                "compat:ocean_fish",
                CanonicalRarity.THREE_STAR,
                SpeciesEligibility.anyHabitat(Set.of("ocean"))
        );
        SpeciesProfile river = profile(
                "compat:river_fish",
                CanonicalRarity.THREE_STAR,
                SpeciesEligibility.anyHabitat(Set.of("river"))
        );

        FishingEnvironment environment = new FishingEnvironment(Set.of("river"), Map.of());
        assertEquals(
                "compat:river_fish",
                selection.select(
                        List.of(ocean, river),
                        FishingContext.neutral(),
                        environment,
                        new SplittableRandom(1L)
                ).speciesId()
        );
    }

    @Test
    void rejectsAnEmptyEligiblePool() {
        assertThrows(IllegalArgumentException.class, () -> selection.select(
                List.of(),
                FishingContext.neutral(),
                FishingEnvironment.empty(),
                new SplittableRandom(1L)
        ));
    }

    private int rareSelections(
            List<SpeciesProfile> profiles,
            FishingContext context,
            int attempts,
            long seed
    ) {
        int rare = 0;
        SplittableRandom random = new SplittableRandom(seed);
        for (int i = 0; i < attempts; i++) {
            SpeciesProfile selected = selection.select(profiles, context, FishingEnvironment.empty(), random);
            if (selected.rarity() == CanonicalRarity.FIVE_STAR) {
                rare++;
            }
        }
        return rare;
    }

    private static SpeciesProfile profile(
            String id,
            CanonicalRarity rarity,
            SpeciesEligibility eligibility
    ) {
        return new SpeciesProfile(
                id,
                rarity,
                1.0,
                eligibility,
                0.8,
                1.0,
                "steady",
                new LogNormalSizeDistribution(10.0, 0.25),
                Set.of(),
                Map.of()
        );
    }
}
