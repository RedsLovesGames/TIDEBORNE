package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CanonicalPresentationConsumerSourceTest {
    private static final Path SATCHEL = Path.of(
            "src/main/java/com/redslovesgames/tidetraits/client/gui/satchel/AnglersSatchelScreen.java");
    private static final Path FISH_PROFILE = Path.of(
            "src/main/java/com/redslovesgames/tidetraits/mixin/client/FishProfileSizeRangeMixin.java");

    @Test
    void satchelRecordPresentationUsesCanonicalFormatting() throws IOException {
        String source = Files.readString(SATCHEL);

        assertTrue(source.contains("CanonicalSpecimenPresentation.length("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.UNAVAILABLE"));
        assertFalse(source.contains("FishingUiFormat."));
    }

    @Test
    void fishProfileOverlayUsesCanonicalFormatting() throws IOException {
        String source = Files.readString(FISH_PROFILE);

        assertTrue(source.contains("CanonicalSpecimenPresentation.length("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.fishScore("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.UNAVAILABLE"));
        assertFalse(source.contains("FishingUiFormat."));
    }
}
