/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.network;

import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.class_9154;

public record TeamDataRequestPayload(int page, String metric, String fishFilter, String eventType) implements CustomPayload {
   public static final class_9154<TeamDataRequestPayload> TYPE = new class_9154(Identifier.of("tide_team_journal", "team_data_request"));
   public static final PacketCodec<RegistryByteBuf, TeamDataRequestPayload> CODEC = PacketCodec.ofStatic((buffer, payload) -> {
      buffer.writeVarInt(payload.page);
      buffer.writeString(payload.metric, 32);
      buffer.writeString(payload.fishFilter, 256);
      buffer.writeString(payload.eventType, 32);
   }, buffer -> new TeamDataRequestPayload(buffer.readVarInt(), buffer.readString(32), buffer.readString(256), buffer.readString(32)));

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
