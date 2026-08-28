/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.entity;

import com.li64.tide.data.FishLengthHolder;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tidetraits.TideTraits;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.RegistryWrapper.class_7874;

public final class SpecimenTransfer {
   public static final String ENTITY_KEY = "TideTraits";
   public static final String VERSION_KEY = "Version";
   public static final String MUTATION_KEY = "Mutation";
   public static final String SEED_KEY = "MutationSeed";
   public static final String PERCENTILE_KEY = "SizePercentile";
   public static final String LENGTH_KEY = "LengthCm";
   public static final String SOURCE_STACK_KEY = "SourceStack";
   public static final String DISPLAY_PREVIEW_KEY = "DisplayPreview";
   public static final int DATA_VERSION = 3;
   private static final AtomicBoolean SNAPSHOT_WARNING_EMITTED = new AtomicBoolean();

   private SpecimenTransfer() {
   }

   public static boolean hasSpecimen(ItemStack stack) {
      String mutation = (String)stack.get(TideTraitsComponents.MUTATION);
      return mutation != null && !mutation.isBlank();
   }

   public static NbtCompound fromStack(ItemStack stack) {
      TraitAxesRuntime.migrateLegacy(stack);
      NbtCompound tag = new NbtCompound();
      String mutation = (String)stack.get(TideTraitsComponents.MUTATION);
      if (mutation != null && !mutation.isBlank()) {
         tag.putInt("Version", 3);
         tag.putString("Mutation", mutation);
         tag.putString("BodyType", TraitAxesRuntime.bodyType(stack));
         Long seed = (Long)stack.get(TideTraitsComponents.MUTATION_SEED);
         if (seed != null) {
            tag.putLong("MutationSeed", seed);
         }

         Double percentile = (Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE);
         if (percentile != null && Double.isFinite(percentile)) {
            tag.putDouble("SizePercentile", clampPercentile(percentile));
         }

         if (TideItemData.FISH_LENGTH.isPresent(stack)) {
            double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
            if (Double.isFinite(length) && length > 0.0) {
               tag.putDouble("LengthCm", length);
            }
         }

         return tag;
      } else {
         return tag;
      }
   }

   public static NbtCompound fromStack(ItemStack stack, class_7874 registries) {
      NbtCompound tag = fromStack(stack);
      if (!tag.isEmpty() && registries != null) {
         try {
            if (stack.copyWithCount(1).encode(registries) instanceof NbtCompound savedStack && !savedStack.isEmpty()) {
               tag.put("SourceStack", savedStack.copy());
            }
         } catch (RuntimeException exception) {
            warnSnapshotFailure("encode", exception);
         }

         return tag;
      } else {
         return tag;
      }
   }

   public static void toStack(NbtCompound source, ItemStack stack) {
      if (source != null && source.contains("Mutation")) {
         String mutation = source.getString("Mutation");
         if (!mutation.isBlank()) {
            stack.set(TideTraitsComponents.MUTATION, mutation);
            if (source.contains("BodyType")) {
               stack.set(TideTraitsComponents.BODY_TYPE, source.getString("BodyType"));
            } else {
               stack.set(TideTraitsComponents.BODY_TYPE, "normal");
            }

            if (source.contains("MutationSeed")) {
               stack.set(TideTraitsComponents.MUTATION_SEED, source.getLong("MutationSeed"));
            }

            if (source.contains("SizePercentile")) {
               double percentile = source.getDouble("SizePercentile");
               if (Double.isFinite(percentile)) {
                  stack.set(TideTraitsComponents.SIZE_PERCENTILE, clampPercentile(percentile));
               }
            }

            if (source.contains("LengthCm")) {
               double length = source.getDouble("LengthCm");
               if (Double.isFinite(length) && length > 0.0) {
                  TideItemData.FISH_LENGTH.set(stack, length);
               }
            }
         }
      }
   }

   public static void toStack(NbtCompound source, ItemStack stack, class_7874 registries) {
      if (source != null && stack != null && !stack.isEmpty()) {
         if (registries != null && source.contains("SourceStack", 10)) {
            try {
               ItemStack savedStack = ItemStack.fromNbtOrEmpty(registries, source.getCompound("SourceStack"));
               if (!savedStack.isEmpty()) {
                  stack.applyComponentsFrom(savedStack.getComponents());
               }
            } catch (RuntimeException exception) {
               warnSnapshotFailure("decode", exception);
            }
         }

         toStack(source, stack);
      }
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
         if (entity instanceof FishLengthHolder holder) {
            double length = holder.tide$getLength();
            if (Double.isFinite(length) && length > 0.0) {
               tag.putDouble("LengthCm", length);
            }
         }

         toStack(tag, stack, entity.getRegistryManager());
      }
   }

   public static void entityToBucket(Entity entity, ItemStack bucket) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound specimen = specimenEntity.tideTraits$getSpecimenTag();
         if (!specimen.isEmpty()) {
            if (entity instanceof FishLengthHolder holder) {
               double length = holder.tide$getLength();
               if (Double.isFinite(length) && length > 0.0) {
                  specimen.putDouble("LengthCm", length);
               }
            }

            NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, bucket, bucketTag -> bucketTag.put("TideTraits", specimen.copy()));
         }
      }
   }

   public static void bucketTagToEntity(NbtCompound bucketTag, Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity && bucketTag != null && bucketTag.contains("TideTraits")) {
         NbtCompound specimen = bucketTag.getCompound("TideTraits").copy();
         if (!specimen.isEmpty()) {
            specimenEntity.tideTraits$setSpecimenTag(specimen);
            applyLengthToEntity(specimen, entity);
         }
      }
   }

   public static void stackToBucket(ItemStack fish, ItemStack bucket, class_7874 registries) {
      NbtCompound specimen = fromStack(fish, registries);
      if (!specimen.isEmpty()) {
         NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, bucket, bucketTag -> bucketTag.put("TideTraits", specimen.copy()));
      }
   }

   public static void markDisplayPreview(Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound tag = specimenEntity.tideTraits$getSpecimenTag();
         if (!tag.isEmpty()) {
            tag.putBoolean("DisplayPreview", true);
            specimenEntity.tideTraits$setSpecimenTag(tag);
         }
      }
   }

   public static boolean isDisplayPreview(Entity entity) {
      return entity instanceof SpecimenEntity specimenEntity && specimenEntity.tideTraits$getSpecimenTag().getBoolean("DisplayPreview");
   }

   private static void applyLengthToEntity(NbtCompound tag, Entity entity) {
      if (entity instanceof FishLengthHolder holder && tag.contains("LengthCm")) {
         double length = tag.getDouble("LengthCm");
         if (Double.isFinite(length) && length > 0.0) {
            holder.tide$setLength(length);
         }
      }
   }

   private static double clampPercentile(double percentile) {
      return Math.max(0.0, Math.min(100.0, percentile));
   }

   private static void warnSnapshotFailure(String operation, RuntimeException exception) {
      if (SNAPSHOT_WARNING_EMITTED.compareAndSet(false, true)) {
         TideTraits.LOGGER.warn("Could not {} a specimen's generic component snapshot; core mutation and size data remain intact", operation, exception);
      } else {
         TideTraits.LOGGER.debug("Specimen component snapshot {} failed", operation, exception);
      }
   }
}
