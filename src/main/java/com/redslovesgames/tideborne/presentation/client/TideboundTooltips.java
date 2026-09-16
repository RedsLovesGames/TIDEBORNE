/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.client;

import com.li64.tide.util.BaitUtils;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.gear.LeaderTier;
import com.redslovesgames.tideborne.registry.TideboundItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

final class TideboundTooltips {
   private static final double EPSILON = 1.0E-9;

   private TideboundTooltips() {
   }

   static void append(ItemStack stack, TooltipType flag, List<Text> lines) {
      FishingGearRegistry.GearProfile profile = FishingGearRegistry.resolve(stack).orElse(null);
      if (appendTideBobberEffects(stack, flag, lines)) {
         return;
      }
      if (isTideboundItem(stack)) {
         if (profile == FishingGearRegistry.GearProfile.LEVIATHAN_BAIT) {
            lines.removeAll(BaitUtils.getDescriptionLines(stack));
         }

         if (profile == FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD) {
            lines.add(flavor("Pale bone worn smooth by a sea that never forgets."));
         } else if (profile == FishingGearRegistry.GearProfile.LEVIATHAN_BAIT) {
            lines.add(flavor(Text.translatable("tooltip.tideborne.fishing.leviathan_bait.flavor")));
         } else if (stack.isOf(TideboundItems.CHUM_BUCKET)) {
            lines.add(flavor("The ocean always knows when dinner is served."));
         } else if (stack.isOf(TideboundItems.SHARK_TOOTH)) {
            lines.add(flavor("Still sharp enough to make the water feel unsafe."));
         }

         if (flag.isAdvanced() && TideboundClientConfig.get().showEquipmentTooltips) {
            if (!ClientTideboundSettings.available()) {
               lines.add(gray(Text.translatable("tooltip.tideborne.fishing.stats_syncing")));
            } else if (profile != null) {
               switch (profile) {
                  case TENTACLE_LINE -> {
                     lines.add(stat("Catch Zone", "tentacle_zone"));
                     lines.add(stat("Fish Movement", "tentacle_speed"));
                  }
                  case SWIFT_LINE -> {
                     lines.add(stat("Catch Zone", "swift_zone"));
                     lines.add(stat("Fish Movement", "swift_speed"));
                  }
                  case COPPER_LEADER -> appendLeader(lines, LeaderTier.COPPER);
                  case IRON_LEADER -> appendLeader(lines, LeaderTier.IRON);
                  case GOLD_LEADER -> appendLeader(lines, LeaderTier.GOLD);
                  case DIAMOND_LEADER -> appendLeader(lines, LeaderTier.DIAMOND);
                  case SEAFARERS_HOOK ->
                     lines.add(Text.translatable("tooltip.tideborne.fishing.seafarer.legendary_focus", multiplier("seafarer_rare")));
                  case SHARK_TOOTH_HOOK -> {
                     lines.add(Text.translatable("tooltip.tideborne.fishing.shark_tooth.large_focus", multiplier("tooth_predatory")));
                     lines.add(Text.translatable("tooltip.tideborne.fishing.shark_tooth.small_focus", multiplier("tooth_small")));
                  }
                  case KUJIRA_BONE_FISHING_ROD -> {
                     lines.add(Text.translatable("tooltip.tideborne.fishing.kujira.rod_stats"));
                     lines.add(Text.translatable("tooltip.tideborne.fishing.kujira.crate_focus", multiplier("kujira_crates")));
                  }
                  case LEVIATHAN_BAIT -> {
                     lines.add(Text.translatable("tooltip.tideborne.fishing.leviathan_bait.fish_only"));
                     lines.add(
                        Text.translatable(
                           "tooltip.tideborne.fishing.leviathan_bait.luck",
                           new Object[]{ClientTideboundSettings.integer("leviathan_fish_luck")}
                        )
                     );
                     lines.add(
                        Text.translatable(
                           "tooltip.tideborne.fishing.leviathan_bait.difficulty",
                           new Object[]{multiplier("leviathan_strength"), multiplier("leviathan_tempo")}
                        )
                     );
                     lines.add(Text.translatable("tooltip.tideborne.fishing.trait_luck", ClientTideboundSettings.integer("leviathan_trait_luck")));
                     lines.add(Text.translatable("tooltip.tideborne.fishing.leviathan_bait.conditions"));
                  }
                  default -> {
                     // Native Tide profiles are intentionally not duplicated here.
                  }
               }
            } else if (stack.isOf(TideboundItems.CHUM_BUCKET)) {
               lines.add(
                  Text.translatable(
                     "tooltip.tideborne.fishing.chum.scent",
                     ClientTideboundSettings.integer("chum_radius"),
                     ClientTideboundSettings.integer("chum_duration")
                  )
               );
            }
         }
      }
   }

