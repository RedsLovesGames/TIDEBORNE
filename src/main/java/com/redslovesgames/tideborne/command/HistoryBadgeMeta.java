/*
 * RECONSTRUCTED SOURCE BASELINE
 * Recovered from Tideborne 1.3.57 bytecode.
 * See docs/RECONSTRUCTION.md before changing behavior.
 */
package com.redslovesgames.tideborne.command;

import com.redslovesgames.tideteamjournal.StoredFishScoreStorage;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.nbt.NbtCompound;

public final class HistoryBadgeMeta {
   private static final ThreadLocal<NbtCompound> CURRENT = new ThreadLocal<>();
   private static final ThreadLocal<NbtCompound> LAST = new ThreadLocal<>();
   private static final ConcurrentMap<UUID, NbtCompound> EVENT_META = new ConcurrentHashMap<>();

   private HistoryBadgeMeta() {
   }

   public static void capture(NbtCompound tag) {
      if (tag != null) {
         CURRENT.set(extract(tag));
      }
   }

   public static void finish() {
      NbtCompound current = CURRENT.get();
      if (current != null) {
         LAST.set(current);
      }
      CURRENT.remove();
   }

   public static void bindCurrent(UUID id, String eventType) {
      if (!"REPAIR".equals(eventType) && id != null && !EVENT_META.containsKey(id)) {
         NbtCompound meta = CURRENT.get();
         if (meta == null) {
            meta = LAST.get();
         }
         if (meta != null) {
            EVENT_META.put(id, meta);
         }
      }
   }

   public static void register(UUID id, NbtCompound tag) {
      if (id != null && tag != null && hasAny(tag)) {
         EVENT_META.put(id, extract(tag));
      }
   }

   public static void write(UUID id, NbtCompound target) {
      if (id == null || target == null) {
         return;
      }
      NbtCompound meta = EVENT_META.get(id);
      if (meta == null) {
         return;
      }

      copyString(meta, target, "condition");
      if (!target.getString("condition").isEmpty()) {
         target.putString("mutation", target.getString("condition"));
      }
      copyString(meta, target, "body_type");
      copyString(meta, target, "pigmentation");
      copyString(meta, target, "quality");
      if (meta.contains("percentile", 99)) {
         target.putDouble("percentile", meta.getDouble("percentile"));
      }
      if (meta.contains("length", 99)) {
         target.putDouble("length", meta.getDouble("length"));
      }
      StoredFishScoreStorage.readCanonical(meta).ifPresent(score -> StoredFishScoreStorage.writeCanonical(target, score));
   }

   private static boolean hasAny(NbtCompound tag) {
      return !tag.getString("condition").isEmpty()
         || !tag.getString("mutation").isEmpty()
         || !tag.getString("body_type").isEmpty()
         || !tag.getString("pigmentation").isEmpty()
         || !tag.getString("quality").isEmpty()
         || tag.contains("percentile", 99)
         || tag.contains("length", 99)
         || StoredFishScoreStorage.readCanonical(tag).isPresent();
   }

   private static NbtCompound extract(NbtCompound source) {
      NbtCompound result = new NbtCompound();
      String condition = source.getString("condition");
      if (condition.isEmpty()) {
         condition = source.getString("mutation");
      }
      if (!condition.isEmpty()) {
         result.putString("condition", condition);
      }
      copyString(source, result, "body_type");
      copyString(source, result, "pigmentation");
      copyString(source, result, "quality");
      if (source.contains("percentile", 99)) {
         result.putDouble("percentile", source.getDouble("percentile"));
      }
      if (source.contains("length", 99)) {
         result.putDouble("length", source.getDouble("length"));
      }
      StoredFishScoreStorage.readCanonical(source).ifPresent(score -> StoredFishScoreStorage.writeCanonical(result, score));
      return result;
   }

   private static void copyString(NbtCompound source, NbtCompound target, String key) {
      String value = source.getString(key);
      if (!value.isEmpty()) {
         target.putString(key, value);
      }
   }
}
