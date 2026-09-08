package com.redslovesgames.tideborne.fishing.v2;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Pure deterministic migration from pre-V2 Tideborne fish identity into canonical Fishing System 2.0 data.
 *
 * <p>This service never calls the specimen generator. Existing canonical specimens are returned verbatim,
 * valid legacy physical length is preserved, and missing identity values are recovered deterministically.
 */
public final class LegacyFishMigrationService {
    private static final long DERIVED_SEED_SALT = 0xC6BC279692B5CC83L;
    private static final long FALLBACK_PERCENTILE_SALT = 0x9B8D2C4F6A17E305L;
    private static final long FNV_OFFSET_BASIS = 0xCBF29CE484222325L;
    private static final long FNV_PRIME = 0x100000001B3L;

    public MigrationResult migrate(SpeciesProfile species, LegacyFish legacy) {
        Objects.requireNonNull(species, "species");
        Objects.requireNonNull(legacy, "legacy");

        SpecimenData canonical = legacy.canonicalSpecimen();
        if (canonical != null) {
            if (!species.speciesId().equals(canonical.speciesId())) {
                throw new IllegalArgumentException("species profile and canonical specimen IDs must match");
            }
            return new MigrationResult(canonical, false);
        }

        if (!species.speciesId().equals(legacy.speciesId())) {
            throw new IllegalArgumentException("species profile and legacy fish IDs must match");
        }
        if (legacy.schemaVersion() != null && legacy.schemaVersion() >= SpecimenGenerator.SCHEMA_VERSION) {
            throw new IllegalArgumentException("canonical schema data must be supplied as an existing canonical specimen");
        }

        boolean preservedSeed = legacy.deterministicSeed() != null;
        long seed = preservedSeed ? legacy.deterministicSeed() : deriveSeed(legacy);
        SizeDistribution distribution = species.sizeDistribution();
        boolean hasPhysicalDistribution = !(distribution instanceof NoPhysicalSizeDistribution);
        boolean hasLegacyLength = hasPhysicalDistribution && isUsableLength(legacy.physicalLength());
        boolean hasLegacyPercentile = isUsablePercentile(legacy.percentile());

        double basePercentile;
        String percentileSource;
        if (hasLegacyPercentile) {
            basePercentile = legacy.percentile();
            percentileSource = "legacy-percentile";
        } else if (hasLegacyLength) {
            basePercentile = clampPercentile(distribution.percentile(legacy.physicalLength()));
            percentileSource = "species-cdf";
        } else {
            basePercentile = TraitRandom.unitDouble(seed, FALLBACK_PERCENTILE_SALT) * 100.0;
            percentileSource = "deterministic-fallback";
        }

        double baseLength;
        double finalLength;
        String sizeSource;
        if (!hasPhysicalDistribution) {
            baseLength = 0.0;
            finalLength = 0.0;
            sizeSource = "no-physical-size";
        } else if (hasLegacyLength) {
            finalLength = legacy.physicalLength();
            baseLength = hasLegacyPercentile
                    ? finiteQuantile(distribution, basePercentile)
                    : finalLength;
            sizeSource = "legacy-physical-length";
        } else {
            baseLength = finiteQuantile(distribution, basePercentile);
            finalLength = baseLength;
            sizeSource = "species-quantile";
        }

        double finalPercentile = hasPhysicalDistribution
                ? clampPercentile(distribution.percentile(finalLength))
                : basePercentile;

        String mutation = token(legacy.mutation());
        String legacyBodyType = token(legacy.bodyType());
        SpecimenData.BodyType bodyType = mapBodyType(legacyBodyType, mutation);
        SpecimenData.Condition condition = mapCondition(mutation);
        SpecimenData.Pigmentation pigmentation = mapPigmentation(mutation);
        SpecimenData.SpecimenQuality quality = mapQuality(mutation);

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("legacySchemaVersion", legacy.schemaVersion() == null ? "unversioned" : legacy.schemaVersion().toString());
        attributes.put("seedSource", preservedSeed ? "legacy" : "derived");
        attributes.put("percentileSource", percentileSource);
        attributes.put("sizeSource", sizeSource);
        if (!mutation.isEmpty()) {
            attributes.put("legacyMutation", mutation);
        }
        if (!legacyBodyType.isEmpty()) {
            attributes.put("legacyBodyType", legacyBodyType);
        }

        SpecimenData migratedIdentity = new SpecimenData(
                species.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                basePercentile,
                baseLength,
                finalLength,
                finalPercentile,
                bodyType,
                condition,
                pigmentation,
                quality,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                new SpecimenData.Provenance("legacy-migration", "fishing-system-2-migration", attributes)
        );
        return new MigrationResult(withCanonicalFishScore(species.rarity(), migratedIdentity), true);
    }

