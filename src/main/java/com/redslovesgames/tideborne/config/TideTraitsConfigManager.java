/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redslovesgames.tideborne.fishing.TideTraits;
import com.redslovesgames.tideborne.satchel.SatchelProtectionRule;
import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class TideTraitsConfigManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String FILE_NAME = "tide_traits.json";
   private static volatile TideTraitsConfigManager.Settings current = TideTraitsConfigManager.Settings.defaults();

   private TideTraitsConfigManager() {
   }

   public static TideTraitsConfigManager.Settings current() {
      return current;
   }

   public static synchronized TideTraitsConfigManager.Settings load() {
      Path path = FabricLoader.getInstance().getConfigDir().resolve("tide_traits.json");
      if (!Files.exists(path)) {
         current = TideTraitsConfigManager.Settings.defaults();

         try {
            Files.createDirectories(path.getParent());

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
               GSON.toJson(defaultJson(current), writer);
            }
         } catch (IOException exception) {
            TideTraits.LOGGER.warn("Could not write default Tide Traits config {}; using defaults", path, exception);
         }

         return current;
      } else {
         try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
               throw new IllegalArgumentException("root must be a JSON object");
            }

            current = parse(parsed.getAsJsonObject());
         } catch (IOException | RuntimeException exception) {
            TideTraits.LOGGER.warn("Invalid Tide Traits config {}; using validated defaults", path, exception);
            current = TideTraitsConfigManager.Settings.defaults();
         }

         return current;
      }
   }

   private static TideTraitsConfigManager.Settings parse(JsonObject root) {
      TideTraitsConfigManager.Settings defaults = TideTraitsConfigManager.Settings.defaults();
      JsonObject mutations = object(root, "mutations");
      TideTraitsConfig.Builder mutationBuilder = TideTraitsConfig.builder()
         .redistributeIneligibleMutationOdds(bool(mutations, "redistribute_ineligible_odds", false));
      JsonObject odds = object(mutations, "odds");

      for (FishMutation mutation : FishMutation.mutations()) {
         mutationBuilder.probability(mutation, number(odds, mutation.serializedName(), defaults.mutations().probability(mutation)));
      }

      JsonObject mutationSize = object(root, "mutation_size");
      double[] dwarf = range(mutationSize, "dwarf_multiplier", 0.65, 0.82);
      double[] giant = range(mutationSize, "giant_multiplier", 1.2, 1.45);
      double[] parasite = range(mutationSize, "parasite_multiplier", 0.9, 0.97);
      double[] perfect = range(mutationSize, "perfect_normal_percentile", 75.0, 95.0);
      TideTraitsConfig mutationConfig = mutationBuilder.dwarfLengthMultiplier(dwarf[0], dwarf[1])
         .giantLengthMultiplier(giant[0], giant[1])
         .parasiteLengthMultiplier(parasite[0], parasite[1])
         .perfectSpecimenNormalPercentile(perfect[0], perfect[1])
         .build();
      JsonObject rendering = object(root, "rendering");
      int cacheMaximum = integer(rendering, "dynamic_texture_cache_maximum", 256, 16, 2048);
      JsonObject satchel = object(root, "satchel");
      int conversionCost = integer(satchel, "conversion_xp_cost", 100, 0, 1000000);
      JsonObject capacity = object(root, "satchel_capacity");
      List<Double> multipliers = doubleList(capacity, "multipliers", List.of(1.0, 1.5, 2.0, 3.0));
      List<Integer> capacityCosts = intList(capacity, "xp_costs", List.of(0, 150, 450, 1000));
      if (multipliers.size() == 4
         && capacityCosts.size() == 4
         && !multipliers.stream().anyMatch(value -> !Double.isFinite(value) || value <= 0.0)
         && !capacityCosts.stream().anyMatch(value -> value < 0)) {
         Map<String, Integer> featureCosts = new LinkedHashMap<>(defaults.satchel().featureXpCosts());
         JsonObject xp = object(root, "xp_costs");
         featureCosts.replaceAll((id, fallback) -> integer(xp, id, fallback, 0, 1000000));
         Map<String, Boolean> protectionDefaults = new LinkedHashMap<>(defaults.satchel().protectionDefaults());
         JsonObject protection = object(root, "protection_defaults");
         protectionDefaults.replaceAll((id, fallback) -> bool(protection, id, fallback));
         JsonObject multiplayer = object(root, "multiplayer");
         boolean sharedDiscovery = bool(multiplayer, "shared_discovery", true);
         JsonObject debug = object(root, "debug");
         boolean debugLogging = bool(debug, "logging", false);
         return new TideTraitsConfigManager.Settings(
            mutationConfig,
            new TideTraitsConfigManager.RenderingSettings(cacheMaximum),
            new TideTraitsConfigManager.SatchelSettings(conversionCost, multipliers, capacityCosts, featureCosts, protectionDefaults),
            sharedDiscovery,
            debugLogging
         );
      } else {
         throw new IllegalArgumentException("satchel capacity arrays must contain four positive/non-negative values");
      }
   }

   private static JsonObject defaultJson(TideTraitsConfigManager.Settings settings) {
      JsonObject root = new JsonObject();
      root.addProperty("_documentation", "Server authoritative. Odds are marginal probabilities in [0,1]. Restart/reload the server after editing.");
      JsonObject mutations = new JsonObject();
      mutations.addProperty(
         "_comment", "One draw selects zero or one Condition; Giant/Dwarf Body Type and Perfect Specimen quality are resolved independently."
      );
      mutations.addProperty("redistribute_ineligible_odds", false);
      JsonObject odds = new JsonObject();

      for (FishMutation mutation : FishMutation.mutations()) {
         odds.addProperty(mutation.serializedName(), settings.mutations().probability(mutation));
      }

      mutations.add("odds", odds);
      root.add("mutations", mutations);
      JsonObject sizes = new JsonObject();
      sizes.addProperty(
         "_comment",
         "Inclusive min/max ranges; Body Type finalizes Giant/Dwarf length, Parasite keeps its configured size effect, and Perfect Specimen does not remap size."
      );
      sizes.add("dwarf_multiplier", array(0.65, 0.82));
      sizes.add("giant_multiplier", array(1.2, 1.45));
      sizes.add("parasite_multiplier", array(0.9, 0.97));
      sizes.add("perfect_normal_percentile", array(75.0, 95.0));
      root.add("mutation_size", sizes);
      JsonObject rendering = new JsonObject();
      rendering.addProperty("_comment", "Maximum generated client textures; reload/disconnect releases them.");
      rendering.addProperty("dynamic_texture_cache_maximum", settings.rendering().dynamicTextureCacheMaximum());
      root.add("rendering", rendering);
      JsonObject satchel = new JsonObject();
      satchel.addProperty("_comment", "Sneak-use a Tide Fish Satchel to convert it in place without losing contents.");
      satchel.addProperty("conversion_xp_cost", settings.satchel().conversionXpCost());
      root.add("satchel", satchel);
      JsonObject capacity = new JsonObject();
      capacity.addProperty("_comment", "Four levels: base and Capacity I-III; capacity always multiplies Tide's resolved base.");
      capacity.add("multipliers", toArray(settings.satchel().capacityMultipliers()));
      capacity.add("xp_costs", toArray(settings.satchel().capacityXpCosts()));
      root.add("satchel_capacity", capacity);
      JsonObject xp = new JsonObject();
      xp.addProperty("_comment", "Raw vanilla XP points, not experience levels.");
      settings.satchel().featureXpCosts().forEach(xp::addProperty);
      root.add("xp_costs", xp);
      JsonObject protection = new JsonObject();
      protection.addProperty("_comment", "Initial automatic Trophy Lock rules. Copied into each satchel when Trophy Lock is purchased.");
      settings.satchel().protectionDefaults().forEach(protection::addProperty);
      root.add("protection_defaults", protection);
      JsonObject multiplayer = new JsonObject();
      multiplayer.addProperty("_comment", "Uses the optional Tide Multiplayer Extras / FTB Teams sidecar when available.");
      multiplayer.addProperty("shared_discovery", settings.sharedDiscovery());
      root.add("multiplayer", multiplayer);
      JsonObject debug = new JsonObject();
      debug.addProperty("logging", settings.debugLogging());
      root.add("debug", debug);
      return root;
   }

   private static JsonObject object(JsonObject parent, String key) {
      JsonElement value = parent.get(key);
      return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
   }

   private static boolean bool(JsonObject object, String key, boolean fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonPrimitive() ? value.getAsBoolean() : fallback;
   }

   private static double number(JsonObject object, String key, double fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonPrimitive() ? value.getAsDouble() : fallback;
   }

   private static int integer(JsonObject object, String key, int fallback, int min, int max) {
      JsonElement value = object.get(key);
      int result = value != null && value.isJsonPrimitive() ? value.getAsInt() : fallback;
      if (result >= min && result <= max) {
         return result;
      } else {
         throw new IllegalArgumentException(key + " must be in [" + min + ", " + max + "]");
      }
   }

   private static double[] range(JsonObject object, String key, double defaultMin, double defaultMax) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() && value.getAsJsonArray().size() == 2
         ? new double[]{value.getAsJsonArray().get(0).getAsDouble(), value.getAsJsonArray().get(1).getAsDouble()}
         : new double[]{defaultMin, defaultMax};
   }

   private static List<Double> doubleList(JsonObject object, String key, List<Double> fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() ? value.getAsJsonArray().asList().stream().<Double>map(JsonElement::getAsDouble).toList() : fallback;
   }

   private static List<Integer> intList(JsonObject object, String key, List<Integer> fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() ? value.getAsJsonArray().asList().stream().<Integer>map(JsonElement::getAsInt).toList() : fallback;
   }

   private static JsonArray array(double min, double max) {
      JsonArray array = new JsonArray();
      array.add(min);
      array.add(max);
      return array;
   }

   private static JsonArray toArray(List<? extends Number> values) {
      JsonArray array = new JsonArray();
      values.forEach(array::add);
      return array;
   }

   public record RenderingSettings(int dynamicTextureCacheMaximum) {
   }

   public record SatchelSettings(
      int conversionXpCost,
      List<Double> capacityMultipliers,
      List<Integer> capacityXpCosts,
      Map<String, Integer> featureXpCosts,
      Map<String, Boolean> protectionDefaults
   ) {
      public SatchelSettings {
         capacityMultipliers = List.copyOf(capacityMultipliers);
         capacityXpCosts = List.copyOf(capacityXpCosts);
         featureXpCosts = Collections.unmodifiableMap(new LinkedHashMap<>(featureXpCosts));
         protectionDefaults = Collections.unmodifiableMap(new LinkedHashMap<>(protectionDefaults));
      }

      public int featureCost(String id, int fallback) {
         return this.featureXpCosts.getOrDefault(id, fallback);
      }
   }

   public record Settings(
      TideTraitsConfig mutations,
      TideTraitsConfigManager.RenderingSettings rendering,
      TideTraitsConfigManager.SatchelSettings satchel,
      boolean sharedDiscovery,
      boolean debugLogging
   ) {
      public static TideTraitsConfigManager.Settings defaults() {
         Map<String, Integer> costs = new LinkedHashMap<>();
         costs.put("tackle_organizer", 100);
         costs.put("auto_stow", 200);
         costs.put("record_keeper", 250);
         costs.put("trait_scanner", 300);
         costs.put("trophy_lock", 350);
         costs.put("shared_ledger", 400);
         Map<String, Boolean> protectionDefaults = new LinkedHashMap<>();

         for (SatchelProtectionRule rule : SatchelProtectionRule.values()) {
            protectionDefaults.put(rule.id(), true);
         }

         return new TideTraitsConfigManager.Settings(
            TideTraitsConfig.defaults(),
            new TideTraitsConfigManager.RenderingSettings(256),
            new TideTraitsConfigManager.SatchelSettings(100, List.of(1.0, 1.5, 2.0, 3.0), List.of(0, 150, 450, 1000), costs, protectionDefaults),
            true,
            false
         );
      }
   }
}
