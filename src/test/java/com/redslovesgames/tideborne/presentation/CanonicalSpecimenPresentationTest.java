package com.redslovesgames.tideborne.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class CanonicalSpecimenPresentationTest {
    @Test
    void canonicalViewOwnsStableTraitOrderLabelsAndColors() {
        CanonicalSpecimenPresentation.View view = CanonicalSpecimenPresentation.present(specimen(), CanonicalRarity.FOUR_STAR);

        assertEquals("tide:trout", view.speciesId());
        assertEquals("2471", view.fishScore());
        assertEquals("P96.4", view.percentile());
        assertEquals("1.24 m", view.length());
        assertEquals("★★★★", view.rarityStars());
        assertEquals(List.of(
                CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE,
                CanonicalSpecimenPresentation.TraitAxis.CONDITION,
                CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION,
                CanonicalSpecimenPresentation.TraitAxis.QUALITY
        ), view.traits().stream().map(CanonicalSpecimenPresentation.TraitDisplay::axis).toList());
        assertEquals("Giant", view.trait(CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE).value());
        assertEquals("Parasite Ridden", view.trait(CanonicalSpecimenPresentation.TraitAxis.CONDITION).value());
        assertEquals("Iridescent", view.trait(CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION).value());
        assertEquals("Perfect Specimen", view.trait(CanonicalSpecimenPresentation.TraitAxis.QUALITY).value());
        assertEquals(CanonicalSpecimenPresentation.BODY_TYPE_COLOR,
                view.trait(CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE).color());
        assertEquals(CanonicalSpecimenPresentation.CONDITION_COLOR,
                view.trait(CanonicalSpecimenPresentation.TraitAxis.CONDITION).color());
        assertEquals(CanonicalSpecimenPresentation.PIGMENTATION_COLOR,
                view.trait(CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION).color());
        assertEquals(CanonicalSpecimenPresentation.QUALITY_COLOR,
                view.trait(CanonicalSpecimenPresentation.TraitAxis.QUALITY).color());
    }

    @Test
    void historicalStringTraitsUseTheSameCanonicalProjection() {
        List<CanonicalSpecimenPresentation.TraitDisplay> traits = CanonicalSpecimenPresentation.traits(
                "giant",
                "parasite_ridden",
                "iridescent",
                "perfect_specimen"
        );

        assertEquals(List.of(
                CanonicalSpecimenPresentation.TraitAxis.BODY_TYPE,
                CanonicalSpecimenPresentation.TraitAxis.CONDITION,
                CanonicalSpecimenPresentation.TraitAxis.PIGMENTATION,
                CanonicalSpecimenPresentation.TraitAxis.QUALITY
        ), traits.stream().map(CanonicalSpecimenPresentation.TraitDisplay::axis).toList());
        assertEquals("Giant", traits.get(0).value());
        assertEquals("Parasite Ridden", traits.get(1).value());
        assertEquals("Iridescent", traits.get(2).value());
        assertEquals("Perfect Specimen", traits.get(3).value());
        assertEquals(CanonicalSpecimenPresentation.BODY_TYPE_COLOR, traits.get(0).color());
        assertEquals(CanonicalSpecimenPresentation.CONDITION_COLOR, traits.get(1).color());
        assertEquals(CanonicalSpecimenPresentation.PIGMENTATION_COLOR, traits.get(2).color());
        assertEquals(CanonicalSpecimenPresentation.QUALITY_COLOR, traits.get(3).color());

        List<CanonicalSpecimenPresentation.TraitDisplay> missing = CanonicalSpecimenPresentation.unavailableTraits();
        assertEquals(4, missing.size());
        assertEquals(List.of("N/A", "N/A", "N/A", "N/A"), missing.stream()
                .map(CanonicalSpecimenPresentation.TraitDisplay::value)
                .toList());
    }

    @Test
    void formattingAndRaritySentinelsAreCentralized() {
        assertEquals("N/A", CanonicalSpecimenPresentation.fishScore(OptionalInt.empty()));
        assertEquals("N/A", CanonicalSpecimenPresentation.percentile(Double.NaN));
        assertEquals("89.9 cm", CanonicalSpecimenPresentation.length(89.9));
        assertEquals("1.72 m", CanonicalSpecimenPresentation.length(171.6));
        assertEquals("Perfect Specimen", CanonicalSpecimenPresentation.trait("perfect_specimen"));
        assertEquals("★", CanonicalSpecimenPresentation.rarityStars(1));
        assertEquals("★★★★★", CanonicalSpecimenPresentation.rarityStars(CanonicalRarity.FIVE_STAR));
        assertEquals("N/A", CanonicalSpecimenPresentation.rarityStars(0));
        assertEquals("N/A", CanonicalSpecimenPresentation.rarityStars(6));
    }

    @Test
    void canonicalTraitListIsImmutable() {
        CanonicalSpecimenPresentation.View view = CanonicalSpecimenPresentation.present(specimen(), 4);
        assertThrows(UnsupportedOperationException.class, () -> view.traits().clear());
        assertThrows(UnsupportedOperationException.class, () -> CanonicalSpecimenPresentation.unavailableTraits().clear());
    }

    private static SpecimenData specimen() {
        return new SpecimenData(
                "tide:trout",
                2,
                2,
                0x5EEDL,
                93.0,
                110.0,
                124.0,
                96.4,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.PARASITE_RIDDEN,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(801.2),
                OptionalInt.of(2471),
                new SpecimenData.Provenance("test", "presentation-test", Map.of())
        );
    }
}
