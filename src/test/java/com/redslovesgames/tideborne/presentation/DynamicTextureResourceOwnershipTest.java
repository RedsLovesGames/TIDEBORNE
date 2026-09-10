package com.redslovesgames.tideborne.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DynamicTextureResourceOwnershipTest {
    private static final Path RENDERING = Path.of(
            "src/main/java/com/redslovesgames/tideborne/presentation/render/MutationRendering.java");
    private static final Path CACHE = Path.of(
            "src/main/java/com/redslovesgames/tideborne/presentation/render/MutationTextureCache.java");

    @Test
    void generatedMutationTexturesUseCurrentTideborneOwnership() throws IOException {
        String rendering = Files.readString(RENDERING);
        String cache = Files.readString(CACHE);

        assertTrue(rendering.contains(
                "return \"tideborne\".equals(texture.getNamespace()) && texture.getPath().startsWith(\"dynamic/mutation/\");"));
        assertTrue(cache.contains(
                "Identifier location = Identifier.of(\"tideborne\", \"dynamic/mutation/\""));
        assertFalse(rendering.contains(
                "\"tide_traits\".equals(texture.getNamespace()) && texture.getPath().startsWith(\"dynamic/mutation/\")"));
        assertFalse(cache.contains(
                "Identifier.of(\"tide_traits\", \"dynamic/mutation/\""));
    }

    @Test
    void externallyVisibleHistoricalReloadIdentityIsPreserved() throws IOException {
        String rendering = Files.readString(RENDERING);
        assertTrue(rendering.contains(
                "private static final Identifier RELOAD_LISTENER_ID = Identifier.of(\"tide_traits\", \"mutation_textures\");"));
    }
}
