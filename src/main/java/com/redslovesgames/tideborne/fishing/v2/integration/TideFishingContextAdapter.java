package com.redslovesgames.tideborne.fishing.v2.integration;

import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import java.util.Map;
import java.util.Set;

/** Converts Tide's live server fishing context into the immutable Fishing System 2.0 boundary. */
public final class TideFishingContextAdapter {
    public FishingContext context(com.li64.tide.data.fishing.FishingContext tide) {
        return new FishingContext(
                0.0,
                tide.luck(),
                0.0,
                Map.of(),
                Map.of(),
                Map.of(
                        "temperature", (double) tide.temperature(),
                        "moon_phase", (double) tide.moonPhase()
                )
        );
    }

    public FishingEnvironment environment(com.li64.tide.data.fishing.FishingContext tide) {
        return new FishingEnvironment(
                Set.of(tide.medium()),
                Map.of(
                        "dimension", tide.dimension().getValue().toString(),
                        "biome", tide.exactBiome().getKey().map(key -> key.getValue().toString()).orElse("unknown"),
                        "nearest_biome", tide.nearestBiome().getKey().map(key -> key.getValue().toString()).orElse("unknown"),
                        "medium", tide.medium(),
                        "moon_phase", Integer.toString(tide.moonPhase()),
                        "temperature", Float.toString(tide.temperature()),
                        "season", String.valueOf(tide.season())
                )
        );
    }
}
