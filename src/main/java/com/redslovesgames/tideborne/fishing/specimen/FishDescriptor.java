/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.specimen;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public record FishDescriptor(Identifier canonicalSpeciesId, Item item, FishData fishData, Optional<SizeData> sizeData) {
   public FishDescriptor {
      Objects.requireNonNull(canonicalSpeciesId, "canonicalSpeciesId");
      Objects.requireNonNull(item, "item");
      Objects.requireNonNull(fishData, "fishData");
      Objects.requireNonNull(sizeData, "sizeData");
   }

   public static FishDescriptor fromFishData(FishData data) {
      Objects.requireNonNull(data, "data");
      FishData canonical = FishData.get((Item)data.fish().value()).orElse(data);
      return fromCanonicalData(canonical);
   }

   static FishDescriptor fromCanonicalData(FishData canonical) {
      Objects.requireNonNull(canonical, "canonical");
      Item canonicalItem = (Item)canonical.fish().value();
      Identifier canonicalId = Registries.ITEM.getId(canonicalItem);
      return new FishDescriptor(canonicalId, canonicalItem, canonical, canonical.size());
   }

   public boolean supportsPhysicalLength() {
      return this.sizeData.isPresent();
   }
}
