package com.redslovesgames.tideborne.fishing.simulation;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.FishingContext;
import com.redslovesgames.tideborne.fishing.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.SpeciesSelectionService;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.specimen.TraitMomentumProgression;
import com.redslovesgames.tideborne.fishing.specimen.TraitMomentumStorage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SplittableRandom;

/**
 * Offline deterministic Fishing System 2.0 balance simulator.
 *
 * <p>This utility is intentionally separate from live catch integration. It owns no Minecraft entity,
 * ItemStack, player persistence, hook state, networking, or runtime authority. It consumes the same pure
 * canonical species selector, specimen generator, and Momentum transition rules as production catches.
 */
public final class FishingSimulator {
    private static final long CATCH_STEP = 0x9E3779B97F4A7C15L;
    private static final long SPECIES_SALT = 0x5350454349455353L;
    private static final long SPECIMEN_SALT = 0x53504543494D454EL;
    private static final long PERFECT_CATCH_SALT = 0x5045524645435443L;

    private final SpeciesSelectionService speciesSelection = new SpeciesSelectionService();
    private final SpecimenGenerator specimens = new SpecimenGenerator();

    public Result simulate(List<SpeciesProfile> species, Config config) {
        return simulate(species, FishingEnvironment.empty(), config);
    }

    public Result simulate(List<SpeciesProfile> species, FishingEnvironment environment, Config config) {
        return simulate(species, environment, config, FishingGearModifiers.neutral());
    }

    /** Full gear inputs use production's capped selector and TL boundaries; config luck remains non-gear. */
    public Result simulate(List<SpeciesProfile> species, FishingEnvironment environment, Config config, FishingGearModifiers gear) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(gear, "gear");

        List<SpeciesProfile> pool = filteredPool(species, config.rarityFilter());
        FishingContext context = new FishingContext(
                0.0,
                config.fishingLuck(),
                config.traitLuck(),
                Map.of(),
                Map.of(),
                Map.of()
        );

        Map<String, Long> speciesCounts = new HashMap<>();
        EnumMap<CanonicalRarity, Long> rarityCounts = enumCounts(CanonicalRarity.class);
        EnumMap<SpecimenData.BodyType, Long> bodyTypeCounts = enumCounts(SpecimenData.BodyType.class);
        EnumMap<SpecimenData.Condition, Long> conditionCounts = enumCounts(SpecimenData.Condition.class);
        EnumMap<SpecimenData.Pigmentation, Long> pigmentationCounts = enumCounts(SpecimenData.Pigmentation.class);
        EnumMap<SpecimenData.SpecimenQuality, Long> qualityCounts = enumCounts(SpecimenData.SpecimenQuality.class);
        Map<String, Integer> momentumBySpecies = new HashMap<>();

        NumericAccumulator naturalPercentiles = new NumericAccumulator(10, 0.0, 100.0);
        NumericAccumulator finalPercentiles = new NumericAccumulator(10, 0.0, 100.0);
        ScoreAccumulator scores = new ScoreAccumulator();
        long perfectCatchCount = 0L;
        long catchesUsingMomentum = 0L;
        long totalCapturedMomentum = 0L;
        int maxCapturedMomentum = 0;
        long momentumIncrements = 0L;
        long momentumResets = 0L;

