package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.config.TideborneTraitsDraft;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import com.redslovesgames.tideborne.journal.ServerConfig;
import com.redslovesgames.tideborne.journal.client.ClientConfig;
import com.redslovesgames.tideborne.presentation.client.ClientTideboundSettings;
import com.redslovesgames.tideborne.presentation.client.TideboundClientConfig;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** The single Tideborne-owned in-game configuration UI. */
public final class TideborneConfigScreen {
   private TideborneConfigScreen() {
   }

   public static Screen create(Screen parent) {
      ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(text("Tideborne Settings"));
      ConfigEntryBuilder entries = builder.entryBuilder();
      TideborneTraitsDraft traits = TideborneTraitsDraft.from(TideTraitsConfigManager.current());
      ServerConfig.Values teamServer = ServerConfig.get();
      ClientConfig.Values teamClient = ClientConfig.get();
      TideboundClientConfig.Values fishingClient = TideboundClientConfig.get();
      TideboundConfig.Values[] fishingServer = new TideboundConfig.Values[1];

      addTraits(builder, entries, traits);
      addSatchel(builder, entries, traits);
      addTeam(builder, entries, teamServer, teamClient);
      addClient(builder, entries, fishingClient);
      addFishing(builder, entries, fishingServer);

      builder.setSavingRunnable(() -> {
         TideborneConfigBackend.saveAll(traits, teamServer, fishingServer[0]);
         if (fishingServer[0] != null) {
            TideborneConfigNetworkingClient.sendFishingUpdate(TideboundConfig.toJson(fishingServer[0]));
         }
      });
      return builder.build();
   }

   private static void addTraits(ConfigBuilder builder, ConfigEntryBuilder entries, TideborneTraitsDraft draft) {
      ConfigCategory category = builder.getOrCreateCategory(text("Traits: Body Types & Conditions"));
      category.addEntry(entries.startTextDescription(text("Trait odds are probabilities from 0.0 to 1.0. Body type and condition are rolled independently.")).build());
      for (FishMutation mutation : FishMutation.mutations()) {
         category.addEntry(doubleEntry(entries, pretty(mutation.serializedName()) + " chance", draft.odds.get(mutation), value -> draft.odds.put(mutation, value)));
      }
      category.addEntry(entries.startBooleanToggle(text("Redistribute excluded condition odds"), draft.redistributeIneligibleOdds)
         .setSaveConsumer(value -> draft.redistributeIneligibleOdds = value).build());
      category.addEntry(doubleEntry(entries, "Dwarf body minimum length multiplier", draft.dwarfMin, value -> draft.dwarfMin = value));
      category.addEntry(doubleEntry(entries, "Dwarf body maximum length multiplier", draft.dwarfMax, value -> draft.dwarfMax = value));
      category.addEntry(doubleEntry(entries, "Giant body minimum length multiplier", draft.giantMin, value -> draft.giantMin = value));
      category.addEntry(doubleEntry(entries, "Giant body maximum length multiplier", draft.giantMax, value -> draft.giantMax = value));
      category.addEntry(doubleEntry(entries, "Parasite minimum length multiplier", draft.parasiteMin, value -> draft.parasiteMin = value));
      category.addEntry(doubleEntry(entries, "Parasite maximum length multiplier", draft.parasiteMax, value -> draft.parasiteMax = value));
      category.addEntry(doubleEntry(entries, "Perfect condition minimum percentile", draft.perfectMinPercentile, value -> draft.perfectMinPercentile = value));
      category.addEntry(doubleEntry(entries, "Perfect condition maximum percentile", draft.perfectMaxPercentile, value -> draft.perfectMaxPercentile = value));
      category.addEntry(entries.startBooleanToggle(text("Shared trait/size discoveries"), draft.sharedDiscovery)
         .setSaveConsumer(value -> draft.sharedDiscovery = value).build());
      category.addEntry(entries.startBooleanToggle(text("Debug logging"), draft.debugLogging)
         .setSaveConsumer(value -> draft.debugLogging = value).build());
      category.addEntry(intEntry(entries, "Dynamic condition texture cache", draft.dynamicTextureCacheMaximum, value -> draft.dynamicTextureCacheMaximum = value));
   }

