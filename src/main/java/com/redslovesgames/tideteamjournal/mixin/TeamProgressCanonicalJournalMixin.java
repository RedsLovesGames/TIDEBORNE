package com.redslovesgames.tideteamjournal.mixin;

import com.redslovesgames.tideborne.command.HistoryBadgeMeta;
import com.redslovesgames.tideborne.fishing.v2.SpecimenData;
import com.redslovesgames.tideborne.fishing.v2.integration.CanonicalSpecimenStorage;
import com.redslovesgames.tideteamjournal.StoredFishScoreStorage;
import com.redslovesgames.tideteamjournal.TeamCanonicalJournalCapture;
import com.redslovesgames.tideteamjournal.TeamProgressStore;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps team journal score/catch capture on the finalized server canonical specimen. */
@Mixin(value = TeamProgressStore.class, remap = false)
abstract class TeamProgressCanonicalJournalMixin {
    @Shadow @Final private static ThreadLocal TIDEBORNE_CURRENT_FISH;
    @Shadow @Final private static ThreadLocal TIDEBORNE_LAST_FISH;

    /**
     * Shared FishScore consumer boundary used by Satchel/profile/item display code.
     * Canonical storage may migrate a readable legacy specimen once; an unreadable or scoreless
     * specimen stays scoreless instead of falling through to the reconstructed pre-V2 formula.
     */
    @Inject(method = "tideborneFishScore", at = @At("HEAD"), cancellable = true)
    private static void tideborne$canonicalScore(ItemStack stack, CallbackInfoReturnable<Double> callback) {
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        callback.setReturnValue(specimen != null && specimen.fishScore().isPresent()
                ? (double) specimen.fishScore().getAsInt()
                : -1.0);
    }

    @Inject(method = "ensureInitialized", at = @At("RETURN"), cancellable = true)
    private static void tideborne$migrateStoredScores(NbtCompound root, CallbackInfoReturnable<Boolean> callback) {
        boolean changed = StoredFishScoreStorage.migrateRoot(root);
        changed |= syncRootMirrors(root);
        if (changed && !Boolean.TRUE.equals(callback.getReturnValue())) {
            callback.setReturnValue(true);
        }
    }

    /**
     * The reconstructed TeamProgressStore still serializes the historical fish_score field.
     * Before its trusted merge runs, mirror canonical values into that compatibility field so
     * the merge compares the canonical score without any production-only Mixin redirects.
     */
    @Inject(method = "mergeTrackedDataOnce", at = @At("HEAD"))
    private static void tideborne$prepareStoredScoreMerge(
            NbtCompound targetRoot,
            NbtCompound sourceRoot,
            int historyLimit,
            CallbackInfo callback
    ) {
        StoredFishScoreStorage.migrateRoot(targetRoot);
        StoredFishScoreStorage.migrateRoot(sourceRoot);
        syncRootMirrors(targetRoot);
        syncRootMirrors(sourceRoot);
    }

    /** Promote only values produced by the trusted merge back into canonical storage. */
    @Inject(method = "mergeTrackedDataOnce", at = @At("RETURN"))
    private static void tideborne$finishStoredScoreMerge(
            NbtCompound targetRoot,
            NbtCompound sourceRoot,
            int historyLimit,
            CallbackInfo callback
    ) {
        promoteRootLegacyScores(targetRoot);
        syncRootMirrors(targetRoot);
    }

    @Inject(method = "readHistory", at = @At("HEAD"))
    private static void tideborne$migrateHistoryScores(NbtCompound root, CallbackInfoReturnable<?> callback) {
        StoredFishScoreStorage.migrateRoot(root);
        syncRootMirrors(root);
    }

    @Inject(method = "tideborneRegisterContributorFishScore", at = @At("HEAD"))
    private static void tideborne$migrateContributorRead(java.util.UUID id, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        syncLegacyMirror(tag);
    }

    @Inject(method = "tideborneUpdateContributorFishScore", at = @At("HEAD"))
    private static void tideborne$migrateContributorWrite(NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        StoredFishScoreStorage.updateBest(
                tag,
                StoredFishScoreStorage.highestCanonicalScore(threadLocalTag(TIDEBORNE_CURRENT_FISH), threadLocalTag(TIDEBORNE_LAST_FISH))
        );
        syncLegacyMirror(tag);
    }

    /** The original method has now compared and written only a trusted current-catch score. */
    @Inject(method = "tideborneUpdateContributorFishScore", at = @At("RETURN"))
    private static void tideborne$finishContributorWrite(NbtCompound tag, CallbackInfo callback) {
        promoteLegacyScore(tag);
        syncLegacyMirror(tag);
    }

    @Inject(method = "tideborneRegisterEventMeta", at = @At("HEAD"))
    private static void tideborne$migrateEventRead(TeamProgressStore.RecordEvent event, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
        syncLegacyMirror(tag);
    }

