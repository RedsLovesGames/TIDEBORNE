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
   @Invoker("addRenderableWidget")
   <T extends Element & Drawable & Selectable> T tideTeamJournal$addRenderableWidget(T var1);

   @Invoker("addWidget")
   <T extends Element & Selectable> T tideTeamJournal$addWidget(T var1);
}
