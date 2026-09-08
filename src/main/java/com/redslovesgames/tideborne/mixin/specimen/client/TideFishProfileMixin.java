/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.mixin.specimen.client;

import com.li64.tide.client.gui.screens.journal.FishProfile;
import com.li64.tide.client.gui.screens.journal.ProfileComponent;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.journal.client.DiscoveryBadgesComponent;
import com.redslovesgames.tideborne.fishing.specimen.FishDescriptor;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = FishProfile.class, priority = 500)
public abstract class TideFishProfileMixin {
   @Inject(
      method = "buildComponentsWithStats(Lnet/minecraft/item/ItemStack;Lcom/li64/tide/data/fishing/FishData;)Ljava/util/ArrayList;",
      at = @At("RETURN"),
      require = 0
   )
   private static void tideTraits$appendDiscoveryBadges(ItemStack bareJournalStack, FishData fishData, CallbackInfoReturnable<ArrayList<ProfileComponent>> cir) {
      ArrayList<ProfileComponent> components = (ArrayList<ProfileComponent>)cir.getReturnValue();
      if (components != null && fishData != null) {
         try {
            Identifier speciesId = FishDescriptor.fromFishData(fishData).canonicalSpeciesId();
            FishStats stats = TidePlayerData.CLIENT_DATA.getDataFor(bareJournalStack.getItem())
               .flatMap(playerData -> playerData.stats)
               .orElse(new FishStats());
            components.add(Math.max(0, components.size() - 1), new DiscoveryBadgesComponent(speciesId, stats));
         } catch (RuntimeException var6) {
         }
      }
   }
}
