package com.redslovesgames.tideborne.journal;

import com.redslovesgames.tideborne.fishing.specimen.CanonicalSpecimenStorage;

import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Canonical Fishing System 2.0 specimen-record comparison and Team Top Fish indexing.
 *
 * <p>This service compares finalized immutable specimen state only. It never generates traits,
 * rerolls identity, increments catch progression, or mutates Momentum. Higher canonical FishScore
 * wins first, followed by final percentile, final length, species ID, and the stable specimen seed.
 */
public final class CanonicalSpecimenRecordIndexer {
    public static final int TEAM_TOP_FISH_LIMIT = 15;
    public static final String TEAM_TOP_FISH_KEY = "top_fish";

    private static final String CANONICAL_SCORE_KEY = "canonical_fish_score";
    private static final String LEGACY_SCORE_KEY = "fish_score";

    private CanonicalSpecimenRecordIndexer() {
    }

    /** Returns a positive value when {@code left} is the preferred canonical record. */
    public static int compareSpecimens(SpecimenData left, SpecimenData right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }

        int compared = Integer.compare(score(left), score(right));
        if (compared != 0) {
            return compared;
        }
        compared = Double.compare(left.finalPercentile(), right.finalPercentile());
        if (compared != 0) {
            return compared;
        }
        compared = Double.compare(left.finalLength(), right.finalLength());
        if (compared != 0) {
            return compared;
        }

