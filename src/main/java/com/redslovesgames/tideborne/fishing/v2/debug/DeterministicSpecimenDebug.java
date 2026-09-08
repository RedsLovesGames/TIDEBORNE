package com.redslovesgames.tideborne.fishing.v2.debug;

import com.redslovesgames.tideborne.fishing.v2.BodyTypeGenerator;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpeciesSelectionService;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.SpecimenGenerator;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Pure debug-only specimen reproducer.
 *
 * <p>Normal reproduction delegates to the production {@link SpecimenGenerator}. The optional forced
 * percentile path substitutes only the initial natural percentile/base-length sample, then hands the
 * specimen back to the same canonical Body Type and post-fight services used by production. Nothing
 * in this utility persists data or mutates server/gameplay state.
 */
public final class DeterministicSpecimenDebug {
    private final SpecimenGenerator specimenGenerator;
    private final BodyTypeGenerator bodyTypes;
    private final SpeciesSelectionService speciesSelection;

    public DeterministicSpecimenDebug() {
        this(new SpecimenGenerator(), new BodyTypeGenerator(), new SpeciesSelectionService());
    }

    DeterministicSpecimenDebug(
            SpecimenGenerator specimenGenerator,
            BodyTypeGenerator bodyTypes,
            SpeciesSelectionService speciesSelection
    ) {
        this.specimenGenerator = Objects.requireNonNull(specimenGenerator, "specimenGenerator");
        this.bodyTypes = Objects.requireNonNull(bodyTypes, "bodyTypes");
        this.speciesSelection = Objects.requireNonNull(speciesSelection, "speciesSelection");
    }

    public Reproduction reproduce(
            SpeciesProfile species,
            long specimenSeed,
            double fishingLuck,
            double traitLuck,
            boolean perfectCatch
    ) {
        return reproduce(
                species,
                specimenSeed,
                fishingLuck,
                traitLuck,
                perfectCatch,
                OptionalDouble.empty()
        );
    }

    public Reproduction reproduce(
            SpeciesProfile species,
            long specimenSeed,
            double fishingLuck,
            double traitLuck,
            boolean perfectCatch,
            OptionalDouble forcedPercentile
    ) {
        Objects.requireNonNull(species, "species");
        requireFinite("fishingLuck", fishingLuck);
        requireFinite("traitLuck", traitLuck);
        OptionalDouble normalizedForced = forcedPercentile == null ? OptionalDouble.empty() : forcedPercentile;

        SpecimenData.Provenance provenance = SpecimenData.Provenance.generated();
        SpecimenData preFight;
        if (normalizedForced.isPresent()) {
            preFight = forcedPreFight(
                    species,
                    specimenSeed,
                    traitLuck,
                    normalizedForced.getAsDouble(),
                    provenance
            );
        } else {
            preFight = specimenGenerator.generatePreFight(
                    species,
                    specimenSeed,
                    provenance,
                    traitLuck
            );
        }

        SpecimenData finalized = specimenGenerator.finalizeAfterFight(
                species,
                preFight,
                traitLuck,
                perfectCatch
        );
        double adjustedSpeciesWeight = speciesSelection.adjustedWeight(species, fishingLuck);
        return new Reproduction(
                finalized,
                species.encounterWeight(),
                adjustedSpeciesWeight,
                fishingLuck,
                traitLuck,
                perfectCatch,
                normalizedForced
        );
    }

    private SpecimenData forcedPreFight(
            SpeciesProfile species,
            long specimenSeed,
            double traitLuck,
            double forcedPercentile,
            SpecimenData.Provenance provenance
    ) {
        if (!Double.isFinite(forcedPercentile) || forcedPercentile < 0.0 || forcedPercentile >= 100.0) {
            throw new IllegalArgumentException("forcedPercentile must be finite and in [0, 100)");
        }

        double baseLength = species.sizeDistribution().quantile(forcedPercentile / 100.0);
        SpecimenData forcedBase = new SpecimenData(
                species.speciesId(),
                SpecimenGenerator.SCHEMA_VERSION,
                SpecimenGenerator.GENERATION_VERSION,
                specimenSeed,
                forcedPercentile,
                baseLength,
                baseLength,
                forcedPercentile,
                SpecimenData.BodyType.NORMAL,
                SpecimenData.Condition.NORMAL,
                SpecimenData.Pigmentation.NORMAL,
                SpecimenData.SpecimenQuality.NORMAL,
                false,
                OptionalDouble.empty(),
                OptionalInt.empty(),
                provenance
        );
        return bodyTypes.applyPhysicalSize(species, forcedBase, traitLuck, 1.0);
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }

    /** Complete immutable output needed to paste a deterministic reproduction into a bug report. */
    public record Reproduction(
            SpecimenData specimen,
            double baseSpeciesWeight,
            double adjustedSpeciesWeight,
            double fishingLuck,
            double traitLuck,
            boolean perfectCatch,
            OptionalDouble forcedPercentile
    ) {
        public Reproduction {
            Objects.requireNonNull(specimen, "specimen");
            forcedPercentile = forcedPercentile == null ? OptionalDouble.empty() : forcedPercentile;
        }

        public boolean usedForcedPercentile() {
            return forcedPercentile.isPresent();
        }
    }
}
