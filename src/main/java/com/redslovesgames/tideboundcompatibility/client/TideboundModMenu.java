/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.network.TideboundSettingsUpdatePayload;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.util.function.Consumer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public final class TideboundModMenu implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return TideboundModMenu::create;
   }

   private static Screen create(Screen parent) {
      ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Tidebound Compatibility"));
      ConfigEntryBuilder entries = builder.entryBuilder();
      TideboundClientConfig.Values client = TideboundClientConfig.get();
      ConfigCategory display = builder.getOrCreateCategory(Text.literal("Client display"));
      display.addEntry(
         entries.startBooleanToggle(Text.literal("Show fishing HUD"), client.showFishingHud)
            .setDefaultValue(true)
            .setSaveConsumer(v -> client.showFishingHud = v)
            .build()
      );
      display.addEntry(
         entries.startBooleanToggle(Text.literal("Show advanced equipment stats (F3 + H)"), client.showEquipmentTooltips)
            .setDefaultValue(true)
            .setSaveConsumer(v -> client.showEquipmentTooltips = v)
            .build()
      );
      Runnable save = TideboundClientConfig::save;
      boolean operator = MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.hasPermissionLevel(2);
      if (operator && ClientTideboundSettings.available()) {
         TideboundConfig.Values values = categories(builder, entries);
         save = () -> {
            TideboundClientConfig.save();
            ClientPlayNetworking.send(new TideboundSettingsUpdatePayload(TideboundConfig.toJson(values)));
         };
      } else {
         builder.getOrCreateCategory(Text.literal("Server gameplay"))
            .addEntry(
               entries.startTextDescription(
                     Text.literal(
                        ClientTideboundSettings.available() ? "Only server operators can edit gameplay settings." : "Join a server first to receive settings."
                     )
                  )
                  .build()
            );
      }

      builder.setSavingRunnable(save);
      return builder.build();
   }

   private static TideboundConfig.Values categories(ConfigBuilder builder, ConfigEntryBuilder e) {
      TideboundConfig.Values v = TideboundConfig.valuesFromJson(ClientTideboundSettings.settingsJson());
      ConfigCategory gear = builder.getOrCreateCategory(Text.literal("Fishing Gear"));
      gear.addEntry(d(e, "Tentacle Line catch zone", v.tentacleCatchZoneMultiplier, x -> v.tentacleCatchZoneMultiplier = x));
      gear.addEntry(d(e, "Tentacle Line fish speed", v.tentacleFishSpeedMultiplier, x -> v.tentacleFishSpeedMultiplier = x));
      gear.addEntry(d(e, "Abaia Line catch zone", v.swiftCatchZoneMultiplier, x -> v.swiftCatchZoneMultiplier = x));
      gear.addEntry(d(e, "Abaia Line fish speed", v.swiftFishSpeedMultiplier, x -> v.swiftFishSpeedMultiplier = x));
      gear.addEntry(d(e, "Seafarer night legendary weight", v.seafarersRareWeightMultiplier, x -> v.seafarersRareWeightMultiplier = x));
      gear.addEntry(d(e, "Kujira ocean crate weight", v.kujiraOceanCrateMultiplier, x -> v.kujiraOceanCrateMultiplier = x));
      ConfigCategory bait = builder.getOrCreateCategory(Text.literal("Leviathan Bait"));
      bait.addEntry(
         e.startBooleanToggle(Text.literal("Force fish-only catches"), v.leviathanBaitFishOnly)
            .setSaveConsumer(x -> v.leviathanBaitFishOnly = x)
            .build()
      );
      bait.addEntry(i(e, "Fish-selection luck bonus", v.leviathanBaitFishSelectionLuckBonus, x -> v.leviathanBaitFishSelectionLuckBonus = x));
      bait.addEntry(d(e, "Minigame speed multiplier", v.leviathanBaitMinigameSpeedMultiplier, x -> v.leviathanBaitMinigameSpeedMultiplier = x));
      bait.addEntry(d(e, "Catch-zone multiplier", v.leviathanBaitCatchZoneMultiplier, x -> v.leviathanBaitCatchZoneMultiplier = x));
      ConfigCategory loss = builder.getOrCreateCategory(Text.literal("Shark Catch Loss"));
      loss.addEntry(
         e.startBooleanToggle(Text.literal("Enable abstract shark catch loss"), v.enableSharkCatchLoss)
            .setSaveConsumer(x -> v.enableSharkCatchLoss = x)
            .build()
      );
      loss.addEntry(percent(e, "Base chance", v.sharkTheftBaseChance, x -> v.sharkTheftBaseChance = x));
      loss.addEntry(percent(e, "Scent chance per strength", v.sharkTheftScentChancePerStrength, x -> v.sharkTheftScentChancePerStrength = x));
      loss.addEntry(percent(e, "Scent bonus cap", v.sharkTheftScentBonusCap, x -> v.sharkTheftScentBonusCap = x));
      loss.addEntry(percent(e, "Large-fish bonus", v.sharkTheftLargeFishBonus, x -> v.sharkTheftLargeFishBonus = x));
      loss.addEntry(percent(e, "Tuna bonus", v.sharkTheftTunaBonus, x -> v.sharkTheftTunaBonus = x));
      loss.addEntry(percent(e, "Maximum chance", v.sharkTheftMaximumChance, x -> v.sharkTheftMaximumChance = x));
      loss.addEntry(percent(e, "Steel Leader protection", v.steelLeaderCatchLossPreventionChance, x -> v.steelLeaderCatchLossPreventionChance = x));
      ConfigCategory ecosystem = builder.getOrCreateCategory(Text.literal("Shark Ecosystem"));
      ecosystem.addEntry(
         e.startBooleanToggle(Text.literal("Enable fish scent and attraction"), v.enableSharkFishAttraction)
            .setSaveConsumer(x -> v.enableSharkFishAttraction = x)
            .build()
      );
      ecosystem.addEntry(
         e.startBooleanToggle(Text.literal("Enable living Tide fish predation"), v.enableSharkFishPredation)
            .setSaveConsumer(x -> v.enableSharkFishPredation = x)
            .build()
      );
      ecosystem.addEntry(d(e, "Detection radius", v.sharkFishDetectionRadius, x -> v.sharkFishDetectionRadius = x));
      ecosystem.addEntry(i(e, "Maximum fullness for food targeting", v.maximumFullnessForFoodTargeting, x -> v.maximumFullnessForFoodTargeting = x));
      ecosystem.addEntry(d(e, "Large fish scent", v.largeFishScentMultiplier, x -> v.largeFishScentMultiplier = x));
      ecosystem.addEntry(d(e, "Strong shark food scent", v.strongSharkFoodScentMultiplier, x -> v.strongSharkFoodScentMultiplier = x));
      ConfigCategory chum = builder.getOrCreateCategory(Text.literal("Chum"));
      chum.addEntry(e.startBooleanToggle(Text.literal("Enable Chum Buckets"), v.enableChum).setSaveConsumer(x -> v.enableChum = x).build());
      chum.addEntry(i(e, "Duration (seconds)", v.chumDuration, x -> v.chumDuration = x));
      chum.addEntry(d(e, "Scent radius", v.chumRadius, x -> v.chumRadius = x));
      chum.addEntry(d(e, "Scent strength", v.chumScentStrength, x -> v.chumScentStrength = x));
      chum.addEntry(i(e, "Particle density per pulse", v.chumParticleCount, x -> v.chumParticleCount = x));
      chum.addEntry(i(e, "Particle pulse interval (ticks)", v.chumParticlePulseInterval, x -> v.chumParticlePulseInterval = x));
      ConfigCategory spawning = builder.getOrCreateCategory(Text.literal("Optional Shark Spawning"));
      spawning.addEntry(
         e.startBooleanToggle(Text.literal("Allow chum-triggered shark spawns"), v.allowChumTriggeredSpawns)
            .setSaveConsumer(x -> v.allowChumTriggeredSpawns = x)
            .build()
      );
      spawning.addEntry(i(e, "Spawn check interval", v.chumSpawnCheckInterval, x -> v.chumSpawnCheckInterval = x));
      spawning.addEntry(i(e, "Spawn chance one in", v.chumSpawnChanceOneIn, x -> v.chumSpawnChanceOneIn = x));
      spawning.addEntry(i(e, "Nearby shark cap", v.chumNearbySharkCap, x -> v.chumNearbySharkCap = x));
      spawning.addEntry(i(e, "Spawn attempts", v.chumSpawnAttempts, x -> v.chumSpawnAttempts = x));
      return v;
   }

   private static AbstractConfigListEntry<Double> d(ConfigEntryBuilder e, String label, double value, Consumer<Double> save) {
      return e.startDoubleField(Text.literal(label), value).setSaveConsumer(save).build();
   }

   private static AbstractConfigListEntry<Double> percent(ConfigEntryBuilder e, String label, double value, Consumer<Double> save) {
      return e.startDoubleField(Text.literal(label + " (%)"), value * 100.0).setSaveConsumer(x -> save.accept(x / 100.0)).build();
   }

   private static AbstractConfigListEntry<Integer> i(ConfigEntryBuilder e, String label, int value, Consumer<Integer> save) {
      return e.startIntField(Text.literal(label), value).setSaveConsumer(save).build();
   }
}
