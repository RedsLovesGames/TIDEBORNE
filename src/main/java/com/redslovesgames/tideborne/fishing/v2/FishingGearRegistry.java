package com.redslovesgames.tideborne.fishing.v2;

import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** Exact-ID canonical identity registry for Fishing System 2.0 gear. */
public final class FishingGearRegistry {
    private static final Map<Identifier, GearProfile> BY_ID = createProfiles();
    private static final Map<GearProfile, Identifier> BY_PROFILE = invertProfiles(BY_ID);
    private static final Set<GearProfile> PROFILES = registeredProfiles(BY_PROFILE);
    private static final Set<Identifier> TIDE_BOBBER_IDS = createTideBobberIds();

    private FishingGearRegistry() {}

    public static Optional<GearProfile> resolve(ItemStack stack) {
        return stack == null || stack.isEmpty() ? Optional.empty() : resolve(stack.getItem());
    }
    public static Optional<GearProfile> resolve(Item item) {
        return item == null ? Optional.empty() : resolveId(Registries.ITEM.getId(item));
    }
    public static Optional<GearProfile> resolveId(Identifier itemId) {
        return itemId == null ? Optional.empty() : Optional.ofNullable(BY_ID.get(itemId));
    }
    public static Optional<Identifier> registeredId(GearProfile profile) {
        return profile == null ? Optional.empty() : Optional.ofNullable(BY_PROFILE.get(profile));
    }
    public static boolean matches(ItemStack stack, GearProfile profile) {
        return profile != null && resolve(stack).filter(profile::equals).isPresent();
    }
    public static Set<GearProfile> profiles() { return PROFILES; }
    public static Set<Identifier> supportedBobberIds() { return TIDE_BOBBER_IDS; }
    public static boolean isSupportedBobberId(Identifier itemId) { return itemId != null && TIDE_BOBBER_IDS.contains(itemId); }
    public static boolean isSupportedBobber(ItemStack stack) {
        return stack != null && !stack.isEmpty() && isSupportedBobberId(Registries.ITEM.getId(stack.getItem()));
    }
    public static Optional<Slot> resolveSlot(Identifier itemId) {
        if (itemId == null) return Optional.empty();
        GearProfile profile = BY_ID.get(itemId);
        if (profile != null) return Optional.of(profile.slot());
        return TIDE_BOBBER_IDS.contains(itemId) ? Optional.of(Slot.BOBBER) : Optional.empty();
    }

    private static Map<Identifier, GearProfile> createProfiles() {
        LinkedHashMap<Identifier, GearProfile> profiles = new LinkedHashMap<>();
        for (GearProfile profile : GearProfile.values()) {
            GearProfile previous = profiles.put(profile.itemId(), profile);
            if (previous != null) throw new IllegalStateException("Fishing gear item ID registered twice: " + profile.itemId());
        }
        return Collections.unmodifiableMap(profiles);
    }
    private static Set<Identifier> createTideBobberIds() {
        LinkedHashSet<Identifier> ids = new LinkedHashSet<>();
        for (String path : List.of(
                "red_bobber","orange_bobber","yellow_bobber","lime_bobber","green_bobber","cyan_bobber","light_blue_bobber","blue_bobber",
                "purple_bobber","magenta_bobber","pink_bobber","white_bobber","light_gray_bobber","gray_bobber","black_bobber","brown_bobber",
                "golden_apple_bobber","enchanted_golden_apple_bobber","iron_bobber","golden_bobber","diamond_bobber","netherite_bobber","amethyst_bobber",
                "echo_bobber","chorus_bobber","feather_bobber","lichen_bobber","nautilus_bobber","pearl_bobber","heart_bobber","grassy_bobber","duck_bobber")) {
            ids.add(Identifier.of("tide", path));
        }
        return Collections.unmodifiableSet(ids);
    }
    private static Map<GearProfile, Identifier> invertProfiles(Map<Identifier, GearProfile> profiles) {
        EnumMap<GearProfile, Identifier> result = new EnumMap<>(GearProfile.class);
        profiles.forEach((id, profile) -> result.put(profile, id));
        return Collections.unmodifiableMap(result);
    }
    private static Set<GearProfile> registeredProfiles(Map<GearProfile, Identifier> profiles) {
        EnumSet<GearProfile> result = EnumSet.noneOf(GearProfile.class);
        result.addAll(profiles.keySet());
        return Collections.unmodifiableSet(result);
    }

    public enum Origin { TIDE, TIDEBORNE }
    public enum Slot { LINE, HOOK, ROD, BAIT, ATTACHMENT, BOBBER }
    public enum GearProfile {
        TIDE_COPPER_LINE("tide","copper_line",Origin.TIDE,Slot.LINE),
        TIDE_IRON_LINE("tide","iron_line",Origin.TIDE,Slot.LINE),
        TIDE_GOLDEN_LINE("tide","golden_line",Origin.TIDE,Slot.LINE),
        TIDE_DIAMOND_LINE("tide","diamond_line",Origin.TIDE,Slot.LINE),
        TENTACLE_LINE("tidebound_compatibility","tentacle_line",Origin.TIDEBORNE,Slot.LINE),
        SWIFT_LINE("tidebound_compatibility","swift_line",Origin.TIDEBORNE,Slot.LINE),
        COPPER_LEADER("tidebound_compatibility","copper_leader",Origin.TIDEBORNE,Slot.ATTACHMENT),
        IRON_LEADER("tidebound_compatibility","steel_leader",Origin.TIDEBORNE,Slot.ATTACHMENT),
        GOLD_LEADER("tidebound_compatibility","gold_leader",Origin.TIDEBORNE,Slot.ATTACHMENT),
        DIAMOND_LEADER("tidebound_compatibility","diamond_leader",Origin.TIDEBORNE,Slot.ATTACHMENT),
        SEAFARERS_HOOK("tidebound_compatibility","seafarers_hook",Origin.TIDEBORNE,Slot.HOOK),
        SHARK_TOOTH_HOOK("tidebound_compatibility","shark_tooth_hook",Origin.TIDEBORNE,Slot.HOOK),
        KUJIRA_BONE_FISHING_ROD("tidebound_compatibility","kujira_bone_fishing_rod",Origin.TIDEBORNE,Slot.ROD),
        LEVIATHAN_BAIT("tidebound_compatibility","leviathan_bait",Origin.TIDEBORNE,Slot.BAIT);
        private final Identifier itemId; private final Origin origin; private final Slot slot;
        GearProfile(String namespace,String path,Origin origin,Slot slot){this.itemId=Identifier.of(namespace,path);this.origin=origin;this.slot=slot;}
        public Identifier itemId(){return itemId;} public Origin origin(){return origin;} public Slot slot(){return slot;}
    }
}
