/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ClientRecordFishMarkers {
   public static final ClientRecordFishMarkers.Status NONE = new ClientRecordFishMarkers.Status(false, false, 0.0, 0.0);

   private ClientRecordFishMarkers() {
   }

   public static ClientRecordFishMarkers.Status get(ItemStack stack) {
      if (!stack.isEmpty() && TidePlayerData.CLIENT_DATA != null) {
         Double length = (Double)TideItemData.FISH_LENGTH.getOptional(stack).orElse(null);
         FishData fish = FishData.get(stack).or(() -> FishData.fromBucket(stack)).orElse(null);
         if (length != null && Double.isFinite(length) && !(length <= 0.0) && fish != null) {
            FishPlayerData playerData = (FishPlayerData)TidePlayerData.CLIENT_DATA.getDataFor((Item)fish.fish().value()).orElse(null);
            FishStats stats = playerData == null ? null : (FishStats)playerData.stats.orElse(null);
            if (stats != null && !stats.isEmpty()) {
               double largest = stats.getLargestCatch();
               double smallest = stats.getSmallestCatch();
               return new ClientRecordFishMarkers.Status(sameLength(length, largest), sameLength(length, smallest), largest, smallest);
            } else {
               return NONE;
            }
         } else {
            return NONE;
         }
      } else {
         return NONE;
      }
   }

   private static boolean sameLength(double itemLength, double recordLength) {
      if (Double.isFinite(recordLength) && !(recordLength <= 0.0)) {
         double tolerance = Math.max(1.0E-6, Math.ulp(recordLength) * 4.0);
         return Math.abs(itemLength - recordLength) <= tolerance;
      } else {
         return false;
      }
   }

   public record Status(boolean largest, boolean smallest, double largestSize, double smallestSize) {
      public boolean isRecord() {
         return this.largest || this.smallest;
      }
   }
}
