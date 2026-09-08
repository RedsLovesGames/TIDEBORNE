package com.redslovesgames.tideborne.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FishingInspectPresentationSourceTest {
    @Test
    void operatorInspectionUsesCanonicalSpecimenPresentation() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/redslovesgames/tideborne/command/FishingInspectCommand.java"));

        assertTrue(source.contains("CanonicalSpecimenPresentation.present("));
        assertTrue(source.contains("presentation.percentile()"));
        assertTrue(source.contains("presentation.length()"));
        assertTrue(source.contains("presentation.rarityStars()"));
        assertTrue(source.contains("presentation.fishScore()"));
        assertTrue(source.contains("presentation.trait(TraitAxis.BODY_TYPE).value()"));
        assertTrue(source.contains("presentation.trait(TraitAxis.CONDITION).value()"));
        assertTrue(source.contains("presentation.trait(TraitAxis.PIGMENTATION).value()"));
        assertTrue(source.contains("presentation.trait(TraitAxis.QUALITY).value()"));
        assertFalse(source.contains("\"Body Type: \" + specimen.bodyType()"));
        assertFalse(source.contains("specimen.fishScore().getAsInt()"));
    }
}
