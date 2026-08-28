package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FishingContextTest {
    @Test
    void snapshotsAndCombinesModifierSources() {
        Map<String, Double> equipment = new HashMap<>();
        equipment.put("strength", 0.10);
        FishingContext context = new FishingContext(
                2.0,
                15.0,
                8.0,
                equipment,
                Map.of("strength", 0.15),
                Map.of("strength", -0.05)
        );
        equipment.put("strength", 999.0);

        assertEquals(0.20, context.additiveModifier("strength"), 1.0e-12);
    }

    @Test
    void rejectsNonFiniteLuck() {
        assertThrows(IllegalArgumentException.class, () -> new FishingContext(
                0.0,
                Double.NaN,
                0.0,
                Map.of(),
                Map.of(),
                Map.of()
        ));
    }
}

