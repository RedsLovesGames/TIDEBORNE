/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.network;

import com.redslovesgames.tideborne.fishing.FishingGameplayInitializer;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record SharkCatchLossPayload() implements CustomPayload {
   public static final Id<SharkCatchLossPayload> TYPE = new Id(FishingGameplayInitializer.id("shark_catch_loss"));
   public static final PacketCodec<RegistryByteBuf, SharkCatchLossPayload> CODEC = PacketCodec.unit(new SharkCatchLossPayload());

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
