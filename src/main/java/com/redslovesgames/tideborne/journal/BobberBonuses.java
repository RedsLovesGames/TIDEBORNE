/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal;

import java.util.Map;
import net.minecraft.util.Identifier;

/** Display-only synchronized luck/lure values; server gear resolves through BobberGearModifiers. */
public final class BobberBonuses {
   private static volatile Map<Identifier, BobberBonuses.Bonus> clientBonuses = Map.of();
   private static volatile BobberBonuses.Bonus clientFallback = new BobberBonuses.Bonus(0, 1);
   private static volatile boolean clientEnabled = true;

   private BobberBonuses() {
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
