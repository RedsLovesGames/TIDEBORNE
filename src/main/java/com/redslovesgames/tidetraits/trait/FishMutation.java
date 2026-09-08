/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum FishMutation {
   NORMAL("normal"),
   ALBINO("albino"),
   DWARF("dwarf"),
   GIANT("giant"),
   PERFECT_SPECIMEN("perfect_specimen"),
   SCARRED("scarred"),
   PARASITE_RIDDEN("parasite_ridden"),
   IRIDESCENT("iridescent");

   private static final List<FishMutation> MUTATIONS = List.of(SCARRED, PARASITE_RIDDEN, DWARF, GIANT, ALBINO, PERFECT_SPECIMEN, IRIDESCENT);
   public static final Codec<FishMutation> CODEC = Codec.STRING
      .comapFlatMap(
         name -> bySerializedName(name).<DataResult>map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown fish mutation: " + name)),
         FishMutation::serializedName
      );
   private final String serializedName;

   FishMutation(String serializedName) {
      this.serializedName = serializedName;
   }

   public String serializedName() {
      return this.serializedName;
   }

   public boolean isMutation() {
      return this != NORMAL;
   }

   public static List<FishMutation> mutations() {
      return MUTATIONS;
   }

   public static Optional<FishMutation> bySerializedName(String name) {
      if (name == null) {
         return Optional.empty();
      }

      String normalized = name.toLowerCase(Locale.ROOT);
      return List.of(values()).stream().filter(mutation -> mutation.serializedName.equals(normalized)).findFirst();
   }
}
