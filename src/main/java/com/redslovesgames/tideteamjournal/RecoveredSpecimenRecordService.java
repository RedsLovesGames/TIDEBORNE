package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
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
    private static final String TEAM_ROOT_KEY = "tide_team_journal";
    private static final String JOURNAL_KEY = "journal";
    private static final String CONTRIBUTORS_KEY = "contributors";

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

            boolean changed = JournalSpecimenStore.indexBest(root, specimen);
            changed |= indexContributor(root, player, specimen);

            NbtCompound topFish = CanonicalSpecimenRecordIndexer.project(specimen);
            topFish.putInt("fish_stars", TeamProgressStore.tideborneFishStars(stack));
            topFish.putUuid("catcher_id", player.getUuid());
            topFish.putString("catcher_name", player.getGameProfile().getName());
            topFish.putBoolean("recovered", true);
            changed |= CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, topFish);

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

    private static boolean indexContributor(NbtCompound root, ServerPlayerEntity player, SpecimenData specimen) {
        TeamProgressStore.ensureInitialized(root);
        NbtCompound contributors = root.getCompound(CONTRIBUTORS_KEY);
        String key = player.getUuid().toString();
        NbtCompound contributor = contributors.contains(key, NbtElement.COMPOUND_TYPE)
                ? contributors.getCompound(key)
                : new NbtCompound();
        contributor.putString("name", player.getGameProfile().getName());
        boolean changed = StoredFishScoreStorage.updateBest(contributor, specimen.fishScore());
        contributors.put(key, contributor);
        root.put(CONTRIBUTORS_KEY, contributors);
        return changed;
    }
}
