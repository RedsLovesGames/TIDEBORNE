/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.satchel;

import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item.class_1793;

public final class SatchelRegistration {
   public static final Identifier STATE_ID = id("satchel_state");
   public static final Identifier ANGLERS_SATCHEL_ID = id("anglers_satchel");
   public static final ComponentType<NbtCompound> SATCHEL_STATE = (ComponentType<NbtCompound>)Registry.register(
      Registries.DATA_COMPONENT_TYPE, STATE_ID, ComponentType.builder().codec(NbtCompound.CODEC).packetCodec(PacketCodecs.NBT_COMPOUND).build()
   );
   public static final Item ANGLERS_SATCHEL = (Item)Registry.register(
      Registries.ITEM,
      ANGLERS_SATCHEL_ID,
      new AnglersSatchelItem(
         new class_1793()
            .maxCount(1)
            .component(TideDataComponents.SATCHEL_CONTENTS, new SatchelContents())
            .component(TideDataComponents.FISH_SATCHEL_OPENED, false)
            .component(SATCHEL_STATE, SatchelState.empty().toTag())
      )
   );

   private SatchelRegistration() {
   }

   public static void init() {
   }

   public static boolean isAnglersSatchel(ItemStack stack) {
      return stack != null && !stack.isEmpty() && stack.isOf(ANGLERS_SATCHEL);
   }

   private static Identifier id(String path) {
      return Identifier.of("tide_traits", path);
   }
}
