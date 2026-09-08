package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TraitLuckProbabilityServiceTest {
    private static final double EPSILON = 1.0e-12;
    private final TraitLuckProbabilityService service = new TraitLuckProbabilityService();

    @Test
    void zeroTraitLuckReturnsTheOriginalProbability() {
        assertEquals(0.0, service.adjustProbability(0.0, 0.0));
        assertEquals(0.01, service.adjustProbability(0.01, 0.0));
        assertEquals(0.5, service.adjustProbability(0.5, 0.0));
        assertEquals(1.0, service.adjustProbability(1.0, 0.0));
    }

    @Test
    void positiveTraitLuckIncreasesProbabilityMonotonically() {
        double probability = 0.05;
        double previous = service.adjustProbability(probability, 0.0);

        for (double traitLuck : new double[]{1.0, 5.0, 10.0, 20.0, 30.0, 100.0}) {
            double adjusted = service.adjustProbability(probability, traitLuck);
            assertTrue(adjusted > previous,
                    "expected Trait Luck " + traitLuck + " to increase probability above " + previous);
            previous = adjusted;
        }
    }

    @Test
    void outputAlwaysStaysInsideProbabilityRange() {
        double[] probabilities = {-100.0, -1.0, 0.0, 0.0001, 0.25, 0.99, 1.0, 2.0, 100.0,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] traitLuckValues = {Double.NEGATIVE_INFINITY, -1_000_000.0, -10.0, -5.0, 0.0, 5.0,
                30.0, 1_000_000.0, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN};

        for (double probability : probabilities) {
            for (double traitLuck : traitLuckValues) {
                double adjusted = service.adjustProbability(probability, traitLuck);
                assertTrue(adjusted >= 0.0 && adjusted <= 1.0,
                        "out-of-range result " + adjusted + " for P=" + probability + ", T=" + traitLuck);
            }
        }
    }

    @Test
    void matchesKnownNumericalCases() {
        assertEquals(0.014962437264446238, service.adjustProbability(0.01, 5.0), EPSILON);
        assertEquals(0.0199, service.adjustProbability(0.01, 10.0), EPSILON);
        assertEquals(0.029701, service.adjustProbability(0.01, 20.0), EPSILON);
        assertEquals(0.03940399, service.adjustProbability(0.01, 30.0), EPSILON);
        assertEquals(0.0975, service.adjustProbability(0.05, 10.0), EPSILON);
        assertEquals(0.75, service.adjustProbability(0.5, 10.0), EPSILON);
    }

    @Test
    void edgeProbabilitiesRemainExactForAnyTraitLuck() {
        double[] traitLuckValues = {Double.NEGATIVE_INFINITY, -10.0, -5.0, 0.0, 30.0,
                Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN};

        for (double traitLuck : traitLuckValues) {
            assertEquals(0.0, service.adjustProbability(0.0, traitLuck));
            assertEquals(1.0, service.adjustProbability(1.0, traitLuck));
        }
    }

    @Test
    void probabilityInputClampsAndRejectsNaN() {
        assertEquals(0.0, service.adjustProbability(-0.25, 20.0));
        assertEquals(1.0, service.adjustProbability(1.25, -10.0));
        assertEquals(0.0, service.adjustProbability(Double.NEGATIVE_INFINITY, 20.0));
        assertEquals(1.0, service.adjustProbability(Double.POSITIVE_INFINITY, -10.0));
        assertThrows(IllegalArgumentException.class,
                () -> service.adjustProbability(Double.NaN, 20.0));
    }

    @Test
    void negativeAndExtremeTraitLuckHaveDefinedSafeBehavior() {
        assertEquals(0.1339745962155614, service.adjustProbability(0.25, -5.0), EPSILON);
        assertEquals(0.0, service.adjustProbability(0.25, -10.0));
        assertEquals(0.0, service.adjustProbability(0.25, -1000.0));
        assertEquals(0.0, service.adjustProbability(0.25, Double.NEGATIVE_INFINITY));
        assertEquals(0.25, service.adjustProbability(0.25, Double.NaN));
        assertEquals(1.0, service.adjustProbability(0.25, Double.POSITIVE_INFINITY));
        assertEquals(1.0, service.adjustProbability(0.25, Double.MAX_VALUE));
    }
}
