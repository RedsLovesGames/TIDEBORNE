package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TraitRandomTest {
    @Test
    void sameSeedAndSaltProduceExactlySameValue() {
        double first = TraitRandom.unitDouble(0x1234ABCD5678EF90L, TraitRandom.Salts.BODY_TYPE_EVENT);
        double second = TraitRandom.unitDouble(0x1234ABCD5678EF90L, TraitRandom.Salts.BODY_TYPE_EVENT);

        assertEquals(first, second);
        assertEquals(
                TraitRandom.mixedLong(0x1234ABCD5678EF90L, TraitRandom.Salts.BODY_TYPE_EVENT),
                TraitRandom.mixedLong(0x1234ABCD5678EF90L, TraitRandom.Salts.BODY_TYPE_EVENT)
        );
    }

    @Test
    void differentSaltsProduceIndependentDeterministicValues() {
        long seed = 8827349234L;
        double bodyType = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        double bodyTypeSize = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        double condition = TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);
        double pigmentation = TraitRandom.unitDouble(seed, TraitRandom.Salts.PIGMENTATION_EVENT);

        assertNotEquals(bodyType, bodyTypeSize);
        assertNotEquals(bodyType, condition);
        assertNotEquals(bodyType, pigmentation);
        assertNotEquals(bodyTypeSize, condition);
        assertNotEquals(bodyTypeSize, pigmentation);
        assertNotEquals(condition, pigmentation);
    }

    @Test
    void repeatedCallsDoNotDependOnCallOrder() {
        long seed = -551928371337L;
        double bodyFirst = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);
        double sizeSecond = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        double conditionThird = TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);

        double conditionFirst = TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_EVENT);
        double sizeSecondAgain = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        double bodyThird = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_EVENT);

        assertEquals(bodyFirst, bodyThird);
        assertEquals(sizeSecond, sizeSecondAgain);
        assertEquals(conditionThird, conditionFirst);
    }

    @Test
    void unrelatedSaltCannotShiftExistingValues() {
        long seed = 42L;
        double bodyBefore = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_VARIANT);
        double bodySizeBefore = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        double conditionBefore = TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_VARIANT);

        TraitRandom.unitDouble(seed, 0x0F0E0D0C0B0A0908L);

        double bodyAfter = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_VARIANT);
        double bodySizeAfter = TraitRandom.unitDouble(seed, TraitRandom.Salts.BODY_TYPE_SIZE);
        double conditionAfter = TraitRandom.unitDouble(seed, TraitRandom.Salts.CONDITION_VARIANT);

        assertEquals(bodyBefore, bodyAfter);
        assertEquals(bodySizeBefore, bodySizeAfter);
        assertEquals(conditionBefore, conditionAfter);
    }

    @Test
    void unitDoubleAlwaysStaysInsideHalfOpenUnitInterval() {
        long[] seeds = {Long.MIN_VALUE, -1L, 0L, 1L, 7L, 999_999_999L, Long.MAX_VALUE};
        long[] salts = {
                TraitRandom.Salts.BODY_TYPE_EVENT,
                TraitRandom.Salts.BODY_TYPE_VARIANT,
                TraitRandom.Salts.BODY_TYPE_SIZE,
                TraitRandom.Salts.CONDITION_EVENT,
                TraitRandom.Salts.CONDITION_VARIANT,
                TraitRandom.Salts.PIGMENTATION_EVENT,
                TraitRandom.Salts.PIGMENTATION_VARIANT,
                TraitRandom.Salts.PERFECT_SPECIMEN
        };

        for (long seed : seeds) {
            for (long salt : salts) {
                double value = TraitRandom.unitDouble(seed, salt);
                assertTrue(value >= 0.0, "value must be >= 0");
                assertTrue(value < 1.0, "value must be < 1");
            }
        }
    }
}
