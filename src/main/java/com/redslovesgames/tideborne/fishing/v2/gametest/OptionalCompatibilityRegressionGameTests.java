package com.redslovesgames.tideborne.fishing.v2.gametest;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.v2.integration.ApexWatersSpeciesProfileAdapter;
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
    private static final String MYTHS_MOD_ID = "myths_of_the_sea";
    private static final String APEX_VERSION = "1.1.1";
    private static final String MYTHS_VERSION = "1.3.0";
    private static final long SPECIMEN_SEED = 0x560F710A2026L;

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void optionalRuntimePresenceMatchesRequestedMatrix(TestContext helper) {
        boolean expectedApex = Boolean.parseBoolean(System.getProperty("tideborne.expectedApexRuntime", "false"));
        boolean expectedMyths = Boolean.parseBoolean(System.getProperty("tideborne.expectedMythsRuntime", "false"));
        FabricLoader loader = FabricLoader.getInstance();

        helper.assertTrue(loader.isModLoaded(ApexWatersSpeciesProfileAdapter.MOD_ID) == expectedApex,
                "Apex Waters runtime presence did not match the Stage 56 matrix");
        helper.assertTrue(loader.isModLoaded(MYTHS_MOD_ID) == expectedMyths,
                "Myths of the Sea runtime presence did not match the Stage 56 matrix");

        if (expectedApex) {
            String version = loader.getModContainer(ApexWatersSpeciesProfileAdapter.MOD_ID)
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
        Set<String> apexSpecies = tideFishSpeciesIds(ApexWatersSpeciesProfileAdapter.MOD_ID);
        Set<String> mythsSpecies = tideFishSpeciesIds(MYTHS_MOD_ID);

        // Apex Waters 1.1.1 and Myths of the Sea 1.3.0 add aquatic entities/items,
        // but neither supported artifact registers Tide FishData. Therefore neither can
        // enter Tide's fish selector or become a synthetic Fishing System 2.0 species.
        helper.assertTrue(apexSpecies.isEmpty(),
                "Apex Waters unexpectedly registered Tide fish species: " + apexSpecies);
        helper.assertTrue(mythsSpecies.isEmpty(),
                "Myths of the Sea unexpectedly registered Tide fish species: " + mythsSpecies);

        ApexWatersSpeciesProfileAdapter apexAdapter = new ApexWatersSpeciesProfileAdapter();
        helper.assertTrue(apexAdapter.tideCatchableSpeciesIds().isEmpty(),
                "Apex Waters canonical profile audit unexpectedly exposed catchable species");
        helper.assertTrue(apexAdapter.adapt(ApexWatersSpeciesProfileAdapter.GREAT_WHITE_SHARK_ENTITY_ID).isEmpty(),
                "Apex Great White Shark entity was incorrectly adapted into a Tide fish profile");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void nativeTideProfileAndSpecimenGenerationRemainStableWithOptionalMods(TestContext helper) {
        FishData nativeFish = TideData.FISH.get().values().stream()
                .filter(data -> !isOptionalNamespace(speciesId(data)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No native Tide FishData was available for Stage 56 validation"));

        TideSpeciesProfileAdapter adapter = new TideSpeciesProfileAdapter();
        SpeciesProfile profile = adapter.adaptForMigration(nativeFish);
        SpecimenGenerator generator = new SpecimenGenerator();
        SpecimenData first = generator.generate(profile, SPECIMEN_SEED, SpecimenData.Provenance.generated());
        SpecimenData second = generator.generate(profile, SPECIMEN_SEED, SpecimenData.Provenance.generated());

        helper.assertTrue(profile.speciesId().equals(speciesId(nativeFish)),
                "Optional runtime changed the canonical species ID produced from Tide FishData");
        helper.assertTrue(first.equals(second),
                "Optional runtime changed deterministic canonical specimen generation");
        helper.assertTrue(first.speciesId().equals(profile.speciesId()),
                "Generated specimen did not retain the adapted Tide species ID");
        helper.assertTrue(Double.isFinite(first.baseLength()) && first.baseLength() > 0.0,
                "Generated specimen did not retain a valid canonical base size");
        helper.assertTrue(Double.isFinite(first.finalLength()) && first.finalLength() > 0.0,
                "Generated specimen did not retain a valid canonical final size");
        helper.assertTrue(first.fishScore().isPresent(),
                "Generated specimen did not receive canonical FishScore under optional runtime validation");
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
        return speciesId.startsWith(ApexWatersSpeciesProfileAdapter.MOD_ID + ":")
                || speciesId.startsWith(MYTHS_MOD_ID + ":");
    }
}
