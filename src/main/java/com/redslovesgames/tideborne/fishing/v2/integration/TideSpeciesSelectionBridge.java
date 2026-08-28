package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.Tide;
import com.li64.tide.config.TideServerConfig;
import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FightProfile;
import com.redslovesgames.tideborne.fishing.v2.FightProfileService;
import com.redslovesgames.tideborne.fishing.v2.FishingContext;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpeciesSelectionService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Server-authoritative bridge that owns V2 species selection and natural specimen generation. */
public final class TideSpeciesSelectionBridge {
    public static final TideSpeciesSelectionBridge INSTANCE = new TideSpeciesSelectionBridge();

    private final TideFishingContextAdapter contextAdapter = new TideFishingContextAdapter();
    private final TideSpeciesProfileAdapter profileAdapter = new TideSpeciesProfileAdapter();
    private final SpeciesSelectionService selector = new SpeciesSelectionService();
    private final SpecimenGenerator specimenGenerator = new SpecimenGenerator();
    private final FightProfileService fightProfiles = new FightProfileService();

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
            clearHookState(tideContext.hook());
            return CatchResult.empty();
        }

        long catchSeed = tideContext.rng().nextLong();
        SpeciesProfile selected = selector.select(
                profiles,
                context,
                environment,
                new SplittableRandom(CatchSeedDeriver.selectionSeed(catchSeed))
        );
        FishData data = dataBySpecies.get(selected.speciesId());
        if (data == null) {
            clearHookState(tideContext.hook());
            return CatchResult.empty();
        }

        SpecimenData specimen = specimenGenerator.generateBase(
                selected,
                CatchSeedDeriver.specimenSeed(catchSeed),
                new SpecimenData.Provenance(
                        "new-catch",
                        "fishing-system-2-runtime",
                        Map.of("selection", "tide-fish-selector", "authority", "server")
                )
        );
        FightProfile fightProfile = fightProfiles.create(selected, specimen);

        // Do not call FishData#getResult here. Tide's implementation performs its own
        // independent fish-length roll, which would violate the one-canonical-size rule.
        ItemStack stack = new ItemStack((Item) data.fish().value());
        if (Tide.SERVER_CONFIG.items.bucketableFishItems == TideServerConfig.Items.BucketableMode.WHEN_LIVING
                && data.bucket().isPresent()) {
            TideItemData.IS_BUCKETABLE.set(stack, true);
        }
        CanonicalSpecimenStorage.write(stack, specimen);
        CatchResult result = data.createResult(stack);

        TideFishingHook hook = tideContext.hook();
        if (hook != null) {
            CanonicalCatchStateManager.put(
                    hook,
                    new CanonicalCatchStateManager.CatchState(
                            catchSeed,
                            context,
                            environment,
                            selected,
                            specimen,
                            fightProfile
                    )
            );
        }
        return result;
    }

    private static void clearHookState(TideFishingHook hook) {
        CanonicalCatchStateManager.clear(hook);
    }
}
