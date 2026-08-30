package com.redslovesgames.tideborne.fishing.v2;

import com.li64.tide.registries.TideItems;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Canonical identity registry for fishing gear consumed by Fishing System 2.0.
 *
 * <p>Resolution is deliberately based on the registered {@link Item} instance rather than display
 * names, translation keys, class names, or substring matching. A visually or textually similar item
 * therefore cannot inherit Tideborne fishing behavior unless it is explicitly registered here.
 */
public final class FishingGearRegistry {
    private static final Map<Item, GearProfile> BY_ITEM = createProfiles();
    private static final Set<GearProfile> PROFILES =
            Collections.unmodifiableSet(EnumSet.allOf(GearProfile.class));

    private FishingGearRegistry() {
    }

    public static Optional<GearProfile> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return resolve(stack.getItem());
    }

    public static Optional<GearProfile> resolve(Item item) {
        if (item == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_ITEM.get(item));
    }

    public static boolean matches(ItemStack stack, GearProfile profile) {
        return profile != null && resolve(stack).filter(profile::equals).isPresent();
    }

    public static Set<GearProfile> profiles() {
        return PROFILES;
    }

    private static Map<Item, GearProfile> createProfiles() {
        IdentityHashMap<Item, GearProfile> profiles = new IdentityHashMap<>();

        register(profiles, TideItems.COPPER_LINE, GearProfile.TIDE_COPPER_LINE);
        register(profiles, TideItems.IRON_LINE, GearProfile.TIDE_IRON_LINE);
        register(profiles, TideItems.GOLDEN_LINE, GearProfile.TIDE_GOLDEN_LINE);
        register(profiles, TideItems.DIAMOND_LINE, GearProfile.TIDE_DIAMOND_LINE);

        register(profiles, TideboundItems.TENTACLE_LINE, GearProfile.TENTACLE_LINE);
        register(profiles, TideboundItems.SWIFT_LINE, GearProfile.SWIFT_LINE);
        register(profiles, TideboundItems.STEEL_LEADER, GearProfile.STEEL_LEADER);
        register(profiles, TideboundItems.SEAFARERS_HOOK, GearProfile.SEAFARERS_HOOK);
        register(profiles, TideboundItems.SHARK_TOOTH_HOOK, GearProfile.SHARK_TOOTH_HOOK);
        register(profiles, TideboundItems.KUJIRA_BONE_FISHING_ROD, GearProfile.KUJIRA_BONE_FISHING_ROD);
        register(profiles, TideboundItems.LEVIATHAN_BAIT, GearProfile.LEVIATHAN_BAIT);

        return Collections.unmodifiableMap(profiles);
    }

    private static void register(Map<Item, GearProfile> profiles, Item item, GearProfile profile) {
        GearProfile previous = profiles.put(item, profile);
        if (previous != null) {
            throw new IllegalStateException("Fishing gear item registered twice: " + previous + " and " + profile);
        }
    }

    public enum Origin {
        TIDE,
        TIDEBORNE
    }

    public enum Slot {
        LINE,
        HOOK,
        ROD,
        BAIT,
        ATTACHMENT
    }

    public enum GearProfile {
        TIDE_COPPER_LINE(Origin.TIDE, Slot.LINE),
        TIDE_IRON_LINE(Origin.TIDE, Slot.LINE),
        TIDE_GOLDEN_LINE(Origin.TIDE, Slot.LINE),
        TIDE_DIAMOND_LINE(Origin.TIDE, Slot.LINE),
        TENTACLE_LINE(Origin.TIDEBORNE, Slot.LINE),
        SWIFT_LINE(Origin.TIDEBORNE, Slot.LINE),
        STEEL_LEADER(Origin.TIDEBORNE, Slot.ATTACHMENT),
        SEAFARERS_HOOK(Origin.TIDEBORNE, Slot.HOOK),
        SHARK_TOOTH_HOOK(Origin.TIDEBORNE, Slot.HOOK),
        KUJIRA_BONE_FISHING_ROD(Origin.TIDEBORNE, Slot.ROD),
        LEVIATHAN_BAIT(Origin.TIDEBORNE, Slot.BAIT);

        private final Origin origin;
        private final Slot slot;

        GearProfile(Origin origin, Slot slot) {
            this.origin = origin;
            this.slot = slot;
        }

        public Origin origin() {
            return origin;
        }

        public Slot slot() {
            return slot;
        }
    }
}
