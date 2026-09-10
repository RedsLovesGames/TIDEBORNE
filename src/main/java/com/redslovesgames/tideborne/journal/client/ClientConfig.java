/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.redslovesgames.tideborne.config.TideborneConfigStore;
import com.redslovesgames.tideborne.journal.TideTeamJournal;
import java.io.IOException;

/** Compatibility facade for the historical team-journal client config. */
public final class ClientConfig {
   public static final int LARGEST_COLOR_DEFAULT = 10121284;
   public static final int SMALLEST_COLOR_DEFAULT = 7757682;
   public static final int DISCOVERY_COLOR_DEFAULT = 5207921;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static Values values = new Values();

   private ClientConfig() {
   }

   public static Values get() {
      return values;
   }

   public static void load() {
      try {
         JsonObject section = TideborneConfigStore.readSection(TideborneConfigStore.TEAM_CLIENT);
         if (section != null) {
            Values parsed = GSON.fromJson(section, Values.class);
            if (parsed != null) values = parsed;
         } else {
            save();
         }
         validate();
      } catch (RuntimeException | IOException exception) {
         TideTeamJournal.LOGGER.error("Could not load Tideborne client journal configuration; using defaults", exception);
         values = new Values();
      }
   }

   public static void save() {
      validate();
      try {
         TideborneConfigStore.writeSection(TideborneConfigStore.TEAM_CLIENT, GSON.toJsonTree(values));
      } catch (IOException exception) {
         TideTeamJournal.LOGGER.error("Could not save Tideborne client journal configuration", exception);
      }
   }

   private static void validate() {
      values.toastDurationSeconds = Math.max(2, Math.min(20, values.toastDurationSeconds));
      values.defaultTab = valid(values.defaultTab, "summary", "leaderboard", "history") ? values.defaultTab : "summary";
      values.defaultMetric = valid(values.defaultMetric, "catches", "species", "record_events", "active_records", "fish_score") ? values.defaultMetric : "catches";
      values.toastMode = valid(values.toastMode, "full", "compact", "off") ? values.toastMode : "full";
      if (values.largestColor == 16777045) values.largestColor = LARGEST_COLOR_DEFAULT;
      if (values.smallestColor == 16733695) values.smallestColor = SMALLEST_COLOR_DEFAULT;
      if (values.discoveryColor == 5636095) values.discoveryColor = DISCOVERY_COLOR_DEFAULT;
   }

   private static boolean valid(String value, String... choices) {
      if (value == null) return false;
      for (String choice : choices) if (choice.equals(value)) return true;
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
      public int largestColor = LARGEST_COLOR_DEFAULT;
      public int smallestColor = SMALLEST_COLOR_DEFAULT;
      public int discoveryColor = DISCOVERY_COLOR_DEFAULT;
   }
}
