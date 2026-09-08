/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.compat.apex;

import com.li64.tide.data.TideTags.Items;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook.CatchType;
import com.redslovesgames.tideborne.compat.TideboundCompatibility;
import com.redslovesgames.tideborne.config.TideboundConfig;
import com.redslovesgames.tideborne.registry.TideboundTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.Registries;
import org.joml.Vector3f;

public final class SharkScentManager {
   private static final int MAX_ZONES_PER_LEVEL = 256;
   private static final Identifier APEX_SHARK = Identifier.of("apexwaters", "great_white_shark");
   private static final Map<ServerWorld, List<SharkScentManager.ScentZone>> ZONES = new WeakHashMap<>();

   private SharkScentManager() {
   }

   public static void addChumZone(ServerWorld level, Vec3d position, double radius, long durationTicks, UUID owner) {
      addOrRefresh(
         level,
         new SharkScentManager.ScentZone(
            position, radius, TideboundConfig.get().chumScentStrength, level.getTime() + durationTicks, SharkScentManager.Kind.CHUM, owner
         )
      );
   }

   public static void onCatchSelected(TideFishingHook hook) {
      if (hook.getWorld() instanceof ServerWorld level && enabled() && hook.hasHookedItem() && hook.getCatchType() == CatchType.FISH) {
         double strength = strongestScent(hook.getHookedItems());
         if (strength >= 1.0) {
            addOrRefresh(
               level,
               new SharkScentManager.ScentZone(
                  hook.getPos(),
                  Math.min(48.0, 12.0 + strength * 8.0),
                  strength,
                  level.getTime() + 200L,
                  SharkScentManager.Kind.FISHING,
                  ownerId(hook)
               )
            );
         }
      }
   }

   public static void pulseFishingHook(TideFishingHook hook) {
      if (hook.getWorld() instanceof ServerWorld level && enabled() && hook.hasHookedItem() && hook.getCatchType() == CatchType.FISH) {
         double strength = strongestScent(hook.getHookedItems());
         if (!(strength < 1.0)) {
            long start = hook.getMinigameStartTime();
            if (start > 0L) {
               strength *= 1.0 + Math.min(1.0, (level.getTime() - start) / 200.0);
            }

            addOrRefresh(
               level,
               new SharkScentManager.ScentZone(
                  hook.getPos(),
                  Math.min(56.0, 12.0 + strength * 8.0),
                  strength,
                  level.getTime() + 120L,
                  SharkScentManager.Kind.FISHING,
                  ownerId(hook)
               )
            );
         }
      }
   }

   public static double fishScent(ItemStack stack) {
      if (!stack.isEmpty() && stack.isIn(TideboundTags.SHARK_FOOD)) {
         TideboundConfig.Values config = TideboundConfig.get();
         double strength = 1.0;
         FishData data = (FishData)FishData.get(stack).orElse(null);
         if (data != null) {
            double length = data.getAverageLength();
            strength *= sizeMultiplier(length, config.largeFishScentMultiplier);
         }

         if (stack.isIn(TideboundTags.STRONG_SHARK_FOOD) || stack.isIn(Items.LEGENDARY_FISH)) {
            strength *= config.strongSharkFoodScentMultiplier;
         }

         return strength * Math.min(4.0, Math.sqrt(stack.getCount()));
      } else {
         return 0.0;
      }
   }

   public static double fishEntityScent(Entity entity) {
      FishData data = (FishData)FishData.get(entity).orElse(null);
      if (data == null) {
         return 0.0;
      }

      ItemStack stack = new ItemStack((ItemConvertible)data.fish().value());
      return Math.max(0.5, fishScent(stack));
   }

   public static boolean isLargeCatch(ItemStack stack) {
      return !stack.isIn(TideboundTags.LARGE_FISH) && !stack.isIn(Items.LEGENDARY_FISH)
         ? FishData.get(stack).map(data -> data.getAverageLength() >= 100.0).orElse(false)
         : true;
   }

   public static double sizeMultiplier(double length, double largeFishMultiplier) {
      if (length < 35.0) {
         return 0.55;
      }

      if (length >= 100.0) {
         return largeFishMultiplier;
      }

      double t = (length - 35.0) / 65.0;
      return 1.0 + t * (largeFishMultiplier - 1.0);
   }

