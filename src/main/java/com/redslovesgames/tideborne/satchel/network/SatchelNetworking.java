/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel.network;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tideborne.discovery.multiplayer.PersonalTideJournal;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import com.redslovesgames.tideborne.satchel.AnglersSatchelItem;
import com.redslovesgames.tideborne.satchel.AnglersSatchelStorage;
import com.redslovesgames.tideborne.satchel.SatchelCapacityLevel;
import com.redslovesgames.tideborne.satchel.SatchelFeature;
import com.redslovesgames.tideborne.satchel.SatchelProtectionRule;
import com.redslovesgames.tideborne.satchel.SatchelPurchaseResult;
import com.redslovesgames.tideborne.satchel.SatchelPurchaseService;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import com.redslovesgames.tideborne.satchel.SatchelService;
import com.redslovesgames.tideborne.satchel.SatchelSortConfiguration;
import com.redslovesgames.tideborne.satchel.SatchelSortKey;
import com.redslovesgames.tideborne.satchel.SatchelSortRule;
import com.redslovesgames.tideborne.satchel.SatchelSorting;
import com.redslovesgames.tideborne.satchel.SatchelState;
import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import com.redslovesgames.tideborne.fishing.specimen.legacy.TraitAxesRuntime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.minecraft.util.Hand;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SatchelNetworking {
   private static final AtomicLong TOKENS = new AtomicLong(91980027727872L);
   private static final Map<UUID, SatchelNetworking.Session> SESSIONS = new HashMap<>();
   private static boolean payloadsRegistered;
   private static boolean initialized;

   private SatchelNetworking() {
   }

   public static synchronized void initServer() {
      ensurePayloadsRegistered();
      if (!initialized) {
         ServerPlayNetworking.registerGlobalReceiver(
            SatchelRequestPayload.TYPE, (payload, context) -> context.server().execute(() -> handle(context.player(), payload))
         );
         ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> closeSession(handler.getPlayer()));
         ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> closeSession(oldPlayer));
         AnglersSatchelItem.registerScreenOpener(SatchelNetworking::openHeld);
         initialized = true;
      }
   }

   public static synchronized void ensurePayloadsRegistered() {
      if (!payloadsRegistered) {
         PayloadTypeRegistry.playC2S().register(SatchelRequestPayload.TYPE, SatchelRequestPayload.STREAM_CODEC);
         PayloadTypeRegistry.playS2C().register(SatchelViewPayload.TYPE, SatchelViewPayload.STREAM_CODEC);
         payloadsRegistered = true;
      }
   }

   private static boolean openHeld(ServerPlayerEntity player, Hand hand, ItemStack stack) {
      if (player == null || hand == null || stack == null || player.getStackInHand(hand) != stack || !SatchelRegistration.isAnglersSatchel(stack)) {
         return false;
      } else if (!ServerPlayNetworking.canSend(player, SatchelViewPayload.TYPE)) {
         SatchelService.setOpen(stack, false);
         return false;
      } else {
         closeSession(player);
         SatchelService.setOpen(stack, true);
         SatchelNetworking.Session session = new SatchelNetworking.Session(hand, stack, nextToken(), fingerprint(stack));
         SESSIONS.put(player.getUuid(), session);
         sendFull(player, session, SatchelNetworkStatus.OPENED, "");
         return true;
      }
   }

   private static void handle(ServerPlayerEntity player, SatchelRequestPayload request) {
      if (request.protocolVersion() == 2 && request.action() != SatchelRequestPayload.RequestAction.INVALID) {
         SatchelNetworking.Session session = SESSIONS.get(player.getUuid());
         if (request.action() == SatchelRequestPayload.RequestAction.CLOSE) {
            if (session != null) {
               if (request.hand() != session.hand) {
                  return;
               }

               boolean exactHeld = isExactHeldSession(player, request.hand(), session);
               closeSession(player);
               sendInvalid(player, request.hand(), exactHeld ? SatchelNetworkStatus.CLOSED : SatchelNetworkStatus.HELD_ITEM_CHANGED, "");
            } else {
               sendInvalid(player, request.hand(), SatchelNetworkStatus.CLOSED, "");
            }
         } else if (!isExactHeldSession(player, request.hand(), session)) {
            closeSession(player);
            sendInvalid(player, request.hand(), SatchelNetworkStatus.HELD_ITEM_CHANGED, "");
         } else if (request.action() == SatchelRequestPayload.RequestAction.REFRESH) {
            sendFull(player, session, SatchelNetworkStatus.REFRESHED, "");
         } else if (request.stateToken() == session.token && fingerprint(session.stack) == session.fingerprint) {
            SatchelNetworking.Result result = switch (request.action()) {
               case PURCHASE_FEATURE -> purchaseFeature(player, session, request.featureId());
               case TOGGLE_FEATURE -> toggleFeature(player, session, request.featureId(), request.booleanArgument());
               case PURCHASE_NEXT_CAPACITY -> purchaseNextCapacity(player, session);
               case SORT -> sort(session, request.sortRules());
               case EXTRACT -> extract(player, session, request.intArgument());
               case SET_PROTECTED -> setProtected(session, request.intArgument(), request.booleanArgument());
               case SET_PROTECTION_RULE -> setProtectionRule(session, request.featureId(), request.booleanArgument());
               case TOGGLE_ACTIVE -> toggleActive(player, session);
               default -> new SatchelNetworking.Result(SatchelNetworkStatus.INVALID_REQUEST, "action");
            };
            player.getInventory().markDirty();
            sendFull(player, session, result.status, result.detail);
         } else {
            sendFull(player, session, SatchelNetworkStatus.STALE_VIEW, "refresh_and_retry");
         }
      } else {
         closeSession(player);
         sendInvalid(player, request.hand(), SatchelNetworkStatus.INVALID_REQUEST, "protocol");
      }
   }

   private static SatchelNetworking.Result purchaseFeature(ServerPlayerEntity player, SatchelNetworking.Session session, String featureId) {
      SatchelFeature feature = SatchelFeature.byId(featureId).orElse(null);
      if (feature == null) {
         return invalid("feature");
      }

      boolean multiplayerAvailable = multiplayerAvailable(player);
      return feature.requiresMultiplayerExtras() && !multiplayerAvailable
         ? status(SatchelNetworkStatus.PREREQUISITE_MISSING)
         : purchaseResult(SatchelPurchaseService.purchaseHeldFeature(player, session.hand, feature, multiplayerAvailable));
   }

   private static SatchelNetworking.Result toggleFeature(ServerPlayerEntity player, SatchelNetworking.Session session, String featureId, boolean enabled) {
      SatchelFeature feature = SatchelFeature.byId(featureId).orElse(null);
      if (feature == null) {
         return invalid("feature");
      } else {
         SatchelState state = AnglersSatchelStorage.state(session.stack);
         if (!state.isFeatureUnlocked(feature)) {
            return status(SatchelNetworkStatus.FEATURE_LOCKED);
         } else if (enabled && feature.requiresMultiplayerExtras() && !multiplayerAvailable(player)) {
            return status(SatchelNetworkStatus.PREREQUISITE_MISSING);
         } else {
            return SatchelPurchaseService.setHeldFeatureEnabled(player, session.hand, feature, enabled)
               ? status(SatchelNetworkStatus.SUCCESS)
               : status(SatchelNetworkStatus.COMMIT_FAILED);
         }
      }
   }

   private static SatchelNetworking.Result purchaseNextCapacity(ServerPlayerEntity player, SatchelNetworking.Session session) {
      SatchelState state = AnglersSatchelStorage.state(session.stack);
      Optional<SatchelCapacityLevel> next = SatchelCapacityLevel.byLevel(state.capacityLevel()).flatMap(SatchelCapacityLevel::next);
      return next.isEmpty()
         ? status(SatchelNetworkStatus.MAX_CAPACITY)
         : purchaseResult(SatchelPurchaseService.purchaseHeldCapacityLevel(player, session.hand, next.get().level()));
   }

   private static SatchelNetworking.Result sort(SatchelNetworking.Session session, List<SatchelRequestPayload.SortRuleRequest> requestedRules) {
      SatchelState state = AnglersSatchelStorage.state(session.stack);
      if (!state.isFeatureUnlocked(SatchelFeature.TACKLE_ORGANIZER)) {
         return status(SatchelNetworkStatus.FEATURE_LOCKED);
      }

      if (!state.isFeatureEnabled(SatchelFeature.TACKLE_ORGANIZER)) {
         return status(SatchelNetworkStatus.FEATURE_DISABLED);
      }

      Optional<SatchelSortConfiguration> configuration = SatchelRequestValidator.sortConfiguration(requestedRules);
      if (configuration.isEmpty()) {
         return invalid("sort_rules");
      }

      AnglersSatchelStorage.SortStatus sortStatus = AnglersSatchelStorage.sort(
         session.stack, configuration.get(), new SatchelSorting.TideMetadataResolver(SatchelNetworking::traitSortData)
      );

      return switch (sortStatus) {
         case SUCCESS -> status(SatchelNetworkStatus.SUCCESS);
         case ORGANIZER_LOCKED -> status(SatchelNetworkStatus.FEATURE_LOCKED);
         case INVALID_SATCHEL -> invalid("satchel");
         case COMMIT_FAILED -> status(SatchelNetworkStatus.COMMIT_FAILED);
      };
   }

   private static SatchelNetworking.Result extract(ServerPlayerEntity player, SatchelNetworking.Session session, int slot) {
      int size = AnglersSatchelStorage.size(session.stack);
      if (slot < 0 || slot >= size) {
         return status(SatchelNetworkStatus.INVALID_SLOT);
      }

      if (AnglersSatchelStorage.isProtected(session.stack, slot)) {
         return status(SatchelNetworkStatus.PROTECTED);
      }

      AnglersSatchelStorage.ExtractionResult extraction = AnglersSatchelStorage.extractAt(session.stack, slot, false);
      if (extraction.status() != AnglersSatchelStorage.ExtractionStatus.SUCCESS) {
         return switch (extraction.status()) {
            case INVALID_SLOT, EMPTY -> status(SatchelNetworkStatus.INVALID_SLOT);
            case PROTECTED -> status(SatchelNetworkStatus.PROTECTED);
            default -> status(SatchelNetworkStatus.COMMIT_FAILED);
         };
      } else {
         ItemStack extracted = extraction.extracted();
         ItemStack remainder = extracted.copy();
         player.getInventory().insertStack(remainder);
         if (remainder.isEmpty()) {
            return status(SatchelNetworkStatus.SUCCESS);
         }

         ItemEntity dropped = player.dropItem(remainder.copy(), false);
         if (dropped != null) {
            return status(SatchelNetworkStatus.SUCCESS);
         }

         AnglersSatchelStorage.InsertionResult rollback = AnglersSatchelStorage.insert(session.stack, remainder);
         if (rollback.fullyInserted()) {
            return new SatchelNetworking.Result(SatchelNetworkStatus.COMMIT_FAILED, "extraction_rolled_back");
         }

         player.getInventory().offer(rollback.remainder(), false);
         return new SatchelNetworking.Result(SatchelNetworkStatus.COMMIT_FAILED, "delivery_fallback");
      }
   }

   private static SatchelNetworking.Result setProtected(SatchelNetworking.Session session, int slot, boolean protect) {
      int size = AnglersSatchelStorage.size(session.stack);
      if (slot >= 0 && slot < size) {
         SatchelState state = AnglersSatchelStorage.state(session.stack);
         if (!state.isFeatureUnlocked(SatchelFeature.TROPHY_LOCK)) {
            return status(SatchelNetworkStatus.FEATURE_LOCKED);
         } else if (protect && !state.isFeatureEnabled(SatchelFeature.TROPHY_LOCK)) {
            return status(SatchelNetworkStatus.FEATURE_DISABLED);
         } else {
            return AnglersSatchelStorage.setProtected(session.stack, slot, protect)
               ? status(SatchelNetworkStatus.SUCCESS)
               : status(SatchelNetworkStatus.COMMIT_FAILED);
         }
      } else {
         return status(SatchelNetworkStatus.INVALID_SLOT);
      }
   }

   private static SatchelNetworking.Result setProtectionRule(SatchelNetworking.Session session, String ruleId, boolean enabled) {
      SatchelProtectionRule rule = SatchelProtectionRule.byId(ruleId).orElse(null);
      if (rule == null) {
         return invalid("protection_rule");
      }

      SatchelState state = AnglersSatchelStorage.state(session.stack);
      if (!state.isFeatureUnlocked(SatchelFeature.TROPHY_LOCK)) {
         return status(SatchelNetworkStatus.FEATURE_LOCKED);
      }

      if (enabled && !state.isFeatureEnabled(SatchelFeature.TROPHY_LOCK)) {
         return status(SatchelNetworkStatus.FEATURE_DISABLED);
      }

      AnglersSatchelStorage.setState(session.stack, state.withProtectionRule(rule.id(), enabled));
      return status(SatchelNetworkStatus.SUCCESS);
   }

   private static SatchelNetworking.Result toggleActive(ServerPlayerEntity player, SatchelNetworking.Session session) {
      SatchelService.toggleExclusiveActive(player, session.stack);
      return status(SatchelNetworkStatus.SUCCESS);
   }

   private static SatchelSorting.TraitData traitSortData(ItemStack stack) {
      String mutationId = TraitAxesRuntime.condition(stack);
      double probability = FishMutation.bySerializedName(mutationId)
         .map(TideTraitsConfigManager.current().mutations()::probability)
         .orElse(1.7976931348623157E308);
      return new SatchelSorting.TraitData(
         mutationId,
         probability,
         (Double)stack.getOrDefault(TideTraitsComponents.SIZE_PERCENTILE, -1.0),
         (Long)stack.getOrDefault(TideTraitsComponents.MUTATION_SEED, 0L)
      );
   }

   private static SatchelNetworking.Result purchaseResult(SatchelPurchaseResult result) {
      return switch (result.status()) {
         case SUCCESS -> status(SatchelNetworkStatus.SUCCESS);
         case INVALID_REQUEST, INVALID_SATCHEL -> status(SatchelNetworkStatus.INVALID_REQUEST);
         case ALREADY_OWNED -> status(SatchelNetworkStatus.ALREADY_OWNED);
         case PREREQUISITE_MISSING -> status(SatchelNetworkStatus.PREREQUISITE_MISSING);
         case PREVIOUS_LEVEL_REQUIRED -> status(SatchelNetworkStatus.PREVIOUS_LEVEL_REQUIRED);
         case INSUFFICIENT_XP -> status(SatchelNetworkStatus.INSUFFICIENT_XP);
         case COMMIT_FAILED -> status(SatchelNetworkStatus.COMMIT_FAILED);
      };
   }

   private static void sendFull(ServerPlayerEntity player, SatchelNetworking.Session session, SatchelNetworkStatus status, String detail) {
      if (!ServerPlayNetworking.canSend(player, SatchelViewPayload.TYPE)) {
         closeSession(player);
      } else {
         session.token = nextToken();
         session.fingerprint = fingerprint(session.stack);
         ServerPlayNetworking.send(player, new SatchelViewPayload(buildView(player, session, status, detail)));
      }
   }

   private static SatchelView buildView(ServerPlayerEntity player, SatchelNetworking.Session session, SatchelNetworkStatus status, String detail) {
      SatchelState state = AnglersSatchelStorage.state(session.stack);
      boolean multiplayerAvailable = multiplayerAvailable(player);
      List<SatchelFeatureView> features = Arrays.stream(SatchelFeature.values())
         .map(
            feature -> new SatchelFeatureView(
               feature.id(),
               feature.xpCost(),
               state.isFeatureUnlocked(feature),
               state.isFeatureEnabled(feature),
               !feature.requiresMultiplayerExtras() || multiplayerAvailable
            )
         )
         .toList();
      Optional<SatchelCapacityLevel> next = SatchelCapacityLevel.byLevel(state.capacityLevel()).flatMap(SatchelCapacityLevel::next);
      List<ItemStack> contents = AnglersSatchelStorage.contents(session.stack);
      TidePlayerData personalData = PersonalTideJournal.load(player).orElse(null);
      List<PersonalRecordView> personalRecords = contents.stream()
         .map(stack -> PersonalTideJournal.statsFor(personalData, stack).map(PersonalRecordView::from).orElseGet(PersonalRecordView::unavailable))
         .toList();
      Set<Integer> protectedSlots = IntStream.range(0, contents.size())
         .filter(slot -> state.isSlotProtected(slot) || Boolean.TRUE.equals(contents.get(slot).get(TideTraitsComponents.PROTECTED)))
         .boxed()
         .collect(Collectors.toCollection(LinkedHashSet::new));
      Set<String> protectionRules = Arrays.stream(SatchelProtectionRule.values())
         .filter(rulex -> state.protectionRuleEnabled(rulex.id()))
         .map(SatchelProtectionRule::id)
         .collect(Collectors.toCollection(LinkedHashSet::new));
      EnumSet<SatchelSortKey> seen = EnumSet.noneOf(SatchelSortKey.class);
      List<SatchelSortRule> sortRules = new ArrayList<>();

      for (SatchelSortRule rule : state.sortConfiguration().rules()) {
         if (sortRules.size() >= 6) {
            break;
         }

         if (seen.add(rule.key())) {
            sortRules.add(rule);
         }
      }

      return new SatchelView(
         2,
         session.hand,
         session.token,
         true,
         state.isOpen(),
         state.isActive(),
         multiplayerAvailable,
         Math.max(0, player.totalExperience),
         state.capacityLevel(),
         AnglersSatchelStorage.capacity(session.stack),
         next.map(SatchelCapacityLevel::level).orElse(-1),
         next.map(SatchelCapacityLevel::xpCost).orElse(-1),
         features,
         sortRules,
         contents,
         personalRecords,
         protectedSlots,
         protectionRules,
         status,
         detail
      );
   }

   private static void closeSession(ServerPlayerEntity player) {
      if (player != null) {
         SatchelNetworking.Session session = SESSIONS.remove(player.getUuid());
         if (session != null) {
            if (SatchelRegistration.isAnglersSatchel(session.stack)) {
               SatchelService.setOpen(session.stack, false);
               player.getInventory().markDirty();
            }
         }
      }
   }

   private static void sendInvalid(ServerPlayerEntity player, Hand hand, SatchelNetworkStatus status, String detail) {
      if (player != null && ServerPlayNetworking.canSend(player, SatchelViewPayload.TYPE)) {
         SatchelView view = new SatchelView(
            2,
            hand == null ? Hand.MAIN_HAND : hand,
            0L,
            false,
            false,
            false,
            multiplayerAvailable(player),
            Math.max(0, player.totalExperience),
            0,
            0,
            -1,
            -1,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            Set.of(),
            Set.of(),
            status,
            detail
         );
         ServerPlayNetworking.send(player, new SatchelViewPayload(view));
      }
   }

   private static boolean isExactHeldSession(ServerPlayerEntity player, Hand requestedHand, SatchelNetworking.Session session) {
      return session != null
         && session.hand == requestedHand
         && player.getStackInHand(session.hand) == session.stack
         && SatchelRegistration.isAnglersSatchel(session.stack);
   }

   private static boolean multiplayerAvailable(ServerPlayerEntity player) {
      try {
         return MultiplayerDiscoveryCompat.isAvailable(player);
      } catch (RuntimeException | LinkageError ignored) {
         return false;
      }
   }

   private static int fingerprint(ItemStack stack) {
      return 31 * ItemStack.hashCode(stack) + stack.getCount();
   }

   private static long nextToken() {
      return TOKENS.updateAndGet(value -> value == 9223372036854775807L ? 1L : value + 1L);
   }

   private static SatchelNetworking.Result invalid(String detail) {
      return new SatchelNetworking.Result(SatchelNetworkStatus.INVALID_REQUEST, detail);
   }

   private static SatchelNetworking.Result status(SatchelNetworkStatus status) {
      return new SatchelNetworking.Result(status, "");
   }

   private record Result(SatchelNetworkStatus status, String detail) {
   }

   private static final class Session {
      private final Hand hand;
      private final ItemStack stack;
      private long token;
      private int fingerprint;

      private Session(Hand hand, ItemStack stack, long token, int fingerprint) {
         this.hand = hand;
         this.stack = stack;
         this.token = token;
         this.fingerprint = fingerprint;
      }
   }
}