        var weightedPool = speciesSelection.eligibleSpecies(pool, context, environment, gear);
        for (int index = 0; index < config.catchCount(); index++) {
            long catchSeed = catchSeed(config.simulationSeed(), index);
            SpeciesProfile selected = speciesSelection.selectWeighted(
                    weightedPool,
                    new SplittableRandom(mix64(catchSeed ^ SPECIES_SALT))
            );

            int capturedMomentum = config.momentumEnabled()
                    ? momentumBySpecies.getOrDefault(selected.speciesId(), 0)
                    : 0;
            double effectiveTraitLuck = TraitMomentumProgression.effectiveTraitLuck(
                    config.traitLuck() + FishingGearEffects.traitLuck(gear),
                    capturedMomentum
            );
            long specimenSeed = mix64(catchSeed ^ SPECIMEN_SALT);
            boolean perfectCatch = unitDouble(catchSeed, PERFECT_CATCH_SALT) < config.perfectCatchRate();

            SpecimenData preFight = specimens.generatePreFight(
                    selected,
                    specimenSeed,
                    SpecimenData.Provenance.generated(),
                    effectiveTraitLuck
            );
            SpecimenData specimen = specimens.finalizeAfterFight(
                    selected,
                    preFight,
                    effectiveTraitLuck,
                    perfectCatch
            );

            speciesCounts.merge(selected.speciesId(), 1L, Long::sum);
            increment(rarityCounts, selected.rarity());
            increment(bodyTypeCounts, specimen.bodyType());
            increment(conditionCounts, specimen.condition());
            increment(pigmentationCounts, specimen.pigmentation());
            increment(qualityCounts, specimen.specimenQuality());
            naturalPercentiles.add(specimen.basePercentile());
            finalPercentiles.add(specimen.finalPercentile());
            scores.add(specimen.fishScore().orElseThrow(() ->
                    new IllegalStateException("finalized canonical specimen is missing FishScore")));

            if (perfectCatch) {
                perfectCatchCount++;
            }
            if (capturedMomentum > 0) {
                catchesUsingMomentum++;
            }
            totalCapturedMomentum += capturedMomentum;
            maxCapturedMomentum = Math.max(maxCapturedMomentum, capturedMomentum);

            if (config.momentumEnabled()) {
                int nextMomentum = TraitMomentumProgression.nextMomentum(capturedMomentum, specimen);
                if (nextMomentum == 0 && capturedMomentum > 0) {
                    momentumResets++;
                } else if (nextMomentum > capturedMomentum) {
                    momentumIncrements++;
                }
                momentumBySpecies.put(selected.speciesId(), nextMomentum);
            }
        }

