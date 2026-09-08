/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.fishing.specimen;

import com.li64.tide.data.SendableDataMap;
import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public final class FishDescriptorManager {
   private final AtomicReference<Map<Identifier, FishDescriptor>> descriptors = new AtomicReference<>(Map.of());

   public int rebuildFromTide() {
      if (TideData.FISH == null) {
         this.descriptors.set(Map.of());
         return 0;
      } else {
         SendableDataMap<FishData> data = (SendableDataMap<FishData>)TideData.FISH.get();
         if (data == null) {
            this.descriptors.set(Map.of());
            return 0;
         } else {
            return this.rebuild(data.valueStream());
         }
      }
   }

   public int rebuild(Stream<FishData> fishData) {
      List<FishData> entries = fishData.filter(Objects::nonNull).toList();
      LinkedHashMap<Identifier, FishDescriptor> rebuilt = new LinkedHashMap<>();

      for (FishData canonical : SnapshotParentResolver.resolve(
         entries, entry -> (Item)entry.fish().value(), entry -> entry.parent().map(parent -> (Item)parent.value())
      )) {
         FishDescriptor descriptor = FishDescriptor.fromCanonicalData(canonical);
         rebuilt.putIfAbsent(descriptor.canonicalSpeciesId(), descriptor);
      }

      Map<Identifier, FishDescriptor> snapshot = Map.copyOf(rebuilt);
      this.descriptors.set(snapshot);
      return snapshot.size();
   }

   public Optional<FishDescriptor> find(Identifier canonicalSpeciesId) {
      return Optional.ofNullable(this.descriptors.get().get(canonicalSpeciesId));
   }

   public Map<Identifier, FishDescriptor> snapshot() {
      return this.descriptors.get();
   }

   public void clear() {
      this.descriptors.set(Map.of());
   }
}
