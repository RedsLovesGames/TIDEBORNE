package com.redslovesgames.tideborne.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DataResourceOwnershipTest {
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Path CANONICAL = DATA.resolve("tideborne");
    private static final Path LEGACY = DATA.resolve("tidebound_compatibility");
    private static final Set<String> ACCESSORIES = Set.of(
            "copper_leader.json",
            "diamond_leader.json",
            "gold_leader.json",
            "seafarers_hook.json",
            "shark_tooth_hook.json",
            "steel_leader.json",
            "swift_line.json",
            "tentacle_line.json");

    @Test
    void currentFlatDataLivesUnderTideborneWithoutChangingItemIdentity() throws IOException {
        Path bait = CANONICAL.resolve("bait/leviathan_bait.json");
        assertTrue(Files.isRegularFile(bait));
        assertHistoricalItemIdentity(bait);

        Path accessories = CANONICAL.resolve("rod_accessories");
        assertTrue(Files.isDirectory(accessories));
        try (var files = Files.list(accessories)) {
            assertEquals(
                    ACCESSORIES,
                    files.filter(Files::isRegularFile)
                            .map(path -> path.getFileName().toString())
                            .collect(Collectors.toSet()));
        }
        for (String name : ACCESSORIES) {
            assertHistoricalItemIdentity(accessories.resolve(name));
        }

        assertFalse(Files.exists(LEGACY.resolve("bait")));
        assertFalse(Files.exists(LEGACY.resolve("rod_accessories")));
    }

    @Test
    void externallyAddressableCompatibilityAndForeignNamespacesRemainInPlace() {
        for (Path path : List.of(
                LEGACY.resolve("recipe/leviathan_bait.json"),
                LEGACY.resolve("tags/item/shark_food.json"),
                DATA.resolve("tide_traits/recipe/anglers_satchel.json"),
                DATA.resolve("tide_traits/tags/item/mutation_excluded.json"),
                DATA.resolve("tide/tags/item/bait_items.json"),
                DATA.resolve("myths_of_the_sea/tags/item/hippocampus_food.json"))) {
            assertTrue(Files.exists(path), path.toString());
        }
    }

    private static void assertHistoricalItemIdentity(Path path) throws IOException {
        String json = Files.readString(path);
        assertTrue(
                json.contains("\"item\":\"tidebound_compatibility:")
                        || json.contains("\"item\": \"tidebound_compatibility:"),
                path + " must keep the persisted historical item identifier");
    }
}
