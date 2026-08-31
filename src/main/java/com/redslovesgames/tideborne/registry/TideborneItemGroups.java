package com.redslovesgames.tideborne.registry;

import com.redslovesgames.tideborne.Tideborne;
import com.redslovesgames.tideboundcompatibility.TideboundCompatibility;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import com.redslovesgames.tidetraits.satchel.SatchelRegistration;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
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
            .entries((context, entries) -> currentCreativeItems().forEach(entries::add))
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

    /**
     * Returns the exact curated item order for the integrations active in this runtime.
     * The returned list is immutable so creative presentation cannot mutate registry state.
     */
    public static List<Item> currentCreativeItems() {
        return creativeItems(
                TideboundCompatibility.isMythsIntegrationActive(),
                TideboundCompatibility.isApexIntegrationActive()
        );
    }

    /**
     * Pure visibility/order contract used by the creative tab and compatibility-matrix tests.
     */
    public static List<Item> creativeItems(boolean mythsActive, boolean apexActive) {
        List<Item> items = new ArrayList<>();

        // Storage / progression
        items.add(SatchelRegistration.ANGLERS_SATCHEL);

        // Rods
        if (mythsActive) {
            items.add(TideboundItems.KUJIRA_BONE_FISHING_ROD);
        }

        // Lines / leaders
        if (mythsActive) {
            items.add(TideboundItems.TENTACLE_LINE);
            items.add(TideboundItems.SWIFT_LINE);
        }
        if (apexActive) {
            items.add(TideboundItems.STEEL_LEADER);
        }

        // Hooks
        if (mythsActive) {
            items.add(TideboundItems.SEAFARERS_HOOK);
        }
        if (apexActive) {
            items.add(TideboundItems.SHARK_TOOTH_HOOK);
        }

        // Bait / fishing utilities / crafting material
        if (mythsActive) {
            items.add(TideboundItems.LEVIATHAN_BAIT);
        }
        if (apexActive) {
            items.add(TideboundItems.CHUM_BUCKET);
            items.add(TideboundItems.SHARK_TOOTH);
        }

        return List.copyOf(items);
    }
}
