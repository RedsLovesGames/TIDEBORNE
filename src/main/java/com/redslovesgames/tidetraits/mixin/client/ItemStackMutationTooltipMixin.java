/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.redslovesgames.tideborne.api.TideborneFishingApi;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitDisplay;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
      ItemStack stack = (ItemStack)(Object)this;
      Optional<SpecimenData> canonicalSpecimen = TideborneFishingApi.readCurrentSpecimen(stack);
      if (canonicalSpecimen.isEmpty()) {
         return;
      }

      SpecimenData specimen = canonicalSpecimen.orElseThrow();
      int rarityStars = TideborneFishingApi.resolveSpeciesProfile(specimen.speciesId())
         .map(profile -> profile.rarity().stars())
         .orElse(0);
      CanonicalSpecimenPresentation.View presentation = CanonicalSpecimenPresentation.present(specimen, rarityStars);

      ArrayList<Text> original = new ArrayList<>((Collection<Text>)callback.getReturnValue());
      int insertionIndex = canonicalInsertionIndex(original);
      original.removeIf(ItemStackMutationTooltipMixin::isCanonicalFishField);
      insertionIndex = Math.min(insertionIndex, original.size());

      List<Text> canonical = new ArrayList<>();
      canonical.add(field("Species", CanonicalSpecimenPresentation.trait(speciesPath(presentation.speciesId())), Formatting.AQUA));
      if (!CanonicalSpecimenPresentation.UNAVAILABLE.equals(presentation.rarityStars())) {
         canonical.add(field("Rarity", presentation.rarityStars(), Formatting.GOLD));
      }
      canonical.add(field("Length", presentation.length(), Formatting.AQUA));
      canonical.add(field("Percentile", presentation.percentile(), Formatting.AQUA));
      for (TraitDisplay trait : presentation.traits()) {
         canonical.add(field(trait.label(), trait.value(), trait.color()));
      }
      if (presentation.perfectCatch()) {
         canonical.add(Text.literal("Perfect Catch").formatted(Formatting.AQUA));
      }
      if (specimen.fishScore().isPresent()) {
         canonical.add(field("FishScore", presentation.fishScore(), Formatting.AQUA));
      }

      original.addAll(insertionIndex, canonical);
      callback.setReturnValue(List.copyOf(original));
   }

   private static MutableText field(String label, String value, Formatting valueColor) {
      return Text.literal(label + ": ").formatted(Formatting.GRAY)
         .append(Text.literal(value).formatted(valueColor));
   }

   private static MutableText field(String label, String value, int valueColor) {
      return Text.literal(label + ": ").formatted(Formatting.GRAY)
         .append(Text.literal(value).styled(style -> style.withColor(valueColor)));
   }

   private static int canonicalInsertionIndex(List<Text> tooltip) {
      for (int i = 0; i < tooltip.size(); i++) {
         if (isCanonicalFishField(tooltip.get(i))) {
            return i;
         }
      }
      return Math.min(1, tooltip.size());
   }

   private static boolean isCanonicalFishField(Text line) {
      String text = line.getString();
      return text.startsWith("Species:")
         || text.startsWith("Rarity:")
         || text.startsWith("Length:")
         || text.startsWith("Percentile:")
         || text.startsWith("Body Type:")
         || text.startsWith("Condition:")
         || text.startsWith("Pigmentation:")
         || text.startsWith("Quality:")
         || text.startsWith("Fish Score:")
         || text.startsWith("FishScore:")
         || text.equals("Perfect Specimen")
         || text.equals("Perfect Catch");
   }

   private static String speciesPath(String speciesId) {
      int separator = speciesId.indexOf(':');
      return separator >= 0 && separator + 1 < speciesId.length()
         ? speciesId.substring(separator + 1)
         : speciesId;
   }
}
