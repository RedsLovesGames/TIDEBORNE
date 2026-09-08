package com.redslovesgames.tideborne.satchel;

import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.rods.BaitContents;
import com.li64.tide.data.rods.CustomRodManager;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/** Pure tackle exchange planner. It never mutates the supplied rod or preset pockets. */
public final class SatchelTackleExchange {
    private SatchelTackleExchange() {}

    public static Plan plan(ItemStack currentRod, List<ItemStack> requestedPockets) {
        if (currentRod == null || currentRod.isEmpty()) return Plan.failed("No fishing rod equipped", currentRod, requestedPockets);
        ArrayList<ItemStack> pockets = normalized(requestedPockets);
        ItemStack rod = currentRod.copy();

        for (int i = 0; i < SatchelPreset.SLOTS.size(); i++) {
            ItemStack requested = pockets.get(i);
            if (requested.isEmpty()) continue;
            FishingGearRegistry.Slot slot = SatchelPreset.SLOTS.get(i);
            if (!FishingGearRegistry.accepts(slot, requested)) return Plan.failed("Invalid item for " + slot, currentRod, requestedPockets);
            if (slot == FishingGearRegistry.Slot.ATTACHMENT && requested.contains(DataComponentTypes.CUSTOM_NAME)) {
                return Plan.failed("Named attachment cannot be safely rewritten", currentRod, requestedPockets);
            }
        }

        ItemStack requestedRod = pockets.get(0);
        if (!requestedRod.isEmpty()) {
            BaitContents bait = rod.get(TideDataComponents.BAIT_CONTENTS);
            if (isVanillaRod(requestedRod) && bait != null && bait.size() > 1) {
                return Plan.failed("Replacement rod cannot preserve all bait slots", currentRod, requestedPockets);
            }
            ItemStack incoming = requestedRod.copy();
            ItemStack displaced = rod.copy();
            swapRodAccessories(rod, incoming, displaced);
            rod = incoming;
            pockets.set(0, displaced);
        }

        if (!pockets.get(1).isEmpty()) {
            ItemStack previous = physicalOrEmpty(CustomRodManager.getLine(rod), "tide:fishing_line");
            CustomRodManager.setLine(rod, pockets.get(1));
            pockets.set(1, previous);
        }
        if (!pockets.get(2).isEmpty()) {
            ItemStack previous = physicalOrEmpty(CustomRodManager.getHook(rod), "tide:fishing_hook");
            CustomRodManager.setHook(rod, pockets.get(2));
            pockets.set(2, previous);
        }
        if (!pockets.get(3).isEmpty()) {
            ItemStack previous = physicalOrEmpty(CustomRodManager.getBobber(rod), "tide:red_bobber");
            CustomRodManager.setBobber(rod, pockets.get(3));
            pockets.set(3, previous);
        }
        if (!pockets.get(4).isEmpty()) {
            BaitContents existing = rod.get(TideDataComponents.BAIT_CONTENTS);
            ArrayList<ItemStack> bait = new ArrayList<>();
            if (existing != null) {
                for (int i = 0; i < existing.size(); i++) bait.add(existing.get(i).copy());
            }
            ItemStack previous = bait.isEmpty() ? ItemStack.EMPTY : bait.get(0).copy();
            if (bait.isEmpty()) bait.add(pockets.get(4).copy()); else bait.set(0, pockets.get(4).copy());
            rod.set(TideDataComponents.BAIT_CONTENTS, new BaitContents(bait));
            pockets.set(4, previous);
        }

        return new Plan(true, "", rod, pockets);
    }

    private static void swapRodAccessories(ItemStack current, ItemStack incoming, ItemStack displaced) {
        ItemStack currentLine = physicalOrEmpty(CustomRodManager.getLine(current), "tide:fishing_line");
        ItemStack currentHook = physicalOrEmpty(CustomRodManager.getHook(current), "tide:fishing_hook");
        ItemStack currentBobber = physicalOrEmpty(CustomRodManager.getBobber(current), "tide:red_bobber");
        BaitContents currentBait = current.get(TideDataComponents.BAIT_CONTENTS);

        ItemStack incomingLine = physicalOrEmpty(CustomRodManager.getLine(incoming), "tide:fishing_line");
        ItemStack incomingHook = physicalOrEmpty(CustomRodManager.getHook(incoming), "tide:fishing_hook");
        ItemStack incomingBobber = physicalOrEmpty(CustomRodManager.getBobber(incoming), "tide:red_bobber");
        BaitContents incomingBait = incoming.get(TideDataComponents.BAIT_CONTENTS);

        if (!currentLine.isEmpty()) CustomRodManager.setLine(incoming, currentLine);
        if (!currentHook.isEmpty()) CustomRodManager.setHook(incoming, currentHook);
        if (!currentBobber.isEmpty()) CustomRodManager.setBobber(incoming, currentBobber);
        if (currentBait != null && currentBait.size() > 0) incoming.set(TideDataComponents.BAIT_CONTENTS, copyBait(currentBait));

        if (!incomingLine.isEmpty()) CustomRodManager.setLine(displaced, incomingLine);
        if (!incomingHook.isEmpty()) CustomRodManager.setHook(displaced, incomingHook);
        if (!incomingBobber.isEmpty()) CustomRodManager.setBobber(displaced, incomingBobber);
        if (incomingBait != null && incomingBait.size() > 0) displaced.set(TideDataComponents.BAIT_CONTENTS, copyBait(incomingBait));
    }

    private static BaitContents copyBait(BaitContents source) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) items.add(source.get(i).copy());
        return new BaitContents(items);
    }

    private static ItemStack physicalOrEmpty(ItemStack stack, String virtualDefault) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        return Registries.ITEM.getId(stack.getItem()).toString().equals(virtualDefault) ? ItemStack.EMPTY : stack.copy();
    }

    private static boolean isVanillaRod(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).toString().equals("minecraft:fishing_rod");
    }

    private static ArrayList<ItemStack> normalized(List<ItemStack> source) {
        ArrayList<ItemStack> result = new ArrayList<>(SatchelPreset.SLOTS.size());
        for (int i = 0; i < SatchelPreset.SLOTS.size(); i++) {
            ItemStack stack = source != null && i < source.size() && source.get(i) != null ? source.get(i) : ItemStack.EMPTY;
            result.add(stack.copy());
        }
        return result;
    }

    public record Plan(boolean success, String error, ItemStack rod, List<ItemStack> pockets) {
        public Plan {
            rod = rod == null ? ItemStack.EMPTY : rod.copy();
            pockets = pockets == null ? List.of() : pockets.stream().map(stack -> stack == null ? ItemStack.EMPTY : stack.copy()).toList();
        }
        private static Plan failed(String error, ItemStack rod, List<ItemStack> pockets) {
            return new Plan(false, error, rod, normalized(pockets));
        }
        @Override public ItemStack rod() { return rod.copy(); }
        @Override public List<ItemStack> pockets() { return pockets.stream().map(ItemStack::copy).toList(); }
    }
}
