/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.nbt.NbtCompound;

public final class HistoryBadgeMeta {
   private static final ThreadLocal<NbtCompound> CURRENT = new ThreadLocal<>();
   private static final ThreadLocal<NbtCompound> LAST = new ThreadLocal<>();
   private static final ConcurrentMap<UUID, NbtCompound> EVENT_META = new ConcurrentHashMap<>();

   private HistoryBadgeMeta() {
   }

   public static void capture(NbtCompound var0) {
      if (var0 != null) {
         CURRENT.set(extract(var0));
      }
   }

   public static void finish() {
      NbtCompound var0 = CURRENT.get();
      if (var0 != null) {
         LAST.set(var0);
      }

      CURRENT.remove();
   }

   public static void bindCurrent(UUID var0, String var1) {
      if (!"REPAIR".equals(var1)) {
         if (var0 != null && !EVENT_META.containsKey(var0)) {
            NbtCompound var2 = CURRENT.get();
            if (var2 == null) {
               var2 = LAST.get();
            }

            if (var2 != null) {
               EVENT_META.put(var0, var2);
            }
         }
      }
   }

   public static void register(UUID var0, NbtCompound var1) {
      if (var0 != null && var1 != null) {
         if (hasAny(var1)) {
            EVENT_META.put(var0, extract(var1));
         }
      }
   }

   public static void write(UUID var0, NbtCompound var1) {
      if (var0 != null && var1 != null) {
         NbtCompound var2 = EVENT_META.get(var0);
         if (var2 != null) {
            String var3 = var2.getString("condition");
            String var4 = var2.getString("body_type");
            if (!var3.isEmpty()) {
               var1.putString("condition", var3);
               var1.putString("mutation", var3);
            }

            if (!var4.isEmpty()) {
               var1.putString("body_type", var4);
            }

            if (var2.contains("percentile", 6)) {
               var1.putDouble("percentile", var2.getDouble("percentile"));
            }
         }
      }
   }

   private static boolean hasAny(NbtCompound var0) {
      return !var0.getString("condition").isEmpty()
         || !var0.getString("mutation").isEmpty()
         || !var0.getString("body_type").isEmpty()
         || var0.contains("percentile", 6);
   }

   private static NbtCompound extract(NbtCompound var0) {
      NbtCompound var1 = new NbtCompound();
      String var2 = var0.getString("condition");
      if (var2.isEmpty()) {
         var2 = var0.getString("mutation");
      }

      String var3 = var0.getString("body_type");
      if (!var2.isEmpty()) {
         var1.putString("condition", var2);
      }

      if (!var3.isEmpty()) {
         var1.putString("body_type", var3);
      }

      if (var0.contains("percentile", 6)) {
         var1.putDouble("percentile", var0.getDouble("percentile"));
      }

      return var1;
   }
}
