/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.trait;

import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import com.redslovesgames.tidetraits.config.TideTraitsConfig;
import com.redslovesgames.tidetraits.fish.SpecimenSizeService;
import net.minecraft.item.ItemStack;

public final class TraitAxesRuntime {
   private static final long BODY_SIZE_SALT = 8583800735156012005L;
   private static final long PARASITE_SIZE_SALT = -6626703657320631856L;

   private TraitAxesRuntime() {
   }

   /** True when the stack carries the server-authoritative Fishing System 2.0 specimen identity. */
   public static boolean isCanonicalV2(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      }

      Integer schemaVersion = (Integer)stack.get(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION);
      String speciesId = (String)stack.get(TideTraitsComponents.SPECIMEN_SPECIES_ID);
      return schemaVersion != null && schemaVersion >= 2 && speciesId != null && !speciesId.isBlank();
   }

   /**
    * Mirrors the canonical Body Type to the legacy component without rolling or reclassifying it.
    * Interim V2 stacks created before the canonical component existed are migrated by copying the
    * already-persisted legacy Body Type once.
    */
   public static void mirrorCanonicalBodyType(ItemStack stack) {
      if (!isCanonicalV2(stack)) {
         return;
      }

      String canonical = normalizedBodyType((String)stack.get(TideTraitsComponents.SPECIMEN_BODY_TYPE));
      if (canonical == null) {
         canonical = normalizedBodyType((String)stack.get(TideTraitsComponents.BODY_TYPE));
         if (canonical == null) {
            canonical = "normal";
         }
         stack.set(TideTraitsComponents.SPECIMEN_BODY_TYPE, canonical);
      }
      stack.set(TideTraitsComponents.BODY_TYPE, canonical);
   }

   public static String bodyType(ItemStack var0) {
      if (isCanonicalV2(var0)) {
         mirrorCanonicalBodyType(var0);
         String canonical = normalizedBodyType((String)var0.get(TideTraitsComponents.SPECIMEN_BODY_TYPE));
         return canonical == null ? "normal" : canonical;
      }

      String var1 = (String)var0.getOrDefault(TideTraitsComponents.BODY_TYPE, "normal");
      if (var1 == null || var1.isBlank()) {
         var1 = "normal";
      }

      String var2 = (String)var0.getOrDefault(TideTraitsComponents.MUTATION, "normal");
      return !"giant".equals(var2) && !"dwarf".equals(var2) ? var1 : var2;
   }

   public static String condition(ItemStack var0) {
      String var1 = (String)var0.getOrDefault(TideTraitsComponents.MUTATION, "normal");
      if (var1 == null || var1.isBlank()) {
         return "normal";
      } else {
         return !"giant".equals(var1) && !"dwarf".equals(var1) ? var1 : "normal";
      }
   }

   public static void migrateLegacy(ItemStack var0) {
      if (isCanonicalV2(var0)) {
         mirrorCanonicalBodyType(var0);
         return;
      }

      String var1 = (String)var0.getOrDefault(TideTraitsComponents.MUTATION, "normal");
      if ("giant".equals(var1) || "dwarf".equals(var1)) {
         var0.set(TideTraitsComponents.BODY_TYPE, var1);
         var0.set(TideTraitsComponents.MUTATION, "normal");
      } else if (var0.get(TideTraitsComponents.BODY_TYPE) == null) {
         var0.set(TideTraitsComponents.BODY_TYPE, "normal");
      }
   }

   /*
    * The old normalizeNew path was removed in Stage 50. It contained the P97 Giant/P3 Dwarf gates,
    * legacy Perfect Specimen percentile gate, and a second body-size multiplier. New catches are
    * canonical V2 before legacy hooks run, and old-world migration uses persisted values instead.
    */

   public static double bodyMultiplier(String var0, long var1, TideTraitsConfig var3) {
      if (var0 != null && var3 != null) {
         double var4 = DeterministicValues.unitDouble(var1, BODY_SIZE_SALT);
         if ("giant".equalsIgnoreCase(var0)) {
            double var10 = var3.giantLengthMultiplier().minInclusive();
            double var11 = var3.giantLengthMultiplier().maxInclusive();
            if (Math.abs(var10 - 1.2) < 1.0E-9 && Math.abs(var11 - 1.45) < 1.0E-9) {
               var10 = 1.08;
               var11 = 1.3;
            }

            return var10 + (var11 - var10) * var4;
         } else if ("dwarf".equalsIgnoreCase(var0)) {
            double var6 = var3.dwarfLengthMultiplier().minInclusive();
            double var8 = var3.dwarfLengthMultiplier().maxInclusive();
            if (Math.abs(var6 - 0.65) < 1.0E-9 && Math.abs(var8 - 0.82) < 1.0E-9) {
               var6 = 0.55;
               var8 = 0.8;
            }

            return var6 + (var8 - var6) * var4;
         } else {
            return 1.0;
         }
      } else {
         return 1.0;
      }
   }

   public static double conditionSizeMultiplier(String var0, long var1, TideTraitsConfig var3) {
      if (var0 == null || var3 == null) {
         return 1.0;
      }

      if (!"parasite".equalsIgnoreCase(var0) && !"parasite_ridden".equalsIgnoreCase(var0)) {
         return 1.0;
      }

      double var4 = DeterministicValues.unitDouble(var1, PARASITE_SIZE_SALT);
      double var6 = var3.parasiteLengthMultiplier().minInclusive();
      double var8 = var3.parasiteLengthMultiplier().maxInclusive();
      return var6 + (var8 - var6) * var4;
   }

   public static long identitySeed(ItemStack var0) {
      return var0.get(TideTraitsComponents.MUTATION_SEED) instanceof Long var2 ? var2 : 0L;
   }

   public static double applyBodyEffects(ItemStack var0, double var1, TideTraitsConfig var3) {
      if (isCanonicalV2(var0)) {
         return var1;
      }
      if (Double.isFinite(var1) && !(var1 <= 0.0)) {
         double var4 = bodyMultiplier(bodyType(var0), identitySeed(var0), var3);
         return Double.isFinite(var4) && var4 > 0.0 ? var1 * var4 : var1;
      } else {
         return var1;
      }
   }

   public static double recoverBodyNormalLength(ItemStack var0, double var1, TideTraitsConfig var3) {
      if (isCanonicalV2(var0)) {
         return var1;
      }
      if (Double.isFinite(var1) && !(var1 <= 0.0)) {
         double var4 = bodyMultiplier(bodyType(var0), identitySeed(var0), var3);
         return Double.isFinite(var4) && var4 > 0.0 ? var1 / var4 : var1;
      } else {
         return var1;
      }
   }

   public static FishMutation conditionMutationForEdit(ItemStack var0, FishMutation var1, boolean var2) {
      if (var2) {
         return FishMutation.NORMAL;
      } else if (var1 != FishMutation.GIANT && var1 != FishMutation.DWARF) {
         return var1 == null ? FishMutation.NORMAL : var1;
      } else {
         return FishMutation.bySerializedName(condition(var0)).orElse(FishMutation.NORMAL);
      }
   }

   public static String bodyTypeForEdit(ItemStack var0, FishMutation var1, boolean var2) {
      if (isCanonicalV2(var0)) {
         return bodyType(var0);
      }
      if (var2) {
         return "normal";
      } else {
         return var1 != FishMutation.GIANT && var1 != FishMutation.DWARF ? bodyType(var0) : var1.serializedName();
      }
   }

   public static String conditionForEdit(ItemStack var0, FishMutation var1, boolean var2) {
      return conditionMutationForEdit(var0, var1, var2).serializedName();
   }

   public static SpecimenSizeService.AppliedSize finishAdminEdit(
      ItemStack var0, FishMutation var1, boolean var2, SpecimenSizeService.AppliedSize var3, TideTraitsConfig var4
   ) {
      if (isCanonicalV2(var0)) {
         mirrorCanonicalBodyType(var0);
         return var3;
      }

      String var5 = bodyTypeForEdit(var0, var1, var2);
      String var6 = conditionForEdit(var0, var1, var2);
      var0.set(TideTraitsComponents.BODY_TYPE, var5);
      var0.set(TideTraitsComponents.MUTATION, var6);
      double var7 = var3.finalPhysicalLengthCm();
      if (Double.isFinite(var7) && var7 > 0.0) {
         var7 *= bodyMultiplier(var5, var3.specimen().identitySeed(), var4);
      }

      return new SpecimenSizeService.AppliedSize(var3.specimen(), var7);
   }

   public static double physicalMultiplier(ItemStack var0, TideTraitsConfig var1) {
      if (isCanonicalV2(var0)) {
         return 1.0;
      }
      long var2 = identitySeed(var0);
      return bodyMultiplier(bodyType(var0), var2, var1) * conditionSizeMultiplier(condition(var0), var2, var1);
   }

   public static double applyCurrentPhysicalEffects(ItemStack var0, double var1, TideTraitsConfig var3) {
      if (isCanonicalV2(var0)) {
         return var1;
      }
      if (Double.isFinite(var1) && !(var1 <= 0.0)) {
         double var4 = physicalMultiplier(var0, var3);
         return Double.isFinite(var4) && var4 > 0.0 ? var1 * var4 : var1;
      } else {
         return var1;
      }
   }

   public static double recoverCurrentNormalLength(ItemStack var0, double var1, TideTraitsConfig var3) {
      if (isCanonicalV2(var0)) {
         return var1;
      }
      if (Double.isFinite(var1) && !(var1 <= 0.0)) {
         double var4 = physicalMultiplier(var0, var3);
         return Double.isFinite(var4) && var4 > 0.0 ? var1 / var4 : var1;
      } else {
         return var1;
      }
   }

   public static double conditionProbabilityTotal(TideTraitsConfig var0) {
      return safeProbability(var0, FishMutation.SCARRED)
         + safeProbability(var0, FishMutation.PARASITE_RIDDEN)
         + safeProbability(var0, FishMutation.ALBINO)
         + safeProbability(var0, FishMutation.IRIDESCENT);
   }

   private static double safeProbability(TideTraitsConfig var0, FishMutation var1) {
      try {
         double var2 = var0.probability(var1);
         return Double.isFinite(var2) && var2 > 0.0 ? var2 : 0.0;
      } catch (RuntimeException var4) {
         return 0.0;
      }
   }

   public static boolean isSpecial(ItemStack var0) {
      return !"normal".equals(bodyType(var0)) || !"normal".equals(condition(var0));
   }

   public static double conditionBonus(String var0) {
      if (var0 == null) {
         return 0.0;
      }

      return switch (var0.toLowerCase()) {
         case "parasite", "parasite_ridden" -> 15.0;
         case "scarred" -> 25.0;
         case "albino" -> 175.0;
         case "iridescent" -> 325.0;
         case "perfect_specimen" -> 350.0;
         default -> 0.0;
      };
   }

   public static double bodyTypeBonus(String var0, double var1, double var3, double var5) {
      if (var0 == null) {
         return 0.0;
      } else if ("giant".equalsIgnoreCase(var0)) {
         double var13 = var5 > 0.0 ? var3 / var5 : 1.0;
         double var9 = clamp((var13 - 1.0) / 0.3, 0.0, 1.0);
         double var11 = clamp((var1 - 97.0) / 3.0, 0.0, 1.0);
         return 80.0 + 140.0 * var9 + 80.0 * var11;
      } else if ("dwarf".equalsIgnoreCase(var0)) {
         double var7 = var1 >= 0.0 ? clamp((3.0 - var1) / 3.0, 0.0, 1.0) : 0.0;
         return 80.0 + 220.0 * var7;
      } else {
         return 0.0;
      }
   }

   public static double perfectMultiplier(String var0) {
      return "perfect_specimen".equalsIgnoreCase(var0) ? 1.2 : 1.0;
   }

   public static double score(ItemStack var0, int var1, double var2, double var4, double var6) {
      return scoreFromParts(var2, var1, condition(var0), bodyType(var0), var4, var6);
   }

   public static double scoreFromParts(double var0, int var2, String var3, String var4, double var5, double var7) {
      double var9 = clamp(Double.isFinite(var0) ? var0 : 0.0, 0.0, 100.0);
      int var11 = Math.max(1, var2);
      double var12 = (var11 - 1) * 62.5;
      double var14 = var7 > 0.0 && Double.isFinite(var7) ? Math.min(300.0, 75.0 * Math.sqrt(var7 / 100.0)) : 0.0;
      double var16 = var5 > 0.0 && Double.isFinite(var5) ? Math.min(150.0, 15.0 * (var5 / 100.0)) : 0.0;
      double var18 = var9 * 5.0 + var12 + conditionBonus(var3) + var14 + var16 + bodyTypeBonus(var4, var9, var5, var7);
      return var18 * perfectMultiplier(var3);
   }

   public static String traitSummary(ItemStack var0) {
      return traitSummaryParts(bodyType(var0), condition(var0));
   }

   public static String traitSummaryParts(String var0, String var1) {
      if (var0 == null || var0.isBlank()) {
         var0 = "normal";
      }

      if (var1 == null || var1.isBlank()) {
         var1 = "normal";
      }

      if ("giant".equals(var1) || "dwarf".equals(var1)) {
         if ("normal".equals(var0)) {
            var0 = var1;
         }

         var1 = "normal";
      }

      boolean var2 = "normal".equals(var0);
      boolean var3 = "normal".equals(var1);
      if (var2 && var3) {
         return "Normal";
      } else if (var3) {
         return titleCase(var0);
      } else {
         return var2 ? titleCase(var1) : titleCase(var0) + " / " + titleCase(var1);
      }
   }

   public static String titleCase(String var0) {
      if (var0 != null && !var0.isBlank()) {
         if ("mutation_rarity".equalsIgnoreCase(var0)) {
            return "Condition Rarity";
         }

         if ("mutation".equalsIgnoreCase(var0)) {
            return "Condition";
         }

         if ("mutations".equalsIgnoreCase(var0)) {
            return "Traits";
         }

         String[] var1 = var0.replace('-', '_').split("_");
         StringBuilder var2 = new StringBuilder();

         for (String var6 : var1) {
            if (!var6.isEmpty()) {
               if (var2.length() > 0) {
                  var2.append(' ');
               }

               var2.append(Character.toUpperCase(var6.charAt(0)));
               if (var6.length() > 1) {
                  var2.append(var6.substring(1).toLowerCase());
               }
            }
         }

         return var2.length() == 0 ? "Normal" : var2.toString();
      } else {
         return "Normal";
      }
   }

   private static String normalizedBodyType(String value) {
      if (value == null || value.isBlank()) {
         return null;
      }

      return switch (value.toLowerCase()) {
         case "normal" -> "normal";
         case "giant" -> "giant";
         case "dwarf" -> "dwarf";
         default -> null;
      };
   }

   private static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }
}
