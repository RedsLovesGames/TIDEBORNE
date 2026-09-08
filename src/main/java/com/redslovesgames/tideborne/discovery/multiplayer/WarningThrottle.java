/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.discovery.multiplayer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

final class WarningThrottle {
   static final long DEFAULT_INTERVAL_NANOS = 60000000000L;
   private final LongSupplier nanoTime;
   private final long intervalNanos;
   private final Set<String> once = ConcurrentHashMap.newKeySet();
   private final ConcurrentMap<String, Long> last = new ConcurrentHashMap<>();

   WarningThrottle() {
      this(System::nanoTime, 60000000000L);
   }

   WarningThrottle(LongSupplier nanoTime, long intervalNanos) {
      if (intervalNanos < 0L) {
         throw new IllegalArgumentException("intervalNanos must be non-negative");
      }

      this.nanoTime = nanoTime;
      this.intervalNanos = intervalNanos;
   }

   boolean once(String key) {
      return this.once.add(key);
   }

   boolean rateLimited(String key) {
      long now = this.nanoTime.getAsLong();
      boolean[] permitted = new boolean[1];
      this.last.compute(key, (ignored, previous) -> {
         if (previous != null && now - previous < this.intervalNanos && now - previous >= 0L) {
            return (Long)previous;
         }

         permitted[0] = true;
         return now;
      });
      return permitted[0];
   }
}