    private static SpecimenData withCanonicalFishScore(CanonicalRarity rarity, SpecimenData specimen) {
        FishScoreV2Service.Result score = new FishScoreV2Service().calculate(rarity, specimen);
        return new SpecimenData(
                specimen.speciesId(),
                specimen.schemaVersion(),
                specimen.generationVersion(),
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                specimen.baseLength(),
                specimen.finalLength(),
                specimen.finalPercentile(),
                specimen.bodyType(),
                specimen.condition(),
                specimen.pigmentation(),
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                OptionalDouble.of(score.rawScore()),
                OptionalInt.of(score.fishScore()),
                specimen.provenance()
        );
    }

    private static SpecimenData.BodyType mapBodyType(String legacyBodyType, String mutation) {
        String value = legacyBodyType.isEmpty() ? mutation : legacyBodyType;
        return switch (value) {
            case "giant" -> SpecimenData.BodyType.GIANT;
            case "dwarf" -> SpecimenData.BodyType.DWARF;
            default -> SpecimenData.BodyType.NORMAL;
        };
    }

    private static SpecimenData.Condition mapCondition(String mutation) {
        return switch (mutation) {
            case "scarred" -> SpecimenData.Condition.SCARRED;
            case "parasite", "parasite_ridden" -> SpecimenData.Condition.PARASITE_RIDDEN;
            default -> SpecimenData.Condition.NORMAL;
        };
    }

    private static SpecimenData.Pigmentation mapPigmentation(String mutation) {
        return switch (mutation) {
            case "albino" -> SpecimenData.Pigmentation.ALBINO;
            case "iridescent" -> SpecimenData.Pigmentation.IRIDESCENT;
            default -> SpecimenData.Pigmentation.NORMAL;
        };
    }

    private static SpecimenData.SpecimenQuality mapQuality(String mutation) {
        return switch (mutation) {
            case "perfect", "perfect_specimen" -> SpecimenData.SpecimenQuality.PERFECT_SPECIMEN;
            default -> SpecimenData.SpecimenQuality.NORMAL;
        };
    }

    private static long deriveSeed(LegacyFish legacy) {
        long hash = FNV_OFFSET_BASIS;
        hash = hashString(hash, legacy.speciesId());
        hash = hashString(hash, token(legacy.mutation()));
        hash = hashString(hash, token(legacy.bodyType()));
        hash = hashLong(hash, legacy.percentile() == null ? 0x5A17C9E3D4B26801L : Double.doubleToLongBits(legacy.percentile()));
        hash = hashLong(hash, legacy.physicalLength() == null ? 0xA4F3906B1E27D85CL : Double.doubleToLongBits(legacy.physicalLength()));
        return TraitRandom.mixedLong(hash, DERIVED_SEED_SALT);
    }

    private static long hashString(long hash, String value) {
        for (byte valueByte : value.getBytes(StandardCharsets.UTF_8)) {
            hash ^= valueByte & 0xFFL;
            hash *= FNV_PRIME;
        }
        hash ^= 0xFFL;
        return hash * FNV_PRIME;
    }

    private static long hashLong(long hash, long value) {
        long result = hash;
        for (int shift = 0; shift < Long.SIZE; shift += Byte.SIZE) {
            result ^= (value >>> shift) & 0xFFL;
            result *= FNV_PRIME;
        }
        return result;
    }

    private static double finiteQuantile(SizeDistribution distribution, double percentile) {
        double probability = Math.min(percentile / 100.0, Math.nextDown(1.0));
        double length = distribution.quantile(Math.max(0.0, probability));
        if (!Double.isFinite(length) || length < 0.0) {
            throw new IllegalArgumentException("species size distribution produced an invalid migration length");
        }
        return length;
    }

    private static boolean isUsableLength(Double length) {
        return length != null && Double.isFinite(length) && length > 0.0;
    }

    private static boolean isUsablePercentile(Double percentile) {
        return percentile != null && Double.isFinite(percentile) && percentile >= 0.0 && percentile <= 100.0;
    }

    private static double clampPercentile(double percentile) {
        if (!Double.isFinite(percentile)) {
            throw new IllegalArgumentException("species size distribution produced a non-finite percentile");
        }
        return Math.max(0.0, Math.min(100.0, percentile));
    }

    private static String token(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    public record LegacyFish(
            String speciesId,
            Integer schemaVersion,
            Long deterministicSeed,
            Double percentile,
            Double physicalLength,
            String mutation,
            String bodyType,
            SpecimenData canonicalSpecimen
    ) {
        public LegacyFish {
            if (canonicalSpecimen == null && (speciesId == null || speciesId.isBlank() || !speciesId.contains(":"))) {
                throw new IllegalArgumentException("legacy speciesId must be a namespaced ID");
            }
        }

        public static LegacyFish canonical(SpecimenData specimen) {
            Objects.requireNonNull(specimen, "specimen");
            return new LegacyFish(
                    specimen.speciesId(),
                    specimen.schemaVersion(),
                    specimen.deterministicSeed(),
                    specimen.basePercentile(),
                    specimen.finalLength(),
                    null,
                    null,
                    specimen
            );
        }
    }

    public record MigrationResult(SpecimenData specimen, boolean migrated) {
        public MigrationResult {
            Objects.requireNonNull(specimen, "specimen");
        }
    }
}
