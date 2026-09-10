/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.redslovesgames.tideborne.config.TideborneConfigStore;
import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import java.io.IOException;

/** Compatibility facade for the historical Tidebound client settings shape. */
public final class TideboundClientConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static Values values = new Values();

   private TideboundClientConfig() {
   }

   public static Values get() {
      return values;
   }

   public static void load() {
      try {
         JsonObject section = TideborneConfigStore.readSection(TideborneConfigStore.FISHING_CLIENT);
         if (section != null) {
            Values parsed = GSON.fromJson(section, Values.class);
            if (parsed != null) values = parsed;
         } else {
            save();
         }
      } catch (IOException | RuntimeException exception) {
         FishingGameplayInitializer.LOGGER.error("Could not load Tideborne fishing client settings; using defaults", exception);
         values = new Values();
      }
   }

   public static void save() {
      try {
         TideborneConfigStore.writeSection(TideborneConfigStore.FISHING_CLIENT, GSON.toJsonTree(values));
      } catch (IOException exception) {
         FishingGameplayInitializer.LOGGER.error("Could not save Tideborne fishing client settings", exception);
      }
   }

   public static final class Values {
      public boolean showFishingHud = true;
      public boolean showEquipmentTooltips = true;
   }
}
