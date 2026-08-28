package com.redslovesgames.tideborne.fishing.v2;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Minimal data-only environment passed to species eligibility rules. */
public record FishingEnvironment(Set<String> habitatTags, Map<String, String> attributes) {
    public FishingEnvironment {
        habitatTags = habitatTags == null || habitatTags.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(new TreeSet<>(habitatTags));
        attributes = attributes == null || attributes.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new TreeMap<>(attributes));
        if (habitatTags.stream().anyMatch(tag -> tag == null || tag.isBlank())) {
            throw new IllegalArgumentException("habitat tags must not be blank");
        }
        if (attributes.entrySet().stream().anyMatch(entry -> entry.getKey() == null
                || entry.getKey().isBlank() || entry.getValue() == null)) {
            throw new IllegalArgumentException("environment attributes must have nonblank keys and nonnull values");
        }
    }

    public static FishingEnvironment empty() {
        return new FishingEnvironment(Set.of(), Map.of());
    }
}

