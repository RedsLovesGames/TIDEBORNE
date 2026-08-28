/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.component;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.component.ComponentType;

public final class TideTraitsComponents {
   public static final ComponentType<String> MUTATION = register(
      "mutation", ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build()
   );
   public static final ComponentType<Long> MUTATION_SEED = register(
      "mutation_seed", ComponentType.<Long>builder().codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG).build()
   );
   public static final ComponentType<Double> SIZE_PERCENTILE = register(
      "size_percentile", ComponentType.<Double>builder().codec(Codec.DOUBLE).packetCodec(PacketCodecs.DOUBLE).build()
   );
   public static final ComponentType<Boolean> PROTECTED = register(
      "protected", ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build()
   );
   public static final ComponentType<String> BODY_TYPE = register(
      "body_type", ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build()
   );
   public static final ComponentType<Boolean> STEEL_LEADER_ATTACHED = register(
      "steel_leader_attached", ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build()
   );

   private TideTraitsComponents() {
   }

   public static void init() {
   }

   private static <T> ComponentType<T> register(String path, ComponentType<T> type) {
      Identifier id = Identifier.of("tide_traits", path);
      return (ComponentType<T>)Registry.register(Registries.DATA_COMPONENT_TYPE, id, type);
   }
}
