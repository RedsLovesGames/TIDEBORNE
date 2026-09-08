package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.TideSpeciesProfileAdapter;
import java.util.Set;
import java.util.TreeSet;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/** Runtime regression matrix for Tideborne's supported optional fishing compatibility mods. */
public final class OptionalCompatibilityRegressionGameTests implements FabricGameTest {
    private static final String APEX_MOD_ID = "apexwaters";
    private static final String MYTHS_MOD_ID = "myths_of_the_sea";
    private static final String APEX_VERSION = "1.1.1";
    private static final String MYTHS_VERSION = "1.3.0";
    private static final long SPECIMEN_SEED = 0x560F710A2026L;

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void optionalRuntimePresenceMatchesRequestedMatrix(TestContext helper) {
        boolean expectedApex = Boolean.parseBoolean(System.getProperty("tideborne.expectedApexRuntime", "false"));
        boolean expectedMyths = Boolean.parseBoolean(System.getProperty("tideborne.expectedMythsRuntime", "false"));
        FabricLoader loader = FabricLoader.getInstance();

        helper.assertTrue(loader.isModLoaded(APEX_MOD_ID) == expectedApex,
                "Apex Waters runtime presence did not match the Stage 56 matrix");
        helper.assertTrue(loader.isModLoaded(MYTHS_MOD_ID) == expectedMyths,
                "Myths of the Sea runtime presence did not match the Stage 56 matrix");

        if (expectedApex) {
            String version = loader.getModContainer(APEX_MOD_ID)
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
            helper.assertTrue(version.startsWith(APEX_VERSION),
                    "Stage 56 loaded an unexpected Apex Waters version: " + version);
        }
        if (expectedMyths) {
            String version = loader.getModContainer(MYTHS_MOD_ID)
                    .orElseThrow()
                    .getMetadata()
                    .getVersion()
                    .getFriendlyString();
            helper.assertTrue(version.startsWith(MYTHS_VERSION),
                    "Stage 56 loaded an unexpected Myths of the Sea version: " + version);
        }
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void optionalModsDoNotInventTideFishProfilesOrEligibility(TestContext helper) {
        Set<String> apexSpecies = tideFishSpeciesIds(APEX_MOD_ID);
        Set<String> mythsSpecies = tideFishSpeciesIds(MYTHS_MOD_ID);

        // Apex Waters 1.1.1 and Myths of the Sea 1.3.0 add aquatic entities/items,
        // but neither supported artifact registers Tide FishData. Therefore neither can
        // enter Tide's fish selector or become a synthetic Fishing System 2.0 species.
        helper.assertTrue(apexSpecies.isEmpty(),
                "Apex Waters unexpectedly registered Tide fish species: " + apexSpecies);
        helper.assertTrue(mythsSpecies.isEmpty(),
                "Myths of the Sea unexpectedly registered Tide fish species: " + mythsSpecies);

        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void everyLiveTideProfileAndSpecimenRemainStableWithOptionalMods(TestContext helper) {
        TideSpeciesProfileAdapter adapter = new TideSpeciesProfileAdapter();
        SpecimenGenerator generator = new SpecimenGenerator();
        int validated = 0;

        for (FishData fish : TideData.FISH.get().values()) {
            String expectedSpeciesId = speciesId(fish);
            helper.assertTrue(!isOptionalNamespace(expectedSpeciesId),
                    "Optional runtime unexpectedly entered the Tide fish eligibility pool: " + expectedSpeciesId);

            SpeciesProfile profile = adapter.adaptForMigration(fish);
            helper.assertTrue(profile.speciesId().equals(expectedSpeciesId),
                    "Optional runtime changed the canonical species ID for " + expectedSpeciesId);

            long seed = SPECIMEN_SEED ^ expectedSpeciesId.hashCode();
            SpecimenData first = generator.generate(profile, seed, SpecimenData.Provenance.generated());
            SpecimenData second = generator.generate(profile, seed, SpecimenData.Provenance.generated());

            helper.assertTrue(first.equals(second),
                    "Optional runtime changed deterministic canonical specimen generation for " + expectedSpeciesId);
            helper.assertTrue(first.speciesId().equals(profile.speciesId()),
                    "Generated specimen did not retain the adapted Tide species ID for " + expectedSpeciesId);
            helper.assertTrue(Double.isFinite(first.baseLength()) && first.baseLength() > 0.0,
                    "Generated specimen did not retain a valid canonical base size for " + expectedSpeciesId);
            helper.assertTrue(Double.isFinite(first.finalLength()) && first.finalLength() > 0.0,
                    "Generated specimen did not retain a valid canonical final size for " + expectedSpeciesId);
            helper.assertTrue(first.fishScore().isPresent(),
                    "Generated specimen did not receive canonical FishScore for " + expectedSpeciesId);
            validated++;
        }

        helper.assertTrue(validated > 0, "No Tide fish profiles were available for Stage 56 validation");
        helper.complete();
    }

    private static Set<String> tideFishSpeciesIds(String namespace) {
        Set<String> species = new TreeSet<>();
        for (FishData data : TideData.FISH.get().values()) {
            String speciesId = speciesId(data);
            if (speciesId.startsWith(namespace + ":")) {
                species.add(speciesId);
            }
        }
        return Set.copyOf(species);
    }

    private static String speciesId(FishData data) {
        Item fishItem = (Item) data.fish().value();
        return Registries.ITEM.getId(fishItem).toString();
    }

    private static boolean isOptionalNamespace(String speciesId) {
        return speciesId.startsWith(APEX_MOD_ID + ":")
                || speciesId.startsWith(MYTHS_MOD_ID + ":");
    }
}
