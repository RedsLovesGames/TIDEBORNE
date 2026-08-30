package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

class FishingGearRegistryTest {
    @Test
    void everySupportedFishingGearIdHasOneCanonicalProfile() {
        assertProfile("tide:copper_line", FishingGearRegistry.GearProfile.TIDE_COPPER_LINE);
        assertProfile("tide:iron_line", FishingGearRegistry.GearProfile.TIDE_IRON_LINE);
        assertProfile("tide:golden_line", FishingGearRegistry.GearProfile.TIDE_GOLDEN_LINE);
        assertProfile("tide:diamond_line", FishingGearRegistry.GearProfile.TIDE_DIAMOND_LINE);

        assertProfile("tidebound_compatibility:tentacle_line", FishingGearRegistry.GearProfile.TENTACLE_LINE);
        assertProfile("tidebound_compatibility:swift_line", FishingGearRegistry.GearProfile.SWIFT_LINE);
        assertProfile("tidebound_compatibility:steel_leader", FishingGearRegistry.GearProfile.STEEL_LEADER);
        assertProfile("tidebound_compatibility:seafarers_hook", FishingGearRegistry.GearProfile.SEAFARERS_HOOK);
        assertProfile("tidebound_compatibility:shark_tooth_hook", FishingGearRegistry.GearProfile.SHARK_TOOTH_HOOK);
        assertProfile("tidebound_compatibility:kujira_bone_fishing_rod", FishingGearRegistry.GearProfile.KUJIRA_BONE_FISHING_ROD);
        assertProfile("tidebound_compatibility:leviathan_bait", FishingGearRegistry.GearProfile.LEVIATHAN_BAIT);

        assertEquals(FishingGearRegistry.GearProfile.values().length, FishingGearRegistry.profiles().size());
    }

    @Test
    void everyCanonicalProfileReversesToTheSameExactId() {
        for (FishingGearRegistry.GearProfile profile : FishingGearRegistry.GearProfile.values()) {
            Identifier itemId = FishingGearRegistry.registeredId(profile).orElseThrow();
            assertEquals(profile, FishingGearRegistry.resolveId(itemId).orElseThrow());
            assertEquals(profile.itemId(), itemId);
        }
    }

    @Test
    void unregisteredLookalikeIdsCannotInheritBehavior() {
        assertTrue(FishingGearRegistry.resolveId(Identifier.of("othermod", "diamond_line")).isEmpty());
        assertTrue(FishingGearRegistry.resolveId(Identifier.of("tide", "diamond_line_extra")).isEmpty());
        assertTrue(FishingGearRegistry.resolveId(Identifier.of("tidebound_compatibility", "swift_line_plus")).isEmpty());
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

    private static void assertProfile(String itemId, FishingGearRegistry.GearProfile expected) {
        Identifier id = Identifier.of(itemId);
        assertEquals(expected, FishingGearRegistry.resolveId(id).orElseThrow());
        assertEquals(id, FishingGearRegistry.registeredId(expected).orElseThrow());
    }
}
