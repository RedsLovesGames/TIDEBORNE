package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class MixinResourceOwnershipTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final List<String> RETIRED = List.of(
            "tide_traits.mixins.json",
            "tide_traits.client.mixins.json",
            "tide_team_journal.mixins.json",
            "tidebound_compatibility.mixins.json",
            "tidebound_compatibility.apex.mixins.json",
            "tide_team_journal.refmap.json",
            "tidebound_compatibility.refmap.json",
            "tideborne.refmap.json"
    );

    @Test
    void activeSourceMixinMetadataUsesTideborneOwnership() {
        assertTrue(Files.isRegularFile(RESOURCES.resolve("tideborne.mixins.json")));
        assertTrue(Files.isRegularFile(RESOURCES.resolve("tideborne.client.mixins.json")));
        assertTrue(Files.isRegularFile(RESOURCES.resolve("tideborne.apex.mixins.json")));
        assertTrue(Files.isRegularFile(RESOURCES.resolve("tideborne.apex.refmap.json")));
        for (String retired : RETIRED) {
            assertFalse(Files.exists(RESOURCES.resolve(retired)), retired);
        }
    }

    @Test
    void sourceCommonClientAndOptionalFailureSemanticsStaySeparated() throws IOException {
        String common = Files.readString(RESOURCES.resolve("tideborne.mixins.json"));
        String client = Files.readString(RESOURCES.resolve("tideborne.client.mixins.json"));
        String apex = Files.readString(RESOURCES.resolve("tideborne.apex.mixins.json"));

        assertTrue(common.contains("\"required\": true"));
        assertTrue(common.contains("\"defaultRequire\": 1"));
        assertFalse(common.contains("\"refmap\""));
        assertTrue(common.contains("\"specimen.AnglersSatchelRecipeMixin\""));
        assertTrue(common.contains("\"journal.TeamProgressCanonicalJournalMixin\""));
        assertTrue(common.contains("\"tide.AnglingTableLeaderMixin\""));
        assertTrue(common.contains("\"journal.client.FishingJournalMixin\""));
        assertTrue(common.contains("\"tide.AnglingTableScreenLeaderMixin\""));

        assertTrue(client.contains("\"required\": false"));
        assertTrue(client.contains("\"defaultRequire\": 0"));
        assertFalse(client.contains("\"refmap\""));
        assertTrue(client.contains("\"specimen.client.MinecraftMutationRenderingMixin\""));
        assertFalse(client.contains("journal.client."));
        assertFalse(client.contains("tide.AnglingTableScreenLeaderMixin"));

        assertTrue(apex.contains("\"required\": false"));
        assertTrue(apex.contains("\"defaultRequire\": 1"));
        assertTrue(apex.contains("\"plugin\": \"com.redslovesgames.tideborne.mixin.tide.OptionalCompatMixinPlugin\""));
        assertTrue(apex.contains("\"refmap\": \"tideborne.apex.refmap.json\""));
        assertTrue(apex.contains("\"GreatWhiteSharkMixin\""));
    }

    @Test
    void apexRefmapIsNarrowAndCurrent() throws IOException {
        String refmap = Files.readString(RESOURCES.resolve("tideborne.apex.refmap.json"));
        assertTrue(refmap.contains("com/redslovesgames/tideborne/mixin/compat/apex/GreatWhiteSharkMixin"));
        assertTrue(refmap.contains("Lcom/acorsicanfrog/apexwaters/entity/GreatWhiteSharkEntity;method_5959()V"));
        assertFalse(refmap.contains("com/redslovesgames/tideteamjournal/"));
        assertFalse(refmap.contains("com/redslovesgames/tideboundcompatibility/"));
        assertFalse(refmap.contains("AnglingTableLeaderMixin"));
    }

    @Test
    void fabricMetadataReferencesOnlyCanonicalConfigsAndKeepsCompatibilityProvides() throws IOException {
        String fabric = Files.readString(RESOURCES.resolve("fabric.mod.json"));
        assertTrue(fabric.contains("\"tideborne.mixins.json\""));
        assertTrue(fabric.contains("\"tideborne.client.mixins.json\""));
        assertTrue(fabric.contains("\"tideborne.apex.mixins.json\""));
        for (String retired : RETIRED) {
            assertFalse(fabric.contains(retired), retired);
        }
        assertTrue(fabric.contains("\"tide_traits\""));
        assertTrue(fabric.contains("\"tide_team_journal\""));
        assertTrue(fabric.contains("\"tidebound_compatibility\""));
    }
}
