package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.Tide;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.CatchTimestamp;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideteamjournal.OwnedFishJournalBackfill;
import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class LegacyJournalPersistenceGameTests implements FabricGameTest {
    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void personalAndTeamLegacyJournalRootsBackfillCanonicalRecordsIdempotently(TestContext helper) {
        ServerPlayerEntity player = (ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack cod = new ItemStack(Items.COD);
        FishData fish = FishData.get(cod).orElseThrow();
        double legacyLength = 58.5;

        FishStats stats = new FishStats();
        stats.logCatch(CatchTimestamp.now(player.getWorld()), legacyLength);
        TidePlayerData legacyJournal = new TidePlayerData();
        legacyJournal.fishPlayerData.put(
                fish.fish(),
                new FishPlayerData(true, true, false, Optional.of(stats))
        );

        NbtCompound personalRoot = Tide.PLATFORM.getPlayerData(player);
        personalRoot.put("TidePlayerData", legacyJournal.getAsTag());
        personalRoot.remove(JournalSpecimenStore.ROOT_KEY);
        helper.assertTrue(PersonalTideJournal.load(player).isPresent(),
                "Legacy personal Tide journal could not be loaded");
        helper.assertTrue(personalRoot.contains(JournalSpecimenStore.ROOT_KEY, 10),
                "Personal journal load did not backfill reconstructable canonical records");
        assertMigratedRecord(helper, personalRoot, "minecraft:cod", legacyLength, "personal");
        NbtCompound personalOnce = personalRoot.copy();
        PersonalTideJournal.load(player);
        helper.assertTrue(personalOnce.equals(personalRoot),
                "Repeated personal journal load changed already-migrated persistence");

        NbtCompound teamRoot = new NbtCompound();
        teamRoot.put("journal", legacyJournal.getAsTag());
        NbtCompound holders = new NbtCompound();
        holders.putString("owner_marker", "preserve");
        teamRoot.put("record_holders", holders.copy());
        helper.assertTrue(JournalSpecimenStore.migrateLegacyJournal(teamRoot),
                "Legacy team journal root did not backfill canonical records");
        assertMigratedRecord(helper, teamRoot, "minecraft:cod", legacyLength, "team");
        helper.assertTrue(holders.equals(teamRoot.getCompound("record_holders")),
                "Journal migration reinterpreted unrelated record-holder ownership metadata");
        NbtCompound teamOnce = teamRoot.copy();
        helper.assertTrue(!JournalSpecimenStore.migrateLegacyJournal(teamRoot),
                "Already-migrated team journal reported another migration");
        helper.assertTrue(teamOnce.equals(teamRoot),
                "Repeated team journal migration changed persistence");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void ownedLengthOnlyLegacyFishBackfillsMissingLatestWithoutReplayingCatch(TestContext helper) {
        ServerPlayerEntity player = (ServerPlayerEntity) helper.createMockCreativeServerPlayerInWorld();
        ItemStack cod = new ItemStack(Items.COD);
        FishData fish = FishData.get(cod).orElseThrow();
        double legacyLength = 47.25;

        FishStats stats = new FishStats();
        stats.logCatch(CatchTimestamp.now(player.getWorld()), legacyLength);
        TidePlayerData legacyJournal = new TidePlayerData();
        legacyJournal.fishPlayerData.put(
                fish.fish(),
                new FishPlayerData(true, true, false, Optional.of(stats))
        );

        NbtCompound teamRoot = new NbtCompound();
        teamRoot.put("journal", legacyJournal.getAsTag());
        TideItemData.FISH_LENGTH.set(cod, legacyLength);
        NbtCompound historicalJournal = teamRoot.getCompound("journal").copy();

        helper.assertTrue(
                CanonicalSpecimenStorage.detectMigration(cod) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Length-only old Tide fish unexpectedly started with Tideborne specimen metadata"
        );
        helper.assertTrue(
                JournalSpecimenStore.read(teamRoot, "minecraft:cod", JournalSpecimenStore.LATEST).isEmpty(),
                "Aggregate legacy migration unexpectedly invented a latest specimen"
        );
        helper.assertTrue(
                OwnedFishJournalBackfill.backfillStack(teamRoot, cod),
                "Actual owned legacy fish did not backfill the missing Journal specimen"
        );

        SpecimenData imported = JournalSpecimenStore.read(
                teamRoot,
                "minecraft:cod",
                JournalSpecimenStore.LATEST
        ).orElseThrow();
        helper.assertTrue(Double.compare(imported.finalLength(), legacyLength) == 0,
                "Owned-fish Journal backfill changed the preserved physical length");
        helper.assertTrue(imported.fishScore().isPresent(),
                "Owned-fish Journal backfill did not persist canonical FishScore");
        helper.assertTrue(
                CanonicalSpecimenStorage.detectMigration(cod) == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT,
                "Owned length-only fish was not normalized to current canonical specimen data"
        );
        helper.assertTrue(historicalJournal.equals(teamRoot.getCompound("journal")),
                "Owned-fish Journal backfill changed Tide catch counts, dates, or record statistics"
        );

        NbtCompound once = teamRoot.copy();
        helper.assertTrue(!OwnedFishJournalBackfill.backfillStack(teamRoot, cod),
                "Repeated owned-fish Journal backfill imported the same species twice");
        helper.assertTrue(once.equals(teamRoot),
                "Repeated owned-fish Journal backfill was not idempotent");
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void ownedFishDoesNotUnlockAnUncaughtJournalSpecies(TestContext helper) {
        ItemStack cod = new ItemStack(Items.COD);
        double legacyLength = 41.0;
        TideItemData.FISH_LENGTH.set(cod, legacyLength);

        NbtCompound teamRoot = new NbtCompound();
        teamRoot.put("journal", new TidePlayerData().getAsTag());
        helper.assertTrue(!OwnedFishJournalBackfill.backfillStack(teamRoot, cod),
                "Owned-fish backfill unlocked a species that was never caught in the Journal");
        helper.assertTrue(
                CanonicalSpecimenStorage.detectMigration(cod) == CanonicalSpecimenStorage.MigrationState.NONE,
                "Rejected uncaught fish was mutated during Journal backfill"
        );
        helper.assertTrue(
                JournalSpecimenStore.read(teamRoot, "minecraft:cod", JournalSpecimenStore.LATEST).isEmpty(),
                "Rejected uncaught fish created a canonical Journal specimen"
        );
        helper.complete();
    }

    private static void assertMigratedRecord(
            TestContext helper,
            NbtCompound root,
            String speciesId,
            double expectedLength,
            String surface
    ) {
        SpecimenData largest = JournalSpecimenStore.read(root, speciesId, JournalSpecimenStore.LARGEST).orElseThrow();
        SpecimenData smallest = JournalSpecimenStore.read(root, speciesId, JournalSpecimenStore.SMALLEST).orElseThrow();
        helper.assertTrue(Double.compare(expectedLength, largest.finalLength()) == 0,
                "Legacy " + surface + " largest length changed during migration");
        helper.assertTrue(Double.compare(expectedLength, smallest.finalLength()) == 0,
                "Legacy " + surface + " smallest length changed during migration");
        helper.assertTrue(largest.rawFishScore().isEmpty() && largest.fishScore().isEmpty(),
                "Legacy " + surface + " migration invented historical FishScore data");
        helper.assertTrue(JournalSpecimenStore.read(root, speciesId, JournalSpecimenStore.LATEST).isEmpty(),
                "Legacy " + surface + " migration invented an unreconstructable latest specimen");
    }
}
