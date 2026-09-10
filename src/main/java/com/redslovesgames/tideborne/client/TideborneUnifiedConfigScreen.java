/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.client;

import net.minecraft.client.gui.screen.Screen;

/** @deprecated Use {@link TideborneConfigScreen}. */
@Deprecated(forRemoval = false)
public final class TideborneUnifiedConfigScreen {
   private TideborneUnifiedConfigScreen() {
   }

   public static Screen create(Screen parent) {
      return TideborneConfigScreen.create(parent);
   }
}
