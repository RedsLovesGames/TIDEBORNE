/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.entity;

import com.li64.tide.data.FishLengthHolder;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.TideTraits;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public final class SpecimenTransfer {
   public static final String ENTITY_KEY = "TideTraits";
   public static final String VERSION_KEY = "Version";
   public static final String MUTATION_KEY = "Mutation";
   public static final String SEED_KEY = "MutationSeed";
   public static final String PERCENTILE_KEY = "SizePercentile";
   public static final String LENGTH_KEY = "LengthCm";
   public static final String SOURCE_STACK_KEY = "SourceStack";
   public static final String DISPLAY_PREVIEW_KEY = "DisplayPreview";
   public static final String BODY_TYPE_KEY = "BodyType";
   public static final String CANONICAL_BODY_TYPE_KEY = "CanonicalBodyType";
   public static final String CANONICAL_CONDITION_KEY = "CanonicalCondition";
   public static final String CANONICAL_PIGMENTATION_KEY = "CanonicalPigmentation";
   public static final String CANONICAL_RAW_FISH_SCORE_KEY = "CanonicalRawFishScore";
   public static final String CANONICAL_FISH_SCORE_KEY = "CanonicalFishScore";
   public static final int DATA_VERSION = 6;
   private static final AtomicBoolean SNAPSHOT_WARNING_EMITTED = new AtomicBoolean();

   private SpecimenTransfer() {
   }

   public static boolean hasSpecimen(ItemStack stack) {
      CanonicalSpecimenStorage.MigrationState state = CanonicalSpecimenStorage.detectMigration(stack);
      if (state != CanonicalSpecimenStorage.MigrationState.NONE
         && state != CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY) {
         return true;
      }
      String mutation = stack == null ? null : stack.get(TideTraitsComponents.MUTATION);
      return mutation != null && !mutation.isBlank();
   }

   public static NbtCompound fromStack(ItemStack stack) {
      NbtCompound tag = new NbtCompound();
      CanonicalSpecimenStorage.MigrationState state = CanonicalSpecimenStorage.detectMigration(stack);
      if (state == CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
         Optional<SpecimenData> canonical = CanonicalSpecimenStorage.read(stack);
         if (canonical.isEmpty()) {
            return tag;
         }
         SpecimenData specimen = canonical.get();
         tag.putInt(VERSION_KEY, DATA_VERSION);
         CanonicalSpecimenStorage.writeTransferData(tag, specimen);
         writeCompatibilityMirrors(tag, specimen);
         return tag;
      }
      if (state != CanonicalSpecimenStorage.MigrationState.LEGACY_ONLY) {
         return tag;
      }

      String mutation = stack.get(TideTraitsComponents.MUTATION);
      if (mutation == null || mutation.isBlank()) {
         return tag;
      }
      tag.putInt(VERSION_KEY, DATA_VERSION);
      tag.putString(MUTATION_KEY, mutation);
      String bodyType = stack.get(TideTraitsComponents.BODY_TYPE);
      tag.putString(BODY_TYPE_KEY, bodyType == null || bodyType.isBlank() ? "normal" : bodyType);
      Long seed = stack.get(TideTraitsComponents.MUTATION_SEED);
      if (seed != null) {
         tag.putLong(SEED_KEY, seed);
      }
      Double percentile = stack.get(TideTraitsComponents.SIZE_PERCENTILE);
      if (percentile != null && Double.isFinite(percentile)) {
         tag.putDouble(PERCENTILE_KEY, clampPercentile(percentile));
      }
      if (TideItemData.FISH_LENGTH.isPresent(stack)) {
         double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
         if (Double.isFinite(length) && length > 0.0) {
            tag.putDouble(LENGTH_KEY, length);
         }
      }
      return tag;
   }

   public static NbtCompound fromStack(ItemStack stack, WrapperLookup registries) {
      NbtCompound tag = fromStack(stack);
      if (!tag.isEmpty() && registries != null) {
         try {
            if (stack.copyWithCount(1).encode(registries) instanceof NbtCompound savedStack && !savedStack.isEmpty()) {
               tag.put(SOURCE_STACK_KEY, savedStack.copy());
            }
         } catch (RuntimeException exception) {
            warnSnapshotFailure("encode", exception);
         }
      }
      return tag;
   }

   public static void toStack(NbtCompound source, ItemStack stack) {
      if (source == null || stack == null || stack.isEmpty()) {
         return;
      }
      if (CanonicalSpecimenStorage.hasTransferPayload(source)) {
         CanonicalSpecimenStorage.restoreTransferData(source, stack);
         return;
      }
      restoreLegacyTransfer(source, stack);
   }

   public static void toStack(NbtCompound source, ItemStack stack, WrapperLookup registries) {
      if (source == null || stack == null || stack.isEmpty()) {
         return;
      }
      if (registries != null && source.contains(SOURCE_STACK_KEY, 10)) {
         try {
            ItemStack savedStack = ItemStack.fromNbtOrEmpty(registries, source.getCompound(SOURCE_STACK_KEY));
            if (!savedStack.isEmpty()) {
               stack.applyComponentsFrom(savedStack.getComponents());
            }
         } catch (RuntimeException exception) {
            warnSnapshotFailure("decode", exception);
         }
      }
      toStack(source, stack);
   }

   public static void stackToEntity(ItemStack stack, Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound tag = fromStack(stack, entity.getRegistryManager());
         if (!tag.isEmpty()) {
            specimenEntity.tideTraits$setSpecimenTag(tag);
            applyLengthToEntity(tag, entity);
         }
      }
   }

   public static void entityToStack(Entity entity, ItemStack stack) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound tag = specimenEntity.tideTraits$getSpecimenTag();
         syncLegacyLengthFromEntity(tag, entity);
         toStack(tag, stack, entity.getRegistryManager());
      }
   }

   public static void entityToBucket(Entity entity, ItemStack bucket) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound specimen = specimenEntity.tideTraits$getSpecimenTag();
         if (!specimen.isEmpty()) {
            syncLegacyLengthFromEntity(specimen, entity);
            NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, bucket, bucketTag -> bucketTag.put(ENTITY_KEY, specimen.copy()));
         }
      }
   }

   public static void bucketTagToEntity(NbtCompound bucketTag, Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity && bucketTag != null && bucketTag.contains(ENTITY_KEY)) {
         NbtCompound specimen = bucketTag.getCompound(ENTITY_KEY).copy();
         if (!specimen.isEmpty()) {
            specimenEntity.tideTraits$setSpecimenTag(specimen);
            applyLengthToEntity(specimen, entity);
         }
      }
   }

   public static void stackToBucket(ItemStack fish, ItemStack bucket, WrapperLookup registries) {
      NbtCompound specimen = fromStack(fish, registries);
      if (!specimen.isEmpty()) {
         NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, bucket, bucketTag -> bucketTag.put(ENTITY_KEY, specimen.copy()));
      }
   }

   public static void markDisplayPreview(Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound tag = specimenEntity.tideTraits$getSpecimenTag();
         if (!tag.isEmpty()) {
            tag.putBoolean(DISPLAY_PREVIEW_KEY, true);
            specimenEntity.tideTraits$setSpecimenTag(tag);
         }
      }
   }

   public static boolean isDisplayPreview(Entity entity) {
      return entity instanceof SpecimenEntity specimenEntity && specimenEntity.tideTraits$getSpecimenTag().getBoolean(DISPLAY_PREVIEW_KEY);
   }

   private static void writeCompatibilityMirrors(NbtCompound tag, SpecimenData specimen) {
      String bodyType = specimen.bodyType().name().toLowerCase(java.util.Locale.ROOT);
      String condition = specimen.condition().name().toLowerCase(java.util.Locale.ROOT);
      tag.putString(MUTATION_KEY, condition);
      tag.putString(BODY_TYPE_KEY, bodyType);
      tag.putString(CANONICAL_BODY_TYPE_KEY, bodyType);
      tag.putString(CANONICAL_CONDITION_KEY, condition);
      tag.putString(CANONICAL_PIGMENTATION_KEY, specimen.pigmentation().name().toLowerCase(java.util.Locale.ROOT));
      specimen.rawFishScore().ifPresent(value -> tag.putDouble(CANONICAL_RAW_FISH_SCORE_KEY, value));
      specimen.fishScore().ifPresent(value -> tag.putInt(CANONICAL_FISH_SCORE_KEY, value));
      tag.putLong(SEED_KEY, specimen.deterministicSeed());
      tag.putDouble(PERCENTILE_KEY, specimen.finalPercentile());
      tag.putDouble(LENGTH_KEY, specimen.finalLength());
   }

   private static void restoreLegacyTransfer(NbtCompound source, ItemStack stack) {
      if (source.contains(MUTATION_KEY)) {
         String mutation = source.getString(MUTATION_KEY);
         if (!mutation.isBlank()) {
            stack.set(TideTraitsComponents.MUTATION, mutation);
         }
      }
      if (source.contains(BODY_TYPE_KEY)) {
         String bodyType = source.getString(BODY_TYPE_KEY);
         stack.set(TideTraitsComponents.BODY_TYPE, bodyType.isBlank() ? "normal" : bodyType);
      }
      if (source.contains(SEED_KEY)) {
         stack.set(TideTraitsComponents.MUTATION_SEED, source.getLong(SEED_KEY));
      }
      if (source.contains(PERCENTILE_KEY)) {
         double percentile = source.getDouble(PERCENTILE_KEY);
         if (Double.isFinite(percentile)) {
            stack.set(TideTraitsComponents.SIZE_PERCENTILE, clampPercentile(percentile));
         }
      }
      if (source.contains(LENGTH_KEY)) {
         double length = source.getDouble(LENGTH_KEY);
         if (Double.isFinite(length) && length > 0.0) {
            TideItemData.FISH_LENGTH.set(stack, length);
         }
      }
   }

   private static void applyLengthToEntity(NbtCompound tag, Entity entity) {
      if (entity instanceof FishLengthHolder holder) {
         Optional<SpecimenData> canonical = CanonicalSpecimenStorage.readTransferData(tag);
         double length = canonical.map(SpecimenData::finalLength)
               .orElseGet(() -> tag.contains(LENGTH_KEY) ? tag.getDouble(LENGTH_KEY) : 0.0);
         if (Double.isFinite(length) && length > 0.0) {
            holder.tide$setLength(length);
         }
      }
   }

   private static void syncLegacyLengthFromEntity(NbtCompound tag, Entity entity) {
      if (CanonicalSpecimenStorage.hasTransferPayload(tag)) {
         return;
      }
      if (entity instanceof FishLengthHolder holder) {
         double length = holder.tide$getLength();
         if (Double.isFinite(length) && length > 0.0) {
            tag.putDouble(LENGTH_KEY, length);
         }
      }
   }

   private static double clampPercentile(double percentile) {
      return Math.max(0.0, Math.min(100.0, percentile));
   }

   private static void warnSnapshotFailure(String operation, RuntimeException exception) {
      if (SNAPSHOT_WARNING_EMITTED.compareAndSet(false, true)) {
         TideTraits.LOGGER.warn("Could not {} a specimen's generic component snapshot; core mutation, size, and FishScore data remain intact", operation, exception);
      } else {
         TideTraits.LOGGER.debug("Specimen component snapshot {} failed", operation, exception);
      }
   }
}
