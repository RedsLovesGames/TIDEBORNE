package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.item.ItemStack;

/** Physical six-slot tackle preset. Restored from the authoritative GameTest contract. */
public final class SatchelPreset {
    public static final List<FishingGearRegistry.Slot> SLOTS = List.of(
            FishingGearRegistry.Slot.ROD,
            FishingGearRegistry.Slot.LINE,
            FishingGearRegistry.Slot.HOOK,
            FishingGearRegistry.Slot.BOBBER,
            FishingGearRegistry.Slot.BAIT,
            FishingGearRegistry.Slot.ATTACHMENT);

    private final UUID id;
    private final String name;
    private final List<ItemStack> items;
    private final UUID legacyRod;

    public SatchelPreset(UUID id, String name, List<ItemStack> items, UUID legacyRod) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNullElse(name, "Preset");
        this.items = normalized(items);
        this.legacyRod = legacyRod;
    }

    public static SatchelPreset empty(String name) {
        return new SatchelPreset(UUID.randomUUID(), name, List.of(), null);
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public UUID legacyRod() { return legacyRod; }

    public List<ItemStack> items() {
        return items.stream().map(ItemStack::copy).toList();
    }

    public SatchelPreset renamed(String newName) {
        return new SatchelPreset(id, newName, items, legacyRod);
    }

    public SatchelPreset withItems(List<ItemStack> replacement) {
        return new SatchelPreset(id, name, replacement, legacyRod);
    }

    public SatchelPreset withLegacyRod(UUID rod) {
        return new SatchelPreset(id, name, items, rod);
    }

    private static List<ItemStack> normalized(List<ItemStack> source) {
        ArrayList<ItemStack> result = new ArrayList<>(SLOTS.size());
        for (int i = 0; i < SLOTS.size(); i++) {
            ItemStack stack = source != null && i < source.size() && source.get(i) != null ? source.get(i) : ItemStack.EMPTY;
            result.add(stack.copy());
        }
        return List.copyOf(result);
    }
}
