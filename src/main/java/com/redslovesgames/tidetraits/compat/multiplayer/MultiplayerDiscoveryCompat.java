/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import com.redslovesgames.tidetraits.TideTraits;
import com.redslovesgames.tidetraits.discovery.DiscoveryManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MultiplayerDiscoveryCompat {
   public static final String TIDE_TEAM_JOURNAL_MOD_ID = "tideborne";
   public static final String FTB_TEAMS_MOD_ID = "ftbteams";
   private static final Identifier NORMAL_MUTATION = Identifier.of("tide_traits", "normal");
   private static final WarningThrottle WARNINGS = new WarningThrottle();
   private static final long REQUEST_COOLDOWN_TICKS = 10L;
   private static final ConcurrentHashMap<UUID, Long> LAST_REQUEST_TICK = new ConcurrentHashMap<>();
   private static volatile ReflectiveFtbTeamsBridge bridge;
   private static volatile SharedDiscoveryAvailability bridgeAvailability = SharedDiscoveryAvailability.UNKNOWN;
   private static boolean payloadsRegistered;
   private static boolean serverInitialized;

   private MultiplayerDiscoveryCompat() {
   }

   public static synchronized void initServer() {
      ensurePayloadsRegistered();
      ensureBridgeInitialized();
      if (!serverInitialized) {
         ServerPlayNetworking.registerGlobalReceiver(SharedDiscoveryRequestPayload.TYPE, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (payload.protocolVersion() == 1 && availability(player).isAvailable() && allowRefreshRequest(player)) {
               sync(player);
            }
         });
         ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            LAST_REQUEST_TICK.remove(player.getUuid());
            sync(player);
         });
         ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> LAST_REQUEST_TICK.remove(handler.getPlayer().getUuid()));
         ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> {
            LAST_REQUEST_TICK.remove(newPlayer.getUuid());
            sync(newPlayer);
         });
         serverInitialized = true;
      }
   }

   public static SharedDiscoveryAvailability availability() {
      ensureBridgeInitialized();
      return bridgeAvailability;
   }

   public static SharedDiscoveryAvailability availability(ServerPlayerEntity player) {
      if (player != null && availability() == SharedDiscoveryAvailability.AVAILABLE) {
         try {
            return bridge.effectiveTeam(player).isPresent() ? SharedDiscoveryAvailability.AVAILABLE : SharedDiscoveryAvailability.TEAM_UNRESOLVED;
         } catch (RuntimeException | LinkageError failure) {
            warnFailure(player, "availability", failure);
            return SharedDiscoveryAvailability.ERROR;
         }
      } else {
         return player == null ? SharedDiscoveryAvailability.TEAM_UNRESOLVED : availability();
      }
   }

   public static boolean isAvailable() {
      return availability().isAvailable();
   }

   public static boolean isAvailable(ServerPlayerEntity player) {
      return availability(player).isAvailable();
   }

   public static boolean recordCatch(ServerPlayerEntity player, Identifier canonicalSpeciesId, Identifier mutationId, Identifier sizeBandId) {
      if (player != null && canonicalSpeciesId != null && (mutationId != null || sizeBandId != null)) {
         Identifier discoverableMutation = NORMAL_MUTATION.equals(mutationId) ? null : mutationId;
         boolean personalChanged = recordPersonal(player, canonicalSpeciesId, discoverableMutation, sizeBandId);
         ensureBridgeInitialized();
         if (bridgeAvailability == SharedDiscoveryAvailability.AVAILABLE && bridge != null) {
            try {
               Optional<Object> effectiveTeam = bridge.effectiveTeam(player);
               if (effectiveTeam.isEmpty()) {
                  warnMissingTeam(player);
                  send(player, SharedDiscoverySnapshot.unavailable(SharedDiscoveryAvailability.TEAM_UNRESOLVED));
                  return personalChanged;
               }

               Object team = effectiveTeam.get();
               NbtCompound extraData = bridge.extraData(team);
               boolean sharedChanged = SharedDiscoveryNbt.add(extraData, canonicalSpeciesId, discoverableMutation, sizeBandId);
               if (sharedChanged) {
                  bridge.markDirty(team);
               }

               SharedDiscoverySnapshot snapshot = SharedDiscoverySnapshot.available(SharedDiscoveryNbt.snapshot(extraData));
               broadcast(team, player, snapshot);
               return personalChanged || sharedChanged;
            } catch (RuntimeException | LinkageError failure) {
               warnFailure(player, "record catch", failure);
               send(player, SharedDiscoverySnapshot.unavailable(SharedDiscoveryAvailability.ERROR));
               return personalChanged;
            }
         } else {
            return personalChanged;
         }
      } else {
         if (WARNINGS.once("invalid-record-catch")) {
            TideTraits.LOGGER.warn("Skipped malformed Tide Traits discovery record request");
         }

         return false;
      }
   }

   public static SharedDiscoverySnapshot snapshot(ServerPlayerEntity player) {
      if (player == null) {
         return SharedDiscoverySnapshot.unavailable(SharedDiscoveryAvailability.TEAM_UNRESOLVED);
      }

      ensureBridgeInitialized();
      if (bridgeAvailability == SharedDiscoveryAvailability.AVAILABLE && bridge != null) {
         try {
            Optional<Object> team = bridge.effectiveTeam(player);
            if (team.isEmpty()) {
               warnMissingTeam(player);
               return SharedDiscoverySnapshot.unavailable(SharedDiscoveryAvailability.TEAM_UNRESOLVED);
            } else {
               return SharedDiscoverySnapshot.available(SharedDiscoveryNbt.snapshot(bridge.extraData(team.get())));
            }
         } catch (RuntimeException | LinkageError failure) {
            warnFailure(player, "read snapshot", failure);
            return SharedDiscoverySnapshot.unavailable(SharedDiscoveryAvailability.ERROR);
         }
      } else {
         return SharedDiscoverySnapshot.unavailable(bridgeAvailability);
      }
   }

   public static boolean sync(ServerPlayerEntity player) {
      return player != null && send(player, snapshot(player));
   }

   private static boolean allowRefreshRequest(ServerPlayerEntity player) {
      long now = player.getServerWorld().getTime();
      Long previous = LAST_REQUEST_TICK.put(player.getUuid(), now);
      return previous == null || now < previous || now - previous >= 10L;
   }

   static synchronized void ensurePayloadsRegistered() {
      if (!payloadsRegistered) {
         PayloadTypeRegistry.playS2C().register(SharedDiscoverySyncPayload.TYPE, SharedDiscoverySyncPayload.STREAM_CODEC);
         PayloadTypeRegistry.playC2S().register(SharedDiscoveryRequestPayload.TYPE, SharedDiscoveryRequestPayload.STREAM_CODEC);
         payloadsRegistered = true;
      }
   }

   private static boolean recordPersonal(ServerPlayerEntity player, Identifier speciesId, Identifier mutationId, Identifier sizeBandId) {
      try {
         boolean changed = mutationId != null && DiscoveryManager.discoverMutation(player, speciesId, mutationId);
         if (sizeBandId != null) {
            changed |= DiscoveryManager.discoverSizeBand(player, speciesId, sizeBandId);
         }

         if (changed) {
            DiscoveryManager.sync(player);
         }

         return changed;
      } catch (RuntimeException | LinkageError failure) {
         warnFailure(player, "record personal fallback", failure);
         return false;
      }
   }

   private static synchronized void ensureBridgeInitialized() {
      if (bridgeAvailability == SharedDiscoveryAvailability.UNKNOWN) {
         FabricLoader loader = FabricLoader.getInstance();
         if (loader.isModLoaded("tideborne") && loader.isModLoaded("ftbteams")) {
            try {
               bridge = ReflectiveFtbTeamsBridge.create();
               bridgeAvailability = SharedDiscoveryAvailability.AVAILABLE;
            } catch (RuntimeException | LinkageError failure) {
               bridge = null;
               bridgeAvailability = SharedDiscoveryAvailability.ERROR;
               if (WARNINGS.once("bridge-init")) {
                  TideTraits.LOGGER.warn("Tideborne shared discovery integration could not initialize; personal discovery remains active", failure);
               }
            }
         } else {
            bridgeAvailability = SharedDiscoveryAvailability.MISSING_MODS;
         }
      }
   }

   private static void broadcast(Object team, ServerPlayerEntity initiatingPlayer, SharedDiscoverySnapshot snapshot) {
      send(initiatingPlayer, snapshot);

      try {
         Collection<?> members = bridge.onlineMembers(team);
         Set<UUID> sent = new HashSet<>();
         sent.add(initiatingPlayer.getUuid());

         for (Object member : members) {
            if (member instanceof ServerPlayerEntity serverPlayer && sent.add(serverPlayer.getUuid())) {
               send(serverPlayer, snapshot);
            }
         }
      } catch (RuntimeException | LinkageError failure) {
         warnFailure(initiatingPlayer, "broadcast shared snapshot", failure);
      }
   }

   private static boolean send(ServerPlayerEntity player, SharedDiscoverySnapshot snapshot) {
      ensurePayloadsRegistered();
      if (!ServerPlayNetworking.canSend(player, SharedDiscoverySyncPayload.TYPE)) {
         return false;
      }

      ServerPlayNetworking.send(player, new SharedDiscoverySyncPayload(snapshot));
      return true;
   }

   private static void warnMissingTeam(ServerPlayerEntity player) {
      String key = "missing-team:" + player.getUuid();
      if (WARNINGS.once(key)) {
         TideTraits.LOGGER
            .warn("No effective FTB team resolved for {}; shared discovery was skipped and personal discovery retained", player.getGameProfile().getName());
      }
   }

   private static void warnFailure(ServerPlayerEntity player, String operation, Throwable failure) {
      String playerKey = player == null ? "unknown" : player.getUuid().toString();
      if (WARNINGS.rateLimited(operation + ":" + playerKey)) {
         TideTraits.LOGGER
            .warn(
               "Optional shared discovery operation '{}' failed for {}; the catch/personal journal continues",
               new Object[]{operation, player == null ? "unknown player" : player.getGameProfile().getName(), failure}
            );
      }
   }
}
