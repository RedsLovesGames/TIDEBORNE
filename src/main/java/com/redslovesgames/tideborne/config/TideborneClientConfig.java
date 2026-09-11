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
import net.fabricmc.loader.api.FabricLoader;

/** Owns local presentation-only settings in tideborne-client.json. */
public final class TideborneClientConfig {
   public static final int SCHEMA_VERSION = 1;
   public static final String JOURNAL = "journal";
   public static final String FISHING = "fishing";

   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String LEGACY_JOURNAL = LegacyPersistenceIds.JOURNAL_CLIENT_CONFIG;
   private static final String LEGACY_FISHING = LegacyPersistenceIds.TIDEBOUND_CLIENT_CONFIG;

   private TideborneClientConfig() {
   }

   public static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("tideborne-client.json");
   }

   public static synchronized void initialize() throws IOException {
      boolean existed = Files.exists(path());
      JsonObject root = existed ? readRoot() : newRoot();
      boolean changed = !existed || !root.has("schema_version") || root.get("schema_version").getAsInt() != SCHEMA_VERSION;
      root.addProperty("schema_version", SCHEMA_VERSION);

      if (!root.has(JOURNAL)) {
         JsonObject imported = TideborneConfigStore.readSection("team_client");
         if (imported == null) imported = TideborneConfigStore.readLegacyFile(LEGACY_JOURNAL);
         if (imported != null) {
            root.add(JOURNAL, imported);
            backupLegacyIfPresent(LEGACY_JOURNAL);
            changed = true;
         }
      }

      if (!root.has(FISHING)) {
         JsonObject imported = TideborneConfigStore.readSection("fishing_client");
         if (imported == null) imported = TideborneConfigStore.readLegacyFile(LEGACY_FISHING);
         if (imported != null) {
            root.add(FISHING, imported);
            backupLegacyIfPresent(LEGACY_FISHING);
            changed = true;
         }
      }

      if (changed) writeRoot(root);

      // Older unified-config builds stored client sections in tideborne.json.
      // Remove them only after tideborne-client.json has been written successfully.
      if (Files.exists(path())) {
         TideborneConfigStore.removeSection("team_client");
         TideborneConfigStore.removeSection("fishing_client");
      }
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

   private static JsonObject newRoot() {
      JsonObject root = new JsonObject();
      root.addProperty("schema_version", SCHEMA_VERSION);
      root.addProperty("_documentation", "Canonical Tideborne local client configuration.");
      return root;
   }

   private static JsonObject readRoot() throws IOException {
      JsonElement parsed = JsonParser.parseString(Files.readString(path(), StandardCharsets.UTF_8));
      if (!parsed.isJsonObject()) throw new IOException("Tideborne client config root is not a JSON object: " + path());
      return parsed.getAsJsonObject();
   }

   private static void backupLegacyIfPresent(String fileName) throws IOException {
      Path legacy = FabricLoader.getInstance().getConfigDir().resolve(fileName);
      if (!Files.isRegularFile(legacy)) return;
      Path backup = legacy.resolveSibling(legacy.getFileName() + ".tideborne-migrated.bak");
      if (!Files.exists(backup)) Files.copy(legacy, backup, StandardCopyOption.COPY_ATTRIBUTES);
   }

   private static void writeRoot(JsonObject root) throws IOException {
      Files.createDirectories(path().getParent());
      Path temporary = path().resolveSibling("tideborne-client.json.tmp");
      Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);
      try {
         Files.move(temporary, path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException exception) {
         Files.move(temporary, path(), StandardCopyOption.REPLACE_EXISTING);
      }
   }
}
