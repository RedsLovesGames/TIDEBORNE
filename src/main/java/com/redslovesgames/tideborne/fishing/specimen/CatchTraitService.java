/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.specimen;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.TideTraits;
import com.redslovesgames.tideborne.discovery.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import com.redslovesgames.tideborne.config.TideTraitsConfig;
import com.redslovesgames.tideborne.config.TideTraitsConfigManager;
import com.redslovesgames.tideborne.discovery.DiscoveryManager;
import com.redslovesgames.tideborne.fishing.specimen.FishDescriptor;
import com.redslovesgames.tideborne.fishing.specimen.FishDescriptorManager;
import com.redslovesgames.tideborne.fishing.specimen.FishPercentileService;
import com.redslovesgames.tideborne.fishing.specimen.FishSizeClass;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenSizeService;
import com.redslovesgames.tideborne.fishing.specimen.legacy.FishMutation;
import com.redslovesgames.tideborne.fishing.specimen.legacy.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.legacy.TraitAxesRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.random.Random;

public final class CatchTraitService {
   public static final CatchTraitService INSTANCE = new CatchTraitService();
   private static final Identifier TIDE_FISH_RELOAD = Identifier.of("tide", "fishing/fish");
   private final FishDescriptorManager descriptors = new FishDescriptorManager();
   private final FishPercentileService percentiles = new FishPercentileService();
   // Retained only for classifying already-saved legacy physical lengths during migration.
   private final SpecimenSizeService legacyMigrationSizes = new SpecimenSizeService(this.percentiles);
   private volatile TideTraitsConfig config = TideTraitsConfig.defaults();

   private CatchTraitService() {
   }

