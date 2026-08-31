package com.redslovesgames.tideboundcompatibility.fishing;

import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.fishing.v2.FishingGearEffects;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** Fixed specialization effects for Tide bobbers. Fishing Luck/lure remain server-configured separately. */
public final class BobberGearModifiers {
    private BobberGearModifiers() {}

    public static FishingGearModifiers forHook(TideFishingHook hook) {
        if (hook == null) return FishingGearModifiers.neutral();
        return forBobber(CustomRodManager.getBobber(hook.getRod()));
    }

    public static FishingGearModifiers forRod(ItemStack rod) {
        if (rod == null || rod.isEmpty()) return FishingGearModifiers.neutral();
        return forBobber(CustomRodManager.getBobber(rod));
    }

    public static FishingGearModifiers forBobber(ItemStack bobber) {
        if (bobber == null || bobber.isEmpty()) return FishingGearModifiers.neutral();
        Identifier id = Registries.ITEM.getId(bobber.getItem());
        if (!"tide".equals(id.getNamespace())) return FishingGearModifiers.neutral();
        return switch (id.getPath()) {
            case "grassy_bobber" -> zone(1.04D);
            case "lichen_bobber" -> zone(1.03D);
            case "iron_bobber" -> FishingGearModifiers.compose(zone(1.05D), protection(0.05D));
            case "diamond_bobber" -> FishingGearModifiers.compose(zone(1.08D), protection(0.10D));
            case "netherite_bobber" -> FishingGearModifiers.compose(zone(1.10D), protection(0.15D));
            case "amethyst_bobber" -> FishingGearModifiers.builder().traitLuck(1.0D).build();
            case "echo_bobber" -> FishingGearModifiers.builder().traitLuck(2.0D).build();
            case "duck_bobber" -> crate(1.10D);
            case "nautilus_bobber" -> crate(1.25D);
            case "heart_bobber" -> crate(1.40D);
            default -> FishingGearModifiers.neutral();
        };
    }

    private static FishingGearModifiers zone(double multiplier) {
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, multiplier).build();
    }

    private static FishingGearModifiers crate(double multiplier) {
        return FishingGearModifiers.builder().namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER, multiplier).build();
    }

    private static FishingGearModifiers protection(double chance) {
        return FishingGearModifiers.builder()
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, chance)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES, 1.0D)
                .build();
    }
}
