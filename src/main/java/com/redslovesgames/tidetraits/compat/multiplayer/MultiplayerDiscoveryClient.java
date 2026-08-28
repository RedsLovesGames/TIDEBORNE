/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;

@Environment(EnvType.CLIENT)
public final class MultiplayerDiscoveryClient {
   private static volatile SharedDiscoverySnapshot current = SharedDiscoverySnapshot.unknown();
   private static boolean initialized;

   private MultiplayerDiscoveryClient() {
   }

   public static synchronized void initClient() {
      if (!initialized) {
         MultiplayerDiscoveryCompat.ensurePayloadsRegistered();
         ClientPlayNetworking.registerGlobalReceiver(SharedDiscoverySyncPayload.TYPE, (payload, context) -> current = payload.snapshot());
         ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> current = SharedDiscoverySnapshot.unknown());
         initialized = true;
      }
   }

   public static boolean requestSync() {
      if (initialized && ClientPlayNetworking.canSend(SharedDiscoveryRequestPayload.TYPE)) {
         ClientPlayNetworking.send(SharedDiscoveryRequestPayload.current());
         return true;
      } else {
         return false;
      }
   }

   public static SharedDiscoverySnapshot snapshot() {
      return current;
   }

   public static SharedDiscoveryAvailability availability() {
      return current.availability();
   }

   public static boolean isAvailable() {
      return current.isAvailable();
   }
}
