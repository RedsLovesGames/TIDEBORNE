package com.redslovesgames.tideborne.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.FishScoreV2Service;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

class TideborneFishingApiTest {
    @Test
    void exposesStoredAndCalculatedFishScoreWithoutChangingSpecimen() {
        SpecimenData specimen = specimen(80.0, 240.0, 1234);

        assertEquals(1234, TideborneFishingApi.readFishScore(specimen).orElseThrow());
        assertEquals(456.5, TideborneFishingApi.readRawFishScore(specimen).orElseThrow(), 0.0001);

        FishScoreV2Service.Result expected = new FishScoreV2Service().calculate(CanonicalRarity.FOUR_STAR, specimen);
        FishScoreV2Service.Result actual = TideborneFishingApi.calculateFishScore(CanonicalRarity.FOUR_STAR, specimen);
        assertEquals(expected, actual);
        assertEquals(80.0, specimen.finalPercentile());
        assertEquals(240.0, specimen.finalLength());
    }

    @Test
    void exposesCanonicalGearQueries() {
        FishingGearModifiers modifiers = new FishingGearModifiers(
                3.0,
                4.0,
                1.25,
                0.8,
                FishingGearModifiers.IdRestriction.only(java.util.Set.of("fish")),
                FishingGearModifiers.IdRestriction.excluding(java.util.Set.of("crate")),
                Map.of(SpecimenData.BodyType.GIANT, 1.5),
                Map.of("bonus", 2.0),
                Map.of("speed", 0.75)
        );

        assertEquals(3.0, TideborneFishingApi.fishingLuck(modifiers));
        assertEquals(4.0, TideborneFishingApi.traitLuck(modifiers));
        assertEquals(1.25, TideborneFishingApi.strengthMultiplier(modifiers));
        assertEquals(0.8, TideborneFishingApi.tempoMultiplier(modifiers));
        assertEquals(1.5, TideborneFishingApi.bodyTypeChanceMultiplier(modifiers, SpecimenData.BodyType.GIANT));
        assertEquals(2.0, TideborneFishingApi.namedAdditiveModifier(modifiers, "bonus"));
        assertEquals(0.75, TideborneFishingApi.namedMultiplierModifier(modifiers, "speed"));
        assertTrue(TideborneFishingApi.categoryAllowed(modifiers, "fish"));
        assertFalse(TideborneFishingApi.categoryAllowed(modifiers, "crate"));
        assertFalse(TideborneFishingApi.catchPoolAllowed(modifiers, "crate"));
        assertTrue(TideborneFishingApi.catchPoolAllowed(modifiers, "fish"));
    }

    @Test
    void recordQueriesDelegateToCanonicalOrdering() {
        SpecimenData lower = specimen(70.0, 210.0, 900);
        SpecimenData higher = specimen(90.0, 260.0, 1500);

        assertTrue(TideborneFishingApi.compareRecords(higher, lower) > 0);
        assertTrue(TideborneFishingApi.shouldReplaceBestRecord(lower, higher));
        assertFalse(TideborneFishingApi.shouldReplaceBestRecord(higher, lower));
        assertTrue(TideborneFishingApi.sameSpecimenIdentity(higher, higher));
        assertFalse(TideborneFishingApi.sameSpecimenIdentity(higher, lower));
    }

    @Test
    void readsCanonicalTransferPayloadWithoutReconstructingSpecimenState() {
        SpecimenData specimen = specimen(88.0, 255.0, 1777);
        NbtCompound transfer = new NbtCompound();
        CanonicalSpecimenStorage.writeTransferData(transfer, specimen);

        assertEquals(specimen, TideborneFishingApi.readTransferredSpecimen(transfer).orElseThrow());
        assertTrue(TideborneFishingApi.readTransferredSpecimen(new NbtCompound()).isEmpty());
        assertTrue(TideborneFishingApi.readTransferredSpecimen(null).isEmpty());
    }

    @Test
    void readsTeamTopFishInCanonicalBestFirstOrder() {
        SpecimenData lower = specimen(65.0, 190.0, 700);
        SpecimenData middle = specimen(78.0, 225.0, 1200);
        SpecimenData higher = specimen(94.0, 280.0, 1900);
        NbtCompound teamRecords = new NbtCompound();

        assertTrue(CanonicalSpecimenRecordIndexer.indexTeamTopFish(
                teamRecords,
                CanonicalSpecimenRecordIndexer.project(middle)
        ));
        assertTrue(CanonicalSpecimenRecordIndexer.indexTeamTopFish(
                teamRecords,
                CanonicalSpecimenRecordIndexer.project(lower)
        ));
        assertTrue(CanonicalSpecimenRecordIndexer.indexTeamTopFish(
                teamRecords,
                CanonicalSpecimenRecordIndexer.project(higher)
        ));

        assertEquals(List.of(higher, middle, lower), TideborneFishingApi.readTeamTopFish(teamRecords));
        assertEquals(1900, TideborneFishingApi.highestTeamScore(teamRecords).orElseThrow());
        assertTrue(TideborneFishingApi.readTeamTopFish(null).isEmpty());
        assertTrue(TideborneFishingApi.highestTeamScore(null).isEmpty());
    }

    private static SpecimenData specimen(double percentile, double length, int fishScore) {
        return new SpecimenData(
                "tide:cod",
                1,
                1,
                fishScore,
                percentile,
                length,
                length,
                percentile,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.of(456.5),
                OptionalInt.of(fishScore),
                SpecimenData.Provenance.generated()
        );
    }
}
