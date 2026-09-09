package com.redslovesgames.tideborne.satchel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SatchelPresetNameTest {
    @Test void userNamesAreBoundedAndDoNotRequireAnArchetype() {
        assertEquals("Shark Hunt", SatchelPreset.cleanName("  Shark Hunt  "));
        assertEquals("My odd build", SatchelPreset.cleanName("My odd build"));
        assertEquals("New Preset", SatchelPreset.cleanName(" "));
        assertEquals(24, SatchelPreset.cleanName("x".repeat(100)).length());
        assertEquals("SharkHunt", SatchelPreset.cleanName("Shark\nHunt"));
    }
}
