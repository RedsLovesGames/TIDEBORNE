/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.discovery;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNetworkIds;

import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record DiscoverySyncPayload(DiscoverySnapshot snapshot) implements CustomPayload {
   public static final Id<DiscoverySyncPayload> TYPE = new Id(LegacyNetworkIds.DISCOVERY_SYNC);
   private static final PacketCodec<RegistryByteBuf, NbtCompound> TAG_CODEC = PacketCodecs.NBT_COMPOUND.cast();
   public static final PacketCodec<RegistryByteBuf, DiscoverySyncPayload> STREAM_CODEC = PacketCodec.tuple(
      TAG_CODEC, payload -> DiscoveryNbt.encodeSnapshot(payload.snapshot), tag -> new DiscoverySyncPayload(DiscoveryNbt.decodeSnapshot(tag))
   );

   public DiscoverySyncPayload {
      Objects.requireNonNull(snapshot, "snapshot");
   }

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
