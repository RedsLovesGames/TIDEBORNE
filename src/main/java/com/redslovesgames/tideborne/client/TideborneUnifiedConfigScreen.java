/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import com.redslovesgames.tideborne.config.TideborneConfigBackend;
import com.redslovesgames.tideborne.config.TideborneTraitsDraft;
import com.redslovesgames.tideboundcompatibility.client.ClientTideboundSettings;
import com.redslovesgames.tideboundcompatibility.client.TideboundClientConfig;
import com.redslovesgames.tideboundcompatibility.client.TideboundModMenu;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideteamjournal.ServerConfig;
import com.redslovesgames.tideteamjournal.client.ClientConfig;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.trait.FishMutation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Consumer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;

public final class TideborneUnifiedConfigScreen {
   private TideborneUnifiedConfigScreen() {
   }

   public static Screen create(Screen var0) {
      ConfigBuilder var1 = ConfigBuilder.create().setParentScreen(var0).setTitle(t("Tideborne Settings"));
      ConfigEntryBuilder var2 = var1.entryBuilder();
      TideborneTraitsDraft var3 = TideborneTraitsDraft.from(TideTraitsConfigManager.current());
      ServerConfig.Values var4 = ServerConfig.get();
      ClientConfig.Values var5 = ClientConfig.get();
      TideboundClientConfig.Values var6 = TideboundClientConfig.get();
      TideboundConfig.Values[] var7 = new TideboundConfig.Values[1];
      addTraits(var1, var2, var3);
      addSatchel(var1, var2, var3);
      addTeam(var1, var2, var4, var5);
      addClient(var1, var2, var6);
      addFishingCategories(var1, var2, var7);
      var1.setSavingRunnable(() -> {
         TideborneConfigBackend.saveAll(var3, var4, var7[0]);
         if (var7[0] != null) {
            sendFishingUpdate(TideboundConfig.toJson(var7[0]));
         }
      });
      return var1.build();
   }

   private static void addTraits(ConfigBuilder var0, ConfigEntryBuilder var1, TideborneTraitsDraft var2) {
      ConfigCategory var3 = var0.getOrCreateCategory(t("Traits: Body Types & Conditions"));
      var3.addEntry(var1.startTextDescription(t("Trait odds are probabilities from 0.0 to 1.0. Body type and condition are rolled independently.")).build());

      for (FishMutation var5 : FishMutation.mutations()) {
         String var6 = pretty(var5.serializedName()) + " chance";
         var3.addEntry(doubleEntry(var1, var6, var2.odds.get(var5), var2x -> var2.odds.put(var5, var2x)));
      }

      var3.addEntry(
         var1.startBooleanToggle(t("Redistribute excluded condition odds"), var2.redistributeIneligibleOdds)
            .setSaveConsumer(var1x -> var2.redistributeIneligibleOdds = var1x)
            .build()
      );
      var3.addEntry(doubleEntry(var1, "Dwarf body minimum length multiplier", var2.dwarfMin, var1x -> var2.dwarfMin = var1x));
      var3.addEntry(doubleEntry(var1, "Dwarf body maximum length multiplier", var2.dwarfMax, var1x -> var2.dwarfMax = var1x));
      var3.addEntry(doubleEntry(var1, "Giant body minimum length multiplier", var2.giantMin, var1x -> var2.giantMin = var1x));
      var3.addEntry(doubleEntry(var1, "Giant body maximum length multiplier", var2.giantMax, var1x -> var2.giantMax = var1x));
      var3.addEntry(doubleEntry(var1, "Parasite minimum length multiplier", var2.parasiteMin, var1x -> var2.parasiteMin = var1x));
      var3.addEntry(doubleEntry(var1, "Parasite maximum length multiplier", var2.parasiteMax, var1x -> var2.parasiteMax = var1x));
      var3.addEntry(doubleEntry(var1, "Perfect condition minimum percentile", var2.perfectMinPercentile, var1x -> var2.perfectMinPercentile = var1x));
      var3.addEntry(doubleEntry(var1, "Perfect condition maximum percentile", var2.perfectMaxPercentile, var1x -> var2.perfectMaxPercentile = var1x));
      var3.addEntry(
         var1.startBooleanToggle(t("Shared trait/size discoveries"), var2.sharedDiscovery).setSaveConsumer(var1x -> var2.sharedDiscovery = var1x).build()
      );
      var3.addEntry(var1.startBooleanToggle(t("Debug logging"), var2.debugLogging).setSaveConsumer(var1x -> var2.debugLogging = var1x).build());
      var3.addEntry(intEntry(var1, "Dynamic condition texture cache", var2.dynamicTextureCacheMaximum, var1x -> var2.dynamicTextureCacheMaximum = var1x));
   }

