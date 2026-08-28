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
import net.minecraft.network.packet.CustomPayload.class_9154;

public record RecordEventPayload(NbtCompound tag) implements CustomPayload {
   public static final class_9154<RecordEventPayload> TYPE = new class_9154(Identifier.of("tide_team_journal", "record_event"));
   public static final PacketCodec<RegistryByteBuf, RecordEventPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag),
      buffer -> new RecordEventPayload(Objects.requireNonNullElseGet(buffer.readNbt(), NbtCompound::new))
   );

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
