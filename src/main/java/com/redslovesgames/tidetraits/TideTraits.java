/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits;

import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.command.TideTraitsCommands;
import com.redslovesgames.tidetraits.compat.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.discovery.DiscoveryManager;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import com.redslovesgames.tidetraits.satchel.network.SatchelNetworking;
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
