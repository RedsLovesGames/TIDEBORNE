package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.Tide;
import com.li64.tide.config.TideServerConfig;
import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.CatchResult;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.*;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.TideborneFishingGearModifiers;
import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** Server-authoritative bridge that owns V2 species selection and pre-fight specimen generation. */
public final class TideSpeciesSelectionBridge {
    public static final TideSpeciesSelectionBridge INSTANCE=new TideSpeciesSelectionBridge();
    private final TideFishingContextAdapter contextAdapter=new TideFishingContextAdapter();private final TideSpeciesProfileAdapter profileAdapter=new TideSpeciesProfileAdapter();private final SpeciesSelectionService selector=new SpeciesSelectionService();private final SpecimenGenerator specimenGenerator=new SpecimenGenerator();private final FightProfileService fightProfiles=new FightProfileService();private TideSpeciesSelectionBridge(){}
    public CatchResult select(com.li64.tide.data.fishing.FishingContext tideContext){
        TideFishingHook hook=tideContext.hook();if(hook==null||hook.getWorld().isClient()||!(hook.getPlayerOwner() instanceof ServerPlayerEntity serverPlayer)){clearHookState(hook);return CatchResult.empty();}
        FishingGearModifiers specimenGear=TideborneFishingGearModifiers.forSelection(tideContext,TideboundConfig.get());
        FishingContext nativeContext=contextAdapter.context(tideContext);FishingContext context=nativeContext.withGearModifiers(specimenGear);FishingEnvironment environment=contextAdapter.environment(tideContext);List<SpeciesProfile> profiles=new ArrayList<>();Map<String,FishData> dataBySpecies=new HashMap<>();
        for(FishData data:TideData.FISH.get().values())profileAdapter.adapt(data,tideContext).ifPresent(c->{profiles.add(c.profile());dataBySpecies.putIfAbsent(c.profile().speciesId(),c.fishData());});
        if(profiles.isEmpty()){clearHookState(hook);return CatchResult.empty();}
        List<SpeciesSelectionService.WeightedSpecies> eligible=selector.eligibleSpecies(profiles,nativeContext,environment,specimenGear);long catchSeed=tideContext.rng().nextLong();SpeciesProfile selected=selector.select(profiles,nativeContext,environment,specimenGear,new SplittableRandom(CatchSeedDeriver.selectionSeed(catchSeed)));FishData data=dataBySpecies.get(selected.speciesId());if(data==null){clearHookState(hook);return CatchResult.empty();}
        int momentum=TraitMomentumStorage.get(serverPlayer,selected.speciesId());double effectiveTL=TraitMomentumProgression.effectiveTraitLuck(context.traitLuck(),momentum);SpecimenData specimen=specimenGenerator.generatePreFight(selected,CatchSeedDeriver.specimenSeed(catchSeed),new SpecimenData.Provenance("new-catch","fishing-system-2-runtime",Map.of("selection","tide-fish-selector","authority","server")),effectiveTL);FightProfile fight=fightProfiles.create(selected,specimen,TideborneFishingGearModifiers.forCanonicalFight(hook,TideboundConfig.get()));
        ItemStack stack=new ItemStack((Item)data.fish().value());if(Tide.SERVER_CONFIG.items.bucketableFishItems==TideServerConfig.Items.BucketableMode.WHEN_LIVING&&data.bucket().isPresent())TideItemData.IS_BUCKETABLE.set(stack,true);CanonicalSpecimenStorage.write(stack,specimen);CatchResult result=data.createResult(stack);CanonicalCatchStateManager.put(hook,new CanonicalCatchStateManager.CatchState(catchSeed,context,environment,selected,specimen,fight,momentum,eligible));return result;
    }
    private static void clearHookState(TideFishingHook hook){CanonicalCatchStateManager.clear(hook);}
}