   public void init() {
      ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new CatchTraitService.ReloadListener());
   }

   public TideTraitsConfig config() {
      return this.config;
   }

   public void setConfig(TideTraitsConfig config) {
      this.config = Objects.requireNonNull(config, "config");
   }

   public FishDescriptorManager descriptors() {
      return this.descriptors;
   }

   /** Legacy percentile baselines remain readable for migration/compatibility only. */
   public FishPercentileService percentiles() {
      return this.percentiles;
   }

   public List<ItemStack> individualizeNewCatches(List<ItemStack> catches, Random ignoredRandom) {
      if (catches != null && !catches.isEmpty()) {
         List<ItemStack> individualized = new ArrayList<>();

         for (ItemStack stack : catches) {
            if (stack != null && !stack.isEmpty() && !FishData.get(stack).isEmpty()) {
               int count = Math.max(1, stack.getCount());

               for (int specimenIndex = 0; specimenIndex < count; specimenIndex++) {
                  ItemStack specimen = count == 1 ? stack : stack.copyWithCount(1);
                  this.assignIfAbsent(specimen, ignoredRandom);
                  individualized.add(specimen);
               }
            } else {
               individualized.add(stack == null ? ItemStack.EMPTY : stack);
            }
         }

         return individualized;
      } else {
         return catches == null ? List.of() : new ArrayList<>(catches);
      }
   }

   /**
    * Compatibility gate retained at the reconstructed Tide hook sites.
    *
    * Fishing System 2.0 owns every new-catch trait axis and the canonical specimen seed before this
    * method runs. This method may mirror canonical fields or normalize already-persisted legacy
    * fields, but it never selects a mutation, creates a seed, or consumes the supplied RNG.
    */
   public boolean assignIfAbsent(ItemStack stack, Random ignoredRandom) {
      if (TraitAxesRuntime.isCanonicalV2(stack)) {
         String canonicalCondition = (String)stack.get(TideTraitsComponents.SPECIMEN_CONDITION);
         if (canonicalCondition != null && !canonicalCondition.isBlank()) {
            stack.set(TideTraitsComponents.MUTATION, canonicalCondition);
         }

         TraitAxesRuntime.migrateLegacy(stack);
         return false;
      }

      Optional<FishData> fishData = FishData.get(stack);
      if (fishData.isEmpty()) {
         return false;
      }

      String existingId = (String)stack.get(TideTraitsComponents.MUTATION);
      if (existingId != null && !existingId.isBlank()) {
         this.migratePersistedClassification(stack, fishData.get());
      }

      // Fresh noncanonical fish are deliberately left untouched. New runtime catches have already
      // been created by the V2 specimen pipeline; this branch exists only for legacy compatibility.
      return false;
   }

   public void ensureAssignedBeforeLog(ItemStack stack, Random ignoredRandom) {
      this.assignIfAbsent(stack, ignoredRandom);
   }

   public void recordSuccessfulCatch(ItemStack stack, ServerPlayerEntity player) {
      Optional<FishData> fishData = FishData.get(stack);
      if (!fishData.isEmpty()) {
         Identifier species = FishDescriptor.fromFishData(fishData.get()).canonicalSpeciesId();
         Identifier sharedMutation = null;
         Identifier sharedSizeBand = null;
         String mutationValue = (String)stack.get(TideTraitsComponents.MUTATION);
         Optional<FishMutation> mutation = FishMutation.bySerializedName(mutationValue);
         if (mutation.isPresent() && mutation.get().isMutation()) {
            sharedMutation = namespaced(mutation.get().serializedName());
         }

         Double percentile = (Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE);
         if (percentile != null && Double.isFinite(percentile) && percentile >= 0.0 && percentile <= 100.0) {
            FishSizeClass sizeClass = FishSizeClass.fromPercentile(percentile);
            sharedSizeBand = namespaced(sizeClass.serializedName());
         }

         if (!TideTraitsConfigManager.current().sharedDiscovery() || sharedMutation == null && sharedSizeBand == null) {
            boolean changed = false;
            if (sharedMutation != null) {
               changed = DiscoveryManager.discoverMutation(player, species, sharedMutation);
            }

            if (sharedSizeBand != null) {
               changed |= DiscoveryManager.discoverSizeBand(player, species, sharedSizeBand);
            }

            if (changed) {
               DiscoveryManager.sync(player);
            }

            tideborne$recordBodyTypeDiscovery(stack, player, species);
         } else {
            MultiplayerDiscoveryCompat.recordCatch(player, species, sharedMutation, sharedSizeBand);
            tideborne$recordBodyTypeDiscovery(stack, player, species);
         }
      }
   }

   /**
    * Compatibility-only old-world normalization. It may infer a percentile from an already-saved
    * physical length when the persisted legacy identity seed exists. It never invents a seed or
    * generates a mutation/trait.
    */
   private void migratePersistedClassification(ItemStack stack, FishData data) {
      TraitAxesRuntime.migrateLegacy(stack);
      String mutationId = (String)stack.get(TideTraitsComponents.MUTATION);
      Optional<FishMutation> mutation = FishMutation.bySerializedName(mutationId);
      if (mutation.isEmpty()) {
         return;
      }

      Long seed = (Long)stack.get(TideTraitsComponents.MUTATION_SEED);
      if (seed == null) {
         return;
      }

      Double existingPercentile = (Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE);
      if (existingPercentile == null || !Double.isFinite(existingPercentile)) {
         double finalLength = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
         FishDescriptor descriptor = FishDescriptor.fromFishData(data);
         SpecimenData specimen = SpecimenData.unclassified(seed, mutation.get());
         SpecimenSizeService.AppliedSize classified = this.legacyMigrationSizes
            .classifyExistingFinalLength(
               specimen,
               TraitAxesRuntime.recoverCurrentNormalLength(stack, finalLength, this.config),
               descriptor.canonicalSpeciesId(),
               descriptor.sizeData()
            );
         classified.percentile().ifPresent(value -> stack.set(TideTraitsComponents.SIZE_PERCENTILE, value));
      }
   }

   private static Identifier namespaced(String path) {
      return Identifier.of("tide_traits", path);
   }

   private static void tideborne$recordBodyTypeDiscovery(ItemStack var0, ServerPlayerEntity var1, Identifier var2) {
      String var3 = TraitAxesRuntime.bodyType(var0);
      if (!"normal".equals(var3)) {
         Identifier var4 = namespaced(var3);
         if (TideTraitsConfigManager.current().sharedDiscovery() && MultiplayerDiscoveryCompat.isAvailable(var1)) {
            MultiplayerDiscoveryCompat.recordCatch(var1, var2, var4, null);
         } else {
            DiscoveryManager.discoverMutationAndSync(var1, var2, var4);
         }
      }
   }

   private final class ReloadListener implements SimpleSynchronousResourceReloadListener {
      private final Identifier id = CatchTraitService.namespaced("fish_descriptors");

      public Identifier getFabricId() {
         return this.id;
      }

      public Collection<Identifier> getFabricDependencies() {
         return List.of(CatchTraitService.TIDE_FISH_RELOAD);
      }

      public void reload(ResourceManager resourceManager) {
         int count = CatchTraitService.this.descriptors.rebuildFromTide();
         CatchTraitService.this.percentiles.rebuild(CatchTraitService.this.descriptors.snapshot().values());
         TideTraits.LOGGER.info("Indexed {} Tide-compatible canonical fish species", count);
      }
   }
}
