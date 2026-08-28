/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class AnglingTableLeaderSupport {
   private AnglingTableLeaderSupport() {
   }

   public static Object createSlotDefinition() {
      try {
         Class var0 = Class.forName("net.minecraft.screen.slot.ForgingSlotsManager");
         Object var1 = var0.getMethod("create").invoke(null);
         Method var2 = var1.getClass().getMethod("input", int.class, int.class, int.class, Predicate.class);
         Method var3 = var1.getClass().getMethod("output", int.class, int.class, int.class);
         Method var4 = var1.getClass().getMethod("build");
         var2.invoke(var1, 0, 26, 11, (Predicate<Object>)var0x -> inTag(var0x, "FISHING_RODS"));
         var2.invoke(var1, 1, 134, 8, (Predicate<Object>)var0x -> inTag(var0x, "LINES") && !SteelLeaderAttachment.isSteelLeaderStack(var0x));
         var2.invoke(var1, 2, 134, 32, (Predicate<Object>)var0x -> inTag(var0x, "BOBBERS"));
         var2.invoke(var1, 3, 134, 56, (Predicate<Object>)var0x -> inTag(var0x, "HOOKS"));
         var2.invoke(var1, 4, 110, 56, SteelLeaderAttachment::isSteelLeaderStack);
         var3.invoke(var1, 5, 26, 49);
         return var4.invoke(var1);
      } catch (Throwable var5) {
         throw new RuntimeException("Unable to create Tideborne angling-table slots", var5);
      }
   }

   public static void beforeUpdate(Object var0) {
      try {
         Object var1 = field(var0, "input");
         Object var2 = invGet(var1, 0);
         if (!isEmpty(var2)) {
            Object var3 = invGet(var1, 4);

            try {
               Class var4 = Class.forName("com.li64.tide.data.rods.CustomRodManager");
               Object var5 = SteelLeaderAttachment.invokeStatic(var4, "getLine", var2);
               if (SteelLeaderAttachment.isSteelLeaderStack(var5)) {
                  SteelLeaderAttachment.invokeStatic(var4, "setLine", var2, null);
                  if (isEmpty(var3)) {
                     invSet(var1, 4, newLeaderStack());
                     var3 = invGet(var1, 4);
                  } else {
                     SteelLeaderAttachment.set(var2, true);
                  }
               }
            } catch (Throwable var6) {
            }

            if (SteelLeaderAttachment.has(var2) && isEmpty(var3)) {
               SteelLeaderAttachment.set(var2, false);
               invSet(var1, 4, newLeaderStack());
            }
         }
      } catch (Throwable var7) {
         throw new RuntimeException("Steel Leader pre-update failed", var7);
      }
   }

   public static void afterUpdate(Object var0) {
      try {
         Object var1 = field(var0, "input");
         Object var2 = field(var0, "output");
         Object var3 = invGet(var1, 0);
         if (!isEmpty(var3)) {
            Object var4 = invGet(var1, 4);
            if (!isEmpty(var4)) {
               Object var5 = invGet(var2, 0);
               if (isEmpty(var5)) {
                  var5 = copy(var3);
               }

               SteelLeaderAttachment.set(var5, true);
               invSet(var2, 0, var5);
            }
         }
      } catch (Throwable var6) {
         throw new RuntimeException("Steel Leader result update failed", var6);
      }
   }

   public static void drawLeaderSlot(Object var0, Object var1) {
      try {
         int var2 = ((Number)field(var1, "x")).intValue();
         int var3 = ((Number)field(var1, "y")).intValue();
         fill(var0, var2 + 109, var3 + 55, var2 + 127, var3 + 73, -12963032);
         fill(var0, var2 + 110, var3 + 56, var2 + 126, var3 + 72, -7503766);
         fill(var0, var2 + 111, var3 + 57, var2 + 126, var3 + 72, -2700883);
         fill(var0, var2 + 111, var3 + 57, var2 + 125, var3 + 58, -990009);
      } catch (Throwable var4) {
      }
   }

   private static void fill(Object var0, int var1, int var2, int var3, int var4, int var5) throws Exception {
      SteelLeaderAttachment.invoke(var0, "fill", var1, var2, var3, var4, var5);
   }

   private static boolean inTag(Object var0, String var1) {
      try {
         Class var2 = Class.forName("com.li64.tide.data.TideTags$Items");
         Object var3 = var2.getField(var1).get(null);
         return Boolean.TRUE.equals(SteelLeaderAttachment.invoke(var0, "isIn", var3));
      } catch (Throwable var4) {
         return false;
      }
   }

   private static Object field(Object var0, String var1) throws Exception {
      for (Class var2 = var0.getClass(); var2 != null; var2 = var2.getSuperclass()) {
         try {
            Field var3 = var2.getDeclaredField(var1);
            var3.setAccessible(true);
            return var3.get(var0);
         } catch (NoSuchFieldException var4) {
         }
      }

      throw new NoSuchFieldException(var1);
   }

   private static Object invGet(Object var0, int var1) throws Exception {
      return SteelLeaderAttachment.invoke(var0, "getStack", var1);
   }

   private static void invSet(Object var0, int var1, Object var2) throws Exception {
      SteelLeaderAttachment.invoke(var0, "setStack", var1, var2);
   }

   private static boolean isEmpty(Object var0) {
      try {
         return Boolean.TRUE.equals(SteelLeaderAttachment.invoke(var0, "isEmpty"));
      } catch (Throwable var2) {
         return true;
      }
   }

   private static Object copy(Object var0) throws Exception {
      return SteelLeaderAttachment.invoke(var0, "copy");
   }

   private static Object newLeaderStack() throws Exception {
      Object var0 = SteelLeaderAttachment.steelLeaderItem();
      Class var1 = Class.forName("net.minecraft.item.ItemStack");

      for (Constructor var5 : var1.getConstructors()) {
         if (var5.getParameterCount() == 1 && var5.getParameterTypes()[0].isInstance(var0)) {
            return var5.newInstance(var0);
         }
      }

      for (Constructor var9 : var1.getDeclaredConstructors()) {
         if (var9.getParameterCount() == 1 && var9.getParameterTypes()[0].isAssignableFrom(var0.getClass())) {
            var9.setAccessible(true);
            return var9.newInstance(var0);
         }
      }

      throw new IllegalStateException("No ItemStack constructor for Steel Leader");
   }
}
