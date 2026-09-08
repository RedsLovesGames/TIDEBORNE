package com.redslovesgames.tideborne.fishing.tide;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.data.fishing.SizeData;
import com.li64.tide.data.fishing.modifiers.FishingModifier;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.specimen.LogNormalSizeDistribution;
import com.redslovesgames.tideborne.fishing.specimen.NoPhysicalSizeDistribution;
import com.redslovesgames.tideborne.fishing.specimen.SizeDistribution;
import com.redslovesgames.tideborne.fishing.SpeciesEligibility;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import com.li64.tide.data.TideTags;
import com.li64.tide.data.fishing.conditions.types.SaltwaterCondition;
import com.li64.tide.data.fishing.conditions.FishingCondition;
import com.li64.tide.data.fishing.conditions.types.EitherCondition;
import com.li64.tide.data.fishing.conditions.types.FishingMediumCondition;
import com.li64.tide.data.fishing.modifiers.types.TemperatureModifier;
import com.li64.tide.data.journal.JournalGroup;
import com.redslovesgames.tideborne.registry.TideboundTags;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

/** Adapts authoritative Tide FishData into context-normalized Fishing System 2.0 species profiles. */
public final class TideSpeciesProfileAdapter {
    private static final double Z10 = -1.2815515655446004;
    private static final double Z90 = 1.2815515655446004;

    public Optional<Candidate> adapt(FishData data, com.li64.tide.data.fishing.FishingContext context) {
        // Tide owns the fishing-environment rules. A profile exists for this cast only after
        // Tide has accepted every configured condition through shouldKeep(context).
        if (data == null || !data.shouldKeep(context)) {
            return Optional.empty();
        }

        double encounterWeight = contextWeightWithoutLegacyQuality(data, context);
        if (!Double.isFinite(encounterWeight) || encounterWeight <= 0.0) {
            return Optional.empty();
        }

        return Optional.of(new Candidate(data, profile(data, encounterWeight)));
    }

    /**
     * Builds the stable species profile needed to migrate an already-existing fish stack.
     * Migration deliberately ignores current biome, weather, bait, luck, and other encounter context,
     * but still preserves Tide's authoritative base selection weight as species metadata.
     */
    public SpeciesProfile adaptForMigration(FishData data) {
        if (data == null) {
            throw new IllegalArgumentException("fish data is required for migration");
        }
        return profile(data, data.weight());
    }

    private static SpeciesProfile profile(FishData data, double encounterWeight) {
        Item fishItem = (Item) data.fish().value();
        String speciesId = Registries.ITEM.getId(fishItem).toString();
        return new SpeciesProfile(
                speciesId,
                CanonicalRarity.fromStars(data.profile().rarity().getNumStars()),
                encounterWeight,
                // Runtime profiles are already context-normalized by FishData#shouldKeep above.
                SpeciesEligibility.always(),
                data.strength(),
                data.speed(),
                serializedBehaviorId(data.behavior().name()),
                data.size().<SizeDistribution>map(TideSpeciesProfileAdapter::sizeDistribution)
                        .orElse(NoPhysicalSizeDistribution.INSTANCE),
                Set.of(),
                Map.of(),
                targetTags(data)
        );
    }

    /** Species classification reads authoritative tags/conditions, never rolled specimen geometry. */
    public static Set<String> targetTags(FishData data) {
        ItemStack fish = new ItemStack(data.fish().value());
        Set<String> tags = new TreeSet<>();
        if (fish.isIn(TideboundTags.LARGE_FISH)) tags.add("large");
        if (fish.isIn(TideboundTags.LEVIATHAN_TARGETS)) tags.add("boss");
        if (fish.isIn(TideboundTags.PREDATORY_FISH)) tags.add("predatory");
        if (fish.isIn(TideboundTags.VERY_SMALL_FISH)) tags.add("very_small");
        if (fish.isIn(TideTags.Items.LEGENDARY_FISH)) tags.add("legendary");
        if (data.profile().group() == JournalGroup.SALTWATER) tags.add("ocean");
        if (data.profile().group() == JournalGroup.LAVA) tags.add("warm");
        if (data.profile().group() == JournalGroup.VOID || data.profile().group() == JournalGroup.UNDERGROUND) tags.add("deep");
        data.conditions().forEach(condition -> addConditionTargets(condition, tags));
        // Tide preferred temperatures are centered on zero; positive means warm affinity.
        if (data.modifiers().stream().anyMatch(modifier -> modifier instanceof TemperatureModifier temperature
                && temperature.getPreferred() > 0)) tags.add("warm");
        if (tags.contains("large") || tags.contains("predatory")) tags.add("heavy");
        if (tags.contains("heavy") || tags.contains("ocean")) tags.add("kujira_target");
        return Set.copyOf(tags);
    }

    private static void addConditionTargets(FishingCondition condition, Set<String> tags) {
        if (condition instanceof SaltwaterCondition) tags.add("ocean");
        if (condition instanceof FishingMediumCondition medium) {
            if ("lava".equals(medium.getMediumId())) tags.add("warm");
            if ("void".equals(medium.getMediumId())) tags.add("deep");
        }
        if (condition instanceof EitherCondition either) {
            addConditionTargets(either.getConditionA(), tags);
            addConditionTargets(either.getConditionB(), tags);
        }
        // Do not descend into negated conditions and infer the opposite association.
    }

    /**
     * Tide serializes MinigameBehavior as the enum name lowercased with Locale.ROOT.
     * Derive the ID from that stable contract instead of a mapping-sensitive interface method.
     */
    static String serializedBehaviorId(String enumName) {
        return enumName.toLowerCase(Locale.ROOT);
    }

    /**
     * Preserves Tide environmental modifiers while leaving gear contributions separate,
     * but intentionally omits Tide's legacy selection_quality path. Fishing System 2.0 applies
     * Fishing Luck once through CanonicalRarity after this boundary.
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

        // Gear remains separate until SpeciesSelectionService can cap the complete gear ratio.
        return weight;
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

    /**
     * Keeps the exact authoritative Tide record attached to the normalized profile. Bucket item,
     * display data, journal flags, parent metadata, and other Tide-only metadata therefore stay
     * available to the runtime bridge without creating duplicate canonical representations.
     */
    public record Candidate(FishData fishData, SpeciesProfile profile) {
    }
}