   private static void addSatchel(ConfigBuilder var0, ConfigEntryBuilder var1, TideborneTraitsDraft var2) {
      ConfigCategory var3 = var0.getOrCreateCategory(t("Angler's Satchel"));
      var3.addEntry(intEntry(var1, "Fish Satchel conversion XP", var2.conversionXpCost, var1x -> var2.conversionXpCost = var1x));

      while (var2.capacityMultipliers.size() < 4) {
         var2.capacityMultipliers.add(1.0);
      }

      while (var2.capacityXpCosts.size() < 4) {
         var2.capacityXpCosts.add(0);
      }

      for (int var4 = 1; var4 <= 3; var4++) {
         int var5 = var4;
         var3.addEntry(
            doubleEntry(var1, "Capacity " + roman(var4) + " multiplier", var2.capacityMultipliers.get(var4), var2x -> var2.capacityMultipliers.set(var5, var2x))
         );
         var3.addEntry(intEntry(var1, "Capacity " + roman(var4) + " XP cost", var2.capacityXpCosts.get(var4), var2x -> var2.capacityXpCosts.set(var5, var2x)));
      }

      for (String var8 : new ArrayList<>(var2.featureXpCosts.keySet())) {
         var3.addEntry(intEntry(var1, pretty(var8) + " XP cost", var2.featureXpCosts.get(var8), var2x -> var2.featureXpCosts.put(var8, var2x)));
      }

      for (String var9 : new ArrayList<>(var2.protectionDefaults.keySet())) {
         var3.addEntry(
            var1.startBooleanToggle(t("Protect " + pretty(var9) + " by default"), var2.protectionDefaults.get(var9))
               .setSaveConsumer(var2x -> var2.protectionDefaults.put(var9, var2x))
               .build()
         );
      }
   }

