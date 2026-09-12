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

    @Test
    void fittingScaleNeverEnlargesNormalOrWideScreens() {
        assertEquals(1.0F, FishingUiLayout.fitScale(1920, 1080, 400, 260, 6), 0.0001F);
        assertEquals(1.0F, FishingUiLayout.fitScale(900, 600, 400, 260, 6), 0.0001F);
    }

    @Test
    void fittingScaleUsesTheTighterWindowAxis() {
        assertEquals(0.77F, FishingUiLayout.fitScale(320, 240, 400, 260, 6), 0.0001F);
        assertEquals(228.0F / 260.0F, FishingUiLayout.fitScale(500, 240, 400, 260, 6), 0.0001F);
    }

    @Test
    void inverseCenteredCoordinateMatchesCenteredScaling() {
        float scale = FishingUiLayout.fitScale(320, 240, 400, 260, 6);
        assertEquals(160.0, FishingUiLayout.inverseCenteredCoordinate(160.0, 320, scale), 0.0001);
        assertEquals(341.8182, FishingUiLayout.inverseCenteredCoordinate(300.0, 320, scale), 0.001);
    }
}
