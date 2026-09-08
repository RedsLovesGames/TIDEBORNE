package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;

/** @deprecated Legacy facade. Steel Leader is now the Iron Leader tier. */
@Deprecated
public final class SteelLeaderAttachment {
   private SteelLeaderAttachment() {}

   public static boolean has(Object value) {
      return LeaderAttachment.has(value);
   }

   public static void set(Object value, boolean attached) {
      LeaderAttachment.set(value, attached ? LeaderTier.IRON : null);
   }

   public static boolean isSteelLeaderStack(Object value) {
      return LeaderAttachment.tierOfStack(value) == LeaderTier.IRON;
   }

   public static boolean hasOnHook(Object value) {
      return value instanceof TideFishingHook hook && LeaderAttachment.tierOnHook(hook) != null;
   }

   public static Object steelLeaderItem() {
      return TideboundItems.IRON_LEADER;
   }
}
