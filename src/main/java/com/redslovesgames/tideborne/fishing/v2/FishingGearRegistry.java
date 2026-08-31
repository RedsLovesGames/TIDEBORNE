package com.redslovesgames.tideborne.fishing.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Canonical identity registry for fishing gear consumed by Fishing System 2.0.
 *
 * <p>Resolution is deliberately based on exact registered item IDs rather than display names,
 * translation keys, class names, tags alone, or substring matching. A visually or textually similar
 * item therefore cannot inherit Tideborne fishing behavior unless its exact namespaced ID is known.
 */
public final class FishingGearRegistry {
    private static final Map<Identifier, GearProfile> BY_ID = createProfiles();
    private static final Map<GearProfile, Identifier> BY_PROFILE = invertProfiles(BY_ID);
    private static final Set<GearProfile> PROFILES = registeredProfiles(BY_PROFILE);
    private static final Set<Identifier> TIDE_BOBBER_IDS = createTideBobberIds();

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
        return resolveId(Registries.ITEM.getId(item));
    }

    /** Exact-ID lookup used by diagnostics and plain unit tests without bootstrapping game registries. */
    public static Optional<GearProfile> resolveId(Identifier itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_ID.get(itemId));
    }

    public static Optional<Identifier> registeredId(GearProfile profile) {
        if (profile == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_PROFILE.get(profile));
    }

    public static boolean matches(ItemStack stack, GearProfile profile) {
        return profile != null && resolve(stack).filter(profile::equals).isPresent();
    }

    public static Set<GearProfile> profiles() {
        return PROFILES;
    }

    /** Current exact Tide bobber IDs that may receive server-configured Fishing System 2.0 bonuses. */
    public static Set<Identifier> supportedBobberIds() {
        return TIDE_BOBBER_IDS;
    }

    public static boolean isSupportedBobberId(Identifier itemId) {
        return itemId != null && TIDE_BOBBER_IDS.contains(itemId);
    }

    public static boolean isSupportedBobber(ItemStack stack) {
        return stack != null && !stack.isEmpty() && isSupportedBobberId(Registries.ITEM.getId(stack.getItem()));
    }

    public static Optional<Slot> resolveSlot(Identifier itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        GearProfile profile = BY_ID.get(itemId);
        if (profile != null) {
            return Optional.of(profile.slot());
        }
        return TIDE_BOBBER_IDS.contains(itemId) ? Optional.of(Slot.BOBBER) : Optional.empty();
    }

    private static Map<Identifier, GearProfile> createProfiles() {
        LinkedHashMap<Identifier, GearProfile> profiles = new LinkedHashMap<>();
        for (GearProfile profile : GearProfile.values()) {
            GearProfile previous = profiles.put(profile.itemId(), profile);
            if (previous != null) {
                throw new IllegalStateException(
                        "Fishing gear item ID registered twice: " + profile.itemId()
                                + " for " + previous + " and " + profile
                );
            }
        }
        return Collections.unmodifiableMap(profiles);
    }

    private static Set<Identifier> createTideBobberIds() {
        LinkedHashSet<Identifier> ids = new LinkedHashSet<>();
        for (String path : List.of(
                "red_bobber", "orange_bobber", "yellow_bobber", "lime_bobber",
                "green_bobber", "cyan_bobber", "light_blue_bobber", "blue_bobber",
                "purple_bobber", "magenta_bobber", "pink_bobber", "white_bobber",
                "light_gray_bobber", "gray_bobber", "black_bobber", "brown_bobber",
                "golden_apple_bobber", "enchanted_golden_apple_bobber", "iron_bobber",
                "golden_bobber", "diamond_bobber", "netherite_bobber", "amethyst_bobber",
                "echo_bobber", "chorus_bobber", "feather_bobber", "lichen_bobber",
                "nautilus_bobber", "pearl_bobber", "heart_bobber", "grassy_bobber", "duck_bobber"
        )) {
            ids.add(Identifier.of("tide", path));
        }
        return Collections.unmodifiableSet(ids);
    }

    private static Map<GearProfile, Identifier> invertProfiles(Map<Identifier, GearProfile> profiles) {
        EnumMap<GearProfile, Identifier> byProfile = new EnumMap<>(GearProfile.class);
        profiles.forEach((itemId, profile) -> {
            Identifier previous = byProfile.put(profile, itemId);
            if (previous != null) {
                throw new IllegalStateException("Fishing gear profile registered twice: " + profile);
            }
        });
        if (byProfile.size() != GearProfile.values().length) {
            throw new IllegalStateException(
                    "Fishing gear profile registry is incomplete: expected "
                            + GearProfile.values().length + ", found " + byProfile.size()
            );
        }
        return Collections.unmodifiableMap(byProfile);
    }

    private static Set<GearProfile> registeredProfiles(Map<GearProfile, Identifier> profiles) {
        EnumSet<GearProfile> registered = EnumSet.noneOf(GearProfile.class);
        registered.addAll(profiles.keySet());
        return Collections.unmodifiableSet(registered);
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
        ATTACHMENT,
        BOBBER
    }

    public enum GearProfile {
        TIDE_COPPER_LINE("tide", "copper_line", Origin.TIDE, Slot.LINE),
        TIDE_IRON_LINE("tide", "iron_line", Origin.TIDE, Slot.LINE),
        TIDE_GOLDEN_LINE("tide", "golden_line", Origin.TIDE, Slot.LINE),
        TIDE_DIAMOND_LINE("tide", "diamond_line", Origin.TIDE, Slot.LINE),
        TENTACLE_LINE("tidebound_compatibility", "tentacle_line", Origin.TIDEBORNE, Slot.LINE),
        SWIFT_LINE("tidebound_compatibility", "swift_line", Origin.TIDEBORNE, Slot.LINE),
        STEEL_LEADER("tidebound_compatibility", "steel_leader", Origin.TIDEBORNE, Slot.ATTACHMENT),
        SEAFARERS_HOOK("tidebound_compatibility", "seafarers_hook", Origin.TIDEBORNE, Slot.HOOK),
        SHARK_TOOTH_HOOK("tidebound_compatibility", "shark_tooth_hook", Origin.TIDEBORNE, Slot.HOOK),
        KUJIRA_BONE_FISHING_ROD("tidebound_compatibility", "kujira_bone_fishing_rod", Origin.TIDEBORNE, Slot.ROD),
        LEVIATHAN_BAIT("tidebound_compatibility", "leviathan_bait", Origin.TIDEBORNE, Slot.BAIT);

        private final Identifier itemId;
        private final Origin origin;
        private final Slot slot;

        GearProfile(String namespace, String path, Origin origin, Slot slot) {
            this.itemId = Identifier.of(namespace, path);
            this.origin = origin;
            this.slot = slot;
        }

        public Identifier itemId() {
            return itemId;
        }

        public Origin origin() {
            return origin;
        }

        public Slot slot() {
            return slot;
        }
    }
}
