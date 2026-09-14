/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.client.validation.CiDirectConnect;
import com.redslovesgames.tideborne.config.TideborneClientConfig;
import com.redslovesgames.tideborne.presentation.client.TideboundCompatibilityClient;
import com.redslovesgames.tideborne.journal.client.TideTeamJournalClient;
import com.redslovesgames.tideborne.presentation.client.TideTraitsClient;
import net.fabricmc.api.ClientModInitializer;

public final class TideborneClient implements ClientModInitializer {
   private static boolean initialized;

   public void onInitializeClient() {
      if (!initialized) {
         initialized = true;
         try {
            TideborneClientConfig.initialize();
         } catch (Exception exception) {
            System.err.println("[Tideborne] Client config migration failed; client defaults will remain usable: " + exception.getMessage());
         }
         TideTraitsClient.initialize();
         TideTeamJournalClient.initialize();
         TideboundCompatibilityClient.initialize();
         CiDirectConnect.initializeFromEnvironment();
         System.out.println("[Tideborne] Unified client configuration and rendering systems initialized.");
      }
   }
}
