/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redslovesgames.tideboundcompatibility.client.TideboundClientConfig;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideteamjournal.ServerConfig;
import com.redslovesgames.tideteamjournal.client.ClientConfig;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.trait.FishMutation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;

public final class TideborneConfigBackend {
   public static final int SCHEMA_VERSION = 2;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String FILE = "tideborne.json";
   private static final Map<String, String> SECTIONS = Map.of(
      "traits",
      "tide_traits.json",
      "team_server",
      "tide_team_journal-server.json",
      "team_client",
      "tide_team_journal-client.json",
      "fishing_server",
      "tidebound_compatibility.json",
      "fishing_client",
      "tidebound_compatibility-client.json"
   );

   private TideborneConfigBackend() {
   }

   public static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("tideborne.json");
   }

   public static synchronized void beforeSubsystems() {
      try {
         JsonObject var0;
         if (Files.exists(path())) {
            var0 = readObject(path());
         } else {
            var0 = newRoot();
            importLegacy(var0, true);
            writeRoot(var0);
         }

         exportMirrors(var0);
      } catch (Exception var1) {
         System.err.println("[Tideborne] Unified config migration failed; legacy configs remain usable: " + var1.getMessage());
      }
   }

   public static synchronized void afterSubsystems() {
      try {
         JsonObject var0 = Files.exists(path()) ? readObject(path()) : newRoot();
         importLegacy(var0, false);
         writeRoot(var0);
      } catch (Exception var1) {
         System.err.println("[Tideborne] Could not refresh tideborne.json: " + var1.getMessage());
      }
   }

   public static synchronized void saveAll(TideborneTraitsDraft var0, ServerConfig.Values var1, TideboundConfig.Values var2) {
      try {
         writeTraitsMirror(var0.toSettings());
         writePojoMirror("team_server", var1);
         if (var2 != null) {
            Files.writeString(legacyPath("fishing_server"), TideboundConfig.toJson(var2), StandardCharsets.UTF_8);
         }

         ClientConfig.save();
         TideboundClientConfig.save();
         TideTraitsConfigManager.load();
         ServerConfig.load();
         if (var2 != null) {
            TideboundConfig.applyBalanceJson(TideboundConfig.toJson(var2));
         }

         JsonObject var3 = Files.exists(path()) ? readObject(path()) : newRoot();
         importLegacy(var3, false);
         writeRoot(var3);
      } catch (Exception var4) {
         throw new IllegalStateException("Could not save Tideborne configuration", var4);
      }
   }

   public static synchronized String reloadServerSide() {
      try {
         if (Files.exists(path())) {
            exportMirrors(readObject(path()));
         }

         TideTraitsConfigManager.load();
         boolean var0 = ServerConfig.load();
         TideboundConfig.Result var1 = TideboundConfig.reloadBalance();
         afterSubsystems();
         return "Reloaded Tideborne config (team=" + var0 + ", fishing=" + var1.success() + ")";
      } catch (Exception var2) {
         return "Tideborne reload failed: " + var2.getMessage();
      }
   }

   private static JsonObject newRoot() {
      JsonObject var0 = new JsonObject();
      var0.addProperty("schema_version", 2);
      var0.addProperty(
         "_documentation", "Canonical Tideborne config. Legacy module config files are generated compatibility mirrors; edit this file or use ModMenu."
      );
      JsonObject var1 = new JsonObject();
      var1.addProperty("legacy_import_complete", true);
      var1.addProperty("legacy_namespaces_preserved", true);
      var1.addProperty("note", "Serialized registry/network/saved-data IDs remain under their historical namespaces for world compatibility.");
      var0.add("migration", var1);
      return var0;
   }

   private static void importLegacy(JsonObject var0, boolean var1) throws IOException {
      var0.addProperty("schema_version", 2);

      for (Entry var3 : SECTIONS.entrySet()) {
         Path var4 = FabricLoader.getInstance().getConfigDir().resolve((String)var3.getValue());
         if (Files.exists(var4)) {
            if (var1) {
               backupOnce(var4);
            }

            try {
               JsonElement var5 = JsonParser.parseString(Files.readString(var4, StandardCharsets.UTF_8));
               if (var5.isJsonObject()) {
                  var0.add((String)var3.getKey(), var5.getAsJsonObject());
               }
            } catch (RuntimeException var6) {
            }
         }
      }
   }

   private static void exportMirrors(JsonObject var0) throws IOException {
      for (Entry var2 : SECTIONS.entrySet()) {
         JsonElement var3 = var0.get((String)var2.getKey());
         if (var3 != null && var3.isJsonObject()) {
            Path var4 = FabricLoader.getInstance().getConfigDir().resolve((String)var2.getValue());
            Files.createDirectories(var4.getParent());
            Files.writeString(var4, GSON.toJson(var3), StandardCharsets.UTF_8);
         }
      }
   }

   private static void writePojoMirror(String var0, Object var1) throws IOException {
      Files.writeString(legacyPath(var0), GSON.toJson(var1), StandardCharsets.UTF_8);
   }

   private static Path legacyPath(String var0) {
      return FabricLoader.getInstance().getConfigDir().resolve(SECTIONS.get(var0));
   }

   private static void backupOnce(Path var0) throws IOException {
      Path var1 = var0.resolveSibling(var0.getFileName().toString() + ".tideborne-migrated.bak");
      if (!Files.exists(var1)) {
         Files.copy(var0, var1, StandardCopyOption.COPY_ATTRIBUTES);
      }
   }

   private static JsonObject readObject(Path var0) throws IOException {
      JsonElement var1 = JsonParser.parseString(Files.readString(var0, StandardCharsets.UTF_8));
      if (!var1.isJsonObject()) {
         throw new IOException("Root is not a JSON object: " + var0);
      } else {
         return var1.getAsJsonObject();
      }
   }

   private static void writeRoot(JsonObject var0) throws IOException {
      Files.createDirectories(path().getParent());
      Path var1 = path().resolveSibling("tideborne.json.tmp");
      Files.writeString(var1, GSON.toJson(var0), StandardCharsets.UTF_8);

      try {
         Files.move(var1, path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException var3) {
         Files.move(var1, path(), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private static void writeTraitsMirror(TideTraitsConfigManager.Settings var0) throws IOException {
      JsonObject var1 = new JsonObject();
      var1.addProperty("_documentation", "Managed by Tideborne. Server authoritative.");
      JsonObject var2 = new JsonObject();
      var2.addProperty("redistribute_ineligible_odds", var0.mutations().redistributeIneligibleMutationOdds());
      JsonObject var3 = new JsonObject();

      for (FishMutation var5 : FishMutation.mutations()) {
         var3.addProperty(var5.serializedName(), var0.mutations().probability(var5));
      }

      var2.add("odds", var3);
      var1.add("mutations", var2);
      JsonObject var10 = new JsonObject();
      var10.add("dwarf_multiplier", pair(var0.mutations().dwarfLengthMultiplier().minInclusive(), var0.mutations().dwarfLengthMultiplier().maxInclusive()));
      var10.add("giant_multiplier", pair(var0.mutations().giantLengthMultiplier().minInclusive(), var0.mutations().giantLengthMultiplier().maxInclusive()));
      var10.add(
         "parasite_multiplier", pair(var0.mutations().parasiteLengthMultiplier().minInclusive(), var0.mutations().parasiteLengthMultiplier().maxInclusive())
      );
      var10.add(
         "perfect_normal_percentile",
         pair(var0.mutations().perfectSpecimenNormalPercentile().minInclusive(), var0.mutations().perfectSpecimenNormalPercentile().maxInclusive())
      );
      var1.add("mutation_size", var10);
      JsonObject var11 = new JsonObject();
      var11.addProperty("dynamic_texture_cache_maximum", var0.rendering().dynamicTextureCacheMaximum());
      var1.add("rendering", var11);
      JsonObject var6 = new JsonObject();
      var6.addProperty("conversion_xp_cost", var0.satchel().conversionXpCost());
      var1.add("satchel", var6);
      JsonObject var7 = new JsonObject();
      var7.add("multipliers", GSON.toJsonTree(var0.satchel().capacityMultipliers()));
      var7.add("xp_costs", GSON.toJsonTree(var0.satchel().capacityXpCosts()));
      var1.add("satchel_capacity", var7);
      var1.add("xp_costs", GSON.toJsonTree(var0.satchel().featureXpCosts()));
      var1.add("protection_defaults", GSON.toJsonTree(var0.satchel().protectionDefaults()));
      JsonObject var8 = new JsonObject();
      var8.addProperty("shared_discovery", var0.sharedDiscovery());
      var1.add("multiplayer", var8);
      JsonObject var9 = new JsonObject();
      var9.addProperty("logging", var0.debugLogging());
      var1.add("debug", var9);
      Files.writeString(legacyPath("traits"), GSON.toJson(var1), StandardCharsets.UTF_8);
   }

   private static JsonElement pair(double var0, double var2) {
      return JsonParser.parseString("[" + var0 + "," + var2 + "]");
   }
}
