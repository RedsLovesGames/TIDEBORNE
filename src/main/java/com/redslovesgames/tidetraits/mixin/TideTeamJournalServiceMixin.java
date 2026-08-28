/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com/redslovesgames/tideteamjournal/TeamJournalService", remap = false)
public abstract class TideTeamJournalServiceMixin {
   @Unique
   private static final ThreadLocal<Boolean> TIDE_TRAITS$NATIVE_FALLBACK = ThreadLocal.withInitial(() -> false);

   @Inject(method = "loadFor", at = @At("HEAD"), require = 0, remap = false)
   private static void tideTraits$beginLoad(ServerPlayerEntity player, CallbackInfoReturnable<TidePlayerData> cir) {
      PersonalTideJournal.markExtrasTrackerActive();
      TIDE_TRAITS$NATIVE_FALLBACK.set(false);
   }

   @Inject(method = "loadNative", at = @At("RETURN"), require = 0, remap = false)
   private static void tideTraits$markNativeFallback(ServerPlayerEntity player, CallbackInfoReturnable<TidePlayerData> cir) {
      TIDE_TRAITS$NATIVE_FALLBACK.set(true);
   }

   @Inject(method = "loadFor", at = @At("RETURN"), require = 0, remap = false)
   private static void tideTraits$finishLoad(ServerPlayerEntity player, CallbackInfoReturnable<TidePlayerData> cir) {
      try {
         if (!TIDE_TRAITS$NATIVE_FALLBACK.get()) {
            PersonalTideJournal.markExtrasTeamData((TidePlayerData)cir.getReturnValue());
         }
      } finally {
         TIDE_TRAITS$NATIVE_FALLBACK.remove();
      }
   }
}
