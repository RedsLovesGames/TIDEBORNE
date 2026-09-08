package com.redslovesgames.tideborne.fishing.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class TraitMomentumStateTest {
    @Test
    void speciesValuesAreIndependent() {
        TraitMomentumState state = new TraitMomentumState();

        state.set("tide:trout", 4);
        state.set("tide:bass", 11);

        assertEquals(4, state.get("tide:trout"));
        assertEquals(11, state.get("tide:bass"));
        state.add("tide:trout", 2);
        assertEquals(6, state.get("tide:trout"));
        assertEquals(11, state.get("tide:bass"));
    }

    @Test
    void capAndFloorAreEnforcedIncludingOverflowSafeAdditions() {
        TraitMomentumState state = new TraitMomentumState();

        assertEquals(TraitMomentumStorage.MAX_MOMENTUM, state.set("tide:trout", 999));
        assertEquals(TraitMomentumStorage.MAX_MOMENTUM, state.add("tide:trout", Integer.MAX_VALUE));
        assertEquals(0, state.set("tide:trout", -8));
        assertEquals(0, state.get("tide:trout"));
        assertEquals(0, state.add("tide:trout", Integer.MIN_VALUE));
    }

    @Test
    void serializationRoundTripPreservesPerSpeciesValues() {
        TraitMomentumState original = new TraitMomentumState();
        original.set("tide:trout", 7);
        original.set("tide:bass", 13);

        NbtCompound playerRoot = new NbtCompound();
        TraitMomentumStorage.writeToPlayerRoot(playerRoot, original);
        TraitMomentumState restored = TraitMomentumStorage.readFromPlayerRoot(playerRoot.copy());

        assertEquals(7, restored.get("tide:trout"));
        assertEquals(13, restored.get("tide:bass"));
        assertEquals(2, restored.snapshot().size());
        assertTrue(playerRoot.contains(TraitMomentumStorage.PLAYER_DATA_KEY));
    }

    @Test
    void missingPlayerOrSpeciesDataDefaultsToZero() {
        TraitMomentumState empty = TraitMomentumStorage.readFromPlayerRoot(new NbtCompound());

        assertEquals(0, empty.get("tide:trout"));
        assertEquals(0, empty.get("tide:never_seen"));
        assertTrue(empty.snapshot().isEmpty());
    }

    @Test
    void malformedAndOldDataAreHandledWithoutCreatingInvalidMomentum() {
        NbtCompound malformedRoot = new NbtCompound();
        NbtCompound malformedMomentum = new NbtCompound();
        malformedMomentum.putInt("DataVersion", 1);
        NbtCompound species = new NbtCompound();
        species.putString("tide:wrong_type", "high");
        species.putInt("not a namespaced id", 8);
        species.putInt("tide:too_high", 999);
        species.putInt("tide:negative", -4);
        malformedMomentum.put("Species", species);
        malformedRoot.put(TraitMomentumStorage.PLAYER_DATA_KEY, malformedMomentum);

        TraitMomentumState sanitized = TraitMomentumStorage.readFromPlayerRoot(malformedRoot);
        assertEquals(0, sanitized.get("tide:wrong_type"));
        assertEquals(TraitMomentumStorage.MAX_MOMENTUM, sanitized.get("tide:too_high"));
        assertEquals(0, sanitized.get("tide:negative"));
        assertFalse(sanitized.snapshot().containsKey("not a namespaced id"));

        NbtCompound oldDirectMap = new NbtCompound();
        oldDirectMap.putInt("tide:legacy_fish", 6);
        NbtCompound oldPlayerRoot = new NbtCompound();
        oldPlayerRoot.put(TraitMomentumStorage.PLAYER_DATA_KEY, oldDirectMap);
        TraitMomentumState migrated = TraitMomentumStorage.readFromPlayerRoot(oldPlayerRoot);

        assertEquals(6, migrated.get("tide:legacy_fish"));
        assertEquals(0, migrated.get("tide:other_fish"));
    }
}
