package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class TestSourceIntegrityArchitectureTest {
    private static final Pattern TYPE_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private|abstract|final|static|sealed|non-sealed)\\s+)*(?:class|interface|enum|record)\\s+[A-Za-z_$][A-Za-z0-9_$]*"
    );

    @Test
    void everyTestJavaSourceDeclaresAType() throws IOException {
        Path root = Path.of("src/test/java");
        assertTrue(Files.isDirectory(root), "Missing test source root: " + root);

        try (var files = Files.walk(root)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                assertTrue(
                        TYPE_DECLARATION.matcher(source).find(),
                        () -> "Test Java source contains no type declaration: " + file
                );
            }
        }
    }
}