   private static boolean appendTideBobberEffects(ItemStack stack, TooltipType flag, List<Text> lines) {
      if (!FishingGearRegistry.isSupportedBobber(stack)) {
         return false;
      }
      if (!flag.isAdvanced() || !TideboundClientConfig.get().showEquipmentTooltips) {
         return true;
      }

      FishingGearRegistry.bobberModifiers(Registries.ITEM.getId(stack.getItem())).ifPresent(modifiers -> {
         List<String> effects = bobberEffectLabels(modifiers);
         if (!effects.isEmpty()) {
            lines.add(gray(Text.translatable("tooltip.tideborne.fishing.effects")));
            effects.forEach(effect -> lines.add(Text.literal(effect)));
         }
      });
      return true;
   }

   static List<String> bobberEffectLabels(FishingGearModifiers modifiers) {
      List<String> effects = new ArrayList<>();
      if (Math.abs(modifiers.fishingLuck()) > EPSILON) {
         effects.add("Fishing Luck: " + formatSigned(modifiers.fishingLuck()));
      }
      if (Math.abs(modifiers.traitLuck()) > EPSILON) {
         effects.add("Trait Luck: " + formatSigned(modifiers.traitLuck()));
      }

      double lure = modifiers.namedAdditiveModifier(FishingGearEffects.LURE_BONUS);
      if (Math.abs(lure) > EPSILON) {
         effects.add("Lure Speed: " + formatSigned(lure));
      }

      double catchZone = modifiers.namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER);
      if (Math.abs(catchZone - 1.0) > EPSILON) {
         effects.add("Catch Zone: " + formatMultiplier(catchZone));
      }

      double crateWeight = modifiers.namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER);
      if (Math.abs(crateWeight - 1.0) > EPSILON) {
         effects.add("Crates favored: " + formatMultiplier(crateWeight));
      }

      double protection = modifiers.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE);
      if (Math.abs(protection) > EPSILON) {
         effects.add("Shark Protection: " + formatPercent(protection));
      }
      return List.copyOf(effects);
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
      lines.add(Text.literal("Catch Zone: " + formatMultiplier(tier.catchZoneMultiplier())));
      lines.add(Text.literal("Fish Movement: " + formatMultiplier(tier.fishSpeedMultiplier())));
      lines.add(Text.literal("Shark Protection: " + formatPercent(tier.protection())));
   }

   private static Text flavor(String text) {
      return flavor(Text.literal(text));
   }

   private static Text flavor(Text text) {
      return text.copy().styled(style -> style.withColor(9079434).withItalic(true));
   }

   private static Text gray(String text) {
      return gray(Text.literal(text));
   }

   private static Text gray(Text text) {
      return text.copy().styled(style -> style.withColor(8947848));
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

   private static String formatSigned(double value) {
      if (Math.abs(value - Math.rint(value)) <= EPSILON) {
         return String.format(Locale.ROOT, "%+.0f", value);
      }
      return String.format(Locale.ROOT, "%+.1f", value);
   }

   private static String formatMultiplier(double value) {
      return String.format(Locale.ROOT, "%.0f%%", value * 100.0);
   }

   private static String formatPercent(double value) {
      return String.format(Locale.ROOT, "%.1f%%", value * 100.0);
   }
}
