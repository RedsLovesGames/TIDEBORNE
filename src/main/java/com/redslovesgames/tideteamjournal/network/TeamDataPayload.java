/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.network;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record TeamDataPayload(NbtCompound tag) implements CustomPayload {
   public static final Id<TeamDataPayload> TYPE = new Id(Identifier.of("tide_team_journal", "team_data"));
   public static final PacketCodec<RegistryByteBuf, TeamDataPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag),
      buffer -> new TeamDataPayload(Objects.requireNonNullElseGet(buffer.readNbt(), NbtCompound::new))
   );

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
