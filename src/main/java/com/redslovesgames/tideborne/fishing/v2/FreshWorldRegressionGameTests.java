package com.redslovesgames.tideborne.fishing.v2;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderAttachment;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import com.redslovesgames.tideboundcompatibility.fishing.TideborneFishingGearModifiers;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/** Fresh-state regression coverage after the Stage 50-53 legacy calculator removals. */
@SuppressWarnings("deprecation")
public final class FreshWorldRegressionGameTests implements FabricGameTest {
    private static final String SPECIES_ID = "tideborne:fresh_world_fixture";

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void freshMomentumStorageStartsEmptyAndRoundTripsCurrentFormat(TestContext helper) {
        NbtCompound playerRoot = new NbtCompound();
        TraitMomentumState initial = TraitMomentumStorage.readFromPlayerRoot(playerRoot);

        helper.assertTrue(initial.snapshot().isEmpty(), "Fresh player state unexpectedly contained Trait Momentum");
        helper.assertTrue(initial.get(SPECIES_ID) == 0, "Fresh species Momentum did not start at zero");

        initial.set(SPECIES_ID, 1);
        TraitMomentumStorage.writeToPlayerRoot(playerRoot, initial);
        helper.assertTrue(playerRoot.contains(TraitMomentumStorage.PLAYER_DATA_KEY),
                "Current Trait Momentum root was not written for fresh player state");

        TraitMomentumState restored = TraitMomentumStorage.readFromPlayerRoot(playerRoot);
        helper.assertTrue(restored.get(SPECIES_ID) == 1,
                "Current versioned Trait Momentum did not round-trip from fresh player state");
        helper.assertTrue(restored.snapshot().size() == 1,
                "Fresh Momentum round trip created unexpected species state");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void freshCanonicalGenerationPersistsV2SizeTraitsAndScore(TestContext helper) {
        SpeciesProfile species = fixtureSpecies();
        SpecimenData specimen = new SpecimenGenerator().generate(
                species,
                0x54F12E2026L,
                SpecimenData.Provenance.generated()
        );
        ItemStack fish = new ItemStack(Items.COD);

        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(fish) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Fresh fish stack unexpectedly required legacy migration before canonical generation");
        helper.assertTrue(specimen.schemaVersion() == SpecimenGenerator.SCHEMA_VERSION,
                "Fresh specimen did not use the current canonical schema");
        helper.assertTrue(specimen.generationVersion() == SpecimenGenerator.GENERATION_VERSION,
                "Fresh specimen did not use the current canonical generation version");
        helper.assertTrue(Double.isFinite(specimen.baseLength()) && specimen.baseLength() > 0.0,
                "Fresh specimen did not receive a valid canonical base length");
        helper.assertTrue(Double.isFinite(specimen.finalLength()) && specimen.finalLength() > 0.0,
                "Fresh specimen did not receive a valid canonical final length");
        helper.assertTrue(specimen.basePercentile() >= 0.0 && specimen.basePercentile() < 100.0,
                "Fresh specimen natural percentile was outside the canonical range");
        helper.assertTrue(specimen.rawFishScore().isPresent(),
                "Fresh finalized specimen did not receive canonical raw FishScore");
        helper.assertTrue(specimen.fishScore().isPresent()
                        && specimen.fishScore().getAsInt() >= 1
                        && specimen.fishScore().getAsInt() <= 3000,
                "Fresh finalized specimen did not receive canonical 1-3000 FishScore");

        CanonicalSpecimenStorage.write(fish, specimen);
        helper.assertTrue(CanonicalSpecimenStorage.detectMigration(fish)
                        == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Fresh generated fish was not persisted as current canonical state");
        helper.assertTrue(specimen.equals(CanonicalSpecimenStorage.read(fish).orElseThrow()),
                "Fresh generated specimen changed during canonical ItemStack persistence");
        helper.assertTrue(Double.compare(
                        specimen.finalLength(),
                        TideItemData.FISH_LENGTH.getOrDefault(fish, -1.0)
                ) == 0,
                "Fresh generated fish did not mirror canonical final length to Tide item data");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void freshGearStateUsesCurrentComponentsAndCanonicalModifiers(TestContext helper) {
        TideboundConfig.Values config = new TideboundConfig.Values();
        ItemStack rod = new ItemStack(Items.FISHING_ROD);

        helper.assertTrue(!SteelLeaderAttachment.has(rod),
                "Brand-new rod unexpectedly contained legacy leader attachment state");
        assertNeutralLegacyLeader(helper, SteelLeaderGearModifiers.forAttachmentState(false, config));

        SteelLeaderAttachment.set(rod, true);
        helper.assertTrue(SteelLeaderAttachment.has(rod),
                "Legacy leader attachment component could not be attached to a brand-new rod");
        FishingGearModifiers leader = SteelLeaderGearModifiers.forAttachmentState(
                SteelLeaderAttachment.has(rod),
                config
        );
        helper.assertTrue(Double.compare(FishingGearEffects.catchZoneAreaMultiplier(leader), 0.95) == 0,
                "Legacy Steel attachment did not migrate to Iron Leader catch-zone modifier");
        helper.assertTrue(Double.compare(FishingGearEffects.minigameSpeedMultiplier(leader), 1.03) == 0,
                "Legacy Steel attachment did not migrate to Iron Leader minigame-speed modifier");
        helper.assertTrue(Double.compare(FishingGearEffects.catchLossPreventionChance(leader), 0.55) == 0,
                "Legacy Steel attachment did not migrate to Iron Leader catch-loss prevention");
        helper.assertTrue(Double.compare(
                        leader.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES),
                        1.0
                ) == 0,
                "Migrated Iron Leader did not expose one canonical protection source");

        SteelLeaderAttachment.set(rod, false);
        helper.assertTrue(!SteelLeaderAttachment.has(rod),
                "Legacy leader attachment component could not be removed from a fresh rod");

        FishingGearModifiers leviathan = TideborneFishingGearModifiers.leviathanBait(true);
        helper.assertTrue(Double.compare(leviathan.fishingLuck(), 15.0) == 0,
                "Fresh Leviathan Bait did not provide +15 Fishing Luck");
        helper.assertTrue(Double.compare(leviathan.traitLuck(), 4.0) == 0,
                "Fresh Leviathan Bait did not provide +4 Trait Luck");
        helper.assertTrue(Double.compare(leviathan.strengthMultiplier(), 1.25) == 0,
                "Fresh Leviathan Bait did not provide Strength x1.25");
        helper.assertTrue(Double.compare(leviathan.tempoMultiplier(), 1.20) == 0,
                "Fresh Leviathan Bait did not provide Tempo x1.20");
        helper.assertTrue(leviathan.categoryRestriction().allows(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY),
                "Fresh Leviathan Bait did not allow the fish catch category");
        helper.assertTrue(!leviathan.categoryRestriction().allows("crate"),
                "Fresh Leviathan Bait did not exclude non-fish catch categories");
        helper.complete();
    }

    private static void assertNeutralLegacyLeader(TestContext helper, FishingGearModifiers modifiers) {
        helper.assertTrue(Double.compare(FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0) == 0,
                "Fresh rod without a leader changed catch-zone area");
        helper.assertTrue(Double.compare(FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0) == 0,
                "Fresh rod without a leader changed minigame speed");
        helper.assertTrue(Double.compare(FishingGearEffects.catchLossPreventionChance(modifiers), 0.0) == 0,
                "Fresh rod without a leader added catch-loss prevention");
        helper.assertTrue(Double.compare(
                        modifiers.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES),
                        0.0
                ) == 0,
                "Fresh rod without a leader exposed a protection source");
    }

    private static SpeciesProfile fixtureSpecies() {
        return new SpeciesProfile(
                SPECIES_ID,
                CanonicalRarity.THREE_STAR,
                1.0,
                SpeciesEligibility.always(),
                1.0,
                1.0,
                "steady",
                new LogNormalSizeDistribution(30.0, 0.20),
                Set.of(),
                Map.of()
        );
    }
}
