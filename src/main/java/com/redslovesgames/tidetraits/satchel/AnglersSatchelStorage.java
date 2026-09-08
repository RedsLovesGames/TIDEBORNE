/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.registries.items.FishSatchelItem;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.LegacyPersistenceMigration;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class AnglersSatchelStorage {
   private AnglersSatchelStorage() {
   }

   public static SatchelState state(ItemStack satchel) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return SatchelState.empty();
      }

      NbtCompound data = (NbtCompound)satchel.getOrDefault(SatchelRegistration.SATCHEL_STATE, SatchelState.empty().toTag());
      return SatchelState.fromTag(data);
   }

   public static void setState(ItemStack satchel, SatchelState state) {
      requireSatchel(satchel);
      satchel.set(SatchelRegistration.SATCHEL_STATE, state.toTag());
   }

   public static int capacity(ItemStack satchel) {
      requireSatchel(satchel);
      return SatchelCapacity.forLevel(state(satchel).capacityLevel());
   }

   public static List<ItemStack> contents(ItemStack satchel) {
      requireSatchel(satchel);
      migrateStoredContents(satchel);
      return storedContents(satchel).items().stream().<ItemStack>map(ItemStack::copy).toList();
   }

   public static int size(ItemStack satchel) {
      requireSatchel(satchel);
      migrateStoredContents(satchel);
      return storedContents(satchel).size();
   }

   public static AnglersSatchelStorage.InsertionResult insert(ItemStack satchel, ItemStack offered) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return AnglersSatchelStorage.InsertionResult.failed(AnglersSatchelStorage.InsertionStatus.INVALID_SATCHEL, offered);
      }

      if (offered == null || offered.isEmpty()) {
         return AnglersSatchelStorage.InsertionResult.failed(AnglersSatchelStorage.InsertionStatus.EMPTY_INPUT, ItemStack.EMPTY);
      }

      if (offered.getItem().canBeNested() && FishSatchelItem.canPutInSatchel(offered)) {
         List<ItemStack> current = contents(satchel);
         List<ItemStack> incoming = new ArrayList<>(offered.getCount());

         for (int index = 0; index < offered.getCount(); index++) {
            ItemStack incomingStack = offered.copyWithCount(1);
            LegacyPersistenceMigration.migrateStack(incomingStack);
            incoming.add(incomingStack);
         }

         SatchelInsertionPlan<ItemStack> plan = SatchelInsertionPlan.create(current, incoming, capacity(satchel));
         if (plan.insertedCount() == 0) {
            return AnglersSatchelStorage.InsertionResult.failed(AnglersSatchelStorage.InsertionStatus.FULL, offered);
         }

         try {
            writeContents(satchel, plan.contents());
         } catch (RuntimeException exception) {
            return AnglersSatchelStorage.InsertionResult.failed(AnglersSatchelStorage.InsertionStatus.COMMIT_FAILED, offered);
         }

         int remainderCount = offered.getCount() - plan.insertedCount();
         ItemStack remainder = remainderCount == 0 ? ItemStack.EMPTY : offered.copyWithCount(remainderCount);
         return new AnglersSatchelStorage.InsertionResult(AnglersSatchelStorage.InsertionStatus.SUCCESS, plan.insertedCount(), remainder);
      } else {
         return AnglersSatchelStorage.InsertionResult.failed(AnglersSatchelStorage.InsertionStatus.INELIGIBLE_ITEM, offered);
      }
   }

   public static AnglersSatchelStorage.InsertionResult moveInto(ItemStack satchel, ItemStack source) {
      AnglersSatchelStorage.InsertionResult result = insert(satchel, source);
      if (result.insertedCount() > 0) {
         source.decrement(result.insertedCount());
      }

      return result;
   }

   public static boolean setProtected(ItemStack satchel, int slot, boolean protect) {
      if (SatchelRegistration.isAnglersSatchel(satchel) && slot >= 0 && slot < size(satchel)) {
         SatchelState beforeState = state(satchel);
         if (!beforeState.isFeatureUnlocked(SatchelFeature.TROPHY_LOCK)) {
            return false;
         }

         List<ItemStack> updated = new ArrayList<>(contents(satchel));
         ItemStack specimen = updated.get(slot).copy();
         if (protect) {
            specimen.set(TideTraitsComponents.PROTECTED, true);
         } else {
            specimen.remove(TideTraitsComponents.PROTECTED);
         }

         updated.set(slot, specimen);
         return commitContentsAndState(satchel, updated, beforeState, beforeState.withSlotProtected(slot, protect));
      } else {
         return false;
      }
   }

   public static boolean isProtected(ItemStack satchel, int slot) {
      return SatchelRegistration.isAnglersSatchel(satchel) && slot >= 0 && slot < size(satchel)
         ? state(satchel).isSlotProtected(slot)
            || Boolean.TRUE.equals(((ItemStack)storedContents(satchel).items().get(slot)).get(TideTraitsComponents.PROTECTED))
         : false;
   }

   public static AnglersSatchelStorage.ExtractionResult extractAt(ItemStack satchel, int slot, boolean allowProtected) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return AnglersSatchelStorage.ExtractionResult.failed(AnglersSatchelStorage.ExtractionStatus.INVALID_SATCHEL);
      }

      List<ItemStack> current = contents(satchel);
      if (slot >= 0 && slot < current.size()) {
         SatchelState beforeState = state(satchel);
         if ((beforeState.isSlotProtected(slot) || Boolean.TRUE.equals(current.get(slot).get(TideTraitsComponents.PROTECTED))) && !allowProtected) {
            return AnglersSatchelStorage.ExtractionResult.failed(AnglersSatchelStorage.ExtractionStatus.PROTECTED);
         }

         ItemStack extracted = current.get(slot).copy();
         List<ItemStack> updated = new ArrayList<>(current);
         updated.remove(slot);
         TreeSet<Integer> shifted = new TreeSet<>();

         for (int protectedSlot : beforeState.protectedSlots()) {
            if (protectedSlot < slot) {
               shifted.add(protectedSlot);
            } else if (protectedSlot > slot) {
               shifted.add(protectedSlot - 1);
            }
         }

         SatchelState afterState = beforeState.withProtectedSlots(shifted);
         return !commitContentsAndState(satchel, updated, beforeState, afterState)
            ? AnglersSatchelStorage.ExtractionResult.failed(AnglersSatchelStorage.ExtractionStatus.COMMIT_FAILED)
            : new AnglersSatchelStorage.ExtractionResult(AnglersSatchelStorage.ExtractionStatus.SUCCESS, extracted);
      } else {
         return AnglersSatchelStorage.ExtractionResult.failed(AnglersSatchelStorage.ExtractionStatus.INVALID_SLOT);
      }
   }

   public static AnglersSatchelStorage.ExtractionResult extractFirstUnprotected(ItemStack satchel) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return AnglersSatchelStorage.ExtractionResult.failed(AnglersSatchelStorage.ExtractionStatus.INVALID_SATCHEL);
      }

      int count = size(satchel);

      for (int slot = 0; slot < count; slot++) {
         if (!isProtected(satchel, slot)) {
            return extractAt(satchel, slot, false);
         }
      }

      return AnglersSatchelStorage.ExtractionResult.failed(
         count == 0 ? AnglersSatchelStorage.ExtractionStatus.EMPTY : AnglersSatchelStorage.ExtractionStatus.PROTECTED
      );
   }

   public static AnglersSatchelStorage.SortStatus sort(
      ItemStack satchel, SatchelSortConfiguration configuration, Function<ItemStack, SatchelSortDescriptor> descriptorResolver
   ) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         return AnglersSatchelStorage.SortStatus.INVALID_SATCHEL;
      }

      SatchelState beforeState = state(satchel);
      if (!beforeState.isFeatureUnlocked(SatchelFeature.TACKLE_ORGANIZER)) {
         return AnglersSatchelStorage.SortStatus.ORGANIZER_LOCKED;
      }

      List<ItemStack> current = contents(satchel);
      List<SatchelSorter.IndexedValue<ItemStack>> sorted = SatchelSorter.sortedEntries(current, configuration, descriptorResolver);
      List<ItemStack> updated = sorted.stream().map(SatchelSorter.IndexedValue::value).toList();
      Set<Integer> oldProtected = beforeState.protectedSlots();
      TreeSet<Integer> remappedProtected = new TreeSet<>();

      for (int newSlot = 0; newSlot < sorted.size(); newSlot++) {
         if (oldProtected.contains(sorted.get(newSlot).originalIndex())) {
            remappedProtected.add(newSlot);
         }
      }

      SatchelState afterState = beforeState.withSortConfiguration(configuration).withProtectedSlots(remappedProtected);
      return commitContentsAndState(satchel, updated, beforeState, afterState)
         ? AnglersSatchelStorage.SortStatus.SUCCESS
         : AnglersSatchelStorage.SortStatus.COMMIT_FAILED;
   }

   private static void migrateStoredContents(ItemStack satchel) {
      SatchelContents stored = storedContents(satchel);
      List<ItemStack> migrated = new ArrayList<>(stored.size());
      boolean changed = false;
      for (ItemStack original : stored.items()) {
         ItemStack copy = original.copy();
         CanonicalSpecimenStorage.MigrationState before = CanonicalSpecimenStorage.detectMigration(copy);
         LegacyPersistenceMigration.migrateStack(copy);
         CanonicalSpecimenStorage.MigrationState after = CanonicalSpecimenStorage.detectMigration(copy);
         changed |= before != CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT
            && after == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT;
         migrated.add(copy);
      }
      if (changed) {
         try {
            writeContents(satchel, migrated);
         } catch (RuntimeException ignored) {
            // Keep the original SatchelContents intact if a one-time migration commit cannot be written.
         }
      }
   }

   private static boolean commitContentsAndState(ItemStack satchel, List<ItemStack> contents, SatchelState beforeState, SatchelState afterState) {
      SatchelContents previousContents = storedContents(satchel);

      try {
         writeContents(satchel, contents);
         setState(satchel, afterState);
         return true;
      } catch (RuntimeException exception) {
         try {
            satchel.set(TideDataComponents.SATCHEL_CONTENTS, previousContents);
            setState(satchel, beforeState);
         } catch (RuntimeException var7) {
         }

         return false;
      }
   }

   private static SatchelContents storedContents(ItemStack satchel) {
      return (SatchelContents)satchel.getOrDefault(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents());
   }

   private static void writeContents(ItemStack satchel, List<ItemStack> contents) {
      List<ItemStack> copies = contents.stream().<ItemStack>map(ItemStack::copy).toList();
      satchel.set(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents(copies));
   }

   private static void requireSatchel(ItemStack satchel) {
      if (!SatchelRegistration.isAnglersSatchel(satchel)) {
         throw new IllegalArgumentException("ItemStack is not an Angler's Satchel");
      }
   }

   public record ExtractionResult(AnglersSatchelStorage.ExtractionStatus status, ItemStack extracted) {
      public ExtractionResult {
         extracted = extracted == null ? ItemStack.EMPTY : extracted.copy();
      }

      private static AnglersSatchelStorage.ExtractionResult failed(AnglersSatchelStorage.ExtractionStatus status) {
         return new AnglersSatchelStorage.ExtractionResult(status, ItemStack.EMPTY);
      }

      public Optional<ItemStack> item() {
         return this.extracted.isEmpty() ? Optional.empty() : Optional.of(this.extracted.copy());
      }
   }

   public enum ExtractionStatus {
      SUCCESS,
      INVALID_SATCHEL,
      INVALID_SLOT,
      EMPTY,
      PROTECTED,
      COMMIT_FAILED;
   }

   public record InsertionResult(AnglersSatchelStorage.InsertionStatus status, int insertedCount, ItemStack remainder) {
      public InsertionResult {
         remainder = remainder == null ? ItemStack.EMPTY : remainder.copy();
      }

      private static AnglersSatchelStorage.InsertionResult failed(AnglersSatchelStorage.InsertionStatus status, ItemStack remainder) {
         return new AnglersSatchelStorage.InsertionResult(status, 0, remainder);
      }

      public boolean insertedAny() {
         return this.insertedCount > 0;
      }

      public boolean fullyInserted() {
         return this.status == AnglersSatchelStorage.InsertionStatus.SUCCESS && this.remainder.isEmpty();
      }
   }

   public enum InsertionStatus {
      SUCCESS,
      INVALID_SATCHEL,
      EMPTY_INPUT,
      INELIGIBLE_ITEM,
      FULL,
      COMMIT_FAILED;
   }

   public enum SortStatus {
      SUCCESS,
      INVALID_SATCHEL,
      ORGANIZER_LOCKED,
      COMMIT_FAILED;
   }
}
