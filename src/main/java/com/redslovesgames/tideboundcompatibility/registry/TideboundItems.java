/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.registry;

import com.li64.tide.registries.items.FishingHookItem;
import com.li64.tide.registries.items.FishingLineItem;
import com.li64.tide.registries.items.TideFishingRodItem;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.item.ChumBucketItem;
import com.redslovesgames.tideboundcompatibility.item.KujiraBoneFishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item.Settings;

public final class TideboundItems {
   public static final Item TENTACLE_LINE = new FishingLineItem(new Settings().maxCount(1), "item.tidebound_compatibility.tentacle_line.desc");
   public static final Item SEAFARERS_HOOK = new FishingHookItem(new Settings().maxCount(1), "item.tidebound_compatibility.seafarers_hook.desc");
   public static final Item SWIFT_LINE = new FishingLineItem(new Settings().maxCount(1), "item.tidebound_compatibility.swift_line.desc");
   public static final TideFishingRodItem KUJIRA_BONE_FISHING_ROD = new KujiraBoneFishingRodItem(3, 512.0, new Settings());
   public static final Item LEVIATHAN_BAIT = new Item(new Settings());
   public static final Item CHUM_BUCKET = new ChumBucketItem(new Settings().maxCount(1));
   public static final Item STEEL_LEADER = new FishingLineItem(new Settings().maxCount(1), "item.tidebound_compatibility.steel_leader.desc");
   public static final Item SHARK_TOOTH = new Item(new Settings());
   public static final Item SHARK_TOOTH_HOOK = new FishingHookItem(new Settings().maxCount(1), "item.tidebound_compatibility.shark_tooth_hook.desc");

   private TideboundItems() {
   }

   public static void register() {
      register("tentacle_line", TENTACLE_LINE);
      register("seafarers_hook", SEAFARERS_HOOK);
      register("swift_line", SWIFT_LINE);
      register("kujira_bone_fishing_rod", KUJIRA_BONE_FISHING_ROD);
      register("leviathan_bait", LEVIATHAN_BAIT);
      register("chum_bucket", CHUM_BUCKET);
      register("steel_leader", STEEL_LEADER);
      register("shark_tooth", SHARK_TOOTH);
      register("shark_tooth_hook", SHARK_TOOTH_HOOK);
   }

   private static void register(String path, Item item) {
      Registry.register(Registries.ITEM, TideboundCompatibility.id(path), item);
   }
}
