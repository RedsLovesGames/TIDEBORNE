package com.redslovesgames.tideborne.registry;

import com.redslovesgames.tideborne.Tideborne;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Dedicated creative-mode presentation for Tideborne-owned items. */
public final class TideborneItemGroups {
    public static final Identifier TIDEBORNE_ID = Identifier.of(Tideborne.MOD_ID, "tideborne");

    public static final ItemGroup TIDEBORNE = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SatchelRegistration.ANGLERS_SATCHEL))
            .displayName(Text.translatable("itemGroup.tideborne"))
            .entries((context, entries) -> {
                // Storage / progression
                entries.add(SatchelRegistration.ANGLERS_SATCHEL);

                // Rods
                if (TideboundCompatibility.isMythsIntegrationActive()) {
                    entries.add(TideboundItems.KUJIRA_BONE_FISHING_ROD);
                }

                // Lines / leaders
                if (TideboundCompatibility.isMythsIntegrationActive()) {
                    entries.add(TideboundItems.TENTACLE_LINE);
                    entries.add(TideboundItems.SWIFT_LINE);
                }
                if (TideboundCompatibility.isApexIntegrationActive()) {
                    entries.add(TideboundItems.STEEL_LEADER);
                }

                // Hooks
                if (TideboundCompatibility.isMythsIntegrationActive()) {
                    entries.add(TideboundItems.SEAFARERS_HOOK);
                }
                if (TideboundCompatibility.isApexIntegrationActive()) {
                    entries.add(TideboundItems.SHARK_TOOTH_HOOK);
                }

                // Bait / fishing utilities / crafting material
                if (TideboundCompatibility.isMythsIntegrationActive()) {
                    entries.add(TideboundItems.LEVIATHAN_BAIT);
                }
                if (TideboundCompatibility.isApexIntegrationActive()) {
                    entries.add(TideboundItems.CHUM_BUCKET);
                    entries.add(TideboundItems.SHARK_TOOTH);
                }
            })
            .build();

    private static boolean initialized;

    private TideborneItemGroups() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        Registry.register(Registries.ITEM_GROUP, TIDEBORNE_ID, TIDEBORNE);
    }
}
