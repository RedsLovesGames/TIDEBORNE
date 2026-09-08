/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.mixin;

import com.li64.tide.data.minigame.FishCatchMinigame;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideboundcompatibility.fishing.FishingModifiers;
import java.util.HashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishCatchMinigame.class)
abstract class FishCatchMinigameMixin {
   @Shadow(remap = false)
   @Final
   private TideFishingHook hook;

   @ModifyArgs(
      method = "<init>",
      at = @At(value = "INVOKE", target = "Lcom/li64/tide/network/messages/MinigameClientMsg;<init>(BBBFF)V", remap = false),
      remap = false
   )
   private void tidebound$applyLineAndBaitBehavior(Args args) {
      FishingModifiers.MinigameValues values = FishingModifiers.modifyMinigame(this.hook, (Byte)args.get(2), (Float)args.get(3), (Float)args.get(4));
      args.set(2, values.behavior());
      args.set(3, values.area());
      args.set(4, values.speed());
   }

   @Inject(method = "<init>", at = @At("RETURN"), remap = false)
   private void tidebound$markStruggleStart(CallbackInfo callback) {
      this.hook.setMinigameStartTime(this.hook.getWorld().getTime());
   }

   /**
    * Tide normally records a short server-side delay after every minigame finishes. The rod checks
    * that delay before allowing another cast, even though the successful catch has already been
    * retrieved and the hook discarded. Tideborne intentionally removes that post-catch lockout so a
    * completed catch can be followed by a new cast immediately.
    */
   @Redirect(
      method = "onFinish",
      at = @At(
         value = "INVOKE",
         target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
      ),
      remap = false
   )
   private Object tidebound$skipPostCatchDelay(HashMap<?, ?> delays, Object player, Object expiresAt) {
      return null;
   }
}
