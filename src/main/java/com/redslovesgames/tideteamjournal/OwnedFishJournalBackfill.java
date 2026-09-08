package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.CrateFishProgressionBridge;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import java.util.function.Consumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Backfills a missing canonical Journal specimen from fish the player actually still owns.
 *
 * <p>This path never calls Tide's catch accounting. It may deterministically normalize a readable
 * legacy fish stack, then copies that canonical specimen into the Journal sidecar only when the
 * species is already unlocked and no real/latest canonical specimen has previously been stored.
 */
public final class OwnedFishJournalBackfill {
    private static final String TEAM_ROOT_KEY = "tide_team_journal";
    private static final String JOURNAL_KEY = "journal";
    private static boolean initialized;

    private OwnedFishJournalBackfill() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        TeamEvent.PLAYER_LOGGED_IN.register(
                (Consumer<PlayerLoggedInAfterTeamEvent>) event -> backfillAndSync(event.getPlayer())
        );
        TeamEvent.PLAYER_CHANGED.register((Consumer<PlayerChangedTeamEvent>) event -> {
            if (event.getPlayer() != null) {
                backfillAndSync(event.getPlayer());
            }
        });
    }

    /** Imports missing specimen snapshots from the player's current inventory and syncs if changed. */
    public static int backfillAndSync(ServerPlayerEntity player) {
        if (player == null) {
            return 0;
        }
        try {
            // Ensure the authoritative personal/effective team Journal roots and legacy migrations exist.
            TeamJournalService.loadFor(player);
            Team team = (Team) FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
            if (team == null) {
                return 0;
            }
            NbtCompound extraData = team.getExtraData();
            if (!extraData.contains(TEAM_ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
                return 0;
            }

            NbtCompound root = extraData.getCompound(TEAM_ROOT_KEY);
            int imported = backfillInventory(root, player);
            if (imported > 0) {
                team.markDirty();
                TeamJournalService.syncCurrentJournal(player);
                TideTeamJournal.LOGGER.info(
                        "Backfilled {} owned legacy fish specimen(s) into {}'s team Journal without replaying catches",
                        imported,
                        player.getGameProfile().getName()
                );
            }
            return imported;
        } catch (RuntimeException | LinkageError failure) {
            TideTeamJournal.LOGGER.warn(
                    "Could not backfill owned legacy fish into the Journal for {}",
                    player.getGameProfile().getName(),
                    failure
            );
            return 0;
        }
    }

    public static int backfillInventory(NbtCompound journalRoot, ServerPlayerEntity player) {
        if (journalRoot == null || player == null) {
            return 0;
        }
        int imported = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (backfillStack(journalRoot, player.getInventory().getStack(slot))) {
                imported++;
            }
        }
        return imported;
    }

    /**
     * Imports one actual owned fish as the missing latest-known specimen for an already-unlocked
     * Journal species. Historical Tide counters, dates, largest/smallest values, history, records,
     * contributor totals, rewards, and Momentum are untouched.
     */
    public static boolean backfillStack(NbtCompound journalRoot, ItemStack stack) {
        if (journalRoot == null || stack == null || stack.isEmpty()
                || !journalRoot.contains(JOURNAL_KEY, NbtElement.COMPOUND_TYPE)) {
            return false;
        }

        FishData fish = FishData.get(stack).or(() -> FishData.fromBucket(stack)).orElse(null);
        if (fish == null) {
            return false;
        }

        TidePlayerData journal = new TidePlayerData(journalRoot.getCompound(JOURNAL_KEY));
        Item fishItem = (Item) fish.fish().value();
        boolean alreadyUnlocked = journal.getDataFor(fishItem)
                .map(data -> data.isUnlocked)
                .orElse(false);
        if (!alreadyUnlocked) {
            return false;
        }

        String speciesId = Registries.ITEM.getId(fishItem).toString();
        if (JournalSpecimenStore.read(journalRoot, speciesId, JournalSpecimenStore.LATEST).isPresent()) {
            return false;
        }

        // This accepts current canonical fish, recoverable Tideborne legacy fish, and old Tide fish
        // that only retain a physical length. It does not call logCatch or any progression path.
        if (!CrateFishProgressionBridge.ensureCanonicalForCatchAccounting(stack)) {
            return false;
        }
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        if (specimen == null || !speciesId.equals(specimen.speciesId())) {
            return false;
        }

        return JournalSpecimenStore.capture(journalRoot, specimen, false, false);
    }
}
