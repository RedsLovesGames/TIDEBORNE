/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.client.gui.satchel;

import com.redslovesgames.tidetraits.satchel.network.SatchelNetworking;
import com.redslovesgames.tidetraits.satchel.network.SatchelRequestPayload;
import com.redslovesgames.tidetraits.satchel.network.SatchelView;
import com.redslovesgames.tidetraits.satchel.network.SatchelViewPayload;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public final class SatchelClientNetworking {
   private static volatile SatchelView current;
   private static boolean initialized;

   private SatchelClientNetworking() {
   }

   public static synchronized void initClient() {
      if (!initialized) {
         SatchelNetworking.ensurePayloadsRegistered();
         ClientPlayNetworking.registerGlobalReceiver(
            SatchelViewPayload.TYPE, (payload, context) -> context.client().execute(() -> accept(context.client(), payload.view()))
         );
         ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> current = null);
         initialized = true;
      }
   }

   public static SatchelView currentView() {
      return current;
   }

   public static boolean refresh(SatchelView view) {
      return send(SatchelRequestPayload.simple(SatchelRequestPayload.RequestAction.REFRESH, view.hand(), view.stateToken()));
   }

   public static boolean purchaseFeature(SatchelView view, String featureId) {
      return send(SatchelRequestPayload.feature(SatchelRequestPayload.RequestAction.PURCHASE_FEATURE, view.hand(), view.stateToken(), featureId, false));
   }

   public static boolean toggleFeature(SatchelView view, String featureId, boolean enabled) {
      return send(SatchelRequestPayload.feature(SatchelRequestPayload.RequestAction.TOGGLE_FEATURE, view.hand(), view.stateToken(), featureId, enabled));
   }

   public static boolean purchaseNextCapacity(SatchelView view) {
      return send(SatchelRequestPayload.simple(SatchelRequestPayload.RequestAction.PURCHASE_NEXT_CAPACITY, view.hand(), view.stateToken()));
   }

   public static boolean sort(SatchelView view, List<SatchelRequestPayload.SortRuleRequest> rules) {
      return send(SatchelRequestPayload.sort(view.hand(), view.stateToken(), rules));
   }

   public static boolean extract(SatchelView view, int slot) {
      return send(SatchelRequestPayload.slot(SatchelRequestPayload.RequestAction.EXTRACT, view.hand(), view.stateToken(), slot, false));
   }

   public static boolean setProtected(SatchelView view, int slot, boolean protect) {
      return send(SatchelRequestPayload.slot(SatchelRequestPayload.RequestAction.SET_PROTECTED, view.hand(), view.stateToken(), slot, protect));
   }

   public static boolean setProtectionRule(SatchelView view, String ruleId, boolean enabled) {
      return send(SatchelRequestPayload.feature(SatchelRequestPayload.RequestAction.SET_PROTECTION_RULE, view.hand(), view.stateToken(), ruleId, enabled));
   }

   public static boolean toggleActive(SatchelView view) {
      return send(SatchelRequestPayload.simple(SatchelRequestPayload.RequestAction.TOGGLE_ACTIVE, view.hand(), view.stateToken()));
   }

   public static void close(Hand hand, long stateToken) {
      send(SatchelRequestPayload.simple(SatchelRequestPayload.RequestAction.CLOSE, hand, stateToken));
      current = null;
   }

   private static boolean send(SatchelRequestPayload payload) {
      try {
         if (!ClientPlayNetworking.canSend(SatchelRequestPayload.TYPE)) {
            return false;
         }

         ClientPlayNetworking.send(payload);
         return true;
      } catch (IllegalStateException ignored) {
         return false;
      }
   }

   private static void accept(MinecraftClient client, SatchelView view) {
      if (view.protocolVersion() != 2) {
         current = null;
         rejectCurrent(client, Text.literal("Angler's Satchel protocol mismatch"));
      } else if (view.valid() && view.open()) {
         current = view;
         if (client.currentScreen instanceof AnglersSatchelScreen screen && screen.hand() == view.hand()) {
            screen.updateFromServer(view);
         } else {
            client.setScreen(new AnglersSatchelScreen(view));
         }
      } else {
         current = null;
         rejectCurrent(client, Text.literal(statusText(view)));
      }
   }

   private static void rejectCurrent(MinecraftClient client, Text reason) {
      if (client.currentScreen instanceof AnglersSatchelScreen screen) {
         screen.closeFromServer();
         client.setScreen(null);
      }

      if (client.player != null) {
         client.player.sendMessage(reason, true);
      }
   }

   static String statusText(SatchelView view) {
      return switch (view.status()) {
         case CLOSED -> "Angler's Satchel closed";
         case HELD_ITEM_CHANGED -> "Satchel closed: keep the same satchel in the same hand";
         case STALE_VIEW -> "Satchel changed; refreshed from server";
         case FEATURE_LOCKED -> "That satchel feature is locked";
         case FEATURE_DISABLED -> "Enable that satchel feature first";
         case PREREQUISITE_MISSING -> "Tide Multiplayer Extras is not available on this server";
         case INSUFFICIENT_XP -> "Not enough XP points";
         case ALREADY_OWNED -> "Upgrade already owned";
         case PREVIOUS_LEVEL_REQUIRED -> "Purchase the previous capacity level first";
         case MAX_CAPACITY -> "Satchel capacity is already maxed";
         case INVALID_SLOT -> "That specimen slot is no longer available";
         case PROTECTED -> "Unprotect that specimen before extracting it";
         case COMMIT_FAILED -> "The server could not safely apply that change";
         case INVALID_REQUEST -> "The server rejected that satchel request";
         case CLIENT_UNAVAILABLE -> "Satchel networking is unavailable";
         case OPENED -> "Satchel opened";
         case REFRESHED -> "Satchel refreshed";
         case SUCCESS -> "Saved";
      };
   }
}
