package com.redslovesgames.tideborne.journal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhysicalSizeRecordComparisonTest {
    @Test
    void equalityAndImprovementsShareTheAbsoluteTolerance() {
        double previous = 35.0;
        assertTrue(RecordHolderStore.samePhysicalSize(previous, previous));
        for (double direction : new double[]{-1.0, 1.0}) {
            double inside = previous + direction * 0.999E-6;
            double outside = previous + direction * 1.001E-6;
            assertTrue(RecordHolderStore.samePhysicalSize(inside, previous));
            assertFalse(RecordHolderStore.samePhysicalSize(outside, previous));
            assertFalse(RecordHolderStore.isLargerPhysicalRecord(inside, true, previous));
            assertFalse(RecordHolderStore.isSmallerPhysicalRecord(inside, true, previous));
            assertEquals(direction > 0, RecordHolderStore.isLargerPhysicalRecord(outside, true, previous));
            assertEquals(direction < 0, RecordHolderStore.isSmallerPhysicalRecord(outside, true, previous));
        }
        assertFalse(RecordHolderStore.isLargerPhysicalRecord(previous, true, previous));
        assertFalse(RecordHolderStore.isSmallerPhysicalRecord(previous, true, previous));
    }

    @Test
    void fourUlpsAreInclusiveAndFiveUlpsImproveLargeRecords() {
        double previous = 1E10;
        double ulp = Math.ulp(previous);
        assertTrue(RecordHolderStore.samePhysicalSize(previous + 4 * ulp, previous));
        assertTrue(RecordHolderStore.samePhysicalSize(previous - 4 * ulp, previous));
        assertFalse(RecordHolderStore.isLargerPhysicalRecord(previous + 4 * ulp, true, previous));
        assertFalse(RecordHolderStore.isSmallerPhysicalRecord(previous - 4 * ulp, true, previous));
        assertTrue(RecordHolderStore.isLargerPhysicalRecord(previous + 5 * ulp, true, previous));
        assertTrue(RecordHolderStore.isSmallerPhysicalRecord(previous - 5 * ulp, true, previous));
    }

    @Test
    void firstSizedCatchEstablishesBothRecordsWithoutConsultingAnAbsentValue() {
        assertTrue(RecordHolderStore.isLargerPhysicalRecord(35, false, Double.NaN));
        assertTrue(RecordHolderStore.isSmallerPhysicalRecord(35, false, Double.NaN));
        assertTrue(RecordHolderStore.isSmallerPhysicalRecord(35, false, 0));
    }

    @Test
    void nonFiniteValuesDoNotCompareEqualAndValidationStaysAtCallers() {
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(RecordHolderStore.samePhysicalSize(35, invalid));
            assertFalse(RecordHolderStore.samePhysicalSize(invalid, 35));
            assertFalse(RecordHolderStore.samePhysicalSize(invalid, invalid));
        }
        assertFalse(RecordHolderStore.isLargerPhysicalRecord(Double.NaN, true, 35));
        assertFalse(RecordHolderStore.isSmallerPhysicalRecord(Double.NaN, true, 35));
        // Satchel equality historically accepts a finite zero; client markers additionally require positive records.
        assertTrue(RecordHolderStore.samePhysicalSize(0, 0));
    }
}
