package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ItemStackTooltipPresentationSourceTest {
    @Test
    void canonicalFishTooltipDoesNotReconstructSpecimenPresentation() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/redslovesgames/tideborne/mixin/specimen/client/ItemStackMutationTooltipMixin.java"));

        assertTrue(source.contains("TideborneFishingApi.readCurrentSpecimen(stack)"));
        assertTrue(source.contains("CanonicalSpecimenPresentation.present(specimen, rarityStars)"));
        assertTrue(source.contains("for (TraitDisplay trait : presentation.traits())"));
        assertTrue(source.contains("trait.label()"));
        assertTrue(source.contains("trait.value()"));
        assertTrue(source.contains("trait.color()"));
        assertTrue(source.contains("presentation.length()"));
        assertTrue(source.contains("presentation.percentile()"));
        assertTrue(source.contains("presentation.rarityStars()"));
        assertTrue(source.contains("presentation.fishScore()"));
        assertFalse(source.contains("TideTraitsComponents.SPECIMEN_"));
        assertFalse(source.contains("FishingUiFormat."));
        assertFalse(source.contains("traitColor("));
        assertFalse(source.contains("\"★\".repeat("));
    }
}
