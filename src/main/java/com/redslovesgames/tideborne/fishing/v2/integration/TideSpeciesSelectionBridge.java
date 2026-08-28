package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpeciesSelectionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

/** Server-authoritative bridge that replaces only Tide's within-fish species choice. */
public final class TideSpeciesSelectionBridge {
    public static final TideSpeciesSelectionBridge INSTANCE = new TideSpeciesSelectionBridge();

    private final TideFishingContextAdapter contextAdapter = new TideFishingContextAdapter();
    private final TideSpeciesProfileAdapter profileAdapter = new TideSpeciesProfileAdapter();
    private final SpeciesSelectionService selector = new SpeciesSelectionService();

    private TideSpeciesSelectionBridge() {
    }

    public CatchResult select(com.li64.tide.data.fishing.FishingContext tideContext) {
        FishingContext context = contextAdapter.context(tideContext);
        FishingEnvironment environment = contextAdapter.environment(tideContext);
        List<SpeciesProfile> profiles = new ArrayList<>();
        Map<String, FishData> dataBySpecies = new HashMap<>();

        for (FishData data : TideData.FISH.get().values()) {
            profileAdapter.adapt(data, tideContext).ifPresent(candidate -> {
                profiles.add(candidate.profile());
                dataBySpecies.putIfAbsent(candidate.profile().speciesId(), candidate.fishData());
            });
        }

        if (profiles.isEmpty()) {
            return CatchResult.empty(null);
        }

        SpeciesProfile selected = selector.select(
                profiles,
                context,
                environment,
                new SplittableRandom(tideContext.rng().nextLong())
        );
        FishData data = dataBySpecies.get(selected.speciesId());
        return data == null ? CatchResult.empty(null) : data.getResult(tideContext);
    }
}
