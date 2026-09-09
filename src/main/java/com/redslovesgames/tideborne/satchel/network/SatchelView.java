/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel.network;

import com.redslovesgames.tideborne.satchel.SatchelSortRule;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public record SatchelView(
   int protocolVersion,
   Hand hand,
   long stateToken,
   boolean valid,
   boolean open,
   boolean active,
   boolean multiplayerAvailable,
   int experiencePoints,
   int capacityLevel,
   int capacity,
   int nextCapacityLevel,
   int nextCapacityCost,
   List<SatchelFeatureView> features,
   List<SatchelSortRule> sortRules,
   List<ItemStack> contents,
   List<PersonalRecordView> personalRecords,
   Set<Integer> protectedSlots,
   Set<String> protectionRules,
   SatchelNetworkStatus status,
   String detail,
   net.minecraft.nbt.NbtCompound tackle
) {
   /** Compatibility constructor for the unchanged version-2 fields. */
   public SatchelView(
   int protocolVersion,
   Hand hand,
   long stateToken,
   boolean valid,
   boolean open,
   boolean active,
   boolean multiplayerAvailable,
   int experiencePoints,
   int capacityLevel,
   int capacity,
   int nextCapacityLevel,
   int nextCapacityCost,
   List<SatchelFeatureView> features,
   List<SatchelSortRule> sortRules,
   List<ItemStack> contents,
   List<PersonalRecordView> personalRecords,
   Set<Integer> protectedSlots,
   Set<String> protectionRules,
   SatchelNetworkStatus status,
   String detail
   ) {
      this(protocolVersion, hand, stateToken, valid, open, active, multiplayerAvailable, experiencePoints, capacityLevel, capacity, nextCapacityLevel, nextCapacityCost, features, sortRules, contents, personalRecords, protectedSlots, protectionRules, status, detail, new net.minecraft.nbt.NbtCompound());
   }

   public net.minecraft.nbt.NbtCompound tackle() { return tackle.copy(); }

   public SatchelView {
      tackle = tackle == null ? new net.minecraft.nbt.NbtCompound() : tackle.copy();
      hand = Objects.requireNonNull(hand, "hand");
      features = List.copyOf(Objects.requireNonNullElse(features, List.of()));
      sortRules = List.copyOf(Objects.requireNonNullElse(sortRules, List.of()));
      contents = copyStacks(Objects.requireNonNullElse(contents, List.of()));
      personalRecords = List.copyOf(Objects.requireNonNullElse(personalRecords, List.of()));
      if (personalRecords.size() != contents.size()) {
         throw new IllegalArgumentException("Personal record views must align with satchel contents");
      }

      protectedSlots = Set.copyOf(new TreeSet<>(Objects.requireNonNullElse(protectedSlots, Set.of())));
      protectionRules = Set.copyOf(new TreeSet<>(Objects.requireNonNullElse(protectionRules, Set.of())));
      status = Objects.requireNonNull(status, "status");
      detail = Objects.requireNonNullElse(detail, "");
   }

   public List<ItemStack> contents() {
      return copyStacks(this.contents);
   }

   public ItemStack stackAt(int slot) {
      return slot >= 0 && slot < this.contents.size() ? this.contents.get(slot).copy() : ItemStack.EMPTY;
   }

   public Optional<PersonalRecordView> personalRecordAt(int slot) {
      if (slot >= 0 && slot < this.personalRecords.size()) {
         PersonalRecordView record = this.personalRecords.get(slot);
         return record.available() ? Optional.of(record) : Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public int size() {
      return this.contents.size();
   }

   public boolean isProtected(int slot) {
      return this.protectedSlots.contains(slot);
   }

   public boolean protectionRuleEnabled(String id) {
      return this.protectionRules.contains(id);
   }

   public SatchelFeatureView feature(String id) {
      return this.features.stream().filter(feature -> feature.id().equals(id)).findFirst().orElse(null);
   }

   private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
      return stacks.stream().map(stack -> stack == null ? ItemStack.EMPTY : stack.copy()).toList();
   }
}
