/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.network;

import com.redslovesgames.tideborne.compat.TideboundCompatibility;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record TideboundSettingsUpdatePayload(String json) implements CustomPayload {
   public static final Id<TideboundSettingsUpdatePayload> TYPE = new Id(TideboundCompatibility.id("settings_update"));
   public static final PacketCodec<RegistryByteBuf, TideboundSettingsUpdatePayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeString(payload.json, 16384), buffer -> new TideboundSettingsUpdatePayload(buffer.readString(16384))
   );

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
