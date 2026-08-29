package com.redslovesgames.tideteamjournal.client;

import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenNetworkCodec;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/** Client-side read-only cache of server-projected canonical Journal specimen display data. */
public final class ClientJournalSpecimens {
    private static NbtCompound snapshot = new NbtCompound();

    private ClientJournalSpecimens() {
    }

    public static void update(NbtCompound packetTag) {
        snapshot = packetTag == null ? new NbtCompound() : packetTag.copy();
    }

    public static Optional<JournalSpecimenNetworkCodec.DisplaySpecimen> read(Identifier speciesId, String recordKind) {
        if (speciesId == null) {
            return Optional.empty();
        }
        return JournalSpecimenNetworkCodec.readDisplay(snapshot, speciesId.toString(), recordKind);
    }

    static void clearForTest() {
        snapshot = new NbtCompound();
    }
}
