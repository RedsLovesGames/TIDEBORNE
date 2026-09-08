/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public final class AnglersSatchelRecipeSupport {
   private AnglersSatchelRecipeSupport() {
   }

   public static boolean isAnglersSatchelRecipe(Object var0) {
      try {
         Object var1 = field(var0, "result");
         Class var2 = Class.forName("com.redslovesgames.tidetraits.satchel.SatchelRegistration");
         Method var3 = compatible(var2, "isAnglersSatchel", var1);
         return Boolean.TRUE.equals(var3.invoke(null, var1));
      } catch (Throwable var4) {
         return false;
      }
   }

   public static boolean validThreeStarRing(Object var0) {
      try {
         int var1 = ((Number)invoke(var0, "getSize")).intValue();
         if (var1 != 9) {
            return false;
         }

         Class var2 = Class.forName("com.redslovesgames.tideteamjournal.TeamProgressStore");

         for (int var3 = 0; var3 < 9; var3++) {
            if (var3 != 4) {
               Object var4 = invoke(var0, "getStackInSlot", var3);
               Method var5 = compatible(var2, "tideborneFishStars", var4);
               int var6 = ((Number)var5.invoke(null, var4)).intValue();
               if (var6 < 3) {
                  return false;
               }
            }
         }

         return true;
      } catch (Throwable var7) {
         return false;
      }
   }

   public static Object convertedOutput(Object var0, Object var1) {
      try {
         Object var2 = invoke(var0, "getStackInSlot", 4);
         Class var3 = Class.forName("com.redslovesgames.tidetraits.satchel.SatchelPurchaseService");
         Method var4 = compatible(var3, "convertedCopy", var2);
         if (var4.invoke(null, var2) instanceof Optional var6) {
            return var6.isPresent() ? var6.get() : var1;
         } else {
            return var1;
         }
      } catch (Throwable var7) {
         return var1;
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

   private static Object invoke(Object var0, String var1, Object... var2) throws Exception {
      Method var3 = compatible(var0.getClass(), var1, var2);
      var3.setAccessible(true);
      return var3.invoke(var0, var2);
   }

   private static Method compatible(Class<?> var0, String var1, Object... var2) throws Exception {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2.length) {
               boolean var8 = true;
               Class[] var9 = var7.getParameterTypes();

               for (int var10 = 0; var10 < var9.length; var10++) {
                  if (var2[var10] != null && !wrap(var9[var10]).isInstance(var2[var10])) {
                     var8 = false;
                     break;
                  }
               }

               if (var8) {
                  var7.setAccessible(true);
                  return var7;
               }
            }
         }
      }

      throw new NoSuchMethodException(var0.getName() + "." + var1);
   }

   private static Class<?> wrap(Class<?> var0) {
      if (!var0.isPrimitive()) {
         return var0;
      } else if (var0 == int.class) {
         return Integer.class;
      } else if (var0 == boolean.class) {
         return Boolean.class;
      } else if (var0 == long.class) {
         return Long.class;
      } else if (var0 == double.class) {
         return Double.class;
      } else if (var0 == float.class) {
         return Float.class;
      } else if (var0 == byte.class) {
         return Byte.class;
      } else if (var0 == short.class) {
         return Short.class;
      } else {
         return var0 == char.class ? Character.class : var0;
      }
   }
}
