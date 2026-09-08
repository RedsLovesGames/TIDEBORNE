package com.redslovesgames.tideborne.fishing.gear;

import com.li64.tide.data.rods.CustomRodManager;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.redslovesgames.tideborne.registry.TideboundItems;
import com.redslovesgames.tideborne.registry.TideTraitsComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Persists material leader tier on a rod while remaining compatible with legacy steel_leader_attached saves. */
public final class LeaderAttachment {
    private LeaderAttachment() {}

    public static LeaderTier tier(Object value) {
        if (!(value instanceof ItemStack stack)) {
            return null;
        }
        LeaderTier explicit = LeaderTier.parse(stack.get(TideTraitsComponents.LEADER_TIER));
        if (explicit != null) {
            return explicit;
        }
        return Boolean.TRUE.equals(stack.get(TideTraitsComponents.STEEL_LEADER_ATTACHED)) ? LeaderTier.IRON : null;
    }

    public static void set(Object value, LeaderTier tier) {
        if (!(value instanceof ItemStack stack)) {
            return;
        }
        if (tier == null) {
            clear(stack);
            return;
        }
        stack.set(TideTraitsComponents.LEADER_TIER, tier.id());
        stack.set(TideTraitsComponents.STEEL_LEADER_ATTACHED, Boolean.TRUE);
    }

    public static void clear(ItemStack stack) {
        stack.remove(TideTraitsComponents.LEADER_TIER);
        stack.remove(TideTraitsComponents.STEEL_LEADER_ATTACHED);
    }

    public static boolean has(Object value) {
        return tier(value) != null;
    }

    public static LeaderTier tierOfStack(Object value) {
        if (!(value instanceof ItemStack stack) || stack.isEmpty()) {
            return null;
        }
        if (stack.isOf(TideboundItems.COPPER_LEADER)) {
            return LeaderTier.COPPER;
        }
        if (stack.isOf(TideboundItems.IRON_LEADER)) {
            return LeaderTier.IRON;
        }
        if (stack.isOf(TideboundItems.GOLD_LEADER)) {
            return LeaderTier.GOLD;
        }
        if (stack.isOf(TideboundItems.DIAMOND_LEADER)) {
            return LeaderTier.DIAMOND;
        }
        return null;
    }

    public static boolean isLeaderStack(Object value) {
        return tierOfStack(value) != null;
    }

    public static LeaderTier tierOnHook(TideFishingHook hook) {
        if (hook == null) {
            return null;
        }
        ItemStack rod = hook.getRod();
        LeaderTier attached = tier(rod);
        return attached != null ? attached : tierOfStack(CustomRodManager.getLine(rod));
    }

    public static Item item(LeaderTier tier) {
        return switch (tier) {
            case COPPER -> TideboundItems.COPPER_LEADER;
            case IRON -> TideboundItems.IRON_LEADER;
            case GOLD -> TideboundItems.GOLD_LEADER;
            case DIAMOND -> TideboundItems.DIAMOND_LEADER;
        };
    }
}
