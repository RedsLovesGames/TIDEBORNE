/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.network;

import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import java.util.Objects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload.Id;

public record TideboundSettingsPayload(NbtCompound tag) implements CustomPayload {
   public static final Id<TideboundSettingsPayload> TYPE = new Id(TideboundCompatibility.id("settings"));
   public static final PacketCodec<RegistryByteBuf, TideboundSettingsPayload> CODEC = PacketCodec.ofStatic(
      (buffer, payload) -> buffer.writeNbt(payload.tag),
      buffer -> new TideboundSettingsPayload(Objects.requireNonNullElseGet(buffer.readNbt(), NbtCompound::new))
   );

   public static TideboundSettingsPayload fromServer() {
      TideboundConfig.Values v = TideboundConfig.get();
      return fromValues(v, TideboundCompatibility.isMythsIntegrationActive(), TideboundCompatibility.isApexIntegrationActive());
   }

   public static TideboundSettingsPayload fromValues(TideboundConfig.Values v, boolean mythsActive, boolean apexActive) {
      NbtCompound tag = new NbtCompound();
      tag.putString("settings_json", TideboundConfig.toJson(v));
      tag.putBoolean("myths_active", mythsActive);
      tag.putBoolean("apex_active", apexActive);
      tag.putBoolean("leviathan_fish_only", v.leviathanBaitFishOnly);
      tag.putInt("leviathan_fish_luck", v.leviathanBaitFishSelectionLuckBonus);
      tag.putDouble("leviathan_speed", v.leviathanBaitMinigameSpeedMultiplier);
      tag.putDouble("leviathan_zone", v.leviathanBaitCatchZoneMultiplier);
      tag.putDouble("tentacle_zone", v.tentacleCatchZoneMultiplier);
      tag.putDouble("tentacle_speed", v.tentacleFishSpeedMultiplier);
      tag.putDouble("swift_zone", v.swiftCatchZoneMultiplier);
      tag.putDouble("swift_speed", v.swiftFishSpeedMultiplier);
      tag.putDouble("steel_zone", v.steelLeaderCatchZoneMultiplier);
      tag.putDouble("steel_speed", v.steelLeaderFishSpeedMultiplier);
      tag.putDouble("steel_prevent", v.steelLeaderCatchLossPreventionChance);
      tag.putDouble("seafarer_rare", v.seafarersRareWeightMultiplier);
      tag.putDouble("kujira_crates", v.kujiraOceanCrateMultiplier);
      tag.putDouble("tooth_predatory", v.sharkToothPredatoryWeightMultiplier);
      tag.putDouble("tooth_small", v.sharkToothSmallFishWeightMultiplier);
      tag.putBoolean("chum_enabled", v.enableChum);
      tag.putBoolean("shark_attraction", v.enableSharkFishAttraction);
      tag.putInt("chum_duration", v.chumDuration);
      tag.putDouble("chum_radius", v.chumRadius);
      tag.putInt("chum_particles", v.chumParticleCount);
      tag.putBoolean("shark_catch_loss", v.enableSharkCatchLoss);
      return new TideboundSettingsPayload(tag);
   }

   public Id<? extends CustomPayload> getId() {
      return TYPE;
   }
}
