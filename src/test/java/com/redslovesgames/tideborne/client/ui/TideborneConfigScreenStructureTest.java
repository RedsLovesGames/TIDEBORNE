package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class TideborneConfigScreenStructureTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/redslovesgames/tideborne/client/TideborneConfigScreen.java");

    @Test
    void settingsUseSixStableRootCategories() throws IOException {
        String source = Files.readString(SCREEN);
        List<String> roots = List.of(
                "Fishing",
                "Satchel",
                "Journal & Teams",
                "Sharks & Ecosystem",
                "Client & HUD",
                "Advanced");

        for (String root : roots) {
            assertTrue(
                    source.contains("builder.getOrCreateCategory(text(\"" + root + "\"))"),
                    "Missing settings root: " + root);
        }
        assertEquals(6, occurrences(source, "builder.getOrCreateCategory("));
    }

    @Test
    void denseSettingsLiveInNestedPresentationGroups() throws IOException {
        String source = Files.readString(SCREEN);
        List<String> groups = List.of(
                "General & Catching",
                "Gear, Lines & Leaders",
                "Bait",
                "Bobbers",
                "Advanced Balance",
                "Behavior",
                "Sorting",
                "Upgrades",
                "Records",
                "Journal",
                "Teams",
                "Shared Discoveries",
                "Sharks",
                "Chum",
                "Shark Catch Loss",
                "Optional Spawning",
                "Rendering",
                "HUD",
                "Tooltips",
                "Accessibility & Presentation",
                "Debug",
                "Specimen Distribution",
                "Diagnostics & Compatibility");

        for (String group : groups) {
            assertTrue(
                    source.contains("section(entries, \"" + group + "\""),
                    "Missing nested settings group: " + group);
        }
    }

    private static int occurrences(String source, String target) {
        int count = 0;
        int cursor = 0;
        while ((cursor = source.indexOf(target, cursor)) >= 0) {
            count++;
            cursor += target.length();
        }
        return count;
    }
}
