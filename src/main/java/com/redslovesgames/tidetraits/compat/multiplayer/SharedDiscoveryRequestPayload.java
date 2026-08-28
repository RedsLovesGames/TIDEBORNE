/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record SharedDiscoveryRequestPayload(int protocolVersion) implements CustomPayload {
   public static final int CURRENT_PROTOCOL = 1;
   public static final Id<SharedDiscoveryRequestPayload> TYPE = new Id(Identifier.of("tide_traits", "shared_discovery_request"));
   public static final PacketCodec<RegistryByteBuf, SharedDiscoveryRequestPayload> STREAM_CODEC = new PacketCodec<RegistryByteBuf, SharedDiscoveryRequestPayload>() {
      public SharedDiscoveryRequestPayload decode(RegistryByteBuf buffer) {
         return new SharedDiscoveryRequestPayload(buffer.readVarInt());
      }

      public void encode(RegistryByteBuf buffer, SharedDiscoveryRequestPayload payload) {
         buffer.writeVarInt(payload.protocolVersion);
      }
   };

   public static SharedDiscoveryRequestPayload current() {
      return new SharedDiscoveryRequestPayload(1);
   }

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
