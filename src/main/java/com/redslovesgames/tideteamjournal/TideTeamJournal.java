/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.li64.tide.util.TideUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.redslovesgames.tideteamjournal.network.BobberSettingsPayload;
import com.redslovesgames.tideteamjournal.network.OpenTeamRecordsPayload;
import com.redslovesgames.tideteamjournal.network.RecordEventPayload;
import com.redslovesgames.tideteamjournal.network.RecordHoldersPayload;
import com.redslovesgames.tideteamjournal.network.TeamDataPayload;
import com.redslovesgames.tideteamjournal.network.TeamDataRequestPayload;
import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLoggedInAfterTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TideTeamJournal implements ModInitializer {
   public static final String MOD_ID = "tide_team_journal";
   public static final Logger LOGGER = LoggerFactory.getLogger("tide_team_journal");

   public void onInitialize() {
      ServerConfig.load();
      PayloadTypeRegistry.playS2C().register(RecordHoldersPayload.TYPE, RecordHoldersPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(TeamDataPayload.TYPE, TeamDataPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(RecordEventPayload.TYPE, RecordEventPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(BobberSettingsPayload.TYPE, BobberSettingsPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(OpenTeamRecordsPayload.TYPE, OpenTeamRecordsPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(TeamDataRequestPayload.TYPE, TeamDataRequestPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(
         TeamDataRequestPayload.TYPE,
         (payload, context) -> TeamJournalService.sendTeamData(context.player(), payload.page(), payload.metric(), payload.fishFilter(), payload.eventType())
      );
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         register(dispatcher, "tideborne_internal_team");
         register(dispatcher, "ttj");
      });
      TeamEvent.PLAYER_LOGGED_IN.register((Consumer<PlayerLoggedInAfterTeamEvent>)event -> TeamJournalService.syncCurrentJournal(event.getPlayer()));
      TeamEvent.PLAYER_CHANGED.register((Consumer<PlayerChangedTeamEvent>)event -> {
         if (event.getPlayer() != null) {
            TeamJournalService.syncCurrentJournal(event.getPlayer());
         }
      });
      LOGGER.info("[Tideborne] Shared/team journal initialized");
   }

   private static void register(CommandDispatcher<ServerCommandSource> dispatcher, String root) {
      dispatcher.register(command(root));
   }

   private static LiteralArgumentBuilder<ServerCommandSource> command(String root) {
      return (LiteralArgumentBuilder<ServerCommandSource>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                             root
                                          )
                                          .executes(TideTeamJournal::openRecords))
                                       .then((LiteralArgumentBuilder)CommandManager.literal("help").executes(TideTeamJournal::help)))
                                    .then((LiteralArgumentBuilder)CommandManager.literal("open").executes(TideTeamJournal::openRecords)))
                                 .then((LiteralArgumentBuilder)CommandManager.literal("merge").executes(TideTeamJournal::merge)))
                              .then(
                                 (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("claim")
                                       .then((LiteralArgumentBuilder)CommandManager.literal("largest").executes(context -> claimRecord(context, true))))
                                    .then((LiteralArgumentBuilder)CommandManager.literal("smallest").executes(context -> claimRecord(context, false)))
                              ))
                           .then(
                              (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("assign")
                                    .then(
                                       (LiteralArgumentBuilder)CommandManager.literal("largest")
                                          .then(
                                             (RequiredArgumentBuilder)CommandManager.argument("member", EntityArgumentType.player())
                                                .executes(context -> assignRecord(context, true))
                                          )
                                    ))
                                 .then(
                                    (LiteralArgumentBuilder)CommandManager.literal("smallest")
                                       .then(
                                          (RequiredArgumentBuilder)CommandManager.argument("member", EntityArgumentType.player())
                                             .executes(context -> assignRecord(context, false))
                                       )
                                 )
                           ))
                        .then(
                           (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("claimall").executes(TideTeamJournal::claimAllRecords))
                              .then(
                                 (RequiredArgumentBuilder)CommandManager.argument("member", EntityArgumentType.player())
                                    .executes(TideTeamJournal::claimAllRecordsFor)
                              )
                        ))
                     .then((LiteralArgumentBuilder)CommandManager.literal("status").executes(TideTeamJournal::recordStatus)))
                  .then(
                     (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("leaderboard")
                           .executes(context -> leaderboard(context, "catches")))
                        .then(
                           (RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("metric", StringArgumentType.word())
                                 .suggests((context, builder) -> CommandSource.suggestMatching(ServerConfig.get().visibleMetrics, builder))
                                 .executes(context -> leaderboard(context, StringArgumentType.getString(context, "metric"))))
                              .then(
                                 (RequiredArgumentBuilder)CommandManager.argument("page", IntegerArgumentType.integer(1))
                                    .executes(
                                       context -> leaderboard(
                                          context, StringArgumentType.getString(context, "metric"), IntegerArgumentType.getInteger(context, "page") - 1
                                       )
                                    )
                              )
                        )
                  ))
               .then(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("history")
                           .executes(context -> history(context, 0, "")))
                        .then(
                           (RequiredArgumentBuilder)CommandManager.argument("page", IntegerArgumentType.integer(1))
                              .executes(context -> history(context, IntegerArgumentType.getInteger(context, "page") - 1, ""))
                        ))
                     .then(
                        (LiteralArgumentBuilder)CommandManager.literal("fish")
                           .then(
                              (RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("fish", IdentifierArgumentType.identifier())
                                    .suggests((context, builder) -> CommandSource.suggestIdentifiers(Registries.ITEM.getIds(), builder))
                                    .executes(context -> history(context, 0, IdentifierArgumentType.getIdentifier(context, "fish").toString())))
                                 .then(
                                    (RequiredArgumentBuilder)CommandManager.argument("page", IntegerArgumentType.integer(1))
                                       .executes(
                                          context -> history(
                                             context, IntegerArgumentType.getInteger(context, "page") - 1, IdentifierArgumentType.getIdentifier(context, "fish").toString()
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               (LiteralArgumentBuilder)CommandManager.literal("member")
                  .then((RequiredArgumentBuilder)CommandManager.argument("name", StringArgumentType.word()).executes(TideTeamJournal::member))
            ))
         .then(
            (LiteralArgumentBuilder)CommandManager.literal("config")
               .then(
                  (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("reload").requires(source -> source.hasPermissionLevel(2)))
                     .executes(TideTeamJournal::reloadConfig)
               )
         );
   }

   private static int help(CommandContext<ServerCommandSource> context) {
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.tide_team_journal.help.header"), false);

      for (String key : List.of("open", "leaderboard", "history", "member", "merge", "claim", "assign", "claimall", "status", "reload")) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.tide_team_journal.help." + key), false);
      }

      return 1;
   }

   private static int openRecords(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      if (ServerPlayNetworking.canSend(player, OpenTeamRecordsPayload.TYPE)) {
         ServerPlayNetworking.send(player, new OpenTeamRecordsPayload());
         return 1;
      } else {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.open.failed"));
         return 0;
      }
   }

   private static int merge(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      TeamJournalService.ManualMergeResult result = TeamJournalService.manuallyMergePersonalJournal(((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      switch (result) {
         case MERGED:
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.tide_team_journal.merge.success"), false);
            break;
         case ALREADY_SHARED:
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.tide_team_journal.merge.already_shared"), false);
            break;
         case NOT_IN_PARTY:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.merge.not_in_party"));
            break;
         case FAILED:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.merge.failed"));
      }

      return result == TeamJournalService.ManualMergeResult.FAILED ? 0 : 1;
   }

   private static int claimRecord(CommandContext<ServerCommandSource> context, boolean largest) throws CommandSyntaxException {
      TeamJournalService.ClaimResult result = TeamJournalService.claimHeldRecord(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), largest);
      reportClaim(context, result, largest);
      return result == TeamJournalService.ClaimResult.CLAIMED ? 1 : 0;
   }

   private static int assignRecord(CommandContext<ServerCommandSource> context, boolean largest) throws CommandSyntaxException {
      ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "member");
      TeamJournalService.ClaimResult result = TeamJournalService.assignHeldRecord(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), target, largest);
      if (result == TeamJournalService.ClaimResult.CLAIMED) {
         ((ServerCommandSource)context.getSource())
            .sendFeedback(() -> Text.translatable("command.tide_team_journal.assign.success", new Object[]{target.getGameProfile().getName()}), false);
      } else {
         reportClaim(context, result, largest);
      }

      return result == TeamJournalService.ClaimResult.CLAIMED ? 1 : 0;
   }

   private static void reportClaim(CommandContext<ServerCommandSource> context, TeamJournalService.ClaimResult result, boolean largest) {
      switch (result) {
         case CLAIMED:
            ((ServerCommandSource)context.getSource())
               .sendFeedback(
                  () -> Text.translatable(
                     "command.tide_team_journal.claim.success",
                     new Object[]{Text.translatable(largest ? "command.tide_team_journal.claim.largest" : "command.tide_team_journal.claim.smallest")}
                  ),
                  false
               );
            break;
         case NOT_IN_PARTY:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.merge.not_in_party"));
            break;
         case INVALID_FISH:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.claim.invalid_fish"));
            break;
         case INVALID_PROOF:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.claim.invalid_proof"));
            break;
         case INVALID_TARGET:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.claim.invalid_target"));
            break;
         case NOT_AUTHORIZED:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.claim.not_authorized"));
            break;
         case FAILED:
            ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.claim.failed"));
      }
   }

   private static int claimAllRecords(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      TeamJournalService.ClaimAllResult result = TeamJournalService.claimAllRecords(((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      if (result.result() == TeamJournalService.ClaimResult.CLAIMED) {
         ((ServerCommandSource)context.getSource())
            .sendFeedback(() -> Text.translatable("command.tide_team_journal.claimall.success", new Object[]{result.fishCount()}), false);
      } else {
         reportClaim(context, result.result(), true);
      }

      return result.result() == TeamJournalService.ClaimResult.CLAIMED ? result.fishCount() : 0;
   }

   private static int claimAllRecordsFor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "member");
      TeamJournalService.ClaimAllResult result = TeamJournalService.claimAllRecords(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), target);
      if (result.result() == TeamJournalService.ClaimResult.CLAIMED) {
         ((ServerCommandSource)context.getSource())
            .sendFeedback(
               () -> Text.translatable(
                  "command.tide_team_journal.claimall.target_success", new Object[]{result.fishCount(), target.getGameProfile().getName()}
               ),
               false
            );
      } else {
         reportClaim(context, result.result(), true);
      }

      return result.result() == TeamJournalService.ClaimResult.CLAIMED ? result.fishCount() : 0;
   }

   private static int recordStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      TeamJournalService.RecordStatus status = TeamJournalService.getHeldRecordStatus(((ServerCommandSource)context.getSource()).getPlayerOrThrow());
      if (status.result() != TeamJournalService.ClaimResult.CLAIMED) {
         reportClaim(context, status.result(), true);
         return 0;
      } else {
         ((ServerCommandSource)context.getSource())
            .sendFeedback(
               () -> Text.translatable(
                  "command.tide_team_journal.status",
                  new Object[]{
                     status.largest().isBlank() ? Text.translatable("command.tide_team_journal.none") : status.largest(),
                     status.smallest().isBlank() ? Text.translatable("command.tide_team_journal.none") : status.smallest()
                  }
               ),
               false
            );
         return 1;
      }
   }

   private static int leaderboard(CommandContext<ServerCommandSource> context, String metric) throws CommandSyntaxException {
      return leaderboard(context, metric, 0);
   }

   private static int leaderboard(CommandContext<ServerCommandSource> context, String metric, int page) throws CommandSyntaxException {
      if (!List.of("catches", "species", "record_events", "active_records", "fish_score").contains(metric)) {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.metric.invalid", new Object[]{metric}));
         return 0;
      }

      if (!ServerConfig.get().visibleMetrics.contains(metric)) {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.feature_disabled"));
         return 0;
      }

      NbtCompound data = TeamJournalService.teamData(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), 0, metric, "");
      if (!data.getBoolean("leaderboard_enabled")) {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.feature_disabled"));
         return 0;
      }

      ((ServerCommandSource)context.getSource())
         .sendFeedback(
            () -> Text.translatable(
               "command.tide_team_journal.leaderboard.title",
               new Object[]{data.getString("team_name"), Text.translatable("metric.tide_team_journal." + metric)}
            ),
            false
         );
      int rank = page * 10 + 1;
      int skipped = 0;
      int shown = 0;

      for (NbtElement raw : data.getList("contributors", 10)) {
         if (skipped++ >= page * 10) {
            if (shown++ >= 10) {
               break;
            }

            NbtCompound entry = (NbtCompound)raw;
            int place;
            int var12 = place = rank++;
            ((ServerCommandSource)context.getSource())
               .sendFeedback(
                  () -> Text.translatable(
                     "command.tide_team_journal.leaderboard.row",
                     new Object[]{
                        place,
                        entry.getString("name"),
                        entry.getInt(metric),
                        entry.getBoolean("former") ? Text.translatable("screen.tide_team_journal.former_suffix") : ""
                     }
                  ),
                  false
               );
         }
      }

      return shown;
   }

   private static int history(CommandContext<ServerCommandSource> context, int page, String fish) throws CommandSyntaxException {
      NbtCompound data = TeamJournalService.teamData(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), page, "catches", fish);
      if (!data.getBoolean("history_enabled")) {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.feature_disabled"));
         return 0;
      }

      ((ServerCommandSource)context.getSource())
         .sendFeedback(
            () -> Text.translatable("command.tide_team_journal.history.title", new Object[]{data.getInt("page") + 1, data.getInt("pages")}),
            false
         );
      int count = 0;

      for (NbtElement raw : data.getList("history", 10)) {
         TeamProgressStore.RecordEvent event = TeamProgressStore.RecordEvent.fromTag((NbtCompound)raw);
         if (event != null) {
            Identifier fishId = Identifier.tryParse(event.fish());
            Text fishName = (Text)(fishId == null
               ? Text.literal(event.fish())
               : ((Item)Registries.ITEM.get(fishId)).getName());
            ((ServerCommandSource)context.getSource())
               .sendFeedback(
                  () -> Text.translatable(
                     "command.tide_team_journal.history.row",
                     new Object[]{
                        event.targetName(),
                        Text.translatable("event.tide_team_journal." + event.type().name().toLowerCase()),
                        fishName,
                        TideUtils.getFormattedLength(event.newSize()),
                        Instant.ofEpochMilli(event.timestamp()).toString()
                     }
                  ),
                  false
               );
            count++;
         }
      }

      return count;
   }

   private static int member(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      String name = StringArgumentType.getString(context, "name");
      NbtCompound data = TeamJournalService.teamData(((ServerCommandSource)context.getSource()).getPlayerOrThrow(), 0, "catches", "");
      List<NbtCompound> matches = new ArrayList<>();

      for (NbtElement raw : data.getList("contributors", 10)) {
         NbtCompound entry = (NbtCompound)raw;
         if (entry.getString("name").equalsIgnoreCase(name) || entry.getUuid("id").toString().equalsIgnoreCase(name)) {
            matches.add(entry);
         }
      }

      if (matches.size() > 1) {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.member.ambiguous", new Object[]{name}));
         return 0;
      } else if (matches.size() == 1) {
         NbtCompound entry = matches.getFirst();
         ((ServerCommandSource)context.getSource())
            .sendFeedback(
               () -> Text.translatable(
                  "command.tide_team_journal.member",
                  new Object[]{
                     entry.getString("name"),
                     entry.getInt("catches"),
                     entry.getInt("species"),
                     entry.getInt("record_events"),
                     entry.getInt("active_records")
                  }
               ),
               false
            );
         return 1;
      } else {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.member.not_found", new Object[]{name}));
         return 0;
      }
   }

   private static int reloadConfig(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      if (TeamJournalService.reloadServerConfig(((ServerCommandSource)context.getSource()).getServer())) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("command.tide_team_journal.config.reloaded"), true);
         return 1;
      } else {
         ((ServerCommandSource)context.getSource()).sendError(Text.translatable("command.tide_team_journal.config.failed"));
         return 0;
      }
   }
}
