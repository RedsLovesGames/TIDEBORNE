package com.redslovesgames.tidetraits.client.gui.satchel;

import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Read-only client projection of the canonical Fishing System 2.0 specimen fields used by the
 * Angler's Satchel UI. This class never generates, normalizes, or recalculates specimen state.
 */
@Environment(EnvType.CLIENT)
final class SatchelSpecimenDisplay {
   private final double length;
   private final double percentile;
   private final SpecimenData.BodyType bodyType;
   private final SpecimenData.Condition condition;
   private final SpecimenData.Pigmentation pigmentation;
   private final SpecimenData.SpecimenQuality quality;
   private final boolean perfectCatch;
   private final OptionalInt fishScore;
   private final int rarityStars;

   private SatchelSpecimenDisplay(
      double length,
      double percentile,
      SpecimenData.BodyType bodyType,
      SpecimenData.Condition condition,
      SpecimenData.Pigmentation pigmentation,
      SpecimenData.SpecimenQuality quality,
      boolean perfectCatch,
      OptionalInt fishScore,
      int rarityStars
   ) {
      this.length = length;
      this.percentile = percentile;
      this.bodyType = bodyType;
      this.condition = condition;
      this.pigmentation = pigmentation;
      this.quality = quality;
      this.perfectCatch = perfectCatch;
      this.fishScore = fishScore == null ? OptionalInt.empty() : fishScore;
      this.rarityStars = rarityStars >= 1 && rarityStars <= 5 ? rarityStars : 0;
   }

   static Optional<SatchelSpecimenDisplay> from(ItemStack stack) {
      if (stack == null
         || stack.isEmpty()
         || CanonicalSpecimenStorage.detectMigration(stack) != CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
         return Optional.empty();
      }

      // The state gate above guarantees read() is a decode-only canonical read on the client.
      return CanonicalSpecimenStorage.read(stack).map(specimen -> fromCanonical(specimen, rarityStars(specimen.speciesId())));
   }

   static SatchelSpecimenDisplay fromCanonical(SpecimenData specimen, int rarityStars) {
      if (specimen == null) {
         throw new IllegalArgumentException("canonical specimen is required");
      }
      return new SatchelSpecimenDisplay(
         specimen.finalLength(),
         specimen.finalPercentile(),
         specimen.bodyType(),
         specimen.condition(),
         specimen.pigmentation(),
         specimen.specimenQuality(),
         specimen.perfectCatch(),
         specimen.fishScore(),
         rarityStars
      );
   }

   double length() {
      return this.length;
   }

   double percentile() {
      return this.percentile;
   }

   SpecimenData.BodyType bodyType() {
      return this.bodyType;
   }

   SpecimenData.Condition condition() {
      return this.condition;
   }

   SpecimenData.Pigmentation pigmentation() {
      return this.pigmentation;
   }

   SpecimenData.SpecimenQuality quality() {
      return this.quality;
   }

   boolean perfectCatch() {
      return this.perfectCatch;
   }

   OptionalInt fishScore() {
      return this.fishScore;
   }

   int scoreOrMissing() {
      return this.fishScore.orElse(-1);
   }

   String scoreLabel() {
      return this.fishScore.isPresent() ? Integer.toString(this.fishScore.getAsInt()) : "--";
   }

   String bodyTypeLabel() {
      return titleCase(this.bodyType.name());
   }

   String conditionLabel() {
      return titleCase(this.condition.name());
   }

   String pigmentationLabel() {
      return titleCase(this.pigmentation.name());
   }

   String qualityLabel() {
      return titleCase(this.quality.name());
   }

   String rarityStarsLabel() {
      return this.rarityStars == 0 ? "?" : "★".repeat(this.rarityStars);
   }

   private static int rarityStars(String speciesId) {
      Identifier id = Identifier.tryParse(speciesId);
      if (id == null) {
         return 0;
      }

      try {
         Item item = Registries.ITEM.get(id);
         return FishData.get(item)
            .map(data -> data.profile().rarity().getNumStars())
            .filter(stars -> stars >= 1 && stars <= 5)
            .orElse(0);
      } catch (RuntimeException ignored) {
         return 0;
      }
   }

   private static String titleCase(String id) {
      StringBuilder output = new StringBuilder();
      for (String part : id.toLowerCase(Locale.ROOT).replace('-', '_').split("_")) {
         if (!part.isBlank()) {
            if (!output.isEmpty()) {
               output.append(' ');
            }
            output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
         }
      }
      return output.toString();
   }
}