        // Lexically smaller species IDs and unsigned seeds win otherwise-equal ties. This makes
        // ordering stable across restarts without using timestamps, client state, or randomness.
        compared = right.speciesId().compareTo(left.speciesId());
        if (compared != 0) {
            return compared;
        }
        return -Long.compareUnsigned(left.deterministicSeed(), right.deterministicSeed());
    }

    public static boolean shouldReplaceBest(SpecimenData current, SpecimenData candidate) {
        return candidate != null && (current == null || compareSpecimens(candidate, current) > 0);
    }

    public static boolean sameSpecimenIdentity(SpecimenData left, SpecimenData right) {
        return left != null
                && right != null
                && left.schemaVersion() == right.schemaVersion()
                && left.generationVersion() == right.generationVersion()
                && left.deterministicSeed() == right.deterministicSeed()
                && left.speciesId().equals(right.speciesId());
    }

    /**
     * Creates the server-owned projection consumed by Team Top Fish and record UI. The nested
     * canonical transfer payload remains the source of specimen identity; flat fields are only
     * compatibility/display mirrors.
     */
    public static NbtCompound project(SpecimenData specimen) {
        if (specimen == null) {
            return new NbtCompound();
        }
        NbtCompound tag = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(tag, specimen);
        tag.putString("fish", specimen.speciesId());
        tag.putDouble("length", specimen.finalLength());
        tag.putDouble("percentile", specimen.finalPercentile());
        tag.putString("body_type", serialized(specimen.bodyType()));
        tag.putString("condition", serialized(specimen.condition()));
        tag.putString("mutation", serialized(specimen.condition()));
        tag.putString("pigmentation", serialized(specimen.pigmentation()));
        tag.putString("quality", serialized(specimen.specimenQuality()));
        tag.putBoolean("perfect_catch", specimen.perfectCatch());
        specimen.rawFishScore().ifPresent(value -> tag.putDouble("canonical_raw_fish_score", value));
        specimen.fishScore().ifPresent(score -> {
            tag.putInt(CANONICAL_SCORE_KEY, score);
            tag.putInt(LEGACY_SCORE_KEY, score);
        });
        return tag;
    }

    /**
     * Inserts one finalized canonical specimen into Team Top Fish, prevents duplicate canonical
     * identity, sorts deterministically, and keeps exactly the best fifteen candidates at most.
     */
    public static boolean indexTeamTopFish(NbtCompound root, NbtCompound candidate) {
        if (root == null || candidate == null) {
            return false;
        }
        SpecimenData candidateSpecimen = CanonicalSpecimenStorage.readTransferData(candidate).orElse(null);
        if (candidateSpecimen == null || candidateSpecimen.fishScore().isEmpty()) {
            return false;
        }

        NbtList existing = root.getList(TEAM_TOP_FISH_KEY, NbtElement.COMPOUND_TYPE);
        List<NbtCompound> records = new ArrayList<>();
        for (NbtElement element : existing) {
            if (!(element instanceof NbtCompound tag)) {
                continue;
            }
            Optional<SpecimenData> stored = CanonicalSpecimenStorage.readTransferData(tag);
            if (stored.filter(value -> sameSpecimenIdentity(value, candidateSpecimen)).isPresent()) {
                return false;
            }
            records.add(tag.copy());
        }
        records.add(candidate.copy());
        records.sort(CanonicalSpecimenRecordIndexer::compareProjectionsBestFirst);
        if (records.size() > TEAM_TOP_FISH_LIMIT) {
            records = new ArrayList<>(records.subList(0, TEAM_TOP_FISH_LIMIT));
        }

        NbtList replacement = new NbtList();
        records.forEach(replacement::add);
        if (existing.equals(replacement)) {
            return false;
        }
        root.put(TEAM_TOP_FISH_KEY, replacement);
        return true;
    }

    /** Reads the highest valid canonical score from an indexed Team Top Fish list. */
    public static OptionalInt highestTeamScore(NbtCompound root) {
        if (root == null) {
            return OptionalInt.empty();
        }
        int best = -1;
        for (NbtElement element : root.getList(TEAM_TOP_FISH_KEY, NbtElement.COMPOUND_TYPE)) {
            if (element instanceof NbtCompound tag) {
                best = Math.max(best, score(tag));
            }
        }
        return best > 0 ? OptionalInt.of(best) : OptionalInt.empty();
    }

    private static int compareProjectionsBestFirst(NbtCompound left, NbtCompound right) {
        SpecimenData leftSpecimen = CanonicalSpecimenStorage.readTransferData(left).orElse(null);
        SpecimenData rightSpecimen = CanonicalSpecimenStorage.readTransferData(right).orElse(null);
        if (leftSpecimen != null && rightSpecimen != null) {
            int canonical = compareSpecimens(leftSpecimen, rightSpecimen);
            if (canonical != 0) {
                return -canonical;
            }
        }

        int compared = Integer.compare(score(right), score(left));
        if (compared != 0) {
            return compared;
        }
        compared = Double.compare(finiteOrNegative(right, "percentile"), finiteOrNegative(left, "percentile"));
        if (compared != 0) {
            return compared;
        }
        compared = Double.compare(finiteOrNegative(right, "length"), finiteOrNegative(left, "length"));
        if (compared != 0) {
            return compared;
        }
        compared = left.getString("fish").compareTo(right.getString("fish"));
        if (compared != 0) {
            return compared;
        }
        return stableProjectionKey(left).compareTo(stableProjectionKey(right));
    }

    private static int score(SpecimenData specimen) {
        return specimen == null ? -1 : specimen.fishScore().orElse(-1);
    }

    private static int score(NbtCompound tag) {
        if (tag.contains(CANONICAL_SCORE_KEY, 99)) {
            int score = tag.getInt(CANONICAL_SCORE_KEY);
            if (score > 0) {
                return score;
            }
        }
        if (tag.contains(LEGACY_SCORE_KEY, 99)) {
            int score = tag.getInt(LEGACY_SCORE_KEY);
            if (score > 0) {
                return score;
            }
        }
        return -1;
    }

    private static double finiteOrNegative(NbtCompound tag, String key) {
        if (!tag.contains(key, 99)) {
            return -1.0;
        }
        double value = tag.getDouble(key);
        return Double.isFinite(value) ? value : -1.0;
    }

    private static String stableProjectionKey(NbtCompound tag) {
        Optional<SpecimenData> specimen = CanonicalSpecimenStorage.readTransferData(tag);
        if (specimen.isPresent()) {
            SpecimenData value = specimen.orElseThrow();
            return value.speciesId() + ':' + Long.toUnsignedString(value.deterministicSeed());
        }
        if (tag.contains("nonce", 99)) {
            return "legacy-nonce:" + Long.toUnsignedString(tag.getLong("nonce"));
        }
        return "legacy:" + tag.getString("fish") + ':' + score(tag) + ':' + tag.getLong("timestamp")
                + ':' + tag.getString("catcher_name");
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
