package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNamespaces;
import com.redslovesgames.tideborne.migration.legacy.ids.LegacyNetworkIds;
import com.redslovesgames.tideborne.migration.legacy.ids.LegacyPersistenceIds;
import com.redslovesgames.tideborne.migration.legacy.ids.LegacyRegistryIds;
import com.redslovesgames.tideborne.migration.legacy.ids.LegacyServiceIds;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class HistoricalIdOwnershipTest {
    private static final Path MAIN_JAVA = Path.of("src/main/java");
    private static final Path LEGACY_IDS = Path.of(
            "src/main/java/com/redslovesgames/tideborne/migration/legacy/ids");

    @Test
    void exactHistoricalIdentifiersRemainFrozen() {
        assertEquals("tide_traits", LegacyNamespaces.TIDE_TRAITS);
        assertEquals("tide_team_journal", LegacyNamespaces.TIDE_TEAM_JOURNAL);
        assertEquals("tidebound_compatibility", LegacyNamespaces.TIDEBOUND_COMPATIBILITY);

        assertEquals("tide_traits:anglers_satchel", LegacyRegistryIds.ANGLERS_SATCHEL.toString());
        assertEquals("tide_traits:satchel_state", LegacyRegistryIds.SATCHEL_STATE.toString());
        assertEquals("tidebound_compatibility:steel_leader", LegacyRegistryIds.STEEL_LEADER.toString());
        assertEquals("tidebound_compatibility:chum_projectile", LegacyRegistryIds.CHUM_PROJECTILE.toString());

        assertEquals("tide_traits:discovery_sync", LegacyNetworkIds.DISCOVERY_SYNC.toString());
        assertEquals("tide_traits:anglers_satchel_request", LegacyNetworkIds.SATCHEL_REQUEST.toString());
        assertEquals("tide_team_journal:record_holders", LegacyNetworkIds.RECORD_HOLDERS.toString());
        assertEquals("tide_team_journal:team_data_request", LegacyNetworkIds.TEAM_DATA_REQUEST.toString());
        assertEquals("tidebound_compatibility:settings", LegacyNetworkIds.TIDEBOUND_SETTINGS.toString());
        assertEquals("tidebound_compatibility:shark_catch_loss", LegacyNetworkIds.SHARK_CATCH_LOSS.toString());

        assertEquals("tide_team_journal", LegacyPersistenceIds.TEAM_JOURNAL_ROOT);
        assertEquals("tide_team_journal_record_holders", LegacyPersistenceIds.JOURNAL_RECORD_HOLDERS);
        assertEquals("tide_traits.json", LegacyPersistenceIds.TRAITS_CONFIG);
        assertEquals("tidebound_compatibility-client.json", LegacyPersistenceIds.TIDEBOUND_CLIENT_CONFIG);

        assertEquals("tide_traits:mutation_textures", LegacyServiceIds.MUTATION_TEXTURES_RELOAD_LISTENER.toString());
        assertEquals("tide_traits:normal", LegacyServiceIds.NORMAL_MUTATION.toString());
    }

    @Test
    void allHistoricalComponentPathsRetainTheirSerializedNamespace() {
        List<String> paths = List.of(
                "mutation", "mutation_seed", "size_percentile", "protected", "body_type",
                "steel_leader_attached", "leader_tier", "specimen_species_id", "specimen_schema_version",
                "specimen_generation_version", "specimen_deterministic_seed", "specimen_base_percentile",
                "specimen_base_length", "specimen_final_length", "specimen_final_percentile",
                "specimen_percentile_definition", "specimen_body_type", "specimen_condition",
                "specimen_pigmentation", "specimen_quality", "specimen_perfect_catch",
                "specimen_raw_fish_score", "specimen_fish_score");
        for (String path : paths) {
            assertEquals("tide_traits:" + path, LegacyRegistryIds.tideTraitsComponent(path).toString());
        }
    }

    @Test
    void normalProductionCodeDoesNotOwnRawHistoricalNamespaceLiterals() throws IOException {
        try (var files = Files.walk(MAIN_JAVA)) {
            for (Path path : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                if (path.startsWith(LEGACY_IDS) || path.toString().contains("/fishing/gametest/")) {
                    continue;
                }
                String source = Files.readString(path);
                assertFalse(source.contains("\"tide_traits\""), () -> "raw tide_traits owner leak: " + path);
                assertFalse(source.contains("\"tide_team_journal\""), () -> "raw tide_team_journal owner leak: " + path);
                assertFalse(source.contains("\"tidebound_compatibility\""), () -> "raw tidebound_compatibility owner leak: " + path);
            }
        }
    }
}
