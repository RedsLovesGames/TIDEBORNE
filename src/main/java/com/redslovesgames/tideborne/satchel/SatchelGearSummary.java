package com.redslovesgames.tideborne.satchel;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/** Compact preset specialization summary used by the Satchel UI and regression tests. */
public final class SatchelGearSummary {
    private SatchelGearSummary() {}

    public static String forPreset(List<ItemStack> items) {
        if (items == null || items.stream().allMatch(stack -> stack == null || stack.isEmpty())) return "";
        for (ItemStack stack : items) {
            if (stack == null || stack.isEmpty()) continue;
            String id = Registries.ITEM.getId(stack.getItem()).toString();
            if (id.equals("tide:echo_bobber")) return "Trait-focused tackle";
        }
        return "Tackle preset";
    }
}
