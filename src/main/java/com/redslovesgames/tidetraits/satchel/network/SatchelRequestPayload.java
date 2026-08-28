/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel.network;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.class_9154;

public record SatchelRequestPayload(
   int protocolVersion,
   SatchelRequestPayload.RequestAction action,
   Hand hand,
   long stateToken,
   int intArgument,
   boolean booleanArgument,
   String featureId,
   List<SatchelRequestPayload.SortRuleRequest> sortRules
) implements CustomPayload {
   public static final int PROTOCOL_VERSION = 2;
   public static final int MAX_SORT_RULES = 6;
   private static final int MAX_ID_LENGTH = 32;
   public static final class_9154<SatchelRequestPayload> TYPE = new class_9154(Identifier.of("tide_traits", "anglers_satchel_request"));
   public static final PacketCodec<RegistryByteBuf, SatchelRequestPayload> STREAM_CODEC = new PacketCodec<RegistryByteBuf, SatchelRequestPayload>() {
      public SatchelRequestPayload decode(RegistryByteBuf buffer) {
         int protocol = buffer.readVarInt();
         SatchelRequestPayload.RequestAction action = SatchelRequestPayload.RequestAction.byId(buffer.readString(32))
            .orElse(SatchelRequestPayload.RequestAction.INVALID);
         Hand hand = buffer.readBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
         long token = buffer.readLong();
         int intArgument = buffer.readVarInt();
         boolean booleanArgument = buffer.readBoolean();
         String featureId = buffer.readString(32);
         int ruleCount = buffer.readVarInt();
         if (ruleCount >= 0 && ruleCount <= 6) {
            List<SatchelRequestPayload.SortRuleRequest> rules = IntStream.range(0, ruleCount)
               .mapToObj(ignored -> new SatchelRequestPayload.SortRuleRequest(buffer.readString(32), buffer.readString(32)))
               .toList();
            return new SatchelRequestPayload(protocol, action, hand, token, intArgument, booleanArgument, featureId, rules);
         } else {
            throw new IllegalArgumentException("Invalid satchel sort-rule count: " + ruleCount);
         }
      }

      public void encode(RegistryByteBuf buffer, SatchelRequestPayload payload) {
         if (payload.sortRules.size() > 6) {
            throw new IllegalArgumentException("Too many satchel sort rules");
         }

         buffer.writeVarInt(payload.protocolVersion);
         buffer.writeString(payload.action.id(), 32);
         buffer.writeBoolean(payload.hand == Hand.OFF_HAND);
         buffer.writeLong(payload.stateToken);
         buffer.writeVarInt(payload.intArgument);
         buffer.writeBoolean(payload.booleanArgument);
         buffer.writeString(payload.featureId, 32);
         buffer.writeVarInt(payload.sortRules.size());

         for (SatchelRequestPayload.SortRuleRequest rule : payload.sortRules) {
            buffer.writeString(rule.keyId, 32);
            buffer.writeString(rule.directionId, 32);
         }
      }
   };

   public SatchelRequestPayload {
      action = Objects.requireNonNull(action, "action");
      hand = Objects.requireNonNull(hand, "hand");
      featureId = Objects.requireNonNullElse(featureId, "");
      sortRules = List.copyOf(Objects.requireNonNullElse(sortRules, List.of()));
   }

   public static SatchelRequestPayload simple(SatchelRequestPayload.RequestAction action, Hand hand, long token) {
      return new SatchelRequestPayload(2, action, hand, token, 0, false, "", List.of());
   }

   public static SatchelRequestPayload feature(SatchelRequestPayload.RequestAction action, Hand hand, long token, String featureId, boolean enabled) {
      return new SatchelRequestPayload(2, action, hand, token, 0, enabled, featureId, List.of());
   }

   public static SatchelRequestPayload slot(SatchelRequestPayload.RequestAction action, Hand hand, long token, int slot, boolean value) {
      return new SatchelRequestPayload(2, action, hand, token, slot, value, "", List.of());
   }

   public static SatchelRequestPayload sort(Hand hand, long token, List<SatchelRequestPayload.SortRuleRequest> rules) {
      return new SatchelRequestPayload(2, SatchelRequestPayload.RequestAction.SORT, hand, token, 0, false, "", rules);
   }

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }

   public enum RequestAction {
      REFRESH("refresh"),
      CLOSE("close"),
      PURCHASE_FEATURE("purchase_feature"),
      TOGGLE_FEATURE("toggle_feature"),
      PURCHASE_NEXT_CAPACITY("purchase_next_capacity"),
      SORT("sort"),
      EXTRACT("extract"),
      SET_PROTECTED("set_protected"),
      SET_PROTECTION_RULE("set_protection_rule"),
      TOGGLE_ACTIVE("toggle_active"),
      INVALID("invalid");

      private final String id;

      RequestAction(String id) {
         this.id = id;
      }

      public String id() {
         return this.id;
      }

      public static Optional<SatchelRequestPayload.RequestAction> byId(String id) {
         if (id == null) {
            return Optional.empty();
         }

         String normalized = id.toLowerCase(Locale.ROOT);
         return Arrays.stream(values()).filter(value -> value != INVALID && value.id.equals(normalized)).findFirst();
      }
   }

   public record SortRuleRequest(String keyId, String directionId) {
      public SortRuleRequest {
         keyId = Objects.requireNonNullElse(keyId, "");
         directionId = Objects.requireNonNullElse(directionId, "");
      }
   }
}
