/* RECONSTRUCTED SOURCE BASELINE */
package com.redslovesgames.tideborne.registry;

import com.li64.tide.registries.items.FishingHookItem;
import com.li64.tide.registries.items.FishingLineItem;
import com.li64.tide.registries.items.TideFishingRodItem;
import com.redslovesgames.tideborne.ecosystem.ChumBucketItem;
import com.redslovesgames.tideborne.fishing.gear.KujiraBoneFishingRodItem;
import com.redslovesgames.tideborne.migration.legacy.ids.LegacyRegistryIds;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class TideboundItems {
   public static final Item TENTACLE_LINE=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.tentacle_line.desc");
   public static final Item SEAFARERS_HOOK=new FishingHookItem(new Settings().maxCount(1),"item.tidebound_compatibility.seafarers_hook.desc");
   public static final Item SWIFT_LINE=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.swift_line.desc");
   public static final TideFishingRodItem KUJIRA_BONE_FISHING_ROD=new KujiraBoneFishingRodItem(3,512.0,new Settings());
   public static final Item LEVIATHAN_BAIT=new Item(new Settings()); public static final Item CHUM_BUCKET=new ChumBucketItem(new Settings().maxCount(1));
   public static final Item COPPER_LEADER=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.copper_leader.desc");
   public static final Item IRON_LEADER=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.steel_leader.desc");
   /** @deprecated The old registry ID is retained and now represents Iron Leader. */ @Deprecated public static final Item STEEL_LEADER=IRON_LEADER;
   public static final Item GOLD_LEADER=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.gold_leader.desc");
   public static final Item DIAMOND_LEADER=new FishingLineItem(new Settings().maxCount(1),"item.tidebound_compatibility.diamond_leader.desc");
   public static final Item SHARK_TOOTH=new Item(new Settings()); public static final Item SHARK_TOOTH_HOOK=new FishingHookItem(new Settings().maxCount(1),"item.tidebound_compatibility.shark_tooth_hook.desc");
   private TideboundItems(){}
   public static void register(){
      register(LegacyRegistryIds.TENTACLE_LINE,TENTACLE_LINE);
      register(LegacyRegistryIds.SEAFARERS_HOOK,SEAFARERS_HOOK);
      register(LegacyRegistryIds.SWIFT_LINE,SWIFT_LINE);
      register(LegacyRegistryIds.KUJIRA_BONE_FISHING_ROD,KUJIRA_BONE_FISHING_ROD);
      register(LegacyRegistryIds.LEVIATHAN_BAIT,LEVIATHAN_BAIT);
      register(LegacyRegistryIds.CHUM_BUCKET,CHUM_BUCKET);
      register(LegacyRegistryIds.COPPER_LEADER,COPPER_LEADER);
      register(LegacyRegistryIds.STEEL_LEADER,IRON_LEADER);
      register(LegacyRegistryIds.GOLD_LEADER,GOLD_LEADER);
      register(LegacyRegistryIds.DIAMOND_LEADER,DIAMOND_LEADER);
      register(LegacyRegistryIds.SHARK_TOOTH,SHARK_TOOTH);
      register(LegacyRegistryIds.SHARK_TOOTH_HOOK,SHARK_TOOTH_HOOK);
   }
   private static void register(Identifier id,Item item){Registry.register(Registries.ITEM,id,item);}
}
