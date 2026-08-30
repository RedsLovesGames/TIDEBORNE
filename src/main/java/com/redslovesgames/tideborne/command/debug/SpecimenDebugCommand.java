package com.redslovesgames.tideborne.command.debug;

import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.BodyTypeGenerator;
import com.redslovesgames.tideborne.fishing.v2.FishScoreV2Service;
import com.redslovesgames.tideborne.fishing.v2.NoPhysicalSizeDistribution;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.TideSpeciesProfileAdapter;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Server-authoritative operator edits for canonical Fishing System 2.0 specimens. */
public final class SpecimenDebugCommand {
    private static final TideSpeciesProfileAdapter PROFILES = new TideSpeciesProfileAdapter();
    private static final BodyTypeGenerator BODY_TYPES = new BodyTypeGenerator();
    private static final FishScoreV2Service FISH_SCORES = new FishScoreV2Service();

    private SpecimenDebugCommand() {
    }

    public static int setPercentile(ServerCommandSource source, double percentile) {
        Optional<Target> target = target(source);
        if (target.isEmpty()) {
            return 0;
        }
        Target value = target.get();
        SpecimenData current = value.specimen();

        double baseLength = current.baseLength();
        if (value.profile().sizeDistribution() != NoPhysicalSizeDistribution.INSTANCE) {
            if (percentile <= 0.0 || percentile >= 100.0) {
                return fail(source, "Physical fish percentiles must be greater than 0 and less than 100.");
            }
            baseLength = value.profile().sizeDistribution().quantile(percentile / 100.0);
            if (!Double.isFinite(baseLength) || baseLength <= 0.0) {
                return fail(source, "That percentile does not produce a finite positive physical length.");
            }
        }

        SpecimenData naturalEdit = new SpecimenData(
                current.speciesId(),
                current.schemaVersion(),
                current.generationVersion(),
                current.deterministicSeed(),
                percentile,
                baseLength,
                baseLength,
                percentile,
                current.bodyType(),
                current.condition(),
                current.pigmentation(),
                current.specimenQuality(),
                current.perfectCatch(),
                OptionalDouble.empty(),
                OptionalInt.empty(),
                current.provenance()
        );
        SpecimenData resized = BODY_TYPES.applyPhysicalSize(value.profile(), naturalEdit, current.bodyType());
        return persist(source, value, resized,
                String.format(Locale.ROOT, "Set canonical natural percentile to %.4f.", percentile));
    }

    public static int setBodyType(ServerCommandSource source, SpecimenData.BodyType bodyType) {
        Optional<Target> target = target(source);
        if (target.isEmpty()) {
            return 0;
        }
        Target value = target.get();
        if (bodyType != SpecimenData.BodyType.NORMAL
                && value.profile().sizeDistribution() == NoPhysicalSizeDistribution.INSTANCE) {
            return fail(source, "Giant and Dwarf require a species with physical SizeData.");
        }
        SpecimenData edited = BODY_TYPES.applyPhysicalSize(value.profile(), withoutScore(value.specimen()), bodyType);
        return persist(source, value, edited, "Set canonical Body Type to " + serialized(bodyType) + ".");
    }

    public static int setCondition(ServerCommandSource source, SpecimenData.Condition condition) {
        Optional<Target> target = target(source);
        if (target.isEmpty()) {
            return 0;
        }
        Target value = target.get();
        SpecimenData current = value.specimen();
        SpecimenData edited = new SpecimenData(
                current.speciesId(),
                current.schemaVersion(),
                current.generationVersion(),
                current.deterministicSeed(),
                current.basePercentile(),
                current.baseLength(),
                current.finalLength(),
                current.finalPercentile(),
                current.bodyType(),
                condition,
                current.pigmentation(),
                current.specimenQuality(),
                current.perfectCatch(),
                OptionalDouble.empty(),
                OptionalInt.empty(),
                current.provenance()
        );
        return persist(source, value, edited, "Set canonical Condition to " + serialized(condition) + ".");
    }

    public static int setPigmentation(ServerCommandSource source, SpecimenData.Pigmentation pigmentation) {
        Optional<Target> target = target(source);
        if (target.isEmpty()) {
            return 0;
        }
        Target value = target.get();
        SpecimenData current = value.specimen();
        SpecimenData edited = new SpecimenData(
                current.speciesId(),
                current.schemaVersion(),
                current.generationVersion(),
                current.deterministicSeed(),
                current.basePercentile(),
                current.baseLength(),
                current.finalLength(),
                current.finalPercentile(),
                current.bodyType(),
                current.condition(),
                pigmentation,
                current.specimenQuality(),
                current.perfectCatch(),
                OptionalDouble.empty(),
                OptionalInt.empty(),
                current.provenance()
        );
        return persist(source, value, edited, "Set canonical Pigmentation to " + serialized(pigmentation) + ".");
    }

    private static Optional<Target> target(ServerCommandSource source) {
        ServerPlayerEntity player;
        try {
            player = source.getPlayerOrThrow();
        } catch (Exception exception) {
            fail(source, "This command must be run by a player.");
            return Optional.empty();
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            fail(source, "Hold a Tide fish in your main hand.");
            return Optional.empty();
        }

        Optional<FishData> fish = FishData.get(stack);
        if (fish.isEmpty()) {
            fail(source, "Hold a Tide-recognized fish item, not a bucket, in your main hand.");
            return Optional.empty();
        }

        Optional<SpecimenData> specimen = CanonicalSpecimenStorage.read(stack);
        if (specimen.isEmpty()) {
            fail(source, "The held fish has no readable canonical Fishing System 2.0 specimen.");
            return Optional.empty();
        }

        SpeciesProfile profile;
        try {
            profile = PROFILES.adaptForMigration(fish.get());
        } catch (RuntimeException exception) {
            fail(source, "Could not resolve the canonical species profile: " + exception.getMessage());
            return Optional.empty();
        }

        if (!profile.speciesId().equals(specimen.get().speciesId())) {
            fail(source, "Held item species does not match its canonical specimen identity.");
            return Optional.empty();
        }
        return Optional.of(new Target(stack, profile, specimen.get()));
    }

    private static int persist(ServerCommandSource source, Target target, SpecimenData edited, String message) {
        FishScoreV2Service.Result score = FISH_SCORES.calculate(target.profile().rarity(), edited);
        SpecimenData scored = withScore(edited, score);
        CanonicalSpecimenStorage.write(target.stack(), scored);
        source.sendFeedback(
                () -> Text.literal(String.format(
                        Locale.ROOT,
                        "%s Final length %.3f cm, final percentile %.4f, FishScore %d.",
                        message,
                        scored.finalLength(),
                        scored.finalPercentile(),
                        scored.fishScore().orElse(1)
                )),
                false
        );
        return 1;
    }

    private static SpecimenData withoutScore(SpecimenData specimen) {
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

    private static SpecimenData withScore(SpecimenData specimen, FishScoreV2Service.Result score) {
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
                OptionalDouble.of(score.rawScore()),
                OptionalInt.of(score.fishScore()),
                specimen.provenance()
        );
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }

    private static int fail(ServerCommandSource source, String message) {
        source.sendError(Text.literal(message));
        return 0;
    }

    private record Target(ItemStack stack, SpeciesProfile profile, SpecimenData specimen) {
    }
}
