/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class TideboundConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static volatile TideboundConfig.Values values = new TideboundConfig.Values();

   private TideboundConfig() {
   }

   public static TideboundConfig.Values get() {
      return values;
   }

   public static synchronized TideboundConfig.Result load() {
      return load(false);
   }

   public static synchronized TideboundConfig.Result reloadBalance() {
      return load(true);
   }

   public static synchronized TideboundConfig.Result applyBalanceJson(String json) {
      try {
         TideboundConfig.Values requested = parse(json);
         TideboundConfig.Values previous = values;
         TideboundConfig.Values validated = validate(requested);
         validated.enableMythsCompat = previous.enableMythsCompat;
         validated.enableApexCompat = previous.enableApexCompat;
         TideboundConfig.Result saved = write(validated);
         if (saved.success()) {
            values = validated;
         }

         return saved;
      } catch (RuntimeException exception) {
         TideboundCompatibility.LOGGER.warn("Rejected invalid Tidebound settings", exception);
         return TideboundConfig.Result.failure("Settings contain an invalid value.");
      }
   }

   public static String settingsJson() {
      return GSON.toJson(values);
   }

   public static String toJson(TideboundConfig.Values input) {
      return GSON.toJson(input);
   }

   public static TideboundConfig.Values valuesFromJson(String json) {
      try {
         return validate(parse(json));
      } catch (RuntimeException ignored) {
         return new TideboundConfig.Values();
      }
   }

   private static TideboundConfig.Result load(boolean preserveIntegrationToggles) {
      Path path = path();

      try {
         TideboundConfig.Values loaded = Files.isRegularFile(path) ? parse(Files.readString(path)) : new TideboundConfig.Values();
         TideboundConfig.Values validated = validate(loaded);
         if (preserveIntegrationToggles) {
            validated.enableMythsCompat = values.enableMythsCompat;
            validated.enableApexCompat = values.enableApexCompat;
         }

         TideboundConfig.Result saved = write(validated);
         if (saved.success()) {
            values = validated;
         }

         return saved.success() ? TideboundConfig.Result.success("Configuration loaded.") : saved;
      } catch (Exception exception) {
         TideboundCompatibility.LOGGER.error("Could not load {}", path, exception);
         return TideboundConfig.Result.failure("Configuration file is invalid; kept the previous settings.");
      }
   }

   private static TideboundConfig.Values parse(String json) {
      JsonObject object = JsonParser.parseString(json).getAsJsonObject();
      migrate(object, "minimumSharkHungerForFishTarget", "maximumFullnessForFoodTargeting");
      migrate(object, "rareFishScentMultiplier", "strongSharkFoodScentMultiplier");
      migrate(object, "steelLeaderTheftPreventionChance", "steelLeaderCatchLossPreventionChance");
      migrate(object, "leviathanBaitDifficultyMultiplier", "leviathanBaitMinigameSpeedMultiplier");
      return (TideboundConfig.Values)GSON.fromJson(object, TideboundConfig.Values.class);
   }

   private static void migrate(JsonObject object, String oldKey, String newKey) {
      if (!object.has(newKey) && object.has(oldKey)) {
         object.add(newKey, object.get(oldKey));
      }
   }

   public static TideboundConfig.Values validate(TideboundConfig.Values v) {
      finite(v.sharkFishDetectionRadius, "sharkFishDetectionRadius");
      finite(v.chumRadius, "chumRadius");
      finite(v.chumScentStrength, "chumScentStrength");
      finite(v.largeFishScentMultiplier, "largeFishScentMultiplier");
      finite(v.strongSharkFoodScentMultiplier, "strongSharkFoodScentMultiplier");
      finite(v.leviathanBaitFishSelectionLuckBonus, "leviathanBaitFishSelectionLuckBonus");
      finite(v.leviathanBaitMinigameSpeedMultiplier, "leviathanBaitMinigameSpeedMultiplier");
      finite(v.leviathanBaitCatchZoneMultiplier, "leviathanBaitCatchZoneMultiplier");
      finite(v.tentacleCatchZoneMultiplier, "tentacleCatchZoneMultiplier");
      finite(v.tentacleFishSpeedMultiplier, "tentacleFishSpeedMultiplier");
      finite(v.swiftCatchZoneMultiplier, "swiftCatchZoneMultiplier");
      finite(v.swiftFishSpeedMultiplier, "swiftFishSpeedMultiplier");
      finite(v.steelLeaderCatchZoneMultiplier, "steelLeaderCatchZoneMultiplier");
      finite(v.steelLeaderFishSpeedMultiplier, "steelLeaderFishSpeedMultiplier");
      finite(v.steelLeaderCatchLossPreventionChance, "steelLeaderCatchLossPreventionChance");
      finite(v.seafarersRareWeightMultiplier, "seafarersRareWeightMultiplier");
      finite(v.kujiraOceanCrateMultiplier, "kujiraOceanCrateMultiplier");
      finite(v.sharkToothPredatoryWeightMultiplier, "sharkToothPredatoryWeightMultiplier");
      finite(v.sharkToothSmallFishWeightMultiplier, "sharkToothSmallFishWeightMultiplier");
      finite(v.sharkPreyAcquisitionChance, "sharkPreyAcquisitionChance");
      finite(v.sharkTheftBaseChance, "sharkTheftBaseChance");
      finite(v.sharkTheftScentChancePerStrength, "sharkTheftScentChancePerStrength");
      finite(v.sharkTheftScentBonusCap, "sharkTheftScentBonusCap");
      finite(v.sharkTheftLargeFishBonus, "sharkTheftLargeFishBonus");
      finite(v.sharkTheftTunaBonus, "sharkTheftTunaBonus");
      finite(v.sharkTheftMaximumChance, "sharkTheftMaximumChance");
      v.sharkFishDetectionRadius = clamp(v.sharkFishDetectionRadius, 8.0, 96.0);
      v.maximumFullnessForFoodTargeting = clamp(v.maximumFullnessForFoodTargeting, 0, 10000);
      v.chumDuration = clamp(v.chumDuration, 1, 3600);
      v.chumRadius = clamp(v.chumRadius, 8.0, 96.0);
      v.chumParticleCount = clamp(v.chumParticleCount, 0, 1000);
      v.chumParticlePulseInterval = clamp(v.chumParticlePulseInterval, 1, 1200);
      v.chumScentStrength = clamp(v.chumScentStrength, 0.0, 64.0);
      v.chumSpawnCheckInterval = clamp(v.chumSpawnCheckInterval, 20, 72000);
      v.chumSpawnChanceOneIn = clamp(v.chumSpawnChanceOneIn, 1, 100000);
      v.chumNearbySharkCap = clamp(v.chumNearbySharkCap, 0, 32);
      v.chumSpawnAttempts = clamp(v.chumSpawnAttempts, 1, 128);
      v.largeFishScentMultiplier = clamp(v.largeFishScentMultiplier, 0.0, 10.0);
      v.strongSharkFoodScentMultiplier = clamp(v.strongSharkFoodScentMultiplier, 0.0, 10.0);
      v.sharkPreyAcquisitionChance = clamp(v.sharkPreyAcquisitionChance, 0.0, 1.0);
      v.leviathanBaitFishSelectionLuckBonus = clamp(v.leviathanBaitFishSelectionLuckBonus, 0, 100);
      v.leviathanBaitMinigameSpeedMultiplier = clamp(v.leviathanBaitMinigameSpeedMultiplier, 0.1, 5.0);
      v.leviathanBaitCatchZoneMultiplier = clamp(v.leviathanBaitCatchZoneMultiplier, 0.05, 1.0);
      v.tentacleCatchZoneMultiplier = clamp(v.tentacleCatchZoneMultiplier, 0.1, 3.0);
      v.tentacleFishSpeedMultiplier = clamp(v.tentacleFishSpeedMultiplier, 0.1, 3.0);
      v.swiftCatchZoneMultiplier = clamp(v.swiftCatchZoneMultiplier, 0.1, 3.0);
      v.swiftFishSpeedMultiplier = clamp(v.swiftFishSpeedMultiplier, 0.1, 3.0);
      v.steelLeaderCatchZoneMultiplier = clamp(v.steelLeaderCatchZoneMultiplier, 0.1, 3.0);
      v.steelLeaderFishSpeedMultiplier = clamp(v.steelLeaderFishSpeedMultiplier, 0.1, 3.0);
      v.steelLeaderCatchLossPreventionChance = clamp(v.steelLeaderCatchLossPreventionChance, 0.0, 1.0);
      v.seafarersRareWeightMultiplier = clamp(v.seafarersRareWeightMultiplier, 0.0, 10.0);
      v.kujiraOceanCrateMultiplier = clamp(v.kujiraOceanCrateMultiplier, 0.0, 10.0);
      v.sharkToothPredatoryWeightMultiplier = clamp(v.sharkToothPredatoryWeightMultiplier, 0.0, 10.0);
      v.sharkToothSmallFishWeightMultiplier = clamp(v.sharkToothSmallFishWeightMultiplier, 0.0, 10.0);
      v.sharkTheftBaseChance = clamp(v.sharkTheftBaseChance, 0.0, 1.0);
      v.sharkTheftScentChancePerStrength = clamp(v.sharkTheftScentChancePerStrength, 0.0, 1.0);
      v.sharkTheftScentBonusCap = clamp(v.sharkTheftScentBonusCap, 0.0, 1.0);
      v.sharkTheftLargeFishBonus = clamp(v.sharkTheftLargeFishBonus, 0.0, 1.0);
      v.sharkTheftTunaBonus = clamp(v.sharkTheftTunaBonus, 0.0, 1.0);
      v.sharkTheftMaximumChance = clamp(v.sharkTheftMaximumChance, 0.0, 1.0);
      v.sharkPreyCooldown = clamp(v.sharkPreyCooldown, 0, 72000);
      v.sharkScanInterval = clamp(v.sharkScanInterval, 5, 1200);
      v.sharkFoodSatiation = clamp(v.sharkFoodSatiation, 1, 10000);
      return v;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static void finite(double value, String name) {
      if (!Double.isFinite(value)) {
         throw new IllegalArgumentException(name + " must be finite");
      }
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("tidebound_compatibility.json");
   }

   private static TideboundConfig.Result write(TideboundConfig.Values input) {
      try {
         Files.createDirectories(path().getParent());
         Files.writeString(path(), GSON.toJson(input));
         return TideboundConfig.Result.success("Configuration saved.");
      } catch (IOException exception) {
         TideboundCompatibility.LOGGER.error("Could not write Tidebound config", exception);
         return TideboundConfig.Result.failure("Could not write the configuration file.");
      }
   }

   public record Result(boolean success, String message) {
      public static TideboundConfig.Result success(String message) {
         return new TideboundConfig.Result(true, message);
      }

      public static TideboundConfig.Result failure(String message) {
         return new TideboundConfig.Result(false, message);
      }
   }

   public static final class Values {
      public boolean enableMythsCompat = true;
      public boolean enableApexCompat = true;
      public boolean debugLogging = false;
      public boolean enableSharkFishAttraction = true;
      public boolean enableSharkFishPredation = true;
      public double sharkFishDetectionRadius = 48.0;
      public double sharkPreyAcquisitionChance = 0.35;
      public int maximumFullnessForFoodTargeting = 40;
      public int sharkPreyCooldown = 200;
      public int sharkScanInterval = 20;
      public int sharkFoodSatiation = 30;
      public boolean enableSharkCatchLoss = true;
      public double sharkTheftBaseChance = 0.005;
      public double sharkTheftScentChancePerStrength = 0.012;
      public double sharkTheftScentBonusCap = 0.12;
      public double sharkTheftLargeFishBonus = 0.05;
      public double sharkTheftTunaBonus = 0.1;
      public double sharkTheftMaximumChance = 0.25;
      public double largeFishScentMultiplier = 2.0;
      public double strongSharkFoodScentMultiplier = 2.5;
      public double steelLeaderCatchLossPreventionChance = 0.9;
      public double tentacleCatchZoneMultiplier = 1.45;
      public double tentacleFishSpeedMultiplier = 1.18;
      public double swiftCatchZoneMultiplier = 1.2;
      public double swiftFishSpeedMultiplier = 1.05;
      public double steelLeaderCatchZoneMultiplier = 0.9;
      public double steelLeaderFishSpeedMultiplier = 1.05;
      public double seafarersRareWeightMultiplier = 1.35;
      public double kujiraOceanCrateMultiplier = 1.2;
      public double sharkToothPredatoryWeightMultiplier = 2.5;
      public double sharkToothSmallFishWeightMultiplier = 0.35;
      public boolean leviathanBaitFishOnly = true;
      public int leviathanBaitFishSelectionLuckBonus = 15;
      public double leviathanBaitMinigameSpeedMultiplier = 1.2;
      public double leviathanBaitCatchZoneMultiplier = 0.8;
      public boolean enableChum = true;
      public boolean allowChumTriggeredSpawns = false;
      public int chumDuration = 90;
      public int chumParticleCount = 500;
      public int chumParticlePulseInterval = 10;
      public int chumSpawnCheckInterval = 200;
      public int chumSpawnChanceOneIn = 20;
      public int chumNearbySharkCap = 2;
      public int chumSpawnAttempts = 16;
      public double chumRadius = 48.0;
      public double chumScentStrength = 8.0;
   }
}
