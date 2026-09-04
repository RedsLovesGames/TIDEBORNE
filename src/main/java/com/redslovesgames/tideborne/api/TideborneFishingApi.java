package com.redslovesgames.tideborne.api;

import com.li64.tide.data.TideData;
import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.v2.CanonicalRarity;
import com.redslovesgames.tideborne.fishing.v2.FishScoreV2Service;
import com.redslovesgames.tideborne.fishing.v2.FishingGearModifiers;
import com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry;
import com.redslovesgames.tideborne.fishing.v2.SpeciesProfile;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenRecordIndexer;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideborne.fishing.v2.integration.TideSpeciesProfileAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/**
 * Small stable read/query facade for canonical Tideborne fishing state.
 *
 * <p>Feature code should prefer this boundary over reaching into persistence, scoring, gear,
 * species-adapter, or record-index implementation classes. This facade does not create a second
 * specimen representation: it returns the existing canonical domain records unchanged.
 */
public final class TideborneFishingApi {
    private static final FishScoreV2Service FISH_SCORE = new FishScoreV2Service();
    private static final TideSpeciesProfileAdapter TIDE_SPECIES = new TideSpeciesProfileAdapter();

    private TideborneFishingApi() {
    }

    /**
     * Reads canonical specimen state from a fish stack. Legacy stacks may be migrated by the
     * canonical persistence boundary before the result is returned.
     */
    public static Optional<SpecimenData> readSpecimen(ItemStack stack) {
        return CanonicalSpecimenStorage.read(stack);
    }

    /** Reads canonical specimen state from transfer/record NBT without mutation or legacy fallback. */
    public static Optional<SpecimenData> readTransferredSpecimen(NbtCompound source) {
        return CanonicalSpecimenStorage.readTransferData(source);
    }

    public static OptionalInt readFishScore(ItemStack stack) {
        Optional<SpecimenData> specimen = readSpecimen(stack);
        return specimen.isPresent() ? readFishScore(specimen.orElseThrow()) : OptionalInt.empty();
    }

    public static OptionalInt readFishScore(SpecimenData specimen) {
        return specimen == null ? OptionalInt.empty() : specimen.fishScore();
    }

    public static OptionalDouble readRawFishScore(ItemStack stack) {
        Optional<SpecimenData> specimen = readSpecimen(stack);
        return specimen.isPresent() ? readRawFishScore(specimen.orElseThrow()) : OptionalDouble.empty();
    }

    public static OptionalDouble readRawFishScore(SpecimenData specimen) {
        return specimen == null ? OptionalDouble.empty() : specimen.rawFishScore();
    }

    /** Explicit score calculation for finalized specimen state. Read paths never reroll a specimen. */
    public static FishScoreV2Service.Result calculateFishScore(CanonicalRarity rarity, SpecimenData specimen) {
        return FISH_SCORE.calculate(rarity, specimen);
    }

    /** Resolves a registered Tide/Tideborne gear identity without exposing registry maps. */
    public static Optional<FishingGearRegistry.GearProfile> resolveGearProfile(ItemStack stack) {
        return FishingGearRegistry.resolve(stack);
    }

    public static double fishingLuck(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).fishingLuck();
    }

    public static double traitLuck(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).traitLuck();
    }

    public static double strengthMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).strengthMultiplier();
    }

    public static double tempoMultiplier(FishingGearModifiers modifiers) {
        return requireModifiers(modifiers).tempoMultiplier();
    }

    public static double bodyTypeChanceMultiplier(
            FishingGearModifiers modifiers,
            SpecimenData.BodyType bodyType
    ) {
        return requireModifiers(modifiers).bodyTypeChanceMultiplier(bodyType);
    }

    public static double namedAdditiveModifier(FishingGearModifiers modifiers, String key) {
        return requireModifiers(modifiers).namedAdditiveModifier(key);
    }

    public static double namedMultiplierModifier(FishingGearModifiers modifiers, String key) {
        return requireModifiers(modifiers).namedMultiplierModifier(key);
    }

    public static boolean categoryAllowed(FishingGearModifiers modifiers, String categoryId) {
        return requireModifiers(modifiers).categoryRestriction().allows(categoryId);
    }

    public static boolean catchPoolAllowed(FishingGearModifiers modifiers, String catchId) {
        return requireModifiers(modifiers).catchPoolRestriction().allows(catchId);
    }

    /**
     * Resolves the context-independent canonical profile for a Tide fish item. This is metadata
     * access only and intentionally does not apply current biome, weather, bait, or luck rules.
     */
    public static Optional<SpeciesProfile> resolveSpeciesProfile(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        for (FishData data : TideData.FISH.get().values()) {
            if (data != null && data.fish().value() == stack.getItem()) {
                return adaptSpecies(data);
            }
        }
        return Optional.empty();
    }

    /** Resolves context-independent canonical species metadata by namespaced fish item ID. */
    public static Optional<SpeciesProfile> resolveSpeciesProfile(String speciesId) {
        if (speciesId == null || speciesId.isBlank() || !speciesId.contains(":")) {
            return Optional.empty();
        }
        for (FishData data : TideData.FISH.get().values()) {
            Optional<SpeciesProfile> profile = adaptSpecies(data);
            if (profile.filter(value -> value.speciesId().equals(speciesId)).isPresent()) {
                return profile;
            }
        }
        return Optional.empty();
    }

    /** Returns all currently registered Tide species profiles in stable species-ID order. */
    public static List<SpeciesProfile> speciesProfiles() {
        Map<String, SpeciesProfile> profiles = new TreeMap<>();
        for (FishData data : TideData.FISH.get().values()) {
            adaptSpecies(data).ifPresent(profile -> profiles.putIfAbsent(profile.speciesId(), profile));
        }
        return List.copyOf(profiles.values());
    }

    public static int compareRecords(SpecimenData left, SpecimenData right) {
        return CanonicalSpecimenRecordIndexer.compareSpecimens(left, right);
    }

    public static boolean shouldReplaceBestRecord(SpecimenData current, SpecimenData candidate) {
        return CanonicalSpecimenRecordIndexer.shouldReplaceBest(current, candidate);
    }

    public static boolean sameSpecimenIdentity(SpecimenData left, SpecimenData right) {
        return CanonicalSpecimenRecordIndexer.sameSpecimenIdentity(left, right);
    }

    public static OptionalInt highestTeamScore(NbtCompound teamRecords) {
        return CanonicalSpecimenRecordIndexer.highestTeamScore(teamRecords);
    }

    /** Reads valid canonical Team Top Fish specimens in the indexer's stored best-first order. */
    public static List<SpecimenData> readTeamTopFish(NbtCompound teamRecords) {
        if (teamRecords == null) {
            return List.of();
        }
        List<SpecimenData> specimens = new ArrayList<>();
        for (NbtElement element : teamRecords.getList(
                CanonicalSpecimenRecordIndexer.TEAM_TOP_FISH_KEY,
                NbtElement.COMPOUND_TYPE
        )) {
            if (element instanceof NbtCompound record) {
                CanonicalSpecimenStorage.readTransferData(record).ifPresent(specimens::add);
            }
        }
        return List.copyOf(specimens);
    }

    private static Optional<SpeciesProfile> adaptSpecies(FishData data) {
        if (data == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(TIDE_SPECIES.adaptForMigration(data));
        } catch (IllegalArgumentException | ClassCastException exception) {
            return Optional.empty();
        }
    }

    private static FishingGearModifiers requireModifiers(FishingGearModifiers modifiers) {
        return Objects.requireNonNull(modifiers, "modifiers");
    }
}
