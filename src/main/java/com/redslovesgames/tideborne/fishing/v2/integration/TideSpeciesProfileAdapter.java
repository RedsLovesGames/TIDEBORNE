package com.redslovesgames.tideborne.fishing.v2.integration;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.data.fishing.modifiers.FishingModifier;
import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.NoPhysicalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideboundcompatibility.fishing.FishingModifiers;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

/** Adapts authoritative Tide FishData into context-normalized Fishing System 2.0 species profiles. */
public final class TideSpeciesProfileAdapter {
    private static final double Z10 = -1.2815515655446004;
    private static final double Z90 = 1.2815515655446004;

    public Optional<Candidate> adapt(FishData data, com.li64.tide.data.fishing.FishingContext context) {
        if (data == null || !data.shouldKeep(context)) {
            return Optional.empty();
        }

        double encounterWeight = contextWeightWithoutLegacyQuality(data, context);
        if (!Double.isFinite(encounterWeight) || encounterWeight <= 0.0) {
            return Optional.empty();
        }

        Item fishItem = (Item) data.fish().value();
        String speciesId = Registries.ITEM.getId(fishItem).toString();
        SpeciesProfile profile = new SpeciesProfile(
                speciesId,
                CanonicalRarity.fromStars(data.profile().rarity().getNumStars()),
                encounterWeight,
                SpeciesEligibility.always(),
                data.strength(),
                data.speed(),
                data.behavior().toString(),
                data.size().<SizeDistribution>map(TideSpeciesProfileAdapter::sizeDistribution)
                        .orElse(NoPhysicalSizeDistribution.INSTANCE),
                Set.of(),
                Map.of()
        );
        return Optional.of(new Candidate(data, profile));
    }

    /**
     * Preserves Tide environmental/compatibility modifiers but intentionally omits selection_quality.
     * Fishing System 2.0 applies Fishing Luck once through CanonicalRarity after this boundary.
     */
    static double contextWeightWithoutLegacyQuality(
            FishData data,
            com.li64.tide.data.fishing.FishingContext context
    ) {
        double weight = data.weight();
        if (!Double.isFinite(weight) || weight <= 0.0) {
            return 0.0;
        }
        for (FishingModifier modifier : data.modifiers()) {
            weight = modifier.apply(weight, context);
            if (!Double.isFinite(weight) || weight <= 0.0) {
                return 0.0;
            }
        }
        return FishingModifiers.modifyFishWeight(data, context, weight);
    }

    static LogNormalSizeDistribution sizeDistribution(SizeData size) {
        double low = size.typicalLowCm();
        double high = size.typicalHighCm();
        if (!Double.isFinite(low) || !Double.isFinite(high) || low <= 0.0 || high <= low) {
            throw new IllegalArgumentException("Tide size range must contain positive increasing typical bounds");
        }
        double lnLow = Math.log(low);
        double lnHigh = Math.log(high);
        double sigma = (lnHigh - lnLow) / (Z90 - Z10);
        double median = Math.exp(lnLow - Z10 * sigma);
        return new LogNormalSizeDistribution(median, sigma);
    }

    public record Candidate(FishData fishData, SpeciesProfile profile) {
    }
}