        return new Result(
                config.catchCount(),
                Map.copyOf(speciesCounts),
                Map.copyOf(rarityCounts),
                naturalPercentiles.finish(),
                finalPercentiles.finish(),
                Map.copyOf(bodyTypeCounts),
                Map.copyOf(conditionCounts),
                Map.copyOf(pigmentationCounts),
                Map.copyOf(qualityCounts),
                perfectCatchCount,
                new MomentumEffects(
                        catchesUsingMomentum,
                        totalCapturedMomentum,
                        maxCapturedMomentum,
                        momentumIncrements,
                        momentumResets,
                        Map.copyOf(momentumBySpecies)
                ),
                scores.finish()
        );
    }

    private static List<SpeciesProfile> filteredPool(
            List<SpeciesProfile> species,
            Optional<CanonicalRarity> rarityFilter
    ) {
        List<SpeciesProfile> filtered = new ArrayList<>();
        for (SpeciesProfile profile : species) {
            if (profile != null && (rarityFilter.isEmpty() || profile.rarity() == rarityFilter.get())) {
                filtered.add(profile);
            }
        }
        if (filtered.isEmpty()) {
            throw new IllegalArgumentException("simulation species pool is empty after rarity filtering");
        }
        return List.copyOf(filtered);
    }

    private static long catchSeed(long rootSeed, int index) {
        return mix64(rootSeed + CATCH_STEP * (index + 1L));
    }

    private static double unitDouble(long seed, long salt) {
        return new SplittableRandom(mix64(seed ^ salt)).nextDouble();
    }

    private static long mix64(long value) {
        long z = value;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static <E extends Enum<E>> EnumMap<E, Long> enumCounts(Class<E> type) {
        EnumMap<E, Long> counts = new EnumMap<>(type);
        for (E value : type.getEnumConstants()) {
            counts.put(value, 0L);
        }
        return counts;
    }

    private static <E extends Enum<E>> void increment(EnumMap<E, Long> counts, E value) {
        counts.put(value, counts.get(value) + 1L);
    }

    public record Config(
            long simulationSeed,
            int catchCount,
            double fishingLuck,
            double traitLuck,
            double perfectCatchRate,
            Optional<CanonicalRarity> rarityFilter,
            boolean momentumEnabled
    ) {
        public Config {
            if (catchCount <= 0) {
                throw new IllegalArgumentException("catchCount must be positive");
            }
            if (!Double.isFinite(fishingLuck)) {
                throw new IllegalArgumentException("fishingLuck must be finite");
            }
            if (!Double.isFinite(traitLuck)) {
                throw new IllegalArgumentException("traitLuck must be finite");
            }
            if (!Double.isFinite(perfectCatchRate) || perfectCatchRate < 0.0 || perfectCatchRate > 1.0) {
                throw new IllegalArgumentException("perfectCatchRate must be between 0 and 1");
            }
            rarityFilter = rarityFilter == null ? Optional.empty() : rarityFilter;
        }

        public static Config neutral(long simulationSeed, int catchCount) {
            return new Config(simulationSeed, catchCount, 0.0, 0.0, 0.0, Optional.empty(), true);
        }
    }

    public record Result(
            int catchCount,
            Map<String, Long> speciesCounts,
            Map<CanonicalRarity, Long> rarityCounts,
            Distribution naturalPercentiles,
            Distribution finalPercentiles,
            Map<SpecimenData.BodyType, Long> bodyTypeCounts,
            Map<SpecimenData.Condition, Long> conditionCounts,
            Map<SpecimenData.Pigmentation, Long> pigmentationCounts,
            Map<SpecimenData.SpecimenQuality, Long> qualityCounts,
            long perfectCatchCount,
            MomentumEffects momentum,
            FishScoreDistribution fishScores
    ) {
        public double rate(long count) {
            return catchCount == 0 ? 0.0 : (double) count / catchCount;
        }
    }

    /** Ten equal-width buckets. Percentile distributions use deciles [0,10), ... [90,100]. */
    public record Distribution(List<Long> buckets, double min, double max, double mean) {
        public Distribution {
            buckets = List.copyOf(buckets);
        }
    }

    public record MomentumEffects(
            long catchesUsingMomentum,
            long totalCapturedMomentum,
            int maxCapturedMomentum,
            long increments,
            long resets,
            Map<String, Integer> finalMomentumBySpecies
    ) {
        public MomentumEffects {
            finalMomentumBySpecies = Map.copyOf(finalMomentumBySpecies);
        }

        public double averageCapturedMomentum(int catchCount) {
            return catchCount <= 0 ? 0.0 : (double) totalCapturedMomentum / catchCount;
        }
    }

    /** FishScore bands are 1-500, 501-1000, 1001-1500, 1501-2000, 2001-2500, 2501-3000. */
    public record FishScoreDistribution(List<Long> bands, int min, int max, double mean) {
        public FishScoreDistribution {
            bands = List.copyOf(bands);
        }
    }

    private static final class NumericAccumulator {
        private final long[] buckets;
        private final double lower;
        private final double upper;
        private long count;
        private double sum;
        private double min = Double.POSITIVE_INFINITY;
        private double max = Double.NEGATIVE_INFINITY;

        private NumericAccumulator(int bucketCount, double lower, double upper) {
            this.buckets = new long[bucketCount];
            this.lower = lower;
            this.upper = upper;
        }

        private void add(double value) {
            if (!Double.isFinite(value) || value < lower || value > upper) {
                throw new IllegalArgumentException("distribution value outside configured bounds: " + value);
            }
            int bucket = value == upper
                    ? buckets.length - 1
                    : Math.min(buckets.length - 1,
                            (int) ((value - lower) / (upper - lower) * buckets.length));
            buckets[bucket]++;
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        private Distribution finish() {
            List<Long> result = new ArrayList<>(buckets.length);
            for (long bucket : buckets) {
                result.add(bucket);
            }
            return new Distribution(result, min, max, sum / count);
        }
    }

    private static final class ScoreAccumulator {
        private final long[] bands = new long[6];
        private long count;
        private long sum;
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;

        private void add(int score) {
            if (score < 1 || score > 3000) {
                throw new IllegalArgumentException("FishScore outside canonical range: " + score);
            }
            bands[Math.min(5, (score - 1) / 500)]++;
            count++;
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
        }

        private FishScoreDistribution finish() {
            List<Long> result = new ArrayList<>(bands.length);
            for (long band : bands) {
                result.add(band);
            }
            return new FishScoreDistribution(result, min, max, (double) sum / count);
        }
    }
}
