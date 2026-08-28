/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.redslovesgames.tideteamjournal.TeamProgressStore;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.item.Item.TooltipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMutationTooltipMixin {
   @Inject(
      method = "getTooltip(Lnet/minecraft/item/Item$TooltipContext;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/tooltip/TooltipType;)Ljava/util/List;",
      at = @At("RETURN"),
      cancellable = true,
      require = 1
   )
   private void tideTraits$appendMutationTooltip(TooltipContext context, PlayerEntity player, TooltipType flag, CallbackInfoReturnable<List<Text>> callback) {
      ItemStack var5 = (ItemStack)this;
      double var6 = TeamProgressStore.tideborneFishScore(var5);
      if (var6 >= 0.0) {
         ArrayList var8 = new ArrayList((Collection)callback.getReturnValue());
         int var9 = var8.size();

         for (int var10 = 0; var10 < var8.size(); var10++) {
            if (((Text)var8.get(var10)).getString().startsWith("Rarity:")) {
               var9 = var10 + 1;
               break;
            }
         }

         Text var11 = TeamProgressStore.tideborneFishScoreTooltip(var5);
         if (var11 != null) {
            var8.add(var9, var11);
            var9++;
         }

         String var14 = TraitAxesRuntime.bodyType(var5);
         if (!"normal".equals(var14)) {
            MutableText var15 = Text.literal("Body Type: ")
               .append(Text.literal(TraitAxesRuntime.titleCase(var14)).formatted(Formatting.LIGHT_PURPLE));
            var8.add(var9, var15);
            var9++;
         }

         String var12 = TraitAxesRuntime.condition(var5);
         if (!var12.isBlank() && !"normal".equals(var12)) {
            MutableText var13 = Text.literal("Condition: ")
               .append(Text.literal(titleCase(var12)).formatted(Formatting.LIGHT_PURPLE));
            var8.add(var9, var13);
         }

         callback.setReturnValue(List.copyOf(var8));
      }
   }

   private static String titleCase(String id) {
      StringBuilder output = new StringBuilder();

      for (String part : id.toLowerCase(Locale.ROOT).replace('-', '_').split("_")) {
         if (!part.isBlank()) {
            if (!output.isEmpty()) {
               output.append(' ');
            }

            output.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
         }
      }

      return output.toString();
   }
}
