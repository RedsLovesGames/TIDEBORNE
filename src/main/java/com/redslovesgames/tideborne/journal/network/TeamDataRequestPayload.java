/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.network;

import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record TeamDataRequestPayload(int page, String metric, String fishFilter, String eventType) implements CustomPayload {
   public static final Id<TeamDataRequestPayload> TYPE = new Id(Identifier.of("tide_team_journal", "team_data_request"));
   public static final PacketCodec<RegistryByteBuf, TeamDataRequestPayload> CODEC = PacketCodec.ofStatic((buffer, payload) -> {
      buffer.writeVarInt(payload.page);
      buffer.writeString(payload.metric, 32);
      buffer.writeString(payload.fishFilter, 256);
      buffer.writeString(payload.eventType, 32);
   }, buffer -> new TeamDataRequestPayload(buffer.readVarInt(), buffer.readString(32), buffer.readString(256), buffer.readString(32)));

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
