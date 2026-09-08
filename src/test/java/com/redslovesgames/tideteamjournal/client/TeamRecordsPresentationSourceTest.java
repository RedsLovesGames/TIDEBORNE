package com.redslovesgames.tideteamjournal.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TeamRecordsPresentationSourceTest {
    private static final Path TEAM_RECORDS = Path.of(
            "src/main/java/com/redslovesgames/tideteamjournal/client/TeamRecordsScreen.java");

    @Test
    void compactRecordRowsUseCanonicalSpecimenPresentation() throws IOException {
        String source = Files.readString(TEAM_RECORDS);

        assertTrue(source.contains("CanonicalSpecimenPresentation.percentile("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.length("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.fishScore("));
        assertTrue(source.contains("CanonicalSpecimenPresentation.unavailableTraits()"));
        assertTrue(source.contains("CanonicalSpecimenPresentation.traitForAxis("));
        assertTrue(source.contains("body.shortLabel()"));
        assertTrue(source.contains("condition.shortLabel()"));
        assertTrue(source.contains("pigmentation.shortLabel()"));
        assertTrue(source.contains("quality.shortLabel()"));

        assertFalse(source.contains("FishingUiFormat.percentile("));
        assertFalse(source.contains("FishingUiFormat.length("));
        assertFalse(source.contains("FishingUiFormat.fishScore("));
        assertFalse(source.contains("\"  Body \""));
        assertFalse(source.contains("\"  Condition \""));
        assertFalse(source.contains("\"  Pigment: \""));
        assertFalse(source.contains("\"  Quality: \""));
    }

    @Test
    void fishingUiFormatIsUsedOnlyForClientSpecificTimestampFormattingHere() throws IOException {
        String source = Files.readString(TEAM_RECORDS);
        assertTrue(source.contains("FishingUiFormat.timestamp("));
        assertFalse(source.contains("FishingUiFormat.UNAVAILABLE"));
        assertFalse(source.contains("FishingUiFormat.trait("));
    }
}
