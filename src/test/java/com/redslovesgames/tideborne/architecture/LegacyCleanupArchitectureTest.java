package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redslovesgames.tidetraits.trait.TraitAxesRuntime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LegacyCleanupArchitectureTest {
    private static final Path TRAITS_MIXIN_CONFIG = Path.of("src/main/resources/tide_traits.mixins.json");
    private static final Path LEGACY_SCORE_MIXIN = Path.of(
            "src/main/java/com/redslovesgames/tidetraits/mixin/LegacyFishScoreCalculatorMixin.java");
    private static final Path INACTIVE_SATCHEL_MIXIN = Path.of(
            "src/main/java/com/redslovesgames/tidetraits/mixin/FishSatchelConversionMixin.java");

    @Test
    void legacyFishScoreCompatibilitySignaturesStayDisabledWithoutSelfMixin() throws IOException {
        String config = Files.readString(TRAITS_MIXIN_CONFIG);

        assertFalse(config.contains("LegacyFishScoreCalculatorMixin"));
        assertFalse(Files.exists(LEGACY_SCORE_MIXIN));
        assertEquals(-1.0, TraitAxesRuntime.score(null, 5, 99.0, 50.0, 40.0));
        assertEquals(-1.0, TraitAxesRuntime.scoreFromParts(99.0, 5, "scarred", "giant", 50.0, 40.0));
    }

    @Test
    void inactiveSatchelConversionMixinSourceStaysRemoved() {
        assertFalse(Files.exists(INACTIVE_SATCHEL_MIXIN));
    }
}
