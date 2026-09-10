/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.redslovesgames.tideborne.journal.ServerConfig;
import com.redslovesgames.tideborne.journal.client.ClientConfig;
import com.redslovesgames.tideborne.presentation.client.TideboundClientConfig;

/**
 * Compatibility coordinator retained for existing callers.
 * Persistence ownership lives exclusively in {@link TideborneConfigStore}.
 */
public final class TideborneConfigBackend {
   public static final int SCHEMA_VERSION = TideborneConfigStore.SCHEMA_VERSION;

   private TideborneConfigBackend() {
   }

   public static synchronized void beforeSubsystems() {
      try {
         TideborneConfigStore.initialize();
      } catch (Exception exception) {
         System.err.println("[Tideborne] Unified config migration failed; defaults will remain usable: " + exception.getMessage());
      }
   }

   /** Historical lifecycle hook. Canonical config no longer needs post-init mirror imports. */
   public static synchronized void afterSubsystems() {
   }

   public static synchronized void saveAll(TideborneTraitsDraft traits, ServerConfig.Values teamServer, TideboundConfig.Values fishingServer) {
      TideTraitsConfigManager.save(traits.toSettings());
      if (!ServerConfig.save(teamServer)) {
         throw new IllegalStateException("Could not save Tideborne team configuration");
      }
      ClientConfig.save();
      TideboundClientConfig.save();
      if (fishingServer != null) {
         TideboundConfig.Result result = TideboundConfig.applyBalanceJson(TideboundConfig.toJson(fishingServer));
         if (!result.success()) {
            throw new IllegalStateException(result.message());
         }
      }
   }

   public static synchronized String reloadServerSide() {
      try {
         TideTraitsConfigManager.load();
         boolean team = ServerConfig.load();
         TideboundConfig.Result fishing = TideboundConfig.reloadBalance();
         return "Reloaded Tideborne config (team=" + team + ", fishing=" + fishing.success() + ")";
      } catch (Exception exception) {
         return "Tideborne reload failed: " + exception.getMessage();
      }
   }
}
