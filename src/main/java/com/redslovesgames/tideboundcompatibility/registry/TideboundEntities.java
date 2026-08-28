/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.registry;

import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.entity.ChumProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.entity.EntityType.Builder;

public final class TideboundEntities {
   public static final EntityType<ChumProjectileEntity> CHUM_PROJECTILE = (EntityType<ChumProjectileEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      TideboundCompatibility.id("chum_projectile"),
      Builder.<ChumProjectileEntity>create(ChumProjectileEntity::new, SpawnGroup.MISC).dimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10).build()
   );

   private TideboundEntities() {
   }

   public static void register() {
   }
}
