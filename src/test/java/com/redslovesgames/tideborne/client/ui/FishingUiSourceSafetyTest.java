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
            "src/main/java/com/redslovesgames/tideborne/journal/client/TeamRecordsScreen.java",
            "src/main/java/com/redslovesgames/tideborne/journal/client/TopFishScreen.java",
            "src/main/java/com/redslovesgames/tideborne/satchel/client/AnglersSatchelScreen.java",
            "src/main/java/com/redslovesgames/tideborne/mixin/specimen/client/FishProfileSizeRangeMixin.java",
            "src/main/java/com/redslovesgames/tideborne/journal/client/TeamStatsComponent.java",
            "src/main/java/com/redslovesgames/tideborne/journal/client/DiscoveryBadgesComponent.java",
            "src/main/java/com/redslovesgames/tideborne/mixin/journal/client/FishingJournalMixin.java",
            "src/main/java/com/redslovesgames/tideborne/mixin/specimen/client/TideFishProfileMixin.java",
            "src/main/java/com/redslovesgames/tideborne/mixin/specimen/client/ItemRendererMutationTintMixin.java",
            "src/main/java/com/redslovesgames/tideborne/presentation/render/MutationRendering.java"
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
        assertTrue(stats.contains("BEST_SECTION_HEIGHT = 43"));
        assertTrue(stats.contains("graphics.fill(x + 4, y + cursorY"));
        assertTrue(stats.contains("\"Best Specimen\""));
        assertTrue(stats.contains("\"PC \""));

        String mixins = Files.readString(Path.of("src/main/resources/tideborne.client.mixins.json"));
        assertFalse(mixins.contains("TeamStatsPercentileMixin"));

        String journal = Files.readString(Path.of(AFFECTED_UI.get(6)));
        int buttonY = constantValue(journal, "TEAM_RECORDS_BUTTON_Y");
        int buttonHeight = constantValue(journal, "TEAM_RECORDS_BUTTON_HEIGHT");
        assertTrue(buttonY + buttonHeight <= 260);
        assertTrue(buttonY >= 238);
    }

    @Test
    void firstCatchSharesTheSizeBadgeRowAndUsesCompactScale() throws IOException {
        String badges = Files.readString(Path.of(AFFECTED_UI.get(5)));
        assertTrue(badges.contains("FIRST_CATCH_SCALE = 0.75F"));
        assertTrue(badges.contains("\"FC \""));
        assertTrue(badges.contains("drawScaledRight"));
        assertTrue(badges.contains("sizeY + 2"));

        String profile = Files.readString(Path.of(AFFECTED_UI.get(7)));
        assertTrue(profile.contains("new DiscoveryBadgesComponent(speciesId, stats)"));
    }

    @Test
    void bestSpecimenUsesCanonicalTraitPresentation() throws IOException {
        String stats = Files.readString(Path.of(AFFECTED_UI.get(4)));
        assertTrue(stats.contains("CanonicalSpecimenPresentation.traits("));
        assertTrue(stats.contains("TraitDisplay"));
        assertTrue(stats.contains("trait.shortLabel()"));
        assertTrue(stats.contains("trait.label()"));
        assertTrue(stats.contains("trait.color()"));
        assertFalse(stats.contains("FishingUiFormat.trait("));
        assertFalse(stats.contains("tideTraits$traitColor"));
        assertFalse(stats.contains("\"Body \""));
        assertFalse(stats.contains("\"Cond \""));
        assertFalse(stats.contains("\"Pig \""));
        assertFalse(stats.contains("\"Qual \""));
    }

    @Test
    void topFishUsesCanonicalTraitPresentation() throws IOException {
        String topFish = Files.readString(Path.of(AFFECTED_UI.get(1)));
        assertTrue(topFish.contains("List<TraitDisplay> traits"));
        assertTrue(topFish.contains("CanonicalSpecimenPresentation.unavailableTraits()"));
        assertTrue(topFish.contains("display.traits()"));
        assertTrue(topFish.contains("trait.label()"));
        assertTrue(topFish.contains("trait.value()"));
        assertTrue(topFish.contains("trait.color()"));
        assertFalse(topFish.contains("CanonicalSpecimenPresentation.BODY_TYPE_COLOR"));
        assertFalse(topFish.contains("CanonicalSpecimenPresentation.CONDITION_COLOR"));
        assertFalse(topFish.contains("CanonicalSpecimenPresentation.PIGMENTATION_COLOR"));
        assertFalse(topFish.contains("CanonicalSpecimenPresentation.QUALITY_COLOR"));
        assertFalse(topFish.contains("\"Body Type\""));
        assertFalse(topFish.contains("\"Pigmentation\""));
    }

    @Test
    void topFishUsesBoundedFifteenSlotTable() throws IOException {
        String topFish = Files.readString(Path.of(AFFECTED_UI.get(1)));
        int slots = constantValue(topFish, "TOP_FISH_SLOTS");
        int panelRight = constantValue(topFish, "LIST_PANEL_RIGHT");
        int panelBottom = constantValue(topFish, "LIST_PANEL_BOTTOM");
        int rowRight = constantValue(topFish, "LIST_ROW_RIGHT");
        int scoreRight = constantValue(topFish, "SCORE_RIGHT_X");

        assertEquals(15, slots);
        assertTrue(panelRight <= 196);
        assertTrue(panelBottom <= 240);
        assertTrue(rowRight <= panelRight);
        assertTrue(scoreRight <= rowRight);
        assertTrue(topFish.contains("for (int row = 0; row < TOP_FISH_SLOTS; row++)"));
        assertTrue(topFish.contains("if (row >= visibleCount)"));
        assertTrue(topFish.contains("Text.literal(\"—\")"));
        assertTrue(topFish.contains("graphics.enableScissor("));
        assertTrue(topFish.contains("graphics.disableScissor();"));
    }

    @Test
    void canonicalPigmentationFiltersReachItemAndThreeDimensionalPreviews() throws IOException {
        String itemRenderer = Files.readString(Path.of(AFFECTED_UI.get(8)));
        assertTrue(itemRenderer.contains("SPECIMEN_PIGMENTATION"));
        assertTrue(itemRenderer.contains("FishMutation.ALBINO"));
        assertTrue(itemRenderer.contains("FishMutation.IRIDESCENT"));
        assertTrue(itemRenderer.contains("MutationRendering.textureForItem"));
        assertTrue(itemRenderer.contains("getParticleSprite()"));

        String mutationRendering = Files.readString(Path.of(AFFECTED_UI.get(9)));
        assertTrue(mutationRendering.contains("textureForItem(Identifier original, ItemStack stack)"));
        assertTrue(mutationRendering.contains("SPECIMEN_DETERMINISTIC_SEED"));
        assertTrue(mutationRendering.contains("SPECIMEN_PIGMENTATION"));

        String topFish = Files.readString(Path.of(AFFECTED_UI.get(1)));
        assertTrue(occurrences(topFish, "CanonicalSpecimenStorage.restoreTransferData(") == 1);
        assertTrue(topFish.contains("SpecimenTransfer.stackToEntity(stack, previewEntity);"));

        String fishDisplay = Files.readString(Path.of(
                "src/main/java/com/redslovesgames/tideborne/mixin/specimen/client/FishDisplayBlockEntityMixin.java"));
        assertTrue(fishDisplay.contains("SpecimenTransfer.stackToEntity(displayStack, renderedEntity);"));
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
