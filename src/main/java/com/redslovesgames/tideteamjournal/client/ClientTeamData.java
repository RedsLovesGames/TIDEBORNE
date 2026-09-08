/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.client;

import net.minecraft.nbt.NbtCompound;

public final class ClientTeamData {
   private static NbtCompound snapshot = new NbtCompound();

   private ClientTeamData() {
   }

   public static NbtCompound get() {
      return snapshot;
   }

   public static void update(NbtCompound tag) {
      snapshot = tag.copy();
   }
}
