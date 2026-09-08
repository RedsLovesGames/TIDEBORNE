package com.redslovesgames.tideborne.fishing.gear;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Immutable, composable server-side fishing modifier model for Fishing System 2.0 gear.
 *
 * <p>Numeric stacking semantics are explicit: Fishing Luck, Trait Luck, and named additive modifiers
 * add; Strength, Tempo, Body Type chance, and named multiplier modifiers multiply. Category and
 * catch-pool restrictions compose by intersecting active allow-lists and unioning deny-lists.
 * Composition uses exact decimal accumulation before converting back to {@code double}, and all
 * identifier collections are kept in canonical sorted order, so the result does not depend on the
 * input collection's iteration order.
 *
 * <p>This type is a domain representation only. Runtime gear behavior is intentionally not wired
 * here, and the model has no client or UI dependency.
 */
public record FishingGearModifiers(
        double fishingLuck,
        double traitLuck,
        double strengthMultiplier,
        double tempoMultiplier,
        IdRestriction categoryRestriction,
        IdRestriction catchPoolRestriction,
        Map<SpecimenData.BodyType, Double> bodyTypeChanceMultipliers,
        Map<String, Double> namedAdditiveModifiers,
        Map<String, Double> namedMultiplierModifiers
) {
    public FishingGearModifiers {
        requireFinite("fishingLuck", fishingLuck);
        requireFinite("traitLuck", traitLuck);
        requireNonNegativeFinite("strengthMultiplier", strengthMultiplier);
        requireNonNegativeFinite("tempoMultiplier", tempoMultiplier);
        categoryRestriction = categoryRestriction == null ? IdRestriction.unrestricted() : categoryRestriction;
        catchPoolRestriction = catchPoolRestriction == null ? IdRestriction.unrestricted() : catchPoolRestriction;
        bodyTypeChanceMultipliers = immutableBodyTypeMultipliers(bodyTypeChanceMultipliers);
        namedAdditiveModifiers = immutableNamedModifiers("namedAdditiveModifiers", namedAdditiveModifiers, false);
        namedMultiplierModifiers = immutableNamedModifiers("namedMultiplierModifiers", namedMultiplierModifiers, true);
    }

    private static final FishingGearModifiers NEUTRAL = new FishingGearModifiers(
            0.0, 0.0, 1.0, 1.0, IdRestriction.unrestricted(), IdRestriction.unrestricted(),
            Map.of(), Map.of(), Map.of());

    public static FishingGearModifiers neutral() {
        return NEUTRAL;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static FishingGearModifiers compose(FishingGearModifiers... modifiers) {
        Objects.requireNonNull(modifiers, "modifiers");
        return compose(Arrays.asList(modifiers));
    }

    /**
     * Composes raw contributions with input-order-independent results. Consume the complete result
     * through FishingGearEffects for global limits; clamping partial groups would lose tradeoffs.
     */
    public static FishingGearModifiers compose(Collection<FishingGearModifiers> modifiers) {
        Objects.requireNonNull(modifiers, "modifiers");

        BigDecimal fishingLuck = BigDecimal.ZERO;
        BigDecimal traitLuck = BigDecimal.ZERO;
        BigDecimal strengthMultiplier = BigDecimal.ONE;
        BigDecimal tempoMultiplier = BigDecimal.ONE;
        IdRestriction categoryRestriction = IdRestriction.unrestricted();
        IdRestriction catchPoolRestriction = IdRestriction.unrestricted();
        EnumMap<SpecimenData.BodyType, BigDecimal> bodyTypeMultipliers =
                new EnumMap<>(SpecimenData.BodyType.class);
        TreeMap<String, BigDecimal> namedAdditive = new TreeMap<>();
        TreeMap<String, BigDecimal> namedMultipliers = new TreeMap<>();

        for (FishingGearModifiers modifier : modifiers) {
            Objects.requireNonNull(modifier, "modifiers contains null");
            fishingLuck = fishingLuck.add(decimal(modifier.fishingLuck));
            traitLuck = traitLuck.add(decimal(modifier.traitLuck));
            strengthMultiplier = strengthMultiplier.multiply(decimal(modifier.strengthMultiplier));
            tempoMultiplier = tempoMultiplier.multiply(decimal(modifier.tempoMultiplier));
            categoryRestriction = categoryRestriction.compose(modifier.categoryRestriction);
            catchPoolRestriction = catchPoolRestriction.compose(modifier.catchPoolRestriction);

            modifier.bodyTypeChanceMultipliers.forEach((bodyType, value) ->
                    bodyTypeMultipliers.merge(bodyType, decimal(value), BigDecimal::multiply));
            modifier.namedAdditiveModifiers.forEach((key, value) ->
                    namedAdditive.merge(key, decimal(value), BigDecimal::add));
            modifier.namedMultiplierModifiers.forEach((key, value) ->
                    namedMultipliers.merge(key, decimal(value), BigDecimal::multiply));
        }

        EnumMap<SpecimenData.BodyType, Double> bodyTypeResult = new EnumMap<>(SpecimenData.BodyType.class);
        bodyTypeMultipliers.forEach((bodyType, value) -> bodyTypeResult.put(bodyType, value.doubleValue()));

        return new FishingGearModifiers(
                fishingLuck.doubleValue(),
                traitLuck.doubleValue(),
                strengthMultiplier.doubleValue(),
                tempoMultiplier.doubleValue(),
                categoryRestriction,
                catchPoolRestriction,
                bodyTypeResult,
                decimalMapToDouble(namedAdditive),
                decimalMapToDouble(namedMultipliers)
        );
    }

    public double bodyTypeChanceMultiplier(SpecimenData.BodyType bodyType) {
        Objects.requireNonNull(bodyType, "bodyType");
        return bodyTypeChanceMultipliers.getOrDefault(bodyType, 1.0);
    }

    public double namedAdditiveModifier(String key) {
        return namedAdditiveModifiers.getOrDefault(requireKey(key), 0.0);
    }

    public double namedMultiplierModifier(String key) {
        return namedMultiplierModifiers.getOrDefault(requireKey(key), 1.0);
    }

    private static Map<SpecimenData.BodyType, Double> immutableBodyTypeMultipliers(
            Map<SpecimenData.BodyType, Double> modifiers
    ) {
        if (modifiers == null || modifiers.isEmpty()) {
            return Map.of();
        }
        EnumMap<SpecimenData.BodyType, Double> validated = new EnumMap<>(SpecimenData.BodyType.class);
        modifiers.forEach((bodyType, value) -> {
            if (bodyType == null) {
                throw new IllegalArgumentException("bodyTypeChanceMultipliers contains a null Body Type");
            }
            requireNonNegativeFinite("bodyTypeChanceMultipliers[" + bodyType + "]", value);
            validated.put(bodyType, value);
        });
        return Collections.unmodifiableMap(validated);
    }

    private static Map<String, Double> immutableNamedModifiers(
            String name,
            Map<String, Double> modifiers,
            boolean requireNonNegative
    ) {
        if (modifiers == null || modifiers.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> validated = new TreeMap<>();
        modifiers.forEach((key, value) -> {
            String validatedKey = requireKey(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " contains a null value for " + validatedKey);
            }
            if (requireNonNegative) {
                requireNonNegativeFinite(name + "[" + validatedKey + "]", value);
            } else {
                requireFinite(name + "[" + validatedKey + "]", value);
            }
            validated.put(validatedKey, value);
        });
        return Collections.unmodifiableMap(validated);
    }

    private static Map<String, Double> decimalMapToDouble(Map<String, BigDecimal> values) {
        if (values.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> result = new TreeMap<>();
        values.forEach((key, value) -> result.put(key, value.doubleValue()));
        return result;
    }

    private static BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value);
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("modifier key must not be blank");
        }
        return key;
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    private static void requireNonNegativeFinite(String name, Double value) {
        if (value == null || !Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and nonnegative");
        }
    }

    /**
     * Canonical identifier restriction. An inactive allow-list is unrestricted; an active empty
     * allow-list intentionally allows nothing. Denied IDs always win.
     */
    public record IdRestriction(boolean allowListActive, Set<String> allowedIds, Set<String> deniedIds) {
        private static final IdRestriction UNRESTRICTED = new IdRestriction(false, Set.of(), Set.of());

        public IdRestriction {
            TreeSet<String> allowed = immutableIds("allowedIds", allowedIds);
            TreeSet<String> denied = immutableIds("deniedIds", deniedIds);
            if (allowListActive) {
                allowed.removeAll(denied);
            } else if (!allowed.isEmpty()) {
                throw new IllegalArgumentException("allowedIds require an active allow-list");
            }
            allowedIds = Collections.unmodifiableSet(allowed);
            deniedIds = Collections.unmodifiableSet(denied);
        }

        public static IdRestriction unrestricted() {
            return UNRESTRICTED;
        }

        public static IdRestriction only(Collection<String> allowedIds) {
            Objects.requireNonNull(allowedIds, "allowedIds");
            return new IdRestriction(true, new TreeSet<>(allowedIds), Set.of());
        }

        public static IdRestriction excluding(Collection<String> deniedIds) {
            Objects.requireNonNull(deniedIds, "deniedIds");
            return new IdRestriction(false, Set.of(), new TreeSet<>(deniedIds));
        }

        public boolean allows(String id) {
            String validatedId = requireId(id);
            return !deniedIds.contains(validatedId)
                    && (!allowListActive || allowedIds.contains(validatedId));
        }

        public IdRestriction compose(IdRestriction other) {
            Objects.requireNonNull(other, "other");
            boolean resultActive = allowListActive || other.allowListActive;
            TreeSet<String> resultAllowed = new TreeSet<>();
            if (allowListActive && other.allowListActive) {
                resultAllowed.addAll(allowedIds);
                resultAllowed.retainAll(other.allowedIds);
            } else if (allowListActive) {
                resultAllowed.addAll(allowedIds);
            } else if (other.allowListActive) {
                resultAllowed.addAll(other.allowedIds);
            }

            TreeSet<String> resultDenied = new TreeSet<>(deniedIds);
            resultDenied.addAll(other.deniedIds);
            if (resultActive) {
                resultAllowed.removeAll(resultDenied);
            }
            return new IdRestriction(resultActive, resultAllowed, resultDenied);
        }

        private static TreeSet<String> immutableIds(String name, Collection<String> ids) {
            TreeSet<String> result = new TreeSet<>();
            if (ids == null) {
                return result;
            }
            for (String id : ids) {
                try {
                    result.add(requireId(id));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(name + " contains an invalid ID", exception);
                }
            }
            return result;
        }

        private static String requireId(String id) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("restriction ID must not be blank");
            }
            return id;
        }
    }

    public static final class Builder {
        private double fishingLuck;
        private double traitLuck;
        private double strengthMultiplier = 1.0;
        private double tempoMultiplier = 1.0;
        private IdRestriction categoryRestriction = IdRestriction.unrestricted();
        private IdRestriction catchPoolRestriction = IdRestriction.unrestricted();
        private final EnumMap<SpecimenData.BodyType, Double> bodyTypeChanceMultipliers =
                new EnumMap<>(SpecimenData.BodyType.class);
        private final TreeMap<String, Double> namedAdditiveModifiers = new TreeMap<>();
        private final TreeMap<String, Double> namedMultiplierModifiers = new TreeMap<>();

        private Builder() {
        }

        public Builder trophyFightRelief(double fraction) {
            return namedAdditiveModifier(FishingGearEffects.TROPHY_FIGHT_RELIEF, fraction);
        }

        public Builder targetWeight(String speciesTag, double multiplier) {
            return namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX + requireKey(speciesTag), multiplier);
        }

        public Builder environmentWeight(String habitatTag, double multiplier) {
            return namedMultiplierModifier(FishingGearEffects.ENVIRONMENT_WEIGHT_PREFIX + requireKey(habitatTag), multiplier);
        }

        public Builder fishingLuck(double value) {
            this.fishingLuck = value;
            return this;
        }

        public Builder traitLuck(double value) {
            this.traitLuck = value;
            return this;
        }

        public Builder strengthMultiplier(double value) {
            this.strengthMultiplier = value;
            return this;
        }

        public Builder tempoMultiplier(double value) {
            this.tempoMultiplier = value;
            return this;
        }

        public Builder restrictCategoriesTo(String... ids) {
            this.categoryRestriction = this.categoryRestriction.compose(IdRestriction.only(Arrays.asList(ids)));
            return this;
        }

        public Builder excludeCategories(String... ids) {
            this.categoryRestriction = this.categoryRestriction.compose(IdRestriction.excluding(Arrays.asList(ids)));
            return this;
        }

        public Builder restrictCatchPoolsTo(String... ids) {
            this.catchPoolRestriction = this.catchPoolRestriction.compose(IdRestriction.only(Arrays.asList(ids)));
            return this;
        }

        public Builder excludeCatchPools(String... ids) {
            this.catchPoolRestriction = this.catchPoolRestriction.compose(IdRestriction.excluding(Arrays.asList(ids)));
            return this;
        }

        public Builder bodyTypeChanceMultiplier(SpecimenData.BodyType bodyType, double value) {
            Objects.requireNonNull(bodyType, "bodyType");
            requireNonNegativeFinite("bodyTypeChanceMultiplier", value);
            bodyTypeChanceMultipliers.put(bodyType, value);
            return this;
        }

        public Builder namedAdditiveModifier(String key, double value) {
            requireFinite("namedAdditiveModifier", value);
            namedAdditiveModifiers.put(requireKey(key), value);
            return this;
        }

        public Builder namedMultiplierModifier(String key, double value) {
            requireNonNegativeFinite("namedMultiplierModifier", value);
            namedMultiplierModifiers.put(requireKey(key), value);
            return this;
        }

        public FishingGearModifiers build() {
            return new FishingGearModifiers(
                    fishingLuck,
                    traitLuck,
                    strengthMultiplier,
                    tempoMultiplier,
                    categoryRestriction,
                    catchPoolRestriction,
                    bodyTypeChanceMultipliers,
                    namedAdditiveModifiers,
                    namedMultiplierModifiers
            );
        }
    }
}
