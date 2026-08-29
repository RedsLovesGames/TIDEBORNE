/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import java.lang.reflect.Method;
import net.minecraft.item.ItemStack;

public final class SteelLeaderAttachment {
   private SteelLeaderAttachment() {
   }

   private static Object component() {
      try {
         Class var0 = Class.forName("com.redslovesgames.tidetraits.component.TideTraitsComponents");
         return var0.getField("STEEL_LEADER_ATTACHED").get(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   public static boolean has(Object var0) {
      if (var0 == null) {
         return false;
      }

      if (var0 instanceof ItemStack stack) {
         return Boolean.TRUE.equals(stack.get(TideTraitsComponents.STEEL_LEADER_ATTACHED));
      }

      Object var1 = component();
      if (var1 == null) {
         return false;
      }

      try {
         Object var2 = invoke(var0, "get", var1);
         return Boolean.TRUE.equals(var2);
      } catch (Throwable var3) {
         return false;
      }
   }

   public static void set(Object var0, boolean var1) {
      if (var0 == null) {
         return;
      }

      if (var0 instanceof ItemStack stack) {
         if (var1) {
            stack.set(TideTraitsComponents.STEEL_LEADER_ATTACHED, Boolean.TRUE);
         } else {
            stack.remove(TideTraitsComponents.STEEL_LEADER_ATTACHED);
         }
         return;
      }

      Object var2 = component();
      if (var2 != null) {
         try {
            if (var1) {
               invoke(var0, "set", var2, Boolean.TRUE);
            } else {
               invoke(var0, "remove", var2);
            }
         } catch (Throwable var4) {
         }
      }
   }

   public static boolean isSteelLeaderStack(Object var0) {
      if (var0 == null) {
         return false;
      }

      Object var1 = steelLeaderItem();
      if (var1 == null) {
         return false;
      }

      try {
         return Boolean.TRUE.equals(invoke(var0, "isOf", var1));
      } catch (Throwable var3) {
         return false;
      }
   }

   public static boolean hasOnHook(Object var0) {
      if (var0 == null) {
         return false;
      }

      try {
         Object var1 = invoke(var0, "getRodItem");
         if (has(var1)) {
            return true;
         }

         Class var2 = Class.forName("com.li64.tide.data.rods.CustomRodManager");
         Object var3 = invokeStatic(var2, "getLine", var1);
         return isSteelLeaderStack(var3);
      } catch (Throwable var4) {
         return false;
      }
   }

   public static Object steelLeaderItem() {
      try {
         Class var0 = Class.forName("com.redslovesgames.tideboundcompatibility.registry.TideboundItems");
         return var0.getField("STEEL_LEADER").get(null);
      } catch (Throwable var1) {
         return null;
      }
   }

   static Object invoke(Object var0, String var1, Object... var2) throws Exception {
      Method var3 = findMethod(var0.getClass(), var1, var2);
      var3.setAccessible(true);
      return var3.invoke(var0, var2);
   }

   static Object invokeStatic(Class<?> var0, String var1, Object... var2) throws Exception {
      Method var3 = findMethod(var0, var1, var2);
      var3.setAccessible(true);
      return var3.invoke(null, var2);
   }

   static Method findMethod(Class<?> var0, String var1, Object... var2) throws NoSuchMethodException {
      for (Class var3 = var0; var3 != null; var3 = var3.getSuperclass()) {
         for (Method var7 : var3.getDeclaredMethods()) {
            if (var7.getName().equals(var1) && var7.getParameterCount() == var2.length) {
               Class[] var8 = var7.getParameterTypes();
               boolean var9 = true;

               for (int var10 = 0; var10 < var8.length; var10++) {
                  if (var2[var10] != null) {
                     Class var11 = wrap(var8[var10]);
                     if (!var11.isInstance(var2[var10])) {
                        var9 = false;
                        break;
                     }
                  }
               }

               if (var9) {
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
      } else if (var0 == boolean.class) {
         return Boolean.class;
      } else if (var0 == int.class) {
         return Integer.class;
      } else if (var0 == long.class) {
         return Long.class;
      } else if (var0 == float.class) {
         return Float.class;
      } else if (var0 == double.class) {
         return Double.class;
      } else if (var0 == byte.class) {
         return Byte.class;
      } else if (var0 == short.class) {
         return Short.class;
      } else {
         return var0 == char.class ? Character.class : var0;
      }
   }
}
