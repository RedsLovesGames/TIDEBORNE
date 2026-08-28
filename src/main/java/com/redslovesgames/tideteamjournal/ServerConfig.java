/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class ServerConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static volatile ServerConfig.Values values = ServerConfig.Values.defaults();

   private ServerConfig() {
   }

   public static ServerConfig.Values get() {
      return values;
   }

   public static boolean load() {
      ServerConfig.Values previous = values;
      Path path = path();

      try {
         if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(ServerConfig.Values.defaults()));
         }

         ServerConfig.Values parsed = (ServerConfig.Values)GSON.fromJson(Files.readString(path), ServerConfig.Values.class);
         values = validate(parsed);
         return true;
      } catch (IOException | JsonParseException | IllegalArgumentException exception) {
         values = previous;
         TideTeamJournal.LOGGER.error("Could not load {}; retaining the last valid settings", path, exception);
         return false;
      }
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("tide_team_journal-server.json");
   }

   static ServerConfig.Values validate(ServerConfig.Values raw) {
      if (raw == null) {
         throw new IllegalArgumentException("Configuration is empty");
      }

      raw.historyLimit = Math.max(0, Math.min(10000, raw.historyLimit));
      raw.visibleMetrics = raw.visibleMetrics != null && !raw.visibleMetrics.isEmpty()
         ? raw.visibleMetrics.stream().filter(metric -> List.of("catches", "species", "record_events", "active_records").contains(metric)).distinct().toList()
         : List.of("catches", "species", "record_events", "active_records");
      if (raw.visibleMetrics.isEmpty()) {
         raw.visibleMetrics = List.of("catches", "species", "record_events", "active_records");
      }

      raw.repairMinimumRank = "owner".equalsIgnoreCase(raw.repairMinimumRank) ? "owner" : "officer";
      raw.claimAllMinimumRank = "officer".equalsIgnoreCase(raw.claimAllMinimumRank) ? "officer" : "owner";
      raw.bobberBonuses = raw.bobberBonuses == null ? new LinkedHashMap<>() : raw.bobberBonuses;
      Map<String, BobberBonuses.Bonus> validated = new LinkedHashMap<>();
      raw.bobberBonuses.forEach((id, bonus) -> {
         if (Identifier.tryParse(id) != null && bonus != null) {
            validated.put(id, clamp(bonus));
         } else {
            TideTeamJournal.LOGGER.warn("Ignoring invalid bobber bonus entry {}", id);
         }
      });
      raw.bobberBonuses = validated;
      raw.fallbackBobberBonus = clamp(raw.fallbackBobberBonus == null ? new BobberBonuses.Bonus(0, 1) : raw.fallbackBobberBonus);
      tideborneEnsureFishScoreMetric(raw);
      return raw;
   }

   private static BobberBonuses.Bonus clamp(BobberBonuses.Bonus bonus) {
      return new BobberBonuses.Bonus(Math.max(0, Math.min(10, bonus.luck())), Math.max(0, Math.min(10, bonus.lureSpeed())));
   }

   private static void tideborneEnsureFishScoreMetric(ServerConfig.Values config) {
      ArrayList<String> metrics = new ArrayList<>(config.visibleMetrics);
      if (!metrics.contains("fish_score")) {
         metrics.add("fish_score");
      }

      config.visibleMetrics = metrics;
   }

   public static final class Values {
      public boolean leaderboardEnabled = true;
      public boolean historyEnabled = true;
      public boolean contributionTracking = true;
      public boolean announcementsEnabled = true;
      public boolean recordBadgesEnabled = true;
      public boolean recordTooltipsEnabled = true;
      public boolean operatorBypass = true;
      public boolean membersMayClaimWithExactFish = true;
      public int historyLimit = 200;
      public String repairMinimumRank = "officer";
      public String claimAllMinimumRank = "owner";
      public List<String> visibleMetrics = List.of("catches", "species", "record_events", "active_records", "fish_score");
      public boolean trackDiscoveries = true;
      public boolean trackLargestRecords = true;
      public boolean trackSmallestRecords = true;
      public boolean trackRepairs = true;
      public boolean bobberBonusesEnabled = true;
      public BobberBonuses.Bonus fallbackBobberBonus = new BobberBonuses.Bonus(0, 1);
      public Map<String, BobberBonuses.Bonus> bobberBonuses = defaultBobbers();

      static ServerConfig.Values defaults() {
         return new ServerConfig.Values();
      }

      private static Map<String, BobberBonuses.Bonus> defaultBobbers() {
         Map<String, BobberBonuses.Bonus> result = new LinkedHashMap<>();

         for (String path : List.of(
            "red_bobber",
            "orange_bobber",
            "yellow_bobber",
            "lime_bobber",
            "green_bobber",
            "cyan_bobber",
            "light_blue_bobber",
            "blue_bobber",
            "purple_bobber",
            "magenta_bobber",
            "pink_bobber",
            "white_bobber",
            "light_gray_bobber",
            "gray_bobber",
            "black_bobber",
            "brown_bobber"
         )) {
            result.put("tide:" + path, new BobberBonuses.Bonus(0, 1));
         }

         result.put("tide:golden_apple_bobber", new BobberBonuses.Bonus(1, 1));
         result.put("tide:enchanted_golden_apple_bobber", new BobberBonuses.Bonus(2, 2));
         result.put("tide:iron_bobber", new BobberBonuses.Bonus(0, 2));
         result.put("tide:golden_bobber", new BobberBonuses.Bonus(2, 0));
         result.put("tide:diamond_bobber", new BobberBonuses.Bonus(1, 2));
         result.put("tide:netherite_bobber", new BobberBonuses.Bonus(2, 2));
         result.put("tide:amethyst_bobber", new BobberBonuses.Bonus(2, 0));
         result.put("tide:echo_bobber", new BobberBonuses.Bonus(0, 3));
         result.put("tide:chorus_bobber", new BobberBonuses.Bonus(1, 1));
         result.put("tide:feather_bobber", new BobberBonuses.Bonus(0, 3));
         result.put("tide:lichen_bobber", new BobberBonuses.Bonus(0, 2));
         result.put("tide:nautilus_bobber", new BobberBonuses.Bonus(2, 0));
         result.put("tide:pearl_bobber", new BobberBonuses.Bonus(1, 2));
         result.put("tide:heart_bobber", new BobberBonuses.Bonus(3, 0));
         result.put("tide:grassy_bobber", new BobberBonuses.Bonus(1, 1));
         result.put("tide:duck_bobber", new BobberBonuses.Bonus(0, 2));
         return result;
      }
   }
}
