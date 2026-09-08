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
import net.minecraft.network.packet.CustomPayload.Id;

public record OpenTeamRecordsPayload() implements CustomPayload {
   public static final Id<OpenTeamRecordsPayload> TYPE = new Id(Identifier.of("tide_team_journal", "open_team_records"));
   public static final PacketCodec<RegistryByteBuf, OpenTeamRecordsPayload> CODEC = PacketCodec.unit(new OpenTeamRecordsPayload());

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
