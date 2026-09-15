/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.journal.client;

import com.li64.tide.client.gui.screens.journal.FishProfile;
import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.li64.tide.client.gui.screens.journal.components.StatsComponent;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.redslovesgames.tideborne.journal.client.TeamStatsComponent;
import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishProfile.class)
abstract class FishProfileMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void tideTeamJournal$restoreProfileBlendState(
      DrawContext graphics,
      int mouseX,
      int mouseY,
      float partialTick,
      CallbackInfo callbackInfo
   ) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Inject(method = "buildComponentsWithStats", at = @At("RETURN"))
   private static void tideTeamJournal$showRecordHolders(ItemStack fish, FishData data, CallbackInfoReturnable<ArrayList<ProfileComponent>> callback) {
      ArrayList<ProfileComponent> components = (ArrayList<ProfileComponent>)callback.getReturnValue();
      if (!components.isEmpty() && components.getLast() instanceof StatsComponent) {
         components.removeLast();
      }

      FishStats stats = TidePlayerData.CLIENT_DATA.getDataFor(fish.getItem()).flatMap(playerData -> playerData.stats).orElse(new FishStats());
      components.add(new TeamStatsComponent(stats, Registries.ITEM.getId(fish.getItem())));
   }
}
