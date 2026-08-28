/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel.network;

import java.util.Arrays;
import java.util.Locale;

public enum SatchelNetworkStatus {
   OPENED("opened"),
   REFRESHED("refreshed"),
   CLOSED("closed"),
   SUCCESS("success"),
   STALE_VIEW("stale_view"),
   INVALID_REQUEST("invalid_request"),
   HELD_ITEM_CHANGED("held_item_changed"),
   CLIENT_UNAVAILABLE("client_unavailable"),
   FEATURE_LOCKED("feature_locked"),
   FEATURE_DISABLED("feature_disabled"),
   PREREQUISITE_MISSING("prerequisite_missing"),
   INSUFFICIENT_XP("insufficient_xp"),
   ALREADY_OWNED("already_owned"),
   PREVIOUS_LEVEL_REQUIRED("previous_level_required"),
   MAX_CAPACITY("max_capacity"),
   INVALID_SLOT("invalid_slot"),
   PROTECTED("protected"),
   COMMIT_FAILED("commit_failed");

   private final String id;

   SatchelNetworkStatus(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static SatchelNetworkStatus byId(String id) {
      if (id == null) {
         return INVALID_REQUEST;
      }

      String normalized = id.toLowerCase(Locale.ROOT);
      return Arrays.stream(values()).filter(value -> value.id.equals(normalized)).findFirst().orElse(INVALID_REQUEST);
   }
}
