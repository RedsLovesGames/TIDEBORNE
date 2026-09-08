package com.redslovesgames.tideborne.registry;

import com.redslovesgames.tideborne.Tideborne;
import com.redslovesgames.tideborne.compat.TideboundCompatibility;
import com.redslovesgames.tideborne.registry.TideboundItems;
import com.redslovesgames.tideborne.satchel.SatchelRegistration;
import java.util.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TideborneItemGroups {
 public static final Identifier TIDEBORNE_ID=Identifier.of(Tideborne.MOD_ID,"tideborne"); public static final ItemGroup TIDEBORNE=FabricItemGroup.builder().icon(()->new ItemStack(SatchelRegistration.ANGLERS_SATCHEL)).displayName(Text.translatable("itemGroup.tideborne")).entries((c,e)->currentCreativeItems().forEach(e::add)).build();private static boolean initialized;private TideborneItemGroups(){}
 public static synchronized void init(){if(initialized)return;initialized=true;Registry.register(Registries.ITEM_GROUP,TIDEBORNE_ID,TIDEBORNE);} public static List<Item> currentCreativeItems(){return creativeItems(TideboundCompatibility.isMythsIntegrationActive(),TideboundCompatibility.isApexIntegrationActive());}
 public static List<Item> creativeItems(boolean mythsActive,boolean apexActive){List<Item> items=new ArrayList<>();items.add(SatchelRegistration.ANGLERS_SATCHEL);if(mythsActive)items.add(TideboundItems.KUJIRA_BONE_FISHING_ROD);if(mythsActive){items.add(TideboundItems.TENTACLE_LINE);items.add(TideboundItems.SWIFT_LINE);}if(apexActive){items.add(TideboundItems.COPPER_LEADER);items.add(TideboundItems.IRON_LEADER);items.add(TideboundItems.GOLD_LEADER);items.add(TideboundItems.DIAMOND_LEADER);}if(mythsActive)items.add(TideboundItems.SEAFARERS_HOOK);if(apexActive)items.add(TideboundItems.SHARK_TOOTH_HOOK);if(mythsActive)items.add(TideboundItems.LEVIATHAN_BAIT);if(apexActive){items.add(TideboundItems.CHUM_BUCKET);items.add(TideboundItems.SHARK_TOOTH);}return List.copyOf(items);}
}
