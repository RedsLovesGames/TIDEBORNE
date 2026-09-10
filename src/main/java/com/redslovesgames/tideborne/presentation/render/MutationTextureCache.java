/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.presentation.render;

import com.redslovesgames.tideborne.presentation.resource.TextureCacheLimits;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.ColorHelper.Abgr;

@Environment(EnvType.CLIENT)
final class MutationTextureCache {
   private static final AtomicLong NEXT_TEXTURE_ID = new AtomicLong();
   private final int maximumSize;
   private final LinkedHashMap<MutationTextureCache.Key, MutationTextureCache.Entry> entries = new LinkedHashMap<>(16, 0.75F, true);
   private long retainedBytes;

   MutationTextureCache(int maximumSize) {
      if (maximumSize < 1) {
         throw new IllegalArgumentException("maximumSize must be positive");
      }

      this.maximumSize = maximumSize;
   }

   synchronized Identifier resolve(Identifier original, MutationTextureCache.Visual visual) {
      MutationTextureCache.Key key = new MutationTextureCache.Key(original, visual.effect(), visual.maskVariant(), visual.maskOffsetX(), visual.maskOffsetY());
      MutationTextureCache.Entry cached = this.entries.get(key);
      if (cached != null) {
         return cached.location();
      }

      MutationTextureCache.Entry generated;
      try {
         generated = this.generate(key);
      } catch (IOException | RuntimeException exception) {
         MutationRendering.RenderFailureLog.warn("texture:" + key, "Could not generate mutation texture; using the original", exception);
         generated = MutationTextureCache.Entry.fallback(original);
      }

      this.entries.put(key, generated);
      this.retainedBytes = this.retainedBytes + generated.retainedBytes();
      this.evictOverflow();
      return generated.location();
   }

   synchronized void clear() {
      TextureManager textureManager = textureManager();

      for (MutationTextureCache.Entry entry : this.entries.values()) {
         close(entry, textureManager);
      }

      this.entries.clear();
      this.retainedBytes = 0L;
   }

   private MutationTextureCache.Entry generate(MutationTextureCache.Key key) throws IOException {
      MinecraftClient minecraft = MinecraftClient.getInstance();
      if (minecraft == null) {
         throw new IOException("Minecraft client is not initialized");
      }

      ResourceManager resources = minecraft.getResourceManager();
      NativeImage generated = this.createImage(resources, key);
      NativeImageBackedTexture texture = null;

      try {
         texture = new NativeImageBackedTexture(generated);
         texture.setFilter(false, false);
         Identifier location = Identifier.of("tide_traits", "dynamic/mutation/" + Long.toUnsignedString(NEXT_TEXTURE_ID.incrementAndGet(), 36));
         minecraft.getTextureManager().registerTexture(location, texture);
         return MutationTextureCache.Entry.dynamic(
            location, texture, TextureCacheLimits.estimatedRetainedBytes(generated.getWidth(), generated.getHeight())
         );
      } catch (RuntimeException exception) {
         if (texture != null) {
            texture.close();
         } else {
            generated.close();
         }

         throw exception;
      }
   }

   private NativeImage createImage(ResourceManager resources, MutationTextureCache.Key key) throws IOException {
      NativeImage source = readImage(resources, key.original());

      NativeImage exception;
      try {
         Identifier maskLocation = maskLocation(key);
         NativeImage mask = maskLocation == null ? null : readImage(resources, maskLocation);

         try {
            NativeImage result = new NativeImage(source.getWidth(), source.getHeight(), false);

            try {
               transform(source, mask, result, key);
               exception = result;
            } catch (RuntimeException exceptionx) {
               result.close();
               throw exceptionx;
            }
         } catch (Throwable var11) {
            if (mask != null) {
               try {
                  mask.close();
               } catch (Throwable var9) {
                  var11.addSuppressed(var9);
               }
            }

            throw var11;
         }

         if (mask != null) {
            mask.close();
         }
      } catch (Throwable var12) {
         if (source != null) {
            try {
               source.close();
            } catch (Throwable var8) {
               var12.addSuppressed(var8);
            }
         }

         throw var12;
      }

      if (source != null) {
         source.close();
      }

      return exception;
   }

