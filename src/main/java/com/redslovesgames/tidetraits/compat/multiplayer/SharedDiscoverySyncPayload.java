/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.compat.multiplayer;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.class_9154;

public record SharedDiscoverySyncPayload(SharedDiscoverySnapshot snapshot) implements CustomPayload {
   public static final class_9154<SharedDiscoverySyncPayload> TYPE = new class_9154(Identifier.of("tide_traits", "shared_discovery_sync"));
   private static final PacketCodec<RegistryByteBuf, NbtCompound> TAG_CODEC = PacketCodecs.NBT_COMPOUND.cast();
   public static final PacketCodec<RegistryByteBuf, SharedDiscoverySyncPayload> STREAM_CODEC = PacketCodec.tuple(
      TAG_CODEC, payload -> SharedDiscoveryNbt.encodePacket(payload.snapshot), tag -> new SharedDiscoverySyncPayload(SharedDiscoveryNbt.decodePacket(tag))
   );

   public SharedDiscoverySyncPayload {
      Objects.requireNonNull(snapshot, "snapshot");
   }

   public class_9154<? extends CustomPayload> getId() {
      return TYPE;
   }
}
