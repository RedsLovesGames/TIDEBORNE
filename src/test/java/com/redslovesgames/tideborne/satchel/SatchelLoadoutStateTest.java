package com.redslovesgames.tideborne.satchel;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SatchelLoadoutStateTest {
    @Test void presetsRoundTripWithoutChangingLegacyState() {
        NbtCompound legacy = new NbtCompound();
        legacy.putString("future_key", "preserved");
        SatchelState original = SatchelState.fromTag(legacy).withActive(true)
                .withFeatureUnlocked("trait_scanner").withFeatureEnabled("trait_scanner", true)
                .withSlotProtected(3, true);
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        SatchelState updated = original.withPresetRod(0, first).withPresetRod(6, second);
        SatchelState loaded = SatchelState.fromTag(updated.toTag());
        assertEquals(first, loaded.presetRod(0).orElseThrow());
        assertEquals(second, loaded.presetRod(6).orElseThrow());
        assertTrue(original.presetRod(0).isEmpty());
        assertEquals(original.toTag().getString("future_key"), loaded.toTag().getString("future_key"));
        assertTrue(loaded.isActive());
        assertTrue(loaded.isFeatureEnabled("trait_scanner"));
        assertTrue(loaded.isSlotProtected(3));
        assertTrue(loaded.withPresetRod(0, null).presetRod(0).isEmpty());
        assertEquals(second, loaded.withPresetRod(0, null).presetRod(6).orElseThrow());
    }

    @Test void malformedAndOutOfRangeReferencesCannotSelectEquipment() {
        NbtCompound root = new NbtCompound(), refs = new NbtCompound();
        refs.putString("0", "not-a-uuid");
        root.put("tackle_presets", refs);
        SatchelState state = SatchelState.fromTag(root);
        assertTrue(state.presetRod(0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> state.presetRod(-1));
        assertThrows(IllegalArgumentException.class, () -> state.withPresetRod(7, UUID.randomUUID()));
    }

    @Test void stateAndOutputAreDefensiveCopies() {
        UUID reference = UUID.randomUUID();
        NbtCompound input = SatchelState.empty().withPresetRod(2, reference).toTag();
        SatchelState state = SatchelState.fromTag(input);
        input.remove("tackle_presets");
        state.toTag().remove("tackle_presets");
        assertEquals(reference, state.presetRod(2).orElseThrow());
    }
}
