package com.redslovesgames.tideborne.satchel;

import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.rods.BaitContents;
import com.li64.tide.data.rods.CustomRodManager;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.gear.LeaderAttachment;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Builds an item-conserving swap on copies before either authoritative inventory is written. */
public final class SatchelTackleExchange {
    private SatchelTackleExchange() {}

    public static Plan plan(ItemStack held, List<ItemStack> offered) {
        if (offered.size() != 6) throw new IllegalArgumentException("Six tackle slots required");
        if (!survivesNativeCopy(held) || offered.stream().anyMatch(item -> !survivesNativeCopy(item)))
            return Plan.failed("Remove excess bait from the rod before exchanging tackle");
        List<ItemStack> pockets = new ArrayList<>(offered.stream().map(ItemStack::copy).toList());
        for (int i = 0; i < 6; i++) {
            ItemStack item = pockets.get(i);
            if (!item.isEmpty() && (!FishingGearRegistry.accepts(SatchelPreset.SLOTS.get(i), item)
                    || item.getCount() > (i == 4 ? item.getMaxCount() : 1)
                    || Boolean.TRUE.equals(item.get(TideTraitsComponents.PROTECTED)))) return Plan.failed("Incompatible or protected tackle");
        }
        if (pockets.stream().allMatch(ItemStack::isEmpty)) return Plan.failed("Add tackle to this preset");
        if (!held.isEmpty() && (!FishingGearRegistry.accepts(FishingGearRegistry.Slot.ROD, held) || held.getCount() != 1))
            return Plan.failed("Hold a fishing rod or empty the other hand");
        ItemStack previous = held.copy();
        ItemStack rod = pockets.get(0).isEmpty() ? held.copy() : pockets.get(0).copy();
        if (rod.isEmpty()) return Plan.failed("Hold a rod to equip this partial preset");
        if (Boolean.TRUE.equals(previous.get(TideTraitsComponents.PROTECTED))) return Plan.failed("Unprotect the current rod first");
        try {
            normalizeLeader(rod);
            if (!previous.isEmpty()) normalizeLeader(previous);
            if (!pockets.get(0).isEmpty()) {
                // Empty accessory slots retain the CURRENT equipment even when exchanging rod bodies.
                if (!previous.isEmpty()) {
                    if (pockets.get(1).isEmpty()) swapComponent(rod, previous, TideDataComponents.FISHING_LINE);
                    if (pockets.get(2).isEmpty()) swapComponent(rod, previous, TideDataComponents.FISHING_HOOK);
                    if (pockets.get(3).isEmpty()) swapComponent(rod, previous, TideDataComponents.FISHING_BOBBER);
                    if (pockets.get(4).isEmpty()) swapComponent(rod, previous, TideDataComponents.BAIT_CONTENTS);
                    if (pockets.get(5).isEmpty()) {
                        swapComponent(rod, previous, TideTraitsComponents.LEADER_TIER);
                        swapComponent(rod, previous, TideTraitsComponents.STEEL_LEADER_ATTACHED);
                    }
                }
                pockets.set(0, previous);
            }
            for (int slot = 1; slot < 6; slot++) {
                ItemStack incoming = pockets.get(slot);
                if (incoming.isEmpty()) continue;
                ItemStack displaced = physicalEquipment(rod).get(slot);
                switch (slot) {
                    case 1 -> CustomRodManager.setLine(rod, incoming);
                    case 2 -> CustomRodManager.setHook(rod, incoming);
                    case 3 -> CustomRodManager.setBobber(rod, incoming);
                    case 4 -> {
                        // One visible bait slot changes the first native bait slot; all additional slots survive.
                        List<ItemStack> bait = new ArrayList<>(rod.getOrDefault(TideDataComponents.BAIT_CONTENTS, new BaitContents())
                                .items().stream().map(ItemStack::copy).toList());
                        if (bait.isEmpty()) bait.add(incoming.copy()); else bait.set(0, incoming.copy());

                        rod.set(TideDataComponents.BAIT_CONTENTS, new BaitContents(List.copyOf(bait)));

                    }
                    case 5 -> {
                        if (!ItemStack.areItemsAndComponentsEqual(incoming, new ItemStack(incoming.getItem())))
                            return Plan.failed("Use an unmodified leader; this attachment stores only its tier");
                        LeaderAttachment.set(rod, LeaderAttachment.tierOfStack(incoming));
                    }
                    default -> throw new IllegalArgumentException("slot");
                }
                pockets.set(slot, displaced);
            }
            if (!survivesNativeCopy(rod) || pockets.stream().anyMatch(item -> !survivesNativeCopy(item)))
                return Plan.failed("Rod bait capacities differ; remove extra bait before swapping rods");
            return new Plan(true, "", rod, List.copyOf(pockets));
        } catch (RuntimeException exception) { return Plan.failed("Tackle could not be exchanged safely"); }
    }

    /** Physical attachments only: Tide's implicit default hook/line/bobber must never become free items. */
    public static List<ItemStack> physicalEquipment(ItemStack rod) {
        if (rod.isEmpty()) return java.util.Collections.nCopies(6, ItemStack.EMPTY);
        var bait = rod.getOrDefault(TideDataComponents.BAIT_CONTENTS, new BaitContents());
        var tier = LeaderAttachment.tier(rod);
        if (tier == null) tier = LeaderAttachment.tierOfStack(CustomRodManager.getLine(rod));
        return List.of(rod.copy(), CustomRodManager.hasLine(rod) ? CustomRodManager.getLine(rod).copy() : ItemStack.EMPTY,
                CustomRodManager.hasHook(rod) ? CustomRodManager.getHook(rod).copy() : ItemStack.EMPTY,
                CustomRodManager.hasBobber(rod) ? CustomRodManager.getBobber(rod).copy() : ItemStack.EMPTY,
                bait.isEmpty() ? ItemStack.EMPTY : bait.get(0).copy(), tier == null ? ItemStack.EMPTY : new ItemStack(LeaderAttachment.item(tier)));
    }

    /** Tide normalizes rod capacity during copy/load. Never commit contents it would discard. */
    static boolean survivesNativeCopy(ItemStack stack) {
        return ItemStack.areEqual(stack, stack.copy());
    }

    private static void normalizeLeader(ItemStack rod) {
        ItemStack line = CustomRodManager.hasLine(rod) ? CustomRodManager.getLine(rod) : ItemStack.EMPTY;
        var tier = LeaderAttachment.tierOfStack(line);
        if (tier == null) return;
        if (LeaderAttachment.tier(rod) != null || !ItemStack.areItemsAndComponentsEqual(line, new ItemStack(line.getItem())))
            throw new IllegalArgumentException("Ambiguous legacy leader");
        rod.remove(TideDataComponents.FISHING_LINE);
        LeaderAttachment.set(rod, tier);
    }

    private static <T> void swapComponent(ItemStack first, ItemStack second, ComponentType<T> type) {
        T value = first.get(type), other = second.get(type);
        if (other == null) first.remove(type); else first.set(type, other);
        if (value == null) second.remove(type); else second.set(type, value);
    }

    public record Plan(boolean success, String error, ItemStack rod, List<ItemStack> pockets) {
        private static Plan failed(String error) { return new Plan(false, error, ItemStack.EMPTY, List.of()); }
    }
}
