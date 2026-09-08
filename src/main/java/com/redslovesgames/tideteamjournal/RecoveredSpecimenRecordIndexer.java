package com.redslovesgames.tideteamjournal;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.JournalSpecimenStore;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/** Pure derived-record reconstruction used by server recovery tooling and regression tests. */
public final class RecoveredSpecimenRecordIndexer {
    private static final String CONTRIBUTORS_KEY = "contributors";

    private RecoveredSpecimenRecordIndexer() {
    }

    /**
     * Reindexes one already-existing specimen without invoking any catch/discovery/history logic.
     * The caller must establish that the species was historically unlocked before calling this.
     */
    public static boolean index(
            NbtCompound root,
            SpecimenData specimen,
            UUID ownerId,
            String ownerName,
            int fishStars
    ) {
        if (root == null || specimen == null || specimen.fishScore().isEmpty() || ownerId == null) {
            return false;
        }

        boolean changed = JournalSpecimenStore.indexBest(root, specimen);
        changed |= indexContributor(root, specimen, ownerId, ownerName);

        NbtCompound topFish = CanonicalSpecimenRecordIndexer.project(specimen);
        topFish.putInt("fish_stars", Math.max(0, fishStars));
        topFish.putUuid("catcher_id", ownerId);
        topFish.putString("catcher_name", ownerName == null ? "" : ownerName);
        topFish.putBoolean("recovered", true);
        changed |= CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, topFish);
        return changed;
    }

    private static boolean indexContributor(NbtCompound root, SpecimenData specimen, UUID ownerId, String ownerName) {
        NbtCompound contributors = root.contains(CONTRIBUTORS_KEY, NbtElement.COMPOUND_TYPE)
                ? root.getCompound(CONTRIBUTORS_KEY)
                : new NbtCompound();
        String key = ownerId.toString();
        NbtCompound contributor = contributors.contains(key, NbtElement.COMPOUND_TYPE)
                ? contributors.getCompound(key)
                : new NbtCompound();
        String previousName = contributor.getString("name");
        String safeName = ownerName == null ? "" : ownerName;
        if (!safeName.isBlank()) {
            contributor.putString("name", safeName);
        }
        boolean changed = !safeName.isBlank() && !safeName.equals(previousName);
        changed |= StoredFishScoreStorage.updateBest(contributor, specimen.fishScore());
        contributors.put(key, contributor);
        root.put(CONTRIBUTORS_KEY, contributors);
        return changed;
    }
}
