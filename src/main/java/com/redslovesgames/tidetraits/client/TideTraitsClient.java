/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.client;

import com.li64.tide.client.TideItemModelProperties;
import com.redslovesgames.tidetraits.client.gui.satchel.SatchelClientNetworking;
import com.redslovesgames.tidetraits.client.render.MutationRendering;
import com.redslovesgames.tidetraits.compat.multiplayer.MultiplayerDiscoveryClient;
import com.redslovesgames.tidetraits.discovery.DiscoveryClient;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;

@Environment(EnvType.CLIENT)
public final class TideTraitsClient implements ClientModInitializer {
   public void onInitializeClient() {
      DiscoveryClient.initClient();
      MultiplayerDiscoveryClient.initClient();
      SatchelClientNetworking.initClient();
      MutationRendering.initClient();
      ModelPredicateProviderRegistry.register(
         SatchelRegistration.ANGLERS_SATCHEL, TideItemModelProperties.SATCHEL_STATE_PROPERTY, TideItemModelProperties.SATCHEL_STATE_FUNCTION
      );
   }
}
