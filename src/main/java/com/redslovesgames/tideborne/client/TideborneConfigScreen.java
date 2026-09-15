package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.config.TideborneConfig;
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
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** The single Tideborne-owned in-game configuration UI. */
public final class TideborneConfigScreen {
   private TideborneConfigScreen() {
   }

   public static Screen create(Screen parent) {
      MinecraftClient minecraft = MinecraftClient.getInstance();
      boolean localServer = minecraft.getServer() != null;
      ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(text("Tideborne Settings"));
      ConfigEntryBuilder entries = builder.entryBuilder();
      TideborneTraitsDraft traits = TideborneTraitsDraft.from(TideTraitsConfigManager.current());
      ServerConfig.Values teamServer = ServerConfig.get();
      ClientConfig.Values teamClient = ClientConfig.get();
      TideboundClientConfig.Values fishingClient = TideboundClientConfig.get();
      FishingSettingsAccess fishingAccess = fishingAccess(localServer);
      TideboundConfig.Values[] fishingServer = new TideboundConfig.Values[]{fishingAccess.values()};

      ConfigCategory fishing = builder.getOrCreateCategory(text("Fishing"));
      ConfigCategory satchel = builder.getOrCreateCategory(text("Satchel"));
      ConfigCategory journal = builder.getOrCreateCategory(text("Journal & Teams"));
      ConfigCategory ecosystem = builder.getOrCreateCategory(text("Sharks & Ecosystem"));
      ConfigCategory client = builder.getOrCreateCategory(text("Client & HUD"));
      ConfigCategory advanced = builder.getOrCreateCategory(text("Advanced"));

      addFishing(fishing, entries, fishingAccess, teamServer, localServer);
      addSatchel(satchel, entries, traits, localServer);
      addJournalAndTeams(journal, entries, traits, teamServer, localServer);
      addEcosystem(ecosystem, entries, fishingAccess);
      addClient(client, entries, teamClient, fishingClient);
      addAdvanced(advanced, entries, traits, localServer);

      builder.setSavingRunnable(() -> {
         ClientConfig.save();
         TideboundClientConfig.save();
         if (localServer) {
            TideborneConfig.saveServer(traits, teamServer, fishingServer[0]);
         } else if (fishingServer[0] != null) {
            TideborneConfigNetworkingClient.sendFishingUpdate(TideboundConfig.toJson(fishingServer[0]));
         }
      });
      return builder.build();
   }

