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

public record TideboundSettingsResultPayload(boolean success, String message) implements CustomPayload {
   public static final class_9154<TideboundSettingsResultPayload> TYPE = new class_9154(TideboundCompatibility.id("settings_result"));
   public static final PacketCodec<RegistryByteBuf, TideboundSettingsResultPayload> CODEC = PacketCodec.ofStatic((buffer, payload) -> {
      buffer.writeBoolean(payload.success);
      buffer.writeString(payload.message, 1024);
   }, buffer -> new TideboundSettingsResultPayload(buffer.readBoolean(), buffer.readString(1024)));

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
