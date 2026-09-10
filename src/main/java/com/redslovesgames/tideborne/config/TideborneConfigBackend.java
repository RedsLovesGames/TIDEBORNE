/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.redslovesgames.tideborne.journal.ServerConfig;
import com.redslovesgames.tideborne.journal.client.ClientConfig;
import com.redslovesgames.tideborne.presentation.client.TideboundClientConfig;

/** @deprecated Use Tideborne-owned config classes directly. */
@Deprecated(forRemoval = false)
public final class TideborneConfigBackend {
   public static final int SCHEMA_VERSION = TideborneConfigStore.SCHEMA_VERSION;

   private TideborneConfigBackend() {
   }

   public static void beforeSubsystems() {
      TideborneConfig.initialize();
   }

   public static void afterSubsystems() {
   }

   public static void saveAll(TideborneTraitsDraft traits, ServerConfig.Values teamServer, TideboundConfig.Values fishingServer) {
      TideborneConfig.saveServer(traits, teamServer, fishingServer);
      ClientConfig.save();
      TideboundClientConfig.save();
   }

   public static String reloadServerSide() {
      return TideborneConfig.reloadServerSide();
   }
}
