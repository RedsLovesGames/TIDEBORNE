package com.redslovesgames.tideborne.fishing.v2;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
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
 * translation keys, class names, or substring matching. A visually or textually similar item
 * therefore cannot inherit Tideborne fishing behavior unless its exact namespaced ID has a profile.
 */
public final class FishingGearRegistry {
    private static final Map<Identifier, GearProfile> BY_ID = createProfiles();
    private static final Map<GearProfile, Identifier> BY_PROFILE = invertProfiles(BY_ID);
    private static final Set<GearProfile> PROFILES = registeredProfiles(BY_PROFILE);

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
        ATTACHMENT
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
