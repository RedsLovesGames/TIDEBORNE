/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.compat.apex;

import com.acorsicanfrog.apexwaters.entity.GreatWhiteSharkEntity;
import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import com.redslovesgames.tideborne.registry.TideboundItems;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents.Modify;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.util.Identifier;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.RegistryKeys;

public final class ApexCompat {
   private static final RegistryKey<LootTable> SHARK_LOOT = RegistryKey.of(
      RegistryKeys.LOOT_TABLE, Identifier.of("apexwaters", "entities/great_white_shark")
   );
   private static final Map<GreatWhiteSharkEntity, Long> PREY_COOLDOWNS = new WeakHashMap<>();
   private static boolean initialized;

   private ApexCompat() {
   }

   public static void initialize() {
      if (!initialized) {
         initialized = true;
         LootTableEvents.MODIFY
            .register(
               (Modify)(key, tableBuilder, source, registries) -> {
                  if (key.equals(SHARK_LOOT)) {
                     tableBuilder.pool(
                        LootPool.builder()
                           .rolls(ConstantLootNumberProvider.create(1.0F))
                           .conditionally(RandomChanceLootCondition.builder(0.35F))
                           .with(ItemEntry.builder(TideboundItems.SHARK_TOOTH))
                     );
                  }
               }
            );
         FishingGameplayInitializer.LOGGER.info("Added the compatibility-side shark tooth drop to Apex Waters Great Whites");
      }
   }

   public static boolean preyCooldownActive(GreatWhiteSharkEntity shark) {
      long now = shark.getWorld().getTime();
      long until = PREY_COOLDOWNS.getOrDefault(shark, 0L);
      if (until <= now) {
         PREY_COOLDOWNS.remove(shark);
         return false;
      } else {
         return true;
      }
   }

   public static void setPreyCooldown(GreatWhiteSharkEntity shark, long ticks) {
      PREY_COOLDOWNS.put(shark, shark.getWorld().getTime() + ticks);
   }
}
