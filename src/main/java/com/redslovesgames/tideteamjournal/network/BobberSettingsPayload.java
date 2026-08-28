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

public record BobberSettingsPayload(NbtCompound tag) implements CustomPayload {
   public static final class_9154<BobberSettingsPayload> TYPE = new class_9154(Identifier.of("tide_team_journal", "bobber_settings"));
   public static final PacketCodec<RegistryByteBuf, BobberSettingsPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag),
      buffer -> new BobberSettingsPayload(Objects.requireNonNullElseGet(buffer.readNbt(), NbtCompound::new))
   );

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
