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
    public static final String BEST = "best";
    public static final String LARGEST = "largest";
    public static final String SMALLEST = "smallest";

    private static final String PERSONAL_NATIVE_KEY = "TidePlayerData";
    private static final String TEAM_NATIVE_KEY = "journal";
    private static final LegacyFishMigrationService MIGRATION = new LegacyFishMigrationService();
    private static final TideSpeciesProfileAdapter PROFILE_ADAPTER = new TideSpeciesProfileAdapter();

    private JournalSpecimenStore() {
    }

    /**
     * Captures a real finalized catch. Latest remains as a compatibility snapshot, while Best is
     * independently selected from canonical FishScore and deterministic tie-breakers.
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
        updateBestSnapshot(species, specimen);
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
     * Reindexes one already-existing canonical specimen as a Best Specimen candidate only. This is
     * used by recovery tooling and intentionally does not author latest catch, size records, catch
     * counters, discovery, history, rewards, or Momentum.
     */
    public static boolean indexBest(NbtCompound journalRoot, SpecimenData specimen) {
        if (journalRoot == null || specimen == null || specimen.speciesId().isBlank() || specimen.fishScore().isEmpty()) {
            return false;
        }
        migrateLegacyJournal(journalRoot);
        NbtCompound allSpecies = journalRoot.contains(ROOT_KEY, 10)
                ? journalRoot.getCompound(ROOT_KEY)
                : new NbtCompound();
        NbtCompound species = allSpecies.contains(specimen.speciesId(), 10)
                ? allSpecies.getCompound(specimen.speciesId())
                : new NbtCompound();
        if (!updateBestSnapshot(species, specimen)) {
            return false;
        }
        allSpecies.put(specimen.speciesId(), species);
        journalRoot.put(ROOT_KEY, allSpecies);
        return true;
    }

    /**
     * Backfills reconstructable largest/smallest canonical record snapshots from an old Tide journal.
     * The historical latest specimen, traits, and scores are not present in Tide's aggregate stats,
     * so they are never invented here. Existing canonical sidecars from older Tideborne versions are
     * also promoted into Best only when they already contain a truthful canonical FishScore.
     */
    public static boolean migrateLegacyJournal(NbtCompound journalRoot) {
        if (journalRoot == null) {
            return false;
        }
        try {
            boolean changed = migrateBestSnapshots(journalRoot);
            TidePlayerData journal = legacyJournalData(journalRoot);
            if (journal == null) {
                return changed;
            }
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
            changed |= migrateBestSnapshots(journalRoot);
            return changed;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    /** Reads one canonical snapshot, performing only one-way legacy/backfill work first. */
    public static Optional<SpecimenData> read(NbtCompound journalRoot, String speciesId, String recordKind) {
        if (journalRoot == null || speciesId == null || speciesId.isBlank() || recordKind == null || recordKind.isBlank()) {
            return Optional.empty();
        }
        migrateLegacyJournal(journalRoot);
        return readSnapshot(journalRoot, speciesId, recordKind);
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

    private static boolean migrateBestSnapshots(NbtCompound journalRoot) {
        if (!journalRoot.contains(ROOT_KEY, 10)) {
            return false;
        }
        NbtCompound allSpecies = journalRoot.getCompound(ROOT_KEY);
        boolean changed = false;
        for (String speciesId : allSpecies.getKeys()) {
            if (!allSpecies.contains(speciesId, 10)) {
                continue;
            }
            NbtCompound species = allSpecies.getCompound(speciesId);
            SpecimenData best = readSnapshot(species, BEST).orElse(null);
            for (String candidateKind : new String[]{LATEST, LARGEST, SMALLEST}) {
                SpecimenData candidate = readSnapshot(species, candidateKind).orElse(null);
                if (candidate != null && candidate.fishScore().isPresent()
                        && CanonicalSpecimenRecordIndexer.shouldReplaceBest(best, candidate)) {
                    best = candidate;
                }
            }
            SpecimenData storedBest = readSnapshot(species, BEST).orElse(null);
            if (best != null && (storedBest == null
                    || !CanonicalSpecimenRecordIndexer.sameSpecimenIdentity(storedBest, best))) {
                writeSnapshot(species, BEST, best);
                allSpecies.put(speciesId, species);
                changed = true;
            }
        }
        if (changed) {
            journalRoot.put(ROOT_KEY, allSpecies);
        }
        return changed;
    }

    private static boolean updateBestSnapshot(NbtCompound species, SpecimenData candidate) {
        if (candidate == null || candidate.fishScore().isEmpty()) {
            return false;
        }
        SpecimenData current = readSnapshot(species, BEST).orElse(null);
        if (!CanonicalSpecimenRecordIndexer.shouldReplaceBest(current, candidate)) {
            return false;
        }
        writeSnapshot(species, BEST, candidate);
        return true;
    }

    private static Optional<SpecimenData> readSnapshot(NbtCompound root, String speciesId, String recordKind) {
        if (!root.contains(ROOT_KEY, 10)) {
            return Optional.empty();
        }
        NbtCompound allSpecies = root.getCompound(ROOT_KEY);
        if (!allSpecies.contains(speciesId, 10)) {
            return Optional.empty();
        }
        return readSnapshot(allSpecies.getCompound(speciesId), recordKind);
    }

    private static Optional<SpecimenData> readSnapshot(NbtCompound species, String recordKind) {
        if (species == null || !species.contains(recordKind, 10)) {
            return Optional.empty();
        }
        return CanonicalSpecimenStorage.readTransferData(species.getCompound(recordKind));
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
