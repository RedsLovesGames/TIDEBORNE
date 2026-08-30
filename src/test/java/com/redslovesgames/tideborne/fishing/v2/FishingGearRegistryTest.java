package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.li64.tide.registries.TideItems;
import com.redslovesgames.tideboundcompatibility.registry.TideboundItems;
import net.minecraft.item.Item;
import org.junit.jupiter.api.Test;

class FishingGearRegistryTest {
    @Test
    void everySupportedFishingGearItemHasOneCanonicalProfile() {
        assertProfile(TideItems.COPPER_LINE, FishingGearRegistry.GearProfile.TIDE_COPPER_LINE);
        assertProfile(TideItems.IRON_LINE, FishingGearRegistry.GearProfile.TIDE_IRON_LINE);
        assertProfile(TideItems.GOLDEN_LINE, FishingGearRegistry.GearProfile.TIDE_GOLDEN_LINE);
        assertProfile(TideItems.DIAMOND_LINE, FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE);

        assertProfile(TideboundItems.TENTACLE_LINE, FishingGearRegistry.GearProfile.TENTACLE_LINE);
        assertProfile(TideboundItems.SWIFT_LINE, FishingGearRegistry.GearProfile.SWIFT_LINE);
        assertProfile(TideboundItems.STEEL_LEADER, FishingGearRegistry.GearProfile.STEEL_LEADER);
        assertProfile(TideboundItems.SEAFARERS_HOOK, FishingGearRegistry.GearProfile.SEAFARERS_HOOK);
        assertProfile(TideboundItems.SHARK_TOOTH_HOOK, FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK);
        assertProfile(TideboundItems.KUJIRA_BONE_FISHING_ROD, FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD);
        assertProfile(TideboundItems.LEVIATHAN_BAIT, FishingGearRegistry.GearProfile.LEVIATHAN_BAIT);

        assertEquals(FishingGearRegistry.GearProfile.values().length, FishingGearRegistry.profiles().size());
    }

    @Test
    void everyCanonicalProfileReversesToTheSameRegisteredItem() {
        for (FishingGearRegistry.GearProfile profile : FishingGearRegistry.GearProfile.values()) {
            Item item = FishingGearRegistry.registeredItem(profile).orElseThrow();
            assertEquals(profile, FishingGearRegistry.resolve(item).orElseThrow());
        }
    }

    @Test
    void unregisteredItemCannotInheritBehaviorByNameClassOrSimilarity() {
        Item lookalike = new Item(new Item.Settings());
        assertTrue(FishingGearRegistry.resolve(lookalike).isEmpty());
    }

    @Test
    void profilesExposeCanonicalOriginAndSlotMetadata() {
        assertEquals(FishingGearRegistry.Origin.TIDE,
                FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE.origin());
        assertEquals(FishingGearRegistry.Slot.LINE,
                FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE.slot());
        assertEquals(FishingGearRegistry.Origin.TIDEBORNE,
                FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK.origin());
        assertEquals(FishingGearRegistry.Slot.HOOK,
                FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK.slot());
        assertEquals(FishingGearRegistry.Slot.ATTACHMENT,
                FishingGearRegistry.GearProfile.STEEL_LEADER.slot());
    }

    private static void assertProfile(Item item, FishingGearRegistry.GearProfile expected) {
        assertEquals(expected, FishingGearRegistry.resolve(item).orElseThrow());
        assertEquals(item, FishingGearRegistry.registeredItem(expected).orElseThrow());
    }
}
