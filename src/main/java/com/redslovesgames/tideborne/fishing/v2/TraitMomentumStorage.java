package com.redslovesgames.tideborne.fishing.v2;

import com.li64.tide.data.TidePlayer;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Server-authoritative access to one persisted Trait Momentum value per player and species ID.
 *
 * <p>Momentum is stored in Tide's existing per-player persistent NBT root. It is intentionally not
 * stored in Tide's team-shared journal data, a fish item, a specimen, or any client-owned state.
 */
public final class TraitMomentumStorage {
    public static final int MAX_MOMENTUM = TraitMomentumState.MAX_MOMENTUM;
    static final String PLAYER_DATA_KEY = "FishingV2TraitMomentum";

    private TraitMomentumStorage() {
    }

    public static int get(ServerPlayerEntity player, String speciesId) {
        return read(player).get(speciesId);
    }

    public static int set(ServerPlayerEntity player, String speciesId, int momentum) {
        TraitMomentumState state = read(player);
        int stored = state.set(speciesId, momentum);
        write(player, state);
        return stored;
    }

    public static int add(ServerPlayerEntity player, String speciesId, int delta) {
        TraitMomentumState state = read(player);
        int stored = state.add(speciesId, delta);
        write(player, state);
        return stored;
    }

    public static void clear(ServerPlayerEntity player, String speciesId) {
        TraitMomentumState state = read(player);
        state.clear(speciesId);
        write(player, state);
    }

    /** Applies the canonical progression rule after one completed server-side catch. */
    public static int applyCompletedCatch(ServerPlayerEntity player, SpecimenData specimen) {
        TraitMomentumState state = read(player);
        int stored = TraitMomentumProgression.applyCompletedCatch(state, specimen);
        write(player, state);
        return stored;
    }

    public static Map<String, Integer> snapshot(ServerPlayerEntity player) {
        return read(player).snapshot();
    }

    static TraitMomentumState readFromPlayerRoot(NbtCompound playerRoot) {
        if (playerRoot == null) {
            return new TraitMomentumState();
        }
        NbtElement stored = playerRoot.get(PLAYER_DATA_KEY);
        return stored instanceof NbtCompound compound
                ? TraitMomentumState.fromNbt(compound)
                : new TraitMomentumState();
    }

    static void writeToPlayerRoot(NbtCompound playerRoot, TraitMomentumState state) {
        if (playerRoot == null) {
            throw new IllegalArgumentException("playerRoot cannot be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        playerRoot.put(PLAYER_DATA_KEY, state.toNbt());
    }

    private static TraitMomentumState read(ServerPlayerEntity player) {
        TidePlayer tidePlayer = asTidePlayer(player);
        return readFromPlayerRoot(tidePlayer.tide$getTidePlayerData());
    }

    private static void write(ServerPlayerEntity player, TraitMomentumState state) {
        TidePlayer tidePlayer = asTidePlayer(player);
        NbtCompound playerRoot = tidePlayer.tide$getTidePlayerData();
        if (playerRoot == null) {
            playerRoot = new NbtCompound();
        }
        writeToPlayerRoot(playerRoot, state);
        tidePlayer.tide$setTidePlayerData(playerRoot);
    }

    private static TidePlayer asTidePlayer(ServerPlayerEntity player) {
        if (player == null) {
            throw new IllegalArgumentException("player cannot be null");
        }
        if (player instanceof TidePlayer tidePlayer) {
            return tidePlayer;
        }
        throw new IllegalStateException("TidePlayer mixin is not applied to " + player.getClass().getName());
    }
}
