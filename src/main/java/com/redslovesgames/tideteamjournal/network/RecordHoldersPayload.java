/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.network;

import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenNetworkCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record RecordHoldersPayload(NbtCompound tag) implements CustomPayload {
   public static final Id<RecordHoldersPayload> TYPE = new Id(Identifier.of("tide_team_journal", "record_holders"));
   public static final PacketCodec<RegistryByteBuf, RecordHoldersPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag), buffer -> {
         NbtCompound tag = buffer.readNbt();
         return new RecordHoldersPayload(tag == null ? new NbtCompound() : tag);
      }
   );

   public RecordHoldersPayload {
      tag = JournalSpecimenNetworkCodec.sanitizeRecordHoldersPayload(tag);
   }

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
