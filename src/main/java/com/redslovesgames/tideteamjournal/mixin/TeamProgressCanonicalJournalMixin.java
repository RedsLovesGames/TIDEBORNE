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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps team journal score/catch capture on the finalized server canonical specimen. */
@Mixin(value = TeamProgressStore.class, remap = false)
abstract class TeamProgressCanonicalJournalMixin {
    @Shadow @Final private static ThreadLocal TIDEBORNE_CURRENT_FISH;

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
        if (StoredFishScoreStorage.migrateRoot(root) && !Boolean.TRUE.equals(callback.getReturnValue())) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "mergeTrackedDataOnce", at = @At("HEAD"))
    private static void tideborne$prepareStoredScoreMerge(
            NbtCompound targetRoot,
            NbtCompound sourceRoot,
            int historyLimit,
            CallbackInfo callback
    ) {
        StoredFishScoreStorage.migrateRoot(targetRoot);
        StoredFishScoreStorage.migrateRoot(sourceRoot);
    }

    @Inject(method = "readHistory", at = @At("HEAD"))
    private static void tideborne$migrateHistoryScores(NbtCompound root, CallbackInfoReturnable<?> callback) {
        StoredFishScoreStorage.migrateRoot(root);
    }

    @Inject(method = "tideborneRegisterContributorFishScore", at = @At("HEAD"))
    private static void tideborne$migrateContributorRead(java.util.UUID id, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
    }

    @Inject(method = "tideborneUpdateContributorFishScore", at = @At("HEAD"))
    private static void tideborne$migrateContributorWrite(NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
    }

    @Inject(method = "tideborneRegisterEventMeta", at = @At("HEAD"))
    private static void tideborne$migrateEventRead(TeamProgressStore.RecordEvent event, NbtCompound tag, CallbackInfo callback) {
        StoredFishScoreStorage.migrateLegacyScore(tag);
    }

    @Inject(method = "tideborneRecordCurrentTopFish", at = @At("HEAD"))
    private static void tideborne$migrateTopFishOrdering(NbtCompound root, java.util.UUID id, String name, CallbackInfo callback) {
        StoredFishScoreStorage.migrateRoot(root);
    }

    @Redirect(
            method = "mergeTrackedDataOnce",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalMergeRead(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
    }

    @Redirect(
            method = "mergeTrackedDataOnce",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putInt(Ljava/lang/String;I)V")
    )
    private static void tideborne$canonicalMergeWrite(NbtCompound tag, String key, int value) {
        putCanonicalInt(tag, key, value);
    }

    @Redirect(
            method = "tideborneUpdateContributorFishScore",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalContributorRead(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
    }

    @Redirect(
            method = "tideborneUpdateContributorFishScore",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putInt(Ljava/lang/String;I)V")
    )
    private static void tideborne$canonicalContributorWrite(NbtCompound tag, String key, int value) {
        putCanonicalInt(tag, key, value);
    }

    @Redirect(
            method = "tideborneRegisterContributorFishScore",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalContributorRegistration(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
    }

    @Redirect(
            method = "tideborneRegisterEventMeta",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getBoolean(Ljava/lang/String;)Z")
    )
    private static boolean tideborne$canonicalEventPresence(NbtCompound tag, String key) {
        if (StoredFishScoreStorage.LEGACY_SCORE_KEY.equals(key)) {
            return StoredFishScoreStorage.readCanonical(tag).isPresent();
        }
        return tag.getBoolean(key);
    }

    @Redirect(
            method = "tideborneRegisterEventMeta",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalEventRead(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
    }

    @Redirect(
            method = "tideborneEventFishScoreForWrite",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalEventFallback(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
    }

    @Redirect(
            method = "tideborneRecordCurrentTopFish",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getInt(Ljava/lang/String;)I")
    )
    private static int tideborne$canonicalTopFishRead(NbtCompound tag, String key) {
        return canonicalInt(tag, key);
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
        specimen.fishScore().ifPresent(score -> StoredFishScoreStorage.writeCanonical(tag, score));
        HistoryBadgeMeta.capture(tag);
    }

    @Inject(method = "tideborneClearCatch", at = @At("TAIL"))
    private static void tideborne$clearCanonicalJournalCatch(CallbackInfo callback) {
        TeamCanonicalJournalCapture.clear();
    }

    private static int canonicalInt(NbtCompound tag, String key) {
        if (StoredFishScoreStorage.LEGACY_SCORE_KEY.equals(key)) {
            return StoredFishScoreStorage.readCanonical(tag).orElse(0);
        }
        return tag.getInt(key);
    }

    /** Keep the historical field only as an output mirror; canonical storage is written first. */
    private static void putCanonicalInt(NbtCompound tag, String key, int value) {
        if (StoredFishScoreStorage.LEGACY_SCORE_KEY.equals(key)) {
            StoredFishScoreStorage.writeCanonical(tag, value);
            tag.putInt(StoredFishScoreStorage.LEGACY_SCORE_KEY, value);
            return;
        }
        tag.putInt(key, value);
    }

    private static String serialized(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
