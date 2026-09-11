package com.redslovesgames.tideborne.config;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyPersistenceIds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Owns Tideborne's canonical server/gameplay configuration file and one-way legacy imports.
 * Historical config files are migration inputs only and are never rewritten.
 */
public final class TideborneConfigStore {
   public static final int SCHEMA_VERSION = 2;
   public static final String TRAITS = "traits";
   public static final String TEAM_SERVER = "team_server";
   public static final String FISHING_SERVER = "fishing_server";

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Map<String, String> LEGACY_FILES = legacyFiles();

   private TideborneConfigStore() {
   }

   public static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("tideborne.json");
   }

   public static synchronized void initialize() throws IOException {
      boolean existed = Files.exists(path());
      JsonObject root = existed ? readRoot() : newRoot();
      boolean changed = !existed || !root.has("schema_version") || root.get("schema_version").getAsInt() != SCHEMA_VERSION;
      root.addProperty("schema_version", SCHEMA_VERSION);

      for (Map.Entry<String, String> entry : LEGACY_FILES.entrySet()) {
         if (!root.has(entry.getKey())) {
            Path legacy = FabricLoader.getInstance().getConfigDir().resolve(entry.getValue());
            JsonObject imported = readLegacyObject(legacy);
            if (imported != null) {
               backupOnce(legacy);
               root.add(entry.getKey(), imported);
               changed = true;
            }
         }
      }

      if (changed) writeRoot(root);
   }

   public static synchronized JsonObject readSection(String section) throws IOException {
      initialize();
      JsonObject root = readRoot();
      JsonElement value = root.get(section);
      return value != null && value.isJsonObject() ? value.getAsJsonObject().deepCopy() : null;
   }

   public static synchronized void writeSection(String section, JsonElement value) throws IOException {
      initialize();
      JsonObject root = readRoot();
      root.addProperty("schema_version", SCHEMA_VERSION);
      root.add(section, value.deepCopy());
      writeRoot(root);
   }

   public static synchronized void writeSection(String section, Object value) throws IOException {
      writeSection(section, GSON.toJsonTree(value));
   }

   public static synchronized void removeSection(String section) throws IOException {
      initialize();
      JsonObject root = readRoot();
      if (root.remove(section) != null) writeRoot(root);
   }

   static JsonObject readLegacyFile(String fileName) throws IOException {
      return readLegacyObject(FabricLoader.getInstance().getConfigDir().resolve(fileName));
   }

   private static Map<String, String> legacyFiles() {
      Map<String, String> result = new LinkedHashMap<>();
      result.put(TRAITS, LegacyPersistenceIds.TRAITS_CONFIG);
      result.put(TEAM_SERVER, LegacyPersistenceIds.JOURNAL_SERVER_CONFIG);
      result.put(FISHING_SERVER, LegacyPersistenceIds.TIDEBOUND_SERVER_CONFIG);
      return Map.copyOf(result);
   }

   private static JsonObject newRoot() {
      JsonObject root = new JsonObject();
      root.addProperty("schema_version", SCHEMA_VERSION);
      root.addProperty("_documentation", "Canonical Tideborne server/gameplay configuration. Historical config files are migration inputs only.");
      JsonObject migration = new JsonObject();
      migration.addProperty("legacy_namespaces_preserved", true);
      migration.addProperty("note", "Serialized registry, network, and saved-data IDs retain historical namespaces for compatibility.");
      root.add("migration", migration);
      return root;
   }

   private static JsonObject readRoot() throws IOException {
      JsonElement parsed = JsonParser.parseString(Files.readString(path(), StandardCharsets.UTF_8));
      if (!parsed.isJsonObject()) throw new IOException("Tideborne config root is not a JSON object: " + path());
      return parsed.getAsJsonObject();
   }

   private static JsonObject readLegacyObject(Path path) throws IOException {
      if (!Files.isRegularFile(path)) return null;
      try {
         JsonElement parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
         return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static void backupOnce(Path legacy) throws IOException {
      Path backup = legacy.resolveSibling(legacy.getFileName() + ".tideborne-migrated.bak");
      if (!Files.exists(backup)) Files.copy(legacy, backup, StandardCopyOption.COPY_ATTRIBUTES);
   }

   private static void writeRoot(JsonObject root) throws IOException {
      Files.createDirectories(path().getParent());
      Path temporary = path().resolveSibling("tideborne.json.tmp");
      Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);
      try {
         Files.move(temporary, path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException exception) {
         Files.move(temporary, path(), StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
