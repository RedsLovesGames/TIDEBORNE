/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.catching;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import com.redslovesgames.tidetraits.fish.EmpiricalFishDistribution;
import com.redslovesgames.tidetraits.fish.FishDescriptor;
import com.redslovesgames.tidetraits.trait.DeterministicValues;
import com.redslovesgames.tidetraits.trait.FishMutation;
import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.util.List;
import java.util.Optional;
import net.minecraft.item.ItemStack;

public final class PerfectCatchTraitBoost {
   private static final long PERCENTILE_TRIGGER_SALT = 5815410729529975569L;
   private static final long PERCENTILE_STRENGTH_SALT = 1971462275090437707L;
   private static final long PERCENTILE_SIDE_SALT = 4214826731716357889L;
   private static final long GIANT_ROLL_SALT = 7650794760523985177L;
   private static final long DWARF_ROLL_SALT = 4163329390116612967L;
   private static final long IRIDESCENT_ROLL_SALT = 8207443346169614273L;
   private static final long ALBINO_ROLL_SALT = 5704024059175898085L;
   private static final long PERFECT_ROLL_SALT = 3284796534600278910L;
   private static final long PARASITE_ROLL_SALT = 6633366370764118095L;
   private static final long SCARRED_ROLL_SALT = 1708175715983811855L;

   private PerfectCatchTraitBoost() {
   }

   public static void apply(List<ItemStack> var0) {
      if (var0 != null && !var0.isEmpty()) {
         for (ItemStack var2 : var0) {
            try {
               applyOne(var2);
            } catch (RuntimeException var4) {
            }
         }
      }
   }

   private static void applyOne(ItemStack var0) {
      if (var0 != null && !var0.isEmpty()) {
         Optional var1 = FishData.get(var0);
         if (!var1.isEmpty()) {
            Double var2 = (Double)var0.get(TideTraitsComponents.SIZE_PERCENTILE);
            if (var2 != null && Double.isFinite(var2)) {
               FishDescriptor var3 = FishDescriptor.fromFishData((FishData)var1.get());
               if (var3.supportsPhysicalLength()) {
                  Optional var4 = var3.sizeData();
                  if (!var4.isEmpty()) {
                     CatchTraitService var5 = CatchTraitService.INSTANCE;
                     TideTraitsConfig var6 = var5.config();
                     Optional var7 = var5.percentiles().baseline(var3.canonicalSpeciesId(), (SizeData)var4.get());
                     if (!var7.isEmpty()) {
                        long var8 = TraitAxesRuntime.identitySeed(var0);
                        double var10 = clamp(var2, 0.0, 100.0);
                        if (DeterministicValues.unitDouble(var8, 5815410729529975569L) < 0.7) {
                           double var12 = Math.abs(var10 - 50.0);
                           double var14;
                           if (var12 < 1.0E-9) {
                              var14 = DeterministicValues.unitDouble(var8, 4214826731716357889L) < 0.5 ? -1.0 : 1.0;
                           } else {
                              var14 = var10 < 50.0 ? -1.0 : 1.0;
                           }

                           double var16 = 0.18 + 0.22 * DeterministicValues.unitDouble(var8, 1971462275090437707L);
                           var12 += (50.0 - var12) * var16;
                           var10 = clamp(50.0 + var14 * var12, 0.0, 100.0);
                        }

                        String var24 = TraitAxesRuntime.bodyType(var0);
                        if (var24 == null || var24.isBlank()) {
                           var24 = "normal";
                        }

                        if ("normal".equalsIgnoreCase(var24)) {
                           if (var10 >= 97.0) {
                              double var13 = clamp(var6.probability(FishMutation.GIANT) / 0.03 * 1.75, 0.0, 1.0);
                              if (DeterministicValues.unitDouble(var8, 7650794760523985177L) < var13) {
                                 var24 = "giant";
                              }
                           } else if (var10 <= 3.0) {
                              double var25 = clamp(var6.probability(FishMutation.DWARF) / 0.03 * 1.75, 0.0, 1.0);
                              if (DeterministicValues.unitDouble(var8, 4163329390116612967L) < var25) {
                                 var24 = "dwarf";
                              }
                           }
                        }

                        String var26 = TraitAxesRuntime.condition(var0);
                        if (var26 == null || var26.isBlank()) {
                           var26 = "normal";
                        }

                        if ("normal".equalsIgnoreCase(var26)) {
                           if (DeterministicValues.unitDouble(var8, 8207443346169614273L) < clamp(var6.probability(FishMutation.IRIDESCENT) * 3.0, 0.0, 1.0)) {
                              var26 = "iridescent";
                           } else {
                              TideTraitsConfig.DoubleRange var27 = var6.perfectSpecimenNormalPercentile();
                              double var15 = clamp(var27.minInclusive(), 0.0, 100.0);
                              double var17 = clamp(var27.maxInclusive(), var15, 100.0);
                              double var19 = Math.max(1.0E-6, (var17 - var15) / 100.0);
                              double var21 = clamp(var6.probability(FishMutation.PERFECT_SPECIMEN) / var19 * 2.5, 0.0, 1.0);
                              if (var10 >= var15 && var10 <= var17 && DeterministicValues.unitDouble(var8, 3284796534600278910L) < var21) {
                                 var26 = "perfect_specimen";
                              } else if (DeterministicValues.unitDouble(var8, 5704024059175898085L)
                                 < clamp(var6.probability(FishMutation.ALBINO) * 2.5, 0.0, 1.0)) {
                                 var26 = "albino";
                              } else if (DeterministicValues.unitDouble(var8, 6633366370764118095L)
                                 < clamp(var6.probability(FishMutation.PARASITE_RIDDEN) * 2.0, 0.0, 1.0)) {
                                 var26 = "parasite_ridden";
                              } else if (DeterministicValues.unitDouble(var8, 1708175715983811855L)
                                 < clamp(var6.probability(FishMutation.SCARRED) * 1.5, 0.0, 1.0)) {
                                 var26 = "scarred";
                              }
                           }
                        }

                        var0.set(TideTraitsComponents.BODY_TYPE, var24);
                        var0.set(TideTraitsComponents.MUTATION, var26);
                        var0.set(TideTraitsComponents.SIZE_PERCENTILE, var10);
                        double var28 = ((EmpiricalFishDistribution)var7.get()).lengthAtPercentile(var10);
                        double var29 = TraitAxesRuntime.applyCurrentPhysicalEffects(var0, var28, var6);
                        if (Double.isFinite(var29) && var29 > 0.0) {
                           TideItemData.FISH_LENGTH.set(var0, var29);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }
}
