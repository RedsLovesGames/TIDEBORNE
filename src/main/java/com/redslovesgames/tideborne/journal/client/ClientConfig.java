/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redslovesgames.tideborne.journal.TideTeamJournal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfig {
   public static final int LARGEST_COLOR_DEFAULT = 10121284;
   public static final int SMALLEST_COLOR_DEFAULT = 7757682;
   public static final int DISCOVERY_COLOR_DEFAULT = 5207921;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tide_team_journal-client.json");
   private static ClientConfig.Values values = new ClientConfig.Values();

   private ClientConfig() {
   }

   public static ClientConfig.Values get() {
      return values;
   }

   public static void load() {
      try {
         if (Files.exists(PATH)) {
            ClientConfig.Values parsed = (ClientConfig.Values)GSON.fromJson(Files.readString(PATH), ClientConfig.Values.class);
            if (parsed != null) {
               values = parsed;
            }
         } else {
            save();
         }

         validate();
      } catch (RuntimeException | IOException exception) {
         TideTeamJournal.LOGGER.error("Could not load client configuration; using defaults", exception);
         values = new ClientConfig.Values();
      }
   }

   public static void save() {
      validate();

      try {
         Files.createDirectories(PATH.getParent());
         Files.writeString(PATH, GSON.toJson(values));
      } catch (IOException exception) {
         TideTeamJournal.LOGGER.error("Could not save client configuration", exception);
      }
   }

   private static void validate() {
      values.toastDurationSeconds = Math.max(2, Math.min(20, values.toastDurationSeconds));
      values.defaultTab = valid(values.defaultTab, "summary", "leaderboard", "history") ? values.defaultTab : "summary";
      values.defaultMetric = valid(values.defaultMetric, "catches", "species", "record_events", "active_records", "fish_score")
         ? values.defaultMetric
         : "catches";
      values.toastMode = valid(values.toastMode, "full", "compact", "off") ? values.toastMode : "full";
      if (values.largestColor == 16777045) {
         values.largestColor = 10121284;
      }

      if (values.smallestColor == 16733695) {
         values.smallestColor = 7757682;
      }

      if (values.discoveryColor == 5636095) {
         values.discoveryColor = 5207921;
      }
   }

   private static boolean valid(String value, String... choices) {
      if (value == null) {
         return false;
      }

      for (String choice : choices) {
         if (choice.equals(value)) {
            return true;
         }
      }

      return false;
   }

   public static final class Values {
      public boolean showRecordBadges = true;
      public boolean showRecordTooltips = true;
      public boolean showTeamRecordsButton = true;
      public boolean showFormerMembers = true;
      public String defaultTab = "summary";
      public String defaultMetric = "catches";
      public String toastMode = "full";
      public int toastDurationSeconds = 5;
      public boolean toastSound = true;
      public int largestColor = 10121284;
      public int smallestColor = 7757682;
      public int discoveryColor = 5207921;
   }
}
