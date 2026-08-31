package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideTags;
import com.li64.tide.data.rods.CustomRodManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ForgingSlotsManager;

public final class AnglingTableLeaderSupport {
   private AnglingTableLeaderSupport(){}
   public static ForgingSlotsManager createSlotDefinition(){return ForgingSlotsManager.create().input(0,26,11,s->s.isIn(TideTags.Items.FISHING_RODS)).input(1,134,8,s->s.isIn(TideTags.Items.LINES)&&!LeaderAttachment.isLeaderStack(s)).input(2,134,32,s->s.isIn(TideTags.Items.BOBBERS)).input(3,134,56,s->s.isIn(TideTags.Items.HOOKS)).input(4,110,56,LeaderAttachment::isLeaderStack).output(5,26,49).build();}
   public static void beforeUpdate(Inventory input){ItemStack rod=input.getStack(0);if(rod.isEmpty())return;ItemStack slot=input.getStack(4);ItemStack line=CustomRodManager.getLine(rod);LeaderTier lineTier=LeaderAttachment.tierOfStack(line);if(lineTier!=null){CustomRodManager.setLine(rod,ItemStack.EMPTY);if(slot.isEmpty())input.setStack(4,new ItemStack(LeaderAttachment.item(lineTier)));else LeaderAttachment.set(rod,lineTier);}
      if(LeaderAttachment.has(rod)&&input.getStack(4).isEmpty()){LeaderTier tier=LeaderAttachment.tier(rod);LeaderAttachment.clear(rod);input.setStack(4,new ItemStack(LeaderAttachment.item(tier)));}}
   public static void afterUpdate(Inventory input,CraftingResultInventory output){ItemStack rod=input.getStack(0);if(rod.isEmpty())return;LeaderTier tier=LeaderAttachment.tierOfStack(input.getStack(4));if(tier==null)return;ItemStack result=output.getStack(0);if(result.isEmpty())result=rod.copy();LeaderAttachment.set(result,tier);output.setStack(0,result);}
   public static void drawLeaderSlot(DrawContext c,int x,int y){c.fill(x+109,y+55,x+127,y+73,-12963032);c.fill(x+110,y+56,x+126,y+72,-7503766);c.fill(x+111,y+57,x+126,y+72,-2700883);c.fill(x+111,y+57,x+125,y+58,-990009);}
}
