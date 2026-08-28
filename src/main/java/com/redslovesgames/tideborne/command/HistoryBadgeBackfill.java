/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.li64.tide.data.fishing.SizeData;
import com.redslovesgames.tideteamjournal.TeamJournalService;
import com.redslovesgames.tidetraits.catching.CatchTraitService;
import com.redslovesgames.tidetraits.discovery.DiscoveryManager;
import com.redslovesgames.tidetraits.fish.FishDescriptor;
import com.redslovesgames.tidetraits.fish.FishSizeClass;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HistoryBadgeBackfill {
   private HistoryBadgeBackfill() {
   }

   public static int run(ServerCommandSource var0) {
      if (!var0.hasPermissionLevel(2)) {
         var0.sendFeedback(() -> Text.literal("You need operator permission level 2 to backfill history badges."), false);
         return 0;
      }

      int var1 = 0;
      int var2 = 0;
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;

      for (ServerPlayerEntity var7 : var0.getServer().getPlayerManager().getPlayerList()) {
         try {
            UUID var8 = var7.getUuid();
            int var9 = 0;
            int var10 = 1;
            boolean var11 = false;

            do {
               NbtCompound var12 = TeamJournalService.teamData(var7, var9, "catches", "");
               if (var9 == 0) {
                  var10 = Math.max(1, var12.getInt("pages"));
               }

               for (NbtElement var15 : var12.getList("history", 10)) {
                  if (var15 instanceof NbtCompound var16) {
                     UUID var17;
                     try {
                        var17 = var16.getUuid("actor_id");
                     } catch (RuntimeException var27) {
                        continue;
                     }

                     if (var17 != null && var17.equals(var8)) {
                        var2++;
                        Identifier var18 = Identifier.tryParse(var16.getString("fish"));
                        if (var18 != null) {
                           boolean var19 = false;
                           String var20 = var16.getString("condition");
                           if (var20.isEmpty()) {
                              var20 = var16.getString("mutation");
                           }

                           if (!var20.isEmpty()) {
                              var19 = true;
                              if (!"normal".equals(var20)) {
                                 Identifier var21 = Identifier.of("tide_traits", var20);
                                 if (DiscoveryManager.discoverMutation(var7, var18, var21)) {
                                    var3++;
                                    var11 = true;
                                 }
                              }
                           }

                           String var34 = var16.getString("body_type");
                           if (!var34.isEmpty()) {
                              var19 = true;
                              if (!"normal".equals(var34)) {
                                 Identifier var22 = Identifier.of("tide_traits", var34);
                                 if (DiscoveryManager.discoverMutation(var7, var18, var22)) {
                                    var3++;
                                    var11 = true;
                                 }
                              }
                           }

                           if (!var19) {
                              var4++;
                           }

                           double var35 = 0.0 / 0.0;
                           if (var16.contains("percentile", 6)) {
                              double var24 = var16.getDouble("percentile");
                              if (Double.isFinite(var24) && var24 >= 0.0 && var24 <= 100.0) {
                                 var35 = var24;
                              }
                           }

                           if (!Double.isFinite(var35)) {
                              Optional var36 = CatchTraitService.INSTANCE.descriptors().find(var18);
                              if (var36.isPresent()) {
                                 Optional var25 = ((FishDescriptor)var36.get()).sizeData();
                                 if (var25.isPresent()) {
                                    OptionalDouble var26 = CatchTraitService.INSTANCE
                                       .percentiles()
                                       .percentile(var18, (SizeData)var25.get(), var16.getDouble("new_size"));
                                    if (var26.isPresent()) {
                                       var35 = var26.getAsDouble();
                                    }
                                 }
                              }
                           }

                           if (Double.isFinite(var35) && var35 >= 0.0 && var35 <= 100.0) {
                              String var37 = FishSizeClass.fromPercentile(var35).serializedName();
                              Identifier var38 = Identifier.of("tide_traits", var37);
                              if (DiscoveryManager.discoverSizeBand(var7, var18, var38)) {
                                 var3++;
                                 var11 = true;
                              }
                           }
                        }
                     }
                  }
               }
            } while (++var9 < var10);

            if (var11) {
               DiscoveryManager.sync(var7);
            }

            var1++;
         } catch (RuntimeException var28) {
            var5++;
         }
      }

      int var29 = var1;
      int var30 = var2;
      int var31 = var3;
      int var32 = var4;
      int var33 = var5;
      var0.sendFeedback(
         () -> Text.literal(
            "Tideborne history badge backfill: "
               + var30
               + " events scanned, "
               + var31
               + " badges unlocked for "
               + var29
               + " online players in this world. "
               + var32
               + " legacy events had no saved Body Type/Condition metadata."
               + (var33 > 0 ? " " + var33 + " player histories failed to process." : "")
         ),
         false
      );
      return var3;
   }
}
