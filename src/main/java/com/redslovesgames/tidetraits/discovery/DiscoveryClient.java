/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.discovery;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;

@Environment(EnvType.CLIENT)
public final class DiscoveryClient {
   private static volatile DiscoverySnapshot current = DiscoverySnapshot.empty();
   private static boolean initialized;

   private DiscoveryClient() {
   }

   public static synchronized void initClient() {
      if (!initialized) {
         DiscoveryManager.ensurePayloadRegistered();
         ClientPlayNetworking.registerGlobalReceiver(DiscoverySyncPayload.TYPE, (payload, context) -> current = payload.snapshot());
         ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> current = DiscoverySnapshot.empty());
         initialized = true;
      }
   }

   public static DiscoverySnapshot snapshot() {
      return current;
   }
}
