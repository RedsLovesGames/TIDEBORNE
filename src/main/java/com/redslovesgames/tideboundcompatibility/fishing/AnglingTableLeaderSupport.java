/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.TideTags;
import com.li64.tide.data.rods.CustomRodManager;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ForgingSlotsManager;

public final class AnglingTableLeaderSupport {
   private AnglingTableLeaderSupport() {
   }

   public static ForgingSlotsManager createSlotDefinition() {
      return ForgingSlotsManager.create()
         .input(0, 26, 11, stack -> stack.isIn(TideTags.Items.FISHING_RODS))
         .input(1, 134, 8, stack -> stack.isIn(TideTags.Items.LINES) && !SteelLeaderAttachment.isSteelLeaderStack(stack))
         .input(2, 134, 32, stack -> stack.isIn(TideTags.Items.BOBBERS))
         .input(3, 134, 56, stack -> stack.isIn(TideTags.Items.HOOKS))
         .input(4, 110, 56, SteelLeaderAttachment::isSteelLeaderStack)
         .output(5, 26, 49)
         .build();
   }

   public static void beforeUpdate(Inventory input) {
      ItemStack rod = input.getStack(0);
      if (rod.isEmpty()) {
         return;
      }

      ItemStack leader = input.getStack(4);
      ItemStack currentLine = CustomRodManager.getLine(rod);
      if (SteelLeaderAttachment.isSteelLeaderStack(currentLine)) {
         CustomRodManager.setLine(rod, ItemStack.EMPTY);
         if (leader.isEmpty()) {
            input.setStack(4, new ItemStack(TideboundItems.STEEL_LEADER));
            leader = input.getStack(4);
         } else {
            SteelLeaderAttachment.set(rod, true);
         }
      }

      if (SteelLeaderAttachment.has(rod) && leader.isEmpty()) {
         SteelLeaderAttachment.set(rod, false);
         input.setStack(4, new ItemStack(TideboundItems.STEEL_LEADER));
      }
   }

   public static void afterUpdate(Inventory input, CraftingResultInventory output) {
      ItemStack rod = input.getStack(0);
      if (rod.isEmpty()) {
         return;
      }

      ItemStack leader = input.getStack(4);
      if (leader.isEmpty()) {
         return;
      }

      ItemStack result = output.getStack(0);
      if (result.isEmpty()) {
         result = rod.copy();
      }

      SteelLeaderAttachment.set(result, true);
      output.setStack(0, result);
   }

   public static void drawLeaderSlot(DrawContext context, int x, int y) {
      context.fill(x + 109, y + 55, x + 127, y + 73, -12963032);
      context.fill(x + 110, y + 56, x + 126, y + 72, -7503766);
      context.fill(x + 111, y + 57, x + 126, y + 72, -2700883);
      context.fill(x + 111, y + 57, x + 125, y + 58, -990009);
   }
}
