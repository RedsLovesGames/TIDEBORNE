/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.mixin.client;

import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
      if (CanonicalSpecimenStorage.detectMigration(stack) != CanonicalSpecimenStorage.MigrationState.CANONICAL_CURRENT) {
         return;
      }

      String speciesId = stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
      Double finalLength = stack.get(TideTraitsComponents.SPECIMEN_FINAL_LENGTH);
      Double finalPercentile = stack.get(TideTraitsComponents.SPECIMEN_FINAL_PERCENTILE);
      String bodyType = stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE);
      String condition = stack.get(TideTraitsComponents.SPECIMEN_CONDITION);
      String pigmentation = stack.get(TideTraitsComponents.SPECIMEN_PIGMENTATION);
      String quality = stack.get(TideTraitsComponents.SPECIMEN_QUALITY);
      Boolean perfectCatch = stack.get(TideTraitsComponents.SPECIMEN_PERFECT_CATCH);
      Integer fishScore = stack.get(TideTraitsComponents.SPECIMEN_FISH_SCORE);
      if (speciesId == null || finalLength == null || finalPercentile == null || bodyType == null
         || condition == null || pigmentation == null || quality == null || perfectCatch == null) {
         return;
      }

      ArrayList<Text> original = new ArrayList<>((Collection<Text>)callback.getReturnValue());
      int insertionIndex = canonicalInsertionIndex(original);
      original.removeIf(ItemStackMutationTooltipMixin::isCanonicalFishField);
      insertionIndex = Math.min(insertionIndex, original.size());

      List<Text> canonical = new ArrayList<>();
      canonical.add(field("Species", titleCase(speciesPath(speciesId)), Formatting.AQUA));

      Integer rarityStars = rarityStars(speciesId);
      if (rarityStars != null) {
         canonical.add(field("Rarity", "★".repeat(rarityStars), Formatting.GOLD));
      }

      canonical.add(field("Length", formatLength(finalLength), Formatting.AQUA));
      canonical.add(field("Percentile", formatPercentile(finalPercentile), Formatting.AQUA));
      canonical.add(field("Body Type", titleCase(bodyType), traitColor(bodyType)));
      canonical.add(field("Condition", titleCase(condition), traitColor(condition)));
      canonical.add(field("Pigmentation", titleCase(pigmentation), traitColor(pigmentation)));

      if ("perfect_specimen".equalsIgnoreCase(quality)) {
         canonical.add(Text.literal("Perfect Specimen").formatted(Formatting.GOLD));
      }
      if (perfectCatch) {
         canonical.add(Text.literal("Perfect Catch").formatted(Formatting.AQUA));
      }
      if (fishScore != null) {
         canonical.add(field("Fish Score", Integer.toString(fishScore), Formatting.AQUA));
      }

      original.addAll(insertionIndex, canonical);
      callback.setReturnValue(List.copyOf(original));
   }

   private static MutableText field(String label, String value, Formatting valueColor) {
      return Text.literal(label + ": ").formatted(Formatting.GRAY)
         .append(Text.literal(value).formatted(valueColor));
   }

   private static Formatting traitColor(String value) {
      return "normal".equalsIgnoreCase(value) ? Formatting.GRAY : Formatting.LIGHT_PURPLE;
   }

   private static Integer rarityStars(String speciesId) {
      Identifier id = Identifier.tryParse(speciesId);
      if (id == null) {
         return null;
      }
      Item speciesItem = Registries.ITEM.get(id);
      return FishData.get(speciesItem)
         .map(data -> data.profile().rarity().getNumStars())
         .filter(stars -> stars >= 1 && stars <= 5)
         .orElse(null);
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
         || text.startsWith("Fish Score:")
         || text.equals("Perfect Specimen")
         || text.equals("Perfect Catch");
   }

   private static String speciesPath(String speciesId) {
      int separator = speciesId.indexOf(':');
      return separator >= 0 && separator + 1 < speciesId.length()
         ? speciesId.substring(separator + 1)
         : speciesId;
   }

   private static String formatLength(double length) {
      return String.format(Locale.ROOT, "%.1f cm", length);
   }

   private static String formatPercentile(double percentile) {
      return String.format(Locale.ROOT, "P%.2f", percentile);
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
