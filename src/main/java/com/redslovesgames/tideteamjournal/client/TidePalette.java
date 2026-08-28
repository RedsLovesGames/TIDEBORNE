/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

public final class TidePalette {
   public static final int INK = 5477982;
   public static final int MUTED_INK = 6650722;
   public static final int PARCHMENT = 15392450;
   public static final int PARCHMENT_DARK = 14796701;
   public static final int PAPER_RULE = 14136724;
   public static final int NAVY = 3496824;
   public static final int DISCOVERY = 5207921;
   public static final int LARGEST = 10121284;
   public static final int SMALLEST = 7757682;
   public static final int REPAIR = 7956829;
   public static final int MOSS = 6847056;
   public static final int CHAT_TEXT = 14074789;
   public static final int CHAT_NAME = 9415088;
   public static final int CHAT_TEAL = 8628900;
   public static final int CHAT_MOSS = 10201722;

   private TidePalette() {
   }

   public static int calmAccent(int rgb) {
      int base = 8416092;
      int red = ((rgb >> 16 & 0xFF) * 45 + (base >> 16 & 0xFF) * 55) / 100;
      int green = ((rgb >> 8 & 0xFF) * 45 + (base >> 8 & 0xFF) * 55) / 100;
      int blue = ((rgb & 0xFF) * 45 + (base & 0xFF) * 55) / 100;
      return red << 16 | green << 8 | blue;
   }
}
