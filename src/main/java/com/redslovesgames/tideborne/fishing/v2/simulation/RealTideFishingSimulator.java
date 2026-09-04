package com.redslovesgames.tideborne.fishing.v2.simulation;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpeciesSelectionService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Monte Carlo balance simulator for context-normalized real Tide species pools.
 *
 * <p>The simulator deliberately does not own Tide fish definitions. Callers build each
 * {@link LocationPool} by passing Tide's live {@code FishData} records through
 * {@code TideSpeciesProfileAdapter#adapt}. That boundary runs Tide's real
 * {@code FishData#shouldKeep(FishingContext)}, environmental weight modifiers, authoritative
 * selection weights, rarity, strength/speed metadata, and real size distributions before this
 * class ever sees a fish.
 */
public final class RealTideFishingSimulator {
    private final SpeciesSelectionService selector = new SpeciesSelectionService();
    private final SpecimenGenerator specimens = new SpecimenGenerator();

    public Result simulate(List<LocationPool> locations, GearStage gear, long seed, int catches) {
        Objects.requireNonNull(gear, "gear");
        return simulate(locations, gear.profile(), seed, catches);
    }

    public Result simulate(List<LocationPool> locations, GearProfile gear, long seed, int catches) {
        requireLocations(locations);
        Objects.requireNonNull(gear, "gear");
        if (catches <= 0) throw new IllegalArgumentException("catches must be positive");

        SplittableRandom random = new SplittableRandom(seed);
        EnumMap<CanonicalRarity, Integer> rarityCounts = zeroRarityCounts();
        Map<String, Integer> speciesCounts = new HashMap<>();
        int[] scores = new int[catches];
        int anyTrait = 0;
        int perfectSpecimen = 0;
        int perfectCatch = 0;

        FishingContext selectionContext = new FishingContext(
                0.0,
                gear.fishingLuck(),
                gear.traitLuck(),
                Map.of(),
                Map.of(),
                Map.of()
        );

        for (int i = 0; i < catches; i++) {
            LocationPool location = locations.get(random.nextInt(locations.size()));
            SpeciesProfile species = selector.select(location.species(), selectionContext, location.environment(), random);
            boolean didPerfectCatch = random.nextDouble() < gear.perfectCatchRate();
            SpecimenData specimen = specimens.generatePreFight(
                    species,
                    random.nextLong(),
                    new SpecimenData.Provenance(
                            "balance-simulation",
                            "real-tide-catalog",
                            Map.of("location", location.id(), "gear", gear.id())
                    ),
                    gear.traitLuck()
            );
            specimen = specimens.finalizeAfterFight(species, specimen, gear.traitLuck(), didPerfectCatch);

            rarityCounts.merge(species.rarity(), 1, Integer::sum);
            speciesCounts.merge(species.speciesId(), 1, Integer::sum);
            scores[i] = specimen.fishScore().orElseThrow();
            if (hasAnyTrait(specimen)) anyTrait++;
            if (specimen.specimenQuality() == SpecimenData.SpecimenQuality.PERFECT_SPECIMEN) perfectSpecimen++;
            if (didPerfectCatch) perfectCatch++;
        }

        Arrays.sort(scores);
        return new Result(
                gear,
                catches,
                Collections.unmodifiableMap(rarityCounts),
                Collections.unmodifiableMap(new LinkedHashMap<>(speciesCounts)),
                mean(scores),
                percentile(scores, 0.50),
                percentile(scores, 0.90),
                percentile(scores, 0.99),
                scores[0],
                scores[scores.length - 1],
                anyTrait / (double) catches,
                perfectSpecimen / (double) catches,
                perfectCatch / (double) catches
        );
    }

    public ProgressionResult simulateProgression(
            List<LocationPool> locations,
            GearStage gear,
            long seed,
            int trials,
            int maxCatches
    ) {
        requireLocations(locations);
        Objects.requireNonNull(gear, "gear");
        if (trials <= 0 || maxCatches <= 0) throw new IllegalArgumentException("trials and maxCatches must be positive");

        Set<String> reachableSpecies = new HashSet<>();
        for (LocationPool location : locations) {
            for (SpeciesProfile profile : location.species()) reachableSpecies.add(profile.speciesId());
        }
        int catalogSize = reachableSpecies.size();
        int[] to25 = new int[trials];
        int[] to50 = new int[trials];
        int[] to75 = new int[trials];
        int[] to90 = new int[trials];
        int[] firstFourStar = new int[trials];
        int[] firstFiveStar = new int[trials];
        int[] firstPerfectSpecimen = new int[trials];
        int[] firstScore2000 = new int[trials];

        FishingContext selectionContext = new FishingContext(0.0, gear.fishingLuck(), gear.traitLuck(), Map.of(), Map.of(), Map.of());
        SplittableRandom root = new SplittableRandom(seed);

        for (int trial = 0; trial < trials; trial++) {
            SplittableRandom random = root.split();
            Set<String> discovered = new HashSet<>();
            int a25 = sentinel(maxCatches), a50 = sentinel(maxCatches), a75 = sentinel(maxCatches), a90 = sentinel(maxCatches);
            int four = sentinel(maxCatches), five = sentinel(maxCatches), perfect = sentinel(maxCatches), score2000 = sentinel(maxCatches);

            for (int catchIndex = 1; catchIndex <= maxCatches; catchIndex++) {
                LocationPool location = locations.get((catchIndex - 1) % locations.size());
                SpeciesProfile species = selector.select(location.species(), selectionContext, location.environment(), random);
                discovered.add(species.speciesId());

                boolean didPerfectCatch = random.nextDouble() < gear.perfectCatchRate();
                SpecimenData specimen = specimens.generatePreFight(
                        species,
                        random.nextLong(),
                        new SpecimenData.Provenance("balance-progression", "real-tide-catalog", Map.of()),
                        gear.traitLuck()
                );
                specimen = specimens.finalizeAfterFight(species, specimen, gear.traitLuck(), didPerfectCatch);

                double completion = catalogSize == 0 ? 1.0 : discovered.size() / (double) catalogSize;
                if (a25 > maxCatches && completion >= 0.25) a25 = catchIndex;
                if (a50 > maxCatches && completion >= 0.50) a50 = catchIndex;
                if (a75 > maxCatches && completion >= 0.75) a75 = catchIndex;
                if (a90 > maxCatches && completion >= 0.90) a90 = catchIndex;
                if (four > maxCatches && species.rarity().stars() >= 4) four = catchIndex;
                if (five > maxCatches && species.rarity().stars() >= 5) five = catchIndex;
                if (perfect > maxCatches && specimen.specimenQuality() == SpecimenData.SpecimenQuality.PERFECT_SPECIMEN) perfect = catchIndex;
                if (score2000 > maxCatches && specimen.fishScore().orElse(0) >= 2000) score2000 = catchIndex;

                if (a90 <= maxCatches && four <= maxCatches && five <= maxCatches && perfect <= maxCatches && score2000 <= maxCatches) break;
            }

            to25[trial] = a25;
            to50[trial] = a50;
            to75[trial] = a75;
            to90[trial] = a90;
            firstFourStar[trial] = four;
            firstFiveStar[trial] = five;
            firstPerfectSpecimen[trial] = perfect;
            firstScore2000[trial] = score2000;
        }

        return new ProgressionResult(
                gear,
                catalogSize,
                medianCensored(to25, maxCatches),
                medianCensored(to50, maxCatches),
                medianCensored(to75, maxCatches),
                medianCensored(to90, maxCatches),
                medianCensored(firstFourStar, maxCatches),
                medianCensored(firstFiveStar, maxCatches),
                medianCensored(firstPerfectSpecimen, maxCatches),
                medianCensored(firstScore2000, maxCatches)
        );
    }

    public static Map<CanonicalRarity, Double> rarityShares(Result result) {
        EnumMap<CanonicalRarity, Double> shares = new EnumMap<>(CanonicalRarity.class);
        result.rarityCounts().forEach((rarity, count) -> shares.put(rarity, count / (double) result.catches()));
        return Collections.unmodifiableMap(shares);
    }

    private static boolean hasAnyTrait(SpecimenData specimen) {
        return specimen.bodyType() != SpecimenData.BodyType.NORMAL
                || specimen.condition() != SpecimenData.Condition.NORMAL
                || specimen.pigmentation() != SpecimenData.Pigmentation.NORMAL
                || specimen.specimenQuality() != SpecimenData.SpecimenQuality.NORMAL;
    }

    private static EnumMap<CanonicalRarity, Integer> zeroRarityCounts() {
        EnumMap<CanonicalRarity, Integer> counts = new EnumMap<>(CanonicalRarity.class);
        for (CanonicalRarity rarity : CanonicalRarity.values()) counts.put(rarity, 0);
        return counts;
    }

    private static double mean(int[] values) {
        long sum = 0;
        for (int value : values) sum += value;
        return sum / (double) values.length;
    }

    private static int percentile(int[] sorted, double quantile) {
        int index = (int) Math.round((sorted.length - 1) * quantile);
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }

    private static int sentinel(int maxCatches) {
        return maxCatches + 1;
    }

    private static int medianCensored(int[] values, int maxCatches) {
        int[] copy = values.clone();
        Arrays.sort(copy);
        int value = copy[copy.length / 2];
        return value > maxCatches ? -1 : value;
    }

    private static void requireLocations(List<LocationPool> locations) {
        Objects.requireNonNull(locations, "locations");
        if (locations.isEmpty()) throw new IllegalArgumentException("at least one real Tide location pool is required");
        for (LocationPool location : locations) {
            Objects.requireNonNull(location, "locations contains null");
            if (location.species().isEmpty()) throw new IllegalArgumentException("location has no eligible Tide fish: " + location.id());
        }
    }

    public record LocationPool(String id, String description, FishingEnvironment environment, List<SpeciesProfile> species) {
        public LocationPool {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("location id must not be blank");
            description = description == null ? "" : description;
            Objects.requireNonNull(environment, "environment");
            species = List.copyOf(Objects.requireNonNull(species, "species"));
        }
    }

    public record GearProfile(String id, double fishingLuck, double traitLuck, double perfectCatchRate) {
        public GearProfile {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("gear id must not be blank");
            if (!Double.isFinite(fishingLuck) || !Double.isFinite(traitLuck)) throw new IllegalArgumentException("luck must be finite");
            if (!Double.isFinite(perfectCatchRate) || perfectCatchRate < 0.0 || perfectCatchRate > 1.0) {
                throw new IllegalArgumentException("perfectCatchRate must be in [0,1]");
            }
        }
    }

    /**
     * Representative progression stages. Native Tide luck is modeled as 0 or Luck of the Sea III;
     * Amethyst/Echo bobbers use their exact +1/+2 Trait Luck values; Leviathan Bait uses its exact
     * +15 Fishing Luck and +4 Trait Luck values. Perfect Catch rate is held constant across stages so
     * gear comparisons do not accidentally measure player skill.
     */
    public enum GearStage {
        STARTER("starter", 0.0, 0.0, 0.15),
        MIDGAME_AMETHYST("midgame_amethyst", 3.0, 1.0, 0.15),
        LATEGAME_ECHO("lategame_echo", 3.0, 2.0, 0.15),
        ENDGAME_LEVIATHAN("endgame_leviathan", 18.0, 6.0, 0.15);

        private final GearProfile profile;

        GearStage(String id, double fishingLuck, double traitLuck, double perfectCatchRate) {
            this.profile = new GearProfile(id, fishingLuck, traitLuck, perfectCatchRate);
        }

        public GearProfile profile() { return profile; }
        public String id() { return profile.id(); }
        public double fishingLuck() { return profile.fishingLuck(); }
        public double traitLuck() { return profile.traitLuck(); }
        public double perfectCatchRate() { return profile.perfectCatchRate(); }
    }

    public record Result(
            GearProfile gear,
            int catches,
            Map<CanonicalRarity, Integer> rarityCounts,
            Map<String, Integer> speciesCounts,
            double meanFishScore,
            int p50FishScore,
            int p90FishScore,
            int p99FishScore,
            int minFishScore,
            int maxFishScore,
            double anyTraitRate,
            double perfectSpecimenRate,
            double perfectCatchRate
    ) {}

    public record ProgressionResult(
            GearStage gear,
            int reachableSpecies,
            int medianCatchesTo25Percent,
            int medianCatchesTo50Percent,
            int medianCatchesTo75Percent,
            int medianCatchesTo90Percent,
            int medianCatchesToFirstFourStar,
            int medianCatchesToFirstFiveStar,
            int medianCatchesToFirstPerfectSpecimen,
            int medianCatchesToFirstScore2000
    ) {}
}
