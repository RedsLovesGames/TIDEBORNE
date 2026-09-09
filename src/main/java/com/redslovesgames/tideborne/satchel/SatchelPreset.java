package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import net.minecraft.item.ItemStack;
import java.util.List;
import java.util.UUID;

/** Physical storage only. Selection and names have no gameplay effect. */
public record SatchelPreset(UUID id, String name, List<ItemStack> items, UUID legacyRod) {
    public static final int MAX_PRESETS = 12;
    public static final int MAX_NAME = 24;
    public static final List<FishingGearRegistry.Slot> SLOTS = List.of(FishingGearRegistry.Slot.ROD,
            FishingGearRegistry.Slot.LINE, FishingGearRegistry.Slot.HOOK, FishingGearRegistry.Slot.BOBBER,
            FishingGearRegistry.Slot.BAIT, FishingGearRegistry.Slot.ATTACHMENT);
    public SatchelPreset {
        if (id == null || items.size() != 6) throw new IllegalArgumentException("Invalid preset");
        name = cleanName(name);
        if (items.stream().anyMatch(item -> !SatchelTackleExchange.survivesNativeCopy(item)))
            throw new IllegalArgumentException("Rod contents exceed native capacity");
        items = items.stream().map(ItemStack::copy).toList();
    }
    public static String cleanName(String name) {
        String cleaned = name == null ? "" : name.replaceAll("[\\p{Cntrl}§]", "").strip();
        if (cleaned.isEmpty()) return "New Preset";
        return cleaned.substring(0, Math.min(MAX_NAME, cleaned.length()));
    }
    public static SatchelPreset empty(String name) {
        return new SatchelPreset(UUID.randomUUID(), name, java.util.Collections.nCopies(6, ItemStack.EMPTY), null);
    }
    @Override public List<ItemStack> items() { return items.stream().map(ItemStack::copy).toList(); }
    public SatchelPreset withItems(List<ItemStack> items) { return new SatchelPreset(id, name, items, items.getFirst().isEmpty() ? legacyRod : null); }
    public SatchelPreset renamed(String name) { return new SatchelPreset(id, name, items, legacyRod); }
}
