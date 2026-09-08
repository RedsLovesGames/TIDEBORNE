package com.redslovesgames.tideborne.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class LeaderTextureParityTest {
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/tidebound_compatibility/textures/item");
    private static final Path MODELS = Path.of(
            "src/main/resources/assets/tidebound_compatibility/models/item");
    private static final List<String> MATERIAL_VARIANTS =
            List.of("copper_leader", "gold_leader", "diamond_leader");

    @Test
    void materialLeaderTexturesArePaletteSwapsOfTheExistingLeaderSilhouette() throws IOException {
        BufferedImage base = readTexture("steel_leader");

        for (String variant : MATERIAL_VARIANTS) {
            BufferedImage texture = readTexture(variant);
            assertEquals(base.getWidth(), texture.getWidth(), variant);
            assertEquals(base.getHeight(), texture.getHeight(), variant);

            boolean hasMaterialRecolor = false;
            for (int y = 0; y < base.getHeight(); y++) {
                for (int x = 0; x < base.getWidth(); x++) {
                    int baseArgb = base.getRGB(x, y);
                    int variantArgb = texture.getRGB(x, y);
                    assertEquals(
                            (baseArgb >>> 24) & 0xFF,
                            (variantArgb >>> 24) & 0xFF,
                            variant + " changed the leader silhouette at " + x + "," + y);
                    if (((baseArgb >>> 24) & 0xFF) != 0
                            && (baseArgb & 0x00FFFFFF) != (variantArgb & 0x00FFFFFF)) {
                        hasMaterialRecolor = true;
                    }
                }
            }
            assertTrue(hasMaterialRecolor, variant + " must be visibly recolored");
        }
    }

    @Test
    void materialLeaderModelsUseTheirOwnTextures() throws IOException {
        for (String variant : MATERIAL_VARIANTS) {
            String model = Files.readString(MODELS.resolve(variant + ".json"));
            assertTrue(
                    model.contains("\"layer0\":\"tidebound_compatibility:item/" + variant + "\""),
                    variant + " must not reuse steel_leader");
        }
    }

    private static BufferedImage readTexture(String name) throws IOException {
        BufferedImage image = ImageIO.read(TEXTURES.resolve(name + ".png").toFile());
        assertNotNull(image, name);
        return image;
    }
}
