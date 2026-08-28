package com.redslovesgames.tidetraits.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SpecimenTransferFishScoreTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.createGameVersion();
        TideTraitsComponents.init();
        Bootstrap.initialize();
    }

    @Test
    void canonicalFishScoreSurvivesSharedStackEntityBucketPayloadRoundTrip() {
        ItemStack source = new ItemStack(Items.COD);
        source.set(TideTraitsComponents.SPECIMEN_SPECIES_ID, "tide:test_fish");
        source.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, 2);
        source.set(TideTraitsComponents.SPECIMEN_BODY_TYPE, "giant");
        source.set(TideTraitsComponents.SPECIMEN_CONDITION, "scarred");
        source.set(TideTraitsComponents.SPECIMEN_PIGMENTATION, "iridescent");
        source.set(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE, 712.5);
        source.set(TideTraitsComponents.SPECIMEN_FISH_SCORE, 2271);

        NbtCompound payload = SpecimenTransfer.fromStack(source);

        assertEquals(SpecimenTransfer.DATA_VERSION, payload.getInt(SpecimenTransfer.VERSION_KEY));
        assertTrue(payload.contains(SpecimenTransfer.CANONICAL_RAW_FISH_SCORE_KEY));
        assertTrue(payload.contains(SpecimenTransfer.CANONICAL_FISH_SCORE_KEY));
        assertEquals(712.5, payload.getDouble(SpecimenTransfer.CANONICAL_RAW_FISH_SCORE_KEY), 1.0E-9);
        assertEquals(2271, payload.getInt(SpecimenTransfer.CANONICAL_FISH_SCORE_KEY));

        ItemStack restored = new ItemStack(Items.COD);
        restored.set(TideTraitsComponents.SPECIMEN_SPECIES_ID, "tide:test_fish");
        restored.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, 2);
        SpecimenTransfer.toStack(payload, restored);

        assertEquals(712.5, restored.get(TideTraitsComponents.SPECIMEN_RAW_FISH_SCORE), 1.0E-9);
        assertEquals(2271, restored.get(TideTraitsComponents.SPECIMEN_FISH_SCORE));
        assertEquals("giant", restored.get(TideTraitsComponents.SPECIMEN_BODY_TYPE));
        assertEquals("scarred", restored.get(TideTraitsComponents.SPECIMEN_CONDITION));
        assertEquals("iridescent", restored.get(TideTraitsComponents.SPECIMEN_PIGMENTATION));
    }
}
