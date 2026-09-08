package com.redslovesgames.tideborne.client.ui;

import java.util.function.ToIntFunction;

/** Pure width and alignment rules shared by the book-style Fishing System 2.0 screens. */
public final class FishingUiLayout {
    private static final String ELLIPSIS = "…";

    private FishingUiLayout() {
    }

    public static FittedText ellipsize(String value, int maximumWidth, ToIntFunction<String> width) {
        String text = value == null ? "" : value;
        if (maximumWidth <= 0) {
            return new FittedText("", !text.isEmpty());
        }
        if (width.applyAsInt(text) <= maximumWidth) {
            return new FittedText(text, false);
        }
        if (width.applyAsInt(ELLIPSIS) > maximumWidth) {
            return new FittedText("", true);
        }

        int low = 0;
        int high = text.length();
        while (low < high) {
            int middle = (low + high + 1) >>> 1;
            if (width.applyAsInt(text.substring(0, middle) + ELLIPSIS) <= maximumWidth) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return new FittedText(text.substring(0, low) + ELLIPSIS, true);
    }

    public static int rightAlignedX(int rightEdge, int textWidth) {
        return rightEdge - Math.max(0, textWidth);
    }

    public record FittedText(String text, boolean clipped) {
    }
}
