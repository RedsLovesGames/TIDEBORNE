package com.redslovesgames.tideborne.fishing.gear;

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
    private static final Map<Identifier, FishingGearModifiers> BOBBERS = createBobbers();
    private static final Set<Identifier> TIDE_BOBBER_IDS = BOBBERS.keySet();

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
    public static Optional<FishingGearModifiers> bobberModifiers(Identifier id) { return Optional.ofNullable(BOBBERS.get(id)); }
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

    /** Inventory compatibility uses canonical identities first, then Tide's native accessory tags. */
    public static boolean accepts(Slot slot, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.getItem().canBeNested()) return false;
        Optional<Slot> exact = resolveSlot(Registries.ITEM.getId(stack.getItem()));
        if (exact.isPresent()) return exact.get() == slot;
        return switch (slot) {
            case ROD -> stack.isIn(com.li64.tide.data.TideTags.Items.FISHING_RODS);
            case LINE -> stack.isIn(com.li64.tide.data.TideTags.Items.LINES);
            case HOOK -> stack.isIn(com.li64.tide.data.TideTags.Items.HOOKS);
            case BOBBER -> stack.isIn(com.li64.tide.data.TideTags.Items.BOBBERS);
            case BAIT -> com.li64.tide.util.BaitUtils.isBait(stack);
            case ATTACHMENT -> false;
        };
    }

    private static Map<Identifier, GearProfile> createProfiles() {
        LinkedHashMap<Identifier, GearProfile> profiles = new LinkedHashMap<>();
        for (GearProfile profile : GearProfile.values()) {
            GearProfile previous = profiles.put(profile.itemId(), profile);
            if (previous != null) throw new IllegalStateException("Fishing gear item ID registered twice: " + profile.itemId());
        }
        return Collections.unmodifiableMap(profiles);
    }
    private static Map<Identifier, FishingGearModifiers> createBobbers() {
        LinkedHashMap<Identifier, FishingGearModifiers> profiles = new LinkedHashMap<>();
        for (String path : List.of(
                "red_bobber","orange_bobber","yellow_bobber","lime_bobber","green_bobber","cyan_bobber","light_blue_bobber","blue_bobber",
                "purple_bobber","magenta_bobber","pink_bobber","white_bobber","light_gray_bobber","gray_bobber","black_bobber","brown_bobber")) {
            profiles.put(Identifier.of("tide", path), bobber(0, 0, 1, 1, 1, 0));
        }
        profiles.put(Identifier.of("tide", "golden_bobber"), bobber(2, 0, 0, 1, 1, 0));
        profiles.put(Identifier.of("tide", "golden_apple_bobber"), bobber(2, 0, 2, 1, 1, 0));
        profiles.put(Identifier.of("tide", "enchanted_golden_apple_bobber"), bobber(5, 0, 0, 1, 1, 0));
        profiles.put(Identifier.of("tide", "iron_bobber"), bobber(0, 0, 0, 1.05, 1, .05));
        profiles.put(Identifier.of("tide", "diamond_bobber"), bobber(0, 0, 0, 1.08, 1, .10));
        profiles.put(Identifier.of("tide", "netherite_bobber"), bobber(0, 0, 1, 1.10, 1, .15));
        profiles.put(Identifier.of("tide", "lichen_bobber"), bobber(0, 0, 1, 1.03, 1, 0));
        profiles.put(Identifier.of("tide", "grassy_bobber"), bobber(0, 0, 0, 1.04, 1, 0));
        profiles.put(Identifier.of("tide", "amethyst_bobber"), bobber(0, 1, 1, 1, 1, 0));
        profiles.put(Identifier.of("tide", "echo_bobber"), bobber(0, 2, 1, .96, 1, 0));
        profiles.put(Identifier.of("tide", "feather_bobber"), bobber(0, 0, 2, 1, 1, 0));
        profiles.put(Identifier.of("tide", "chorus_bobber"), bobber(0, 0, 3, .94, 1, 0));
        profiles.put(Identifier.of("tide", "duck_bobber"), bobber(0, 0, 0, 1, 1.10, 0));
        profiles.put(Identifier.of("tide", "nautilus_bobber"), bobber(1, 0, 0, 1, 1.25, 0));
        profiles.put(Identifier.of("tide", "heart_bobber"), bobber(0, 0, 0, .95, 1.40, 0));
        profiles.put(Identifier.of("tide", "pearl_bobber"), bobber(1, 0, 2, 1, 1, 0));
        return Collections.unmodifiableMap(profiles);
    }
    private static FishingGearModifiers bobber(double luck, double traits, double lure, double zone, double crate, double protection) {
        return FishingGearModifiers.builder().fishingLuck(luck).traitLuck(traits)
                .namedAdditiveModifier(FishingGearEffects.LURE_BONUS, lure)
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, zone)
                .namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER, crate)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, protection)
                .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES, protection > 0 ? 1 : 0).build();
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

    public enum Origin { MINECRAFT, TIDE, TIDEBORNE }
    public enum Slot { LINE, HOOK, ROD, BAIT, ATTACHMENT, BOBBER }
    public enum GearProfile {
        WOOD_ROD("minecraft","fishing_rod",Origin.MINECRAFT,Slot.ROD),
        IRON_ROD("tide","iron_fishing_rod",Origin.TIDE,Slot.ROD),
        GOLD_ROD("tide","golden_fishing_rod",Origin.TIDE,Slot.ROD),
        DIAMOND_ROD("tide","diamond_fishing_rod",Origin.TIDE,Slot.ROD),
        NETHERITE_ROD("tide","netherite_fishing_rod",Origin.TIDE,Slot.ROD),
        TIDE_BASE_LINE("tide","fishing_line",Origin.TIDE,Slot.LINE),
        TIDE_BASE_HOOK("tide","fishing_hook",Origin.TIDE,Slot.HOOK),
        FIERY_HOOK("tide","fiery_hook",Origin.TIDE,Slot.HOOK),
        PERMAFROST_HOOK("tide","permafrost_hook",Origin.TIDE,Slot.HOOK),
        TWILIGHT_HOOK("tide","twilight_hook",Origin.TIDE,Slot.HOOK),
        LAVAPROOF_HOOK("tide","lavaproof_hook",Origin.TIDE,Slot.HOOK),
        VOID_HOOK("tide","void_hook",Origin.TIDE,Slot.HOOK),
        NORMAL_BAIT("tide","bait",Origin.TIDE,Slot.BAIT),
        LUCKY_BAIT("tide","lucky_bait",Origin.TIDE,Slot.BAIT),
        MAGNETIC_BAIT("tide","magnetic_bait",Origin.TIDE,Slot.BAIT),
        INCANDESCENT_BAIT("tide","incandescent_bait",Origin.TIDE,Slot.BAIT),
        ABYSS_BAIT("tide","abyss_bait",Origin.TIDE,Slot.BAIT),
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

        /** Additional targeting only. Native Tide still supplies bait luck/lure/crate effects. */
        public FishingGearModifiers baitTargetModifiers() {
            return switch (this) {
                case INCANDESCENT_BAIT -> FishingGearModifiers.builder().targetWeight("warm", 1.40).build();
                case ABYSS_BAIT -> FishingGearModifiers.builder().targetWeight("deep", 1.40).build();
                default -> FishingGearModifiers.neutral();
            };
        }

        /** Frozen rod contributions; native Gold luck is deliberately absent. */
        public FishingGearModifiers rodModifiers() {
            var builder = FishingGearModifiers.builder();
            switch (this) {
                case WOOD_ROD -> { }
                case IRON_ROD -> builder.namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 1.04);
                case GOLD_ROD -> builder.namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 0.97);
                case DIAMOND_ROD -> builder.strengthMultiplier(0.92).trophyFightRelief(0.25);
                case NETHERITE_ROD -> builder.tempoMultiplier(0.95).trophyFightRelief(0.10);
                case KUJIRA_BONE_FISHING_ROD -> builder.strengthMultiplier(0.88).tempoMultiplier(1.10)
                        .targetWeight("kujira_target", 1.35)
                        .namedMultiplierModifier(FishingGearEffects.CRATE_WEIGHT_MULTIPLIER, 0.70);
                default -> { return FishingGearModifiers.neutral(); }
            }
            double protection = switch (this) {
                case IRON_ROD -> 0.05;
                case GOLD_ROD -> 0.02;
                case DIAMOND_ROD -> 0.10;
                case NETHERITE_ROD -> 0.15;
                default -> 0;
            };
            if (protection > 0) builder.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PREVENTION_CHANCE, protection)
                    .namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES, 1);
            return builder.build();
        }
    }
}
