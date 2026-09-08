/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.discovery;

import java.util.Objects;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DiscoveryManager {
   public static final String PERSISTENT_DATA_KEY = "TideTraitsPlayerData";
   public static final int DATA_VERSION = 1;
   private static boolean payloadRegistered;
   private static boolean serverInitialized;

   private DiscoveryManager() {
   }

   public static synchronized void initServer() {
      ensurePayloadRegistered();
      if (!serverInitialized) {
         ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> sync(handler.getPlayer()));
         ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> sync(newPlayer));
         serverInitialized = true;
      }
   }

   public static DiscoverySnapshot snapshot(PlayerEntity player) {
      return DiscoveryNbt.snapshot(Objects.requireNonNull(player, "player"));
   }

   public static boolean discoverMutation(PlayerEntity player, Identifier speciesId, Identifier mutationId) {
      return DiscoveryNbt.addMutation(
         Objects.requireNonNull(player, "player"), Objects.requireNonNull(speciesId, "speciesId"), Objects.requireNonNull(mutationId, "mutationId")
      );
   }

   public static boolean discoverSizeBand(PlayerEntity player, Identifier speciesId, Identifier sizeBandId) {
      return DiscoveryNbt.addSizeBand(
         Objects.requireNonNull(player, "player"), Objects.requireNonNull(speciesId, "speciesId"), Objects.requireNonNull(sizeBandId, "sizeBandId")
      );
   }

   public static boolean discoverMutationAndSync(ServerPlayerEntity player, Identifier speciesId, Identifier mutationId) {
      boolean changed = discoverMutation(player, speciesId, mutationId);
      if (changed) {
         sync(player);
      }

      return changed;
   }

   public static boolean discoverSizeBandAndSync(ServerPlayerEntity player, Identifier speciesId, Identifier sizeBandId) {
      boolean changed = discoverSizeBand(player, speciesId, sizeBandId);
      if (changed) {
         sync(player);
      }

      return changed;
   }

   public static boolean sync(ServerPlayerEntity player) {
      Objects.requireNonNull(player, "player");
      ensurePayloadRegistered();
      if (!ServerPlayNetworking.canSend(player, DiscoverySyncPayload.TYPE)) {
         return false;
      }

      ServerPlayNetworking.send(player, new DiscoverySyncPayload(snapshot(player)));
      return true;
   }

   static synchronized void ensurePayloadRegistered() {
      if (!payloadRegistered) {
         PayloadTypeRegistry.playS2C().register(DiscoverySyncPayload.TYPE, DiscoverySyncPayload.STREAM_CODEC);
         payloadRegistered = true;
      }
   }
}
