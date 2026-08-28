/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.TidePlayerData;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.compat.multiplayer.PersonalTideJournal;
import com.redslovesgames.tidetraits.satchel.AnglersSatchelStorage;
import com.redslovesgames.tidetraits.satchel.SatchelFeature;
import com.redslovesgames.tidetraits.satchel.SatchelService;
import com.redslovesgames.tidetraits.satchel.SatchelState;
import java.util.Locale;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TidePlayerData.class, priority = 900)
public abstract class TidePlayerDataMixin {
   @Unique
   private static final ThreadLocal<PersonalTideJournal.CatchContext> TIDE_TRAITS$CATCH_CONTEXT = new ThreadLocal<>();

   @Inject(method = "logCatch", at = @At("HEAD"))
   private void tideTraits$finalizeBeforeNativeLog(ItemStack stack, ServerPlayerEntity player, World level, CallbackInfo ci) {
      CatchTraitService.INSTANCE.ensureAssignedBeforeLog(stack, player.getRandom());
      TidePlayerData self = (TidePlayerData)this;
      TIDE_TRAITS$CATCH_CONTEXT.set(PersonalTideJournal.prepareCatch(self, stack, player));
   }

   @Inject(method = "logCatch", at = @At("TAIL"))
   private void tideTraits$recordDiscovery(ItemStack stack, ServerPlayerEntity player, World level, CallbackInfo ci) {
      try {
         PersonalTideJournal.RecordBefore recordBefore = PersonalTideJournal.completeCatch(TIDE_TRAITS$CATCH_CONTEXT.get(), stack, player, level);
         CatchTraitService.INSTANCE.recordSuccessfulCatch(stack, player);
         tideTraits$announceRecords(recordBefore, stack, player);
      } finally {
         TIDE_TRAITS$CATCH_CONTEXT.remove();
      }
   }

   @Unique
   private static void tideTraits$announceRecords(PersonalTideJournal.RecordBefore before, ItemStack caught, ServerPlayerEntity player) {
      if (!SatchelService.findActive(player).filter(stack -> {
         SatchelState state = AnglersSatchelStorage.state(stack);
         return state.isFeatureUnlocked(SatchelFeature.RECORD_KEEPER) && state.isFeatureEnabled(SatchelFeature.RECORD_KEEPER);
      }).isEmpty()) {
         FishData fish = (FishData)FishData.get(caught).orElse(null);
         if (before != null && before.available() && fish != null && TideItemData.FISH_LENGTH.isPresent(caught)) {
            double length = (Double)TideItemData.FISH_LENGTH.getOrDefault(caught, 0.0);
            if (Double.isFinite(length) && !(length <= 0.0)) {
               String formatted = String.format(Locale.ROOT, "%.1f", length);
               if (!before.hadStats() || length > before.largest() + tolerance(before.largest())) {
                  player.sendMessage(
                     Text.translatable("message.tide_traits.new_record_largest", new Object[]{caught.getName(), formatted})
                        .formatted(Formatting.AQUA),
                     true
                  );
               }

               if (!before.hadStats() || length < before.smallest() - tolerance(before.smallest())) {
                  player.sendMessage(
                     Text.translatable("message.tide_traits.new_record_smallest", new Object[]{caught.getName(), formatted})
                        .formatted(Formatting.AQUA),
                     true
                  );
               }
            }
         }
      }
   }

   @Unique
   private static double tolerance(double value) {
      return Double.isFinite(value) ? Math.max(1.0E-6, Math.ulp(value) * 4.0) : 0.0;
   }
}
