/* RECONSTRUCTED SOURCE BASELINE */
package com.redslovesgames.tideborne.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import java.io.IOException;

/** Compatibility facade for the historical fishing-server configuration shape. */
public final class TideboundConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static volatile Values values = new Values();

   private TideboundConfig() {}

   public static Values get() {
      return values;
   }

   public static synchronized Result load() {
      return load(false);
   }

   public static synchronized Result reloadBalance() {
      return load(true);
   }

   public static synchronized Result applyBalanceJson(String json) {
      try {
         Values requested = parse(json);
         Values previous = values;
         Values validated = validate(requested);
         validated.enableMythsCompat = previous.enableMythsCompat;
         validated.enableApexCompat = previous.enableApexCompat;
         Result saved = write(validated);
         if (saved.success()) values = validated;
         return saved;
      } catch (RuntimeException exception) {
         FishingGameplayInitializer.LOGGER.warn("Rejected invalid Tideborne fishing settings", exception);
         return Result.failure("Settings contain an invalid value.");
      }
   }

   public static String settingsJson() {
      return GSON.toJson(values);
   }

   public static String toJson(Values input) {
      return GSON.toJson(input);
   }

   public static Values valuesFromJson(String json) {
      try {
         return validate(parse(json));
      } catch (RuntimeException ignored) {
         return new Values();
      }
   }

   private static Result load(boolean preserveStartupCompat) {
      try {
         JsonObject section = TideborneConfigStore.readSection(TideborneConfigStore.FISHING_SERVER);
         Values loaded = section == null ? new Values() : parse(section.toString());
         Values validated = validate(loaded);
         if (preserveStartupCompat) {
            validated.enableMythsCompat = values.enableMythsCompat;
            validated.enableApexCompat = values.enableApexCompat;
         }
         Result saved = write(validated);
         if (saved.success()) values = validated;
         return saved.success() ? Result.success("Configuration loaded.") : saved;
      } catch (Exception exception) {
         FishingGameplayInitializer.LOGGER.error("Could not load Tideborne fishing configuration", exception);
         return Result.failure("Configuration file is invalid; kept the previous settings.");
      }
   }

   private static Values parse(String json) {
      JsonObject object = JsonParser.parseString(json).getAsJsonObject();
      migrate(object, "minimumSharkHungerForFishTarget", "maximumFullnessForFoodTargeting");
      migrate(object, "rareFishScentMultiplier", "strongSharkFoodScentMultiplier");
      migrate(object, "steelLeaderTheftPreventionChance", "steelLeaderCatchLossPreventionChance");
      migrate(object, "leviathanBaitDifficultyMultiplier", "leviathanBaitMinigameSpeedMultiplier");
      return GSON.fromJson(object, Values.class);
   }

   private static void migrate(JsonObject object, String oldKey, String newKey) {
      if (!object.has(newKey) && object.has(oldKey)) object.add(newKey, object.get(oldKey));
   }

   public static Values validate(Values v) {
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
      migrateLegacyBalance(v);
      v.sharkFishDetectionRadius = clamp(v.sharkFishDetectionRadius, 8, 96);
      v.maximumFullnessForFoodTargeting = clamp(v.maximumFullnessForFoodTargeting, 0, 10000);
      v.chumDuration = clamp(v.chumDuration, 1, 3600);
      v.chumRadius = clamp(v.chumRadius, 8, 96);
      v.chumParticleCount = clamp(v.chumParticleCount, 0, 1000);
      v.chumParticlePulseInterval = clamp(v.chumParticlePulseInterval, 1, 1200);
      v.chumScentStrength = clamp(v.chumScentStrength, 0, 64);
      v.chumSpawnCheckInterval = clamp(v.chumSpawnCheckInterval, 20, 72000);
      v.chumSpawnChanceOneIn = clamp(v.chumSpawnChanceOneIn, 1, 100000);
      v.chumNearbySharkCap = clamp(v.chumNearbySharkCap, 0, 32);
      v.chumSpawnAttempts = clamp(v.chumSpawnAttempts, 1, 128);
      v.largeFishScentMultiplier = clamp(v.largeFishScentMultiplier, 0, 10);
      v.strongSharkFoodScentMultiplier = clamp(v.strongSharkFoodScentMultiplier, 0, 10);
      v.sharkPreyAcquisitionChance = clamp(v.sharkPreyAcquisitionChance, 0, 1);
      v.leviathanBaitFishSelectionLuckBonus = clamp(v.leviathanBaitFishSelectionLuckBonus, 0, 100);
      v.leviathanBaitMinigameSpeedMultiplier = clamp(v.leviathanBaitMinigameSpeedMultiplier, .1, 5);
      v.leviathanBaitCatchZoneMultiplier = clamp(v.leviathanBaitCatchZoneMultiplier, .05, 1);
      v.tentacleCatchZoneMultiplier = clamp(v.tentacleCatchZoneMultiplier, .1, 3);
      v.tentacleFishSpeedMultiplier = clamp(v.tentacleFishSpeedMultiplier, .1, 3);
      v.swiftCatchZoneMultiplier = clamp(v.swiftCatchZoneMultiplier, .1, 3);
      v.swiftFishSpeedMultiplier = clamp(v.swiftFishSpeedMultiplier, .1, 3);
      v.steelLeaderCatchZoneMultiplier = clamp(v.steelLeaderCatchZoneMultiplier, .1, 3);
      v.steelLeaderFishSpeedMultiplier = clamp(v.steelLeaderFishSpeedMultiplier, .1, 3);
      v.steelLeaderCatchLossPreventionChance = clamp(v.steelLeaderCatchLossPreventionChance, 0, .95);
      v.seafarersRareWeightMultiplier = clamp(v.seafarersRareWeightMultiplier, 0, 10);
      v.kujiraOceanCrateMultiplier = clamp(v.kujiraOceanCrateMultiplier, 0, 10);
      v.sharkToothPredatoryWeightMultiplier = clamp(v.sharkToothPredatoryWeightMultiplier, 0, 10);
      v.sharkToothSmallFishWeightMultiplier = clamp(v.sharkToothSmallFishWeightMultiplier, 0, 10);
      v.sharkTheftBaseChance = clamp(v.sharkTheftBaseChance, 0, 1);
      v.sharkTheftScentChancePerStrength = clamp(v.sharkTheftScentChancePerStrength, 0, 1);
      v.sharkTheftScentBonusCap = clamp(v.sharkTheftScentBonusCap, 0, 1);
      v.sharkTheftLargeFishBonus = clamp(v.sharkTheftLargeFishBonus, 0, 1);
      v.sharkTheftTunaBonus = clamp(v.sharkTheftTunaBonus, 0, 1);
      v.sharkTheftMaximumChance = clamp(v.sharkTheftMaximumChance, 0, 1);
      v.sharkPreyCooldown = clamp(v.sharkPreyCooldown, 0, 72000);
      v.sharkScanInterval = clamp(v.sharkScanInterval, 5, 1200);
      v.sharkFoodSatiation = clamp(v.sharkFoodSatiation, 1, 10000);
      return v;
   }

   private static void migrateLegacyBalance(Values v) {
      if (v.steelLeaderCatchLossPreventionChance == .9 && v.steelLeaderCatchZoneMultiplier == .9 && v.steelLeaderFishSpeedMultiplier == 1.05) {
         v.steelLeaderCatchLossPreventionChance = .55;
         v.steelLeaderCatchZoneMultiplier = .95;
         v.steelLeaderFishSpeedMultiplier = 1.03;
      }
      if (v.tentacleCatchZoneMultiplier == 1.45 && v.tentacleFishSpeedMultiplier == 1.18) {
         v.tentacleCatchZoneMultiplier = 1.32;
         v.tentacleFishSpeedMultiplier = 1.16;
      }
      if (v.swiftCatchZoneMultiplier == 1.2 && v.swiftFishSpeedMultiplier == 1.05) {
         v.swiftCatchZoneMultiplier = 1.16;
         v.swiftFishSpeedMultiplier = 1.08;
      }
      if (v.sharkToothPredatoryWeightMultiplier == 2.5 && v.sharkToothSmallFishWeightMultiplier == .35) {
         v.sharkToothPredatoryWeightMultiplier = 2.0;
         v.sharkToothSmallFishWeightMultiplier = .45;
      }
      if (v.kujiraOceanCrateMultiplier == 1.2) v.kujiraOceanCrateMultiplier = 1.3;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static void finite(double value, String name) {
      if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite");
   }

   private static Result write(Values input) {
      try {
         TideborneConfigStore.writeSection(TideborneConfigStore.FISHING_SERVER, GSON.toJsonTree(input));
         return Result.success("Configuration saved.");
      } catch (IOException exception) {
         FishingGameplayInitializer.LOGGER.error("Could not write Tideborne fishing config", exception);
         return Result.failure("Could not write the configuration file.");
      }
   }

   public record Result(boolean success, String message) {
      public static Result success(String message) { return new Result(true, message); }
      public static Result failure(String message) { return new Result(false, message); }
   }

   public static final class Values {
      public boolean enableMythsCompat = true, enableApexCompat = true, debugLogging = false,
         enableSharkFishAttraction = true, enableSharkFishPredation = true;
      public double sharkFishDetectionRadius = 48, sharkPreyAcquisitionChance = .35;
      public int maximumFullnessForFoodTargeting = 40, sharkPreyCooldown = 200, sharkScanInterval = 20, sharkFoodSatiation = 30;
      public boolean enableSharkCatchLoss = true;
      public double sharkTheftBaseChance = .005, sharkTheftScentChancePerStrength = .012, sharkTheftScentBonusCap = .12,
         sharkTheftLargeFishBonus = .05, sharkTheftTunaBonus = .1, sharkTheftMaximumChance = .25,
         largeFishScentMultiplier = 2, strongSharkFoodScentMultiplier = 2.5;
      public double steelLeaderCatchLossPreventionChance = .55, tentacleCatchZoneMultiplier = 1.32,
         tentacleFishSpeedMultiplier = 1.16, swiftCatchZoneMultiplier = 1.16, swiftFishSpeedMultiplier = 1.08,
         steelLeaderCatchZoneMultiplier = .95, steelLeaderFishSpeedMultiplier = 1.03,
         seafarersRareWeightMultiplier = 1.35, kujiraOceanCrateMultiplier = 1.3,
         sharkToothPredatoryWeightMultiplier = 2.0, sharkToothSmallFishWeightMultiplier = .45;
      public boolean leviathanBaitFishOnly = true;
      public int leviathanBaitFishSelectionLuckBonus = 15;
      public double leviathanBaitMinigameSpeedMultiplier = 1.2, leviathanBaitCatchZoneMultiplier = .8;
      public boolean enableChum = true, allowChumTriggeredSpawns = false;
      public int chumDuration = 90, chumParticleCount = 500, chumParticlePulseInterval = 10,
         chumSpawnCheckInterval = 200, chumSpawnChanceOneIn = 20, chumNearbySharkCap = 2, chumSpawnAttempts = 16;
      public double chumRadius = 48, chumScentStrength = 8;
   }
}
