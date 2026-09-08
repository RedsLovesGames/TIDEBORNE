/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing;

import com.redslovesgames.tideborne.fishing.specimen.CatchTraitService;
import com.redslovesgames.tideborne.command.TideTraitsCommands;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import com.redslovesgames.tideborne.discovery.DiscoveryManager;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import com.redslovesgames.tideborne.satchel.network.SatchelNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TideTraits implements ModInitializer {
   public static final String MOD_ID = "tide_traits";
   public static final Logger LOGGER = LoggerFactory.getLogger("tide_traits");

   public void onInitialize() {
      TideTraitsConfigManager.Settings settings = TideTraitsConfigManager.load();
      TideTraitsComponents.init();
      SatchelRegistration.init();
      SatchelNetworking.initServer();
      DiscoveryManager.initServer();
      MultiplayerDiscoveryCompat.initServer();
      CatchTraitService.INSTANCE.setConfig(settings.mutations());
      CatchTraitService.INSTANCE.init();
      TideTraitsCommands.init();
      LOGGER.info("[Tideborne] Initializing traits and specimen systems");
   }
}
