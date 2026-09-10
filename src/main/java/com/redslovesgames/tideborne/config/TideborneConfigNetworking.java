package com.redslovesgames.tideborne.config;

import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import com.redslovesgames.tideborne.network.TideboundSettingsPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsResultPayload;
import com.redslovesgames.tideborne.network.TideboundSettingsUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Owns Tideborne configuration synchronization and server-authoritative updates. */
public final class TideborneConfigNetworking {
   private static boolean initialized;

   private TideborneConfigNetworking() {
   }

   public static void initializeServer() {
      if (initialized) return;
      initialized = true;

      PayloadTypeRegistry.playS2C().register(TideboundSettingsPayload.TYPE, TideboundSettingsPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(TideboundSettingsUpdatePayload.TYPE, TideboundSettingsUpdatePayload.CODEC);
      PayloadTypeRegistry.playS2C().register(TideboundSettingsResultPayload.TYPE, TideboundSettingsResultPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(TideboundSettingsUpdatePayload.TYPE, (payload, context) -> {
         if (!context.player().hasPermissionLevel(2)) {
            FishingGameplayInitializer.LOGGER.warn(
               "Rejected Tideborne config update from non-operator {}",
               context.player().getGameProfile().getName()
            );
            sendResult(context.player(), false, "Only server operators can save gameplay settings.");
            return;
         }

         TideboundConfig.Result result = TideboundConfig.applyBalanceJson(payload.json());
         if (result.success()) {
            context.server().getPlayerManager().getPlayerList().forEach(TideborneConfigNetworking::syncSettings);
         }
         sendResult(context.player(), result.success(), result.message());
      });
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncSettings(handler.player));
   }

   public static void syncSettings(ServerPlayerEntity player) {
      if (ServerPlayNetworking.canSend(player, TideboundSettingsPayload.TYPE)) {
         ServerPlayNetworking.send(player, TideboundSettingsPayload.fromServer());
      }
   }

   private static void sendResult(ServerPlayerEntity player, boolean success, String message) {
      if (ServerPlayNetworking.canSend(player, TideboundSettingsResultPayload.TYPE)) {
         ServerPlayNetworking.send(player, new TideboundSettingsResultPayload(success, message));
      }
   }
}
