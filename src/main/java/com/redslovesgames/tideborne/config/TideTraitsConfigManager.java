/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redslovesgames.tideborne.fishing.TideTraits;
import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import com.redslovesgames.tideborne.satchel.SatchelProtectionRule;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compatibility facade for the historical Tide Traits settings shape. */
public final class TideTraitsConfigManager {
   private static volatile Settings current = Settings.defaults();

   private TideTraitsConfigManager() {
   }

   public static Settings current() {
      return current;
   }

   public static synchronized Settings load() {
      Settings previous = current;
      try {
         JsonObject root = TideborneConfigStore.readSection(TideborneConfigStore.TRAITS);
         if (root == null) {
            current = Settings.defaults();
            TideborneConfigStore.writeSection(TideborneConfigStore.TRAITS, toJson(current));
         } else {
            current = parse(root);
         }
      } catch (IOException | RuntimeException exception) {
         TideTraits.LOGGER.warn("Could not load Tideborne traits config; retaining the last valid settings", exception);
         current = previous;
      }
      return current;
   }

   public static synchronized Settings save(Settings settings) {
      try {
         JsonObject json = toJson(settings);
         Settings validated = parse(json);
         TideborneConfigStore.writeSection(TideborneConfigStore.TRAITS, json);
         current = validated;
      } catch (IOException | RuntimeException exception) {
         throw new IllegalStateException("Could not save Tideborne traits configuration", exception);
      }
      return current;
   }

   private static Settings parse(JsonObject root) {
      Settings defaults = Settings.defaults();
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
      int cacheMaximum = integer(object(root, "rendering"), "dynamic_texture_cache_maximum", 256, 16, 2048);
      int conversionCost = integer(object(root, "satchel"), "conversion_xp_cost", 100, 0, 1000000);
      JsonObject capacity = object(root, "satchel_capacity");
      List<Double> multipliers = doubleList(capacity, "multipliers", List.of(1.0, 1.5, 2.0, 3.0));
      List<Integer> capacityCosts = intList(capacity, "xp_costs", List.of(0, 150, 450, 1000));
      if (multipliers.size() != 4 || capacityCosts.size() != 4
         || multipliers.stream().anyMatch(value -> !Double.isFinite(value) || value <= 0.0)
         || capacityCosts.stream().anyMatch(value -> value < 0)) {
         throw new IllegalArgumentException("satchel capacity arrays must contain four positive/non-negative values");
      }

      Map<String, Integer> featureCosts = new LinkedHashMap<>(defaults.satchel().featureXpCosts());
      JsonObject xp = object(root, "xp_costs");
      featureCosts.replaceAll((id, fallback) -> integer(xp, id, fallback, 0, 1000000));
      Map<String, Boolean> protectionDefaults = new LinkedHashMap<>(defaults.satchel().protectionDefaults());
      JsonObject protection = object(root, "protection_defaults");
      protectionDefaults.replaceAll((id, fallback) -> bool(protection, id, fallback));
      boolean sharedDiscovery = bool(object(root, "multiplayer"), "shared_discovery", true);
      boolean debugLogging = bool(object(root, "debug"), "logging", false);
      return new Settings(
         mutationConfig,
         new RenderingSettings(cacheMaximum),
         new SatchelSettings(conversionCost, multipliers, capacityCosts, featureCosts, protectionDefaults),
         sharedDiscovery,
         debugLogging
      );
   }

