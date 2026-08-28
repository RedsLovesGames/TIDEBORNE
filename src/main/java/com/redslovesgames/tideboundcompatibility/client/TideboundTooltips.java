/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import com.li64.tide.util.BaitUtils;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import java.util.List;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

final class TideboundTooltips {
   private TideboundTooltips() {
   }

   static void append(ItemStack stack, TooltipType flag, List<Text> lines) {
      if (isTideboundItem(stack)) {
         if (stack.isOf(TideboundItems.LEVIATHAN_BAIT)) {
            lines.removeAll(BaitUtils.getDescriptionLines(stack));
         }

         if (stack.isOf(TideboundItems.KUJIRA_BONE_FISHING_ROD)) {
            lines.add(flavor("Pale bone worn smooth by a sea that never forgets."));
         } else if (stack.isOf(TideboundItems.LEVIATHAN_BAIT)) {
            lines.add(flavor(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.flavor")));
         } else if (stack.isOf(TideboundItems.CHUM_BUCKET)) {
            lines.add(flavor("The ocean always knows when dinner is served."));
         } else if (stack.isOf(TideboundItems.SHARK_TOOTH)) {
            lines.add(flavor("Still sharp enough to make the water feel unsafe."));
         }

         if (flag.isAdvanced() && TideboundClientConfig.get().showEquipmentTooltips) {
            if (!ClientTideboundSettings.available()) {
               lines.add(gray("Server values unavailable"));
            } else {
               if (stack.isOf(TideboundItems.TENTACLE_LINE)) {
                  lines.add(stat("Catch zone", "tentacle_zone"));
                  lines.add(stat("Fish speed", "tentacle_speed"));
               } else if (stack.isOf(TideboundItems.SWIFT_LINE)) {
                  lines.add(stat("Catch zone", "swift_zone"));
                  lines.add(stat("Fish speed", "swift_speed"));
               } else if (stack.isOf(TideboundItems.STEEL_LEADER)) {
                  lines.add(stat("Catch zone", "steel_zone"));
                  lines.add(stat("Fish speed", "steel_speed"));
                  lines.add(Text.literal("Catch-loss protection: " + percent("steel_prevent")));
               } else if (stack.isOf(TideboundItems.SEAFARERS_HOOK)) {
                  lines.add(Text.literal("Night ocean legendary weight: " + multiplier("seafarer_rare")));
               } else if (stack.isOf(TideboundItems.SHARK_TOOTH_HOOK)) {
                  lines.add(Text.literal("Large/predatory weight: " + multiplier("tooth_predatory")));
                  lines.add(Text.literal("Very-small weight: " + multiplier("tooth_small")));
               } else if (stack.isOf(TideboundItems.KUJIRA_BONE_FISHING_ROD)) {
                  lines.add(Text.literal("Bait slots: 3 | Durability: 512"));
                  lines.add(Text.literal("Ocean crate weight: " + multiplier("kujira_crates")));
               } else if (stack.isOf(TideboundItems.LEVIATHAN_BAIT)) {
                  lines.add(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.fish_only"));
                  lines.add(
                     Text.translatable(
                        "tooltip.tidebound_compatibility.leviathan_bait.luck", new Object[]{ClientTideboundSettings.integer("leviathan_fish_luck")}
                     )
                  );
                  lines.add(
                     Text.translatable(
                        "tooltip.tidebound_compatibility.leviathan_bait.difficulty", new Object[]{multiplier("leviathan_speed"), multiplier("leviathan_zone")}
                     )
                  );
                  lines.add(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.conditions"));
               } else if (stack.isOf(TideboundItems.CHUM_BUCKET)) {
                  lines.add(
                     Text.literal(
                        "Scent: "
                           + ClientTideboundSettings.integer("chum_radius")
                           + " blocks for "
                           + ClientTideboundSettings.integer("chum_duration")
                           + "s; density "
                           + ClientTideboundSettings.integer("chum_particles")
                     )
                  );
               }
            }
         }
      }
   }

   static boolean isTideboundItem(ItemStack stack) {
      return stack.isOf(TideboundItems.TENTACLE_LINE)
         || stack.isOf(TideboundItems.SWIFT_LINE)
         || stack.isOf(TideboundItems.STEEL_LEADER)
         || stack.isOf(TideboundItems.SEAFARERS_HOOK)
         || stack.isOf(TideboundItems.SHARK_TOOTH_HOOK)
         || stack.isOf(TideboundItems.KUJIRA_BONE_FISHING_ROD)
         || stack.isOf(TideboundItems.LEVIATHAN_BAIT)
         || stack.isOf(TideboundItems.CHUM_BUCKET)
         || stack.isOf(TideboundItems.SHARK_TOOTH);
   }

   private static Text flavor(String text) {
      return flavor(Text.literal(text));
   }

   private static Text flavor(Text text) {
      return text.copy().styled(style -> style.withColor(9079434).withItalic(true));
   }

   private static Text gray(String text) {
      return Text.literal(text).styled(style -> style.withColor(8947848));
   }

   private static Text stat(String name, String key) {
      return Text.literal(name + ": " + multiplier(key));
   }

   static String multiplier(String key) {
      return String.format(Locale.ROOT, "%.0f%%", ClientTideboundSettings.decimal(key) * 100.0);
   }

   static String percent(String key) {
      return String.format(Locale.ROOT, "%.1f%%", ClientTideboundSettings.decimal(key) * 100.0);
   }
}
