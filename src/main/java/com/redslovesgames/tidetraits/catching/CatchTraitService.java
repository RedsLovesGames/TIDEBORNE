/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.catching;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tidetraits.TideTraits;
import com.redslovesgames.tidetraits.compat.multiplayer.MultiplayerDiscoveryCompat;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.discovery.DiscoveryManager;
import com.redslovesgames.tidetraits.fish.FishDescriptor;
import com.redslovesgames.tidetraits.fish.FishDescriptorManager;
import com.redslovesgames.tidetraits.fish.FishPercentileService;
import com.redslovesgames.tidetraits.fish.FishSizeClass;
import com.redslovesgames.tidetraits.fish.SpecimenSizeService;
import com.redslovesgames.tidetraits.trait.FishMutation;
import com.redslovesgames.tidetraits.trait.MutationSelector;
import com.redslovesgames.tidetraits.trait.SpecimenData;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;

public final class CatchTraitService {
   public static final CatchTraitService INSTANCE = new CatchTraitService();
   private static final Identifier TIDE_FISH_RELOAD = Identifier.of("tide", "fishing/fish");
   private static final TagKey<Item> ALL_EXCLUDED = itemTag("mutation_excluded");
   private final FishDescriptorManager descriptors = new FishDescriptorManager();
   private final FishPercentileService percentiles = new FishPercentileService();
   private final SpecimenSizeService sizes = new SpecimenSizeService(this.percentiles);
   private final MutationSelector selector = new MutationSelector();
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

   public FishPercentileService percentiles() {
      return this.percentiles;
   }

   public List<ItemStack> individualizeNewCatches(List<ItemStack> catches, Random random) {
      if (catches != null && !catches.isEmpty()) {
         List<ItemStack> individualized = new ArrayList<>();

         for (ItemStack stack : catches) {
            if (stack != null && !stack.isEmpty() && !FishData.get(stack).isEmpty()) {
               int count = Math.max(1, stack.getCount());

               for (int specimenIndex = 0; specimenIndex < count; specimenIndex++) {
                  ItemStack specimen = count == 1 ? stack : stack.copyWithCount(1);
                  this.assignIfAbsent(specimen, random);
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

   public boolean assignIfAbsent(ItemStack stack, Random random) {
      Optional<FishData> fishData = FishData.get(stack);
      if (fishData.isEmpty()) {
         return false;
      }

      String existingId = (String)stack.get(TideTraitsComponents.MUTATION);
      if (existingId != null && !existingId.isBlank()) {
         this.migrateExistingClassification(stack, fishData.get(), random);
         return false;
      }

      FishDescriptor descriptor = FishDescriptor.fromFishData(fishData.get());
      RandomGenerator oneDraw = random::nextLong;
      Predicate<FishMutation> eligibility = mutation -> this.isEligible(stack, descriptor, mutation);
      SpecimenData selected = this.selector.assignOrKeep(Optional.empty(), oneDraw, this.config, eligibility);
      double normalLength = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
      if ((!Double.isFinite(normalLength) || normalLength <= 0.0) && descriptor.sizeData().isPresent()) {
         normalLength = descriptor.fishData().getRandomLength(random);
      }

      SpecimenSizeService.AppliedSize applied = this.sizes.applyNew(selected, normalLength, descriptor, this.config);
      applied = TraitAxesRuntime.normalizeNew(stack, applied, this.config);
      writeSpecimen(stack, applied.specimen());
      if (Double.isFinite(applied.finalPhysicalLengthCm()) && applied.finalPhysicalLengthCm() > 0.0) {
         TideItemData.FISH_LENGTH.set(stack, applied.finalPhysicalLengthCm());
      }

      return true;
   }

   public void ensureAssignedBeforeLog(ItemStack stack, Random random) {
      this.assignIfAbsent(stack, random);
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

   private void migrateExistingClassification(ItemStack stack, FishData data, Random random) {
      TraitAxesRuntime.migrateLegacy(stack);
      String mutationId = (String)stack.get(TideTraitsComponents.MUTATION);
      Optional<FishMutation> mutation = FishMutation.bySerializedName(mutationId);
      if (mutation.isEmpty()) {
         if (stack.get(TideTraitsComponents.MUTATION_SEED) == null) {
            stack.set(TideTraitsComponents.MUTATION_SEED, random.nextLong());
         }
      } else {
         Long seed = (Long)stack.get(TideTraitsComponents.MUTATION_SEED);
         if (seed == null) {
            seed = random.nextLong();
            stack.set(TideTraitsComponents.MUTATION_SEED, seed);
         }

         Double existingPercentile = (Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE);
         if (existingPercentile == null || !Double.isFinite(existingPercentile)) {
            double finalLength = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
            FishDescriptor descriptor = FishDescriptor.fromFishData(data);
            SpecimenData specimen = SpecimenData.unclassified(seed, mutation.get());
            SpecimenSizeService.AppliedSize classified = this.sizes
               .classifyExistingFinalLength(
                  specimen,
                  TraitAxesRuntime.recoverCurrentNormalLength(stack, finalLength, this.config),
                  descriptor.canonicalSpeciesId(),
                  descriptor.sizeData()
               );
            classified.percentile().ifPresent(value -> stack.set(TideTraitsComponents.SIZE_PERCENTILE, value));
         }
      }
   }

   private boolean isEligible(ItemStack stack, FishDescriptor descriptor, FishMutation mutation) {
      if (mutation == FishMutation.DWARF || mutation == FishMutation.GIANT || mutation == FishMutation.PERFECT_SPECIMEN) {
         return false;
      }

      if (!stack.isIn(ALL_EXCLUDED) && !stack.isIn(itemTag(mutation.serializedName() + "_excluded"))) {
         return switch (mutation) {
            case DWARF, GIANT, PERFECT_SPECIMEN -> descriptor.supportsPhysicalLength();
            default -> true;
         };
      } else {
         return false;
      }
   }

   private static void writeSpecimen(ItemStack stack, SpecimenData specimen) {
      stack.set(TideTraitsComponents.MUTATION, specimen.mutation().serializedName());
      stack.set(TideTraitsComponents.MUTATION_SEED, specimen.identitySeed());
      OptionalDouble percentile = specimen.physicalSizePercentile();
      if (percentile.isPresent()) {
         stack.set(TideTraitsComponents.SIZE_PERCENTILE, percentile.getAsDouble());
      } else {
         stack.remove(TideTraitsComponents.SIZE_PERCENTILE);
      }
   }

   private static TagKey<Item> itemTag(String path) {
      return TagKey.of(RegistryKeys.ITEM, namespaced(path));
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
