package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
            "src/main/java/com/redslovesgames/tidetraits/mixin/client/TeamStatsPercentileMixin.java"
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

    private static int occurrences(String source, String needle) {
        int count = 0;
        for (int index = source.indexOf(needle); index >= 0; index = source.indexOf(needle, index + needle.length())) {
            count++;
        }
        return count;
    }
}
