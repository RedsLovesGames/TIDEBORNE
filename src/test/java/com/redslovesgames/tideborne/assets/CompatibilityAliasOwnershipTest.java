package com.redslovesgames.tideborne.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CompatibilityAliasOwnershipTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ASSETS = RESOURCES.resolve("assets");
    private static final Path DATA = RESOURCES.resolve("data");

    @Test
    void historicalAssetNamespacesAreAliasOnly() throws IOException {
        assertEquals(Set.of("lang", "models"), childNames(ASSETS.resolve("tide_traits")));
        assertEquals(Set.of("lang", "models"), childNames(ASSETS.resolve("tidebound_compatibility")));
        assertFalse(Files.exists(ASSETS.resolve("tide_team_journal")));
        assertEquals(4, regularFileCount(ASSETS.resolve("tide_traits/models/item")));
        assertEquals(13, regularFileCount(ASSETS.resolve("tidebound_compatibility/models/item")));

        String traitsLang = Files.readString(ASSETS.resolve("tide_traits/lang/en_us.json"));
        assertTrue(traitsLang.contains("\"item.tide_traits.anglers_satchel\""));
        assertFalse(traitsLang.contains("\"message.tide_traits."));
        assertFalse(traitsLang.contains("\"tooltip.tide_traits."));

        String fishingLang = Files.readString(ASSETS.resolve("tidebound_compatibility/lang/en_us.json"));
        assertTrue(fishingLang.contains("\"item.tidebound_compatibility.leviathan_bait\""));
        assertFalse(fishingLang.contains("\"message.tidebound_compatibility."));
        assertFalse(fishingLang.contains("\"tooltip.tidebound_compatibility."));
        assertFalse(fishingLang.contains("\"toast.tidebound_compatibility."));
        assertFalse(fishingLang.contains("\"key.tidebound_compatibility."));
    }

    @Test
    void historicalDataNamespacesContainOnlyCompatibilitySurfaces() throws IOException {
        assertEquals(Set.of("function", "recipe", "tags"), childNames(DATA.resolve("tide_traits")));
        assertEquals(Set.of("recipe", "tags"), childNames(DATA.resolve("tidebound_compatibility")));
        assertEquals(Set.of("anglers_satchel.json"), childNames(DATA.resolve("tide_traits/recipe")));
        assertEquals(8, regularFileCount(DATA.resolve("tide_traits/tags/item")));
        assertEquals(11, regularFileCount(DATA.resolve("tidebound_compatibility/recipe")));
        assertEquals(8, regularFileCount(DATA.resolve("tidebound_compatibility/tags/item")));
    }

    @Test
    void historicalDiagnosticFunctionIsOnlyAForwardingAlias() throws IOException {
        Path alias = DATA.resolve("tide_traits/function/test_tuna_mutations.mcfunction");
        Path canonical = DATA.resolve("tideborne/function/test_tuna_mutations.mcfunction");
        assertEquals("function tideborne:test_tuna_mutations\n", Files.readString(alias));
        String body = Files.readString(canonical);
        assertEquals(7, body.lines().filter(line -> line.startsWith("give @s tide:tuna[")).count());
        assertTrue(body.contains("tide_traits:mutation=\"albino\""));
        assertTrue(body.contains("tide_traits:mutation_seed=707"));
    }

    @Test
    void historicalModIdsRemainExplicitFabricCompatibilityAliases() throws IOException {
        String fabric = Files.readString(RESOURCES.resolve("fabric.mod.json"));
        assertTrue(fabric.contains("\"tide_traits\""));
        assertTrue(fabric.contains("\"tide_team_journal\""));
        assertTrue(fabric.contains("\"tidebound_compatibility\""));
        assertTrue(fabric.contains("\"id\": \"tideborne\""));
    }

    @Test
    void foreignExtensionNamespacesRemainPresent() {
        assertTrue(Files.isDirectory(ASSETS.resolve("tide")));
        assertTrue(Files.isDirectory(DATA.resolve("tide")));
        assertTrue(Files.isDirectory(DATA.resolve("myths_of_the_sea")));
        assertTrue(Files.isDirectory(DATA.resolve("c")));
        assertTrue(Files.isDirectory(DATA.resolve("minecraft")));
    }

    private static Set<String> childNames(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.map(path -> path.getFileName().toString()).collect(Collectors.toSet());
        }
    }

    private static long regularFileCount(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.filter(Files::isRegularFile).count();
        }
    }
}
