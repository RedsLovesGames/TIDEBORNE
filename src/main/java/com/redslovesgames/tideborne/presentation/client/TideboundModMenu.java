/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import com.redslovesgames.tideborne.client.TideborneConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** @deprecated Historical adapter retained for binary/source compatibility. */
@Deprecated(forRemoval = false)
public final class TideboundModMenu implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return TideborneConfigScreen::create;
   }
}
