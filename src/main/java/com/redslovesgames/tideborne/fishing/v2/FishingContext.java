package com.redslovesgames.tideborne.fishing.v2;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Immutable server-owned inputs for one catch attempt. */
public record FishingContext(
        double biteSpeed,
        double fishingLuck,
        double traitLuck,
        Map<String, Double> equipmentModifiers,
        Map<String, Double> baitModifiers,
        Map<String, Double> environmentModifiers
) {
    public FishingContext {
        requireFinite("biteSpeed", biteSpeed);
        requireFinite("fishingLuck", fishingLuck);
        requireFinite("traitLuck", traitLuck);
        equipmentModifiers = immutableModifiers("equipmentModifiers", equipmentModifiers);
        baitModifiers = immutableModifiers("baitModifiers", baitModifiers);
        environmentModifiers = immutableModifiers("environmentModifiers", environmentModifiers);
    }

    public static FishingContext neutral() {
        return new FishingContext(0.0, 0.0, 0.0, Map.of(), Map.of(), Map.of());
    }

    /** Returns the additive total for one named modifier across all sources. */
    public double additiveModifier(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("modifier key must not be blank");
        }
        return equipmentModifiers.getOrDefault(key, 0.0)
                + baitModifiers.getOrDefault(key, 0.0)
                + environmentModifiers.getOrDefault(key, 0.0);
    }

    private static Map<String, Double> immutableModifiers(String name, Map<String, Double> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> validated = new TreeMap<>();
        modifiers.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(name + " contains a blank key");
            }
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException(name + " contains a non-finite value for " + key);
            }
            validated.put(key, value);
        });
        return Collections.unmodifiableMap(validated);
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}

