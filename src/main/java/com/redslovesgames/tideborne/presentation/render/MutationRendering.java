/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.render;

import com.li64.tide.data.FishLengthHolder;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.TideTraits;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenEntity;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenTransfer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public final class MutationRendering {
   private static final MutationTextureCache TEXTURES = new MutationTextureCache(TideTraitsConfigManager.current().rendering().dynamicTextureCacheMaximum());
   private static final Identifier RELOAD_LISTENER_ID = Identifier.of("tide_traits", "mutation_textures");
   private static final long VARIANT_SALT = -3335678366873096957L;
   private static boolean initialized;

   private MutationRendering() {
   }

   public static synchronized void initClient() {
      if (!initialized) {
         ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            public Identifier getFabricId() {
               return MutationRendering.RELOAD_LISTENER_ID;
            }

            public void reload(ResourceManager resourceManager) {
               MutationRendering.clearTextureCache();
            }
         });
         ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> clearTextureCache());
         initialized = true;
      }
   }

   public static Identifier textureFor(Identifier original, Entity entity) {
      if (original != null && entity != null && !isGenerated(original)) {
         initClient();
         return visual(entity).map(visual -> TEXTURES.resolve(original, visual)).orElse(original);
      } else {
         return original;
      }
   }

   /**
    * Applies the same deterministic mutation texture transform used by fish entities to a flat item
    * texture. The ItemStack is only read here: canonical pigmentation/condition state is never
    * generated or mutated by rendering.
    */
   public static Identifier textureForItem(Identifier original, ItemStack stack) {
      if (original != null && stack != null && !stack.isEmpty() && !isGenerated(original)) {
         initClient();
         return visual(stack).map(visual -> TEXTURES.resolve(original, visual)).orElse(original);
      } else {
         return original;
      }
   }

   public static Identifier textureForForeignFish(Identifier original, LivingEntity entity) {
      if (original != null && entity != null) {
         try {
            return FishData.get(entity).isPresent() ? textureFor(original, entity) : original;
         } catch (RuntimeException exception) {
            MutationRendering.RenderFailureLog.warn("foreign-fish-data", "Could not resolve foreign FishData; using its original texture", exception);
            return original;
         }
      } else {
         return original;
      }
   }

   public static void applyLengthScale(LivingEntity entity, MatrixStack poseStack) {
      if (entity != null && poseStack != null && entity instanceof SpecimenEntity specimenEntity) {
         try {
            NbtCompound specimen = specimenEntity.tideTraits$getSpecimenTag();
            Optional<SpecimenData> canonical = CanonicalSpecimenStorage.readTransferData(specimen);
            boolean hasCanonicalState = canonical.isPresent()
               || !specimen.getString(SpecimenTransfer.CANONICAL_PIGMENTATION_KEY).isBlank()
               || !specimen.getString(SpecimenTransfer.CANONICAL_CONDITION_KEY).isBlank();
            if ((!hasCanonicalState && specimen.getString(SpecimenTransfer.MUTATION_KEY).isBlank()) || SpecimenTransfer.isDisplayPreview(entity)) {
               return;
            }

            Optional<FishData> fishData = FishData.get(entity);
            if (fishData.isEmpty()) {
               return;
            }

            double averageLength = fishData.get().getAverageLength();
            double trackedLength = canonical.isPresent() ? canonical.get().finalLength() : trackedLength(entity, specimen);
            if (!Double.isFinite(averageLength) || averageLength <= 0.0 || !Double.isFinite(trackedLength) || trackedLength <= 0.0) {
               return;
            }

            float scale = (float)Math.max(0.25, Math.min(4.0, Math.sqrt(trackedLength / averageLength)));
            if (Math.abs(scale - 1.0F) > 1.0E-4F) {
               poseStack.scale(scale, scale, scale);
            }
         } catch (RuntimeException exception) {
            MutationRendering.RenderFailureLog.warn("length-scale", "Could not apply specimen length scale", exception);
         }
      }
   }

   public static void clearTextureCache() {
      TEXTURES.clear();
      MutationRendering.RenderFailureLog.reset();
   }

   private static Optional<MutationTextureCache.Visual> visual(Entity entity) {
      if (entity instanceof SpecimenEntity specimenEntity) {
         NbtCompound specimen = specimenEntity.tideTraits$getSpecimenTag();
         Optional<SpecimenData> canonical = CanonicalSpecimenStorage.readTransferData(specimen);
         Optional<MutationTextureCache.VisualEffect> effect = canonical.isPresent()
            ? visualEffect(canonical.get())
            : visualEffect(specimen);
         if (effect.isPresent()) {
            long seed = canonical.isPresent()
               ? canonical.get().deterministicSeed()
               : specimen.contains(SpecimenTransfer.SEED_KEY, 99)
                  ? specimen.getLong(SpecimenTransfer.SEED_KEY)
                  : fallbackSeed(entity.getUuid());
            return visual(effect.get(), seed);
         }
      }
      return Optional.empty();
   }

   private static Optional<MutationTextureCache.Visual> visual(ItemStack stack) {
      Optional<MutationTextureCache.VisualEffect> effect = visualEffect(stack);
      if (effect.isEmpty()) {
         return Optional.empty();
      }

      Long canonicalSeed = stack.get(TideTraitsComponents.SPECIMEN_DETERMINISTIC_SEED);
      Long legacySeed = stack.get(TideTraitsComponents.MUTATION_SEED);
      long seed = canonicalSeed != null ? canonicalSeed : (legacySeed != null ? legacySeed : 0L);
      return visual(effect.get(), seed);
   }

   private static Optional<MutationTextureCache.Visual> visual(MutationTextureCache.VisualEffect effect, long seed) {
      int variants = switch (effect) {
         case SCARRED, PARASITE_RIDDEN -> 4;
         case IRIDESCENT -> 8;
         default -> 1;
      };
      long mixed = mix64(seed ^ VARIANT_SALT ^ effect.historicalOrdinal());
      int variant = Math.floorMod(mixed, variants);
      int offsetX = Math.floorMod(mix64(mixed ^ 7640891576956012809L), 7) - 3;
      int offsetY = Math.floorMod(mix64(mixed ^ -4942790177534073029L), 7) - 3;
      return Optional.of(new MutationTextureCache.Visual(effect, variant, offsetX, offsetY));
   }

   private static Optional<MutationTextureCache.VisualEffect> visualEffect(SpecimenData specimen) {
      Optional<MutationTextureCache.VisualEffect> pigmentation = visualEffectBySerializedName(specimen.pigmentation().name());
      if (pigmentation.filter(effect -> effect == MutationTextureCache.VisualEffect.ALBINO || effect == MutationTextureCache.VisualEffect.IRIDESCENT).isPresent()) {
         return pigmentation;
      }

      Optional<MutationTextureCache.VisualEffect> condition = visualEffectBySerializedName(specimen.condition().name());
      if (condition.filter(effect -> effect == MutationTextureCache.VisualEffect.SCARRED || effect == MutationTextureCache.VisualEffect.PARASITE_RIDDEN).isPresent()) {
         return condition;
      }

      return Optional.empty();
   }

   private static Optional<MutationTextureCache.VisualEffect> visualEffect(NbtCompound specimen) {
      Optional<MutationTextureCache.VisualEffect> pigmentation = visualEffectBySerializedName(
         specimen.getString(SpecimenTransfer.CANONICAL_PIGMENTATION_KEY)
      );
      if (pigmentation.filter(effect -> effect == MutationTextureCache.VisualEffect.ALBINO || effect == MutationTextureCache.VisualEffect.IRIDESCENT).isPresent()) {
         return pigmentation;
      }

      Optional<MutationTextureCache.VisualEffect> condition = visualEffectBySerializedName(
         specimen.getString(SpecimenTransfer.CANONICAL_CONDITION_KEY)
      );
      if (condition.filter(effect -> effect == MutationTextureCache.VisualEffect.SCARRED || effect == MutationTextureCache.VisualEffect.PARASITE_RIDDEN).isPresent()) {
         return condition;
      }

      return visualEffectBySerializedName(specimen.getString(SpecimenTransfer.MUTATION_KEY));
   }

   private static Optional<MutationTextureCache.VisualEffect> visualEffect(ItemStack stack) {
      Optional<MutationTextureCache.VisualEffect> pigmentation = visualEffectBySerializedName(stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION));
      if (pigmentation.filter(effect -> effect == MutationTextureCache.VisualEffect.ALBINO || effect == MutationTextureCache.VisualEffect.IRIDESCENT).isPresent()) {
         return pigmentation;
      }

      Optional<MutationTextureCache.VisualEffect> condition = visualEffectBySerializedName(stack.get(TideTraitsComponents.SPECIMEN_CONDITION));
      if (condition.filter(effect -> effect == MutationTextureCache.VisualEffect.SCARRED || effect == MutationTextureCache.VisualEffect.PARASITE_RIDDEN).isPresent()) {
         return condition;
      }

      return visualEffectBySerializedName(stack.get(TideTraitsComponents.MUTATION));
   }

   private static Optional<MutationTextureCache.VisualEffect> visualEffectBySerializedName(String value) {
      if (value == null) {
         return Optional.empty();
      }

      return switch (value.toLowerCase(Locale.ROOT)) {
         case "albino" -> Optional.of(MutationTextureCache.VisualEffect.ALBINO);
         case "perfect_specimen" -> Optional.of(MutationTextureCache.VisualEffect.PERFECT_SPECIMEN);
         case "scarred" -> Optional.of(MutationTextureCache.VisualEffect.SCARRED);
         case "parasite_ridden" -> Optional.of(MutationTextureCache.VisualEffect.PARASITE_RIDDEN);
         case "iridescent" -> Optional.of(MutationTextureCache.VisualEffect.IRIDESCENT);
         default -> Optional.empty();
      };
   }

   private static double trackedLength(Entity entity, NbtCompound specimen) {
      if (specimen.contains(SpecimenTransfer.LENGTH_KEY, 99)) {
         double synchronizedLength = specimen.getDouble(SpecimenTransfer.LENGTH_KEY);
         if (Double.isFinite(synchronizedLength) && synchronizedLength > 0.0) {
            return synchronizedLength;
         }
      }

      if (entity instanceof FishLengthHolder holder) {
         double length = holder.tide$getLength();
         if (Double.isFinite(length) && length > 0.0) {
            return length;
         }
      }

      return 0.0 / 0.0;
   }

   private static long fallbackSeed(UUID uuid) {
      return mix64(uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 29));
   }

   private static long mix64(long value) {
      value = (value ^ value >>> 30) * -4658895280553007687L;
      value = (value ^ value >>> 27) * -7723592293110705685L;
      return value ^ value >>> 31;
   }

   private static boolean isGenerated(Identifier texture) {
      return "tide_traits".equals(texture.getNamespace()) && texture.getPath().startsWith("dynamic/mutation/");
   }

   @Environment(EnvType.CLIENT)
   static final class RenderFailureLog {
      private static final long LOG_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(30L);
      private static final int MAX_SEEN_KEYS = 256;
      private static final Set<String> SEEN = new LinkedHashSet<>();
      private static long nextLogNanos;

      private RenderFailureLog() {
      }

      static synchronized void warn(String key, String message, Throwable error) {
         if (SEEN.add(key)) {
            if (SEEN.size() > MAX_SEEN_KEYS) {
               SEEN.remove(SEEN.iterator().next());
            }

            long now = System.nanoTime();
            if (now >= nextLogNanos) {
               nextLogNanos = now + LOG_INTERVAL_NANOS;
               TideTraits.LOGGER.warn("{}: {}", message, error.toString());
            }
         }
      }

      static synchronized void reset() {
         SEEN.clear();
         nextLogNanos = 0L;
      }
   }
}
