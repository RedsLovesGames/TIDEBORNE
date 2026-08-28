/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.network;

import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.class_9154;

public record TideboundSettingsUpdatePayload(String json) implements CustomPayload {
   public static final class_9154<TideboundSettingsUpdatePayload> TYPE = new class_9154(TideboundCompatibility.id("settings_update"));
   public static final PacketCodec<RegistryByteBuf, TideboundSettingsUpdatePayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeString(payload.json, 16384), buffer -> new TideboundSettingsUpdatePayload(buffer.readString(16384))
   );

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
