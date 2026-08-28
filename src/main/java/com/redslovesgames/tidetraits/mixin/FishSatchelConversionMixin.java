/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.li64.tide.registries.items.FishSatchelItem;
import com.redslovesgames.tidetraits.config.TideTraitsConfigManager;
import com.redslovesgames.tidetraits.satchel.SatchelPurchaseResult;
import com.redslovesgames.tidetraits.satchel.SatchelPurchaseService;
import java.util.function.Consumer;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishSatchelItem.class)
public abstract class FishSatchelConversionMixin {
   @Inject(method = "use", at = @At("HEAD"), cancellable = true)
   private void tideTraits$convertOnSneakUse(World level, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
      if (player.isSneaking()) {
         ItemStack held = player.getStackInHand(hand);
         if (level.isClient) {
            cir.setReturnValue(TypedActionResult.success(held, true));
         } else if (player instanceof ServerPlayerEntity serverPlayer) {
            int cost = TideTraitsConfigManager.current().satchel().conversionXpCost();
            SatchelPurchaseResult result = SatchelPurchaseService.convertHeldTideSatchel(serverPlayer, hand, cost);

            Text message = switch (result.status()) {
               case SUCCESS -> Text.translatable("message.tide_traits.converted", new Object[]{cost}).formatted(Formatting.AQUA);
               case INSUFFICIENT_XP -> Text.translatable("message.tide_traits.insufficient_xp", new Object[]{cost}).formatted(Formatting.RED);
               default -> Text.translatable("message.tide_traits.conversion_failed").formatted(Formatting.RED);
            };
            serverPlayer.sendMessage(message, true);
            cir.setReturnValue(TypedActionResult.success(serverPlayer.getStackInHand(hand), false));
         }
      }
   }

   @Inject(method = "addTooltip", at = @At("TAIL"))
   private void tideTraits$addConversionHint(ItemStack stack, Consumer<Text> tooltip, CallbackInfo ci) {
      tooltip.accept(Text.translatable("tooltip.tide_traits.convert_hint").formatted(Formatting.DARK_AQUA));
   }
}
