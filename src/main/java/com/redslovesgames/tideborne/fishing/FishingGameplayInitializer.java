/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.redslovesgames.tideborne.command.FishingInspectCommand;
import com.redslovesgames.tideborne.command.FishingRecoveryCommand;
import com.redslovesgames.tideborne.command.FishingReproduceCommand;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.ecosystem.SharkScentManager;
import com.redslovesgames.tideborne.network.SharkCatchLossPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsResultPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsUpdatePayload;
import com.redslovesgames.tideborne.registry.TideboundEntities;
import com.redslovesgames.tideborne.registry.TideboundItems;
import java.lang.reflect.InvocationTargetException;
import java.util.OptionalDouble;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FishingGameplayInitializer {
   public static final String MOD_ID = "tidebound_compatibility";
   public static final Logger LOGGER = LoggerFactory.getLogger("tidebound_compatibility");
   private static final String APEX_COMPAT_CLASS = "com.redslovesgames.tideborne.compat.apex.ApexCompat";
   private static boolean mythsEnabledAtStartup;
   private static boolean apexEnabledAtStartup;
   private static boolean initialized;

   private FishingGameplayInitializer() {
   }

   public static void initialize() {
      if (initialized) {
         return;
      }
      initialized = true;
      TideboundConfig.Result initialLoad = TideboundConfig.load();
      if (!initialLoad.success()) {
         LOGGER.error("{}", initialLoad.message());
      }

      mythsEnabledAtStartup = isMythsLoaded() && TideboundConfig.get().enableMythsCompat;
      apexEnabledAtStartup = isApexLoaded() && TideboundConfig.get().enableApexCompat;
      PayloadTypeRegistry.playS2C().register(TideboundSettingsPayload.TYPE, TideboundSettingsPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(TideboundSettingsUpdatePayload.TYPE, TideboundSettingsUpdatePayload.CODEC);
      PayloadTypeRegistry.playS2C().register(TideboundSettingsResultPayload.TYPE, TideboundSettingsResultPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(SharkCatchLossPayload.TYPE, SharkCatchLossPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(TideboundSettingsUpdatePayload.TYPE, (payload, context) -> {
         if (!context.player().hasPermissionLevel(2)) {
            LOGGER.warn("Rejected Tidebound config update from non-operator {}", context.player().getGameProfile().getName());
            sendSettingsResult(context.player(), false, "Only server operators can save gameplay settings.");
         } else {
            TideboundConfig.Result result = TideboundConfig.applyBalanceJson(payload.json());
            if (result.success()) {
               context.server().getPlayerManager().getPlayerList().forEach(FishingGameplayInitializer::syncSettings);
            }

            sendSettingsResult(context.player(), result.success(), result.message());
         }
      });
      TideboundItems.register();
      TideboundEntities.register();
      ServerTickEvents.END_WORLD_TICK.register(SharkScentManager::tick);
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> syncSettings(handler.player));
      if (mythsEnabledAtStartup) {
         LOGGER.info("Enabled Myths of the Sea integration");
      }

      if (apexEnabledAtStartup) {
         initializeApexIntegration();
         LOGGER.info("Enabled Apex Waters integration");
      }

      LOGGER.info("[Tideborne] Fishing compatibility initialized (Myths: {}, Apex: {})", isMythsLoaded(), isApexLoaded());
   }

   private static void initializeApexIntegration() {
      try {
         Class<?> compatibility = Class.forName(APEX_COMPAT_CLASS, true, FishingGameplayInitializer.class.getClassLoader());
         compatibility.getMethod("initialize").invoke(null);
      } catch (InvocationTargetException exception) {
         Throwable cause = exception.getCause();
         if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
         }
         if (cause instanceof Error error) {
            throw error;
         }
         throw new IllegalStateException("Could not initialize Apex Waters integration", cause);
      } catch (ReflectiveOperationException exception) {
         throw new IllegalStateException("Could not initialize Apex Waters integration", exception);
      }
   }

   public static Identifier id(String path) {
      return Identifier.of("tidebound_compatibility", path);
   }

   public static boolean isMythsLoaded() {
      return FabricLoader.getInstance().isModLoaded("myths_of_the_sea");
   }

   public static boolean isApexLoaded() {
      return FabricLoader.getInstance().isModLoaded("apexwaters");
   }

   public static boolean isMythsIntegrationActive() {
      return mythsEnabledAtStartup;
   }

   public static boolean isApexIntegrationActive() {
      return apexEnabledAtStartup;
   }

   public static void syncSettings(ServerPlayerEntity player) {
      if (ServerPlayNetworking.canSend(player, TideboundSettingsPayload.TYPE)) {
         ServerPlayNetworking.send(player, TideboundSettingsPayload.fromServer());
      }
   }

   private static void sendSettingsResult(ServerPlayerEntity player, boolean success, String message) {
      if (ServerPlayNetworking.canSend(player, TideboundSettingsResultPayload.TYPE)) {
         ServerPlayNetworking.send(player, new TideboundSettingsResultPayload(success, message));
      }
   }

   static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("tideborne_internal_fishing")
         .executes(context -> status(context.getSource()))
         .then(CommandManager.literal("status").executes(context -> status(context.getSource())))
         .then(
            CommandManager.literal("inspect")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> FishingInspectCommand.run(context.getSource()))
         )
         .then(reproduceCommand())
         .then(FishingRecoveryCommand.repairCommand())
         .then(FishingRecoveryCommand.rerollCommand())
         .then(
            CommandManager.literal("reload")
               .requires(source -> source.hasPermissionLevel(2))
               .executes(context -> {
                  TideboundConfig.Result result = TideboundConfig.reloadBalance();
                  if (!result.success()) {
                     context.getSource().sendError(Text.literal(result.message()));
                     return 0;
                  }
                  context.getSource().getServer().getPlayerManager().getPlayerList().forEach(FishingGameplayInitializer::syncSettings);
                  context.getSource().sendFeedback(() -> Text.translatable("command.tidebound_compatibility.reload"), true);
                  return 1;
               })
         );
      dispatcher.register(root);
   }

   private static LiteralArgumentBuilder<ServerCommandSource> reproduceCommand() {
      return CommandManager.literal("reproduce")
         .requires(source -> source.hasPermissionLevel(2))
         .then(
            CommandManager.argument("species", StringArgumentType.word())
               .then(
                  CommandManager.argument("seed", LongArgumentType.longArg())
                     .executes(context -> FishingReproduceCommand.run(
                        context.getSource(),
                        StringArgumentType.getString(context, "species"),
                        LongArgumentType.getLong(context, "seed"),
                        0.0,
                        0.0,
                        false,
                        OptionalDouble.empty()
                     ))
                     .then(
                        CommandManager.argument("fishingLuck", DoubleArgumentType.doubleArg())
                           .then(
                              CommandManager.argument("traitLuck", DoubleArgumentType.doubleArg())
                                 .executes(context -> FishingReproduceCommand.run(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "species"),
                                    LongArgumentType.getLong(context, "seed"),
                                    DoubleArgumentType.getDouble(context, "fishingLuck"),
                                    DoubleArgumentType.getDouble(context, "traitLuck"),
                                    false,
                                    OptionalDouble.empty()
                                 ))
                                 .then(
                                    CommandManager.argument("perfectCatch", BoolArgumentType.bool())
                                       .executes(context -> FishingReproduceCommand.run(
                                          context.getSource(),
                                          StringArgumentType.getString(context, "species"),
                                          LongArgumentType.getLong(context, "seed"),
                                          DoubleArgumentType.getDouble(context, "fishingLuck"),
                                          DoubleArgumentType.getDouble(context, "traitLuck"),
                                          BoolArgumentType.getBool(context, "perfectCatch"),
                                          OptionalDouble.empty()
                                       ))
                                       .then(
                                          CommandManager.argument(
                                             "forcedPercentile",
                                             DoubleArgumentType.doubleArg(0.0, 99.999999999)
                                          ).executes(context -> FishingReproduceCommand.run(
                                             context.getSource(),
                                             StringArgumentType.getString(context, "species"),
                                             LongArgumentType.getLong(context, "seed"),
                                             DoubleArgumentType.getDouble(context, "fishingLuck"),
                                             DoubleArgumentType.getDouble(context, "traitLuck"),
                                             BoolArgumentType.getBool(context, "perfectCatch"),
                                             OptionalDouble.of(DoubleArgumentType.getDouble(context, "forcedPercentile"))
                                          ))
                                       )
                                 )
                           )
                     )
               )
         );
   }

   private static int status(ServerCommandSource source) {
      source.sendFeedback(
         () -> Text.translatable(
            "command.tidebound_compatibility.status", new Object[]{isMythsIntegrationActive(), isApexIntegrationActive(), SharkScentManager.activeZoneCount()}
         ),
         false
      );
      return 1;
   }
}
