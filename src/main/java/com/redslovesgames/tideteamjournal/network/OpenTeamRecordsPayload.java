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

public record OpenTeamRecordsPayload() implements CustomPayload {
   public static final class_9154<OpenTeamRecordsPayload> TYPE = new class_9154(Identifier.of("tide_team_journal", "open_team_records"));
   public static final PacketCodec<RegistryByteBuf, OpenTeamRecordsPayload> CODEC = PacketCodec.unit(new OpenTeamRecordsPayload());

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
