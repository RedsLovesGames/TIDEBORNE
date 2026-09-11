package com.redslovesgames.tideborne.migration.legacy.ids;

import net.minecraft.util.Identifier;

/** Frozen historical registry identities retained for persisted ItemStacks, entities, and components. */
public final class LegacyRegistryIds {
    public static final Identifier ANGLERS_SATCHEL = LegacyNamespaces.tideTraits("anglers_satchel");

    public static final Identifier TENTACLE_LINE = LegacyNamespaces.tideboundCompatibility("tentacle_line");
    public static final Identifier SEAFARERS_HOOK = LegacyNamespaces.tideboundCompatibility("seafarers_hook");
    public static final Identifier SWIFT_LINE = LegacyNamespaces.tideboundCompatibility("swift_line");
    public static final Identifier KUJIRA_BONE_FISHING_ROD = LegacyNamespaces.tideboundCompatibility("kujira_bone_fishing_rod");
    public static final Identifier LEVIATHAN_BAIT = LegacyNamespaces.tideboundCompatibility("leviathan_bait");
    public static final Identifier CHUM_BUCKET = LegacyNamespaces.tideboundCompatibility("chum_bucket");
    public static final Identifier COPPER_LEADER = LegacyNamespaces.tideboundCompatibility("copper_leader");
    public static final Identifier STEEL_LEADER = LegacyNamespaces.tideboundCompatibility("steel_leader");
    public static final Identifier GOLD_LEADER = LegacyNamespaces.tideboundCompatibility("gold_leader");
    public static final Identifier DIAMOND_LEADER = LegacyNamespaces.tideboundCompatibility("diamond_leader");
    public static final Identifier SHARK_TOOTH = LegacyNamespaces.tideboundCompatibility("shark_tooth");
    public static final Identifier SHARK_TOOTH_HOOK = LegacyNamespaces.tideboundCompatibility("shark_tooth_hook");
    public static final Identifier CHUM_PROJECTILE = LegacyNamespaces.tideboundCompatibility("chum_projectile");

    private LegacyRegistryIds() {
    }

    public static Identifier tideTraitsComponent(String path) {
        return LegacyNamespaces.tideTraits(path);
    }
}
