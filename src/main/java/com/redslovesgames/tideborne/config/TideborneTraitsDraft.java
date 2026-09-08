/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.config;

import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.trait.FishMutation;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class TideborneTraitsDraft {
   public final EnumMap<FishMutation, Double> odds = new EnumMap<>(FishMutation.class);
   public boolean redistributeIneligibleOdds;
   public double dwarfMin;
   public double dwarfMax;
   public double giantMin;
   public double giantMax;
   public double parasiteMin;
   public double parasiteMax;
   public double perfectMinPercentile;
   public double perfectMaxPercentile;
   public int dynamicTextureCacheMaximum;
   public int conversionXpCost;
   public final List<Double> capacityMultipliers = new ArrayList<>();
   public final List<Integer> capacityXpCosts = new ArrayList<>();
   public final LinkedHashMap<String, Integer> featureXpCosts = new LinkedHashMap<>();
   public final LinkedHashMap<String, Boolean> protectionDefaults = new LinkedHashMap<>();
   public boolean sharedDiscovery;
   public boolean debugLogging;

   private TideborneTraitsDraft() {
   }

   public static TideborneTraitsDraft from(TideTraitsConfigManager.Settings var0) {
      TideborneTraitsDraft var1 = new TideborneTraitsDraft();

      for (FishMutation var3 : FishMutation.mutations()) {
         var1.odds.put(var3, var0.mutations().probability(var3));
      }

      var1.redistributeIneligibleOdds = var0.mutations().redistributeIneligibleMutationOdds();
      var1.dwarfMin = var0.mutations().dwarfLengthMultiplier().minInclusive();
      var1.dwarfMax = var0.mutations().dwarfLengthMultiplier().maxInclusive();
      var1.giantMin = var0.mutations().giantLengthMultiplier().minInclusive();
      var1.giantMax = var0.mutations().giantLengthMultiplier().maxInclusive();
      var1.parasiteMin = var0.mutations().parasiteLengthMultiplier().minInclusive();
      var1.parasiteMax = var0.mutations().parasiteLengthMultiplier().maxInclusive();
      var1.perfectMinPercentile = var0.mutations().perfectSpecimenNormalPercentile().minInclusive();
      var1.perfectMaxPercentile = var0.mutations().perfectSpecimenNormalPercentile().maxInclusive();
      var1.dynamicTextureCacheMaximum = var0.rendering().dynamicTextureCacheMaximum();
      var1.conversionXpCost = var0.satchel().conversionXpCost();
      var1.capacityMultipliers.addAll(var0.satchel().capacityMultipliers());
      var1.capacityXpCosts.addAll(var0.satchel().capacityXpCosts());
      var1.featureXpCosts.putAll(var0.satchel().featureXpCosts());
      var1.protectionDefaults.putAll(var0.satchel().protectionDefaults());
      var1.sharedDiscovery = var0.sharedDiscovery();
      var1.debugLogging = var0.debugLogging();
      return var1;
   }

   public TideTraitsConfigManager.Settings toSettings() {
      TideTraitsConfig.Builder var1 = TideTraitsConfig.builder()
         .clearMutationProbabilities()
         .redistributeIneligibleMutationOdds(this.redistributeIneligibleOdds)
         .dwarfLengthMultiplier(this.dwarfMin, this.dwarfMax)
         .giantLengthMultiplier(this.giantMin, this.giantMax)
         .parasiteLengthMultiplier(this.parasiteMin, this.parasiteMax)
         .perfectSpecimenNormalPercentile(this.perfectMinPercentile, this.perfectMaxPercentile);

      for (Entry var3 : this.odds.entrySet()) {
         var1.probability((FishMutation)var3.getKey(), (Double)var3.getValue());
      }

      return new TideTraitsConfigManager.Settings(
         var1.build(),
         new TideTraitsConfigManager.RenderingSettings(this.dynamicTextureCacheMaximum),
         new TideTraitsConfigManager.SatchelSettings(
            this.conversionXpCost,
            List.copyOf(this.capacityMultipliers),
            List.copyOf(this.capacityXpCosts),
            Map.copyOf(this.featureXpCosts),
            Map.copyOf(this.protectionDefaults)
         ),
         this.sharedDiscovery,
         this.debugLogging
      );
   }
}
