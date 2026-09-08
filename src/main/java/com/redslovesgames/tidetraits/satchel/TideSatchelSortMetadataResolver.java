/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public final class TideSatchelSortMetadataResolver implements Function<ItemStack, SatchelSortDescriptor> {
   private final Function<ItemStack, SatchelTraitSortData> traitResolver;

   public TideSatchelSortMetadataResolver(Function<ItemStack, SatchelTraitSortData> traitResolver) {
      this.traitResolver = Objects.requireNonNull(traitResolver, "traitResolver");
   }

   public static TideSatchelSortMetadataResolver tideOnly() {
      return new TideSatchelSortMetadataResolver(stack -> SatchelTraitSortData.NORMAL);
   }

   public SatchelSortDescriptor apply(ItemStack stack) {
      SatchelTraitSortData trait = Objects.requireNonNullElse(this.traitResolver.apply(stack), SatchelTraitSortData.NORMAL);
      FishData data = (FishData)FishData.get(stack).orElse(null);
      int rarity = data == null ? -1 : data.profile().rarity().ordinal();
      String region = data == null ? "other / unknown" : data.profile().location().orElse("other / unknown");
      double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(stack, 0.0);
      String registryId = Registries.ITEM.getId(stack.getItem()).toString();
      return new SatchelSortDescriptor(
         stack.getName().getString().toLowerCase(Locale.ROOT),
         trait.configuredMutationProbability(),
         trait.percentile(),
         rarity,
         region,
         length,
         registryId,
         trait.mutationId(),
         trait.deterministicSeed()
      );
   }
}
