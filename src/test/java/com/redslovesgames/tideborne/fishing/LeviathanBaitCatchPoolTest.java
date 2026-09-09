package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.gear.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.gear.LeviathanBaitRules;
import com.redslovesgames.tideborne.fishing.gear.TideborneFishingGearModifiers;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LeviathanBaitCatchPoolTest {
    @Test
    void emptyFishResultDoesNotRetryOrConsumeTheNormalSelector() {
        AtomicInteger fishCalls = new AtomicInteger();
        AtomicInteger normalCalls = new AtomicInteger();
        String result = LeviathanBaitRules.selectCatch(TideborneFishingGearModifiers.leviathanBait(true),
                () -> { normalCalls.incrementAndGet(); return "fallback"; },
                () -> { fishCalls.incrementAndGet(); return null; });
        org.junit.jupiter.api.Assertions.assertNull(result);
        assertEquals(1, fishCalls.get());
        assertEquals(0, normalCalls.get());
    }

    @Test
    void activeLeviathanBaitRestrictsCatchCategoriesToFishOnly() {
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(true);
        FishingGearModifiers.IdRestriction categories = modifiers.categoryRestriction();

        assertTrue(categories.allowListActive());
        assertEquals(Set.of(TideborneFishingGearModifiers.FISH_CATCH_CATEGORY), categories.allowedIds());
        assertTrue(categories.allows("fish"));
        assertFalse(categories.allows("crate"));
        assertFalse(categories.allows("item"));
        assertFalse(categories.allows("junk"));
        assertFalse(categories.allows("treasure"));
        assertTrue(LeviathanBaitRules.isFishOnlyCatchPool(modifiers));
    }

    @Test
    void activeLeviathanBaitUsesOnlyTheFishSelectorBranch() {
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(true);
        AtomicInteger normalCalls = new AtomicInteger();
        AtomicInteger fishCalls = new AtomicInteger();

        String selected = LeviathanBaitRules.selectCatch(
                modifiers,
                () -> {
                    normalCalls.incrementAndGet();
                    return "non-fish";
                },
                () -> {
                    fishCalls.incrementAndGet();
                    return "fish";
                }
        );

        assertEquals("fish", selected);
        assertEquals(0, normalCalls.get());
        assertEquals(1, fishCalls.get());
    }

    @Test
    void inactiveLeviathanBaitLeavesTheNormalCatchPoolUntouched() {
        FishingGearModifiers modifiers = TideborneFishingGearModifiers.leviathanBait(false);
        FishingGearModifiers.IdRestriction categories = modifiers.categoryRestriction();
        AtomicInteger normalCalls = new AtomicInteger();
        AtomicInteger fishCalls = new AtomicInteger();

        assertFalse(categories.allowListActive());
        assertTrue(categories.allows("fish"));
        assertTrue(categories.allows("crate"));
        assertTrue(categories.allows("item"));
        assertFalse(LeviathanBaitRules.isFishOnlyCatchPool(modifiers));

        String selected = LeviathanBaitRules.selectCatch(
                modifiers,
                () -> {
                    normalCalls.incrementAndGet();
                    return "normal-pool";
                },
                () -> {
                    fishCalls.incrementAndGet();
                    return "fish";
                }
        );

        assertEquals("normal-pool", selected);
        assertEquals(1, normalCalls.get());
        assertEquals(0, fishCalls.get());
    }
}
