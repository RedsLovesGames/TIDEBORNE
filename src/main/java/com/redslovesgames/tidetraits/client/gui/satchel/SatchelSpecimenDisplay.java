package com.redslovesgames.tidetraits.client.gui.satchel;

import com.redslovesgames.tideborne.api.TideborneFishingApi;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

/**
 * Thin read-only Satchel adapter over canonical specimen state and the shared presentation layer.
 * This class never generates, migrates, normalizes, or recalculates specimen state.
 */
@Environment(EnvType.CLIENT)
final class SatchelSpecimenDisplay {
   private final SpecimenData specimen;
   private final CanonicalSpecimenPresentation.View presentation;

   private SatchelSpecimenDisplay(SpecimenData specimen, CanonicalSpecimenPresentation.View presentation) {
      this.specimen = specimen;
      this.presentation = presentation;
   }

   static Optional<SatchelSpecimenDisplay> from(ItemStack stack) {
      return TideborneFishingApi.readCurrentSpecimen(stack).map(specimen -> {
         int rarityStars = TideborneFishingApi.resolveSpeciesProfile(specimen.speciesId())
            .map(profile -> profile.rarity().stars())
            .orElse(0);
         return fromCanonical(specimen, rarityStars);
      });
   }

   static SatchelSpecimenDisplay fromCanonical(SpecimenData specimen, int rarityStars) {
      if (specimen == null) {
         throw new IllegalArgumentException("canonical specimen is required");
      }
      return new SatchelSpecimenDisplay(specimen, CanonicalSpecimenPresentation.present(specimen, rarityStars));
   }

   CanonicalSpecimenPresentation.View presentation() {
      return this.presentation;
   }

   List<TraitDisplay> traits() {
      return this.presentation.traits();
   }

   double length() {
      return this.specimen.finalLength();
   }

   String lengthLabel() {
      return this.presentation.length();
   }

   double percentile() {
      return this.specimen.finalPercentile();
   }

   String percentileLabel() {
      return this.presentation.percentile();
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
      return this.presentation.fishScore();
   }

   String bodyTypeLabel() {
      return this.presentation.trait(CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE).value();
   }

   String conditionLabel() {
      return this.presentation.trait(CanonicalSpecimenPresentation.TraitAxis.CONDITION).value();
   }

   String pigmentationLabel() {
      return this.presentation.trait(CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION).value();
   }

   String qualityLabel() {
      return this.presentation.trait(CanonicalSpecimenPresentation.TraitAxis.QUALITY).value();
   }

   String rarityStarsLabel() {
      return this.presentation.rarityStars();
   }
}
