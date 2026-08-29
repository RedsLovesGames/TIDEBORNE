/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.migration.TideborneMigrationManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class TideborneCommands {
   private static boolean initialized;

   private TideborneCommands() {
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(var0, var1, var2) -> register(var0));
      }
   }

   private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      CommandNode<ServerCommandSource> traits = dispatcher.getRoot().getChild("tideborne_internal_traits");
      CommandNode<ServerCommandSource> journal = dispatcher.getRoot().getChild("tideborne_internal_team");
      CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");

      LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.<ServerCommandSource>literal("tideborne")
         .executes(context -> help(context.getSource()))
         .then(LiteralArgumentBuilder.<ServerCommandSource>literal("help").executes(context -> help(context.getSource())))
         .then(LiteralArgumentBuilder.<ServerCommandSource>literal("status").executes(context -> status(context.getSource())))
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> reload(context.getSource()))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("migrate")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("status").executes(context -> migrationStatus(context.getSource())))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("debug")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("backend").executes(context -> debugBackend(context.getSource())))
         )
         .then(
            LiteralArgumentBuilder.<ServerCommandSource>literal("badges")
               .then(LiteralArgumentBuilder.<ServerCommandSource>literal("backfillhistory").executes(HistoryBadgeCommand.INSTANCE))
         );

      redirect(root, "traits", traits);
      redirect(root, "journal", journal);
      redirect(root, "team", journal);
      redirect(root, "fishing", fishing);
      dispatcher.register(root);
      alias(dispatcher, "tidetraits", traits);
      alias(dispatcher, "tideteamjournal", journal);
      alias(dispatcher, "tideboundcompat", fishing);
   }

   private static void redirect(LiteralArgumentBuilder<ServerCommandSource> root, String name, CommandNode<ServerCommandSource> target) {
      if (target != null) {
         root.then(LiteralArgumentBuilder.<ServerCommandSource>literal(name).redirect(target));
      }
   }

   private static void alias(CommandDispatcher<ServerCommandSource> dispatcher, String name, CommandNode<ServerCommandSource> target) {
      if (target != null) {
         dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal(name).redirect(target));
      }
   }

   private static int help(ServerCommandSource var0) {
      send(var0, "Tideborne commands (press Tab at any point for valid choices):");
      send(var0, "/tideborne traits <inspect|setmutation|clearmutation|percentile|dumpfish>");
      send(var0, "/tideborne journal <open|status|merge|claim|assign|claimall|leaderboard|history|member>");
      send(var0, "/tideborne fishing <status|inspect|reload>");
      send(var0, "/tideborne status  |  /tideborne reload  |  /tideborne migrate status");
      send(var0, "/tideborne badges backfillhistory  - backfill per-world discovery badges from retained event history");
      return 1;
   }

   private static int status(ServerCommandSource var0) {
      send(var0, "Tideborne 1.3.13 — unified commands active.");
      send(var0, TideborneMigrationManager.status());
      send(var0, "Use /tideborne help for grouped commands, or press Tab to explore a group.");
      return 1;
   }

   private static int reload(ServerCommandSource var0) {
      send(var0, TideborneConfigBackend.reloadServerSide());
      return 1;
   }

   private static int migrationStatus(ServerCommandSource var0) {
      send(var0, TideborneMigrationManager.status());
      send(var0, "Legacy Tideborne data namespaces remain preserved for world compatibility.");
      return 1;
   }

   private static int debugBackend(ServerCommandSource var0) {
      send(var0, "Tideborne backend facade: active.");
      return 1;
   }

   private static void send(ServerCommandSource var0, String var1) {
      var0.sendFeedback(() -> Text.literal(var1), false);
   }
}
