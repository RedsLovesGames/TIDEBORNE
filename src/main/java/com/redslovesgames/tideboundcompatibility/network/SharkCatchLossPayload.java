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

public record SharkCatchLossPayload() implements CustomPayload {
   public static final class_9154<SharkCatchLossPayload> TYPE = new class_9154(TideboundCompatibility.id("shark_catch_loss"));
   public static final PacketCodec<RegistryByteBuf, SharkCatchLossPayload> CODEC = PacketCodec.unit(new SharkCatchLossPayload());

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
