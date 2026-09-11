/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.network;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNetworkIds;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record RecordEventPayload(NbtCompound tag) implements CustomPayload {
   public static final Id<RecordEventPayload> TYPE = new Id(LegacyNetworkIds.RECORD_EVENT);
   public static final PacketCodec<RegistryByteBuf, RecordEventPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag),
      buffer -> new RecordEventPayload(Objects.requireNonNullElseGet(buffer.readNbt(), NbtCompound::new))
   );

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
