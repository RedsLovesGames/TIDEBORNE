/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin;

import com.redslovesgames.tidetraits.satchel.AnglersSatchelRecipeSupport;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.recipe.ShapedRecipe", remap = false)
public abstract class AnglersSatchelRecipeMixin {
   @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
   private void tideborne$validateSatchelRecipe(CraftingRecipeInput var1, World var2, CallbackInfoReturnable<Boolean> var3) {
      if (var3.getReturnValueZ() && AnglersSatchelRecipeSupport.isAnglersSatchelRecipe(this) && !AnglersSatchelRecipeSupport.validThreeStarRing(var1)) {
         var3.setReturnValue(Boolean.FALSE);
      }
   }

   @Inject(method = "matches(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;)Z", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
   private void tideborne$validateSatchelRecipeBridge(RecipeInput var1, World var2, CallbackInfoReturnable<Boolean> var3) {
      if (var3.getReturnValueZ() && AnglersSatchelRecipeSupport.isAnglersSatchelRecipe(this) && !AnglersSatchelRecipeSupport.validThreeStarRing(var1)) {
         var3.setReturnValue(Boolean.FALSE);
      }
   }

   @Inject(
      method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;",
      at = @At("RETURN"),
      cancellable = true,
      remap = false,
      require = 0
   )
   private void tideborne$preserveSatchelContents(CraftingRecipeInput var1, WrapperLookup var2, CallbackInfoReturnable<ItemStack> var3) {
      if (AnglersSatchelRecipeSupport.isAnglersSatchelRecipe(this) && AnglersSatchelRecipeSupport.validThreeStarRing(var1)) {
         var3.setReturnValue((ItemStack)AnglersSatchelRecipeSupport.convertedOutput(var1, var3.getReturnValue()));
      }
   }

   @Inject(
      method = "craft(Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;",
      at = @At("RETURN"),
      cancellable = true,
      remap = false,
      require = 0
   )
   private void tideborne$preserveSatchelContentsBridge(RecipeInput var1, WrapperLookup var2, CallbackInfoReturnable<ItemStack> var3) {
      if (AnglersSatchelRecipeSupport.isAnglersSatchelRecipe(this) && AnglersSatchelRecipeSupport.validThreeStarRing(var1)) {
         var3.setReturnValue((ItemStack)AnglersSatchelRecipeSupport.convertedOutput(var1, var3.getReturnValue()));
      }
   }
}
