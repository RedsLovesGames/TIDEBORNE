package com.redslovesgames.tideborne.command;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.debug.DeterministicSpecimenDebug;
import com.redslovesgames.tideborne.fishing.tide.TideSpeciesProfileAdapter;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Read-only operator tooling for reproducing canonical Fishing System 2.0 specimens from known inputs. */
public final class FishingReproduceCommand {
    private static final TideSpeciesProfileAdapter PROFILES = new TideSpeciesProfileAdapter();
    private static final DeterministicSpecimenDebug DEBUG = new DeterministicSpecimenDebug();

    private FishingReproduceCommand() {
    }

    public static int run(
            ServerCommandSource source,
            String speciesId,
            long specimenSeed,
            double fishingLuck,
            double traitLuck,
            boolean perfectCatch,
            OptionalDouble forcedPercentile
    ) {
        if (!source.hasPermissionLevel(2)) {
            source.sendError(Text.literal("Fishing specimen reproduction requires operator permission level 2."));
            return 0;
        }

        Identifier requested = Identifier.tryParse(speciesId);
        if (requested == null) {
            source.sendError(Text.literal("Invalid namespaced fish species ID: " + speciesId));
            return 0;
        }

        Optional<SpeciesProfile> profile = findSpecies(requested);
        if (profile.isEmpty()) {
            source.sendError(Text.literal("Unknown Tide fish species: " + requested));
            return 0;
        }

        try {
            DeterministicSpecimenDebug.Reproduction reproduction = DEBUG.reproduce(
                    profile.get(),
                    specimenSeed,
                    fishingLuck,
                    traitLuck,
                    perfectCatch,
                    forcedPercentile
            );
            report(source, reproduction);
            return 1;
        } catch (IllegalArgumentException ex) {
            source.sendError(Text.literal("Unable to reproduce specimen: " + ex.getMessage()));
            return 0;
        }
    }

    private static Optional<SpeciesProfile> findSpecies(Identifier requested) {
        for (FishData data : TideData.FISH.get().values()) {
            Item item = (Item) data.fish().value();
            if (requested.equals(Registries.ITEM.getId(item))) {
                return Optional.of(PROFILES.adaptForMigration(data));
            }
        }
        return Optional.empty();
    }

    private static void report(
            ServerCommandSource source,
            DeterministicSpecimenDebug.Reproduction reproduction
    ) {
        SpecimenData specimen = reproduction.specimen();
        String forced = reproduction.forcedPercentile().isPresent()
                ? number(reproduction.forcedPercentile().getAsDouble())
                : "none";

        send(source, "Fishing System 2.0 deterministic specimen reproduction (read-only)");
        send(source, "Repro key: species=" + specimen.speciesId()
                + " specimenSeed=" + specimen.deterministicSeed()
                + " fishingLuck=" + number(reproduction.fishingLuck())
                + " traitLuck=" + number(reproduction.traitLuck())
                + " perfectCatch=" + reproduction.perfectCatch()
                + " forcedPercentile=" + forced);
        if (reproduction.usedForcedPercentile()) {
            send(source, "DEBUG override: natural percentile was forced for this report only; production generation rules were not changed.");
        }
        send(source, "Species weight: base " + number(reproduction.baseSpeciesWeight())
                + " | with Fishing Luck " + number(reproduction.adjustedSpeciesWeight()));
        send(source, "Natural percentile: " + number(specimen.basePercentile())
                + " | Final percentile: " + number(specimen.finalPercentile()));
        send(source, "Base length: " + number(specimen.baseLength())
                + " | Final length: " + number(specimen.finalLength()));
        send(source, "Body Type: " + specimen.bodyType()
                + " | Condition: " + specimen.condition()
                + " | Pigmentation: " + specimen.pigmentation());
        send(source, "Quality: " + specimen.specimenQuality()
                + " | Perfect Catch: " + specimen.perfectCatch());
        send(source, "Raw FishScore: " + (specimen.rawFishScore().isPresent()
                ? number(specimen.rawFishScore().getAsDouble())
                : "none")
                + " | FishScore: " + (specimen.fishScore().isPresent()
                ? Integer.toString(specimen.fishScore().getAsInt())
                : "none"));
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static void send(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }
}
