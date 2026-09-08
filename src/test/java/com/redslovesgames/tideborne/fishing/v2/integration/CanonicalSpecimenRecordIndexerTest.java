package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Test;

class CanonicalSpecimenRecordIndexerTest {
    @Test
    void topFishKeepsExactlyBestFifteenAndAllowsRepeatedSpecies() {
        NbtCompound root = new NbtCompound();
        for (int i = 0; i < 20; i++) {
            String species = i % 2 == 0 ? "tide:cod" : "tide:tuna";
            NbtCompound candidate = CanonicalSpecimenRecordIndexer.project(specimen(species, i + 1L, 1000 + i, 50 + i, 30 + i));
            candidate.putString("catcher_name", "angler-" + i);
            assertTrue(CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, candidate));
        }

        NbtList list = root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10);
        assertEquals(15, list.size());
        assertEquals(1019, score((NbtCompound) list.get(0)));
        assertEquals(1005, score((NbtCompound) list.get(14)));
        long cod = list.stream().map(element -> ((NbtCompound) element).getString("fish")).filter("tide:cod"::equals).count();
        long tuna = list.stream().map(element -> ((NbtCompound) element).getString("fish")).filter("tide:tuna"::equals).count();
        assertTrue(cod > 1);
        assertTrue(tuna > 1);
    }

    @Test
    void duplicateCanonicalIdentityIsNeverInsertedTwice() {
        NbtCompound root = new NbtCompound();
        SpecimenData specimen = specimen("tide:cod", 77L, 2200, 91.0, 52.0);
        NbtCompound first = CanonicalSpecimenRecordIndexer.project(specimen);
        first.putString("catcher_name", "first");
        NbtCompound duplicate = CanonicalSpecimenRecordIndexer.project(specimen);
        duplicate.putString("catcher_name", "second");

        assertTrue(CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, first));
        NbtCompound once = root.copy();
        assertFalse(CanonicalSpecimenRecordIndexer.indexTeamTopFish(root, duplicate));
        assertEquals(once, root);
        assertEquals(1, root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10).size());
    }

    @Test
    void equalScoresUsePercentileLengthThenStableSeed() {
        NbtCompound root = new NbtCompound();
        CanonicalSpecimenRecordIndexer.indexTeamTopFish(root,
                CanonicalSpecimenRecordIndexer.project(specimen("tide:cod", 30L, 2000, 80.0, 40.0)));
        CanonicalSpecimenRecordIndexer.indexTeamTopFish(root,
                CanonicalSpecimenRecordIndexer.project(specimen("tide:cod", 20L, 2000, 81.0, 39.0)));
        CanonicalSpecimenRecordIndexer.indexTeamTopFish(root,
                CanonicalSpecimenRecordIndexer.project(specimen("tide:cod", 10L, 2000, 81.0, 41.0)));
        CanonicalSpecimenRecordIndexer.indexTeamTopFish(root,
                CanonicalSpecimenRecordIndexer.project(specimen("tide:cod", 5L, 2000, 81.0, 41.0)));

        NbtList list = root.getList(CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY, 10);
        assertEquals(5L, read((NbtCompound) list.get(0)).deterministicSeed());
        assertEquals(10L, read((NbtCompound) list.get(1)).deterministicSeed());
        assertEquals(20L, read((NbtCompound) list.get(2)).deterministicSeed());
        assertEquals(30L, read((NbtCompound) list.get(3)).deterministicSeed());
    }

    @Test
    void projectionPreservesExactCanonicalTraitIdentity() {
        SpecimenData specimen = specimen("tide:cod", 123L, 2875, 97.4, 66.2);
        NbtCompound projected = CanonicalSpecimenRecordIndexer.project(specimen);
        SpecimenData restored = read(projected);

        assertEquals(specimen.deterministicSeed(), restored.deterministicSeed());
        assertEquals(specimen.finalPercentile(), restored.finalPercentile());
        assertEquals(specimen.finalLength(), restored.finalLength());
        assertEquals(specimen.bodyType(), restored.bodyType());
        assertEquals(specimen.condition(), restored.condition());
        assertEquals(specimen.pigmentation(), restored.pigmentation());
        assertEquals(specimen.specimenQuality(), restored.specimenQuality());
        assertEquals(specimen.fishScore(), restored.fishScore());
    }

    private static int score(NbtCompound tag) {
        return CanonicalSpecimenStorage.readTransferData(tag).orElseThrow().fishScore().orElseThrow();
    }

    private static SpecimenData read(NbtCompound tag) {
        return CanonicalSpecimenStorage.readTransferData(tag).orElseThrow();
    }

    private static SpecimenData specimen(String species, long seed, int score, double percentile, double length) {
        return new SpecimenData(
                species,
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                seed,
                Math.max(0.0, percentile - 2.0),
                Math.max(0.0, length - 1.0),
                length,
                percentile,
                SpecimenData.BodyType.GIANT,
                SpecimenData.Condition.SCARRED,
                SpecimenData.Pigmentation.IRIDESCENT,
                SpecimenData.SpecimenQuality.PERFECT_SPECIMEN,
                true,
                OptionalDouble.of(score + 0.25),
                OptionalInt.of(score),
                SpecimenData.Provenance.generated()
        );
    }
}
