package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideboundcompatibility.config.TideboundConfig;
import com.redslovesgames.tideboundcompatibility.fishing.SteelLeaderGearModifiers;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SteelLeaderGearModifiersTest {
    @Test
    void attachedSteelLeaderMapsExactCurrentBehavior() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = SteelLeaderGearModifiers.forAttachmentState(true, config);

        assertEquals(0.90D, FishingGearEffects.catchZoneAreaMultiplier(modifiers), 1.0e-12);
        assertEquals(1.05D, FishingGearEffects.minigameSpeedMultiplier(modifiers), 1.0e-12);
        assertEquals(0.90D, FishingGearEffects.catchLossPreventionChance(modifiers), 1.0e-12);
        assertEquals(1.0D, modifiers.namedAdditiveModifier(FishingGearEffects.CATCH_LOSS_PROTECTION_SOURCES));
        assertEquals(1.0D, modifiers.strengthMultiplier());
        assertEquals(1.0D, modifiers.tempoMultiplier());
        assertEquals(0.0D, modifiers.fishingLuck());
        assertEquals(0.0D, modifiers.traitLuck());
    }

    @Test
    void ordinaryCatchWithoutSteelLeaderIsNeutralAndConsumesNoProtectionRoll() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = SteelLeaderGearModifiers.forAttachmentState(false, config);
        AtomicInteger rolls = new AtomicInteger();

        assertEquals(FishingGearModifiers.neutral(), modifiers);
        assertEquals(1.0D, FishingGearEffects.catchZoneAreaMultiplier(modifiers));
        assertEquals(1.0D, FishingGearEffects.minigameSpeedMultiplier(modifiers));
        assertEquals(0.0D, FishingGearEffects.catchLossPreventionChance(modifiers));
        assertFalse(FishingGearEffects.preventsCatchLoss(modifiers, () -> {
            rolls.incrementAndGet();
            return 0.0D;
        }));
        assertEquals(0, rolls.get());
    }

    @Test
    void disabledApexCompatibilityMakesAttachmentBehaviorNeutral() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        config.enableApexCompat = false;

        assertEquals(FishingGearModifiers.neutral(), SteelLeaderGearModifiers.forAttachmentState(true, config));
    }

    @Test
    void steelLeaderComposesThroughCanonicalNamedModifierModel() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers existing = FishingGearModifiers.builder()
                .fishingLuck(4.0D)
                .namedMultiplierModifier(FishingGearEffects.CATCH_ZONE_AREA_MULTIPLIER, 1.10D)
                .build();

        FishingGearModifiers combined = FishingGearModifiers.compose(
                existing,
                SteelLeaderGearModifiers.forAttachmentState(true, config)
        );

        assertEquals(0.99D, FishingGearEffects.catchZoneAreaMultiplier(combined), 1.0e-12);
        assertEquals(1.05D, FishingGearEffects.minigameSpeedMultiplier(combined), 1.0e-12);
        assertEquals(0.90D, FishingGearEffects.catchLossPreventionChance(combined), 1.0e-12);
        assertEquals(4.0D, combined.fishingLuck());
    }

    @Test
    void sharkLossProtectionPreservesStrictLegacyChanceBoundaryAndRollCount() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        FishingGearModifiers modifiers = SteelLeaderGearModifiers.forAttachmentState(true, config);
        AtomicInteger rolls = new AtomicInteger();

        assertTrue(FishingGearEffects.preventsCatchLoss(modifiers, () -> {
            rolls.incrementAndGet();
            return 0.899999D;
        }));
        assertFalse(FishingGearEffects.preventsCatchLoss(modifiers, () -> {
            rolls.incrementAndGet();
            return 0.90D;
        }));
        assertEquals(2, rolls.get());
    }

    @Test
    void attachedLeaderStillConsumesOneRollAtZeroConfiguredProtection() {
        TideboundConfig.Values config = new TideboundConfig.Values();
        config.steelLeaderCatchLossPreventionChance = 0.0D;
        FishingGearModifiers modifiers = SteelLeaderGearModifiers.forAttachmentState(true, config);
        AtomicInteger rolls = new AtomicInteger();

        assertFalse(FishingGearEffects.preventsCatchLoss(modifiers, () -> {
            rolls.incrementAndGet();
            return 0.5D;
        }));
        assertEquals(1, rolls.get());
    }
}
