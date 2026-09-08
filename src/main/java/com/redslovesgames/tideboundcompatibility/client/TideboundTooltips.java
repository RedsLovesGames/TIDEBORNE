/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import com.li64.tide.util.BaitUtils;
import com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry;
import com.redslovesgames.tideboundcompatibility.fishing.LeaderTier;
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
      FishingGearRegistry.GearProfile profile = FishingGearRegistry.resolve(stack).orElse(null);
      if (isTideboundItem(stack)) {
         if (profile == FishingGearRegistry.GearProfile.LEVIATHAN_BAIT) {
            lines.removeAll(BaitUtils.getDescriptionLines(stack));
         }

         if (profile == FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD) {
            lines.add(flavor("Pale bone worn smooth by a sea that never forgets."));
         } else if (profile == FishingGearRegistry.GearProfile.LEVIATHAN_BAIT) {
            lines.add(flavor(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.flavor")));
         } else if (stack.isOf(TideboundItems.CHUM_BUCKET)) {
            lines.add(flavor("The ocean always knows when dinner is served."));
         } else if (stack.isOf(TideboundItems.SHARK_TOOTH)) {
            lines.add(flavor("Still sharp enough to make the water feel unsafe."));
         }

         if (flag.isAdvanced() && TideboundClientConfig.get().showEquipmentTooltips) {
            if (!ClientTideboundSettings.available()) {
               lines.add(gray("Server values unavailable"));
            } else if (profile != null) {
               switch (profile) {
                  case TENTACLE_LINE -> {
                     lines.add(stat("Catch zone", "tentacle_zone"));
                     lines.add(stat("Fish speed", "tentacle_speed"));
                  }
                  case SWIFT_LINE -> {
                     lines.add(stat("Catch zone", "swift_zone"));
                     lines.add(stat("Fish speed", "swift_speed"));
                  }
                  case COPPER_LEADER -> appendLeader(lines, LeaderTier.COPPER);
                  case IRON_LEADER -> appendLeader(lines, LeaderTier.IRON);
                  case GOLD_LEADER -> appendLeader(lines, LeaderTier.GOLD);
                  case DIAMOND_LEADER -> appendLeader(lines, LeaderTier.DIAMOND);
                  case SEAFARERS_HOOK ->
                     lines.add(Text.literal("Night ocean legendary weight: " + multiplier("seafarer_rare")));
                  case SHARK_TOOTH_HOOK -> {
                     lines.add(Text.literal("Large/predatory weight: " + multiplier("tooth_predatory")));
                     lines.add(Text.literal("Very-small weight: " + multiplier("tooth_small")));
                  }
                  case KUJIRA_BONE_FISHING_ROD -> {
                     lines.add(Text.literal("Bait slots: 3 | Durability: 512"));
                     lines.add(Text.literal("Ocean crate weight: " + multiplier("kujira_crates")));
                  }
                  case LEVIATHAN_BAIT -> {
                     lines.add(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.fish_only"));
                     lines.add(
                        Text.translatable(
                           "tooltip.tidebound_compatibility.leviathan_bait.luck",
                           new Object[]{ClientTideboundSettings.integer("leviathan_fish_luck")}
                        )
                     );
                     lines.add(
                        Text.translatable(
                           "tooltip.tidebound_compatibility.leviathan_bait.fight",
                           new Object[]{multiplier("leviathan_strength"), multiplier("leviathan_tempo")}
                        )
                     );
                     lines.add(Text.literal("Trait Luck: +" + ClientTideboundSettings.integer("leviathan_trait_luck")));
                     lines.add(Text.translatable("tooltip.tidebound_compatibility.leviathan_bait.conditions"));
                  }
                  default -> {
                     // Native Tide profiles are intentionally not Tideborne tooltip entries.
                  }
               }
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

   static boolean isTideboundItem(ItemStack stack) {
      boolean registeredGear = FishingGearRegistry.resolve(stack)
         .map(profile -> profile.origin() == FishingGearRegistry.Origin.TIDEBORNE)
         .orElse(false);
      return registeredGear
         || stack.isOf(TideboundItems.CHUM_BUCKET)
         || stack.isOf(TideboundItems.SHARK_TOOTH);
   }

   private static void appendLeader(List<Text> lines, LeaderTier tier) {
      lines.add(Text.literal("Catch zone: " + formatMultiplier(tier.catchZoneMultiplier())));
      lines.add(Text.literal("Fish speed: " + formatMultiplier(tier.fishSpeedMultiplier())));
      lines.add(Text.literal("Catch-loss protection: " + formatPercent(tier.protection())));
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
      return formatMultiplier(ClientTideboundSettings.decimal(key));
   }

   static String percent(String key) {
      return formatPercent(ClientTideboundSettings.decimal(key));
   }

   private static String formatMultiplier(double value) {
      return String.format(Locale.ROOT, "%.0f%%", value * 100.0);
   }

   private static String formatPercent(double value) {
      return String.format(Locale.ROOT, "%.1f%%", value * 100.0);
   }
}