   private static void addSatchel(ConfigBuilder builder, ConfigEntryBuilder entries, TideborneTraitsDraft draft) {
      ConfigCategory category = builder.getOrCreateCategory(text("Angler's Satchel"));
      category.addEntry(intEntry(entries, "Fish Satchel conversion XP", draft.conversionXpCost, value -> draft.conversionXpCost = value));
      while (draft.capacityMultipliers.size() < 4) draft.capacityMultipliers.add(1.0);
      while (draft.capacityXpCosts.size() < 4) draft.capacityXpCosts.add(0);
      for (int level = 1; level <= 3; level++) {
         int index = level;
         category.addEntry(doubleEntry(entries, "Capacity " + roman(level) + " multiplier", draft.capacityMultipliers.get(level), value -> draft.capacityMultipliers.set(index, value)));
         category.addEntry(intEntry(entries, "Capacity " + roman(level) + " XP cost", draft.capacityXpCosts.get(level), value -> draft.capacityXpCosts.set(index, value)));
      }
      for (String id : new ArrayList<>(draft.featureXpCosts.keySet())) {
         category.addEntry(intEntry(entries, pretty(id) + " XP cost", draft.featureXpCosts.get(id), value -> draft.featureXpCosts.put(id, value)));
      }
      for (String id : new ArrayList<>(draft.protectionDefaults.keySet())) {
         category.addEntry(entries.startBooleanToggle(text("Protect " + pretty(id) + " by default"), draft.protectionDefaults.get(id))
            .setSaveConsumer(value -> draft.protectionDefaults.put(id, value)).build());
      }
   }

   private static void addTeam(ConfigBuilder builder, ConfigEntryBuilder entries, ServerConfig.Values server, ClientConfig.Values client) {
      ConfigCategory category = builder.getOrCreateCategory(text("Journal & Teams"));
      category.addEntry(entries.startBooleanToggle(text("Team leaderboards"), server.leaderboardEnabled).setSaveConsumer(value -> server.leaderboardEnabled = value).build());
      category.addEntry(entries.startBooleanToggle(text("Team history"), server.historyEnabled).setSaveConsumer(value -> server.historyEnabled = value).build());
      category.addEntry(entries.startBooleanToggle(text("Track contributions"), server.contributionTracking).setSaveConsumer(value -> server.contributionTracking = value).build());
      category.addEntry(entries.startBooleanToggle(text("Team announcements"), server.announcementsEnabled).setSaveConsumer(value -> server.announcementsEnabled = value).build());
      category.addEntry(entries.startBooleanToggle(text("Record badges (server)"), server.recordBadgesEnabled).setSaveConsumer(value -> server.recordBadgesEnabled = value).build());
      category.addEntry(entries.startBooleanToggle(text("Record tooltips (server)"), server.recordTooltipsEnabled).setSaveConsumer(value -> server.recordTooltipsEnabled = value).build());
      category.addEntry(entries.startBooleanToggle(text("Members may claim records with exact fish"), server.membersMayClaimWithExactFish).setSaveConsumer(value -> server.membersMayClaimWithExactFish = value).build());
      category.addEntry(intEntry(entries, "Team history limit", server.historyLimit, value -> server.historyLimit = value));
      category.addEntry(entries.startBooleanToggle(text("Track discoveries"), server.trackDiscoveries).setSaveConsumer(value -> server.trackDiscoveries = value).build());
      category.addEntry(entries.startBooleanToggle(text("Track largest records"), server.trackLargestRecords).setSaveConsumer(value -> server.trackLargestRecords = value).build());
      category.addEntry(entries.startBooleanToggle(text("Track smallest records"), server.trackSmallestRecords).setSaveConsumer(value -> server.trackSmallestRecords = value).build());
      category.addEntry(entries.startBooleanToggle(text("Bobber bonuses"), server.bobberBonusesEnabled).setSaveConsumer(value -> server.bobberBonusesEnabled = value).build());
      category.addEntry(entries.startTextDescription(text("Client journal display")).build());
      category.addEntry(entries.startBooleanToggle(text("Show record badges"), client.showRecordBadges).setSaveConsumer(value -> client.showRecordBadges = value).build());
      category.addEntry(entries.startBooleanToggle(text("Show record tooltips"), client.showRecordTooltips).setSaveConsumer(value -> client.showRecordTooltips = value).build());
      category.addEntry(entries.startBooleanToggle(text("Show Team Records button"), client.showTeamRecordsButton).setSaveConsumer(value -> client.showTeamRecordsButton = value).build());
      category.addEntry(entries.startBooleanToggle(text("Show former members"), client.showFormerMembers).setSaveConsumer(value -> client.showFormerMembers = value).build());
      category.addEntry(intEntry(entries, "Toast duration (seconds)", client.toastDurationSeconds, value -> client.toastDurationSeconds = value));
      category.addEntry(entries.startBooleanToggle(text("Toast sound"), client.toastSound).setSaveConsumer(value -> client.toastSound = value).build());
      category.addEntry(entries.startColorField(text("Largest record color"), client.largestColor).setSaveConsumer(value -> client.largestColor = value).build());
      category.addEntry(entries.startColorField(text("Smallest record color"), client.smallestColor).setSaveConsumer(value -> client.smallestColor = value).build());
      category.addEntry(entries.startColorField(text("Discovery color"), client.discoveryColor).setSaveConsumer(value -> client.discoveryColor = value).build());
   }