   private static JsonObject toJson(Settings settings) {
      JsonObject root = new JsonObject();
      root.addProperty("_documentation", "Server authoritative. Odds are marginal probabilities in [0,1]. Restart/reload the server after editing.");
      JsonObject mutations = new JsonObject();
      mutations.addProperty("_comment", "One draw selects zero or one Condition; Giant/Dwarf Body Type and Perfect Specimen quality are resolved independently.");
      mutations.addProperty("redistribute_ineligible_odds", settings.mutations().redistributeIneligibleMutationOdds());
      JsonObject odds = new JsonObject();
      for (FishMutation mutation : FishMutation.mutations()) {
         odds.addProperty(mutation.serializedName(), settings.mutations().probability(mutation));
      }
      mutations.add("odds", odds);
      root.add("mutations", mutations);

      JsonObject sizes = new JsonObject();
      sizes.addProperty("_comment", "Inclusive min/max ranges; Body Type finalizes Giant/Dwarf length, Parasite keeps its configured size effect, and Perfect Specimen does not remap size.");
      sizes.add("dwarf_multiplier", array(settings.mutations().dwarfLengthMultiplier().minInclusive(), settings.mutations().dwarfLengthMultiplier().maxInclusive()));
      sizes.add("giant_multiplier", array(settings.mutations().giantLengthMultiplier().minInclusive(), settings.mutations().giantLengthMultiplier().maxInclusive()));
      sizes.add("parasite_multiplier", array(settings.mutations().parasiteLengthMultiplier().minInclusive(), settings.mutations().parasiteLengthMultiplier().maxInclusive()));
      sizes.add("perfect_normal_percentile", array(settings.mutations().perfectSpecimenNormalPercentile().minInclusive(), settings.mutations().perfectSpecimenNormalPercentile().maxInclusive()));
      root.add("mutation_size", sizes);

      JsonObject rendering = new JsonObject();
      rendering.addProperty("dynamic_texture_cache_maximum", settings.rendering().dynamicTextureCacheMaximum());
      root.add("rendering", rendering);
      JsonObject satchel = new JsonObject();
      satchel.addProperty("conversion_xp_cost", settings.satchel().conversionXpCost());
      root.add("satchel", satchel);
      JsonObject capacity = new JsonObject();
      capacity.add("multipliers", toArray(settings.satchel().capacityMultipliers()));
      capacity.add("xp_costs", toArray(settings.satchel().capacityXpCosts()));
      root.add("satchel_capacity", capacity);
      JsonObject xp = new JsonObject();
      settings.satchel().featureXpCosts().forEach(xp::addProperty);
      root.add("xp_costs", xp);
      JsonObject protection = new JsonObject();
      settings.satchel().protectionDefaults().forEach(protection::addProperty);
      root.add("protection_defaults", protection);
      JsonObject multiplayer = new JsonObject();
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
      if (result < min || result > max) {
         throw new IllegalArgumentException(key + " must be in [" + min + ", " + max + "]");
      }
      return result;
   }

   private static double[] range(JsonObject object, String key, double defaultMin, double defaultMax) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() && value.getAsJsonArray().size() == 2
         ? new double[]{value.getAsJsonArray().get(0).getAsDouble(), value.getAsJsonArray().get(1).getAsDouble()}
         : new double[]{defaultMin, defaultMax};
   }

   private static List<Double> doubleList(JsonObject object, String key, List<Double> fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() ? value.getAsJsonArray().asList().stream().map(JsonElement::getAsDouble).toList() : fallback;
   }

   private static List<Integer> intList(JsonObject object, String key, List<Integer> fallback) {
      JsonElement value = object.get(key);
      return value != null && value.isJsonArray() ? value.getAsJsonArray().asList().stream().map(JsonElement::getAsInt).toList() : fallback;
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
         return featureXpCosts.getOrDefault(id, fallback);
      }
   }

   public record Settings(
      TideTraitsConfig mutations,
      RenderingSettings rendering,
      SatchelSettings satchel,
      boolean sharedDiscovery,
      boolean debugLogging
   ) {
      public static Settings defaults() {
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
         return new Settings(
            TideTraitsConfig.defaults(),
            new RenderingSettings(256),
            new SatchelSettings(100, List.of(1.0, 1.5, 2.0, 3.0), List.of(0, 150, 450, 1000), costs, protectionDefaults),
            true,
            false
         );
      }
   }
}
