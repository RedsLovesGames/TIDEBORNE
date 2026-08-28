/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel.network;

import com.redslovesgames.tidetraits.satchel.SatchelProtectionRule;
import com.redslovesgames.tidetraits.satchel.SatchelSortDirection;
import com.redslovesgames.tidetraits.satchel.SatchelSortKey;
import com.redslovesgames.tidetraits.satchel.SatchelSortRule;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record SatchelViewPayload(SatchelView view) implements CustomPayload {
   private static final int MAX_ID_LENGTH = 32;
   private static final int MAX_DETAIL_LENGTH = 128;
   private static final int MAX_FEATURES = 16;
   private static final int MAX_CONTENTS = 512;
   private static final int MAX_PROTECTION_RULES = SatchelProtectionRule.values().length;
   public static final Id<SatchelViewPayload> TYPE = new Id(Identifier.of("tide_traits", "anglers_satchel_view"));
   public static final PacketCodec<RegistryByteBuf, SatchelViewPayload> STREAM_CODEC = new PacketCodec<RegistryByteBuf, SatchelViewPayload>() {
      public SatchelViewPayload decode(RegistryByteBuf buffer) {
         int protocol = buffer.readVarInt();
         Hand hand = buffer.readBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND;
         long token = buffer.readLong();
         boolean valid = buffer.readBoolean();
         boolean open = buffer.readBoolean();
         boolean active = buffer.readBoolean();
         boolean multiplayerAvailable = buffer.readBoolean();
         int experiencePoints = buffer.readVarInt();
         int capacityLevel = buffer.readVarInt();
         int capacity = buffer.readVarInt();
         int nextCapacityLevel = buffer.readVarInt();
         int nextCapacityCost = buffer.readVarInt();
         int featureCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), 16, "features");
         List<SatchelFeatureView> features = new ArrayList<>(featureCount);

         for (int index = 0; index < featureCount; index++) {
            features.add(
               new SatchelFeatureView(buffer.readString(32), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean())
            );
         }

         int ruleCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), 6, "sort rules");
         List<SatchelSortRule> sortRules = new ArrayList<>(ruleCount);

         for (int index = 0; index < ruleCount; index++) {
            String keyId = buffer.readString(32);
            String directionId = buffer.readString(32);
            SatchelSortKey key = SatchelSortKey.byId(keyId).orElseThrow(() -> new IllegalArgumentException("Unknown satchel sort key"));
            sortRules.add(new SatchelSortRule(key, SatchelViewPayload.strictDirection(directionId)));
         }

         int contentCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), 512, "contents");
         List<ItemStack> contents = new ArrayList<>(contentCount);

         for (int index = 0; index < contentCount; index++) {
            ItemStack stack = (ItemStack)ItemStack.PACKET_CODEC.decode(buffer);
            if (stack.isEmpty()) {
               throw new IllegalArgumentException("Empty stack in satchel view");
            }

            contents.add(stack);
         }

         int personalRecordCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), 512, "personal records");
         if (personalRecordCount != contentCount) {
            throw new IllegalArgumentException("Personal records do not align with satchel contents");
         }

         List<PersonalRecordView> personalRecords = new ArrayList<>(personalRecordCount);

         for (int index = 0; index < personalRecordCount; index++) {
            boolean available = buffer.readBoolean();
            personalRecords.add(available ? new PersonalRecordView(true, buffer.readDouble(), buffer.readDouble()) : PersonalRecordView.unavailable());
         }

         int protectedCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), contentCount, "protected slots");
         Set<Integer> protectedSlots = new LinkedHashSet<>(protectedCount);

         for (int index = 0; index < protectedCount; index++) {
            int slot = buffer.readVarInt();
            if (slot < 0 || slot >= contentCount) {
               throw new IllegalArgumentException("Invalid protected satchel slot");
            }

            protectedSlots.add(slot);
         }

         int protectionRuleCount = SatchelViewPayload.boundedCount(buffer.readVarInt(), SatchelViewPayload.MAX_PROTECTION_RULES, "protection rules");
         Set<String> protectionRules = new LinkedHashSet<>(protectionRuleCount);

         for (int index = 0; index < protectionRuleCount; index++) {
            String id = buffer.readString(32);
            SatchelProtectionRule.byId(id).orElseThrow(() -> new IllegalArgumentException("Unknown satchel protection rule"));
            if (!protectionRules.add(id)) {
               throw new IllegalArgumentException("Duplicate satchel protection rule");
            }
         }

         SatchelNetworkStatus status = SatchelNetworkStatus.byId(buffer.readString(32));
         String detail = buffer.readString(128);
         return new SatchelViewPayload(
            new SatchelView(
               protocol,
               hand,
               token,
               valid,
               open,
               active,
               multiplayerAvailable,
               experiencePoints,
               capacityLevel,
               capacity,
               nextCapacityLevel,
               nextCapacityCost,
               features,
               sortRules,
               contents,
               personalRecords,
               protectedSlots,
               protectionRules,
               status,
               detail
            )
         );
      }

      public void encode(RegistryByteBuf buffer, SatchelViewPayload payload) {
         SatchelView view = payload.view;
         buffer.writeVarInt(view.protocolVersion());
         buffer.writeBoolean(view.hand() == Hand.OFF_HAND);
         buffer.writeLong(view.stateToken());
         buffer.writeBoolean(view.valid());
         buffer.writeBoolean(view.open());
         buffer.writeBoolean(view.active());
         buffer.writeBoolean(view.multiplayerAvailable());
         buffer.writeVarInt(view.experiencePoints());
         buffer.writeVarInt(view.capacityLevel());
         buffer.writeVarInt(view.capacity());
         buffer.writeVarInt(view.nextCapacityLevel());
         buffer.writeVarInt(view.nextCapacityCost());
         SatchelViewPayload.boundedWriteCount(view.features().size(), 16, "features");
         buffer.writeVarInt(view.features().size());

         for (SatchelFeatureView feature : view.features()) {
            buffer.writeString(feature.id(), 32);
            buffer.writeVarInt(feature.xpCost());
            buffer.writeBoolean(feature.unlocked());
            buffer.writeBoolean(feature.enabled());
            buffer.writeBoolean(feature.available());
         }

         SatchelViewPayload.boundedWriteCount(view.sortRules().size(), 6, "sort rules");
         buffer.writeVarInt(view.sortRules().size());

         for (SatchelSortRule rule : view.sortRules()) {
            buffer.writeString(rule.key().id(), 32);
            buffer.writeString(rule.direction().id(), 32);
         }

         List<ItemStack> contents = view.contents();
         SatchelViewPayload.boundedWriteCount(contents.size(), 512, "contents");
         buffer.writeVarInt(contents.size());

         for (ItemStack stack : contents) {
            if (stack.isEmpty()) {
               throw new IllegalArgumentException("Empty stack in satchel view");
            }

            ItemStack.PACKET_CODEC.encode(buffer, stack);
         }

         SatchelViewPayload.boundedWriteCount(view.personalRecords().size(), contents.size(), "personal records");
         if (view.personalRecords().size() != contents.size()) {
            throw new IllegalArgumentException("Personal records do not align with satchel contents");
         }

         buffer.writeVarInt(view.personalRecords().size());

         for (PersonalRecordView record : view.personalRecords()) {
            buffer.writeBoolean(record.available());
            if (record.available()) {
               buffer.writeDouble(record.largest());
               buffer.writeDouble(record.smallest());
            }
         }

         SatchelViewPayload.boundedWriteCount(view.protectedSlots().size(), contents.size(), "protected slots");
         buffer.writeVarInt(view.protectedSlots().size());

         for (int slot : view.protectedSlots()) {
            if (slot < 0 || slot >= contents.size()) {
               throw new IllegalArgumentException("Invalid protected satchel slot");
            }

            buffer.writeVarInt(slot);
         }

         SatchelViewPayload.boundedWriteCount(view.protectionRules().size(), SatchelViewPayload.MAX_PROTECTION_RULES, "protection rules");
         buffer.writeVarInt(view.protectionRules().size());

         for (String id : view.protectionRules()) {
            SatchelProtectionRule.byId(id).orElseThrow(() -> new IllegalArgumentException("Unknown satchel protection rule"));
            buffer.writeString(id, 32);
         }

         buffer.writeString(view.status().id(), 32);
         buffer.writeString(view.detail(), 128);
      }
   };

   public SatchelViewPayload {
      if (view == null) {
         throw new NullPointerException("view");
      }
   }

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }

   private static int boundedCount(int count, int maximum, String name) {
      if (count >= 0 && count <= maximum) {
         return count;
      } else {
         throw new IllegalArgumentException("Invalid satchel " + name + " count: " + count);
      }
   }

   private static void boundedWriteCount(int count, int maximum, String name) {
      boundedCount(count, maximum, name);
   }

   private static SatchelSortDirection strictDirection(String id) {
      return switch (id) {
         case "asc" -> SatchelSortDirection.ASCENDING;
         case "desc" -> SatchelSortDirection.DESCENDING;
         default -> throw new IllegalArgumentException("Unknown satchel sort direction");
      };
   }
}
