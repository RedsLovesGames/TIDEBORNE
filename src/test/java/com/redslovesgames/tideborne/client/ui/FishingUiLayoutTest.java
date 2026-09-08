package com.redslovesgames.tideborne.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FishingUiLayoutTest {
    @Test
    void ellipsisNeverExceedsTheRequestedWidth() {
        FishingUiLayout.FittedText fitted = FishingUiLayout.ellipsize("Stonefish", 6, String::length);
        assertEquals("Stone…", fitted.text());
        assertTrue(fitted.clipped());
        assertTrue(fitted.text().length() <= 6);
    }

    @Test
    void shortTextRemainsUntouched() {
        FishingUiLayout.FittedText fitted = FishingUiLayout.ellipsize("Cod", 6, String::length);
        assertEquals("Cod", fitted.text());
        assertFalse(fitted.clipped());
    }

    @Test
    void rightAlignedTextEndsAtThePanelEdge() {
        assertEquals(82, FishingUiLayout.rightAlignedX(100, 18));
    }
}
