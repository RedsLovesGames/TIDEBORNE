package com.redslovesgames.tideborne.fishing.gear;

import com.li64.tide.data.TideTags;
import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.gear.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry;
import com.redslovesgames.tideborne.journal.BobberBonuses;
import com.redslovesgames.tideborne.journal.ServerConfig;
import java.util.TreeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** Server-configured canonical bobber contribution, captured once per cast. */
public final class BobberGearModifiers {
    private BobberGearModifiers() {}

    /** Thin hook adapter storage; no second modifier model or resolver. */
    public interface HookSnapshot {
        FishingGearModifiers tideborne$bobberModifiers();
    }

    public static FishingGearModifiers forHook(TideFishingHook hook) {
        return hook instanceof HookSnapshot snapshot ? snapshot.tideborne$bobberModifiers() : FishingGearModifiers.neutral();
    }

    public static FishingGearModifiers forRod(ItemStack rod) {
        if (rod == null || rod.isEmpty()) return FishingGearModifiers.neutral();
        return forBobber(CustomRodManager.getBobber(rod));
    }

    public static FishingGearModifiers forBobber(ItemStack bobber) {
        if (bobber == null || bobber.isEmpty()) return FishingGearModifiers.neutral();
        return forId(Registries.ITEM.getId(bobber.getItem()), bobber.isIn(TideTags.Items.BOBBERS), ServerConfig.get());
    }

    public static FishingGearModifiers forId(Identifier id, boolean tagged, ServerConfig.Values config) {
        if (!tagged) return FishingGearModifiers.neutral();
        var base = FishingGearRegistry.bobberModifiers(id).orElseGet(FishingGearModifiers::neutral);
        // Existing config keys override only luck/lure; specialization remains fixed as before.
        var bonus = config.bobberBonusesEnabled
                ? config.bobberBonuses.getOrDefault(id.toString(), config.fallbackBobberBonus) : BobberBonuses.Bonus.NONE;
        var additive = new TreeMap<>(base.namedAdditiveModifiers());
        additive.put(FishingGearEffects.LURE_BONUS, (double) bonus.lureSpeed());
        return new FishingGearModifiers(bonus.luck(), base.traitLuck(), base.strengthMultiplier(), base.tempoMultiplier(),
                base.categoryRestriction(), base.catchPoolRestriction(), base.bodyTypeChanceMultipliers(), additive, base.namedMultiplierModifiers());
    }
}
