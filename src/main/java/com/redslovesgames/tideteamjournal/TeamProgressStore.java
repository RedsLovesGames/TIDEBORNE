package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.redslovesgames.tideborne.command.HistoryBadgeMeta;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import dev.ftb.mods.ftbteams.api.Team;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
   private static final ThreadLocal<Double> TIDEBORNE_CURRENT_SCORE = new ThreadLocal<>();
   private static final ThreadLocal<Integer> TIDEBORNE_CURRENT_STARS = new ThreadLocal<>();
   private static final Map<UUID, Integer> TIDEBORNE_EVENT_SCORES = new HashMap<>();
   private static final Map<UUID, Integer> TIDEBORNE_EVENT_STARS = new HashMap<>();
   private static final Map<UUID, Integer> TIDEBORNE_CONTRIBUTOR_SCORES = new HashMap<>();
   private static final ThreadLocal<NbtCompound> TIDEBORNE_CURRENT_FISH = new ThreadLocal<>();
   private static final ThreadLocal<NbtCompound> TIDEBORNE_LAST_FISH = new ThreadLocal<>();

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

      return migrateStoredScores(root) | changed;
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
         FishStats next = entry.getValue().stats.orElse(null);
         if (next != null && !next.isEmpty()) {
            FishStats previous = before.fishPlayerData.getOrDefault(entry.getKey(), new FishPlayerData(false, false, false, Optional.empty()))
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

      NbtCompound contribution = contributor(root, playerId);
      tideborneUpdateContributorFishScore(contribution);
      contribution.putString("name", playerName);
      contribution.putInt("catches", saturatedAdd(contribution.getInt("catches"), catchDelta));
      Set<String> species = readStrings(contribution, "species");
      species.addAll(caughtSpecies);
      writeStrings(contribution, "species", species);
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
         contribution.putInt("record_events", saturatedAdd(contribution.getInt("record_events"), events.size()));
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
      migrateStoredScores(sourceRoot);
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
            int score = Math.max(StoredFishScoreStorage.readCanonical(target).orElse(0),
                  StoredFishScoreStorage.readCanonical(source).orElse(0));
            StoredFishScoreStorage.writeCanonical(target, score);
            target.putInt("fish_score", score);
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
         if (data.isUnlocked && FishData.get(entry.getKey().value()).map(FishData::hasJournalEntry).orElse(false)) {
            discovered++;
         }

         totalCatches += data.stats.map(FishStats::getAmountCaught).orElse(0);
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
      List<TeamProgressStore.Contributor> contributors = readInitializedContributors(root, team);
      String sortMetric = metric == null ? "catches" : metric;
      Comparator<TeamProgressStore.Contributor> comparator;
      if (sortMetric.equals("fish_score")) {
         comparator = Comparator.comparingInt(TeamProgressStore::tideborneContributorFishScore);
      } else {
         comparator = switch (sortMetric) {
            case "species" -> Comparator.comparingInt(TeamProgressStore.Contributor::species);
            case "record_events" -> Comparator.comparingInt(TeamProgressStore.Contributor::recordEvents);
            case "active_records" -> Comparator.comparingInt(TeamProgressStore.Contributor::activeRecords);
            default -> Comparator.comparingInt(TeamProgressStore.Contributor::catches);
         };
      }

      contributors.sort(comparator.reversed().thenComparing(TeamProgressStore.Contributor::name, String.CASE_INSENSITIVE_ORDER));
      NbtList contributorTags = new NbtList();
      contributors.stream().map(TeamProgressStore.Contributor::toTag).forEach(contributorTags::add);
      result.put("contributors", contributorTags);
      List<TeamProgressStore.RecordEvent> history = config.historyEnabled ? readHistory(root) : List.of();
      if (fishFilter != null && !fishFilter.isBlank()) {
         history = history.stream().filter(event -> event.fish().equals(fishFilter)).toList();
      }

      if (eventType != null && !eventType.isBlank() && !eventType.equals("all")) {
         history = history.stream().filter(event -> event.type().name().equalsIgnoreCase(eventType)).toList();
      }

      int pageSize = 5;
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
      ensureInitialized(root);
      return readInitializedContributors(root, team);
   }

   private static List<TeamProgressStore.Contributor> readInitializedContributors(NbtCompound root, Team team) {
      tideborneClearContributorScores();
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
      return Registries.ITEM.getId(item.value()).toString();
   }

   private static int saturatedAdd(int first, int second) {
      return (int)Math.min(2147483647L, (long)first + second);
   }

   public static int tideborneFishStars(ItemStack stack) {
      try {
         return tideborneFishStarsFromData(FishData.get(stack).orElse(null));
      } catch (RuntimeException ignored) {
         return 0;
      }
   }

   public static double tideborneFishScore(ItemStack stack) {
      SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
      return specimen != null && specimen.fishScore().isPresent() ? specimen.fishScore().getAsInt() : -1.0;
   }

   public static void tideborneBeginCatch(ItemStack stack) {
      TeamCanonicalJournalCapture.begin(stack);
      TIDEBORNE_LAST_FISH.remove();
      SpecimenData specimen = TeamCanonicalJournalCapture.currentSpecimen().orElse(null);
      if (specimen == null || specimen.fishScore().isEmpty()) {
         tideborneClearCatch();
         return;
      }
      int score = specimen.fishScore().getAsInt();
      int stars = tideborneFishStars(stack);
      TIDEBORNE_CURRENT_SCORE.set((double) score);
      TIDEBORNE_CURRENT_STARS.set(stars);
      NbtCompound tag = new NbtCompound();
      tag.putString("fish", Registries.ITEM.getId(stack.getItem()).toString());
      tag.putInt("fish_score", score);
      StoredFishScoreStorage.writeCanonical(tag, score);
      tag.putInt("fish_stars", stars);
      tag.putDouble("length", specimen.finalLength());
      tag.putDouble("percentile", specimen.finalPercentile());
      tag.putString("body_type", specimen.bodyType().name().toLowerCase(Locale.ROOT));
      tag.putString("condition", specimen.condition().name().toLowerCase(Locale.ROOT));
      tag.putString("mutation", tag.getString("condition"));
      tag.putString("pigmentation", specimen.pigmentation().name().toLowerCase(Locale.ROOT));
      tag.putString("quality", specimen.specimenQuality().name().toLowerCase(Locale.ROOT));
      tag.putLong("nonce", System.nanoTime());
      TIDEBORNE_CURRENT_FISH.set(tag);
      HistoryBadgeMeta.capture(tag);
   }

   /** Keep legacy history fallback after clear; canonical indexing expires after the second clear. */
   public static void tideborneClearCatch() {
      NbtCompound current = TIDEBORNE_CURRENT_FISH.get();
      if (current != null) {
         TIDEBORNE_LAST_FISH.set(current);
      }

      TIDEBORNE_CURRENT_SCORE.remove();
      TIDEBORNE_CURRENT_STARS.remove();
      TIDEBORNE_CURRENT_FISH.remove();
      HistoryBadgeMeta.finish();
      TeamCanonicalJournalCapture.clear();
   }

   public static int tideborneCurrentFishScore() {
      Double score = TIDEBORNE_CURRENT_SCORE.get();
      return score == null ? -1 : (int)Math.round(score);
   }

   public static int tideborneCurrentFishStars() {
      Integer stars = TIDEBORNE_CURRENT_STARS.get();
      return stars == null ? 0 : stars;
   }

   public static void tideborneUpdateContributorFishScore(NbtCompound tag) {
      StoredFishScoreStorage.updateBest(tag,
            StoredFishScoreStorage.highestCanonicalScore(TIDEBORNE_CURRENT_FISH.get(), TIDEBORNE_LAST_FISH.get()));
      syncLegacyMirror(tag);
   }

   public static void tideborneClearContributorScores() {
      TIDEBORNE_CONTRIBUTOR_SCORES.clear();
   }

   public static void tideborneRegisterContributorFishScore(UUID id, NbtCompound tag) {
      migrateStoredScore(tag);
      TIDEBORNE_CONTRIBUTOR_SCORES.put(id, StoredFishScoreStorage.readCanonical(tag).orElse(-1));
   }

   public static int tideborneContributorFishScore(TeamProgressStore.Contributor contributor) {
      Integer score = TIDEBORNE_CONTRIBUTOR_SCORES.get(contributor.id());
      return score == null ? -1 : score;
   }

   public static void tideborneRegisterEventMeta(TeamProgressStore.RecordEvent event, NbtCompound tag) {
      migrateStoredScore(tag);
      StoredFishScoreStorage.readCanonical(tag).ifPresent(score -> TIDEBORNE_EVENT_SCORES.put(event.id(), score));

      if (tag.getBoolean("fish_stars")) {
         TIDEBORNE_EVENT_STARS.put(event.id(), tag.getInt("fish_stars"));
      }

      HistoryBadgeMeta.register(event.id(), tag);
   }

   public static int tideborneEventFishScore(TeamProgressStore.RecordEvent event) {
      Integer score = TIDEBORNE_EVENT_SCORES.get(event.id());
      return score == null ? -1 : score;
   }

   public static int tideborneEventFishStars(TeamProgressStore.RecordEvent event) {
      Integer stars = TIDEBORNE_EVENT_STARS.get(event.id());
      return stars == null ? 0 : stars;
   }

   public static int tideborneEventFishScoreForWrite(TeamProgressStore.RecordEvent event) {
      int score = tideborneCurrentFishScore();
      if (score >= 0) {
         return score;
      }

      if (event.type() != TeamProgressStore.EventType.REPAIR) {
         NbtCompound last = TIDEBORNE_LAST_FISH.get();
         if (last != null) {
            return StoredFishScoreStorage.readCanonical(last).orElse(-1);
         }
      }

      return tideborneEventFishScore(event);
   }

   public static int tideborneEventFishStarsForWrite(TeamProgressStore.RecordEvent event) {
      int stars = tideborneCurrentFishStars();
      if (stars > 0) {
         return stars;
      }

      if (event.type() != TeamProgressStore.EventType.REPAIR) {
         NbtCompound last = TIDEBORNE_LAST_FISH.get();
         if (last != null) {
            return last.getInt("fish_stars");
         }
      }

      return tideborneEventFishStars(event);
   }

   public static Text tideborneEnrichChat(Text message, TeamProgressStore.RecordEvent event) {
      int score = tideborneEventFishScore(event);
      int stars = tideborneEventFishStars(event);
      if (score < 0 && stars <= 0) {
         return message;
      }

      StringBuilder suffix = new StringBuilder("  [");

      for (int star = 0; star < stars; star++) {
         suffix.append("\u2605");
      }

      if (stars > 0 && score >= 0) {
         suffix.append(" | ");
      }

      if (score >= 0) {
         suffix.append("Score ").append(score);
      }

      suffix.append("]");
      return message.copy().append(Text.literal(suffix.toString()));
   }

   public static Text tideborneFishScoreTooltip(ItemStack stack) {
      double score = tideborneFishScore(stack);
      if (!(score >= 0.0)) {
         return null;
      }

      int roundedScore = (int)Math.round(score);
      MutableText label = Text.literal("Fish Score: ").formatted(Formatting.GRAY);
      return label.append(Text.literal(Integer.toString(roundedScore)).formatted(Formatting.AQUA));
   }

   public static int tideborneFishStarsFromData(FishData data) {
      if (data == null) {
         return 0;
      }
      return switch (data.profile().rarity().toString().toLowerCase().replace('_', ' ')) {
         case "uncommon" -> 2;
         case "rare" -> 3;
         case "very rare", "epic" -> 4;
         case "legendary" -> 5;
         default -> 1;
      };
   }

   public static double tideborneMutationBonus(String mutation) {
      return TraitAxesRuntime.conditionBonus(mutation);
   }

   public static double tideborneFishScoreFromParts(double percentile, int stars, String mutation) {
      return tideborneFishScoreFromParts(percentile, stars, mutation, 0.0, 0.0);
   }

   public static String tideborneFormatScore(double score) {
      return score == Math.rint(score) ? Integer.toString((int)Math.round(score)) : String.format(Locale.ROOT, "%.1f", score);
   }

   public static double tideborneFishScoreFromParts(double percentile, int stars, String mutation, double length, double recordHigh) {
      return TraitAxesRuntime.scoreFromParts(percentile, stars, mutation, "normal", length, recordHigh);
   }

   public static void tideborneRecordCurrentTopFish(NbtCompound root, UUID catcherId, String catcherName) {
      migrateScoreList(root.getList("top_fish", NbtElement.COMPOUND_TYPE));
      SpecimenData specimen = TeamCanonicalJournalCapture.currentSpecimen().orElse(null);
      if (specimen == null || specimen.fishScore().isEmpty()) {
         return;
      }
      NbtCompound candidate = CanonicalSpecimenRecordIndexer.project(specimen);
      NbtCompound compatibility = TIDEBORNE_CURRENT_FISH.get();
      if (compatibility == null) {
         compatibility = TIDEBORNE_LAST_FISH.get();
      }
      if (compatibility != null) {
         candidate.putInt("fish_stars", compatibility.getInt("fish_stars"));
      }
      candidate.putUuid("catcher_id", catcherId);
      candidate.putString("catcher_name", catcherName == null ? "" : catcherName);
      candidate.putLong("timestamp", System.currentTimeMillis());
      CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, candidate);
   }

   private static boolean migrateStoredScores(NbtCompound root) {
      if (root == null) {
         return false;
      }
      boolean changed = false;
      NbtCompound contributors = root.getCompound("contributors");
      for (String key : contributors.getKeys()) {
         if (contributors.contains(key, NbtElement.COMPOUND_TYPE)) {
            changed |= migrateStoredScore(contributors.getCompound(key));
         }
      }
      changed |= migrateScoreList(root.getList("history", NbtElement.COMPOUND_TYPE));
      changed |= migrateScoreList(root.getList("top_fish", NbtElement.COMPOUND_TYPE));
      return changed;
   }

   private static boolean migrateScoreList(NbtList list) {
      boolean changed = false;
      for (NbtElement element : list) {
         if (element instanceof NbtCompound tag) {
            changed |= migrateStoredScore(tag);
         }
      }
      return changed;
   }

   private static boolean migrateStoredScore(NbtCompound tag) {
      boolean changed = StoredFishScoreStorage.migrateLegacyScore(tag);
      return syncLegacyMirror(tag) | changed;
   }

   /** Compatibility output only; canonical storage remains authoritative. */
   private static boolean syncLegacyMirror(NbtCompound tag) {
      int score = StoredFishScoreStorage.readCanonical(tag).orElse(0);
      if (score <= 0 || tag.getInt("fish_score") == score) {
         return false;
      }
      tag.putInt("fish_score", score);
      return true;
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
         int score = TeamProgressStore.tideborneContributorFishScore(this);
         tag.putInt("fish_score", score);
         StoredFishScoreStorage.writeCanonical(tag, score);
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
         int score = TeamProgressStore.tideborneEventFishScoreForWrite(this);
         tag.putInt("fish_score", score);
         tag.putInt("fish_stars", TeamProgressStore.tideborneEventFishStarsForWrite(this));
         HistoryBadgeMeta.write(this.id(), tag);
         StoredFishScoreStorage.writeCanonical(tag, score);
         return tag;
      }

      public static TeamProgressStore.RecordEvent fromTag(NbtCompound tag) {
         TeamProgressStore.RecordEvent event;
         try {
            event = new TeamProgressStore.RecordEvent(
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

         TeamProgressStore.tideborneRegisterEventMeta(event, tag);
         return event;
      }
   }
}
