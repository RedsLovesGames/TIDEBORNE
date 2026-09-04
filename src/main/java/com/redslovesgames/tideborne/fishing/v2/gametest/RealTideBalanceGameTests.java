package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.TideItems;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.FishingEnvironment;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.integration.TideFishingContextAdapter;
import com.redslovesgames.tideborne.fishing.v2.integration.TideSpeciesProfileAdapter;
import com.redslovesgames.tideborne.fishing.v2.simulation.RealTideFishingSimulator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

/** Runtime-backed balance smoke/report projection using Tide's live 1.21.1 fish catalog. */
public final class RealTideBalanceGameTests implements FabricGameTest {
    private static final int DISTRIBUTION_CATCHES = 25_000;
    private static final int SENSITIVITY_CATCHES = 15_000;
    private static final int PROGRESSION_TRIALS = 40;
    private static final int PROGRESSION_MAX_CATCHES = 2_000;
    private static final long DISTRIBUTION_SEED = 0x5449444542414C41L;
    private static final long SENSITIVITY_SEED = 0x4C55434B56414C55L;
    private static final long PROGRESSION_SEED = 0x5245414C42414C32L;

    private final TideSpeciesProfileAdapter profiles = new TideSpeciesProfileAdapter();
    private final TideFishingContextAdapter contexts = new TideFishingContextAdapter();
    private final RealTideFishingSimulator simulator = new RealTideFishingSimulator();

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void realTideCatalogBalanceProjection(TestContext helper) {
        ServerWorld overworld = helper.getWorld();
        ServerPlayerEntity player = helper.createMockCreativeServerPlayerInWorld();
        ItemStack neutralRod = new ItemStack(TideItems.STONE_FISHING_ROD);
        TideFishingHook hook = new TideFishingHook(
                TideEntityTypes.FISHING_BOBBER,
                player,
                overworld,
                0,
                0,
                0.0F,
                neutralRod
        );
        hook.setPosition(0.5, 62.5, 0.5);
        com.li64.tide.data.fishing.FishingContext base = hook.getContext();

        Registry<Biome> biomeRegistry = overworld.getRegistryManager().get(RegistryKeys.BIOME);
        List<RealTideFishingSimulator.LocationPool> locations = List.of(
                pool("river_day_clear", "River, surface, clear day", base, biomeRegistry, BiomeKeys.RIVER, BiomeKeys.PLAINS, World.OVERWORLD, "water", 62, 0.55F, 0, false, false),
                pool("swamp_night_rain", "Swamp, surface, rainy night", base, biomeRegistry, BiomeKeys.SWAMP, BiomeKeys.SWAMP, World.OVERWORLD, "water", 62, 0.80F, 4, true, true),
                pool("warm_ocean_day", "Warm ocean, surface, clear day", base, biomeRegistry, BiomeKeys.WARM_OCEAN, BiomeKeys.BEACH, World.OVERWORLD, "water", 62, 0.95F, 0, false, false),
                pool("deep_ocean_night", "Deep ocean, deep water, clear night", base, biomeRegistry, BiomeKeys.DEEP_OCEAN, BiomeKeys.BEACH, World.OVERWORLD, "water", 30, 0.50F, 4, true, false),
                pool("frozen_ocean_day", "Frozen ocean, surface, clear day", base, biomeRegistry, BiomeKeys.FROZEN_OCEAN, BiomeKeys.SNOWY_PLAINS, World.OVERWORLD, "water", 62, -0.50F, 0, false, false),
                pool("lush_cave", "Lush cave, underground water", base, biomeRegistry, BiomeKeys.LUSH_CAVES, BiomeKeys.PLAINS, World.OVERWORLD, "water", -20, 0.50F, 0, false, false),
                pool("dripstone_cave", "Dripstone cave, underground water", base, biomeRegistry, BiomeKeys.DRIPSTONE_CAVES, BiomeKeys.PLAINS, World.OVERWORLD, "water", -20, 0.50F, 4, true, false),
                pool("nether_lava", "Nether Wastes, lava fishing", base, biomeRegistry, BiomeKeys.NETHER_WASTES, BiomeKeys.NETHER_WASTES, World.NETHER, "lava", 32, 1.00F, 4, true, false),
                pool("end_void", "The End, void fishing", base, biomeRegistry, BiomeKeys.THE_END, BiomeKeys.THE_END, World.END, "void", 60, 0.50F, 0, false, false)
        );

        int catalogSize = 0;
        for (FishData ignored : TideData.FISH.get().values()) {
            catalogSize++;
        }
        int reachable = locations.stream().flatMap(location -> location.species().stream()).map(SpeciesProfile::speciesId).collect(java.util.stream.Collectors.toSet()).size();
        helper.assertTrue(catalogSize >= 100, "Real Tide fish catalog unexpectedly small: " + catalogSize);
        helper.assertTrue(reachable > 0, "Representative real Tide context matrix produced no eligible species");

        System.out.println("[REAL_BALANCE] catalog_size=" + catalogSize + " representative_reachable_species=" + reachable + " locations=" + locations.size());
        for (RealTideFishingSimulator.LocationPool location : locations) {
            System.out.println("[REAL_BALANCE] location=" + location.id() + " eligible_species=" + location.species().size() + " description=\"" + location.description() + "\"");
        }

        for (RealTideFishingSimulator.GearStage gear : RealTideFishingSimulator.GearStage.values()) {
            RealTideFishingSimulator.Result result = simulator.simulate(
                    locations,
                    gear,
                    DISTRIBUTION_SEED ^ gear.ordinal(),
                    DISTRIBUTION_CATCHES
            );
            RealTideFishingSimulator.ProgressionResult progression = simulator.simulateProgression(
                    locations,
                    gear,
                    PROGRESSION_SEED ^ gear.ordinal(),
                    PROGRESSION_TRIALS,
                    PROGRESSION_MAX_CATCHES
            );

            printDistribution("gear", result);
            System.out.printf(Locale.ROOT,
                    "[REAL_BALANCE] progression gear=%s reachable=%d c25=%d c50=%d c75=%d c90=%d first4=%d first5=%d first_perfect=%d first_score2000=%d%n",
                    gear.id(), progression.reachableSpecies(), progression.medianCatchesTo25Percent(), progression.medianCatchesTo50Percent(), progression.medianCatchesTo75Percent(), progression.medianCatchesTo90Percent(), progression.medianCatchesToFirstFourStar(), progression.medianCatchesToFirstFiveStar(), progression.medianCatchesToFirstPerfectSpecimen(), progression.medianCatchesToFirstScore2000());
        }

        List<RealTideFishingSimulator.GearProfile> sensitivity = List.of(
                new RealTideFishingSimulator.GearProfile("luck0_trait0", 0.0, 0.0, 0.15),
                new RealTideFishingSimulator.GearProfile("luck3_trait0", 3.0, 0.0, 0.15),
                new RealTideFishingSimulator.GearProfile("luck18_trait0", 18.0, 0.0, 0.15),
                new RealTideFishingSimulator.GearProfile("luck0_trait1", 0.0, 1.0, 0.15),
                new RealTideFishingSimulator.GearProfile("luck0_trait2", 0.0, 2.0, 0.15),
                new RealTideFishingSimulator.GearProfile("luck0_trait6", 0.0, 6.0, 0.15)
        );
        for (RealTideFishingSimulator.GearProfile profile : sensitivity) {
            RealTideFishingSimulator.Result result = simulator.simulate(locations, profile, SENSITIVITY_SEED, SENSITIVITY_CATCHES);
            printDistribution("sensitivity", result);
        }

        hook.discard();
        helper.complete();
    }

