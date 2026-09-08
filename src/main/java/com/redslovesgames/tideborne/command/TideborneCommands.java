/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.redslovesgames.tideborne.command.debug.FishingGearRegistryDebugCommand;
import com.redslovesgames.tideborne.command.debug.SpecimenDebugCommand;
import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class TideborneCommands {
   private static boolean initialized;

   private TideborneCommands() {
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
      }
   }

   private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      CommandNode<ServerCommandSource> traits = dispatcher.getRoot().getChild("tideborne_internal_traits");
      CommandNode<ServerCommandSource> journal = dispatcher.getRoot().getChild("tideborne_internal_team");
      CommandNode<ServerCommandSource> fishing = dispatcher.getRoot().getChild("tideborne_internal_fishing");

      LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("tideborne")
         .executes(context -> TideborneCommandUi.showRoot(context.getSource()))
         .then(CommandManager.literal("status").executes(context -> status(context.getSource())))
         .then(
            CommandManager.literal("reload")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> reload(context.getSource()))
         );

      redirect(root, "journal", journal);

      LiteralArgumentBuilder<ServerCommandSource> debug = CommandManager.literal("debug")
         .requires(source -> source.hasPermissionLevel(2))
         .executes(context -> TideborneCommandUi.showDebug(context.getSource()));

      LiteralArgumentBuilder<ServerCommandSource> fishingDebug = CommandManager.literal("fishing")
         .executes(context -> TideborneCommandUi.showFishingDebug(context.getSource()));
      redirect(fishingDebug, "inspect", child(fishing, "inspect"));
      redirect(fishingDebug, "reproduce", child(fishing, "reproduce"));
      debug.then(fishingDebug);

      LiteralArgumentBuilder<ServerCommandSource> specimen = CommandManager.literal("specimen")
         .executes(context -> TideborneCommandUi.showSpecimenDebug(context.getSource()));
      redirect(specimen, "inspect", child(traits, "inspect"));
      redirect(specimen, "reroll", child(fishing, "reroll"));

      LiteralArgumentBuilder<ServerCommandSource> set = CommandManager.literal("set")
         .then(
            CommandManager.literal("percentile")
               .then(
                  CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 100.0))
                     .executes(context -> SpecimenDebugCommand.setPercentile(
                        context.getSource(),
                        DoubleArgumentType.getDouble(context, "value")
                     ))
               )
         )
         .then(
            CommandManager.literal("body")
               .then(CommandManager.literal("normal")
                  .executes(context -> SpecimenDebugCommand.setBodyType(context.getSource(), SpecimenData.BodyType.NORMAL)))
               .then(CommandManager.literal("giant")
                  .executes(context -> SpecimenDebugCommand.setBodyType(context.getSource(), SpecimenData.BodyType.GIANT)))
               .then(CommandManager.literal("dwarf")
                  .executes(context -> SpecimenDebugCommand.setBodyType(context.getSource(), SpecimenData.BodyType.DWARF)))
         )
         .then(
            CommandManager.literal("condition")
               .then(CommandManager.literal("normal")
                  .executes(context -> SpecimenDebugCommand.setCondition(context.getSource(), SpecimenData.Condition.NORMAL)))
               .then(CommandManager.literal("scarred")
                  .executes(context -> SpecimenDebugCommand.setCondition(context.getSource(), SpecimenData.Condition.SCARRED)))
               .then(CommandManager.literal("parasite_ridden")
                  .executes(context -> SpecimenDebugCommand.setCondition(context.getSource(), SpecimenData.Condition.PARASITE_RIDDEN)))
         )
         .then(
            CommandManager.literal("pigmentation")
               .then(CommandManager.literal("normal")
                  .executes(context -> SpecimenDebugCommand.setPigmentation(context.getSource(), SpecimenData.Pigmentation.NORMAL)))
               .then(CommandManager.literal("albino")
                  .executes(context -> SpecimenDebugCommand.setPigmentation(context.getSource(), SpecimenData.Pigmentation.ALBINO)))
               .then(CommandManager.literal("iridescent")
                  .executes(context -> SpecimenDebugCommand.setPigmentation(context.getSource(), SpecimenData.Pigmentation.IRIDESCENT)))
         );
      specimen.then(set);
      specimen.then(CommandManager.literal("body").executes(context -> TideborneCommandUi.showBodyTypes(context.getSource())));
      specimen.then(CommandManager.literal("condition").executes(context -> TideborneCommandUi.showConditions(context.getSource())));
      specimen.then(CommandManager.literal("pigmentation").executes(context -> TideborneCommandUi.showPigmentations(context.getSource())));
      debug.then(specimen);

      redirect(debug, "registry", child(traits, "dumpfish"));
      debug.then(CommandManager.literal("gear").executes(context -> FishingGearRegistryDebugCommand.run(context.getSource())));
      root.then(debug);

      // Hidden compatibility route. This is intentionally omitted from the clickable UI.
      CommandNode<ServerCommandSource> repairInventory = child(child(fishing, "repair"), "inventory");
      if (repairInventory != null) {
         root.then(
            CommandManager.literal("fishing")
               .requires(source -> source.hasPermissionLevel(2))
               .then(
                  CommandManager.literal("repair")
                     .then(executableRedirect("inventory", repairInventory))
               )
         );
      }

      dispatcher.register(root);
   }

   private static CommandNode<ServerCommandSource> child(CommandNode<ServerCommandSource> parent, String name) {
      return parent == null ? null : parent.getChild(name);
   }

   static void redirect(
      LiteralArgumentBuilder<ServerCommandSource> root,
      String name,
      CommandNode<ServerCommandSource> target
   ) {
      if (target != null) {
         root.then(executableRedirect(name, target));
      }
   }

   /**
    * Brigadier redirects continue parsing at the target node, but they do not inherit the target
    * node's executable command when the alias itself is the end of the input. Copy the target
    * command and requirement onto the alias so commands such as `/tideborne journal` and
    * `/tideborne debug specimen inspect` remain executable while deeper arguments still redirect.
    */
   static LiteralArgumentBuilder<ServerCommandSource> executableRedirect(
      String name,
      CommandNode<ServerCommandSource> target
   ) {
      LiteralArgumentBuilder<ServerCommandSource> alias = CommandManager.literal(name)
         .requires(target.getRequirement());
      if (target.getCommand() != null) {
         alias.executes(target.getCommand());
      }
      return alias.redirect(target);
   }

   private static int status(ServerCommandSource source) {
      send(source, "Tideborne 2.0.0 - Fishing System 2.0 active.");
      send(source, "Use /tideborne for the clickable command menu.");
      return 1;
   }

   private static int reload(ServerCommandSource source) {
      send(source, TideborneConfigBackend.reloadServerSide());
      return 1;
   }

   private static void send(ServerCommandSource source, String message) {
      source.sendFeedback(() -> Text.literal(message), false);
   }
}