   private static NativeImage readImage(ResourceManager resources, Identifier location) throws IOException {
      try (
         InputStream raw = resources.open(location);
         BufferedInputStream stream = new BufferedInputStream(raw);
      ) {
         stream.mark(24);
         byte[] header = stream.readNBytes(24);
         stream.reset();
         TextureCacheLimits.Dimensions declared = TextureCacheLimits.readPngDimensions(header);
         validateDimensions(declared.width(), declared.height(), location);
         NativeImage image = NativeImage.read(stream);

         try {
            validateDimensions(image, location);
            if (image.getWidth() != declared.width() || image.getHeight() != declared.height()) {
               throw new IOException("Decoded texture dimensions disagree with PNG header for " + location);
            } else {
               return image;
            }
         } catch (IOException | RuntimeException exception) {
            image.close();
            throw exception;
         }
      }
   }

   private static void validateDimensions(NativeImage image, Identifier location) throws IOException {
      validateDimensions(image.getWidth(), image.getHeight(), location);
   }

   private static void validateDimensions(int width, int height, Identifier location) throws IOException {
      if (!TextureCacheLimits.dimensionsAllowed(width, height)) {
         throw new IOException("Unsafe texture dimensions for " + location + ": " + width + "x" + height);
      }
   }

   private static void transform(NativeImage source, NativeImage mask, NativeImage result, MutationTextureCache.Key key) {
      int width = source.getWidth();
      int height = source.getHeight();

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int sourcePixel = source.getColor(x, y);
            int alpha = Abgr.getAlpha(sourcePixel);
            if (alpha == 0) {
               result.setColor(x, y, 0);
            } else {
               int red = Abgr.getRed(sourcePixel);
               int green = Abgr.getGreen(sourcePixel);
               int blue = Abgr.getBlue(sourcePixel);
               double maskOpacity = maskCoverage(mask, x, y, width, height, key.maskOffsetX(), key.maskOffsetY());

               int transformed = switch (key.effect()) {
                  case ALBINO -> albino(alpha, red, green, blue);
                  case PERFECT_SPECIMEN -> perfect(alpha, red, green, blue);
                  case IRIDESCENT -> iridescent(alpha, red, green, blue, x, y, key.maskVariant(), maskOpacity);
                  case SCARRED -> overlay(alpha, red, green, blue, 92, 39, 34, maskOpacity * (alpha / 255.0) * 0.78);
                  case PARASITE_RIDDEN -> overlay(alpha, red, green, blue, 190, 224, 96, maskOpacity);
               };
               result.setColor(x, y, transformed);
            }
         }
      }
   }

   private static int albino(int alpha, int red, int green, int blue) {
      double luminance = luminance(red, green, blue);
      double desaturation = 0.86;
      double r = mix(red, luminance, desaturation) * 1.22 + 13.0;
      double g = mix(green, luminance, desaturation) * 1.16 + 11.0;
      double b = mix(blue, luminance, desaturation) * 1.09 + 10.0;
      r += 8.0;
      g += 3.0;
      return color(alpha, r, g, b);
   }

   private static int perfect(int alpha, int red, int green, int blue) {
      double luminance = luminance(red, green, blue);
      double saturation = 1.08;
      double r = luminance + (red - luminance) * saturation;
      double g = luminance + (green - luminance) * saturation;
      double b = luminance + (blue - luminance) * saturation;
      r = (r - 127.5) * 1.055 + 127.5;
      g = (g - 127.5) * 1.055 + 127.5;
      b = (b - 127.5) * 1.055 + 127.5;
      double highlight = Math.max(0.0, (luminance - 145.0) / 110.0);
      return color(alpha, r + 6.0 * highlight, g + 7.0 * highlight, b + 8.0 * highlight);
   }

   private static int iridescent(int alpha, int red, int green, int blue, int x, int y, int variant, double sparkleCoverage) {
      int phaseVariant = variant / 2;
      double phase = phaseVariant * 1.5707963267948966;
      double wave = 0.5 + 0.5 * Math.sin(x * 0.57 + y * 0.37 + phase);
      double targetRed = mix(76.0, 184.0, wave);
      double targetGreen = mix(205.0, 151.0, wave);
      double targetBlue = mix(224.0, 241.0, wave);
      double treatment = 0.15 + 0.07 * (luminance(red, green, blue) / 255.0);
      double r = mix(red, targetRed, treatment);
      double g = mix(green, targetGreen, treatment);
      double b = mix(blue, targetBlue, treatment);
      double sparkle = sparkleCoverage * 0.68;
      return color(alpha, mix(r, 232.0, sparkle), mix(g, 250.0, sparkle), mix(b, 255.0, sparkle));
   }

   private static int overlay(int alpha, int red, int green, int blue, int targetRed, int targetGreen, int targetBlue, double amount) {
      double bounded = Math.max(0.0, Math.min(1.0, amount));
      return color(alpha, mix(red, targetRed, bounded), mix(green, targetGreen, bounded), mix(blue, targetBlue, bounded));
   }

   private static int color(int alpha, double red, double green, double blue) {
      return Abgr.getAbgr(alpha, clamp(blue), clamp(green), clamp(red));
   }

   private static double maskCoverage(NativeImage mask, int x, int y, int width, int height, int offsetX, int offsetY) {
      if (mask == null) {
         return 0.0;
      }

      int maskX = x * mask.getWidth() / width - offsetX;
      int maskY = y * mask.getHeight() / height - offsetY;
      return maskX >= 0 && maskX < mask.getWidth() && maskY >= 0 && maskY < mask.getHeight()
         ? Abgr.getAlpha(mask.getColor(maskX, maskY)) / 255.0
         : 0.0;
   }

   private static Identifier maskLocation(MutationTextureCache.Key key) {
      String prefix;
      int variants;
      switch (key.effect()) {
         case IRIDESCENT:
            prefix = "sparkle";
            variants = 2;
            break;
         case SCARRED:
            prefix = "scar";
            variants = 4;
            break;
         case PARASITE_RIDDEN:
            prefix = "parasite";
            variants = 4;
            break;
         default:
            return null;
      }

      int index = Math.floorMod(key.maskVariant(), variants) + 1;
      return Identifier.of("tideborne", "textures/entity/traits/masks/" + prefix + "_0" + index + ".png");
   }

   private void evictOverflow() {
      TextureManager textureManager = textureManager();

      while (this.entries.size() > this.maximumSize || TextureCacheLimits.retainedBudgetExceeded(this.retainedBytes)) {
         Iterator<Map.Entry<MutationTextureCache.Key, MutationTextureCache.Entry>> iterator = this.entries.entrySet().iterator();
         MutationTextureCache.Entry eldest = iterator.next().getValue();
         iterator.remove();
         this.retainedBytes = Math.max(0L, this.retainedBytes - eldest.retainedBytes());
         close(eldest, textureManager);
      }
   }

   private static void close(MutationTextureCache.Entry entry, TextureManager textureManager) {
      if (entry.texture() != null) {
         try {
            if (textureManager != null) {
               textureManager.destroyTexture(entry.location());
            } else {
               entry.texture().close();
            }
         } catch (RuntimeException exception) {
            MutationRendering.RenderFailureLog.warn("texture-close:" + entry.location(), "Could not cleanly release a generated mutation texture", exception);

            try {
               entry.texture().close();
            } catch (RuntimeException var4) {
            }
         }
      }
   }

   private static TextureManager textureManager() {
      MinecraftClient minecraft = MinecraftClient.getInstance();
      return minecraft == null ? null : minecraft.getTextureManager();
   }

   private static int clamp(double value) {
      return (int)Math.round(Math.max(0.0, Math.min(255.0, value)));
   }

   private static double luminance(double red, double green, double blue) {
      return red * 0.2126 + green * 0.7152 + blue * 0.0722;
   }

   private static double mix(double from, double to, double amount) {
      return from + (to - from) * amount;
   }

   @Environment(EnvType.CLIENT)
   private record Entry(Identifier location, NativeImageBackedTexture texture, long retainedBytes) {
      Entry {
         if (retainedBytes < 0L) {
            throw new IllegalArgumentException("retainedBytes must be non-negative");
         }
      }

      static MutationTextureCache.Entry dynamic(Identifier location, NativeImageBackedTexture texture, long retainedBytes) {
         return new MutationTextureCache.Entry(location, texture, retainedBytes);
      }

      static MutationTextureCache.Entry fallback(Identifier original) {
         return new MutationTextureCache.Entry(original, null, 0L);
      }
   }

   enum VisualEffect {
      ALBINO(1),
      PERFECT_SPECIMEN(4),
      SCARRED(5),
      PARASITE_RIDDEN(6),
      IRIDESCENT(7);

      private final int historicalOrdinal;

      VisualEffect(int historicalOrdinal) {
         this.historicalOrdinal = historicalOrdinal;
      }

      int historicalOrdinal() {
         return this.historicalOrdinal;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Key(Identifier original, VisualEffect effect, int maskVariant, int maskOffsetX, int maskOffsetY) {
   }

   @Environment(EnvType.CLIENT)
   record Visual(VisualEffect effect, int maskVariant, int maskOffsetX, int maskOffsetY) {
   }
}
