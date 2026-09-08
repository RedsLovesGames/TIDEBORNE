/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.presentation.client.TideboundCompatibilityClient;
import com.redslovesgames.tideborne.journal.client.TideTeamJournalClient;
import com.redslovesgames.tideborne.presentation.client.TideTraitsClient;
import net.fabricmc.api.ClientModInitializer;

public final class TideborneClient implements ClientModInitializer {
   private static boolean initialized;

   public void onInitializeClient() {
      if (!initialized) {
         initialized = true;
         new TideTraitsClient().onInitializeClient();
         new TideTeamJournalClient().onInitializeClient();
         new TideboundCompatibilityClient().onInitializeClient();
         TideborneConfigBackend.afterSubsystems();
         System.out.println("[Tideborne] Unified client configuration and rendering systems initialized.");
      }
   }
}
