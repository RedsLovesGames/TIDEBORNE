package com.redslovesgames.tideborne.mixin.satchel;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tideborne.discovery.multiplayer.PersonalTideJournal;
import com.redslovesgames.tideborne.satchel.network.SatchelNetworking;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the Satchel open path on Tide's native read-only record data instead of journal backfill. */
@Mixin(value = SatchelNetworking.class, remap = false)
abstract class SatchelNetworkingRecordLookupMixin {
   @SuppressWarnings("PMD.UnusedPrivateMethod")
   @Redirect(
      method = "buildView",
      at = @At(
         value = "INVOKE",
         target = "Lcom/redslovesgames/tideborne/discovery/multiplayer/PersonalTideJournal;load(Lnet/minecraft/server/network/ServerPlayerEntity;)Ljava/util/Optional;"
      )
   )
   private static Optional<TidePlayerData> tideborne$loadNativeRecords(ServerPlayerEntity player) {
      return PersonalTideJournal.loadForRecordLookup(player);
   }
}
