/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.network;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNetworkIds;

import com.redslovesgames.tideborne.journal.JournalSpecimenNetworkCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record RecordHoldersPayload(NbtCompound tag) implements CustomPayload {
   public static final Id<RecordHoldersPayload> TYPE = new Id(LegacyNetworkIds.RECORD_HOLDERS);
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