    @Inject(method = "tideborneEventFishScoreForWrite", at = @At("HEAD"))
    private static void tideborne$prepareEventFallback(TeamProgressStore.RecordEvent event, CallbackInfoReturnable<Integer> callback) {
        syncThreadLocalMirror(TIDEBORNE_LAST_FISH);
    }

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("HEAD"))
    private static void tideborne$migrateTopFishOrdering(NbtCompound root, java.util.UUID id, String name, CallbackInfo callback) {
        StoredFishScoreStorage.migrateRoot(root);
        syncRootMirrors(root);
        syncThreadLocalMirror(TIDEBORNE_CURRENT_FISH);
        syncThreadLocalMirror(TIDEBORNE_LAST_FISH);
    }

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("RETURN"))
    private static void tideborne$finishTopFishOrdering(NbtCompound root, java.util.UUID id, String name, CallbackInfo callback) {
        promoteRootLegacyScores(root);
        syncRootMirrors(root);
    }

    @Inject(method = "tideborneBeginCatch", at = @At("HEAD"))
    private static void tideborne$beginCanonicalJournalCatch(ItemStack stack, CallbackInfo callback) {
        TeamCanonicalJournalCapture.begin(stack);
    }

    /**
     * Enriches the existing server-owned temporary top-fish/history display tag from the finalized
     * canonical specimen. No client or UI code derives these values.
     */
    @Inject(method = "tideborneBeginCatch", at = @At("TAIL"))
    private static void tideborne$projectCanonicalRecordMetadata(ItemStack stack, CallbackInfo callback) {
        SpecimenData specimen = CanonicalSpecimenStorage.read(stack).orElse(null);
        Object current = TIDEBORNE_CURRENT_FISH.get();
        if (specimen == null || !(current instanceof NbtCompound tag)) {
            return;
        }

        tag.putDouble("length", specimen.finalLength());
        tag.putDouble("percentile", specimen.finalPercentile());
        tag.putString("body_type", serialized(specimen.bodyType()));
        tag.putString("condition", serialized(specimen.condition()));
        tag.putString("mutation", serialized(specimen.condition()));
        tag.putString("pigmentation", serialized(specimen.pigmentation()));
        tag.putString("quality", serialized(specimen.specimenQuality()));
        specimen.fishScore().ifPresent(score -> {
            StoredFishScoreStorage.writeCanonical(tag, score);
            tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, score);
        });
        HistoryBadgeMeta.capture(tag);
    }

    @Inject(method = "tideborneClearCatch", at = @At("TAIL"))
    private static void tideborne$clearCanonicalJournalCatch(CallbackInfo callback) {
        TeamCanonicalJournalCapture.clear();
    }

    private static void syncThreadLocalMirror(ThreadLocal threadLocal) {
        Object value = threadLocal.get();
        if (value instanceof NbtCompound tag) {
            syncLegacyMirror(tag);
        }
    }

    private static NbtCompound threadLocalTag(ThreadLocal threadLocal) {
        Object value = threadLocal.get();
        return value instanceof NbtCompound tag ? tag : null;
    }

    /** Compatibility output only. Canonical storage remains the source of truth. */
    private static boolean syncLegacyMirror(NbtCompound tag) {
        int canonical = StoredFishScoreStorage.readCanonical(tag).orElse(0);
        if (canonical <= 0 || tag.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY) == canonical) {
            return false;
        }
        tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, canonical);
        return true;
    }

    /**
     * Called only after a trusted TeamProgressStore operation has produced a compatibility value.
     * A lower/stale compatibility value is never allowed to replace canonical state.
     */
    private static boolean promoteLegacyScore(NbtCompound tag) {
        int legacy = tag.getInt(StoredFishScoreStorage.LEGACY_SCORE_KEY);
        int canonical = StoredFishScoreStorage.readCanonical(tag).orElse(0);
        if (legacy > canonical) {
            StoredFishScoreStorage.writeCanonical(tag, legacy);
            return true;
        }
        if (canonical > 0 && legacy != canonical) {
            tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, canonical);
            return true;
        }
        return false;
    }

    private static boolean syncRootMirrors(NbtCompound root) {
        if (root == null) {
            return false;
        }
        boolean changed = false;
        NbtCompound contributors = root.getCompound("contributors");
        for (String key : contributors.getKeys()) {
            if (contributors.contains(key, NbtElement.COMPOUND_TYPE)) {
                changed |= syncLegacyMirror(contributors.getCompound(key));
            }
        }
        changed |= syncListMirrors(root, "history");
        changed |= syncListMirrors(root, "top_fish");
        return changed;
    }

    private static boolean syncListMirrors(NbtCompound root, String key) {
        boolean changed = false;
        for (NbtElement element : root.getList(key, NbtElement.COMPOUND_TYPE)) {
            if (element instanceof NbtCompound tag) {
                changed |= syncLegacyMirror(tag);
            }
        }
        return changed;
    }

    private static boolean promoteRootLegacyScores(NbtCompound root) {
        if (root == null) {
            return false;
        }
        boolean changed = false;
        NbtCompound contributors = root.getCompound("contributors");
        for (String key : contributors.getKeys()) {
            if (contributors.contains(key, NbtElement.COMPOUND_TYPE)) {
                changed |= promoteLegacyScore(contributors.getCompound(key));
            }
        }
        changed |= promoteListLegacyScores(root, "history");
        changed |= promoteListLegacyScores(root, "top_fish");
        return changed;
    }

    private static boolean promoteListLegacyScores(NbtCompound root, String key) {
        boolean changed = false;
        for (NbtElement element : root.getList(key, NbtElement.COMPOUND_TYPE)) {
            if (element instanceof NbtCompound tag) {
                changed |= promoteLegacyScore(tag);
            }
        }
        return changed;
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
