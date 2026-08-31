package com.redslovesgames.tideborne.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.redslovesgames.tideborne.fishing.v2.integration.LegacyFishRecoveryService;
import com.redslovesgames.tideborne.fishing.v2.integration.LegacyFishRecoveryService.Result;
import com.redslovesgames.tideborne.fishing.v2.integration.LegacyFishRecoveryService.Status;
import com.redslovesgames.tideteamjournal.OwnedFishJournalBackfill;
import com.redslovesgames.tideteamjournal.RecoveredSpecimenRecordService;
import java.util.SplittableRandom;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Operator-only legacy fish repair and explicitly confirmed destructive reroll tooling. */
public final class FishingRecoveryCommand {
    private static final LegacyFishRecoveryService RECOVERY = new LegacyFishRecoveryService();

    private FishingRecoveryCommand() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> repairCommand() {
        return CommandManager.literal("repair")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("held").executes(context -> repairHeld(context.getSource())))
                .then(CommandManager.literal("inventory").executes(context -> repairInventory(context.getSource())));
    }

    public static LiteralArgumentBuilder<ServerCommandSource> rerollCommand() {
        return CommandManager.literal("reroll")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("held")
                        .executes(context -> rerollHeld(context.getSource(), false))
                        .then(CommandManager.literal("--confirm")
                                .executes(context -> rerollHeld(context.getSource(), true))))
                .then(CommandManager.literal("inventory")
                        .executes(context -> rerollInventory(context.getSource(), false))
                        .then(CommandManager.literal("--confirm")
                                .executes(context -> rerollInventory(context.getSource(), true))));
    }

    private static int repairHeld(ServerCommandSource source) {
        ServerPlayerEntity player = player(source);
        if (player == null) return 0;
        ItemStack held = player.getMainHandStack();
        Result result = RECOVERY.repair(held);
        int journalImported = OwnedFishJournalBackfill.backfillAndSync(player);
        boolean recordsIndexed = RecoveredSpecimenRecordService.indexAndSync(player, held);
        if (result.status() == Status.REPAIRED) {
            send(source, "Repaired held legacy fish into canonical Fishing System 2.0 data."
                    + journalSuffix(journalImported, recordsIndexed));
            return 1;
        }
        if (result.status() == Status.ALREADY_CURRENT) {
            send(source, "Held fish is already current canonical Fishing System 2.0 data."
                    + journalSuffix(journalImported, recordsIndexed));
            return journalImported > 0 || recordsIndexed ? 1 : 0;
        }
        if (journalImported > 0 || recordsIndexed) {
            send(source, "Recovered the owned fish into canonical Journal/record projections without replaying a catch."
                    + journalSuffix(journalImported, recordsIndexed));
            return 1;
        }
        source.sendError(Text.literal("Held item is not a recoverable legacy Tide fish. No changes made."));
        return 0;
    }

    private static int repairInventory(ServerCommandSource source) {
        ServerPlayerEntity player = player(source);
        if (player == null) return 0;
        int repaired = 0;
        int current = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            Result result = RECOVERY.repair(stack);
            if (result.status() == Status.REPAIRED) repaired++;
            if (result.status() == Status.ALREADY_CURRENT) current++;
        }
        int journalImported = OwnedFishJournalBackfill.backfillAndSync(player);
        int recordUpdates = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            if (RecoveredSpecimenRecordService.indexAndSync(player, player.getInventory().getStack(slot))) {
                recordUpdates++;
            }
        }
        int repairedCount = repaired;
        int currentCount = current;
        int importedCount = journalImported;
        int indexedCount = recordUpdates;
        source.sendFeedback(() -> Text.literal("Legacy fish repair complete: " + repairedCount
                + " repaired, " + currentCount + " already current, " + importedCount
                + " missing Journal specimen(s) backfilled, " + indexedCount
                + " canonical record projection update(s)."), true);
        return repaired + journalImported + recordUpdates;
    }

    private static int rerollHeld(ServerCommandSource source, boolean confirmed) {
        ServerPlayerEntity player = player(source);
        if (player == null) return 0;
        if (!confirmed) return confirmationRequired(source, "held");
        long seed = new SplittableRandom(System.nanoTime() ^ player.getUuid().getLeastSignificantBits()).nextLong();
        Result result = RECOVERY.reroll(player.getMainHandStack(), seed, true);
        if (result.status() == Status.REROLLED) {
            send(source, "Destructively rerolled held fish with newly generated canonical specimen data.");
            return 1;
        }
        source.sendError(Text.literal("Held item is not a registered Tide fish. No changes made."));
        return 0;
    }

    private static int rerollInventory(ServerCommandSource source, boolean confirmed) {
        ServerPlayerEntity player = player(source);
        if (player == null) return 0;
        if (!confirmed) return confirmationRequired(source, "inventory");
        SplittableRandom seeds = new SplittableRandom(System.nanoTime() ^ player.getUuid().getMostSignificantBits());
        int rerolled = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            Result result = RECOVERY.reroll(player.getInventory().getStack(slot), seeds.nextLong(), true);
            if (result.status() == Status.REROLLED) rerolled++;
        }
        int count = rerolled;
        source.sendFeedback(() -> Text.literal("Destructive canonical reroll complete: " + count + " Tide fish rerolled."), true);
        return rerolled;
    }

    private static int confirmationRequired(ServerCommandSource source, String target) {
        source.sendError(Text.literal("Reroll is destructive and replaces specimen identity. Re-run as `reroll "
                + target + " --confirm` to proceed."));
        return 0;
    }

    private static ServerPlayerEntity player(ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity player) return player;
        source.sendError(Text.literal("Fish repair/reroll targeting must be run by a player."));
        return null;
    }

    private static String journalSuffix(int imported, boolean recordsIndexed) {
        String journal = imported > 0 ? " Backfilled " + imported + " missing Journal specimen(s)." : " No Journal backfill was needed.";
        return journal + (recordsIndexed ? " Reindexed canonical Best Specimen/Top 15 records." : " Canonical records were already current.");
    }

    private static void send(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), true);
    }
}
