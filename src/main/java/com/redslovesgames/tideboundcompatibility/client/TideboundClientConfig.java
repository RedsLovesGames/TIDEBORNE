/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class TideboundClientConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tidebound_compatibility-client.json");
   private static TideboundClientConfig.Values values = new TideboundClientConfig.Values();

   private TideboundClientConfig() {
   }

   public static TideboundClientConfig.Values get() {
      return values;
   }

   public static void load() {
      try {
         if (Files.isRegularFile(PATH)) {
            TideboundClientConfig.Values parsed = (TideboundClientConfig.Values)GSON.fromJson(Files.readString(PATH), TideboundClientConfig.Values.class);
            if (parsed != null) {
               values = parsed;
            }
         } else {
            save();
         }
      } catch (IOException | RuntimeException exception) {
         TideboundCompatibility.LOGGER.error("Could not load Tidebound client settings; using defaults", exception);
         values = new TideboundClientConfig.Values();
      }
   }

   public static void save() {
      try {
         Files.createDirectories(PATH.getParent());
         Files.writeString(PATH, GSON.toJson(values));
      } catch (IOException exception) {
         TideboundCompatibility.LOGGER.error("Could not save Tidebound client settings", exception);
      }
   }

   public static final class Values {
      public boolean showFishingHud = true;
      public boolean showEquipmentTooltips = true;
   }
}
