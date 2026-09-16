package com.redslovesgames.tideborne.satchel.client;

import com.redslovesgames.tideborne.api.TideborneFishingApi;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

/**
 * Thin read-only Satchel adapter over canonical specimen state and the shared presentation layer.
 * This class never generates, migrates, normalizes, or recalculates specimen state.
 */
@Environment(EnvType.CLIENT)
final class SatchelSpecimenDisplay {
   private static final Map<ItemStack, Optional<SatchelSpecimenDisplay>> DISPLAY_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

   private final SpecimenData specimen;
   private final Integer knownRarityStars;
   private CanonicalSpecimenPresentation.View presentation;

   private SatchelSpecimenDisplay(SpecimenData specimen, Integer knownRarityStars) {
      this.specimen = specimen;
      this.knownRarityStars = knownRarityStars;
   }

   static Optional<SatchelSpecimenDisplay> from(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return Optional.empty();
      }
      return DISPLAY_CACHE.computeIfAbsent(
         stack,
         value -> TideborneFishingApi.readCurrentSpecimen(value).map(specimen -> new SatchelSpecimenDisplay(specimen, null))
      );
   }

   static SatchelSpecimenDisplay fromCanonical(SpecimenData specimen, int rarityStars) {
      if (specimen == null) {
         throw new IllegalArgumentException("canonical specimen is required");
      }
      return new SatchelSpecimenDisplay(specimen, rarityStars);
   }

   CanonicalSpecimenPresentation.View presentation() {
      if (this.presentation == null) {
         int rarityStars = this.knownRarityStars != null
            ? this.knownRarityStars
            : TideborneFishingApi.resolveSpeciesProfile(this.specimen.speciesId()).map(profile -> profile.rarity().stars()).orElse(0);
         this.presentation = CanonicalSpecimenPresentation.present(this.specimen, rarityStars);
      }
      return this.presentation;
   }

   List<TraitDisplay> traits() {
      return this.presentation().traits();
   }

   double length() {
      return this.specimen.finalLength();
   }

   String lengthLabel() {
      return this.presentation().length();
   }

   double percentile() {
      return this.specimen.finalPercentile();
   }

   String percentileLabel() {
      return this.presentation().percentile();
   }

   SpecimenData.BodyType bodyType() {
      return this.specimen.bodyType();
   }

   SpecimenData.Condition condition() {
      return this.specimen.condition();
   }

   SpecimenData.Pigmentation pigmentation() {
      return this.specimen.pigmentation();
   }

   SpecimenData.SpecimenQuality quality() {
      return this.specimen.specimenQuality();
   }

   boolean perfectCatch() {
      return this.specimen.perfectCatch();
   }

   OptionalInt fishScore() {
      return TideborneFishingApi.readFishScore(this.specimen);
   }

   int scoreOrMissing() {
      return this.fishScore().orElse(-1);
   }

   String scoreLabel() {
      return CanonicalSpecimenPresentation.fishScore(this.fishScore());
   }

   String bodyTypeLabel() {
      return this.presentation().trait(CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE).value();
   }

   String conditionLabel() {
      return this.presentation().trait(CanonicalSpecimenPresentation.TraitAxis.CONDITION).value();
   }

   String pigmentationLabel() {
      return this.presentation().trait(CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION).value();
   }

   String qualityLabel() {
      return this.presentation().trait(CanonicalSpecimenPresentation.TraitAxis.QUALITY).value();
   }

   String rarityStarsLabel() {
      return this.presentation().rarityStars();
   }
}
