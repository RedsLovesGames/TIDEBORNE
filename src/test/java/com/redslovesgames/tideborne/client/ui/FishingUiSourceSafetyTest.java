package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class FishingUiSourceSafetyTest {
    private static final List<String> AFFECTED_UI = List.of(
            "src/main/java/com/redslovesgames/tideteamjournal/client/TeamRecordsScreen.java",
            "src/main/java/com/redslovesgames/tideteamjournal/client/TopFishScreen.java",
            "src/main/java/com/redslovesgames/tidetraits/client/gui/satchel/AnglersSatchelScreen.java",
            "src/main/java/com/redslovesgames/tidetraits/mixin/client/FishProfileSizeRangeMixin.java",
            "src/main/java/com/redslovesgames/tidetraits/mixin/client/TeamStatsPercentileMixin.java",
            "src/main/java/com/redslovesgames/tidetraits/client/gui/journal/DiscoveryBadgesComponent.java",
            "src/main/java/com/redslovesgames/tideteamjournal/mixin/client/FishingJournalMixin.java"
    );

    @Test
    void affectedUiContainsNoScoreFormulaOrSpecimenGeneration() throws IOException {
        for (String file : AFFECTED_UI) {
            String source = Files.readString(Path.of(file));
            assertFalse(source.contains("FishScoreV2Service"), file);
            assertFalse(source.contains("SpecimenGenerator"), file);
            assertFalse(source.contains("tideborneFishScoreFromParts"), file);
            assertFalse(source.contains("TraitAxesRuntime.scoreFromParts"), file);
        }
    }

    @Test
    void summaryAndHistoryReuseOneStructuredEventRowRenderer() throws IOException {
        String source = Files.readString(Path.of(AFFECTED_UI.get(0)));
        assertEquals(3, occurrences(source, "renderEventRow("));
    }

    @Test
    void discoveryBadgesAreGroupedByCanonicalCategories() throws IOException {
        String source = Files.readString(Path.of(AFFECTED_UI.get(5)));
        assertTrue(source.contains("\"Body Type\""));
        assertTrue(source.contains("\"Condition\""));
        assertTrue(source.contains("\"Pigmentation\""));
        assertTrue(source.contains("\"Quality\""));
        assertTrue(source.contains("\"Size\""));
        assertFalse(source.contains("MUTATIONS ="));
    }

    @Test
    void speciesFallbackDoesNotDuplicateCanonicalSpecimenBlock() throws IOException {
        String source = Files.readString(Path.of(AFFECTED_UI.get(3)));
        assertTrue(source.contains("JournalSpecimenStore.LATEST"));
        assertTrue(source.contains("No canonical specimen recorded"));
    }

    @Test
    void journalStatsStayCompactAndFooterButtonStaysInsideBook() throws IOException {
        String stats = Files.readString(Path.of(AFFECTED_UI.get(4)));
        assertTrue(stats.contains("BASE_LINE_STEP = 9"));
        assertTrue(stats.contains("BEST_SECTION_HEIGHT = 38"));
        assertTrue(stats.contains("index = smallestIndex;"));
        assertTrue(stats.contains("Best Specimen  •  PC "));

        String journal = Files.readString(Path.of(AFFECTED_UI.get(6)));
        int buttonY = constantValue(journal, "TEAM_RECORDS_BUTTON_Y");
        int buttonHeight = constantValue(journal, "TEAM_RECORDS_BUTTON_HEIGHT");
        assertTrue(buttonY + buttonHeight <= 260);
        assertTrue(buttonY >= 238);
    }

    private static int constantValue(String source, String name) {
        String marker = name + " = ";
        int start = source.indexOf(marker);
        assertTrue(start >= 0, name);
        start += marker.length();
        int end = source.indexOf(';', start);
        assertTrue(end > start, name);
        return Integer.parseInt(source.substring(start, end).trim());
    }

    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = source.indexOf(needle); index >= 0; index = source.indexOf(needle, index + needle.length())) {
            count++;
        }
        return count;
    }
}
