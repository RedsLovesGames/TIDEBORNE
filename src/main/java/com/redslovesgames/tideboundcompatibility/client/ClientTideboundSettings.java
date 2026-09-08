/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.client;

import net.minecraft.nbt.NbtCompound;

public final class ClientTideboundSettings {
   private static NbtCompound values;

   private ClientTideboundSettings() {
   }

   public static void update(NbtCompound incoming) {
      values = incoming.copy();
   }

   public static boolean available() {
      return values != null;
   }

   public static boolean bool(String key) {
      return values != null && values.getBoolean(key);
   }

   public static double decimal(String key) {
      return values == null ? 0.0 : values.getDouble(key);
   }

   public static int integer(String key) {
      return values == null ? 0 : values.getInt(key);
   }

   public static String settingsJson() {
      return values == null ? "" : values.getString("settings_json");
   }
}
