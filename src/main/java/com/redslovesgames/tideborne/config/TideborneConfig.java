package com.redslovesgames.tideborne.config;

import com.redslovesgames.tideborne.journal.ServerConfig;

/** Canonical Tideborne server/gameplay configuration lifecycle. */
public final class TideborneConfig {
   private TideborneConfig() {
   }

   public static synchronized void initialize() {
      try {
         TideborneConfigStore.initialize();
      } catch (Exception exception) {
         System.err.println("[Tideborne] Unified config migration failed; defaults will remain usable: " + exception.getMessage());
      }
   }

   public static synchronized void saveServer(
      TideborneTraitsDraft traits,
      ServerConfig.Values teamServer,
      TideboundConfig.Values fishingServer
   ) {
      TideTraitsConfigManager.save(traits.toSettings());
      if (!ServerConfig.save(teamServer)) {
         throw new IllegalStateException("Could not save Tideborne team configuration");
      }
      if (fishingServer != null) {
         TideboundConfig.Result result = TideboundConfig.applyBalanceJson(TideboundConfig.toJson(fishingServer));
         if (!result.success()) throw new IllegalStateException(result.message());
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
