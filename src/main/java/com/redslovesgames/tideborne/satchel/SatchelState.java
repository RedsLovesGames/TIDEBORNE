/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.satchel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public final class SatchelState {
   public static final int CURRENT_VERSION = 1;
   private static final String VERSION = "version";
   private static final String CAPACITY_LEVEL = "capacity_level";
   private static final String UNLOCKED_UPGRADES = "unlocked_upgrades";
   private static final String UPGRADE_ENABLED_STATES = "upgrade_enabled_states";
   private static final String SORT_CONFIGURATION = "sort_configuration";
   private static final String ACTIVE_SATCHEL = "active_satchel";
   private static final String OPEN = "open";
   private static final String TROPHY_PROTECTION = "trophy_protection";
   private static final String PROTECTION_RULES = "rules";
   private static final String PROTECTED_SLOTS = "protected_slots";
   private static final String SORT_KEY = "key";
   private static final String SORT_DIRECTION = "direction";
   private final NbtCompound data;

   private SatchelState(NbtCompound data) {
      this.data = data.copy();
   }

   public static SatchelState empty() {
      NbtCompound data = new NbtCompound();
      data.putInt("version", 1);
      data.putInt("capacity_level", SatchelCapacityLevel.BASE.level());
      return new SatchelState(data);
   }

   public static SatchelState fromTag(NbtCompound source) {
      if (source == null) {
         return empty();
      }

      NbtCompound data = source.copy();
      if (!data.contains("version", 99)) {
         data.putInt("version", 1);
      }

      return new SatchelState(data);
   }

   public int version() {
      return this.data.getInt("version");
   }

   public int capacityLevel() {
      int raw = this.data.getInt("capacity_level");
      return SatchelCapacityLevel.byLevel(raw).isPresent() ? raw : SatchelCapacityLevel.BASE.level();
   }

   public SatchelState withCapacityLevel(int level) {
      if (SatchelCapacityLevel.byLevel(level).isEmpty()) {
         throw new IllegalArgumentException("Unknown capacity level: " + level);
      }

      NbtCompound copy = this.data.copy();
      copy.putInt("capacity_level", level);
      return new SatchelState(copy);
   }

   public Set<String> unlockedFeatureIds() {
      NbtList list = this.data.getList("unlocked_upgrades", 8);
      LinkedHashSet<String> ids = new LinkedHashSet<>();

      for (int index = 0; index < list.size(); index++) {
         ids.add(list.getString(index));
      }

      return Collections.unmodifiableSet(ids);
   }

   public boolean isFeatureUnlocked(SatchelFeature feature) {
      return this.isFeatureUnlocked(feature.id());
   }

   public boolean isFeatureUnlocked(String featureId) {
      return this.unlockedFeatureIds().contains(featureId);
   }

   public SatchelState withFeatureUnlocked(SatchelFeature feature) {
      return this.withFeatureUnlocked(feature.id());
   }

   public SatchelState withFeatureUnlocked(String featureId) {
      Objects.requireNonNull(featureId, "featureId");
      LinkedHashSet<String> ids = new LinkedHashSet<>(this.unlockedFeatureIds());
      ids.add(featureId);
      NbtList list = new NbtList();
      ids.forEach(id -> list.add(NbtString.of(id)));
      NbtCompound copy = this.data.copy();
      copy.put("unlocked_upgrades", list);
      return new SatchelState(copy);
   }

   public boolean isFeatureEnabled(SatchelFeature feature) {
      return this.isFeatureEnabled(feature.id());
   }

   public boolean isFeatureEnabled(String featureId) {
      NbtCompound enabledStates = this.data.getCompound("upgrade_enabled_states");
      return enabledStates.contains(featureId, 1) && enabledStates.getBoolean(featureId);
   }

   public SatchelState withFeatureEnabled(SatchelFeature feature, boolean enabled) {
      return this.withFeatureEnabled(feature.id(), enabled);
   }

   public SatchelState withFeatureEnabled(String featureId, boolean enabled) {
      Objects.requireNonNull(featureId, "featureId");
      NbtCompound copy = this.data.copy();
      NbtCompound enabledStates = copy.getCompound("upgrade_enabled_states").copy();
      enabledStates.putBoolean(featureId, enabled);
      copy.put("upgrade_enabled_states", enabledStates);
      return new SatchelState(copy);
   }

   public SatchelSortConfiguration sortConfiguration() {
      NbtList list = this.data.getList("sort_configuration", 10);
      ArrayList<SatchelSortRule> rules = new ArrayList<>(list.size());

      for (int index = 0; index < list.size(); index++) {
         NbtCompound rule = list.getCompound(index);
         SatchelSortKey.byId(rule.getString("key"))
            .ifPresent(key -> rules.add(new SatchelSortRule(key, SatchelSortDirection.byId(rule.getString("direction")))));
      }

      return new SatchelSortConfiguration(rules);
   }

   public SatchelState withSortConfiguration(SatchelSortConfiguration configuration) {
      Objects.requireNonNull(configuration, "configuration");
      NbtList list = new NbtList();

      for (SatchelSortRule rule : configuration.rules()) {
         NbtCompound encoded = new NbtCompound();
         encoded.putString("key", rule.key().id());
         encoded.putString("direction", rule.direction().id());
         list.add(encoded);
      }

      NbtCompound copy = this.data.copy();
      copy.put("sort_configuration", list);
      return new SatchelState(copy);
   }

   public boolean isActive() {
      return this.data.getBoolean("active_satchel");
   }

   public SatchelState withActive(boolean active) {
      NbtCompound copy = this.data.copy();
      copy.putBoolean("active_satchel", active);
      return new SatchelState(copy);
   }

   public boolean isOpen() {
      return this.data.getBoolean("open");
   }

   public SatchelState withOpen(boolean open) {
      NbtCompound copy = this.data.copy();
      copy.putBoolean("open", open);
      return new SatchelState(copy);
   }

   public boolean protectionRuleEnabled(String ruleId) {
      NbtCompound trophy = this.data.getCompound("trophy_protection");
      NbtCompound rules = trophy.getCompound("rules");
      return rules.contains(ruleId, 1) && rules.getBoolean(ruleId);
   }

   public boolean hasProtectionRule(String ruleId) {
      Objects.requireNonNull(ruleId, "ruleId");
      return this.data.getCompound("trophy_protection").getCompound("rules").contains(ruleId, 1);
   }

   public SatchelState withProtectionRule(String ruleId, boolean enabled) {
      Objects.requireNonNull(ruleId, "ruleId");
      NbtCompound copy = this.data.copy();
      NbtCompound trophy = copy.getCompound("trophy_protection").copy();
      NbtCompound rules = trophy.getCompound("rules").copy();
      rules.putBoolean(ruleId, enabled);
      trophy.put("rules", rules);
      copy.put("trophy_protection", trophy);
      return new SatchelState(copy);
   }

   public SatchelState withProtectionDefaults(Map<String, Boolean> defaults) {
      Objects.requireNonNull(defaults, "defaults");
      SatchelState updated = this;

      for (SatchelProtectionRule rule : SatchelProtectionRule.values()) {
         if (!updated.hasProtectionRule(rule.id())) {
            updated = updated.withProtectionRule(rule.id(), Boolean.TRUE.equals(defaults.get(rule.id())));
         }
      }

      return updated;
   }

   public Set<Integer> protectedSlots() {
      NbtCompound trophy = this.data.getCompound("trophy_protection");
      TreeSet<Integer> slots = new TreeSet<>();
      Arrays.stream(trophy.getIntArray("protected_slots")).filter(slot -> slot >= 0).forEach(slots::add);
      return Collections.unmodifiableSet(slots);
   }

   public boolean isSlotProtected(int slot) {
      return slot >= 0 && this.protectedSlots().contains(slot);
   }

   public SatchelState withSlotProtected(int slot, boolean protect) {
      if (slot < 0) {
         throw new IllegalArgumentException("slot must be non-negative");
      }

      TreeSet<Integer> slots = new TreeSet<>(this.protectedSlots());
      if (protect) {
         slots.add(slot);
      } else {
         slots.remove(slot);
      }

      return this.withProtectedSlots(slots);
   }

   SatchelState withProtectedSlots(Set<Integer> slots) {
      NbtCompound copy = this.data.copy();
      NbtCompound trophy = copy.getCompound("trophy_protection").copy();
      trophy.putIntArray("protected_slots", slots.stream().filter(slot -> slot >= 0).sorted().toList());
      copy.put("trophy_protection", trophy);
      return new SatchelState(copy);
   }

   public Optional<UUID> presetRod(int index) {
      if (index < 0) return Optional.empty();
      String key = "legacy_preset_rod_" + index;
      return this.data.containsUuid(key) ? Optional.of(this.data.getUuid(key)) : Optional.empty();
   }

   public SatchelState withPresetRod(int index, UUID rodId) {
      if (index < 0) throw new IllegalArgumentException("index must be non-negative");
      NbtCompound copy = this.data.copy();
      String key = "legacy_preset_rod_" + index;
      if (rodId == null) copy.remove(key); else copy.putUuid(key, rodId);
      return new SatchelState(copy);
   }

   public NbtCompound toTag() {
      return this.data.copy();
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof SatchelState state && this.data.equals(state.data);
   }

   @Override
   public int hashCode() {
      return this.data.hashCode();
   }

   @Override
   public String toString() {
      return "SatchelState" + this.data;
   }
}
