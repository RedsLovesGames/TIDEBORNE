/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import com.li64.tide.client.TideItemModelProperties;
import com.redslovesgames.tideborne.satchel.client.SatchelClientNetworking;
import com.redslovesgames.tideborne.presentation.render.MutationRendering;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryClient;
import com.redslovesgames.tideborne.discovery.DiscoveryClient;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

@Environment(EnvType.CLIENT)
public final class TideTraitsClient {
   private static boolean initialized;

   private TideTraitsClient() {
   }

   public static void initialize() {
      if (initialized) {
         return;
      }
      initialized = true;
      DiscoveryClient.initClient();
      MultiplayerDiscoveryClient.initClient();
      SatchelClientNetworking.initClient();
      MutationRendering.initClient();
      ModelPredicateProviderRegistry.register(
         SatchelRegistration.ANGLERS_SATCHEL, TideItemModelProperties.SATCHEL_STATE_PROPERTY, TideItemModelProperties.SATCHEL_STATE_FUNCTION
      );
   }
}
