/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.redslovesgames.tideborne.command.HistoryBadgeMeta;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import dev.ftb.mods.ftbteams.api.Team;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.text.MutableText;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;

public final class TeamProgressStore {
   static final String TRACKING_STARTED_KEY = "tracking_started_ms";
   static final String CONTRIBUTORS_KEY = "contributors";
   static final String HISTORY_KEY = "history";
   private static final String NAME_KEY = "name";
   private static final String CATCHES_KEY = "catches";
   private static final String SPECIES_KEY = "species";
   private static final String RECORD_EVENTS_KEY = "record_events";
   private static final ThreadLocal TIDEBORNE_CURRENT_SCORE = new ThreadLocal();
   private static final ThreadLocal TIDEBORNE_CURRENT_STARS = new ThreadLocal();
   private static final Map TIDEBORNE_EVENT_SCORES = new HashMap();
   private static final Map TIDEBORNE_EVENT_STARS = new HashMap();
   private static final Map TIDEBORNE_CONTRIBUTOR_SCORES = new HashMap();
   private static final ThreadLocal TIDEBORNE_CURRENT_FISH = new ThreadLocal();
   private static final ThreadLocal TIDEBORNE_LAST_FISH = new ThreadLocal();

   private TeamProgressStore() {
   }

   static boolean ensureInitialized(NbtCompound root) {
      boolean changed = false;
      if (!root.contains("tracking_started_ms", 4)) {
         root.putLong("tracking_started_ms", Instant.now().toEpochMilli());
         changed = true;
      }

      if (!root.contains("contributors", 10)) {
         root.put("contributors", new NbtCompound());
         changed = true;
      }

      if (!root.contains("history", 9)) {
         root.put("history", new NbtList());
         changed = true;
      }

      return changed;
   }

   static List<TeamProgressStore.RecordEvent> recordCatch(
      NbtCompound root, TidePlayerData before, TidePlayerData after, UUID playerId, String playerName, long gameTime, ServerConfig.Values config
   ) {
      ensureInitialized(root);
      if (!config.contributionTracking) {
         return List.of();
      }

      int catchDelta = 0;
      Set<String> caughtSpecies = new HashSet<>();

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : after.fishPlayerData.entrySet()) {
         FishStats next = (FishStats)entry.getValue().stats.orElse(null);
         if (next != null && !next.isEmpty()) {
            FishStats previous = (FishStats)before.fishPlayerData.getOrDefault(entry.getKey(), new FishPlayerData(false, false, false, Optional.empty()))
               .stats
               .orElse(null);
            int previousCount = previous == null ? 0 : previous.getAmountCaught();
            int delta = Math.max(0, next.getAmountCaught() - previousCount);
            if (delta > 0) {
               catchDelta += delta;
               caughtSpecies.add(key(entry.getKey()));
            }
         }
      }

      if (catchDelta == 0) {
         return List.of();
      }

      NbtCompound var17 = contributor(root, playerId);
      tideborneUpdateContributorFishScore(var17);
      var17.putString("name", playerName);
      var17.putInt("catches", saturatedAdd(var17.getInt("catches"), catchDelta));
      Set<String> species = readStrings(var17, "species");
      species.addAll(caughtSpecies);
      writeStrings(var17, "species", species);
      List<TeamProgressStore.RecordEvent> events = new ArrayList<>();

      for (RecordHolderStore.RecordChange change : RecordHolderStore.findRecordChanges(before, after)) {
         TeamProgressStore.EventType type = switch (change.kind()) {
            case FIRST -> TeamProgressStore.EventType.DISCOVERY;
            case LARGEST -> TeamProgressStore.EventType.LARGEST;
            case SMALLEST -> TeamProgressStore.EventType.SMALLEST;
         };
         if (isTracked(type, config)) {
            TeamProgressStore.RecordEvent event = new TeamProgressStore.RecordEvent(
               UUID.randomUUID(),
               type,
               playerId,
               playerName,
               playerId,
               playerName,
               key(change.fish()),
               change.newSize(),
               change.previousSize(),
               Instant.now().toEpochMilli(),
               gameTime
            );
            appendEvent(root, event, config.historyLimit);
            events.add(event);
         }
      }

