package com.redslovesgames.tideborne.journal;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyPersistenceIds;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-only record reconstruction for already-owned canonical fish.
 *
 * <p>This path updates derived specimen records only. It never calls Tide catch accounting, record
 * event history, discovery, rewards, achievements, Momentum, or any other new-catch progression.
 */
public final class RecoveredSpecimenRecordService {
    private static final String TEAM_ROOT_KEY = LegacyPersistenceIds.TEAM_JOURNAL_ROOT;
    private static final String JOURNAL_KEY = "journal";

    private RecoveredSpecimenRecordService() {
    }

    /** Returns true only when at least one derived record projection changed. */
    public static boolean indexAndSync(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return false;
        }
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        FishData fish = FishData.get(stack).or(() -> FishData.fromBucket(stack)).orElse(null);
        if (specimen == null || specimen.fishScore().isEmpty() || fish == null) {
            return false;
        }

        try {
            TeamJournalService.loadFor(player);
            Team team = (Team) FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
            if (team == null) {
                return false;
            }
            NbtCompound extraData = team.getExtraData();
            if (!extraData.contains(TEAM_ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
                return false;
            }
            NbtCompound root = extraData.getCompound(TEAM_ROOT_KEY);
            if (!root.contains(JOURNAL_KEY, NbtElement.COMPOUND_TYPE)) {
                return false;
            }

            TidePlayerData journal = new TidePlayerData(root.getCompound(JOURNAL_KEY));
            Item fishItem = (Item) fish.fish().value();
            boolean historicallyUnlocked = journal.getDataFor(fishItem).map(data -> data.isUnlocked).orElse(false);
            if (!historicallyUnlocked || !specimen.speciesId().equals(net.minecraft.registry.Registries.ITEM.getId(fishItem).toString())) {
                return false;
            }

            boolean changed = RecoveredSpecimenRecordIndexer.index(
                    root,
                    specimen,
                    player.getUuid(),
                    player.getGameProfile().getName(),
                    TeamProgressStore.tideborneFishStars(stack)
            );
            if (changed) {
                team.markDirty();
                TeamJournalService.syncCurrentJournal(player);
                TeamJournalService.sendTeamData(player, 0, "fish_score", "", "all");
            }
            return changed;
        } catch (RuntimeException | LinkageError failure) {
            TideTeamJournal.LOGGER.warn(
                    "Could not reindex recovered canonical fish records for {}",
                    player.getGameProfile().getName(),
                    failure
            );
            return false;
        }
    }
}
