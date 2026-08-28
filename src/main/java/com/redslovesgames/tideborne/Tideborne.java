/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne;

import com.redslovesgames.tideborne.command.TideborneCommands;
import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.migration.TideborneMigrationManager;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideteamjournal.TideTeamJournal;
import com.redslovesgames.tidetraits.TideTraits;
import net.fabricmc.api.ModInitializer;

public final class Tideborne implements ModInitializer {
   public static final String MOD_ID = "tideborne";
   private static boolean initialized;

   public void onInitialize() {
      if (!initialized) {
         initialized = true;
         System.out.println("[Tideborne] Starting 1.3.13 Native Integration...");
         TideborneMigrationManager.migrate();
         TideborneConfigBackend.beforeSubsystems();
         new TideTraits().onInitialize();
         new TideTeamJournal().onInitialize();
         new TideboundCompatibility().onInitialize();
         TideborneCommands.init();
         TideborneConfigBackend.afterSubsystems();
         System.out.println("[Tideborne] Native backend, unified commands/config, migration and compatibility layers initialized.");
      }
   }
}
