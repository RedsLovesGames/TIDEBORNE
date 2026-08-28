/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.mixin.client;

import com.li64.tide.network.messages.SyncPlayerDataMsg;
import com.redslovesgames.tideteamjournal.client.ClientRecordHolders;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SyncPlayerDataMsg.class)
abstract class SyncPlayerDataMsgMixin {
   @Inject(method = "handle", at = @At("HEAD"))
   private static void tideTeamJournal$readRecordHolders(SyncPlayerDataMsg message, PlayerEntity player, CallbackInfo callback) {
      ClientRecordHolders.update(message.tag());
   }
}