   private static void addTeam(ConfigBuilder var0, ConfigEntryBuilder var1, ServerConfig.Values var2, ClientConfig.Values var3) {
      ConfigCategory var4 = var0.getOrCreateCategory(t("Journal & Teams"));
      var4.addEntry(var1.startBooleanToggle(t("Team leaderboards"), var2.leaderboardEnabled).setSaveConsumer(var1x -> var2.leaderboardEnabled = var1x).build());
      var4.addEntry(var1.startBooleanToggle(t("Team history"), var2.historyEnabled).setSaveConsumer(var1x -> var2.historyEnabled = var1x).build());
      var4.addEntry(
         var1.startBooleanToggle(t("Track contributions"), var2.contributionTracking).setSaveConsumer(var1x -> var2.contributionTracking = var1x).build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Team announcements"), var2.announcementsEnabled).setSaveConsumer(var1x -> var2.announcementsEnabled = var1x).build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Record badges (server)"), var2.recordBadgesEnabled).setSaveConsumer(var1x -> var2.recordBadgesEnabled = var1x).build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Record tooltips (server)"), var2.recordTooltipsEnabled)
            .setSaveConsumer(var1x -> var2.recordTooltipsEnabled = var1x)
            .build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Members may claim records with exact fish"), var2.membersMayClaimWithExactFish)
            .setSaveConsumer(var1x -> var2.membersMayClaimWithExactFish = var1x)
            .build()
      );
      var4.addEntry(intEntry(var1, "Team history limit", var2.historyLimit, var1x -> var2.historyLimit = var1x));
      var4.addEntry(var1.startBooleanToggle(t("Track discoveries"), var2.trackDiscoveries).setSaveConsumer(var1x -> var2.trackDiscoveries = var1x).build());
      var4.addEntry(
         var1.startBooleanToggle(t("Track largest records"), var2.trackLargestRecords).setSaveConsumer(var1x -> var2.trackLargestRecords = var1x).build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Track smallest records"), var2.trackSmallestRecords).setSaveConsumer(var1x -> var2.trackSmallestRecords = var1x).build()
      );
      var4.addEntry(var1.startBooleanToggle(t("Bobber bonuses"), var2.bobberBonusesEnabled).setSaveConsumer(var1x -> var2.bobberBonusesEnabled = var1x).build());
      var4.addEntry(var1.startTextDescription(t("Client journal display")).build());
      var4.addEntry(var1.startBooleanToggle(t("Show record badges"), var3.showRecordBadges).setSaveConsumer(var1x -> var3.showRecordBadges = var1x).build());
      var4.addEntry(
         var1.startBooleanToggle(t("Show record tooltips"), var3.showRecordTooltips).setSaveConsumer(var1x -> var3.showRecordTooltips = var1x).build()
      );
      var4.addEntry(
         var1.startBooleanToggle(t("Show Team Records button"), var3.showTeamRecordsButton)
            .setSaveConsumer(var1x -> var3.showTeamRecordsButton = var1x)
            .build()
      );
      var4.addEntry(var1.startBooleanToggle(t("Show former members"), var3.showFormerMembers).setSaveConsumer(var1x -> var3.showFormerMembers = var1x).build());
      var4.addEntry(intEntry(var1, "Toast duration (seconds)", var3.toastDurationSeconds, var1x -> var3.toastDurationSeconds = var1x));
      var4.addEntry(var1.startBooleanToggle(t("Toast sound"), var3.toastSound).setSaveConsumer(var1x -> var3.toastSound = var1x).build());
      var4.addEntry(var1.startColorField(t("Largest record color"), var3.largestColor).setSaveConsumer(var1x -> var3.largestColor = var1x).build());
      var4.addEntry(var1.startColorField(t("Smallest record color"), var3.smallestColor).setSaveConsumer(var1x -> var3.smallestColor = var1x).build());
      var4.addEntry(var1.startColorField(t("Discovery color"), var3.discoveryColor).setSaveConsumer(var1x -> var3.discoveryColor = var1x).build());
   }

   private static void addClient(ConfigBuilder var0, ConfigEntryBuilder var1, TideboundClientConfig.Values var2) {
      ConfigCategory var3 = var0.getOrCreateCategory(t("Client & Rendering"));
      var3.addEntry(var1.startBooleanToggle(t("Show fishing HUD"), var2.showFishingHud).setSaveConsumer(var1x -> var2.showFishingHud = var1x).build());
      var3.addEntry(
         var1.startBooleanToggle(t("Show advanced equipment stats (F3 + H)"), var2.showEquipmentTooltips)
            .setSaveConsumer(var1x -> var2.showEquipmentTooltips = var1x)
            .build()
      );
      var3.addEntry(
         var1.startTextDescription(t("Condition item overlays, Fish Displays and entity condition visuals use Tideborne's shared rendering pipeline.")).build()
      );
   }

   private static void addFishingCategories(ConfigBuilder var0, ConfigEntryBuilder var1, TideboundConfig.Values[] var2) {
      if (!ClientTideboundSettings.available()) {
         var0.getOrCreateCategory(t("Fishing Gameplay"))
            .addEntry(var1.startTextDescription(t("Join a server/world first to receive authoritative fishing settings.")).build());
      } else {
         try {
            Method var3 = TideboundModMenu.class.getDeclaredMethod("categories", ConfigBuilder.class, ConfigEntryBuilder.class);
            var3.setAccessible(true);
            var2[0] = (TideboundConfig.Values)var3.invoke(null, var0, var1);
         } catch (ReflectiveOperationException var4) {
            var0.getOrCreateCategory(t("Fishing Gameplay"))
               .addEntry(var1.startTextDescription(t("Could not load fishing categories: " + var4.getClass().getSimpleName())).build());
         }
      }
   }

   private static AbstractConfigListEntry<?> doubleEntry(ConfigEntryBuilder var0, String var1, double var2, Consumer<Double> var4) {
      return var0.startDoubleField(t(var1), var2).setSaveConsumer(var4).build();
   }

   private static AbstractConfigListEntry<?> intEntry(ConfigEntryBuilder var0, String var1, int var2, Consumer<Integer> var3) {
      return var0.startIntField(t(var1), var2).setSaveConsumer(var3).build();
   }

   private static Text t(String var0) {
      return Text.literal(var0);
   }

   private static String pretty(String var0) {
      String[] var1 = var0.split("_");
      StringBuilder var2 = new StringBuilder();

      for (String var6 : var1) {
         if (!var6.isEmpty()) {
            if (var2.length() > 0) {
               var2.append(' ');
            }

            var2.append(Character.toUpperCase(var6.charAt(0))).append(var6.substring(1));
         }
      }

      return var2.toString();
   }

   private static String roman(int var0) {
      return var0 == 1 ? "I" : (var0 == 2 ? "II" : "III");
   }

   private static void sendFishingUpdate(String var0) {
      try {
         Class var1 = Class.forName("com.redslovesgames.tideboundcompatibility.network.TideboundSettingsUpdatePayload");
         Object var2 = var1.getConstructor(String.class).newInstance(var0);
         Class var3 = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");

         for (Method var7 : var3.getMethods()) {
            if (var7.getName().equals("send") && var7.getParameterCount() == 1) {
               var7.invoke(null, var2);
               return;
            }
         }
      } catch (Throwable var8) {
      }
   }
}
