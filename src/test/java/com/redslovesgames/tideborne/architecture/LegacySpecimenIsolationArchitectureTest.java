package com.redslovesgames.tideborne.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LegacySpecimenIsolationArchitectureTest {
   private static final Path SOURCE_ROOT = Path.of("src/main/java");
   private static final Path SPECIMEN_ROOT = SOURCE_ROOT.resolve("com/redslovesgames/tideborne/fishing/specimen");
   private static final Path LEGACY_ROOT = SPECIMEN_ROOT.resolve("legacy");
   private static final Path CANONICAL_SPECIMEN_DATA = SPECIMEN_ROOT.resolve("SpecimenData.java");
   private static final String LEGACY_PACKAGE = "com.redslovesgames.tideborne.fishing.specimen.legacy";
   private static final Set<String> LEGACY_MODEL_FILES = Set.of(
      "DeterministicValues.java",
      "FishMutation.java",
      "TraitAxesRuntime.java"
   );
   private static final List<Path> CANONICAL_RUNTIME_FILES = List.of(
      CANONICAL_SPECIMEN_DATA,
      SPECIMEN_ROOT.resolve("CanonicalSpecimenStorage.java"),
      SPECIMEN_ROOT.resolve("SpecimenGenerator.java"),
      SPECIMEN_ROOT.resolve("BodyTypeGenerator.java"),
      SPECIMEN_ROOT.resolve("ConditionGenerator.java"),
      SPECIMEN_ROOT.resolve("PigmentationGenerator.java"),
      SPECIMEN_ROOT.resolve("SpecimenQualityService.java"),
      SPECIMEN_ROOT.resolve("FishScoreV2Service.java"),
      SPECIMEN_ROOT.resolve("CanonicalCatchStateManager.java"),
      SPECIMEN_ROOT.resolve("TraitRandom.java")
   );
   private static final List<Path> CURRENT_PRESENTATION_FILES = List.of(
      SOURCE_ROOT.resolve("com/redslovesgames/tideborne/presentation/render/MutationRendering.java"),
      SOURCE_ROOT.resolve("com/redslovesgames/tideborne/presentation/render/MutationTextureCache.java")
   );

   @Test
   void canonicalSpecimenRuntimeCannotImportMutationEraModels() throws IOException {
      assertNoLegacyImports(CANONICAL_RUNTIME_FILES, "canonical specimen runtime");
   }

   @Test
   void currentSpecimenPresentationCannotImportMutationEraModels() throws IOException {
      assertNoLegacyImports(CURRENT_PRESENTATION_FILES, "current specimen presentation");
   }

   @Test
   void mutationEraModelTypesStayInsideExplicitLegacyBoundary() throws IOException {
      List<String> violations = new ArrayList<>();
      try (var sources = Files.walk(SOURCE_ROOT)) {
         sources.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(path -> {
               String fileName = path.getFileName().toString();
               if (LEGACY_MODEL_FILES.contains(fileName) && !path.startsWith(LEGACY_ROOT)) {
                  violations.add(path + " must live under " + LEGACY_ROOT);
               }
               if ("SpecimenData.java".equals(fileName)
                  && !path.equals(CANONICAL_SPECIMEN_DATA)
                  && !path.startsWith(LEGACY_ROOT)) {
                  violations.add(path + " is an unexpected duplicate specimen model");
               }
            });
      }

      if (!violations.isEmpty()) {
         fail("Legacy specimen ownership violations:\n" + String.join("\n", violations));
      }
   }

   private static void assertNoLegacyImports(List<Path> paths, String area) throws IOException {
      List<String> violations = new ArrayList<>();
      for (Path path : paths) {
         if (!Files.exists(path)) {
            continue;
         }
         if (Files.readString(path).contains(LEGACY_PACKAGE)) {
            violations.add(path.toString());
         }
      }

      if (!violations.isEmpty()) {
         fail(area + " must not import mutation-era specimen models:\n" + String.join("\n", violations));
      }
   }
}