    private static void printDistribution(String kind, RealTideFishingSimulator.Result result) {
        Map<CanonicalRarity, Double> shares = RealTideFishingSimulator.rarityShares(result);
        System.out.printf(Locale.ROOT,
                "[REAL_BALANCE] %s=%s fishing_luck=%.2f trait_luck=%.2f catches=%d mean_score=%.2f p50=%d p90=%d p99=%d min=%d max=%d any_trait=%.6f perfect_specimen=%.6f perfect_catch=%.6f one_star=%.6f two_star=%.6f three_star=%.6f four_star=%.6f five_star=%.6f%n",
                kind, result.gear().id(), result.gear().fishingLuck(), result.gear().traitLuck(), result.catches(), result.meanFishScore(), result.p50FishScore(), result.p90FishScore(), result.p99FishScore(), result.minFishScore(), result.maxFishScore(), result.anyTraitRate(), result.perfectSpecimenRate(), result.perfectCatchRate(),
                shares.getOrDefault(CanonicalRarity.ONE_STAR, 0.0), shares.getOrDefault(CanonicalRarity.TWO_STAR, 0.0), shares.getOrDefault(CanonicalRarity.THREE_STAR, 0.0), shares.getOrDefault(CanonicalRarity.FOUR_STAR, 0.0), shares.getOrDefault(CanonicalRarity.FIVE_STAR, 0.0));
    }

    private RealTideFishingSimulator.LocationPool pool(
            String id,
            String description,
            com.li64.tide.data.fishing.FishingContext base,
            Registry<Biome> biomeRegistry,
            RegistryKey<Biome> exactBiomeKey,
            RegistryKey<Biome> nearestBiomeKey,
            RegistryKey<World> dimension,
            String medium,
            int y,
            float temperature,
            int moonPhase,
            boolean night,
            boolean rain
    ) {
        ServerWorld world = base.level();
        world.setTimeOfDay(night ? 18_000L : 6_000L);
        world.setWeather(rain ? 0 : 6_000, rain ? 6_000 : 0, rain, false);
        RegistryEntry.Reference<Biome> exactBiome = biomeRegistry.entryOf(exactBiomeKey);
        RegistryEntry.Reference<Biome> nearestBiome = biomeRegistry.entryOf(nearestBiomeKey);
        BlockPos blockPos = new BlockPos(0, y, 0);

        com.li64.tide.data.fishing.FishingContext tide = new com.li64.tide.data.fishing.FishingContext(
                base.level(),
                base.hook(),
                base.rod(),
                base.rng(),
                blockPos.toCenterPos(),
                blockPos,
                base.luck(),
                medium,
                exactBiome,
                nearestBiome,
                dimension,
                temperature,
                moonPhase,
                base.season()
        );

        List<SpeciesProfile> eligible = new ArrayList<>();
        for (FishData data : TideData.FISH.get().values()) {
            profiles.adapt(data, tide).ifPresent(candidate -> eligible.add(candidate.profile()));
        }
        FishingEnvironment environment = contexts.environment(tide);
        return new RealTideFishingSimulator.LocationPool(id, description, environment, eligible);
    }
}
