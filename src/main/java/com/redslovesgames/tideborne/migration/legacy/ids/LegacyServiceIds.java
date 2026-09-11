package com.redslovesgames.tideborne.migration.legacy.ids;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/** Historical non-registry service and extension identities retained for compatibility. */
public final class LegacyServiceIds {
    public static final Identifier MUTATION_TEXTURES_RELOAD_LISTENER = LegacyNamespaces.tideTraits("mutation_textures");
    public static final Identifier NORMAL_MUTATION = LegacyNamespaces.tideTraits("normal");

    private LegacyServiceIds() {
    }

    public static Identifier mutation(String path) {
        return LegacyNamespaces.tideTraits(path);
    }

    public static TagKey<Item> tideTraitsItemTag(String path) {
        return TagKey.of(RegistryKeys.ITEM, LegacyNamespaces.tideTraits(path));
    }
}
