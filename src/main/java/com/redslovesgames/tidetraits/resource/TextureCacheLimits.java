/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tidetraits.resource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class TextureCacheLimits {
   public static final int PNG_HEADER_BYTES = 24;
   public static final int MAX_DIMENSION = 4096;
   public static final long MAX_PIXELS_PER_TEXTURE = 4194304L;
   public static final long MAX_RETAINED_BYTES = 67108864L;
   public static final int ESTIMATED_RETAINED_BYTES_PER_PIXEL = 8;
   private static final byte[] PNG_SIGNATURE = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
   private static final byte[] IHDR = new byte[]{73, 72, 68, 82};

   private TextureCacheLimits() {
   }

   public static TextureCacheLimits.Dimensions readPngDimensions(byte[] header) throws IOException {
      if (header == null || header.length < 24) {
         throw new IOException("Truncated PNG header");
      } else if (!Arrays.equals(PNG_SIGNATURE, Arrays.copyOfRange(header, 0, PNG_SIGNATURE.length))) {
         throw new IOException("Invalid PNG signature");
      } else if (!Arrays.equals(IHDR, Arrays.copyOfRange(header, 12, 16))) {
         throw new IOException("PNG does not begin with IHDR");
      } else {
         ByteBuffer values = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN);
         int ihdrLength = values.getInt(8);
         if (ihdrLength != 13) {
            throw new IOException("Invalid PNG IHDR length: " + ihdrLength);
         } else {
            return new TextureCacheLimits.Dimensions(values.getInt(16), values.getInt(20));
         }
      }
   }

   public static boolean dimensionsAllowed(int width, int height) {
      return width > 0 && height > 0 && width <= 4096 && height <= 4096 ? (long)width * height <= 4194304L : false;
   }

   public static long estimatedRetainedBytes(int width, int height) {
      if (!dimensionsAllowed(width, height)) {
         throw new IllegalArgumentException("Unsafe texture dimensions: " + width + "x" + height);
      } else {
         return (long)width * height * 8L;
      }
   }

   public static boolean retainedBudgetExceeded(long retainedBytes) {
      return retainedBytes < 0L || retainedBytes > 67108864L;
   }

   public record Dimensions(int width, int height) {
   }
}
