/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.command;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.fish.EmpiricalFishDistribution;
import com.redslovesgames.tidetraits.fish.FishDescriptor;
import com.redslovesgames.tidetraits.fish.FishPercentileService;
import com.redslovesgames.tidetraits.fish.FishSizeClass;
import com.redslovesgames.tidetraits.fish.SpecimenSizeService;
import com.redslovesgames.tidetraits.satchel.AnglersSatchelStorage;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import com.redslovesgames.tidetraits.satchel.SatchelState;
import com.redslovesgames.tidetraits.trait.FishMutation;
import com.redslovesgames.tidetraits.trait.SpecimenData;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.DataComponentTypes;

public final class TideTraitsCommands {
   private static final String ROOT = "tideborne_internal_traits";
   private static final TagKey<Item> ALL_MUTATIONS_EXCLUDED = itemTag("mutation_excluded");
   private static boolean initialized;

   private TideTraitsCommands() {
   }

   public static synchronized void init() {
      if (!initialized) {
         CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
               CommandManager.literal(ROOT)
                  .requires(source -> source.hasPermissionLevel(2))
                  .then(CommandManager.literal("inspect").executes(context -> inspect(context.getSource())))
                  .then(
                     CommandManager.literal("setmutation")
                        .then(
                           CommandManager.argument("id", StringArgumentType.word())
                              .executes(context -> setMutation(context.getSource(), StringArgumentType.getString(context, "id")))
                        )
                  )
                  .then(CommandManager.literal("clearmutation").executes(context -> clearMutation(context.getSource())))
                  .then(
                     CommandManager.literal("percentile")
                        .then(
                           CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                              .executes(context -> setPercentile(context.getSource(), DoubleArgumentType.getDouble(context, "value")))
                        )
                  )
                  .then(CommandManager.literal("dumpfish").executes(context -> dumpFish(context.getSource())))
            )
         );
         initialized = true;
      }
   }

   private static int inspect(ServerCommandSource source) throws CommandSyntaxException {
      ItemStack held = heldStack(source);
      if (held.isEmpty()) {
         return fail(source, "Hold an item in your main hand to inspect it.");
      }

      Identifier itemId = Registries.ITEM.getId(held.getItem());
      Optional<FishData> directFish = FishData.get(held);
      Optional<FishData> bucketFish = directFish.isPresent() ? Optional.empty() : FishData.fromBucket(held);
      Optional<FishData> recognizedFish = directFish.or(() -> bucketFish);
      source.sendFeedback(() -> Text.literal("Held: " + itemId + " x" + held.getCount()), false);
      recognizedFish.ifPresentOrElse(
         data -> {
            FishDescriptor descriptor = FishDescriptor.fromFishData(data);
            String representation = directFish.isPresent() ? "fish stack" : "fish bucket";
            source.sendFeedback(
               () -> Text.literal(
                  "Tide fish: " + descriptor.canonicalSpeciesId() + " (" + representation + ", physical-size=" + descriptor.supportsPhysicalLength() + ")"
               ),
               false
            );
         },
         () -> source.sendFeedback(() -> Text.literal("Tide fish: not recognized"), false)
      );
      TideTraitsCommands.SpecimenSnapshot specimen = directFish.isPresent()
         ? TideTraitsCommands.SpecimenSnapshot.fromStack(held)
         : TideTraitsCommands.SpecimenSnapshot.fromBucket(held).orElseGet(() -> TideTraitsCommands.SpecimenSnapshot.fromStack(held));
      source.sendFeedback(() -> Text.literal(specimen.describe()), false);
      if (SatchelRegistration.isAnglersSatchel(held)) {
         SatchelState state = AnglersSatchelStorage.state(held);
         int stored = AnglersSatchelStorage.size(held);
         int capacity = AnglersSatchelStorage.capacity(held);
         source.sendFeedback(
            () -> Text.literal(
               String.format(
                  Locale.ROOT,
                  "Satchel: %d/%d slots, level=%d, upgrades=%d, protected=%d, active=%s, open=%s",
                  stored,
                  capacity,
                  state.capacityLevel(),
                  state.unlockedFeatureIds().size(),
                  state.protectedSlots().size(),
                  state.isActive(),
                  state.isOpen()
               )
            ),
            false
         );
      }

      return 1;
   }

   private static int setMutation(ServerCommandSource source, String rawId) throws CommandSyntaxException {
      Optional<FishMutation> parsed = CommandInputs.mutation(rawId);
      if (parsed.isEmpty()) {
         return fail(source, "Unknown mutation ID. Valid IDs: " + CommandInputs.validMutationIds());
      } else {
         ItemStack held = heldStack(source);
         Optional<FishData> fishData = FishData.get(held);
         if (fishData.isEmpty()) {
            return fail(source, "Hold a Tide-recognized fish item (not a bucket) in your main hand.");
         } else {
            FishMutation mutation = parsed.get();
            FishDescriptor descriptor = FishDescriptor.fromFishData(fishData.get());
            if (mutation.isMutation() && isExcluded(held, mutation)) {
               return fail(source, "That fish is excluded from " + mutation.serializedName() + " by its item tags.");
            } else {
               return requiresPhysicalSize(mutation) && !descriptor.supportsPhysicalLength()
                  ? fail(source, mutation.serializedName() + " requires Tide SizeData for this species.")
                  : applyMutationEdit(source, held, descriptor, mutation, false);
            }
         }
      }
   }

   private static int clearMutation(ServerCommandSource source) throws CommandSyntaxException {
      ItemStack held = heldStack(source);
      Optional<FishData> fishData = FishData.get(held);
      if (fishData.isEmpty()) {
         return fail(source, "Hold a Tide-recognized fish item (not a bucket) in your main hand.");
      }

      FishDescriptor descriptor = FishDescriptor.fromFishData(fishData.get());
      return applyMutationEdit(source, held, descriptor, FishMutation.NORMAL, true);
   }

   private static int setPercentile(ServerCommandSource source, double normalized) throws CommandSyntaxException {
      double requestedPercent;
      try {
         requestedPercent = CommandInputs.percentilePercent(normalized);
      } catch (IllegalArgumentException exception) {
         return fail(source, exception.getMessage());
      }

      ItemStack held = heldStack(source);
      TraitAxesRuntime.migrateLegacy(held);
      Optional<FishData> fishData = FishData.get(held);
      if (fishData.isEmpty()) {
         return fail(source, "Hold a Tide-recognized fish item (not a bucket) in your main hand.");
      }

      FishDescriptor descriptor = FishDescriptor.fromFishData(fishData.get());
      if (descriptor.sizeData().isEmpty()) {
         return fail(source, "Tide has no SizeData for " + descriptor.canonicalSpeciesId() + ".");
      }

      FishPercentileService percentiles = CatchTraitService.INSTANCE.percentiles();
      Optional<EmpiricalFishDistribution> baseline = percentiles.baseline(descriptor.canonicalSpeciesId(), descriptor.sizeData().get());
      if (baseline.isEmpty()) {
         return fail(source, "Tide produced no usable size distribution for " + descriptor.canonicalSpeciesId() + ".");
      }

      double lengthCm = baseline.get().lengthAtPercentile(requestedPercent);
      double actualPercent = baseline.get().percentileOf(lengthCm);
      FishSizeClass band = FishSizeClass.fromPercentile(actualPercent);
      String mutation = (String)held.get(TideTraitsComponents.MUTATION);
      if (mutation == null || mutation.isBlank()) {
         held.set(TideTraitsComponents.MUTATION, FishMutation.NORMAL.serializedName());
      }

      ensureSeed(held, source);
      lengthCm = TraitAxesRuntime.applyCurrentPhysicalEffects(held, lengthCm, CatchTraitService.INSTANCE.config());
      final double physicalLengthCm = lengthCm;
      TideItemData.FISH_LENGTH.set(held, physicalLengthCm);
      held.set(TideTraitsComponents.SIZE_PERCENTILE, actualPercent);
      source.sendFeedback(
         () -> Text.literal(
            String.format(
               Locale.ROOT,
               "Set %s target quantile to %.4f (physical length %.3f cm); baseline percentile %.4f%%, band %s. Body Type and Condition preserved.",
               descriptor.canonicalSpeciesId(),
               normalized,
               physicalLengthCm,
               actualPercent,
               band.serializedName()
            )
         ),
         false
      );
      return 1;
   }

   private static int dumpFish(ServerCommandSource source) {
      CatchTraitService service = CatchTraitService.INSTANCE;
      int descriptors = service.descriptors().snapshot().size();
      long physical = service.descriptors().snapshot().values().stream().filter(FishDescriptor::supportsPhysicalLength).count();
      int cached = service.percentiles().cachedSpeciesCount();
      source.sendFeedback(
         () -> Text.literal(
            "Fish registries: descriptors="
               + descriptors
               + ", with SizeData="
               + physical
               + ", without SizeData="
               + (descriptors - physical)
               + ", cached distributions="
               + cached
               + " (4096 deterministic samples each)."
         ),
         false
      );
      return 1;
   }

   private static ItemStack heldStack(ServerCommandSource source) throws CommandSyntaxException {
      ServerPlayerEntity player = source.getPlayerOrThrow();
      return player.getMainHandStack();
   }

   private static long ensureSeed(ItemStack stack, ServerCommandSource source) {
      Long existing = (Long)stack.get(TideTraitsComponents.MUTATION_SEED);
      if (existing != null) {
         return existing;
      }

      long generated = source.getWorld().getRandom().nextLong();
      stack.set(TideTraitsComponents.MUTATION_SEED, generated);
      return generated;
   }

   private static int applyMutationEdit(ServerCommandSource source, ItemStack held, FishDescriptor descriptor, FishMutation newMutation, boolean clearing) {
      TraitAxesRuntime.migrateLegacy(held);
      long seed = ensureSeed(held, source);
      FishMutation oldMutation = FishMutation.bySerializedName((String)held.get(TideTraitsComponents.MUTATION)).orElse(FishMutation.NORMAL);
      double currentLength = (Double)TideItemData.FISH_LENGTH.getOrDefault(held, 0.0);
      SpecimenSizeService sizeService = new SpecimenSizeService(CatchTraitService.INSTANCE.percentiles());
      double normalLength = sizeService.recoverNormalLength(SpecimenData.unclassified(seed, oldMutation), currentLength, CatchTraitService.INSTANCE.config());
      normalLength = TraitAxesRuntime.recoverBodyNormalLength(held, normalLength, CatchTraitService.INSTANCE.config());
      if ((!Double.isFinite(normalLength) || normalLength <= 0.0) && descriptor.sizeData().isPresent()) {
         normalLength = descriptor.fishData().getRandomLength(source.getWorld().getRandom());
      }

      SpecimenSizeService.AppliedSize applied = sizeService.applyNew(
         SpecimenData.unclassified(seed, TraitAxesRuntime.conditionMutationForEdit(held, newMutation, clearing)),
         normalLength,
         descriptor,
         CatchTraitService.INSTANCE.config()
      );
      applied = TraitAxesRuntime.finishAdminEdit(held, newMutation, clearing, applied, CatchTraitService.INSTANCE.config());
      final SpecimenSizeService.AppliedSize finalApplied = applied;
      held.set(TideTraitsComponents.MUTATION, TraitAxesRuntime.conditionForEdit(held, newMutation, clearing));
      held.set(TideTraitsComponents.MUTATION_SEED, seed);
      if (finalApplied.percentile().isPresent()) {
         held.set(TideTraitsComponents.SIZE_PERCENTILE, finalApplied.percentile().getAsDouble());
      } else {
         held.remove(TideTraitsComponents.SIZE_PERCENTILE);
      }

      if (Double.isFinite(finalApplied.finalPhysicalLengthCm()) && finalApplied.finalPhysicalLengthCm() > 0.0) {
         TideItemData.FISH_LENGTH.set(held, finalApplied.finalPhysicalLengthCm());
      }

      String verb = clearing ? "Cleared" : "Set";
      source.sendFeedback(
         () -> Text.literal(
            String.format(
               Locale.ROOT,
               "%s %s trait to %s (identity seed %d, coherent length %.3f cm%s).",
               verb,
               descriptor.canonicalSpeciesId(),
               newMutation.serializedName(),
               seed,
               finalApplied.finalPhysicalLengthCm(),
               finalApplied.percentile().isPresent() ? String.format(Locale.ROOT, ", percentile %.4f%%", finalApplied.percentile().getAsDouble()) : ", no Tide SizeData"
            )
         ),
         false
      );
      return 1;
   }

   private static boolean isExcluded(ItemStack stack, FishMutation mutation) {
      return stack.isIn(ALL_MUTATIONS_EXCLUDED) || stack.isIn(itemTag(mutation.serializedName() + "_excluded"));
   }

   private static boolean requiresPhysicalSize(FishMutation mutation) {
      return mutation == FishMutation.DWARF || mutation == FishMutation.GIANT;
   }

   private static int fail(ServerCommandSource source, String message) {
      source.sendError(Text.literal(message));
      return 0;
   }

   private static TagKey<Item> itemTag(String path) {
      return TagKey.of(RegistryKeys.ITEM, Identifier.of("tide_traits", path));
   }

   private record SpecimenSnapshot(String location, String mutation, Long seed, Double percentile, Double lengthCm, boolean protectedSpecimen) {
      private static TideTraitsCommands.SpecimenSnapshot fromStack(ItemStack stack) {
         Double length = TideItemData.FISH_LENGTH.isPresent(stack) ? (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0) : null;
         return new TideTraitsCommands.SpecimenSnapshot(
            "stack components",
            (String)stack.get(TideTraitsComponents.MUTATION),
            (Long)stack.get(TideTraitsComponents.MUTATION_SEED),
            (Double)stack.get(TideTraitsComponents.SIZE_PERCENTILE),
            validLengthOrNull(length),
            Boolean.TRUE.equals(stack.get(TideTraitsComponents.PROTECTED))
         );
      }

      private static Optional<TideTraitsCommands.SpecimenSnapshot> fromBucket(ItemStack stack) {
         NbtComponent customData = (NbtComponent)stack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
         if (customData == null) {
            return Optional.empty();
         }

         NbtCompound bucketTag = customData.copyNbt();
         if (!bucketTag.contains("TideTraits", 10)) {
            return Optional.empty();
         }

         NbtCompound tag = bucketTag.getCompound("TideTraits");
         return tag.isEmpty()
            ? Optional.empty()
            : Optional.of(
               new TideTraitsCommands.SpecimenSnapshot(
                  "bucket entity data",
                  tag.contains("Mutation", 8) ? tag.getString("Mutation") : null,
                  tag.contains("MutationSeed", 99) ? tag.getLong("MutationSeed") : null,
                  tag.contains("SizePercentile", 99) ? tag.getDouble("SizePercentile") : null,
                  tag.contains("LengthCm", 99) ? validLengthOrNull(tag.getDouble("LengthCm")) : null,
                  false
               )
            );
      }

      private String describe() {
         String mutationValue = this.mutation != null && !this.mutation.isBlank() ? this.mutation : "unassigned";
         String seedValue = this.seed == null ? "none" : Long.toString(this.seed);
         String percentileValue = validPercentile(this.percentile)
            ? String.format(Locale.ROOT, "%.4f%% (%s)", this.percentile, FishSizeClass.fromPercentile(this.percentile).serializedName())
            : "none";
         String lengthValue = this.lengthCm == null ? "none" : String.format(Locale.ROOT, "%.3f cm", this.lengthCm);
         return "Specimen ["
            + this.location
            + "]: mutation="
            + mutationValue
            + ", seed="
            + seedValue
            + ", length="
            + lengthValue
            + ", percentile="
            + percentileValue
            + ", protected="
            + this.protectedSpecimen;
      }

      private static Double validLengthOrNull(Double value) {
         return value != null && Double.isFinite(value) && value > 0.0 ? value : null;
      }

      private static boolean validPercentile(Double value) {
         return value != null && Double.isFinite(value) && value >= 0.0 && value <= 100.0;
      }
   }
}
