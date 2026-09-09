package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class FabricEntrypointArchitectureTest {
    private static final Path PRODUCTION_ROOT = Path.of("src/main/java");
    private static final Path FABRIC_MOD_JSON = Path.of("src/main/resources/fabric.mod.json");
    private static final Map<String, String> GENUINE_ENTRYPOINTS = Map.of(
            "src/main/java/com/redslovesgames/tideborne/Tideborne.java", "ModInitializer",
            "src/main/java/com/redslovesgames/tideborne/client/TideborneClient.java", "ClientModInitializer");
    private static final Pattern IMPLEMENTS = Pattern.compile("\\bimplements\\s+([^\\{]+)\\{");
    private static final Pattern FABRIC_INITIALIZER_INTERFACE = Pattern.compile(
            "\\b(ModInitializer|ClientModInitializer|DedicatedServerModInitializer)\\b");
    private static final Pattern FABRIC_CALLBACK = Pattern.compile("\\b(onInitialize|onInitializeClient|onInitializeServer)\\s*\\(");

    @Test
    void onlyGenuineTideborneClassesBehaveAsFabricEntrypoints() throws IOException {
        List<String> violations = new ArrayList<>();
        try (var paths = Files.walk(PRODUCTION_ROOT)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String normalized = source.toString().replace('\\', '/');
                String text = Files.readString(source);
                String expectedInterface = GENUINE_ENTRYPOINTS.get(normalized);

                Matcher implementsMatcher = IMPLEMENTS.matcher(text);
                while (implementsMatcher.find()) {
                    Matcher initializerMatcher = FABRIC_INITIALIZER_INTERFACE.matcher(implementsMatcher.group(1));
                    while (initializerMatcher.find()) {
                        String initializer = initializerMatcher.group(1);
                        if (!initializer.equals(expectedInterface)) {
                            violations.add(normalized + " implements " + initializer);
                        }
                    }
                }

                Matcher callbackMatcher = FABRIC_CALLBACK.matcher(text);
                while (callbackMatcher.find()) {
                    String callback = callbackMatcher.group(1);
                    boolean allowed = normalized.equals("src/main/java/com/redslovesgames/tideborne/Tideborne.java")
                            ? callback.equals("onInitialize")
                            : normalized.equals("src/main/java/com/redslovesgames/tideborne/client/TideborneClient.java")
                                    && callback.equals("onInitializeClient");
                    if (!allowed) {
                        violations.add(normalized + " declares Fabric-style callback " + callback);
                    }
                }
            }
        }

        if (!violations.isEmpty()) {
            fail("Historical or duplicate Fabric initialization returned: " + violations);
        }
    }

    @Test
    void fabricMetadataNamesOnlyTheGenuineMainAndClientEntrypoints() throws IOException {
        String metadata = Files.readString(FABRIC_MOD_JSON);
        assertTrue(
                Pattern.compile("\\\"main\\\"\\s*:\\s*\\[\\s*\\\"com\\.redslovesgames\\.tideborne\\.Tideborne\\\"\\s*\\]")
                        .matcher(metadata)
                        .find(),
                "fabric.mod.json main entrypoint must remain Tideborne");
        assertTrue(
                Pattern.compile("\\\"client\\\"\\s*:\\s*\\[\\s*\\\"com\\.redslovesgames\\.tideborne\\.client\\.TideborneClient\\\"\\s*\\]")
                        .matcher(metadata)
                        .find(),
                "fabric.mod.json client entrypoint must remain TideborneClient");

        for (String historicalEntrypoint : List.of(
                "com.redslovesgames.tideborne.fishing.TideTraits\"",
                "com.redslovesgames.tideborne.journal.TideTeamJournal\"",
                "com.redslovesgames.tideborne.compat.TideboundCompatibility\"",
                "com.redslovesgames.tideborne.presentation.client.TideTraitsClient\"",
                "com.redslovesgames.tideborne.journal.client.TideTeamJournalClient\"",
                "com.redslovesgames.tideborne.presentation.client.TideboundCompatibilityClient\"")) {
            assertFalse(metadata.contains(historicalEntrypoint), "Historical entrypoint must not return: " + historicalEntrypoint);
        }
    }
}
