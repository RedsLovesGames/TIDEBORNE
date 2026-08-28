package com.redslovesgames.tideborne.fishing.v2.integration;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;

/**
 * Canonical Fishing System 2.0 specimen snapshots stored beside legacy Tide journal data.
 *
 * <p>The store deliberately reuses {@link CanonicalSpecimenStorage}'s transfer payload instead of
 * defining another specimen serialization format. Legacy/native journal NBT is never rewritten or
 * interpreted here.</p>
 */
public final class JournalSpecimenStore {
    public static final String ROOT_KEY = "FishingSystem2JournalSpecimens";
    public static final String LATEST = "latest";
    public static final String LARGEST = "largest";
    public static final String SMALLEST = "smallest";

    private JournalSpecimenStore() {
    }

    /**
     * Records one already-finalized server canonical specimen. The latest snapshot is always
     * refreshed, while record snapshots are changed only when the caller has already established
     * that the canonical final length won the corresponding native journal record.
     */
    public static boolean capture(NbtCompound journalRoot, SpecimenData specimen, boolean largestRecord, boolean smallestRecord) {
        if (journalRoot == null || specimen == null || specimen.speciesId().isBlank()) {
            return false;
        }

        NbtCompound allSpecies = journalRoot.contains(ROOT_KEY, 10)
                ? journalRoot.getCompound(ROOT_KEY)
                : new NbtCompound();
        NbtCompound species = allSpecies.contains(specimen.speciesId(), 10)
                ? allSpecies.getCompound(specimen.speciesId())
                : new NbtCompound();

        writeSnapshot(species, LATEST, specimen);
        if (largestRecord) {
            writeSnapshot(species, LARGEST, specimen);
        }
        if (smallestRecord) {
            writeSnapshot(species, SMALLEST, specimen);
        }

        allSpecies.put(specimen.speciesId(), species);
        journalRoot.put(ROOT_KEY, allSpecies);
        return true;
    }

    /** Side-effect-free canonical read. Missing, legacy-only, or invalid data stays absent. */
    public static Optional<SpecimenData> read(NbtCompound journalRoot, String speciesId, String recordKind) {
        if (journalRoot == null || speciesId == null || speciesId.isBlank() || recordKind == null || recordKind.isBlank()) {
            return Optional.empty();
        }
        if (!journalRoot.contains(ROOT_KEY, 10)) {
            return Optional.empty();
        }
        NbtCompound allSpecies = journalRoot.getCompound(ROOT_KEY);
        if (!allSpecies.contains(speciesId, 10)) {
            return Optional.empty();
        }
        NbtCompound species = allSpecies.getCompound(speciesId);
        if (!species.contains(recordKind, 10)) {
            return Optional.empty();
        }
        return CanonicalSpecimenStorage.readTransferData(species.getCompound(recordKind));
    }

    private static void writeSnapshot(NbtCompound species, String recordKind, SpecimenData specimen) {
        NbtCompound snapshot = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(snapshot, specimen);
        species.put(recordKind, snapshot);
    }
}