   private static FishingSettingsAccess fishingAccess(boolean localServer) {
      if (localServer) {
         return new FishingSettingsAccess(TideboundConfig.valuesFromJson(TideboundConfig.settingsJson()), null);
      }
      if (!ClientTideboundSettings.available()) {
         return new FishingSettingsAccess(null, "Join a server or world first to receive fishing settings.");
      }
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || !client.player.hasPermissionLevel(2)) {
         return new FishingSettingsAccess(null, "Only server operators can edit server fishing settings.");
      }
      return new FishingSettingsAccess(TideboundConfig.valuesFromJson(ClientTideboundSettings.settingsJson()), null);
   }

   private static void addFishing(
      ConfigCategory category,
      ConfigEntryBuilder entries,
      FishingSettingsAccess access,
      ServerConfig.Values teamServer,
      boolean localServer
   ) {
      SubCategoryBuilder general = section(entries, "General & Catching", "The server controls fishing behavior and catch selection.");
      if (access.values() == null) {
         addDescription(general, entries, access.readOnlyReason());
      } else {
         addDescription(general, entries, "These gameplay values come from the server.");
      }
      category.addEntry(general.build());

      SubCategoryBuilder gear = section(entries, "Gear, Lines & Leaders", "Fishing effects from Tideborne gear.");
      if (access.values() != null) {
         TideboundConfig.Values values = access.values();
         gear.add(doubleEntry(entries, "Tentacle Line Catch Zone", values.tentacleCatchZoneMultiplier, x -> values.tentacleCatchZoneMultiplier = x));
         gear.add(doubleEntry(entries, "Tentacle Line Fish Movement", values.tentacleFishSpeedMultiplier, x -> values.tentacleFishSpeedMultiplier = x));
         gear.add(doubleEntry(entries, "Abaia Line Catch Zone", values.swiftCatchZoneMultiplier, x -> values.swiftCatchZoneMultiplier = x));
         gear.add(doubleEntry(entries, "Abaia Line Fish Movement", values.swiftFishSpeedMultiplier, x -> values.swiftFishSpeedMultiplier = x));
         gear.add(doubleEntry(entries, "Seafarer legendary fish preference (night ocean)", values.seafarersRareWeightMultiplier, x -> values.seafarersRareWeightMultiplier = x));
         gear.add(doubleEntry(entries, "Kujira ocean crate preference", values.kujiraOceanCrateMultiplier, x -> values.kujiraOceanCrateMultiplier = x));
      } else {
         addDescription(gear, entries, access.readOnlyReason());
      }
      category.addEntry(gear.build());

      SubCategoryBuilder bait = section(entries, "Bait", "Catch selection and fishing-minigame effects while Leviathan Bait is active.");
      if (access.values() != null) {
         TideboundConfig.Values values = access.values();
         bait.add(entries.startBooleanToggle(text("Always hook fish"), values.leviathanBaitFishOnly).setSaveConsumer(x -> values.leviathanBaitFishOnly = x).build());
         bait.add(intEntry(entries, "Fishing Luck bonus", values.leviathanBaitFishSelectionLuckBonus, x -> values.leviathanBaitFishSelectionLuckBonus = x));
         bait.add(doubleEntry(entries, "Fish Movement multiplier", values.leviathanBaitMinigameSpeedMultiplier, x -> values.leviathanBaitMinigameSpeedMultiplier = x));
         bait.add(doubleEntry(entries, "Catch Zone multiplier", values.leviathanBaitCatchZoneMultiplier, x -> values.leviathanBaitCatchZoneMultiplier = x));
      } else {
         addDescription(bait, entries, access.readOnlyReason());
      }
      category.addEntry(bait.build());

      SubCategoryBuilder bobbers = section(entries, "Bobbers", "Controls whether Tide bobber bonuses count toward team fishing progress.");
      if (localServer) {
         bobbers.add(entries.startBooleanToggle(text("Enable bobber bonuses"), teamServer.bobberBonusesEnabled)
            .setSaveConsumer(value -> teamServer.bobberBonusesEnabled = value).build());
      } else {
         addDescription(bobbers, entries, "This rule is controlled by the connected server.");
      }
      category.addEntry(bobbers.build());

      SubCategoryBuilder balance = section(entries, "Advanced Balance", "Low-level fish trait and size controls are kept in Advanced to avoid accidental edits.");
      addDescription(balance, entries, "Use Advanced > Specimen Distribution for body, condition, and size-roll tuning.");
      category.addEntry(balance.build());
   }

   private static void addSatchel(ConfigCategory category, ConfigEntryBuilder entries, TideborneTraitsDraft draft, boolean editable) {
      SubCategoryBuilder behavior = section(entries, "Behavior", "Conversion cost and default fish-protection behavior.");
      if (editable) {
         behavior.add(intEntry(entries, "Fish Satchel conversion cost (XP)", draft.conversionXpCost, value -> draft.conversionXpCost = value));
         for (String id : new ArrayList<>(draft.protectionDefaults.keySet())) {
            behavior.add(entries.startBooleanToggle(text("Protect " + pretty(id) + " by default"), draft.protectionDefaults.get(id))
               .setSaveConsumer(value -> draft.protectionDefaults.put(id, value)).build());
         }
      } else {
         addDescription(behavior, entries, "Read-only on multiplayer. The server controls Satchel balance.");
      }
      category.addEntry(behavior.build());

      SubCategoryBuilder sorting = section(entries, "Sorting", "Each Satchel keeps its own sorting order.");
      addDescription(sorting, entries, "Open a Satchel and use its Sorting tab to configure Sort Rules.");
      category.addEntry(sorting.build());

      SubCategoryBuilder upgrades = section(entries, "Upgrades", "Capacity size, XP costs, and feature unlock costs.");
      if (editable) {
         while (draft.capacityMultipliers.size() < 4) draft.capacityMultipliers.add(1.0);
         while (draft.capacityXpCosts.size() < 4) draft.capacityXpCosts.add(0);
         for (int level = 1; level <= 3; level++) {
            int index = level;
            upgrades.add(doubleEntry(entries, "Capacity " + roman(level) + " multiplier", draft.capacityMultipliers.get(level), value -> draft.capacityMultipliers.set(index, value)));
            upgrades.add(intEntry(entries, "Capacity " + roman(level) + " cost (XP)", draft.capacityXpCosts.get(level), value -> draft.capacityXpCosts.set(index, value)));
         }
         for (String id : new ArrayList<>(draft.featureXpCosts.keySet())) {
            upgrades.add(intEntry(entries, pretty(id) + " cost (XP)", draft.featureXpCosts.get(id), value -> draft.featureXpCosts.put(id, value)));
         }
      } else {
         addDescription(upgrades, entries, "Read-only on multiplayer. The server controls Satchel upgrade costs.");
      }
      category.addEntry(upgrades.build());

      SubCategoryBuilder records = section(entries, "Records", "Each Satchel keeps its own fish records. The server keeps them up to date.");
      addDescription(records, entries, "Open a Satchel and use its Records tab to browse stored fish records.");
      category.addEntry(records.build());
   }

   private static void addJournalAndTeams(
      ConfigCategory category,
      ConfigEntryBuilder entries,
      TideborneTraitsDraft traits,
      ServerConfig.Values server,
      boolean serverEditable
   ) {
      SubCategoryBuilder journal = section(entries, "Journal", "Team catch history and discovery tracking rules.");
      if (serverEditable) {
         journal.add(entries.startBooleanToggle(text("Team history"), server.historyEnabled).setSaveConsumer(value -> server.historyEnabled = value).build());
         journal.add(intEntry(entries, "Team history limit (entries)", server.historyLimit, value -> server.historyLimit = value));
         journal.add(entries.startBooleanToggle(text("Track discoveries"), server.trackDiscoveries).setSaveConsumer(value -> server.trackDiscoveries = value).build());
         journal.add(entries.startBooleanToggle(text("Track largest records"), server.trackLargestRecords).setSaveConsumer(value -> server.trackLargestRecords = value).build());
         journal.add(entries.startBooleanToggle(text("Track smallest records"), server.trackSmallestRecords).setSaveConsumer(value -> server.trackSmallestRecords = value).build());
      } else {
         addDescription(journal, entries, "Team journal rules are controlled by the connected server.");
      }
      category.addEntry(journal.build());

      SubCategoryBuilder teams = section(entries, "Teams", "Team leaderboards, contributions, and announcements.");
      if (serverEditable) {
         teams.add(entries.startBooleanToggle(text("Team leaderboards"), server.leaderboardEnabled).setSaveConsumer(value -> server.leaderboardEnabled = value).build());
         teams.add(entries.startBooleanToggle(text("Track contributions"), server.contributionTracking).setSaveConsumer(value -> server.contributionTracking = value).build());
         teams.add(entries.startBooleanToggle(text("Team announcements"), server.announcementsEnabled).setSaveConsumer(value -> server.announcementsEnabled = value).build());
      } else {
         addDescription(teams, entries, "Team rules are controlled by the connected server.");
      }
      category.addEntry(teams.build());

      SubCategoryBuilder records = section(entries, "Records", "Server rules for record badges, tooltips, and record claiming.");
      if (serverEditable) {
         records.add(entries.startBooleanToggle(text("Record badges (server)"), server.recordBadgesEnabled).setSaveConsumer(value -> server.recordBadgesEnabled = value).build());
         records.add(entries.startBooleanToggle(text("Record tooltips (server)"), server.recordTooltipsEnabled).setSaveConsumer(value -> server.recordTooltipsEnabled = value).build());
         records.add(entries.startBooleanToggle(text("Members may claim records with the exact fish"), server.membersMayClaimWithExactFish)
            .setSaveConsumer(value -> server.membersMayClaimWithExactFish = value).build());
      } else {
         addDescription(records, entries, "Record rules are controlled by the connected server.");
      }
      category.addEntry(records.build());

      SubCategoryBuilder shared = section(entries, "Shared Discoveries", "Controls whether team members share trait and size discoveries.");
      if (serverEditable) {
         shared.add(entries.startBooleanToggle(text("Share trait and size discoveries"), traits.sharedDiscovery)
            .setSaveConsumer(value -> traits.sharedDiscovery = value).build());
      } else {
         addDescription(shared, entries, "Shared discoveries are controlled by the connected server.");
      }
      category.addEntry(shared.build());
   }

   private static void addEcosystem(ConfigCategory category, ConfigEntryBuilder entries, FishingSettingsAccess access) {
      SubCategoryBuilder sharks = section(entries, "Sharks", "Fish scent, shark attraction, and shark hunting behavior.");
      SubCategoryBuilder chum = section(entries, "Chum", "Chum duration, scent range, and visible particles.");
      SubCategoryBuilder loss = section(entries, "Shark Catch Loss", "Controls when sharks can steal a catch before it is landed.");
      SubCategoryBuilder spawning = section(entries, "Optional Spawning", "Optional shark spawning from chum. Existing shark behavior is unchanged when disabled.");

      if (access.values() == null) {
         addDescription(sharks, entries, access.readOnlyReason());
         addDescription(chum, entries, access.readOnlyReason());
         addDescription(loss, entries, access.readOnlyReason());
         addDescription(spawning, entries, access.readOnlyReason());
      } else {
         TideboundConfig.Values values = access.values();
         sharks.add(entries.startBooleanToggle(text("Enable fish scent and attraction"), values.enableSharkFishAttraction).setSaveConsumer(x -> values.enableSharkFishAttraction = x).build());
         sharks.add(entries.startBooleanToggle(text("Enable sharks hunting living Tide fish"), values.enableSharkFishPredation).setSaveConsumer(x -> values.enableSharkFishPredation = x).build());
         sharks.add(doubleEntry(entries, "Shark detection range (blocks)", values.sharkFishDetectionRadius, x -> values.sharkFishDetectionRadius = x));
         sharks.add(intEntry(entries, "Maximum fullness for food targeting", values.maximumFullnessForFoodTargeting, x -> values.maximumFullnessForFoodTargeting = x));
         sharks.add(doubleEntry(entries, "Large Fish Scent Strength", values.largeFishScentMultiplier, x -> values.largeFishScentMultiplier = x));
         sharks.add(doubleEntry(entries, "Shark Food Scent Strength", values.strongSharkFoodScentMultiplier, x -> values.strongSharkFoodScentMultiplier = x));

         chum.add(entries.startBooleanToggle(text("Enable Chum Buckets"), values.enableChum).setSaveConsumer(x -> values.enableChum = x).build());
         chum.add(intEntry(entries, "Duration (seconds)", values.chumDuration, x -> values.chumDuration = x));
         chum.add(doubleEntry(entries, "Scent range (blocks)", values.chumRadius, x -> values.chumRadius = x));
         chum.add(doubleEntry(entries, "Scent strength", values.chumScentStrength, x -> values.chumScentStrength = x));
         chum.add(intEntry(entries, "Particles per pulse", values.chumParticleCount, x -> values.chumParticleCount = x));
         chum.add(intEntry(entries, "Particle pulse interval (ticks)", values.chumParticlePulseInterval, x -> values.chumParticlePulseInterval = x));

         loss.add(entries.startBooleanToggle(text("Sharks can steal catches"), values.enableSharkCatchLoss).setSaveConsumer(x -> values.enableSharkCatchLoss = x).build());
         loss.add(percentEntry(entries, "Base chance", values.sharkTheftBaseChance, x -> values.sharkTheftBaseChance = x));
         loss.add(percentEntry(entries, "Scent chance per strength", values.sharkTheftScentChancePerStrength, x -> values.sharkTheftScentChancePerStrength = x));
         loss.add(percentEntry(entries, "Scent bonus cap", values.sharkTheftScentBonusCap, x -> values.sharkTheftScentBonusCap = x));
         loss.add(percentEntry(entries, "Large fish bonus", values.sharkTheftLargeFishBonus, x -> values.sharkTheftLargeFishBonus = x));
         loss.add(percentEntry(entries, "Tuna bonus", values.sharkTheftTunaBonus, x -> values.sharkTheftTunaBonus = x));
         loss.add(percentEntry(entries, "Maximum chance", values.sharkTheftMaximumChance, x -> values.sharkTheftMaximumChance = x));
         loss.add(percentEntry(entries, "Steel Leader Shark Protection", values.steelLeaderCatchLossPreventionChance, x -> values.steelLeaderCatchLossPreventionChance = x));

         spawning.add(entries.startBooleanToggle(text("Allow chum-triggered shark spawns"), values.allowChumTriggeredSpawns).setSaveConsumer(x -> values.allowChumTriggeredSpawns = x).build());
         spawning.add(intEntry(entries, "Spawn check interval (ticks)", values.chumSpawnCheckInterval, x -> values.chumSpawnCheckInterval = x));
         spawning.add(intEntry(entries, "Spawn chance (1 in N)", values.chumSpawnChanceOneIn, x -> values.chumSpawnChanceOneIn = x));
         spawning.add(intEntry(entries, "Nearby shark cap", values.chumNearbySharkCap, x -> values.chumNearbySharkCap = x));
         spawning.add(intEntry(entries, "Spawn attempts per check", values.chumSpawnAttempts, x -> values.chumSpawnAttempts = x));
      }

      category.addEntry(sharks.build());
      category.addEntry(chum.build());
      category.addEntry(loss.build());
      category.addEntry(spawning.build());
   }

   private static void addClient(
      ConfigCategory category,
      ConfigEntryBuilder entries,
      ClientConfig.Values journalClient,
      TideboundClientConfig.Values fishingClient
   ) {
      SubCategoryBuilder rendering = section(entries, "Rendering", "Client-only visual preferences. These settings never change server gameplay.");
      addDescription(rendering, entries, "Condition item overlays, Fish Displays, and fish condition visuals use Tideborne's shared rendering.");
      category.addEntry(rendering.build());

      SubCategoryBuilder hud = section(entries, "HUD", "Fishing and record information shown during normal play.");
      hud.add(entries.startBooleanToggle(text("Show fishing HUD"), fishingClient.showFishingHud).setSaveConsumer(value -> fishingClient.showFishingHud = value).build());
      hud.add(entries.startBooleanToggle(text("Show record badges"), journalClient.showRecordBadges).setSaveConsumer(value -> journalClient.showRecordBadges = value).build());
      category.addEntry(hud.build());

      SubCategoryBuilder tooltips = section(entries, "Tooltips", "Extra fishing and record details shown on item tooltips.");
      tooltips.add(entries.startBooleanToggle(text("Show advanced equipment stats (F3 + H)"), fishingClient.showEquipmentTooltips)
         .setSaveConsumer(value -> fishingClient.showEquipmentTooltips = value).build());
      tooltips.add(entries.startBooleanToggle(text("Show record tooltips"), journalClient.showRecordTooltips).setSaveConsumer(value -> journalClient.showRecordTooltips = value).build());
      category.addEntry(tooltips.build());

      SubCategoryBuilder accessibility = section(entries, "Accessibility & Presentation", "Team Records visibility, alert duration, sound, and record colors.");
      accessibility.add(entries.startBooleanToggle(text("Show former members"), journalClient.showFormerMembers).setSaveConsumer(value -> journalClient.showFormerMembers = value).build());
      accessibility.add(intEntry(entries, "Toast duration (seconds)", journalClient.toastDurationSeconds, value -> journalClient.toastDurationSeconds = value));
      accessibility.add(entries.startBooleanToggle(text("Toast sound"), journalClient.toastSound).setSaveConsumer(value -> journalClient.toastSound = value).build());
      accessibility.add(entries.startColorField(text("Largest record color"), journalClient.largestColor).setSaveConsumer(value -> journalClient.largestColor = value).build());
      accessibility.add(entries.startColorField(text("Smallest record color"), journalClient.smallestColor).setSaveConsumer(value -> journalClient.smallestColor = value).build());
      accessibility.add(entries.startColorField(text("Discovery color"), journalClient.discoveryColor).setSaveConsumer(value -> journalClient.discoveryColor = value).build());
      category.addEntry(accessibility.build());

      SubCategoryBuilder debug = section(entries, "Debug", "Troubleshooting-only presentation controls hidden during normal play.");
      debug.add(entries.startBooleanToggle(text("Show Team Records debug button"), journalClient.debugTeamRecordsButton)
         .setDefaultValue(false)
         .setSaveConsumer(value -> journalClient.debugTeamRecordsButton = value)
         .build());
      category.addEntry(debug.build());
   }

   private static void addAdvanced(ConfigCategory category, ConfigEntryBuilder entries, TideborneTraitsDraft draft, boolean editable) {
      SubCategoryBuilder specimen = section(entries, "Specimen Distribution", "Low-level body, condition, size, and percentile tuning.");
      if (editable) {
         addDescription(specimen, entries, "Trait odds use values from 0.0 to 1.0. Body and condition are rolled independently.");
         for (FishMutation mutation : FishMutation.mutations()) {
            specimen.add(doubleEntry(entries, pretty(mutation.serializedName()) + " chance", draft.odds.get(mutation), value -> draft.odds.put(mutation, value)));
         }
         specimen.add(entries.startBooleanToggle(text("Redistribute excluded condition odds"), draft.redistributeIneligibleOdds)
            .setSaveConsumer(value -> draft.redistributeIneligibleOdds = value).build());
         specimen.add(doubleEntry(entries, "Dwarf body minimum length multiplier", draft.dwarfMin, value -> draft.dwarfMin = value));
         specimen.add(doubleEntry(entries, "Dwarf body maximum length multiplier", draft.dwarfMax, value -> draft.dwarfMax = value));
         specimen.add(doubleEntry(entries, "Giant body minimum length multiplier", draft.giantMin, value -> draft.giantMin = value));
         specimen.add(doubleEntry(entries, "Giant body maximum length multiplier", draft.giantMax, value -> draft.giantMax = value));
         specimen.add(doubleEntry(entries, "Parasite minimum length multiplier", draft.parasiteMin, value -> draft.parasiteMin = value));
         specimen.add(doubleEntry(entries, "Parasite maximum length multiplier", draft.parasiteMax, value -> draft.parasiteMax = value));
         specimen.add(doubleEntry(entries, "Perfect condition minimum percentile", draft.perfectMinPercentile, value -> draft.perfectMinPercentile = value));
         specimen.add(doubleEntry(entries, "Perfect condition maximum percentile", draft.perfectMaxPercentile, value -> draft.perfectMaxPercentile = value));
      } else {
         addDescription(specimen, entries, "Read-only on multiplayer. The server controls specimen distribution.");
      }
      category.addEntry(specimen.build());

      SubCategoryBuilder diagnostics = section(entries, "Diagnostics & Compatibility", "Diagnostics and client resource limits for troubleshooting.");
      if (editable) {
         diagnostics.add(entries.startBooleanToggle(text("Debug logging"), draft.debugLogging).setSaveConsumer(value -> draft.debugLogging = value).build());
         diagnostics.add(intEntry(entries, "Dynamic condition texture cache (entries)", draft.dynamicTextureCacheMaximum, value -> draft.dynamicTextureCacheMaximum = value));
      } else {
         addDescription(diagnostics, entries, "Server diagnostic values are read-only on multiplayer clients.");
      }
      category.addEntry(diagnostics.build());
   }

   private static SubCategoryBuilder section(ConfigEntryBuilder entries, String label, String description) {
      SubCategoryBuilder section = entries.startSubCategory(text(label));
      addDescription(section, entries, description);
      return section;
   }

   private static void addDescription(SubCategoryBuilder section, ConfigEntryBuilder entries, String value) {
      if (value != null && !value.isBlank()) {
         section.add(entries.startTextDescription(text(value)).build());
      }
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

   private record FishingSettingsAccess(TideboundConfig.Values values, String readOnlyReason) {
   }
}