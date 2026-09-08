package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class JavaPackageOwnershipArchitectureTest {
    private static final Path PRODUCTION_ROOT = Path.of("src/main/java");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^package\\s+([\\w.]+);");
    private static final List<String> FORBIDDEN = List.of(
            "com.redslovesgames.tidetraits",
            "com.redslovesgames.tideteamjournal",
            "com.redslovesgames.tideboundcompatibility",
            "com.redslovesgames.tideborne.fishing.v2");

    @Test
    void activeProductionJavaUsesUnifiedTideborneOwnership() throws IOException {
        List<String> violations = new ArrayList<>();
        try (var paths = Files.walk(PRODUCTION_ROOT)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String text = Files.readString(source);
                Matcher matcher = PACKAGE.matcher(text);
                if (!matcher.find()) {
                    continue;
                }
                String declared = matcher.group(1);
                for (String forbidden : FORBIDDEN) {
                    if (declared.equals(forbidden) || declared.startsWith(forbidden + ".")) {
                        violations.add(source + " -> " + declared);
                    }
                }
                if (source.toString().replace('\\', '/').contains("/tideborne/fishing/v2/")) {
                    violations.add(source + " -> legacy fishing/v2 source path");
                }
            }
        }
        if (!violations.isEmpty()) {
            fail("Forbidden Java ownership remains: " + violations);
        }
    }
}
