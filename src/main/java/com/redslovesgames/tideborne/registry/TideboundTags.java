/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.registry;

import com.redslovesgames.tideborne.compat.TideboundCompatibility;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;

public final class TideboundTags {
   public static final TagKey<Item> SHARK_FOOD = item("shark_food");
   public static final TagKey<Item> STRONG_SHARK_FOOD = item("strong_shark_food");
   public static final TagKey<Item> HIPPOCAMPUS_FOOD = item("hippocampus_food");
   public static final TagKey<Item> LARGE_FISH = item("large_fish");
   public static final TagKey<Item> VERY_SMALL_FISH = item("very_small_fish");
   public static final TagKey<Item> PREDATORY_FISH = item("predatory_fish");
   /** Eligible Tide fish items explicitly classified as Leviathan/boss targets by a datapack. */
   public static final TagKey<Item> LEVIATHAN_TARGETS = item("leviathan_targets");
   public static final TagKey<Item> TUNA = item("tuna");

   private TideboundTags() {
   }

   private static TagKey<Item> item(String path) {
      return TagKey.of(RegistryKeys.ITEM, TideboundCompatibility.id(path));
   }
}
