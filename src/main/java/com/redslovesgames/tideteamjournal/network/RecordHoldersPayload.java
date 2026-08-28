/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideteamjournal.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.class_9154;

public record RecordHoldersPayload(NbtCompound tag) implements CustomPayload {
   public static final class_9154<RecordHoldersPayload> TYPE = new class_9154(Identifier.of("tide_team_journal", "record_holders"));
   public static final PacketCodec<RegistryByteBuf, RecordHoldersPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag), buffer -> {
         NbtCompound tag = buffer.readNbt();
         return new RecordHoldersPayload(tag == null ? new NbtCompound() : tag);
      }
   );

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
