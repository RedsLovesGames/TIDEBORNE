package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class FeatureCompatibilityOwnershipArchitectureTest {
    private static final Path PRODUCTION_ROOT = Path.of("src/main/java");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^package\\s+([\\w.]+);");
    private static final String COMPAT_ROOT = "com.redslovesgames.tideborne.compat";
    private static final Map<String, String> NATIVE_OWNERS = Map.ofEntries(
            Map.entry("SharkScentManager.java", "com.redslovesgames.tideborne.ecosystem"),
            Map.entry("SharkCatchLoss.java", "com.redslovesgames.tideborne.ecosystem"),
            Map.entry("ChumBucketItem.java", "com.redslovesgames.tideborne.ecosystem"),
            Map.entry("ChumProjectileEntity.java", "com.redslovesgames.tideborne.ecosystem"),
            Map.entry("SteelLeaderGearModifiers.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("LeviathanBaitFishing.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("LeviathanBaitHook.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("KujiraBoneFishingRodItem.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("LeaderAttachment.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("LeaderGearModifiers.java", "com.redslovesgames.tideborne.fishing.gear"),
            Map.entry("TideboundFishingHud.java", "com.redslovesgames.tideborne.presentation.client"),
            Map.entry("TideboundTooltips.java", "com.redslovesgames.tideborne.presentation.client"));

    @Test
    void compatibilityOwnershipContainsOnlyOptionalModCode() throws IOException {
        List<String> violations = new ArrayList<>();
        try (var paths = Files.walk(PRODUCTION_ROOT)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String text = Files.readString(source);
                Matcher matcher = PACKAGE.matcher(text);
                if (!matcher.find()) {
                    continue;
                }
                String declared = matcher.group(1);
                if ((declared.equals(COMPAT_ROOT) || declared.startsWith(COMPAT_ROOT + "."))
                        && !declared.startsWith(COMPAT_ROOT + ".apex")
                        && !declared.startsWith(COMPAT_ROOT + ".myths")) {
                    violations.add(source + " -> compatibility package is not Apex/Myths optional glue");
                }

                String expectedOwner = NATIVE_OWNERS.get(source.getFileName().toString());
                if (expectedOwner != null && !declared.equals(expectedOwner)) {
                    violations.add(source + " -> expected " + expectedOwner + " but declared " + declared);
                }

                if (text.contains("com.redslovesgames.tideborne.compat.TideboundCompatibility")) {
                    violations.add(source + " -> historical compatibility coordinator dependency");
                }
                if (text.contains("com.redslovesgames.tideborne.compat.apex.SharkScentManager")) {
                    violations.add(source + " -> native shark scent gameplay depends on Apex ownership");
                }
            }
        }
        if (!violations.isEmpty()) {
            fail("Feature ownership regressions: " + violations);
        }
    }
}
