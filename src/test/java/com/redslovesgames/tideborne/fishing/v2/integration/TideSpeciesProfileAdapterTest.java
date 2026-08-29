package com.redslovesgames.tideborne.fishing.v2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.MinigameBehavior;
import com.li64.tide.data.fishing.SizeData;
import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;

class TideSpeciesProfileAdapterTest {
    @Test
    void migrationProfilePreservesAuthoritativeTideSpeciesFields() {
        FishData data = FishData.builder()
                .fish(Items.COD)
                .selectionWeight(37.5)
                .selectionQuality(9000.0)
                .strength(0.72f)
                .speed(1.35f)
                .behavior(MinigameBehavior.LINEAR_WRAP)
                .size(12.5, 47.0, 88.0)
                .build();

        SpeciesProfile profile = new TideSpeciesProfileAdapter().adaptForMigration(data);

        assertEquals("minecraft:cod", profile.speciesId());
        assertEquals(CanonicalRarity.fromStars(data.profile().rarity().getNumStars()), profile.rarity());
        assertEquals(37.5, profile.encounterWeight(), 0.0);
        assertEquals(data.strength(), profile.strength(), 0.0);
        assertEquals(data.speed(), profile.tempo(), 0.0);
        assertEquals("linear_wrap", profile.behavior());
        assertEquals(12.5, profile.sizeDistribution().quantile(0.10), 1.0e-7);
        assertEquals(47.0, profile.sizeDistribution().quantile(0.90), 1.0e-7);
    }

    @Test
    void migrationProfileDoesNotTurnSelectionQualityIntoFishingLuckWeight() {
        FishData lowQuality = FishData.builder()
                .fish(Items.COD)
                .selectionWeight(23.0)
                .selectionQuality(0.0)
                .build();
        FishData highQuality = FishData.builder()
                .fish(Items.COD)
                .selectionWeight(23.0)
                .selectionQuality(10000.0)
                .build();

        TideSpeciesProfileAdapter adapter = new TideSpeciesProfileAdapter();
        assertEquals(
                adapter.adaptForMigration(lowQuality).encounterWeight(),
                adapter.adaptForMigration(highQuality).encounterWeight(),
                0.0
        );
    }

    @Test
    void tideTypicalBoundsRemainCanonicalP10AndP90() {
        LogNormalSizeDistribution distribution = TideSpeciesProfileAdapter.sizeDistribution(new SizeData(12.5, 47.0, 88.0));

        // Tide's frozen z anchors and our dependency-free inverse CDF differ only at the
        // approximation floor (roughly 1e-8 cm here), so keep the test tight without
        // requiring bit-identical implementations of the normal inverse.
        assertEquals(12.5, distribution.quantile(0.10), 1.0e-7);
        assertEquals(47.0, distribution.quantile(0.90), 1.0e-7);
        assertEquals(10.0, distribution.percentile(12.5), 5.0e-4);
        assertEquals(90.0, distribution.percentile(47.0), 5.0e-4);
    }
}
