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

   private static void register(CommandDispatcher<ServerCommandSource> var0) {
      CommandNode var1 = var0.getRoot().getChild("tideborne_internal_traits");
      CommandNode var2 = var0.getRoot().getChild("tideborne_internal_team");
      CommandNode var3 = var0.getRoot().getChild("tideborne_internal_fishing");
      LiteralArgumentBuilder var4 = (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(
                           "tideborne"
                        )
                        .executes(var0x -> help((ServerCommandSource)var0x.getSource())))
                     .then(LiteralArgumentBuilder.literal("help").executes(var0x -> help((ServerCommandSource)var0x.getSource()))))
                  .then(LiteralArgumentBuilder.literal("status").executes(var0x -> status((ServerCommandSource)var0x.getSource()))))
               .then(
                  ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("reload").requires(var0x -> var0x.hasPermissionLevel(2)))
                     .executes(var0x -> reload((ServerCommandSource)var0x.getSource()))
               ))
            .then(
               LiteralArgumentBuilder.literal("migrate")
                  .then(LiteralArgumentBuilder.literal("status").executes(var0x -> migrationStatus((ServerCommandSource)var0x.getSource())))
            ))
         .then(
            LiteralArgumentBuilder.literal("debug")
               .then(LiteralArgumentBuilder.literal("backend").executes(var0x -> debugBackend((ServerCommandSource)var0x.getSource())))
         );
      redirect(var4, "traits", var1);
      redirect(var4, "journal", var2);
      redirect(var4, "team", var2);
      redirect(var4, "fishing", var3);
      var4.then(LiteralArgumentBuilder.literal("badges").then(LiteralArgumentBuilder.literal("backfillhistory").executes(HistoryBadgeCommand.INSTANCE)));
      var0.register(var4);
      alias(var0, "tidetraits", var1);
      alias(var0, "tideteamjournal", var2);
      alias(var0, "tideboundcompat", var3);
   }

   private static void redirect(LiteralArgumentBuilder<ServerCommandSource> var0, String var1, CommandNode<ServerCommandSource> var2) {
      if (var2 != null) {
         var0.then(LiteralArgumentBuilder.literal(var1).redirect(var2));
      }
   }

   private static void alias(CommandDispatcher<ServerCommandSource> var0, String var1, CommandNode<ServerCommandSource> var2) {
      if (var2 != null) {
         var0.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(var1).redirect(var2));
      }
   }

   private static int help(ServerCommandSource var0) {
      send(var0, "Tideborne commands (press Tab at any point for valid choices):");
      send(var0, "/tideborne traits <inspect|setmutation|clearmutation|percentile|dumpfish>");
      send(var0, "/tideborne journal <open|status|merge|claim|assign|claimall|leaderboard|history|member>");
      send(var0, "/tideborne fishing <status|reload>");
      send(var0, "/tideborne status  |  /tideborne reload  |  /tideborne migrate status");
      send(var0, "/tideborne badges backfillhistory  - backfill per-world discovery badges from retained event history");
      return 1;
   }

   private static int status(ServerCommandSource var0) {
      send(var0, "Tideborne 1.3.13 \u2014 unified commands active.");
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