      if (!events.isEmpty()) {
         var17.putInt("record_events", saturatedAdd(var17.getInt("record_events"), events.size()));
      }

      return List.copyOf(events);
   }

   static TeamProgressStore.RecordEvent recordRepair(
      NbtCompound root, UUID actorId, String actorName, UUID targetId, String targetName, Identifier fish, double size, long gameTime, int historyLimit
   ) {
      ensureInitialized(root);
      contributor(root, targetId).putString("name", targetName);
      TeamProgressStore.RecordEvent event = new TeamProgressStore.RecordEvent(
         UUID.randomUUID(),
         TeamProgressStore.EventType.REPAIR,
         actorId,
         actorName,
         targetId,
         targetName,
         fish.toString(),
         size,
         0.0,
         Instant.now().toEpochMilli(),
         gameTime
      );
      appendEvent(root, event, historyLimit);
      return event;
   }

   static void mergeTrackedDataOnce(NbtCompound targetRoot, NbtCompound sourceRoot, int historyLimit) {
      ensureInitialized(targetRoot);
      if (sourceRoot.contains("contributors", 10)) {
         if (sourceRoot.contains("tracking_started_ms", 4)) {
            targetRoot.putLong(
               "tracking_started_ms", Math.min(targetRoot.getLong("tracking_started_ms"), sourceRoot.getLong("tracking_started_ms"))
            );
         }

         NbtCompound targetContributors = targetRoot.getCompound("contributors");
         NbtCompound sourceContributors = sourceRoot.getCompound("contributors");

         for (String uuid : sourceContributors.getKeys()) {
            NbtCompound source = sourceContributors.getCompound(uuid);
            NbtCompound target = targetContributors.getCompound(uuid);
            target.putString("name", source.getString("name"));
            target.putInt("catches", saturatedAdd(target.getInt("catches"), source.getInt("catches")));
            target.putInt("record_events", saturatedAdd(target.getInt("record_events"), source.getInt("record_events")));
            target.putInt("fish_score", Math.max(target.getInt("fish_score"), source.getInt("fish_score")));
            Set<String> species = readStrings(target, "species");
            species.addAll(readStrings(source, "species"));
            writeStrings(target, "species", species);
            targetContributors.put(uuid, target);
         }

         Set<UUID> existing = new HashSet<>();
         List<TeamProgressStore.RecordEvent> combined = readHistory(targetRoot);
         combined.forEach(eventx -> existing.add(eventx.id()));

         for (TeamProgressStore.RecordEvent event : readHistory(sourceRoot)) {
            if (existing.add(event.id())) {
               combined.add(event);
            }
         }

         combined.sort(Comparator.comparingLong(TeamProgressStore.RecordEvent::timestamp).reversed());
         writeHistory(targetRoot, combined, historyLimit);
      }
   }

   public static NbtCompound buildSnapshot(
      NbtCompound root, TidePlayerData journal, Team team, int page, String metric, String fishFilter, String eventType, ServerConfig.Values config
   ) {
      ensureInitialized(root);
      NbtCompound result = new NbtCompound();
      result.putLong("tracking_started", root.getLong("tracking_started_ms"));
      result.putString("team_name", team.getShortName());
      result.putBoolean("leaderboard_enabled", config.leaderboardEnabled);
      result.putBoolean("history_enabled", config.historyEnabled);
      NbtList metrics = new NbtList();
      config.visibleMetrics.stream().map(NbtString::of).forEach(metrics::add);
      result.put("visible_metrics", metrics);
      long totalCatches = 0L;
      int discovered = 0;

      for (Entry<RegistryEntry<Item>, FishPlayerData> entry : journal.fishPlayerData.entrySet()) {
         FishPlayerData data = entry.getValue();
         if (data.isUnlocked && FishData.get((Item)entry.getKey().comp_349()).<Boolean>map(FishData::hasJournalEntry).orElse(false)) {
            discovered++;
         }

         totalCatches += data.stats.<Integer>map(FishStats::getAmountCaught).orElse(0).intValue();
      }

      long available = Registries.ITEM
         .stream()
         .map(FishData::get)
         .flatMap(Optional::stream)
         .filter(FishData::hasJournalEntry)
         .map(data -> key(data.fish()))
         .distinct()
         .count();
      result.putLong("total_catches", totalCatches);
      result.putInt("discovered", discovered);
      result.putInt("available", (int)Math.min(2147483647L, available));
      List<TeamProgressStore.Contributor> contributors = readContributors(root, team);
      String contributorTags = metric == null ? "catches" : metric;
      Comparator<TeamProgressStore.Contributor> comparator;
      if (contributorTags.equals("fish_score")) {
         comparator = new FishScoreComparator();
      } else {
         comparator = switch (contributorTags) {
            case "species" -> Comparator.comparingInt(TeamProgressStore.Contributor::species);
            case "record_events" -> Comparator.comparingInt(TeamProgressStore.Contributor::recordEvents);
            case "active_records" -> Comparator.comparingInt(TeamProgressStore.Contributor::activeRecords);
            default -> Comparator.comparingInt(TeamProgressStore.Contributor::catches);
         };
      }

      contributors.sort(comparator.reversed().thenComparing(TeamProgressStore.Contributor::name, String.CASE_INSENSITIVE_ORDER));
      NbtList contributorTagsx = new NbtList();
      contributors.stream().map(TeamProgressStore.Contributor::toTag).forEach(contributorTagsx::add);
      result.put("contributors", contributorTagsx);
      List<TeamProgressStore.RecordEvent> history = config.historyEnabled ? readHistory(root) : List.of();
      if (fishFilter != null && !fishFilter.isBlank()) {
         history = history.stream().filter(event -> event.fish().equals(fishFilter)).toList();
      }

      if (eventType != null && !eventType.isBlank() && !eventType.equals("all")) {
         history = history.stream().filter(event -> event.type().name().equalsIgnoreCase(eventType)).toList();
      }

      int pageSize = 8;
      int maxPage = Math.max(0, (history.size() - 1) / pageSize);
      int safePage = Math.max(0, Math.min(page, maxPage));
      result.putInt("page", safePage);
      result.putInt("pages", maxPage + 1);
      NbtList historyTags = new NbtList();
      history.stream().skip((long)safePage * pageSize).limit(pageSize).map(TeamProgressStore.RecordEvent::toTag).forEach(historyTags::add);
      result.put("history", historyTags);
      result.put("top_fish", root.getList("top_fish", 10));
      return result;
   }

   public static List<TeamProgressStore.Contributor> readContributors(NbtCompound root, Team team) {
      tideborneClearContributorScores();
      ensureInitialized(root);
      List<TeamProgressStore.Contributor> result = new ArrayList<>();
      NbtCompound contributors = root.getCompound("contributors");

      for (String key : contributors.getKeys()) {
         try {
            UUID id = UUID.fromString(key);
            NbtCompound tag = contributors.getCompound(key);
            tideborneRegisterContributorFishScore(id, tag);
            int active = RecordHolderStore.countActiveRecords(root, id);
            result.add(
               new TeamProgressStore.Contributor(
                  id,
                  tag.getString("name"),
                  tag.getInt("catches"),
                  readStrings(tag, "species").size(),
                  tag.getInt("record_events"),
                  active,
                  !team.getMembers().contains(id)
               )
            );
         } catch (IllegalArgumentException exception) {
            TideTeamJournal.LOGGER.warn("Ignoring malformed contributor UUID {}", key);
         }
      }

      return result;
   }

   public static List<TeamProgressStore.RecordEvent> readHistory(NbtCompound root) {
      List<TeamProgressStore.RecordEvent> result = new ArrayList<>();

      for (NbtElement tag : root.getList("history", 10)) {
         TeamProgressStore.RecordEvent event = TeamProgressStore.RecordEvent.fromTag((NbtCompound)tag);
         if (event != null) {
            result.add(event);
         }
      }

      result.sort(Comparator.comparingLong(TeamProgressStore.RecordEvent::timestamp).reversed());
      return result;
   }

   public static void trim(NbtCompound root, int limit) {
      writeHistory(root, readHistory(root), limit);
   }

   private static boolean isTracked(TeamProgressStore.EventType type, ServerConfig.Values config) {
      return switch (type) {
         case DISCOVERY -> config.trackDiscoveries;
         case LARGEST -> config.trackLargestRecords;
         case SMALLEST -> config.trackSmallestRecords;
         case REPAIR -> config.trackRepairs;
      };
   }

   private static void appendEvent(NbtCompound root, TeamProgressStore.RecordEvent event, int limit) {
      HistoryBadgeMeta.bindCurrent(event.id(), event.type().name());
      List<TeamProgressStore.RecordEvent> events = readHistory(root);
      events.add(event);
      events.sort(Comparator.comparingLong(TeamProgressStore.RecordEvent::timestamp).reversed());
      writeHistory(root, events, limit);
   }

   private static void writeHistory(NbtCompound root, List<TeamProgressStore.RecordEvent> events, int limit) {
      NbtList list = new NbtList();
      if (limit > 0) {
         events.stream().limit(limit).map(TeamProgressStore.RecordEvent::toTag).forEach(list::add);
      }

      root.put("history", list);
   }

   private static NbtCompound contributor(NbtCompound root, UUID id) {
      NbtCompound contributors = root.getCompound("contributors");
      String key = id.toString();
      if (!contributors.contains(key, 10)) {
         contributors.put(key, new NbtCompound());
      }

      return contributors.getCompound(key);
   }

   private static Set<String> readStrings(NbtCompound root, String key) {
      Set<String> result = new HashSet<>();
      root.getList(key, 8).forEach(tag -> result.add(tag.asString()));
      return result;
   }

   private static void writeStrings(NbtCompound root, String key, Set<String> values) {
      NbtList list = new NbtList();
      values.stream().sorted().map(NbtString::of).forEach(list::add);
      root.put(key, list);
   }

   private static String key(RegistryEntry<Item> item) {
      return Registries.ITEM.getId((Item)item.comp_349()).toString();
   }

   private static int saturatedAdd(int first, int second) {
      return (int)Math.min(2147483647L, (long)first + second);
   }

   public static int tideborneFishStars(ItemStack var0) {
      try {
         Optional var1 = FishData.get(var0);
         if (!var1.isEmpty()) {
            String var2 = ((FishData)var1.get()).profile().rarity().toString().toLowerCase().replace('_', ' ');
            if (!var2.equals("common")) {
               if (!var2.equals("uncommon")) {
                  if (!var2.equals("rare")) {
                     if (!var2.equals("very rare") && !var2.equals("epic")) {
                        if (!var2.equals("legendary")) {
                           return 1;
                        }

                        return 5;
                     }

                     return 4;
                  }

                  return 3;
               }

               return 2;
            }

            return 1;
         }
      } catch (RuntimeException var4) {
      }

      return 0;
   }

   public static double tideborneFishScore(ItemStack var0) {
      int var1 = tideborneFishStars(var0);
      if (var1 <= 0) {
         return -1.0;
      }

      double var2 = (Double)var0.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
      String var4 = (String)var0.getOrDefault(TideTraitsComponents.MUTATION, "normal");
      double var5 = (Double)TideItemData.FISH_LENGTH.getOrDefault(var0, 0.0);
      double var7 = var5;
      Optional var9 = FishData.get(var0);
      if (var9.isPresent()) {
         Optional var10 = ((FishData)var9.get()).size();
         if (var10.isPresent()) {
            var7 = ((SizeData)var10.get()).recordHighCm();
         }
      }

      return TraitAxesRuntime.score(var0, var1, var2, var5, var7);
   }

   public static void tideborneBeginCatch(ItemStack var0) {
      TIDEBORNE_LAST_FISH.remove();
      double var1 = tideborneFishScore(var0);
      if (!(var1 >= 0.0)) {
         tideborneClearCatch();
      } else {
         TIDEBORNE_CURRENT_SCORE.set(var1);
         int var3 = tideborneFishStars(var0);
         TIDEBORNE_CURRENT_STARS.set(var3);
         NbtCompound var4 = new NbtCompound();
         String var5 = Registries.ITEM.getId(var0.getItem()).toString();
         var4.putString("fish", var5);
         var4.putInt("fish_score", (int)Math.round(var1));
         var4.putInt("fish_stars", var3);
         double var6 = (Double)TideItemData.FISH_LENGTH.getOrDefault(var0, 0.0);
         var4.putDouble("length", var6);
         String var8 = TraitAxesRuntime.condition(var0);
         var4.putString("mutation", var8);
         var4.putString("condition", TraitAxesRuntime.condition(var0));
         var4.putString("body_type", TraitAxesRuntime.bodyType(var0));
         double var9 = (Double)var0.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0);
         var4.putDouble("percentile", var9);
         var4.putLong("nonce", System.nanoTime());
         TIDEBORNE_CURRENT_FISH.set(var4);
         HistoryBadgeMeta.capture(var4);
      }
   }

   public static void tideborneClearCatch() {
      NbtCompound var0 = (NbtCompound)TIDEBORNE_CURRENT_FISH.get();
      if (var0 != null) {
         TIDEBORNE_LAST_FISH.set(var0);
      }

      TIDEBORNE_CURRENT_SCORE.remove();
      TIDEBORNE_CURRENT_STARS.remove();
      TIDEBORNE_CURRENT_FISH.remove();
      HistoryBadgeMeta.finish();
   }

   public static int tideborneCurrentFishScore() {
      Double var0 = (Double)TIDEBORNE_CURRENT_SCORE.get();
      return var0 == null ? -1 : (int)Math.round(var0);
   }

   public static int tideborneCurrentFishStars() {
      Integer var0 = (Integer)TIDEBORNE_CURRENT_STARS.get();
      return var0 == null ? 0 : var0;
   }

   public static void tideborneUpdateContributorFishScore(NbtCompound var0) {
      int var1 = tideborneCurrentFishScore();
      if (var1 >= 0) {
         int var2 = var0.getInt("fish_score");
         if (var1 > var2) {
            var0.putInt("fish_score", var1);
         }
      }
   }

   public static void tideborneClearContributorScores() {
      TIDEBORNE_CONTRIBUTOR_SCORES.clear();
   }

   public static void tideborneRegisterContributorFishScore(UUID var0, NbtCompound var1) {
      TIDEBORNE_CONTRIBUTOR_SCORES.put(var0, var1.getInt("fish_score"));
   }

   public static int tideborneContributorFishScore(TeamProgressStore.Contributor var0) {
      Integer var1 = (Integer)TIDEBORNE_CONTRIBUTOR_SCORES.get(var0.id());
      return var1 == null ? 0 : var1;
   }

   public static void tideborneRegisterEventMeta(TeamProgressStore.RecordEvent var0, NbtCompound var1) {
      if (var1.getBoolean("fish_score")) {
         TIDEBORNE_EVENT_SCORES.put(var0.id(), var1.getInt("fish_score"));
      }

      if (var1.getBoolean("fish_stars")) {
         TIDEBORNE_EVENT_STARS.put(var0.id(), var1.getInt("fish_stars"));
      }

      HistoryBadgeMeta.register(var0.id(), var1);
   }

   public static int tideborneEventFishScore(TeamProgressStore.RecordEvent var0) {
      Integer var1 = (Integer)TIDEBORNE_EVENT_SCORES.get(var0.id());
      return var1 == null ? -1 : var1;
   }

   public static int tideborneEventFishStars(TeamProgressStore.RecordEvent var0) {
      Integer var1 = (Integer)TIDEBORNE_EVENT_STARS.get(var0.id());
      return var1 == null ? 0 : var1;
   }

   public static int tideborneEventFishScoreForWrite(TeamProgressStore.RecordEvent var0) {
      int var1 = tideborneCurrentFishScore();
      if (var1 >= 0) {
         return var1;
      }

      if (var0.type() != TeamProgressStore.EventType.REPAIR) {
         NbtCompound var2 = (NbtCompound)TIDEBORNE_LAST_FISH.get();
         if (var2 != null) {
            return var2.getInt("fish_score");
         }
      }

      return tideborneEventFishScore(var0);
   }

   public static int tideborneEventFishStarsForWrite(TeamProgressStore.RecordEvent var0) {
      int var1 = tideborneCurrentFishStars();
      if (var1 > 0) {
         return var1;
      }

      if (var0.type() != TeamProgressStore.EventType.REPAIR) {
         NbtCompound var2 = (NbtCompound)TIDEBORNE_LAST_FISH.get();
         if (var2 != null) {
            return var2.getInt("fish_stars");
         }
      }

      return tideborneEventFishStars(var0);
   }

   public static Text tideborneEnrichChat(Text var0, TeamProgressStore.RecordEvent var1) {
      int var2 = tideborneEventFishScore(var1);
      int var3 = tideborneEventFishStars(var1);
      if (var2 < 0 && var3 <= 0) {
         return var0;
      }

      StringBuilder var4 = new StringBuilder("  [");

      for (int var5 = 0; var5 < var3; var5++) {
         var4.append("\u2605");
      }

      if (var3 > 0 && var2 >= 0) {
         var4.append(" | ");
      }

      if (var2 >= 0) {
         var4.append("Score ").append(var2);
      }

      var4.append("]");
      return var0.copy().append(Text.literal(var4.toString()));
   }

   public static Text tideborneFishScoreTooltip(ItemStack var0) {
      double var1 = tideborneFishScore(var0);
      if (!(var1 >= 0.0)) {
         return null;
      }

      int var3 = (int)Math.round(var1);
      MutableText var4 = Text.literal("Fish Score: ").formatted(Formatting.GRAY);
      return var4.append(Text.literal(Integer.toString(var3)).formatted(Formatting.AQUA));
   }

   public static int tideborneFishStarsFromData(FishData var0) {
      if (var0 == null) {
         return 0;
      }

      String var1 = var0.profile().rarity().toString().toLowerCase().replace('_', ' ');
      if (!var1.equals("common")) {
         if (!var1.equals("uncommon")) {
            if (!var1.equals("rare")) {
               if (var1.equals("very rare") || var1.equals("epic")) {
                  return 4;
               } else {
                  return !var1.equals("legendary") ? 1 : 5;
               }
            } else {
               return 3;
            }
         } else {
            return 2;
         }
      } else {
         return 1;
      }
   }

   public static double tideborneMutationBonus(String var0) {
      return TraitAxesRuntime.conditionBonus(var0);
   }

   public static double tideborneFishScoreFromParts(double var0, int var2, String var3) {
      return tideborneFishScoreFromParts(var0, var2, var3, 0.0, 0.0);
   }

   public static String tideborneFormatScore(double var0) {
      return var0 == Math.rint(var0) ? Integer.toString((int)Math.round(var0)) : String.format(Locale.ROOT, "%.1f", var0);
   }

   public static double tideborneFishScoreFromParts(double var0, int var2, String var3, double var4, double var6) {
      return TraitAxesRuntime.scoreFromParts(var0, var2, var3, "normal", var4, var6);
   }

   public static void tideborneRecordCurrentTopFish(NbtCompound var0, UUID var1, String var2) {
      NbtCompound var3 = (NbtCompound)TIDEBORNE_CURRENT_FISH.get();
      if (var3 == null) {
         var3 = (NbtCompound)TIDEBORNE_LAST_FISH.get();
      }

      if (var3 != null) {
         var3.putUuid("catcher_id", var1);
         var3.putString("catcher_name", var2);
         var3.putLong("timestamp", System.currentTimeMillis());
         NbtList var4 = var0.getList("top_fish", 10);
         NbtList var5 = new NbtList();
         boolean var6 = false;
         Iterator var7 = var4.iterator();

         while (var7.hasNext() && var5.size() < 12) {
            NbtCompound var8 = (NbtCompound)var7.next();
            if (var8.getLong("nonce") == var3.getLong("nonce")) {
               return;
            }

            if (!var6 && var3.getInt("fish_score") > var8.getInt("fish_score")) {
               var5.add(var3);
               var6 = true;
            }

            if (var5.size() < 12) {
               var5.add(var8);
            }
         }

         if (!var6 && var5.size() < 12) {
            var5.add(var3);
         }

         var0.put("top_fish", var5);
      }
   }

   public record Contributor(UUID id, String name, int catches, int species, int recordEvents, int activeRecords, boolean former) {
      NbtCompound toTag() {
         NbtCompound tag = new NbtCompound();
         tag.putUuid("id", this.id);
         tag.putString("name", this.name);
         tag.putInt("catches", this.catches);
         tag.putInt("species", this.species);
         tag.putInt("record_events", this.recordEvents);
         tag.putInt("active_records", this.activeRecords);
         tag.putBoolean("former", this.former);
         tag.putInt("fish_score", TeamProgressStore.tideborneContributorFishScore(this));
         return tag;
      }
   }

   public enum EventType {
      DISCOVERY,
      LARGEST,
      SMALLEST,
      REPAIR;
   }

   public record RecordEvent(
      UUID id,
      TeamProgressStore.EventType type,
      UUID actorId,
      String actorName,
      UUID targetId,
      String targetName,
      String fish,
      double newSize,
      double previousSize,
      long timestamp,
      long gameTime
   ) {
      NbtCompound toTag() {
         NbtCompound tag = new NbtCompound();
         tag.putUuid("id", this.id);
         tag.putString("type", this.type.name());
         tag.putUuid("actor_id", this.actorId);
         tag.putString("actor_name", this.actorName);
         tag.putUuid("target_id", this.targetId);
         tag.putString("target_name", this.targetName);
         tag.putString("fish", this.fish);
         tag.putDouble("new_size", this.newSize);
         tag.putDouble("previous_size", this.previousSize);
         tag.putLong("timestamp", this.timestamp);
         tag.putLong("game_time", this.gameTime);
         tag.putInt("fish_score", TeamProgressStore.tideborneEventFishScoreForWrite(this));
         tag.putInt("fish_stars", TeamProgressStore.tideborneEventFishStarsForWrite(this));
         HistoryBadgeMeta.write(this.id(), tag);
         return tag;
      }

      public static TeamProgressStore.RecordEvent fromTag(NbtCompound tag) {
         TeamProgressStore.RecordEvent var10000;
         try {
            var10000 = new TeamProgressStore.RecordEvent(
               tag.getUuid("id"),
               TeamProgressStore.EventType.valueOf(tag.getString("type")),
               tag.getUuid("actor_id"),
               tag.getString("actor_name"),
               tag.getUuid("target_id"),
               tag.getString("target_name"),
               tag.getString("fish"),
               tag.getDouble("new_size"),
               tag.getDouble("previous_size"),
               tag.getLong("timestamp"),
               tag.getLong("game_time")
            );
         } catch (IllegalArgumentException exception) {
            return null;
         }

         TeamProgressStore.tideborneRegisterEventMeta(var10000, tag);
         return var10000;
      }
   }
}
