package com.redslovesgames.tideborne.satchel;

import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import java.util.ArrayList;
import java.util.List;

/** Read-only role labels from canonical effects, never an equipment or stat authority. */
public final class SatchelGearSummary {
    private SatchelGearSummary() {}
    public static String forPreset(List<net.minecraft.item.ItemStack> items) {
        if (items.stream().allMatch(net.minecraft.item.ItemStack::isEmpty)) return "";
        var preview = new ArrayList<>(items);
        if (preview.getFirst().isEmpty()) preview.set(0, new net.minecraft.item.ItemStack(net.minecraft.item.Items.FISHING_ROD));
        var plan = SatchelTackleExchange.plan(net.minecraft.item.ItemStack.EMPTY, preview);
        if (!plan.success()) return "";
        var modifiers = com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers.forLoadoutPreview(
                plan.rod(), com.redslovesgames.tideborne.config.TideboundConfig.get());
        return String.join(" · ", archetypes(modifiers));
    }

    public static List<String> archetypes(FishingGearModifiers gear) {
        List<String> result = new ArrayList<>();
        if (FishingGearEffects.trophyFightRelief(gear) > 0) result.add("Trophy");
        if (FishingGearEffects.fishingLuck(gear) > 0 || gear.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX + "legendary") > 1) result.add("Rare Species");
        if (FishingGearEffects.traitLuck(gear) > 0) result.add("Trait");
        if (FishingGearEffects.catchLossPreventionChance(gear) > 0 || FishingGearEffects.catchZoneAreaMultiplier(gear) > 1) result.add("Safe");
        if (gear.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX + "boss") > 1) result.add("Leviathan");
        if (gear.namedAdditiveModifier(FishingGearEffects.LURE_BONUS) > 0) result.add("Fast");
        if (FishingGearEffects.strengthMultiplier(gear) < 1 || gear.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX + "heavy") > 1) result.add("Heavy Fish");
        return List.copyOf(result);
    }
}
