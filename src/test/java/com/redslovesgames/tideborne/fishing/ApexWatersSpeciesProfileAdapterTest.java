package com.redslovesgames.tideborne.fishing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ApexWatersSpeciesProfileAdapterTest {
    private final ApexWatersSpeciesProfileAdapter adapter = new ApexWatersSpeciesProfileAdapter();

    @Test
    void apexWaters111HasNoOfficialTideCatchableSpecies() {
        assertTrue(adapter.tideCatchableSpeciesIds().isEmpty());
    }

    @Test
    void greatWhiteEntityAndItemsAreNotInventedAsFishProfiles() {
        Set<String> officialApexIds = Set.of(
                ApexWatersSpeciesProfileAdapter.GREAT_WHITE_SHARK_ENTITY_ID,
                ApexWatersSpeciesProfileAdapter.GREAT_WHITE_SHARK_SPAWN_EGG_ID,
                ApexWatersSpeciesProfileAdapter.RAW_SHARK_MEAT_ID,
                ApexWatersSpeciesProfileAdapter.COOKED_SHARK_MEAT_ID
        );

        for (String id : officialApexIds) {
            assertFalse(adapter.supports(id), id + " must not be treated as a Tide-catchable species");
            assertTrue(adapter.adapt(id).isEmpty(), id + " must not receive a synthetic SpeciesProfile");
        }
    }

    @Test
    void adapterApiDoesNotReferenceOptionalApexClasses() {
        Class<?> type = ApexWatersSpeciesProfileAdapter.class;
        Stream<Class<?>> apiTypes = Stream.concat(
                Stream.of(type.getDeclaredFields()).map(field -> field.getType()),
                Stream.of(type.getDeclaredMethods()).flatMap(method -> Stream.concat(
                        Stream.of(method.getReturnType()),
                        Stream.of(method.getParameterTypes())
                ))
        );

        assertTrue(apiTypes.noneMatch(candidate -> candidate.getName().startsWith("com.acorsicanfrog.apexwaters")));
    }

    @Test
    void malformedSpeciesIdsFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> adapter.adapt("great_white_shark"));
        assertThrows(IllegalArgumentException.class, () -> adapter.supports(""));
    }
}
