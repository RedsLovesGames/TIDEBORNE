package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SatchelPresentationSourceTest {
    private static final Path SCREEN = Path.of(
            "src/main/java/com/redslovesgames/tideborne/satchel/client/AnglersSatchelScreen.java");

    @Test
    void footerActionsShareRenderedClickAndTooltipCoordinates() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("FOOTER_ACTIVE_X = 136"));
        assertTrue(source.contains("FOOTER_REFRESH_X = 214"));
        assertTrue(source.contains("FOOTER_BUTTON_Y = 223"));
        assertTrue(source.contains("left + FOOTER_ACTIVE_X, top + FOOTER_BUTTON_Y"));
        assertTrue(source.contains("left + FOOTER_REFRESH_X, top + FOOTER_BUTTON_Y"));
    }

    @Test
    void actionAndServerFeedbackIsRenderedInsideTheBook() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("renderStatus(graphics, left, top, logicalMouseX, logicalMouseY)"));
        assertTrue(source.contains("FishingUiLayout.ellipsize(this.localStatus, STATUS_MAX_WIDTH"));
        assertTrue(source.contains("responseStatus(updated)"));
        assertTrue(source.contains("STATUS_Y = 242"));
    }

    @Test
    void unavailableOrganizerIsVisuallyAndInteractivelyGated() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("private boolean sortingAvailable()"));
        assertTrue(source.contains("Tackle Organizer locked or disabled"));
        assertTrue(source.contains("Unlock and enable Tackle Organizer first"));
        assertTrue(source.contains("this.selectTab(AnglersSatchelScreen.Tab.UPGRADES)"));
    }

    @Test
    void recordsHaveBoundedNamesAndScrollPresentation() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("RECORD_VISIBLE = 16"));
        assertTrue(source.contains("FishingUiLayout.ellipsize(fullName, RECORD_NAME_WIDTH"));
        assertTrue(source.contains("renderRecordScrollbar(graphics, left, top)"));
        assertTrue(source.contains("Showing \" + first + \"-\" + last + \" of \" + this.contents.size()"));
    }

    @Test
    void narrowWindowsScaleTheWholeBookAndInvertPointerCoordinates() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("SCREEN_MARGIN = 6"));
        assertTrue(source.contains("FishingUiLayout.fitScale(this.width, this.height, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, SCREEN_MARGIN)"));
        assertTrue(source.contains("graphics.getMatrices().scale(scale, scale, 1.0F)"));
        assertTrue(source.contains("FishingUiLayout.inverseCenteredCoordinate(mouseX, this.width, scale)"));
        assertTrue(source.contains("inside(logicalMouseX, logicalMouseY"));
    }

    @Test
    void satchelHasReadableTitleAndKeyboardNavigation() throws IOException {
        String source = Files.readString(SCREEN);
        assertTrue(source.contains("super(Text.literal(\"Angler's Satchel\"))"));
        assertTrue(source.contains("GLFW.GLFW_KEY_TAB"));
        assertTrue(source.contains("GLFW.GLFW_KEY_1"));
        assertTrue(source.contains("handleContentsKey(keyCode)"));
        assertTrue(source.contains("handleRecordsKey(keyCode)"));
        assertTrue(source.contains("GLFW.GLFW_KEY_PAGE_DOWN"));
    }
}
