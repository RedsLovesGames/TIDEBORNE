/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.mixin.client;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Selectable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
   /*
    * These accessors intentionally use the stable 1.21.1 intermediary names.
    * The reconstructed named targets were not being emitted into the production
    * refmap, leaving the shipped JAR looking for literal named methods on the
    * intermediary Screen class at runtime.
    */
   @Invoker(value = "method_37063", remap = false)
   <T extends Element & Drawable & Selectable> T tideTeamJournal$addRenderableWidget(T var1);

   @Invoker(value = "method_25429", remap = false)
   <T extends Element & Selectable> T tideTeamJournal$addWidget(T var1);
}
