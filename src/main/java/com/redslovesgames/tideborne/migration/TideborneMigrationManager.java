/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.migration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public final class TideborneMigrationManager {
   public static final int CURRENT_VERSION = 3;
   private static final String FILE_NAME = "tideborne-migration.json";

   private TideborneMigrationManager() {
   }

   public static synchronized void migrate() {
      try {
         Path var0 = Path.of("config");
         Files.createDirectories(var0);
         Path var1 = var0.resolve("tideborne-migration.json");
         int var2 = readVersion(var1);
         if (var2 >= 3) {
            return;
         }

         backupOnce(var0.resolve("tideborne.json"));
         backupOnce(var0.resolve("tide_traits.json"));
         backupOnce(var0.resolve("tide_team_journal-server.json"));
         backupOnce(var0.resolve("tide_team_journal-client.json"));
         backupOnce(var0.resolve("tidebound_compatibility.json"));
         backupOnce(var0.resolve("tidebound_compatibility-client.json"));
         String var3 = "{\n  \"migrationVersion\": 3,\n  \"legacyNamespacesPreserved\": true,\n  \"canonicalModId\": \"tideborne\",\n  \"canonicalConfig\": \"tideborne.json\",\n  \"completedAt\": \""
            + Instant.now()
            + "\"\n}\n";
         Files.writeString(var1, var3, StandardCharsets.UTF_8);
         System.out.println("[Tideborne] Migration updated from " + var2 + " to 3.");
      } catch (Throwable var4) {
         System.err.println("[Tideborne] Migration marker update failed; legacy data was left untouched: " + var4);
      }
   }

   public static int currentVersion() {
      try {
         return readVersion(Path.of("config").resolve("tideborne-migration.json"));
      } catch (Throwable var1) {
         return 0;
      }
   }

   public static String status() {
      int var0 = currentVersion();
      return "Tideborne migration " + var0 + "/3" + (var0 >= 3 ? " complete" : " pending");
   }

   private static int readVersion(Path var0) throws IOException {
      if (!Files.exists(var0)) {
         return 0;
      }

      String var1 = Files.readString(var0, StandardCharsets.UTF_8);
      int var2 = var1.indexOf("\"migrationVersion\"");
      if (var2 < 0) {
         return 0;
      }

      int var3 = var1.indexOf(58, var2);
      if (var3 < 0) {
         return 0;
      }

      int var4 = var3 + 1;

      while (var4 < var1.length() && Character.isWhitespace(var1.charAt(var4))) {
         var4++;
      }

      int var5 = var4;

      while (var4 < var1.length() && Character.isDigit(var1.charAt(var4))) {
         var4++;
      }

      return var5 == var4 ? 0 : Integer.parseInt(var1.substring(var5, var4));
   }

   private static void backupOnce(Path var0) throws IOException {
      if (Files.exists(var0)) {
         Path var1 = var0.resolveSibling(var0.getFileName().toString() + ".tideborne-v3.bak");
         if (!Files.exists(var1)) {
            Files.copy(var0, var1, StandardCopyOption.COPY_ATTRIBUTES);
         }
      }
   }
}
