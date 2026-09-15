/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.journal.client;

import com.li64.tide.client.gui.screens.journal.FishingJournal;
import com.redslovesgames.tideborne.journal.client.ClientConfig;
import com.redslovesgames.tideborne.journal.client.TeamRecordsScreen;
import com.redslovesgames.tideborne.journal.client.TideJournalButton;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingJournal.class)
abstract class FishingJournalMixin {
   @Unique
   private static final int TEAM_RECORDS_BUTTON_Y = 240;
   @Unique
   private static final int TEAM_RECORDS_BUTTON_HEIGHT = 16;
   @Unique
   private ButtonWidget tideTeamJournal$teamRecordsButton;

   @Inject(method = "init", at = @At("TAIL"))
   private void tideTeamJournal$addTeamRecordsButton(CallbackInfo callbackInfo) {
      this.tideTeamJournal$teamRecordsButton = null;
      if (ClientConfig.get().debugTeamRecordsButton) {
         Screen screen = (Screen)(Object)this;
         int bookLeft = (screen.width - 400) / 2;
         int bookTop = (screen.height - 260) / 2;
         this.tideTeamJournal$teamRecordsButton = new TideJournalButton(
            bookLeft + 286,
            bookTop + TEAM_RECORDS_BUTTON_Y,
            88,
            TEAM_RECORDS_BUTTON_HEIGHT,
            Text.translatable("screen.tideborne.team_journal.open"),
            button -> TeamRecordsScreen.open(screen)
         );
         ((ScreenAccessor)this).tideTeamJournal$addWidget(this.tideTeamJournal$teamRecordsButton);
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void tideTeamJournal$renderTeamRecordsButton(DrawContext graphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
      if (this.tideTeamJournal$teamRecordsButton != null) {
         this.tideTeamJournal$teamRecordsButton.render(graphics, mouseX, mouseY, partialTick);
      }
   }
}
