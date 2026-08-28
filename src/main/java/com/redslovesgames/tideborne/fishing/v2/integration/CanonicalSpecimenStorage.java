package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.item.TideItemData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tidetraits.component.TideTraitsComponents;
import net.minecraft.item.ItemStack;

/** Writes the canonical Fishing System 2.0 identity while mirroring legacy compatibility components. */
public final class CanonicalSpecimenStorage {
    private CanonicalSpecimenStorage() {
    }

    public static void write(ItemStack stack, SpecimenData specimen) {
        if (stack == null || stack.isEmpty() || specimen == null) {
            return;
        }

        stack.set(TideTraitsComponents.SPECIMEN_SPECIES_ID, specimen.speciesId());
        stack.set(TideTraitsComponents.SPECIMEN_SCHEMA_VERSION, specimen.schemaVersion());
        stack.set(TideTraitsComponents.SPECIMEN_GENERATION_VERSION, specimen.generationVersion());
        stack.set(TideTraitsComponents.SPECIMEN_BASE_PERCENTILE, specimen.basePercentile());
        stack.set(TideTraitsComponents.SPECIMEN_BASE_LENGTH, specimen.baseLength());
        stack.set(TideTraitsComponents.SPECIMEN_FINAL_LENGTH, specimen.finalLength());
        stack.set(TideTraitsComponents.SPECIMEN_CONDITION, specimen.condition().name().toLowerCase());
        stack.set(TideTraitsComponents.SPECIMEN_PIGMENTATION, specimen.pigmentation().name().toLowerCase());
        stack.set(TideTraitsComponents.SPECIMEN_QUALITY, specimen.specimenQuality().name().toLowerCase());
        stack.set(TideTraitsComponents.SPECIMEN_PERFECT_CATCH, specimen.perfectCatch());

        // Compatibility mirrors. Existing 1.x consumers see a normal specimen rather than rerolling it.
        stack.set(TideTraitsComponents.MUTATION_SEED, specimen.deterministicSeed());
        stack.set(TideTraitsComponents.SIZE_PERCENTILE, specimen.finalPercentile());
        stack.set(TideTraitsComponents.BODY_TYPE, specimen.bodyType().name().toLowerCase());
        stack.set(TideTraitsComponents.MUTATION, specimen.condition().name().toLowerCase());

        if (Double.isFinite(specimen.finalLength()) && specimen.finalLength() > 0.0) {
            TideItemData.FISH_LENGTH.set(stack, specimen.finalLength());
        }
    }
}
