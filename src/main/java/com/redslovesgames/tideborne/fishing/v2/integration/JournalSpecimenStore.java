package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.player.FishStats;
import com.li64.tide.data.player.TidePlayerData;
import com.li64.tide.data.player.TidePlayerData.FishPlayerData;
import com.redslovesgames.tideborne.fishing.v2.LegacyFishMigrationService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;

/** Canonical Fishing System 2.0 specimen snapshots stored beside legacy Tide journal data. */
public final class JournalSpecimenStore {
    public static final String ROOT_KEY = "FishingSystem2JournalSpecimens";
    public static final String LATEST = "latest";
    public static final String LARGEST = "largest";
    public static final String SMALLEST = "smallest";

    private static final String PERSONAL_NATIVE_KEY = "TidePlayerData";
    private static final String TEAM_NATIVE_KEY = "journal";
    private static final LegacyFishMigrationService MIGRATION = new LegacyFishMigrationService();
    private static final TideSpeciesProfileAdapter PROFILE_ADAPTER = new TideSpeciesProfileAdapter();

    private JournalSpecimenStore() {
    }

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

    /**
     * Backfills reconstructable largest/smallest canonical record snapshots from an old Tide journal.
     * The historical latest specimen, traits, and scores are not present in Tide's aggregate stats,
     * so they are never invented here. Missing identity is handled only by the canonical migration
     * service, which deterministically derives seed and percentile from the preserved species/length.
     */
    public static boolean migrateLegacyJournal(NbtCompound journalRoot) {
        if (journalRoot == null) {
            return false;
        }
        try {
            TidePlayerData journal = legacyJournalData(journalRoot);
            if (journal == null) {
                return false;
            }
            boolean changed = false;
            for (Entry<RegistryEntry<Item>, FishPlayerData> entry : journal.fishPlayerData.entrySet()) {
                FishStats stats = entry.getValue().stats.filter(value -> !value.isEmpty()).orElse(null);
                if (stats == null) {
                    continue;
                }
                FishData fish = resolveFish(entry.getKey().value()).orElse(null);
                if (fish == null) {
                    continue;
                }
                SpeciesProfile species = PROFILE_ADAPTER.adaptForMigration(fish);
                changed |= migrateLegacyRecord(journalRoot, species, LARGEST, stats.getLargestCatch());
                changed |= migrateLegacyRecord(journalRoot, species, SMALLEST, stats.getSmallestCatch());
            }
            return changed;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    /** Reads one canonical snapshot, performing only the one-time aggregate legacy backfill first. */
    public static Optional<SpecimenData> read(NbtCompound journalRoot, String speciesId, String recordKind) {
        if (journalRoot == null || speciesId == null || speciesId.isBlank() || recordKind == null || recordKind.isBlank()) {
            return Optional.empty();
        }
        migrateLegacyJournal(journalRoot);
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

    /**
     * Reads only the FishScore persisted in the canonical specimen snapshot.
     * Legacy aggregate journal data has no recoverable score, so a missing score stays missing.
     */
    public static OptionalInt readFishScore(NbtCompound journalRoot, String speciesId, String recordKind) {
        Optional<SpecimenData> specimen = read(journalRoot, speciesId, recordKind);
        return specimen.isPresent() ? specimen.get().fishScore() : OptionalInt.empty();
    }

    static boolean migrateLegacyRecord(NbtCompound journalRoot, SpeciesProfile species, String recordKind, double length) {
        if (journalRoot == null || species == null || recordKind == null || recordKind.isBlank()
                || !Double.isFinite(length) || length <= 0.0 || hasSnapshot(journalRoot, species.speciesId(), recordKind)) {
            return false;
        }
        try {
            LegacyFishMigrationService.LegacyFish legacy = new LegacyFishMigrationService.LegacyFish(
                    species.speciesId(), null, null, null, length, null, null, null);
            SpecimenData migrated = withoutSyntheticHistoricalScore(MIGRATION.migrate(species, legacy).specimen());
            NbtCompound allSpecies = journalRoot.contains(ROOT_KEY, 10)
                    ? journalRoot.getCompound(ROOT_KEY)
                    : new NbtCompound();
            NbtCompound speciesTag = allSpecies.contains(species.speciesId(), 10)
                    ? allSpecies.getCompound(species.speciesId())
                    : new NbtCompound();
            writeSnapshot(speciesTag, recordKind, migrated);
            allSpecies.put(species.speciesId(), speciesTag);
            journalRoot.put(ROOT_KEY, allSpecies);
            return true;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }

    private static SpecimenData withoutSyntheticHistoricalScore(SpecimenData specimen) {
        return new SpecimenData(
                specimen.speciesId(),
                specimen.schemaVersion(),
                specimen.generationVersion(),
                specimen.deterministicSeed(),
                specimen.basePercentile(),
                specimen.baseLength(),
                specimen.finalLength(),
                specimen.finalPercentile(),
                specimen.bodyType(),
                specimen.condition(),
                specimen.pigmentation(),
                specimen.specimenQuality(),
                specimen.perfectCatch(),
                OptionalDouble.empty(),
                OptionalInt.empty(),
                specimen.provenance()
        );
    }

    private static TidePlayerData legacyJournalData(NbtCompound root) {
        if (root.contains(TEAM_NATIVE_KEY, 10)) {
            return new TidePlayerData(root.getCompound(TEAM_NATIVE_KEY));
        }
        if (root.contains(PERSONAL_NATIVE_KEY, 10)) {
            return new TidePlayerData(root.getCompound(PERSONAL_NATIVE_KEY));
        }
        return null;
    }

    private static Optional<FishData> resolveFish(Item item) {
        return TideData.FISH.get().values().stream()
                .filter(data -> data != null && data.fish().value() == item)
                .findFirst();
    }

    private static boolean hasSnapshot(NbtCompound root, String speciesId, String recordKind) {
        if (!root.contains(ROOT_KEY, 10)) {
            return false;
        }
        NbtCompound allSpecies = root.getCompound(ROOT_KEY);
        return allSpecies.contains(speciesId, 10) && allSpecies.getCompound(speciesId).contains(recordKind, 10);
    }

    private static void writeSnapshot(NbtCompound species, String recordKind, SpecimenData specimen) {
        NbtCompound snapshot = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(snapshot, specimen);
        species.put(recordKind, snapshot);
    }
}