   private static void addClient(ConfigBuilder builder, ConfigEntryBuilder entries, TideboundClientConfig.Values client) {
      ConfigCategory category = builder.getOrCreateCategory(text("Client & Rendering"));
      category.addEntry(entries.startBooleanToggle(text("Show fishing HUD"), client.showFishingHud).setSaveConsumer(value -> client.showFishingHud = value).build());
      category.addEntry(entries.startBooleanToggle(text("Show advanced equipment stats (F3 + H)"), client.showEquipmentTooltips).setSaveConsumer(value -> client.showEquipmentTooltips = value).build());
      category.addEntry(entries.startTextDescription(text("Condition item overlays, Fish Displays and entity condition visuals use Tideborne's shared rendering pipeline.")).build());
   }

   private static void addFishing(ConfigBuilder builder, ConfigEntryBuilder entries, TideboundConfig.Values[] target) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (!ClientTideboundSettings.available()) {
         builder.getOrCreateCategory(text("Fishing Gameplay")).addEntry(entries.startTextDescription(text("Join a server/world first to receive authoritative fishing settings.")).build());
         return;
      }
      if (client.player == null || !client.player.hasPermissionLevel(2)) {
         builder.getOrCreateCategory(text("Fishing Gameplay")).addEntry(entries.startTextDescription(text("Only server operators can edit gameplay settings.")).build());
         return;
      }

      TideboundConfig.Values values = TideboundConfig.valuesFromJson(ClientTideboundSettings.settingsJson());
      target[0] = values;
      ConfigCategory gear = builder.getOrCreateCategory(text("Fishing Gear"));
      gear.addEntry(doubleEntry(entries, "Tentacle Line catch zone", values.tentacleCatchZoneMultiplier, x -> values.tentacleCatchZoneMultiplier = x));
      gear.addEntry(doubleEntry(entries, "Tentacle Line fish speed", values.tentacleFishSpeedMultiplier, x -> values.tentacleFishSpeedMultiplier = x));
      gear.addEntry(doubleEntry(entries, "Abaia Line catch zone", values.swiftCatchZoneMultiplier, x -> values.swiftCatchZoneMultiplier = x));
      gear.addEntry(doubleEntry(entries, "Abaia Line fish speed", values.swiftFishSpeedMultiplier, x -> values.swiftFishSpeedMultiplier = x));
      gear.addEntry(doubleEntry(entries, "Seafarer night legendary weight", values.seafarersRareWeightMultiplier, x -> values.seafarersRareWeightMultiplier = x));
      gear.addEntry(doubleEntry(entries, "Kujira ocean crate weight", values.kujiraOceanCrateMultiplier, x -> values.kujiraOceanCrateMultiplier = x));

      ConfigCategory bait = builder.getOrCreateCategory(text("Leviathan Bait"));
      bait.addEntry(entries.startBooleanToggle(text("Force fish-only catches"), values.leviathanBaitFishOnly).setSaveConsumer(x -> values.leviathanBaitFishOnly = x).build());
      bait.addEntry(intEntry(entries, "Fish-selection luck bonus", values.leviathanBaitFishSelectionLuckBonus, x -> values.leviathanBaitFishSelectionLuckBonus = x));
      bait.addEntry(doubleEntry(entries, "Minigame speed multiplier", values.leviathanBaitMinigameSpeedMultiplier, x -> values.leviathanBaitMinigameSpeedMultiplier = x));
      bait.addEntry(doubleEntry(entries, "Catch-zone multiplier", values.leviathanBaitCatchZoneMultiplier, x -> values.leviathanBaitCatchZoneMultiplier = x));

      ConfigCategory loss = builder.getOrCreateCategory(text("Shark Catch Loss"));
      loss.addEntry(entries.startBooleanToggle(text("Enable abstract shark catch loss"), values.enableSharkCatchLoss).setSaveConsumer(x -> values.enableSharkCatchLoss = x).build());
      loss.addEntry(percentEntry(entries, "Base chance", values.sharkTheftBaseChance, x -> values.sharkTheftBaseChance = x));
      loss.addEntry(percentEntry(entries, "Scent chance per strength", values.sharkTheftScentChancePerStrength, x -> values.sharkTheftScentChancePerStrength = x));
      loss.addEntry(percentEntry(entries, "Scent bonus cap", values.sharkTheftScentBonusCap, x -> values.sharkTheftScentBonusCap = x));
      loss.addEntry(percentEntry(entries, "Large-fish bonus", values.sharkTheftLargeFishBonus, x -> values.sharkTheftLargeFishBonus = x));
      loss.addEntry(percentEntry(entries, "Tuna bonus", values.sharkTheftTunaBonus, x -> values.sharkTheftTunaBonus = x));
      loss.addEntry(percentEntry(entries, "Maximum chance", values.sharkTheftMaximumChance, x -> values.sharkTheftMaximumChance = x));
      loss.addEntry(percentEntry(entries, "Steel Leader protection", values.steelLeaderCatchLossPreventionChance, x -> values.steelLeaderCatchLossPreventionChance = x));

