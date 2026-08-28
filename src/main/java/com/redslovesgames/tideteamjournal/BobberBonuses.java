/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal;

import com.li64.tide.data.TideTags.Items;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class BobberBonuses {
   private static volatile Map<Identifier, BobberBonuses.Bonus> clientBonuses = Map.of();
   private static volatile BobberBonuses.Bonus clientFallback = new BobberBonuses.Bonus(0, 1);
   private static volatile boolean clientEnabled = true;

   private BobberBonuses() {
   }

   public static BobberBonuses.Bonus get(ItemStack bobber) {
      if (!bobber.isEmpty() && bobber.isIn(Items.BOBBERS)) {
         Identifier id = Registries.ITEM.getId(bobber.getItem());
         return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? forClientId(id) : forId(id);
      } else {
         return BobberBonuses.Bonus.NONE;
      }
   }

   static BobberBonuses.Bonus forId(Identifier id) {
      ServerConfig.Values config = ServerConfig.get();
      return !config.bobberBonusesEnabled ? BobberBonuses.Bonus.NONE : config.bobberBonuses.getOrDefault(id.toString(), config.fallbackBobberBonus);
   }

   public static BobberBonuses.Bonus forClientId(Identifier id) {
      return clientEnabled ? clientBonuses.getOrDefault(id, clientFallback) : BobberBonuses.Bonus.NONE;
   }

   public static void updateClient(boolean enabled, BobberBonuses.Bonus fallback, Map<Identifier, BobberBonuses.Bonus> bonuses) {
      clientEnabled = enabled;
      clientFallback = fallback;
      clientBonuses = Map.copyOf(bonuses);
   }

   public record Bonus(int luck, int lureSpeed) {
      public static final BobberBonuses.Bonus NONE = new BobberBonuses.Bonus(0, 0);

      public boolean isEmpty() {
         return this.luck == 0 && this.lureSpeed == 0;
      }
   }
}