   public static SharkScentManager.ScentZone findBestZone(ServerWorld level, Vec3d position, double maxDetectionRadius) {
      List<SharkScentManager.ScentZone> zones = ZONES.get(level);
      if (zones != null && !zones.isEmpty()) {
         long now = level.getTime();
         SharkScentManager.ScentZone best = null;
         double bestScore = 0.0;

         for (SharkScentManager.ScentZone zone : zones) {
            if (zone.expiresAt > now) {
               double distance = position.distanceTo(zone.position);
               if (!(distance > Math.min(maxDetectionRadius, zone.radius))) {
                  double score = zone.strength / (1.0 + distance * 0.08);
                  if (score > bestScore) {
                     bestScore = score;
                     best = zone;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static void tick(ServerWorld level) {
      List<SharkScentManager.ScentZone> zones = ZONES.get(level);
      if (zones != null) {
         long now = level.getTime();
         zones.removeIf(zone -> zone.expiresAt <= now);
         if (zones.isEmpty()) {
            ZONES.remove(level);
         }

         if (now % TideboundConfig.get().chumParticlePulseInterval == 0L) {
            zones.stream().filter(zone -> zone.kind == SharkScentManager.Kind.CHUM).forEach(zone -> showChumRing(level, zone));
         }

         if (TideboundConfig.get().allowChumTriggeredSpawns && now % TideboundConfig.get().chumSpawnCheckInterval == 0L) {
            zones.stream().filter(zone -> zone.kind == SharkScentManager.Kind.CHUM).findAny().ifPresent(zone -> tryChumSpawn(level, zone));
         }
      }
   }

   private static void showChumRing(ServerWorld level, SharkScentManager.ScentZone zone) {
      BlockPos origin = BlockPos.ofFloored(zone.position);
      int y = origin.getY();

      while (
         y < level.getTopY() - 1
            && level.getFluidState(new BlockPos(origin.getX(), y + 1, origin.getZ())).isIn(FluidTags.WATER)
      ) {
         y++;
      }

      double radius = Math.max(3.0, zone.radius * 0.55);
      DustParticleEffect red = new DustParticleEffect(new Vector3f(1.0F, 0.04F, 0.04F), 1.25F);
      int count = TideboundConfig.get().chumParticleCount;
      if (count != 0) {
         level.spawnParticles(red, zone.position.x, y + 1.05, zone.position.z, count, radius, 0.35, radius, 0.008);
         level.spawnParticles(
            ParticleTypes.BUBBLE,
            zone.position.x,
            y + 0.85,
            zone.position.z,
            Math.max(1, count / 12),
            radius * 0.7,
            0.18,
            radius * 0.7,
            0.015
         );
      }
   }

   public static int activeZoneCount() {
      return ZONES.values().stream().mapToInt(List::size).sum();
   }

   private static void addOrRefresh(ServerWorld level, SharkScentManager.ScentZone incoming) {
      List<SharkScentManager.ScentZone> zones = ZONES.computeIfAbsent(level, ignored -> new ArrayList<>());

      for (SharkScentManager.ScentZone existing : zones) {
         if (existing.kind == incoming.kind && existing.position.squaredDistanceTo(incoming.position) < 36.0) {
            existing.position = incoming.position;
            existing.radius = Math.max(existing.radius, incoming.radius);
            existing.strength = Math.max(existing.strength, incoming.strength);
            existing.expiresAt = Math.max(existing.expiresAt, incoming.expiresAt);
            return;
         }
      }

      zones.add(incoming);
      if (zones.size() > 256) {
         zones.sort(Comparator.comparingLong(zone -> zone.expiresAt));
         zones.remove(0);
      }
   }

   private static double strongestScent(List<ItemStack> stacks) {
      return stacks == null ? 0.0 : stacks.stream().mapToDouble(SharkScentManager::fishScent).max().orElse(0.0);
   }

   private static UUID ownerId(TideFishingHook hook) {
      return hook.getPlayerOwner() == null ? null : hook.getPlayerOwner().getUuid();
   }

   private static void tryChumSpawn(ServerWorld level, SharkScentManager.ScentZone zone) {
      TideboundConfig.Values config = TideboundConfig.get();
      if (enabled() && level.random.nextInt(config.chumSpawnChanceOneIn) == 0) {
         EntityType<?> type = (EntityType<?>)Registries.ENTITY_TYPE.get(APEX_SHARK);
         if (type != null) {
            Box area = new Box(BlockPos.ofFloored(zone.position)).expand(zone.radius);
            long nearby = level.getOtherEntities((Entity)null, area, entity -> entity.getType() == type).size();
            if (nearby < config.chumNearbySharkCap) {
               BlockPos origin = BlockPos.ofFloored(zone.position);

               for (int attempt = 0; attempt < config.chumSpawnAttempts; attempt++) {
                  BlockPos pos = origin.add(
                     level.random.nextInt(33) - 16, level.random.nextInt(9) - 4, level.random.nextInt(33) - 16
                  );
                  if (level.isChunkLoaded(pos)
                     && level.getBiome(pos).isIn(BiomeTags.IS_OCEAN)
                     && level.getFluidState(pos).isIn(FluidTags.WATER)
                     && SpawnRestriction.canSpawn(type, level, SpawnReason.NATURAL, pos, level.random)) {
                     if (!(type.create(level) instanceof MobEntity mob)) {
                        return;
                     }

                     mob.refreshPositionAndAngles(
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F
                     );
                     if (level.isSpaceEmpty(mob)) {
                        mob.initialize(level, level.getLocalDifficulty(pos), SpawnReason.NATURAL, null);
                        level.spawnEntity(mob);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean enabled() {
      TideboundConfig.Values config = TideboundConfig.get();
      return config.enableApexCompat && config.enableSharkFishAttraction && TideboundCompatibility.isApexLoaded();
   }

   public enum Kind {
      CHUM,
      FISHING;
   }

   public static final class ScentZone {
      private Vec3d position;
      private double radius;
      private double strength;
      private long expiresAt;
      private final SharkScentManager.Kind kind;
      private final UUID owner;

      private ScentZone(Vec3d position, double radius, double strength, long expiresAt, SharkScentManager.Kind kind, UUID owner) {
         this.position = position;
         this.radius = radius;
         this.strength = strength;
         this.expiresAt = expiresAt;
         this.kind = kind;
         this.owner = owner;
      }

      public Vec3d position() {
         return this.position;
      }

      public double strength() {
         return this.strength;
      }

      public SharkScentManager.Kind kind() {
         return this.kind;
      }

      public UUID owner() {
         return this.owner;
      }
   }
}