      ConfigCategory ecosystem = builder.getOrCreateCategory(text("Shark Ecosystem"));
      ecosystem.addEntry(entries.startBooleanToggle(text("Enable fish scent and attraction"), values.enableSharkFishAttraction).setSaveConsumer(x -> values.enableSharkFishAttraction = x).build());
      ecosystem.addEntry(entries.startBooleanToggle(text("Enable living Tide fish predation"), values.enableSharkFishPredation).setSaveConsumer(x -> values.enableSharkFishPredation = x).build());
      ecosystem.addEntry(doubleEntry(entries, "Detection radius", values.sharkFishDetectionRadius, x -> values.sharkFishDetectionRadius = x));
      ecosystem.addEntry(intEntry(entries, "Maximum fullness for food targeting", values.maximumFullnessForFoodTargeting, x -> values.maximumFullnessForFoodTargeting = x));
      ecosystem.addEntry(doubleEntry(entries, "Large fish scent", values.largeFishScentMultiplier, x -> values.largeFishScentMultiplier = x));
      ecosystem.addEntry(doubleEntry(entries, "Strong shark food scent", values.strongSharkFoodScentMultiplier, x -> values.strongSharkFoodScentMultiplier = x));

      ConfigCategory chum = builder.getOrCreateCategory(text("Chum"));
      chum.addEntry(entries.startBooleanToggle(text("Enable Chum Buckets"), values.enableChum).setSaveConsumer(x -> values.enableChum = x).build());
      chum.addEntry(intEntry(entries, "Duration (seconds)", values.chumDuration, x -> values.chumDuration = x));
      chum.addEntry(doubleEntry(entries, "Scent radius", values.chumRadius, x -> values.chumRadius = x));
      chum.addEntry(doubleEntry(entries, "Scent strength", values.chumScentStrength, x -> values.chumScentStrength = x));
      chum.addEntry(intEntry(entries, "Particle density per pulse", values.chumParticleCount, x -> values.chumParticleCount = x));
      chum.addEntry(intEntry(entries, "Particle pulse interval (ticks)", values.chumParticlePulseInterval, x -> values.chumParticlePulseInterval = x));

      ConfigCategory spawning = builder.getOrCreateCategory(text("Optional Shark Spawning"));
      spawning.addEntry(entries.startBooleanToggle(text("Allow chum-triggered shark spawns"), values.allowChumTriggeredSpawns).setSaveConsumer(x -> values.allowChumTriggeredSpawns = x).build());
      spawning.addEntry(intEntry(entries, "Spawn check interval", values.chumSpawnCheckInterval, x -> values.chumSpawnCheckInterval = x));
      spawning.addEntry(intEntry(entries, "Spawn chance one in", values.chumSpawnChanceOneIn, x -> values.chumSpawnChanceOneIn = x));
      spawning.addEntry(intEntry(entries, "Nearby shark cap", values.chumNearbySharkCap, x -> values.chumNearbySharkCap = x));
      spawning.addEntry(intEntry(entries, "Spawn attempts", values.chumSpawnAttempts, x -> values.chumSpawnAttempts = x));
   }

   private static AbstractConfigListEntry<?> doubleEntry(ConfigEntryBuilder entries, String label, double value, Consumer<Double> save) {
      return entries.startDoubleField(text(label), value).setSaveConsumer(save).build();
   }

   private static AbstractConfigListEntry<?> percentEntry(ConfigEntryBuilder entries, String label, double value, Consumer<Double> save) {
      return entries.startDoubleField(text(label + " (%)"), value * 100.0).setSaveConsumer(x -> save.accept(x / 100.0)).build();
   }

   private static AbstractConfigListEntry<?> intEntry(ConfigEntryBuilder entries, String label, int value, Consumer<Integer> save) {
      return entries.startIntField(text(label), value).setSaveConsumer(save).build();
   }

   private static Text text(String value) {
      return Text.literal(value);
   }

   private static String pretty(String value) {
      StringBuilder result = new StringBuilder();
      for (String part : value.split("_")) {
         if (part.isEmpty()) continue;
         if (!result.isEmpty()) result.append(' ');
         result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
      }
      return result.toString();
   }

   private static String roman(int value) {
      return value == 1 ? "I" : value == 2 ? "II" : "III";
   }
}
