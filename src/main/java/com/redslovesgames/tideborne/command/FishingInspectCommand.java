package com.redslovesgames.tideborne.command;

import com.redslovesgames.tideborne.fishing.SpeciesSelectionService;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenData;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenGenerator;
import com.redslovesgames.tideborne.fishing.specimen.SpecimenQualityService;
import com.redslovesgames.tideborne.fishing.specimen.TraitMomentumProgression;
import com.redslovesgames.tideborne.fishing.specimen.CanonicalCatchStateManager;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation;
import com.redslovesgames.tideborne.presentation.CanonicalSpecimenPresentation.TraitAxis;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Read-only operator diagnostics for the active Fishing System 2.0 catch. */
public final class FishingInspectCommand {
    private static final SpecimenQualityService QUALITY = new SpecimenQualityService();

    private FishingInspectCommand() {
    }

    public static int run(ServerCommandSource source) {
        if (!source.hasPermissionLevel(2)) {
            source.sendError(Text.literal("Fishing inspect requires operator permission level 2."));
            return 0;
        }
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Fishing inspect must be run by a player with an active Tide fishing hook."));
            return 0;
        }

        Optional<CanonicalCatchStateManager.CatchState> active = CanonicalCatchStateManager.findForPlayer(player);
        if (active.isEmpty()) {
            source.sendFeedback(
                    () -> Text.literal("Fishing System 2.0 inspect: no active canonical fish catch for this player."),
                    false
            );
            return 0;
        }

        CanonicalCatchStateManager.CatchState state = active.get();
        SpecimenData specimen = state.specimen();
        CanonicalSpecimenPresentation.View presentation = CanonicalSpecimenPresentation.present(
                specimen,
                state.species().rarity()
        );
        double effectiveTraitLuck = TraitMomentumProgression.effectiveTraitLuck(
                state.context().traitLuck(),
                state.capturedTraitMomentum()
        );

        send(source, "Fishing System 2.0 inspect (read-only)");
        send(source, "Phase: " + (state.specimenFinalized() ? "post-fight finalized" : "pre-fight"));
        send(source, "Fishing Luck: " + number(state.context().fishingLuck())
                + " | Trait Luck: " + number(state.context().traitLuck())
                + " | Momentum: " + state.capturedTraitMomentum()
                + " | Effective Trait Luck: " + number(effectiveTraitLuck));
        send(source, "Eligible species: " + state.eligibleSpecies().size());
        for (SpeciesSelectionService.WeightedSpecies candidate : state.eligibleSpecies()) {
            send(source, " - " + candidate.profile().speciesId()
                    + " | base weight " + number(candidate.baseWeight())
                    + " | final weight " + number(candidate.adjustedWeight()));
        }

        String selectedWeight = state.eligibleSpecies().stream()
                .filter(candidate -> candidate.profile().speciesId().equals(state.species().speciesId()))
                .findFirst()
                .map(candidate -> " | base weight " + number(candidate.baseWeight())
                        + " | final weight " + number(candidate.adjustedWeight()))
                .orElse(" | weight unavailable");
        send(source, "Selected species: " + state.species().speciesId() + selectedWeight
                + " | rarity " + presentation.rarityStars());
        send(source, "Catch seed: " + state.catchSeed() + " | Specimen seed: " + specimen.deterministicSeed());
        send(source, "Natural percentile: " + number(specimen.basePercentile())
                + " | Final percentile: " + presentation.percentile());
        send(source, "Base length: " + number(specimen.baseLength())
                + " | Final length: " + presentation.length());
        send(source, "Body Type: " + presentation.trait(TraitAxis.BODY_TYPE).value());

        if (state.specimenFinalized()) {
            send(source, "Condition: " + presentation.trait(TraitAxis.CONDITION).value()
                    + " | Pigmentation: " + presentation.trait(TraitAxis.PIGMENTATION).value());
            double qualityTraitLuck = effectiveTraitLuck
                    + (specimen.perfectCatch() ? SpecimenGenerator.PERFECT_CATCH_TRAIT_LUCK_BONUS : 0.0);
            double qualityChance = QUALITY.probability(
                    specimen.finalPercentile(),
                    qualityTraitLuck,
                    specimen.perfectCatch()
            );
            send(source, "Quality chance: " + percent(qualityChance)
                    + " | Quality result: " + presentation.trait(TraitAxis.QUALITY).value()
                    + " | Perfect Catch: " + specimen.perfectCatch());
        } else {
            send(source, "Condition: pending | Pigmentation: pending");
            send(source, "Quality chance/result: pending post-fight finalization");
        }

        send(source, "Strength: " + number(state.fightProfile().strength())
                + " | Tempo: " + number(state.fightProfile().tempo())
                + " | Catch-zone area: " + percent(state.fightProfile().catchZoneArea()));
        send(source, "Fight behavior: " + state.fightProfile().behavior());
        send(source, "FishScore: " + presentation.fishScore());
        return 1;
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static String percent(double probability) {
        return String.format(Locale.ROOT, "%.2f%%", probability * 100.0);
    }

    private static void send(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }
}
