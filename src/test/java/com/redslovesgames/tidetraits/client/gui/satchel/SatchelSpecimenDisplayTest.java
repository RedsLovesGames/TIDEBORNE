package com.redslovesgames.tidetraits.client.gui.satchel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class SatchelSpecimenDisplayTest {
   @Test
   void projectsCanonicalSpecimenFieldsExactlyWithoutRecalculation() {
      SpecimenData specimen = specimen(OptionalInt.of(2711));
      SatchelSpecimenDisplay display = SatchelSpecimenDisplay.fromCanonical(specimen, 5);

      assertEquals(specimen.finalLength(), display.length());
      assertEquals(specimen.finalPercentile(), display.percentile());
      assertEquals(SpecimenData.BodyType.GIANT, display.bodyType());
      assertEquals(SpecimenData.Condition.SCARRED, display.condition());
      assertEquals(SpecimenData.Pigmentation.IRIDESCENT, display.pigmentation());
      assertEquals(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN, display.quality());
      assertTrue(display.perfectCatch());
      assertEquals(OptionalInt.of(2711), display.fishScore());
      assertEquals(2711, display.scoreOrMissing());
      assertEquals("2711", display.scoreLabel());
      assertEquals("★★★★★", display.rarityStarsLabel());
   }

   @Test
   void displayLabelsAndOrderComeFromCanonicalPresentation() {
      SatchelSpecimenDisplay display = SatchelSpecimenDisplay.fromCanonical(specimen(OptionalInt.of(2711)), 4);

      assertEquals("47.8 cm", display.lengthLabel());
      assertEquals("P99.1", display.percentileLabel());
      assertEquals("Giant", display.bodyTypeLabel());
      assertEquals("Scarred", display.conditionLabel());
      assertEquals("Iridescent", display.pigmentationLabel());
      assertEquals("Perfect Specimen", display.qualityLabel());
      assertEquals("★★★★", display.rarityStarsLabel());
      assertEquals(List.of(
         CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE,
         CanonicalSpecimenPresentation.TraitAxis.CONDITION,
         CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION,
         CanonicalSpecimenPresentation.TraitAxis.QUALITY
      ), display.traits().stream().map(CanonicalSpecimenPresentation.TraitDisplay::axis).toList());
      assertEquals(display.presentation().traits(), display.traits());
   }

   @Test
   void scorelessCanonicalSpecimenRemainsScoreless() {
      SatchelSpecimenDisplay display = SatchelSpecimenDisplay.fromCanonical(specimen(OptionalInt.empty()), 0);

      assertFalse(display.fishScore().isPresent());
      assertEquals(-1, display.scoreOrMissing());
      assertEquals("N/A", display.scoreLabel());
      assertEquals("N/A", display.rarityStarsLabel());
   }

   private static SpecimenData specimen(OptionalInt score) {
      return new SpecimenData(
         "tide:stage_44_test",
         SpecimenGenerator.SCHEMA_VERSION,
         SpecimenGenerator.GENERATION_VERSION,
         0x44AAL,
         97.25,
         38.5,
         47.75,
         99.125,
         SpecimenData.BodyType.GIANT,
         SpecimenData.Condition.SCARRED,
         SpecimenData.Pigmentation.IRIDESCENT,
         SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
         true,
         OptionalDouble.of(812.375),
         score,
         SpecimenData.Provenance.generated()
      );
   }
}
