/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.journal.client;

import com.li64.tide.data.fishing.FishData;
import com.redslovesgames.tideborne.fishing.specimen.FishDescriptor;
import net.minecraft.util.Identifier;

public final class JournalRenderContext {
   private static final ThreadLocal<JournalRenderContext.Entry> CURRENT = new ThreadLocal<>();

   private JournalRenderContext() {
   }

   public static void begin(FishData var0) {
      if (var0 == null) {
         CURRENT.remove();
      } else {
         Identifier var1 = FishDescriptor.fromFishData(var0).canonicalSpeciesId();
         CURRENT.set(new JournalRenderContext.Entry(var1, var0));
      }
   }

   public static JournalRenderContext.Entry current() {
      return CURRENT.get();
   }

   public static void end() {
      CURRENT.remove();
   }

   public record Entry(Identifier speciesId, FishData data) {
   }
}
